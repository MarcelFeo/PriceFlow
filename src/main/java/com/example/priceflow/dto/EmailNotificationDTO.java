package com.example.priceflow.dto;

import java.math.BigDecimal;

public record EmailNotificationDTO(
        String to,
        String productName,
        String productUrl,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        BigDecimal changePercent
) {}
