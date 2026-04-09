package com.example.priceflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryResponseDTO(
        Long id,
        BigDecimal price,
        LocalDateTime capturedAt
) {}
