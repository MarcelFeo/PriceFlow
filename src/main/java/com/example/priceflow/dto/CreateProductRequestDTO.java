package com.example.priceflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequestDTO(
        @NotBlank(message = "URL do produto é obrigatória")
        String url,

        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email
) {}
