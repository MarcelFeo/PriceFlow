package com.example.priceflow.infrastructure.messaging;

import com.example.priceflow.dto.EmailNotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertPublisherService {

    private final AmqpTemplate amqpTemplate;

    public void publishPriceAlert(EmailNotificationDTO dto) {
        amqpTemplate.convertAndSend(
                RabbitMQConstants.EMAIL_EXCHANGE,
                "price.alert.drop",
                dto
        );
    }
}
