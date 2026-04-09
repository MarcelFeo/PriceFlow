package com.example.priceflow.infrastructure.email;

import com.example.priceflow.dto.EmailNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPriceAlertEmail(EmailNotificationDTO notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.to());
            helper.setSubject("PriceFlow - Alerta de Preço: " + notification.productName());
            helper.setFrom("noreply@priceflow.com");

            String htmlContent = buildEmailContent(notification);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de alerta de preço enviado para: {}", notification.to());
        } catch (MessagingException e) {
            log.error("Erro ao enviar email para: {}", notification.to(), e);
            throw new RuntimeException("Falha ao enviar email de notificação", e);
        }
    }

    private String buildEmailContent(EmailNotificationDTO notification) {
        String priceChangeColor = notification.changePercent().signum() < 0 ? "#28a745" : "#dc3545";
        String priceChangeSymbol = notification.changePercent().signum() < 0 ? "📉" : "📈";

        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "    <meta charset='UTF-8'>" +
                        "    <style>" +
                        "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                        "        .header { background-color: #007bff; color: white; padding: 20px; border-radius: 5px 5px 0 0; }" +
                        "        .content { background-color: #f8f9fa; padding: 20px; }" +
                        "        .price-info { margin: 20px 0; padding: 15px; background-color: white; border-left: 4px solid %s; }" +
                        "        .price-row { display: flex; justify-content: space-between; margin: 10px 0; }" +
                        "        .label { font-weight: bold; }" +
                        "        .old-price { text-decoration: line-through; color: #999; }" +
                        "        .new-price { font-size: 1.3em; color: %s; font-weight: bold; }" +
                        "        .change-percent { font-size: 1.2em; color: %s; font-weight: bold; }" +
                        "        .button { display: inline-block; background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 15px; }" +
                        "        .footer { background-color: #e9ecef; padding: 15px; text-align: center; font-size: 0.9em; color: #666; }" +
                        "    </style>" +
                        "</head>" +
                        "<body>" +
                        "    <div class='container'>" +
                        "        <div class='header'>" +
                        "            <h1>%s Alerta de Mudança de Preço!</h1>" +
                        "        </div>" +
                        "        <div class='content'>" +
                        "            <p>Olá!</p>" +
                        "            <p>O preço do produto que você está acompanhando mudou!</p>" +
                        "" +
                        "            <div class='price-info'>" +
                        "                <h2 style='margin-top: 0;'>%s</h2>" +
                        "                <div class='price-row'>" +
                        "                    <span class='label'>Preço Anterior:</span>" +
                        "                    <span class='old-price'>R$ %s</span>" +
                        "                </div>" +
                        "                <div class='price-row'>" +
                        "                    <span class='label'>Novo Preço:</span>" +
                        "                    <span class='new-price'>R$ %s</span>" +
                        "                </div>" +
                        "                <div class='price-row'>" +
                        "                    <span class='label'>Mudança:</span>" +
                        "                    <span class='change-percent'>%s %s%%</span>" +
                        "                </div>" +
                        "            </div>" +
                        "" +
                        "            <a href='%s' class='button'>Ver Produto</a>" +
                        "        </div>" +
                        "        <div class='footer'>" +
                        "            <p>Este é um email automático. Por favor, não responda este email.</p>" +
                        "            <p>&copy; 2024 PriceFlow. Todos os direitos reservados.</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>",
                priceChangeColor,
                priceChangeColor,
                priceChangeColor,
                priceChangeSymbol,
                notification.productName(),
                notification.productName(),
                notification.oldPrice(),
                notification.newPrice(),
                priceChangeSymbol,
                Math.abs(notification.changePercent().doubleValue()),
                notification.productUrl()
        );
    }
}
