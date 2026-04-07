package com.example.priceflow.repository;

import com.example.priceflow.domain.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
}
