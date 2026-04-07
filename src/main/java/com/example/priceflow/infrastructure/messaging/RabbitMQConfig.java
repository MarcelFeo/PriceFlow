package com.example.priceflow.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.priceflow.infrastructure.messaging.RabbitMQConstants.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(EMAIL_ROUTING_KEY);
    }

}