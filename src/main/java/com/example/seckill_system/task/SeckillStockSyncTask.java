package com.example.seckill_system.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.seckill_system.config.RabbitMQConfig;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SeckillStockSyncTask {
    
    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Autowired
    private ISeckillService seckillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    // 分布式锁 Key
    private static final String LOCK_KEY = "seckill:task:lock";
    // 锁过期时间 (防止任务挂了死锁)
    private static final long LOCK_EXPIRE = 10;

    /**
     * 库存同步任务
     * 策略：以 DB 为最终真理，重点修复 "Redis=0 但 DB>0" (少卖) 的情况
     */
    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行
    public void syncStock() {
        // 1. 【分布式锁】防止多实例并发执行
        // setIfAbsent = SETNX (原子操作)
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(LOCK_KEY, "LOCKED", LOCK_EXPIRE, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            log.debug("获取分布式锁失败，当前任务由其他实例执行中...");
            return;
        }

        try {
            // 检查 MQ 队列是否有积压 
            // 获取队列属性
            Properties queueProperties = amqpAdmin.getQueueProperties(RabbitMQConfig.QUEUE);
            int messageCount = 0;
            if (queueProperties != null) {
                // QUEUE_MESSAGE_COUNT 表示当前队列里还有多少条未消费的消息
                messageCount = (int) queueProperties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            }

            // 如果队列里还有消息，说明 DB 正处于“追赶”状态，数据肯定是不一致的
            // 此时绝对不能执行同步，否则会把 Redis 的正确数据覆盖成 DB 的旧数据
            if (messageCount > 0) {
                log.warn("发现MQ队列有积压消息({}条)，跳过本次库存同步，等待消费完成。", messageCount);
                return;
            }

            // ... (如果队列是空的，说明 DB 已经追平了，可以放心同步) ...
            log.info("MQ无积压, 开始执行库存对账任务...");

            // 2. 【范围控制】只查 "未结束" 的活动，避免全表扫描
            // 且 create_time 在近期 (例如7天内)，防止扫描很久以前的历史数据
            QueryWrapper<SeckillActivity> query = new QueryWrapper<>();
            query.gt("end_time", LocalDateTime.now());
            List<SeckillActivity> list = seckillActivityService.list(query);

            if (list.isEmpty()) return;

            for (SeckillActivity activity : list) {
                syncSingleActivity(activity);
            }

        } catch (Exception e) {
            log.error("库存同步任务异常", e);
        } finally {
            // 3. 【释放锁】
            stringRedisTemplate.delete(LOCK_KEY);
        }
    }

    /**
     * 单个活动同步逻辑
     */
    private void syncSingleActivity(SeckillActivity activity) {
        String redisKey = "seckill:stock:" + activity.getId();
        String redisStockStr = stringRedisTemplate.opsForValue().get(redisKey);

        // 如果 Redis 里没这个 Key，说明还没预热，或者已经过期，直接跳过，不要同步
        if (!StringUtils.hasText(redisStockStr)) return;

        long redisStock = Long.parseLong(redisStockStr);
        Integer dbStock = activity.getAvailableStock();

        // 4. 【核心对账逻辑】
        
        // 情况 A: Redis > DB
        // 现象：Redis 还有 10 个，DB 只有 8 个。
        // 原因：可能是 DB 刚扣减完，Redis 还没来得及同步（极少见，因为我们是先扣 Redis）。
        // 处理：理论上不应该发生。如果发生了，说明 Redis 数据脏了，会有超卖风险。
        // 动作：强制覆盖 Redis。
        if (redisStock > dbStock) {
            log.warn("检测到超卖风险(Redis > DB)! ActivityId: {}, Redis: {}, DB: {}", activity.getId(), redisStock, dbStock);
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));
        }

        // 情况 B: Redis < DB
        // 现象：Redis 显示 0 (卖完了)，DB 还有 5 个。
        // 原因：RabbitMQ 消息丢了，或者消费者执行失败且没回滚成功。导致产生“少卖”。
        // 处理：这是定时任务最需要修复的核心场景！
        // 动作：回补 Redis 库存。
        if (redisStock < dbStock) {
            // 设定一个阈值，比如差异超过 1 个才同步，避免高并发下的瞬间时间差误判
            // 但如果 Redis 已经是 0 了，必须立刻同步，否则用户买不了了
            if (redisStock == 0 || (dbStock - redisStock > 1)) {
                // 修正 Redis 库存
                stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(dbStock));

                // 【重要】既然回补了库存，如果有“售罄标记 Key”，也得删掉
                seckillService.removeLocalOverMap(activity.getId());
                log.info("检测到少卖(Redis < DB), 正在回补。ActivityId: {}, Redis: {}, DB: {}", activity.getId(), redisStock, dbStock);
            }
        }
    }
}
