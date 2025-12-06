package com.example.seckill_system.mq;

import javax.annotation.Resource;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.seckill_system.config.RabbitMQConfig;
import com.example.seckill_system.dto.SeckillMessage;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillOrderService;
import com.example.seckill_system.service.ISeckillService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class SeckillOrderListener {

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Autowired
    private ISeckillService seckillService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 静态单例 ObjectMapper，用于解析 Redis 里的 JSON
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
    }

    /**
     * 监听秒杀队列，执行“削峰”后的下单逻辑
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(SeckillMessage message) {
        // System.out.println("MQ接收消息: " + message);

        Long userId = message.getUserId();
        Long activityId = message.getActivityId();

        // 1. 获取活动信息 (商品ID、价格)
        // 优先查 Redis，性能最高
        SeckillActivity activity = getActivityFromCache(activityId);

        // 极度兜底：如果 Redis 也没查到（比如缓存刚好失效），去查数据库
        // 虽然在秒杀场景下 Redis 挂了基本就完了，但消费端要保证数据一致性
        if (activity == null) {
            activity = seckillActivityService.getById(activityId);
            if (activity == null) {
                System.err.println("严重错误: 秒杀活动不存在, ActivityId: " + activityId);
                return; // 丢弃消息，无法下单
            }
        }

        try {
            // 2. 执行落库 (写入 MySQL)
            // 这里传入真实的 ProductId 和 SeckillPrice
            Long orderId = seckillOrderService.createSeckillOrder(
                userId,
                activityId,
                activity.getProductId(),
                activity.getSeckillPrice()
            );
            // System.out.println(">>> 数据库下单成功 (User:" + userId + ", Order:" + orderId + ")");

        } catch (DuplicateKeyException e) {
            // 3. 【幂等性处理】核心！
            // 如果 MySQL 报唯一索引冲突 (uk_activity_user)，说明该用户已经创建过订单了。
            // 这种情况可能是 MQ 消息重复发送导致的，我们直接吞掉异常，视为“成功”，
            // 否则 MQ 会以为消费失败，无限重试，导致死循环。

            // System.out.println("重复下单，回补库存...");
            seckillService.recoverStock(activityId);

        } catch (Exception e) {
            // 4. 其他异常 (如数据库连接断开)
            // 抛出异常，让 RabbitMQ 进行重试 (默认重试3次)

            // System.err.println("下单失败 (" + e.getMessage() + ")，正在回补 Redis 库存...");
            seckillService.recoverStock(activityId);

            throw e;
        }
    }

    /**
     * 从 Redis 获取活动详情
     */
    private SeckillActivity getActivityFromCache(Long activityId) {
        String key = "seckill:activity:" + activityId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return mapper.readValue(json, SeckillActivity.class);
        } catch (Exception e) {
            // System.err.println("Redis JSON 解析失败: " + e.getMessage());
            return null;
        }
    }
}
