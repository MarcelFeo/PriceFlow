package com.example.priceflow.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_preco")
@Getter
@Setter
@NoArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal price;
    private LocalDateTime capturedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    public PriceHistory(Product product, BigDecimal price) {
        this.product = product;
        this.price = price;
        this.capturedAt = LocalDateTime.now();
    }

}
