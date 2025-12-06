package com.example.seckill_system.service;

import com.example.seckill_system.vo.RespBean;
import javax.servlet.http.HttpServletResponse;

public interface ISeckillService {

    // 1. 活动预热
    void prepareSeckill(Long activityId);

    // 2. 获取图形验证码
    void getCaptcha(Long userId, Long activityId, HttpServletResponse response);

    // 3. 校验验证码 + 获取秒杀路径 (合二为一，原子操作)
    // 参数: 包含 verifyCode，因为 Service 内部要校验它
    RespBean getSeckillPath(Long userId, Long activityId, String verifyCode);

    // 4. 校验路径 + 执行秒杀 (合二为一，原子操作)
    // 参数: 包含 path，因为 Service 内部要校验它
    RespBean doSeckill(Long userId, Long activityId, String path);

    // 还原库存 (用于订单创建失败时的补偿)
    void recoverStock(Long activityId);

    // 校验验证码 (可选，如果 getSeckillPath 内部校验了，这里其实可以不暴露)
    boolean verifyCaptcha(Long userId, Long activityId, String code);
    
    // 校验路径 (可选)
    boolean checkPath(Long userId, Long activityId, String path);

    void removeLocalOverMap(Long activityId);
}