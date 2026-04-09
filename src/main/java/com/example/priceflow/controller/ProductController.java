package com.example.priceflow.controller;

import com.example.priceflow.domain.PriceHistory;
import com.example.priceflow.domain.Product;
import com.example.priceflow.domain.ProductId;
import com.example.priceflow.dto.AllProductsHistoryResponseDTO;
import com.example.priceflow.dto.CreateProductRequestDTO;
import com.example.priceflow.dto.PriceHistoryResponseDTO;
import com.example.priceflow.dto.ProductResponseDTO;
import com.example.priceflow.repository.PriceHistoryRepository;
import com.example.priceflow.repository.ProductRepository;
import com.example.priceflow.service.ScraperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ScraperService scraperService;

    /**
     * POST /api/products
     *
     * Registra um novo produto para monitoramento
     * Realiza web scraping para obter o preço inicial
     *
     * @param request DTO com URL do produto e email do usuário
     * @return Produto criado com o preço inicial
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody CreateProductRequestDTO request) {

        log.info("Registrando novo produto: URL={}, Email={}", request.url(), request.email());

        // Verifica se produto já existe
        var existingProduct = productRepository.findByUrlAndEmail(request.url(), request.email());
        if (existingProduct.isPresent()) {
            log.warn("Produto já existe para este usuário: {}", request.url());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        try {
            // Faz scraping para obter o preço atual
            BigDecimal currentPrice = scraperService.getPriceFromUrl(request.url());
            log.debug("Preço obtido via scraper: R$ {}", currentPrice);

            // Cria o novo produto
            ProductId productId = new ProductId(request.url(), request.email());
            Product product = new Product();
            product.setId(productId);
            product.setName(extractProductName(request.url()));
            product.setLastPrice(currentPrice);
            product.setHistory(List.of());

            // Salva no banco
            Product savedProduct = productRepository.save(product);
            log.info("Produto salvo com sucesso: {}", request.url());

            // Salva o primeiro preço no histórico
            PriceHistory priceHistory = new PriceHistory(savedProduct, currentPrice);
            priceHistoryRepository.save(priceHistory);
            log.debug("Histórico de preço salvo para: {}", request.url());

            // Retorna o produto criado
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toProductResponse(savedProduct));

        } catch (Exception e) {
            log.error("Erro ao registrar produto: {}", request.url(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * GET /api/products/history
     *
     * Retorna o histórico de preços de TODOS os produtos monitorados
     *
     * @return Lista de todos os produtos com seus históricos
     */
    @GetMapping("/history")
    public ResponseEntity<AllProductsHistoryResponseDTO> getAllProductsHistory() {
        log.info("Buscando histórico de todos os produtos");

        try {
            List<Product> allProducts = productRepository.findAll();

            List<ProductResponseDTO> productResponses = allProducts.stream()
                    .filter(product -> product != null &&
                            product.getId() != null &&
                            product.getId().getUrl() != null &&
                            !product.getId().getUrl().isEmpty())
                    .map(this::toProductResponse)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            AllProductsHistoryResponseDTO response = new AllProductsHistoryResponseDTO(
                    productResponses.size(),
                    productResponses
            );

            log.info("Total de produtos encontrados: {}", productResponses.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao buscar histórico de produtos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/products
     *
     * Remove um produto do monitoramento
     *
     * @param url URL do produto
     * @param email Email do usuário
     * @return Status 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(
            @RequestParam String url,
            @RequestParam String email) {

        log.info("Removendo produto: URL={}, Email={}", url, email);

        try {
            ProductId productId = new ProductId(url, email);
            if (!productRepository.existsById(productId)) {
                log.warn("Produto não encontrado para exclusão: {}", productId);
                return ResponseEntity.notFound().build();
            }

            productRepository.deleteById(productId);
            log.info("Produto removido com sucesso");
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Erro ao remover produto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============= MÉTODOS AUXILIARES =============

    /**
     * Converte uma entidade Product para ProductResponseDTO
     * Inclui o histórico de preços ordenado
     *
     * @param product Entidade Product
     * @return DTO de resposta
     */
    private ProductResponseDTO toProductResponse(Product product) {
        // Validação defensiva
        if (product == null || product.getId() == null) {
            log.warn("Produto ou ID nulo detectado");
            return null;
        }

        String url = product.getId().getUrl();
        String email = product.getId().getEmail();

        if (url == null || url.isEmpty() || email == null || email.isEmpty()) {
            log.warn("URL ou email vazio detectado para produto");
            return null;
        }

        List<PriceHistory> history = priceHistoryRepository
                .findByProductUrlAndEmailOrderByDateDesc(url, email);

        List<PriceHistoryResponseDTO> historyDto = history.stream()
                .map(ph -> new PriceHistoryResponseDTO(
                        ph.getId(),
                        ph.getPrice(),
                        ph.getCapturedAt()
                ))
                .collect(Collectors.toList());

        return new ProductResponseDTO(
                url,
                email,
                product.getName(),
                product.getLastPrice(),
                historyDto
        );
    }

    /**
     * Extrai um nome legível da URL do produto
     * Exemplo: https://www.amazon.com.br/dp/B07XLP9TPC -> "B07XLP9TPC"
     *
     * @param url URL do produto
     * @return Nome extraído
     */
    private String extractProductName(String url) {
        try {
            // Remove protocolos
            String clean = url.replaceAll("https?://", "");
            // Remove www.
            clean = clean.replaceAll("www\\.", "");
            // Remove domínio
            clean = clean.substring(clean.indexOf('/') + 1);
            // Pega os últimos caracteres significativos
            if (clean.length() > 50) {
                clean = clean.substring(0, 50) + "...";
            }
            return clean.isEmpty() ? url : clean;
        } catch (Exception e) {
            log.debug("Erro ao extrair nome da URL, usando URL completa", e);
            return url.length() > 100 ? url.substring(0, 100) + "..." : url;
        }
    }
}
