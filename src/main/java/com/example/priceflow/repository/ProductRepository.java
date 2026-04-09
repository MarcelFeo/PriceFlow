package com.example.priceflow.repository;

import com.example.priceflow.domain.Product;
import com.example.priceflow.domain.ProductId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, ProductId> {

    /**
     * Busca todos os produtos por email
     * @param email Email do usuário
     * @return Lista de produtos do usuário
     */
    @Query("SELECT p FROM Product p WHERE p.id.email = :email")
    List<Product> findByEmail(@Param("email") String email);

    /**
     * Busca um produto específico por URL e email
     * @param url URL do produto
     * @param email Email do usuário
     * @return Produto encontrado
     */
    @Query("SELECT p FROM Product p WHERE p.id.url = :url AND p.id.email = :email")
    Optional<Product> findByUrlAndEmail(@Param("url") String url, @Param("email") String email);

    /**
     * Busca um produto por URL (ignora email)
     * @param url URL do produto
     * @return Produto encontrado
     */
    @Query("SELECT p FROM Product p WHERE p.id.url = :url")
    Optional<Product> findByUrl(@Param("url") String url);
}

