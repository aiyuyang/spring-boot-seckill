package com.example.seckill_system.service;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill_system.entity.OrderInfo;

public interface ISeckillOrderService extends IService<OrderInfo> {

    Long createSeckillOrder(Long userId, Long activityId, Long productId, BigDecimal seckillPrice);
}
