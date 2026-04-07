package com.example.priceflow.repository;

import com.example.priceflow.domain.Product;
import com.example.priceflow.domain.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, ProductId> {
}
