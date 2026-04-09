package com.example.priceflow.dto;

import java.util.List;

public record AllProductsHistoryResponseDTO(
        int totalProducts,
        List<ProductResponseDTO> products
) {}
