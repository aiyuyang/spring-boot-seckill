package com.example.seckill_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill_system.entity.SeckillActivity;

public interface ISeckillActivityService extends IService<SeckillActivity>{
    boolean warmUpCache(Long activityId);

    boolean doSeckill(Long userId, Long activityId);
}
