package com.example.seckill_system.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Seckill Order Entity - Association Table
 * 
 * <p>This entity represents the association between a user, an activity, and an order.
 * It serves as a bridge table that links the seckill activity to the actual order
 * created during the seckill process.</p>
 * 
 * <p><b>Database Design</b>:</p>
 * <p>This table has a unique composite index on {@code (activity_id, user_id)} to enforce
 * the business rule: "Each user can only purchase one item per activity." This prevents
 * duplicate orders even if the same user makes multiple seckill requests.</p>
 * 
 * <p><b>Relationship</b>:</p>
 * <pre>{@code
 * SeckillOrder (1) ──→ (1) OrderInfo
 * SeckillOrder (N) ──→ (1) SeckillActivity
 * SeckillOrder (N) ──→ (1) User (implicit, via userId)
 * }</pre>
 * 
 * <p><b>Idempotency</b>:</p>
 * <p>The unique index {@code uk_activity_user} ensures that if a user attempts to create
 * multiple orders for the same activity (e.g., due to MQ message duplication), only the
 * first order will succeed. Subsequent attempts will throw {@link org.springframework.dao.DuplicateKeyException},
 * which is handled gracefully by the MQ consumer.</p>
 * 
 * <p><b>Usage in Seckill Flow</b>:</p>
 * <ol>
 *   <li>User successfully reduces stock in Redis (via Lua script)</li>
 *   <li>Message is sent to RabbitMQ with userId and activityId</li>
 *   <li>MQ consumer creates {@link OrderInfo} first</li>
 *   <li>MQ consumer creates this {@code SeckillOrder} to link them</li>
 *   <li>If duplicate, unique index prevents second order creation</li>
 * </ol>
 * 
 * @author Ai Yuyang
 * @see OrderInfo
 * @see SeckillActivity
 * @see com.example.seckill_system.service.ISeckillOrderService
 */
@Data
@TableName("seckill_order")
public class SeckillOrder {
    
    /**
     * Primary key, auto-incremented by database.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Reference to the seckill activity this order belongs to.
     * Foreign key to {@code seckill_activity} table.
     */
    private Long activityId;

    /**
     * ID of the user who placed this order.
     * Used in the unique composite index {@code uk_activity_user} to enforce
     * the "one order per user per activity" rule.
     */
    private Long userId;

    /**
     * Reference to the actual order record.
     * Foreign key to {@code order_info} table, which contains order details
     * such as price, status, and creation time.
     */
    private Long orderId;

    /**
     * Timestamp when this seckill order association was created.
     * Typically the same as {@link OrderInfo#createTime}, but stored separately
     * for audit purposes.
     */
    private LocalDateTime createTime;
}
