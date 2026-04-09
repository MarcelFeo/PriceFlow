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
                    .map(this::toProductResponse)
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
     * GET /api/products/{url}/history
     * 
     * Retorna o histórico de preços de um produto específico
     * 
     * @param url URL do produto (enviada como query parameter)
     * @return Histórico de preços do produto específico
     */
    @GetMapping("/history/details")
    public ResponseEntity<ProductResponseDTO> getProductHistory(
            @RequestParam String url) {
        
        log.info("Buscando histórico do produto: {}", url);
        
        try {
            // Busca o produto
            Product product = productRepository.findByUrl(url)
                    .orElseThrow(() -> {
                        log.warn("Produto não encontrado: {}", url);
                        return new IllegalArgumentException("Produto não encontrado");
                    });

            // Retorna com histórico carregado
            ProductResponseDTO response = toProductResponse(product);
            log.info("Histórico encontrado para produto: {} com {} registros", 
                    url, response.history().size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro ao buscar histórico do produto: {}", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/products/{url}/history?email={email}
     * 
     * Retorna o histórico de preços de um produto específico para um usuário específico
     * 
     * @param url URL do produto
     * @param email Email do usuário (opcional)
     * @return Histórico de preços do produto
     */
    @GetMapping("/history/user")
    public ResponseEntity<ProductResponseDTO> getProductHistoryByUser(
            @RequestParam String url,
            @RequestParam(required = false) String email) {
        
        log.info("Buscando histórico do produto: {} para email: {}", url, email);
        
        try {
            Product product;
            
            if (email != null && !email.isEmpty()) {
                product = productRepository.findByUrlAndEmail(url, email)
                        .orElseThrow(() -> {
                            log.warn("Produto não encontrado para URL: {} e Email: {}", url, email);
                            return new IllegalArgumentException("Produto não encontrado para este usuário");
                        });
            } else {
                product = productRepository.findByUrl(url)
                        .orElseThrow(() -> {
                            log.warn("Produto não encontrado: {}", url);
                            return new IllegalArgumentException("Produto não encontrado");
                        });
            }

            ProductResponseDTO response = toProductResponse(product);
            log.info("Histórico encontrado com {} registros", response.history().size());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro ao buscar histórico do produto", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/products/{id}
     * 
     * Retorna os detalhes de um produto específico
     * 
     * @param url URL do produto
     * @param email Email do usuário
     * @return Detalhes do produto
     */
    @GetMapping
    public ResponseEntity<ProductResponseDTO> getProduct(
            @RequestParam String url,
            @RequestParam String email) {
        
        log.debug("Buscando produto específico: URL={}, Email={}", url, email);
        
        try {
            ProductId productId = new ProductId(url, email);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> {
                        log.warn("Produto não encontrado: {}", productId);
                        return new IllegalArgumentException("Produto não encontrado");
                    });

            return ResponseEntity.ok(toProductResponse(product));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Erro ao buscar produto", e);
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
        List<PriceHistory> history = priceHistoryRepository
                .findByProductUrlAndEmailOrderByDateDesc(
                        product.getId().getUrl(),
                        product.getId().getEmail()
                );

        List<PriceHistoryResponseDTO> historyDto = history.stream()
                .map(ph -> new PriceHistoryResponseDTO(
                        ph.getId(),
                        ph.getPrice(),
                        ph.getCapturedAt()
                ))
                .collect(Collectors.toList());

        return new ProductResponseDTO(
                product.getId().getUrl(),
                product.getId().getEmail(),
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
