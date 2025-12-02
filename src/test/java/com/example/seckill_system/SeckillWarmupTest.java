package com.example.seckill_system;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.seckill_system.service.ISeckillActivityService;

@SpringBootTest
public class SeckillWarmupTest {
    
    @Autowired
    private ISeckillActivityService seckillActivityService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testWarmUpCache() {
        Long activityId = 1L;
        boolean result = seckillActivityService.warmUpCache(activityId);

        Assertions.assertTrue(result, "预热方法返回失败");

        String stockKey = "seckill:stock:" + activityId;
        String stockVal = stringRedisTemplate.opsForValue().get(stockKey);
        System.out.println("Redis 库存值：" + stockVal);

        Assertions.assertEquals("100", stockVal, "Redis中的库存数量不正确");

        String activityKey = "seckill:activity:" + activityId;
        String activityJson = stringRedisTemplate.opsForValue().get(activityKey);
        System.out.println("Redis 活动JSON: " + activityJson);
        
        Assertions.assertNotNull(activityJson, "Redis中缺少活动详情");
    }
}
