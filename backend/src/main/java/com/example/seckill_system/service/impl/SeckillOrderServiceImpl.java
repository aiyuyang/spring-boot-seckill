package com.example.seckill_system.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.seckill_system.dto.SeckillMessage;
import com.example.seckill_system.entity.OrderInfo;
import com.example.seckill_system.entity.SeckillActivity;
import com.example.seckill_system.entity.SeckillOrder;
import com.example.seckill_system.exception.GlobalException;
import com.example.seckill_system.mapper.OrderInfoMapper;
import com.example.seckill_system.mapper.SeckillActivityMapper;
import com.example.seckill_system.mapper.SeckillOrderMapper;
import com.example.seckill_system.service.ISeckillOrderService;
import com.example.seckill_system.vo.RespBeanEnum;

/**
 * Seckill Order Service Implementation - Order Creation Logic
 * 
 * <p>This service implements the core order creation logic that runs asynchronously
 * via RabbitMQ. It is called by {@link com.example.seckill_system.mq.SeckillOrderListener}
 * after a seckill request has successfully reduced Redis stock.</p>
 * 
 * <p><b>Transaction Management</b>:</p>
 * <p>All database operations are wrapped in a single transaction. If any step fails,
 * the entire transaction is rolled back, ensuring data consistency. This is critical
 * because we need to ensure that stock reduction and order creation happen atomically.</p>
 * 
 * <p><b>Order Creation Flow</b>:</p>
 * <ol>
 *   <li><b>Stock Reduction</b>: Atomically decrement database stock using SQL row lock</li>
 *   <li><b>Order Creation</b>: Create {@link OrderInfo} record with order details</li>
 *   <li><b>Association Creation</b>: Create {@link SeckillOrder} to link order with activity</li>
 * </ol>
 * 
 * <p><b>Stock Reduction Strategy</b>:</p>
 * <p>The stock is reduced using a conditional SQL update that leverages MySQL's row-level
 * locking. This ensures that even if multiple consumers process messages simultaneously,
 * only one can successfully reduce the stock.</p>
 * 
 * <p><b>Idempotency</b>:</p>
 * <p>The unique index {@code uk_activity_user} on the {@code seckill_order} table ensures
 * that duplicate orders are prevented. If the same message is processed multiple times,
 * the second attempt will throw {@link org.springframework.dao.DuplicateKeyException},
 * which is handled gracefully by the MQ consumer.</p>
 * 
 * <p><b>Data Consistency</b>:</p>
 * <p>This method serves as the final consistency check. If Redis allowed a stock reduction
 * but the database stock is already 0, this method detects the discrepancy and corrects
 * the Redis stock to maintain consistency.</p>
 * 
 * @author Ai Yuyang
 * @see ISeckillOrderService
 * @see com.example.seckill_system.mq.SeckillOrderListener
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements ISeckillOrderService {
    
    /**
     * Order info mapper for creating order records.
     */
    @Resource
    private OrderInfoMapper orderInfoMapper;

    /**
     * Seckill order mapper for creating order associations.
     */
    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    /**
     * Activity mapper for stock reduction operations.
     */
    @Resource
    private SeckillActivityMapper seckillActivityMapper;

    /**
     * Redis template for stock correction operations.
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Creates a seckill order for the specified user and activity.
     * 
     * <p>This is the core method that creates orders in the database. It performs
     * all operations within a single transaction to ensure atomicity.</p>
     * 
     * <p><b>Transaction Atomicity</b>:</p>
     * <p>All operations are wrapped in a {@code @Transactional} annotation. If any
     * step fails, the entire transaction is rolled back, including:</p>
     * <ul>
     *   <li>Stock reduction (if order creation fails)</li>
     *   <li>Order record (if association creation fails)</li>
     * </ul>
     * 
     * <p><b>Stock Reduction</b>:</p>
     * <p>The stock is reduced using a conditional SQL update:</p>
     * <pre>{@code
     * UPDATE seckill_activity 
     * SET available_stock = available_stock - 1 
     * WHERE id = ? AND available_stock > 0
     * }</pre>
     * <p>This leverages MySQL's row-level locking to prevent concurrent update conflicts.
     * The {@code available_stock > 0} condition ensures stock never goes negative.</p>
     * 
     * <p><b>Consistency Check</b>:</p>
     * <p>If the stock reduction fails (affected rows = 0), it means the database stock
     * is already 0, but Redis allowed the reduction. This indicates a data inconsistency.
     * The method corrects this by updating Redis stock to 0, then throws an exception.</p>
     * 
     * <p><b>Idempotency</b>:</p>
     * <p>The unique index {@code uk_activity_user} on the {@code seckill_order} table
     * ensures that if this method is called multiple times with the same userId and
     * activityId, only the first call succeeds. Subsequent calls will throw
     * {@link org.springframework.dao.DuplicateKeyException}, which triggers transaction
     * rollback and stock recovery.</p>
     * 
     * @param userId The user ID placing the order
     * @param activityId The activity ID being purchased
     * @param productId The product ID (from activity details, cached in Redis)
     * @param seckillPrice The seckill price (from activity details, cached in Redis)
     * @return The created order ID
     * @throws GlobalException if stock is insufficient (database stock is 0)
     * @throws org.springframework.dao.DuplicateKeyException if user already purchased (idempotency)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSeckillOrder(Long userId, Long activityId, Long productId, BigDecimal seckillPrice) {
        // Step 1: 【Core】Execute database stock deduction
        // Not query then set, but directly use SQL update, leverage database row lock to prevent concurrent conflicts
        // SQL logic: UPDATE seckill_activity SET available_stock = available_stock - 1 WHERE id = ? AND available_stock > 0
        // 
        // Why this approach?
        // - Atomic operation (single SQL statement)
        // - Leverages MySQL row-level locking (prevents concurrent updates)
        // - Conditional update (only succeeds if stock > 0)
        // - No need for explicit locking (MySQL handles it automatically)
        UpdateWrapper<SeckillActivity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("available_stock = available_stock - 1")
                    .eq("id", activityId)
                    .gt("available_stock", 0);  // Critical: Only update if stock > 0

        int updateCount = seckillActivityMapper.update(null, updateWrapper);

        // If affected rows < 1, stock is already 0, or activity doesn't exist
        // 【Core consistency check】
        if (updateCount < 1) {
            // This means: MySQL deduction failed, database actually has no stock (stock=0)
            // But reaching here means Redis just allowed it (Redis thinks stock > 0)
            // This indicates Redis data is higher than actual (dirty data).
            // 
            // This can happen if:
            // - Redis stock was not properly synchronized
            // - Database was updated by another process
            // - Previous transaction rolled back but Redis wasn't updated
            
            // 🚨 Force sync: Correct Redis stock to 0
            // Query database to confirm stock is actually 0
            SeckillActivity dbActivity = seckillActivityMapper.selectById(activityId);
            if (dbActivity != null && dbActivity.getAvailableStock() == 0) {
                // Database confirms stock is 0, correct Redis
                stringRedisTemplate.opsForValue().set("seckill:stock:" + activityId, "0");
            }

            // System.out.println("DB order creation failed: insufficient stock (ActivityId: " + activityId + ")");
            throw new GlobalException(RespBeanEnum.EMPTY_STOCK);
        }

        // Step 2: Create order detail (OrderInfo)
        // This record contains the core order information: user, product, price, status
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setUserId(userId);
        orderInfo.setProductId(productId);
        orderInfo.setOrderPrice(seckillPrice);
        orderInfo.setOrderStatus(0);  // 0 = Pending
        orderInfo.setCreateTime(LocalDateTime.now());

        orderInfoMapper.insert(orderInfo);

        // Step 3: Create seckill order association (SeckillOrder)
        // This links the order to the activity and enforces the "one-order-per-user" rule
        // 
        // If this step throws DuplicateKeyException (unique index conflict),
        // it means user has already placed an order, transaction will auto-rollback
        // (including first step's stock deduction will be added back due to transaction rollback)
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setActivityId(activityId);
        seckillOrder.setUserId(userId);
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setCreateTime(LocalDateTime.now());

        seckillOrderMapper.insert(seckillOrder);

        // Step 4: Return order ID
        // Transaction commits here if no exceptions occurred
        return orderInfo.getId();
    }
}
