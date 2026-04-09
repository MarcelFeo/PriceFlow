package com.example.priceflow.controller;

import com.example.priceflow.domain.PriceHistory;
import com.example.priceflow.domain.Product;
import com.example.priceflow.domain.ProductId;
import com.example.priceflow.dto.CreateProductRequestDTO;
import com.example.priceflow.repository.PriceHistoryRepository;
import com.example.priceflow.repository.ProductRepository;
import com.example.priceflow.service.ScraperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private PriceHistoryRepository priceHistoryRepository;

    @MockBean
    private ScraperService scraperService;

    private Product testProduct;
    private ProductId testProductId;
    private PriceHistory testPriceHistory;

    @BeforeEach
    void setUp() {
        testProductId = new ProductId(
            "https://www.amazon.com.br/dp/B07EXAMPLE",
            "usuario@example.com"
        );
        
        testProduct = new Product();
        testProduct.setId(testProductId);
        testProduct.setName("Notebook XYZ");
        testProduct.setLastPrice(new BigDecimal("3000.00"));
        testProduct.setHistory(List.of());

        testPriceHistory = new PriceHistory();
        testPriceHistory.setId(1L);
        testPriceHistory.setProduct(testProduct);
        testPriceHistory.setPrice(new BigDecimal("3000.00"));
        testPriceHistory.setCapturedAt(LocalDateTime.now());
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        // Arrange
        CreateProductRequestDTO request = new CreateProductRequestDTO(
            "https://www.amazon.com.br/dp/B07EXAMPLE",
            "usuario@example.com"
        );

        when(productRepository.findByUrlAndEmail(anyString(), anyString())).thenReturn(Optional.empty());
        when(scraperService.getPriceFromUrl(anyString())).thenReturn(new BigDecimal("3000.00"));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(priceHistoryRepository.save(any(PriceHistory.class))).thenReturn(testPriceHistory);
        when(priceHistoryRepository.findByProductUrlAndEmailOrderByDateDesc(anyString(), anyString()))
            .thenReturn(List.of(testPriceHistory));

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.url").value(testProductId.getUrl()))
            .andExpect(jsonPath("$.email").value(testProductId.getEmail()))
            .andExpect(jsonPath("$.name").value("Notebook XYZ"))
            .andExpect(jsonPath("$.lastPrice").value(3000.00));
    }

    @Test
    void shouldReturnConflictWhenProductAlreadyExists() throws Exception {
        // Arrange
        CreateProductRequestDTO request = new CreateProductRequestDTO(
            "https://www.amazon.com.br/dp/B07EXAMPLE",
            "usuario@example.com"
        );

        when(productRepository.findByUrlAndEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testProduct));

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldGetAllProductsHistory() throws Exception {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(testProduct));
        when(priceHistoryRepository.findByProductUrlAndEmailOrderByDateDesc(anyString(), anyString()))
            .thenReturn(List.of(testPriceHistory));

        // Act & Assert
        mockMvc.perform(get("/api/products/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalProducts").value(1))
            .andExpect(jsonPath("$.products[0].url").value(testProductId.getUrl()))
            .andExpect(jsonPath("$.products[0].name").value("Notebook XYZ"));
    }

    @Test
    void shouldGetProductHistoryByUrl() throws Exception {
        // Arrange
        when(productRepository.findByUrl(anyString())).thenReturn(Optional.of(testProduct));
        when(priceHistoryRepository.findByProductUrlAndEmailOrderByDateDesc(anyString(), anyString()))
            .thenReturn(List.of(testPriceHistory));

        // Act & Assert
        mockMvc.perform(get("/api/products/history/details")
                .param("url", testProductId.getUrl()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value(testProductId.getUrl()))
            .andExpect(jsonPath("$.history.length()").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenProductNotFound() throws Exception {
        // Arrange
        when(productRepository.findByUrl(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/products/history/details")
                .param("url", "https://invalid.com"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetProductByUrlAndEmail() throws Exception {
        // Arrange
        when(productRepository.findByUrlAndEmail(anyString(), anyString()))
            .thenReturn(Optional.of(testProduct));
        when(priceHistoryRepository.findByProductUrlAndEmailOrderByDateDesc(anyString(), anyString()))
            .thenReturn(List.of(testPriceHistory));

        // Act & Assert
        mockMvc.perform(get("/api/products/history/user")
                .param("url", testProductId.getUrl())
                .param("email", testProductId.getEmail()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(testProductId.getEmail()));
    }

    @Test
    void shouldGetProductDetails() throws Exception {
        // Arrange
        when(productRepository.findById(any(ProductId.class)))
            .thenReturn(Optional.of(testProduct));
        when(priceHistoryRepository.findByProductUrlAndEmailOrderByDateDesc(anyString(), anyString()))
            .thenReturn(List.of(testPriceHistory));

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .param("url", testProductId.getUrl())
                .param("email", testProductId.getEmail()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Notebook XYZ"));
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        // Arrange
        when(productRepository.existsById(any(ProductId.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                .param("url", testProductId.getUrl())
                .param("email", testProductId.getEmail()))
            .andExpect(status().isNoContent());

        verify(productRepository).deleteById(any(ProductId.class));
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistentProduct() throws Exception {
        // Arrange
        when(productRepository.existsById(any(ProductId.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/products")
                .param("url", "https://invalid.com")
                .param("email", "invalid@example.com"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateEmailInRequest() throws Exception {
        // Arrange
        CreateProductRequestDTO invalidRequest = new CreateProductRequestDTO(
            "https://www.amazon.com.br/dp/B07EXAMPLE",
            "invalid-email"
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldValidateUrlInRequest() throws Exception {
        // Arrange
        CreateProductRequestDTO invalidRequest = new CreateProductRequestDTO(
            "",
            "usuario@example.com"
        );

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
