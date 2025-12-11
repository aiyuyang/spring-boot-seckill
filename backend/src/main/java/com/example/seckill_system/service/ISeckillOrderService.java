package com.example.seckill_system.service;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.seckill_system.entity.OrderInfo;

/**
 * Seckill Order Service Interface
 * 
 * <p>This interface extends MyBatis-Plus's {@link IService} and provides
 * specialized methods for creating seckill orders.</p>
 * 
 * <p><b>Core Method</b>:</p>
 * <p>The {@link #createSeckillOrder(Long, Long, Long, BigDecimal)} method is the
 * heart of the order creation process. It:</p>
 * <ol>
 *   <li>Atomically reduces database stock using SQL row lock</li>
 *   <li>Creates the order record ({@link OrderInfo})</li>
 *   <li>Creates the seckill order association ({@link com.example.seckill_system.entity.SeckillOrder})</li>
 *   <li>Ensures transaction atomicity (all or nothing)</li>
 * </ol>
 * 
 * <p><b>Transaction Management</b>:</p>
 * <p>The order creation is wrapped in a {@code @Transactional} annotation to ensure
 * that if any step fails, all database changes are rolled back, maintaining data consistency.</p>
 * 
 * <p><b>Idempotency</b>:</p>
 * <p>The unique index {@code uk_activity_user} on the {@code seckill_order} table ensures
 * that duplicate orders are prevented even if the same message is processed multiple times
 * (e.g., due to MQ retry).</p>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.service.impl.SeckillOrderServiceImpl
 * @see com.example.seckill_system.mq.SeckillOrderListener
 */
public interface ISeckillOrderService extends IService<OrderInfo> {

    /**
     * Creates a seckill order for the specified user and activity.
     * 
     * <p>This method performs the following operations in a single transaction:</p>
     * <ol>
     *   <li><b>Stock Reduction</b>: Atomically decrements database stock using SQL row lock</li>
     *   <li><b>Order Creation</b>: Creates {@link OrderInfo} record with order details</li>
     *   <li><b>Association Creation</b>: Creates {@link com.example.seckill_system.entity.SeckillOrder}
     *       to link order with activity (enforces one-order-per-user rule via unique index)</li>
     * </ol>
     * 
     * <p><b>Atomicity Guarantee</b>:</p>
     * <p>All operations are wrapped in a transaction. If any step fails (e.g., unique index
     * conflict, stock already zero), the entire transaction is rolled back, ensuring data
     * consistency.</p>
     * 
     * <p><b>Stock Reduction Strategy</b>:</p>
     * <p>The stock is reduced using a conditional SQL update:</p>
     * <pre>{@code
     * UPDATE seckill_activity 
     * SET available_stock = available_stock - 1 
     * WHERE id = ? AND available_stock > 0
     * }</pre>
     * <p>This leverages MySQL's row-level locking to prevent concurrent update conflicts.
     * The {@code available_stock > 0} condition ensures stock never goes negative.</p>
     * 
     * <p><b>Error Handling</b>:</p>
     * <ul>
     *   <li>If stock reduction fails (affected rows = 0), throws {@code EMPTY_STOCK} exception</li>
     *   <li>If unique index conflict occurs, throws {@link org.springframework.dao.DuplicateKeyException}</li>
     *   <li>Both cases trigger transaction rollback</li>
     * </ul>
     * 
     * @param userId The user ID placing the order
     * @param activityId The activity ID being purchased
     * @param productId The product ID (from activity details)
     * @param seckillPrice The seckill price (from activity details)
     * @return The created order ID
     * @throws com.example.seckill_system.exception.GlobalException if stock is insufficient
     * @throws org.springframework.dao.DuplicateKeyException if user already purchased (idempotency)
     */
    Long createSeckillOrder(Long userId, Long activityId, Long productId, BigDecimal seckillPrice);
}
