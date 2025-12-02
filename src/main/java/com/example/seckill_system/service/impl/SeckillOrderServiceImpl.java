package com.example.seckill_system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill_system.dto.SeckillMessage;
import com.example.seckill_system.entity.OrderInfo;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.entity.SeckillOrder;
import com.example.seckill_system.mapper.OrderInfoMapper;
import com.example.seckill_system.mapper.SeckillActivityMapper;
import com.example.seckill_system.mapper.SeckillOrderMapper;
import com.example.seckill_system.service.ISeckillOrderService;

@Service
public class SeckillOrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements ISeckillOrderService {
    
    @Resource
    private OrderInfoMapper orderInfoMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    /**
     * 创建订单的核心业务
     * 事务控制：要么全部成功，要么全部回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(Long userId, Long activityId, Long productId, BigDecimal seckillPrice) {
        // 1. 【核心】执行数据库扣减库存
        // 并不是先查出来再 set，而是直接用 SQL 更新，利用数据库行锁防止并发冲突
        // SQL 逻辑: UPDATE seckill_activity SET available_stock = available_stock - 1 WHERE id = ? AND available_stock > 0
        UpdateWrapper<SeckillActivity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("available_stock = available_stock - 1")
                    .eq("id", activityId)
                    .gt("available_stock", 0);

        int updateCount = seckillActivityMapper.update(null, updateWrapper);

        // 如果影响行数 < 1，说明库存已经是0了，或者活动不存在
        if (updateCount < 1) {
            System.out.println("DB落单失败：库存不足 (ActivityId: " + activityId + ")");
            throw new RuntimeException("库存不足，数据库扣减失败");
        }

        // 2. 创建普通订单明细 (OrderInfo)
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setProductId(productId);
        orderInfo.setOrderPrice(seckillPrice);
        orderInfo.setOrderStatus(0);
        orderInfo.setCreateTime(LocalDateTime.now());

        orderInfoMapper.insert(orderInfo);

        // 3. 创建秒杀订单关联 (SeckillOrder)
        // 这一步如果报 DuplicateKeyException (唯一索引冲突)，
        // 说明该用户已经下过单了，事务会自动回滚（包括第一步扣减的库存也会加回去）
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setActivityId(activityId);
        seckillOrder.setUserId(userId);
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setCreateTime(LocalDateTime.now());

        seckillOrderMapper.insert(seckillOrder);

        // 4. 返回订单ID
        return orderInfo.getId();
    }
}
