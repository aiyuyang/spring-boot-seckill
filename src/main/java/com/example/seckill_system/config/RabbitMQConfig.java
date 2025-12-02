package com.example.seckill_system.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE = "seckill.order.queue";

    public static final String EXCHANGE = "seckill.exchange";

    public static final String ROUTING_KEY = "seckill.order.key";

    @Bean
    public Queue orderQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingOrder() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ROUTING_KEY);
    }

    /**
     * 使用 JSON 序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
