package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.TestSecurityConfig;
import com.hamza.salesmanagementbackend.dto.ProductDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(TestSecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .costPrice(BigDecimal.valueOf(50.00))
                .stockQuantity(100)
                .minStockLevel(10)
                .category("Electronics")
                .brand("TestBrand")
                .productStatus(Product.ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createProduct_Success() throws Exception {
        // Given
        when(productService.createProduct(any(ProductDTO.class))).thenReturn(testProductDTO);

        // When & Then
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService).createProduct(any(ProductDTO.class));
    }

    @Test
    void getAllProducts_Success() throws Exception {
        // Given
        List<ProductDTO> products = Arrays.asList(testProductDTO);
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductById_Success() throws Exception {
        // Given
        when(productService.getProductById(1L)).thenReturn(testProductDTO);

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"));

        verify(productService).getProductById(1L);
    }

    @Test
    void updateProduct_Success() throws Exception {
        // Given
        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(testProductDTO);

        // When & Then
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(1L);

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void updateStock_Success() throws Exception {
        // Given
        when(productService.updateStock(1L, 50)).thenReturn(testProductDTO);

        // When & Then
        mockMvc.perform(patch("/api/products/1/stock")
                .param("quantity", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(100));

        verify(productService).updateStock(1L, 50);
    }

    @Test
    void getLowStockProducts_Success() throws Exception {
        // Given
        List<ProductDTO> lowStockProducts = Arrays.asList(testProductDTO);
        when(productService.getLowStockProducts()).thenReturn(lowStockProducts);

        // When & Then
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(productService).getLowStockProducts();
    }

    @Test
    void searchProducts_Success() throws Exception {
        // Given
        Page<ProductDTO> productPage = new PageImpl<>(Arrays.asList(testProductDTO),
                PageRequest.of(0, 10), 1);
        when(productService.searchProducts(eq("Test"), any())).thenReturn(productPage);

        // When & Then
        mockMvc.perform(get("/api/products/search")
                .param("query", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));

        verify(productService).searchProducts(eq("Test"), any());
    }
}
