package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ProductDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
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
    void createProduct_Success() {
        // Given
        when(productRepository.findBySku(testProductDTO.getSku())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.createProduct(testProductDTO);

        // Then
        assertNotNull(result);
        assertEquals(testProductDTO.getSku(), result.getSku());
        assertEquals(testProductDTO.getName(), result.getName());
        verify(productRepository).findBySku(testProductDTO.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_SkuAlreadyExists_ThrowsException() {
        // Given
        when(productRepository.findBySku(testProductDTO.getSku())).thenReturn(Optional.of(testProduct));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> productService.createProduct(testProductDTO));
        assertTrue(exception.getMessage().contains("SKU already exists"));
        verify(productRepository).findBySku(testProductDTO.getSku());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getSku(), result.get(0).getSku());
        verify(productRepository).findAll();
    }

    @Test
    void getAllProductsWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // When
        Page<ProductDTO> result = productService.getAllProducts(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getSku(), result.getContent().get(0).getSku());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getProductById_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductDTO result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testProduct.getSku(), result.getSku());
        assertEquals(testProduct.getName(), result.getName());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1L));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_Success() {
        // Given
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .sku("TEST-002")
                .price(BigDecimal.valueOf(199.99))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findBySku(updateDTO.getSku())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).findBySku(updateDTO.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, testProductDTO));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_Success() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        productService.deleteProduct(1L);

        // Then
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(1L));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(productRepository).existsById(1L);
        verify(productRepository, never()).deleteById(1L);
    }

    @Test
    void updateStock_Success() {
        // Given
        int newQuantity = 50;
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        ProductDTO result = productService.updateStock(1L, newQuantity);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void reduceStock_Success() {
        // Given
        int reduceBy = 10;
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        productService.reduceStock(1L, reduceBy);

        // Then
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void reduceStock_InsufficientStock_ThrowsException() {
        // Given
        int reduceBy = 150; // More than available stock
        testProduct.setStockQuantity(100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class,
                () -> productService.reduceStock(1L, reduceBy));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getLowStockProducts_Success() {
        // Given
        List<Product> lowStockProducts = Arrays.asList(testProduct);
        when(productRepository.findByStockQuantityLessThan(10)).thenReturn(lowStockProducts);

        // When
        List<ProductDTO> result = productService.getLowStockProducts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByStockQuantityLessThan(10);
    }

    @Test
    void searchProducts_Success() {
        // Given
        String searchTerm = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.searchProducts(searchTerm, pageable)).thenReturn(productPage);

        // When
        Page<ProductDTO> result = productService.searchProducts(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testProduct.getName(), result.getContent().get(0).getName());
        verify(productRepository).searchProducts(searchTerm, pageable);
    }

    @Test
    void getProductsByCategory_Success() {
        // Given
        String category = "Electronics";
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory(category)).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getProductsByCategory(category);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getCategory(), result.get(0).getCategory());
        verify(productRepository).findByCategory(category);
    }
}
