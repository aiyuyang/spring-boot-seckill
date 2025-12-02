package com.example.seckill_system.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill_system.config.RabbitMQConfig;
import com.example.seckill_system.dto.SeckillMessage;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.mapper.SeckillActivityMapper;
import com.example.seckill_system.service.ISeckillActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivity> implements ISeckillActivityService {
    @Resource 
    private StringRedisTemplate stringRedisTemplate;

    @Resource RabbitTemplate rabbitTemplate;

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
    }

    // 1. 【本地缓存标记】
    private final Map<Long, Boolean> localOverMap = new ConcurrentHashMap<>();
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/seckill.lua")));
        seckillScript.setResultType(Long.class);
    }

    @Override
    public boolean warmUpCache(Long activityId) {
        SeckillActivity activity = this.getById(activityId);

        if (activity == null || activity.getAvailableStock() <= 0) {
            return false;
        }

        // 计算过期时间（TTL）
        long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), activity.getEndTime());
        long bufferSeconds = 300L;
        long finalTtl = ttlSeconds > 0 ? ttlSeconds + bufferSeconds : bufferSeconds;

        try {
            // 缓存活动详情
            String activityJson = mapper.writeValueAsString(activity);
            String activityKey = "seckill:activity:" + activityId;
            stringRedisTemplate.opsForValue().set(activityKey, activityJson, Duration.ofSeconds(finalTtl));

            // 缓存库存
            String stockKey = "seckill:stock:" + activityId;
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getAvailableStock()), Duration.ofSeconds(finalTtl));

            // 初始化用户 Set
            String usersKey = "seckill:success_users:" + activityId;
            stringRedisTemplate.delete(usersKey);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        localOverMap.remove(activityId);
        return true;
    }

    @Override
    public boolean doSeckill(Long userId, Long activityId) {
        // 1. 【第0层拦截】JVM 内存标记判断
        // 如果本地 Map 里已经标记该活动结束，直接返回，连 Redis 都不用访问
        if (localOverMap.containsKey(activityId) && localOverMap.get(activityId)) {
            System.out.println("秒杀失败(ActivityId=" + activityId + "): [JVM拦截] 商品已售罄");
            return false;
        }

        // 2. 【第1层拦截】时间校验 (从 Redis 获取活动信息，反序列化)
        // 注意：这一步还是需要的，因为 Lua 脚本只负责扣库存，不负责校验时间
        // 为了性能，如果活动可以保证准时上下架，这一步其实也可以优化掉，但保留更安全
        String activityKey = "seckill:activity:" + activityId;
        String activityJson = stringRedisTemplate.opsForValue().get(activityKey);
        if (activityJson == null) {
            System.err.println("秒杀失败: 活动信息未预热");
            return false;
        }
        
        try {
            SeckillActivity activity = mapper.readValue(activityJson, SeckillActivity.class);
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
                System.out.println("秒杀失败: 不在活动时间范围内");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // 3. 【第2层拦截】执行 Lua 脚本 (原子性操作)
        // 准备 Keys
        List<String> keys = List.of(
                "seckill:success_users:" + activityId, // KEYS[1]
                "seckill:stock:" + activityId          // KEYS[2]
        );

        // 执行脚本
        // execute(script, keys, args...)
        Long result = stringRedisTemplate.execute(seckillScript, keys, String.valueOf(userId));

        // 4. 【结果处理】
        if (result != null) {
            if (result == -1) {
                System.out.println("秒杀失败(UserId=" + userId + "): 重复下单");
                return false;
            } else if (result == 0) {
                // *** 核心点 ***
                // Redis 明确告诉我们库存没了 (result=0)
                // 此时设置 JVM 本地标记，当前服务器之后的请求将全部在 Step 1 被拦截
                localOverMap.put(activityId, true);
                System.out.println("秒杀失败(ActivityId=" + activityId + "): 库存不足，已设置本地售罄标记");
                return false;
            } else if (result == 1) {
                // 5. 【异步下单】脚本执行成功，发送消息
                SeckillMessage message = new SeckillMessage(userId, activityId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
                System.out.println("秒杀成功(UserId=" + userId + "): 消息已发送");
                return true;
            }
        }

        return false;
    }
}
