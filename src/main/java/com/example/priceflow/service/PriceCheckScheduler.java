package com.example.priceflow.service;

import com.example.priceflow.domain.PriceHistory;
import com.example.priceflow.domain.Product;
import com.example.priceflow.dto.EmailNotificationDTO;
import com.example.priceflow.infrastructure.messaging.AlertPublisherService;
import com.example.priceflow.repository.PriceHistoryRepository;
import com.example.priceflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class PriceCheckScheduler {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;
    private final AlertPublisherService alertPublisherService;

    @Scheduled(cron = "0 0 */2 * * *")
    @Transactional
    public void checkPrices() {
        log.info("Iniciando verificação de preços dos produtos...");

        try {
            productRepository.findAll().forEach(product -> {
                try {
                    checkProductPrice(product);
                } catch (Exception e) {
                    log.error("Erro ao verificar preço do produto: {}. URL: {}",
                              product.getName(), product.getId().getUrl(), e);
                }
            });

            log.info("Verificação de preços concluída com sucesso");
        } catch (Exception e) {
            log.error("Erro durante a verificação geral de preços", e);
        }
    }

    /**
     * Verifica o preço de um produto específico
     *
     * @param product Produto a verificar
     */
    private void checkProductPrice(Product product) {
        String productUrl = product.getId().getUrl();
        String productEmail = product.getId().getEmail();

        log.debug("Verificando preço do produto: {} ({})", product.getName(), productUrl);

        try {
            // Obtém o preço atual via scraper
            BigDecimal currentPrice = scraperService.getPriceFromUrl(productUrl);

            if (currentPrice == null) {
                log.warn("Preço não encontrado para o produto: {}", product.getName());
                return;
            }

            // Salva o histórico de preço
            PriceHistory priceHistory = new PriceHistory(product, currentPrice);
            priceHistoryRepository.save(priceHistory);
            log.debug("Histórico de preço salvo para: {} - R$ {}", product.getName(), currentPrice);

            // Compara com o último preço armazenado
            if (product.getLastPrice() == null) {
                // Primeira verificação - apenas armazena o preço
                product.setLastPrice(currentPrice);
                productRepository.save(product);
                log.info("Primeiro preço registrado para: {} - R$ {}", product.getName(), currentPrice);
            } else if (currentPrice.compareTo(product.getLastPrice()) < 0) {
                // Preço diminuiu - envia notificação
                BigDecimal priceReduction = product.getLastPrice().subtract(currentPrice);
                BigDecimal changePercent = calculateChangePercent(product.getLastPrice(), currentPrice);

                log.info("Preço diminuiu para: {} - De R$ {} para R$ {} ({} %)",
                         product.getName(), product.getLastPrice(), currentPrice, changePercent);

                // Cria e envia notificação de alerta
                EmailNotificationDTO emailNotification = new EmailNotificationDTO(
                    productEmail,
                    product.getName(),
                    productUrl,
                    product.getLastPrice(),
                    currentPrice,
                    changePercent
                );

                alertPublisherService.publishPriceAlert(emailNotification);
                log.info("Notificação de alerta publicada para: {}", productEmail);

                // Atualiza o último preço armazenado
                product.setLastPrice(currentPrice);
                productRepository.save(product);
            } else if (currentPrice.compareTo(product.getLastPrice()) > 0) {
                // Preço aumentou - apenas log, sem notificação
                BigDecimal priceIncrease = currentPrice.subtract(product.getLastPrice());
                BigDecimal changePercent = calculateChangePercent(product.getLastPrice(), currentPrice);

                log.info("Preço aumentou para: {} - De R$ {} para R$ {} (+{} %)",
                         product.getName(), product.getLastPrice(), currentPrice, changePercent);

                // Atualiza o último preço armazenado
                product.setLastPrice(currentPrice);
                productRepository.save(product);
            } else {
                // Preço mantém-se igual
                log.debug("Preço mantém-se igual para: {} - R$ {}", product.getName(), currentPrice);
            }

        } catch (Exception e) {
            log.error("Erro ao processar preço para produto: {} ({})",
                     product.getName(), productUrl, e);
        }
    }

    /**
     * Calcula a percentagem de mudança entre dois preços
     *
     * @param oldPrice Preço anterior
     * @param newPrice Novo preço
     * @return Percentagem de mudança (negativo = redução, positivo = aumento)
     */
    private BigDecimal calculateChangePercent(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return newPrice.subtract(oldPrice)
                .multiply(new BigDecimal("100"))
                .divide(oldPrice, 2, RoundingMode.HALF_UP);
    }
}
