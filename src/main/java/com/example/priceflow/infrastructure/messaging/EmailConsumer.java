package com.example.priceflow.infrastructure.messaging;

import com.example.priceflow.dto.EmailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.example.priceflow.infrastructure.messaging.RabbitMQConstants.EMAIL_QUEUE;

@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = EMAIL_QUEUE)
    public void listen(EmailDto email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.to());
        message.setSubject(email.subject());
        message.setText(email.body());
        mailSender.send(message);
    }

}
