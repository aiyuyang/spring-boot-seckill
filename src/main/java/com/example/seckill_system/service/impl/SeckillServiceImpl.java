package com.example.seckill_system.service.impl;

import com.example.seckill_system.config.RabbitMQConfig;
import com.example.seckill_system.dto.SeckillMessage;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.exception.GlobalException;
import com.example.seckill_system.service.ISeckillActivityService;
import com.example.seckill_system.service.ISeckillService;
import com.example.seckill_system.vo.RespBean; 
import com.example.seckill_system.vo.RespBeanEnum; 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wf.captcha.ArithmeticCaptcha; 
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ISeckillActivityService seckillActivityService;

    private final Map<Long, Boolean> localOverMap = new ConcurrentHashMap<>();
    private DefaultRedisScript<Long> seckillScript;

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/seckill.lua")));
        seckillScript.setResultType(Long.class);
    }

    @Override
    public void prepareSeckill(Long activityId) {
        SeckillActivity activity = seckillActivityService.getById(activityId);
        if (activity == null || activity.getAvailableStock() <= 0) {
            throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
        }

        long ttlSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), activity.getEndTime());
        long bufferSeconds = 300L;
        long finalTtl = ttlSeconds > 0 ? ttlSeconds + bufferSeconds : bufferSeconds;

        try {
            String activityJson = mapper.writeValueAsString(activity);
            stringRedisTemplate.opsForValue().set("seckill:activity:" + activityId, activityJson, Duration.ofSeconds(finalTtl));
            stringRedisTemplate.opsForValue().set("seckill:stock:" + activityId, String.valueOf(activity.getAvailableStock()), Duration.ofSeconds(finalTtl));
            stringRedisTemplate.delete("seckill:success_users:" + activityId);
            localOverMap.remove(activityId);
        } catch (Exception e) {
            throw new GlobalException(RespBeanEnum.ERROR);
        }
    }

    // ✅ 修复：补充 getCaptcha 的具体实现
    @Override
    public void getCaptcha(Long userId, Long activityId, HttpServletResponse response) {
        if (userId == null || activityId == null) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 设置响应头
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成算术验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 2);
        
        // 存入 Redis
        String key = "seckill:captcha:" + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, captcha.text(), Duration.ofSeconds(300));

        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            System.err.println("验证码生成失败: " + e.getMessage());
        }
    }

    @Override
    public RespBean getSeckillPath(Long userId, Long activityId, String verifyCode) {
        // TODO 压测期间 暂时注释
        // 1. 校验验证码
        // boolean check = verifyCaptcha(userId, activityId, verifyCode);
        // if (!check) {
        //     return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        // }
        
        // 2. 生成 Path
        String str = UUID.randomUUID().toString().replace("-", "");
        String key = "seckill:path:" + activityId + ":" + userId;
        stringRedisTemplate.opsForValue().set(key, str, Duration.ofSeconds(60));
        
        return RespBean.success(str); // 返回字符串给 Controller
    }

    // 辅助方法，接口里可以不定义，仅内部调用
    public boolean verifyCaptcha(Long userId, Long activityId, String code) {
        if (code == null || code.isEmpty()) return false;
        String key = "seckill:captcha:" + activityId + ":" + userId;
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        boolean check = redisCode != null && redisCode.equals(code);
        if (check) {
            stringRedisTemplate.delete(key);
        }
        return check;
    }

    @Override
    public boolean checkPath(Long userId, Long activityId, String path) {
        if (path == null) return false;
        String key = "seckill:path:" + activityId + ":" + userId;
        String oldPath = stringRedisTemplate.opsForValue().get(key);
        return path.equals(oldPath);
    }

    @Override
    public RespBean doSeckill(Long userId, Long activityId, String path) {

        // 1. Service 内部校验 Path
        boolean check = checkPath(userId, activityId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 2. JVM 内存拦截
        if (localOverMap.containsKey(activityId) && localOverMap.get(activityId)) {
            throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
        }

        // 3. Redis 脚本扣减
        List<String> keys = List.of("seckill:success_users:" + activityId, "seckill:stock:" + activityId);
        Long result = stringRedisTemplate.execute(seckillScript, keys, String.valueOf(userId));

        if (result == -1) {
            throw new GlobalException(RespBeanEnum.REPEAT_ERROR);
        }
        if (result == 0) {
            localOverMap.put(activityId, true);
            throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
        }

        // 4. 发送消息
        SeckillMessage message = new SeckillMessage(userId, activityId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);

        return RespBean.success("排队中"); // 返回成功对象
    }

    @Override
    public void recoverStock(Long activityId) {
        String stockKey = "seckill:stock:" + activityId;
        // 使用 increment 原子加 1
        stringRedisTemplate.opsForValue().increment(stockKey);
        // 移除localOverMap，让请求再次进来
        localOverMap.remove(activityId);

        // System.out.println("库存回滚成功: ActivityId=" + activityId);
    }

    @Override
    public void removeLocalOverMap(Long activityId) {
        localOverMap.remove(activityId);
    }
}