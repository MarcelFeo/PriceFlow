package com.example.priceflow.infrastructure.messaging;

public final class RabbitMQConstants {

    private RabbitMQConstants() {}

    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_ROUTING_KEY = "email.routingKey";
}