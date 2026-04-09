package com.example.priceflow.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDTO(
        String url,
        String email,
        String name,
        BigDecimal lastPrice,
        List<PriceHistoryResponseDTO> history
) {}
