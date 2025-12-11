package com.example.seckill_system.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Seckill Message DTO (Data Transfer Object)
 * 
 * <p>This DTO represents a message sent to RabbitMQ when a user successfully
 * reduces stock in Redis during a seckill operation. The message is consumed
 * asynchronously by {@link com.example.seckill_system.mq.SeckillOrderListener}
 * to create the actual order in the database.</p>
 * 
 * <p><b>Message Flow</b>:</p>
 * <ol>
 *   <li>User sends seckill request</li>
 *   <li>Redis Lua script atomically reduces stock</li>
 *   <li>If successful, this message is sent to RabbitMQ</li>
 *   <li>User receives "In queue" response immediately</li>
 *   <li>MQ consumer processes message and creates order</li>
 * </ol>
 * 
 * <p><b>Why Asynchronous?</b>:</p>
 * <p>Order creation involves database writes which are slow (typically 10-50ms).
 * By moving this to an asynchronous queue, the seckill endpoint can respond
 * in ~7ms, allowing the system to handle 12,000+ QPS. The database writes
 * happen in the background at a controlled rate (10 concurrent consumers).</p>
 * 
 * <p><b>Message Serialization</b>:</p>
 * <p>This class implements {@link Serializable} and is serialized to JSON
 * by RabbitMQ's {@link org.springframework.amqp.support.converter.Jackson2JsonMessageConverter}.
 * The JSON format is:</p>
 * <pre>{@code
 * {
 *   "userId": 1001,
 *   "activityId": 1
 * }
 * }</pre>
 * 
 * <p><b>Idempotency</b>:</p>
 * <p>If the same message is processed multiple times (e.g., due to MQ retry),
 * the unique index {@code uk_activity_user} in {@code seckill_order} table
 * prevents duplicate orders. The consumer handles {@link org.springframework.dao.DuplicateKeyException}
 * gracefully by recovering the Redis stock.</p>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.mq.SeckillOrderListener
 * @see com.example.seckill_system.config.RabbitMQConfig
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage implements Serializable {
    
    /**
     * ID of the user who successfully reduced stock.
     * Used by the MQ consumer to create the order for this user.
     */
    private Long userId;
    
    /**
     * ID of the seckill activity.
     * Used by the MQ consumer to:
     * <ul>
     *   <li>Query activity details (product ID, price)</li>
     *   <li>Reduce database stock</li>
     *   <li>Create seckill order association</li>
     * </ul>
     */
    private Long activityId;
}
