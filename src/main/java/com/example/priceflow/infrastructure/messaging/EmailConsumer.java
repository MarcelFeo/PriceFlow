package com.example.priceflow.infrastructure.messaging;

import com.example.priceflow.dto.EmailNotificationDTO;
import com.example.priceflow.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConstants.EMAIL_QUEUE)
    public void consumeEmailNotification(EmailNotificationDTO notification) {
        log.info("Mensagem recebida da fila: {}", notification);
        try {
            emailService.sendPriceAlertEmail(notification);
        } catch (Exception e) {
            log.error("Erro ao processar notificação de email: {}", notification, e);
            throw e;
        }
    }
}
