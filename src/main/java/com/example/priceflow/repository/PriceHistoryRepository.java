package com.example.priceflow.repository;

import com.example.priceflow.domain.PriceHistory;
import com.example.priceflow.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    /**
     * Busca todo o histórico de preço de um produto
     * Ordenado por data (mais recentes primeiro)
     *
     * @param product Produto
     * @return Lista de histórico de preços
     */
    List<PriceHistory> findByProductOrderByCapturedAtDesc(Product product);

    /**
     * Busca histórico de preço por URL do produto
     * Ordenado por data (mais recentes primeiro)
     *
     * @param url URL do produto
     * @return Lista de histórico de preços
     */
    @Query("SELECT ph FROM PriceHistory ph " +
           "WHERE ph.product.id.url = :url " +
           "ORDER BY ph.capturedAt DESC")
    List<PriceHistory> findByProductUrlOrderByDateDesc(@Param("url") String url);

    /**
     * Busca histórico de preço por URL e email do usuário
     * Ordenado por data (mais recentes primeiro)
     *
     * @param url URL do produto
     * @param email Email do usuário
     * @return Lista de histórico de preços
     */
    @Query("SELECT ph FROM PriceHistory ph " +
           "WHERE ph.product.id.url = :url AND ph.product.id.email = :email " +
           "ORDER BY ph.capturedAt DESC")
    List<PriceHistory> findByProductUrlAndEmailOrderByDateDesc(
            @Param("url") String url,
            @Param("email") String email
    );
}

