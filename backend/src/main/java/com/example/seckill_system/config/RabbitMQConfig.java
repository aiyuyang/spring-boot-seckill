package com.example.seckill_system.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration - Message Queue Setup
 * 
 * <p>This configuration class sets up RabbitMQ components for asynchronous order processing.
 * It defines the exchange, queue, binding, and message converter used by the seckill system.</p>
 * 
 * <p><b>Message Queue Architecture</b>:</p>
 * <pre>{@code
 * Producer (SeckillServiceImpl)
 *     ↓
 * Exchange (seckill.exchange)
 *     ↓
 * Queue (seckill.order.queue)
 *     ↓
 * Consumer (SeckillOrderListener) - 10 concurrent consumers
 *     ↓
 * Database (Order Creation)
 * }</pre>
 * 
 * <p><b>Components</b>:</p>
 * <ul>
 *   <li><b>Exchange</b>: Direct exchange for routing messages to queues</li>
 *   <li><b>Queue</b>: Durable queue that persists messages to disk</li>
 *   <li><b>Binding</b>: Links exchange to queue with routing key</li>
 *   <li><b>Message Converter</b>: Converts Java objects to JSON for message serialization</li>
 * </ul>
 * 
 * <p><b>Durability</b>:</p>
 * <p>The queue is configured as durable ({@code true}), meaning it survives RabbitMQ
 * server restarts. This ensures messages are not lost if the server crashes.</p>
 * 
 * <p><b>Message Serialization</b>:</p>
 * <p>Messages are serialized to JSON using {@link Jackson2JsonMessageConverter}.
 * This allows complex Java objects (like {@link com.example.seckill_system.dto.SeckillMessage})
 * to be sent through the queue and deserialized by consumers.</p>
 * 
 * <p><b>Performance Configuration</b>:</p>
 * <p>The consumer concurrency is configured in {@code application.yml}:</p>
 * <ul>
 *   <li>10 concurrent consumers (process 10 messages simultaneously)</li>
 *   <li>Prefetch count: 1 (each consumer gets 1 message at a time for load balancing)</li>
 * </ul>
 * 
 * @author Ai Yuyang
 * @see com.example.seckill_system.mq.SeckillOrderListener
 * @see com.example.seckill_system.service.impl.SeckillServiceImpl
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * Queue name for seckill order messages.
     * Messages sent to this queue are consumed by {@link com.example.seckill_system.mq.SeckillOrderListener}.
     */
    public static final String QUEUE = "seckill.order.queue";

    /**
     * Exchange name for routing seckill messages.
     * A direct exchange routes messages to queues based on exact routing key matches.
     */
    public static final String EXCHANGE = "seckill.exchange";

    /**
     * Routing key for binding queue to exchange.
     * Messages with this routing key are delivered to the queue.
     */
    public static final String ROUTING_KEY = "seckill.order.key";

    /**
     * Creates a durable queue for seckill order messages.
     * 
     * <p><b>Durability</b>:</p>
     * <p>The queue is durable ({@code true}), meaning:</p>
     * <ul>
     *   <li>Queue metadata is stored on disk</li>
     *   <li>Queue survives RabbitMQ server restarts</li>
     *   <li>Messages in the queue are persisted (if message is persistent)</li>
     * </ul>
     * 
     * <p><b>Queue Behavior</b>:</p>
     * <ul>
     *   <li>Messages are delivered to consumers in FIFO order</li>
     *   <li>Multiple consumers can consume from the same queue (load balancing)</li>
     *   <li>Each message is delivered to only one consumer (no duplicates)</li>
     * </ul>
     * 
     * @return A durable queue bean
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE, true);  // true = durable
    }

    /**
     * Creates a direct exchange for routing seckill messages.
     * 
     * <p><b>Direct Exchange</b>:</p>
     * <p>A direct exchange routes messages to queues based on exact routing key matches.
     * This is the simplest and most efficient routing strategy for point-to-point messaging.</p>
     * 
     * <p><b>Routing Logic</b>:</p>
     * <p>When a message is published with routing key {@code seckill.order.key}, the exchange
     * delivers it to the queue bound with the same routing key.</p>
     * 
     * @return A direct exchange bean
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * Binds the queue to the exchange with the specified routing key.
     * 
     * <p>This binding creates the connection between the exchange and queue, enabling
     * message routing. Messages published to the exchange with the matching routing key
     * will be delivered to this queue.</p>
     * 
     * <p><b>Binding Relationship</b>:</p>
     * <pre>{@code
     * Exchange (seckill.exchange)
     *     ↓ (routing key: seckill.order.key)
     * Queue (seckill.order.queue)
     * }</pre>
     * 
     * @return A binding bean linking queue to exchange
     */
    @Bean
    public Binding bindingOrder() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ROUTING_KEY);
    }

    /**
     * Configures JSON message converter for RabbitMQ.
     * 
     * <p>This converter serializes Java objects to JSON when sending messages and
     * deserializes JSON back to Java objects when receiving messages. This allows
     * complex objects like {@link com.example.seckill_system.dto.SeckillMessage} to
     * be sent through the queue.</p>
     * 
     * <p><b>Serialization Format</b>:</p>
     * <p>Messages are serialized as JSON:</p>
     * <pre>{@code
     * {
     *   "userId": 1001,
     *   "activityId": 1
     * }
     * }</pre>
     * 
     * <p><b>Benefits</b>:</p>
     * <ul>
     *   <li>Human-readable message format (easier debugging)</li>
     *   <li>Language-agnostic (can be consumed by non-Java applications)</li>
     *   <li>Efficient serialization (faster than Java serialization)</li>
     * </ul>
     * 
     * @return A Jackson2JsonMessageConverter bean for JSON serialization
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
