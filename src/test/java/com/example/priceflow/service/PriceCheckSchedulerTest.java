package com.example.priceflow.service;

import com.example.priceflow.domain.PriceHistory;
import com.example.priceflow.domain.Product;
import com.example.priceflow.domain.ProductId;
import com.example.priceflow.dto.EmailNotificationDTO;
import com.example.priceflow.infrastructure.messaging.AlertPublisherService;
import com.example.priceflow.repository.PriceHistoryRepository;
import com.example.priceflow.repository.ProductRepository;
import org.junit.jupiter.api.beforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceCheckSchedulerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ScraperService scraperService;

    @Mock
    private AlertPublisherService alertPublisherService;

    @InjectMocks
    private PriceCheckScheduler priceCheckScheduler;

    private Product product;
    private ProductId productId;

    @beforeEach
    void setUp() {
        productId = new ProductId(
            "https://www.amazon.com.br/dp/B07EXAMPLE",
            "usuario@example.com"
        );
        
        product = new Product();
        product.setId(productId);
        product.setName("Notebook XYZ");
        product.setLastPrice(new BigDecimal("3000.00"));
        product.setHistory(Arrays.asList());
    }

    @Test
    void shouldNotifyWhenPriceLowers() throws Exception {
        // Arrange
        BigDecimal newPrice = new BigDecimal("2500.00");
        
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(newPrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        ArgumentCaptor<EmailNotificationDTO> emailCaptor = ArgumentCaptor.forClass(EmailNotificationDTO.class);
        verify(alertPublisherService).publishPriceAlert(emailCaptor.capture());
        
        EmailNotificationDTO sentEmail = emailCaptor.getValue();
        assertThat(sentEmail.to()).isEqualTo("usuario@example.com");
        assertThat(sentEmail.oldPrice()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(sentEmail.newPrice()).isEqualTo(new BigDecimal("2500.00"));
        assertThat(sentEmail.changePercent()).isLessThan(BigDecimal.ZERO);
    }

    @Test
    void shouldNotNotifyWhenPriceIncreases() throws Exception {
        // Arrange
        BigDecimal newPrice = new BigDecimal("3500.00");
        
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(newPrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService, never()).publishPriceAlert(any());
        verify(productRepository, times(2)).save(any()); // Uma para atualizar lastPrice
    }

    @Test
    void shouldNotNotifyWhenPriceRemainsTheSame() throws Exception {
        // Arrange
        BigDecimal samePrice = new BigDecimal("3000.00");
        
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(samePrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService, never()).publishPriceAlert(any());
        verify(productRepository, times(2)).save(any()); // Uma para atualizar lastPrice
    }

    @Test
    void shouldSaveFirstPriceWhenLastPriceIsNull() throws Exception {
        // Arrange
        product.setLastPrice(null);
        BigDecimal firstPrice = new BigDecimal("2000.00");
        
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(firstPrice);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService, never()).publishPriceAlert(any());
        verify(priceHistoryRepository).save(any(PriceHistory.class));
        verify(productRepository).save(any(Product.class));
        
        assertThat(product.getLastPrice()).isEqualTo(firstPrice);
    }

    @Test
    void shouldHandleExceptionsDuringPriceCheck() throws Exception {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(scraperService.getPriceFromUrl(anyString())).thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThatNoException().isThrownBy(() -> priceCheckScheduler.checkPrices());
        verify(alertPublisherService, never()).publishPriceAlert(any());
    }

    @Test
    void shouldProcessMultipleProducts() throws Exception {
        // Arrange
        Product product2 = new Product();
        ProductId productId2 = new ProductId(
            "https://www.mercadolivre.com.br/item/EXAMPLE",
            "usuario2@example.com"
        );
        product2.setId(productId2);
        product2.setName("Mouse Gamer");
        product2.setLastPrice(new BigDecimal("100.00"));

        BigDecimal price1 = new BigDecimal("2500.00");
        BigDecimal price2 = new BigDecimal("80.00");

        when(productRepository.findAll()).thenReturn(List.of(product, product2));
        when(scraperService.getPriceFromUrl("https://www.amazon.com.br/dp/B07EXAMPLE")).thenReturn(price1);
        when(scraperService.getPriceFromUrl("https://www.mercadolivre.com.br/item/EXAMPLE")).thenReturn(price2);
        when(priceHistoryRepository.save(any())).thenReturn(new PriceHistory());

        // Act
        priceCheckScheduler.checkPrices();

        // Assert
        verify(alertPublisherService, times(2)).publishPriceAlert(any());
    }
}
