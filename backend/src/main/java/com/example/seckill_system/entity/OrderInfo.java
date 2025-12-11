package com.example.seckill_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * Order Information Entity
 * 
 * <p>This entity represents a general order in the system. It contains the core
 * order information such as user, product, price, and status. In the seckill
 * context, this order is linked to a {@link SeckillOrder} to indicate it was
 * created through a seckill activity.</p>
 * 
 * <p><b>Order Lifecycle</b>:</p>
 * <ol>
 *   <li><b>Created</b>: Order is created with status 0 (pending)</li>
 *   <li><b>Processing</b>: Order is being processed (status 1)</li>
 *   <li><b>Completed</b>: Order is completed (status 2)</li>
 *   <li><b>Cancelled</b>: Order is cancelled (status -1)</li>
 * </ol>
 * 
 * <p><b>Relationship with Seckill</b>:</p>
 * <p>When an order is created through a seckill activity:</p>
 * <ol>
 *   <li>An {@code OrderInfo} record is created with the seckill price</li>
 *   <li>A {@link SeckillOrder} record is created to link this order to the activity</li>
 *   <li>The order can be queried by both {@code orderId} and {@code (userId, activityId)}</li>
 * </ol>
 * 
 * <p><b>Asynchronous Creation</b>:</p>
 * <p>Orders are created asynchronously via RabbitMQ to handle high concurrency:</p>
 * <ol>
 *   <li>User request reduces Redis stock (fast, ~7ms response time)</li>
 *   <li>Message is sent to RabbitMQ queue</li>
 *   <li>User receives "In queue" response immediately</li>
 *   <li>MQ consumer creates this order record in the background</li>
 *   <li>User polls for order result until it's created</li>
 * </ol>
 * 
 * @author Ai Yuyang
 * @see SeckillOrder
 * @see com.example.seckill_system.service.ISeckillOrderService
 */
@Data
@TableName("order_info")
public class OrderInfo {
    
    /**
     * Primary key, auto-incremented by database.
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ID of the user who placed this order.
     * Used for querying all orders belonging to a specific user.
     */
    private Long userId;

    /**
     * ID of the product being ordered.
     * Reference to the product catalog (not implemented in this system).
     */
    private Long productId;

    /**
     * Total price of this order.
     * For seckill orders, this equals the seckill price of the activity.
     * Stored as {@link BigDecimal} to ensure precise decimal calculations.
     */
    private BigDecimal orderPrice;

    /**
     * Current status of the order.
     * <ul>
     *   <li>0: Pending (刚创建)</li>
     *   <li>1: Processing (处理中)</li>
     *   <li>2: Completed (已完成)</li>
     *   <li>-1: Cancelled (已取消)</li>
     * </ul>
     */
    private Integer orderStatus;

    /**
     * Timestamp when this order was created.
     * Used for order history and sorting.
     */
    private LocalDateTime createTime;
}
