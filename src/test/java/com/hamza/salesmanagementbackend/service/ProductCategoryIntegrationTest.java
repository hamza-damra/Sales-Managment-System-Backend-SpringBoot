package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ProductDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
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
class ProductCategoryIntegrationTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .costPrice(BigDecimal.valueOf(50.00))
                .stockQuantity(100)
                .category(testCategory)
                .productStatus(Product.ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .costPrice(BigDecimal.valueOf(50.00))
                .stockQuantity(100)
                .categoryId(1L)
                .categoryName("Electronics")
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void createProduct_WithCategoryId_Success() {
        // Given
        ProductDTO inputDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .sku("NEW-001")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(50)
                .categoryId(1L)
                .build();

        Product savedProduct = Product.builder()
                .id(2L)
                .name("New Product")
                .description("New Description")
                .sku("NEW-001")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(50)
                .category(testCategory)
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();

        when(productRepository.findBySku("NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductDTO result = productService.createProduct(inputDTO);

        // Then
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_WithCategoryName_Success() {
        // Given
        ProductDTO inputDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .sku("NEW-001")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(50)
                .categoryName("Electronics")
                .build();

        Product savedProduct = Product.builder()
                .id(2L)
                .name("New Product")
                .description("New Description")
                .sku("NEW-001")
                .price(BigDecimal.valueOf(199.99))
                .stockQuantity(50)
                .category(testCategory)
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();

        when(productRepository.findBySku("NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductDTO result = productService.createProduct(inputDTO);

        // Then
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsException() {
        // Given
        ProductDTO inputDTO = ProductDTO.builder()
                .name("New Product")
                .sku("NEW-001")
                .categoryId(999L)
                .build();

        when(productRepository.findBySku("NEW-001")).thenReturn(Optional.empty());
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(inputDTO));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(productRepository).findBySku("NEW-001");
        verify(categoryRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_CategoryNameNotFound_ThrowsException() {
        // Given
        ProductDTO inputDTO = ProductDTO.builder()
                .name("New Product")
                .sku("NEW-002")
                .categoryName("NonExistent")
                .build();

        when(productRepository.findBySku("NEW-002")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> productService.createProduct(inputDTO));
        assertEquals("Category not found with name: NonExistent", exception.getMessage());
        verify(productRepository).findBySku("NEW-002");
        verify(categoryRepository).findByNameIgnoreCase("NonExistent");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductsByCategoryId_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryId(1L)).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getProductsByCategoryId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals(1L, result.get(0).getCategoryId());
        assertEquals("Electronics", result.get(0).getCategoryName());
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void getProductsByCategoryId_WithPagination_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);

        // When
        Page<ProductDTO> result = productService.getProductsByCategoryId(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        assertEquals(1L, result.getContent().get(0).getCategoryId());
        verify(productRepository).findByCategoryId(1L, pageable);
    }

    @Test
    void getProductsByCategoryName_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryName("Electronics")).thenReturn(products);

        // When
        List<ProductDTO> result = productService.getProductsByCategoryName("Electronics");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        assertEquals("Electronics", result.get(0).getCategoryName());
        verify(productRepository).findByCategoryName("Electronics");
    }

    @Test
    void getProductsByCategoryName_WithPagination_Success() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findByCategoryName("Electronics", pageable)).thenReturn(productPage);

        // When
        Page<ProductDTO> result = productService.getProductsByCategoryName("Electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository).findByCategoryName("Electronics", pageable);
    }

    @Test
    void getAllCategoryNames_Success() {
        // Given
        List<String> categoryNames = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.findDistinctCategoryNames()).thenReturn(categoryNames);

        // When
        List<String> result = productService.getAllCategoryNames();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("Electronics"));
        assertTrue(result.contains("Books"));
        assertTrue(result.contains("Clothing"));
        verify(productRepository).findDistinctCategoryNames();
    }

    @Test
    void getProductsGroupedByCategoryName_Success() {
        // Given
        Product product1 = Product.builder()
                .name("Product 1")
                .category(testCategory)
                .build();

        Product product2 = Product.builder()
                .name("Product 2")
                .category(testCategory)
                .build();

        Category booksCategory = Category.builder()
                .id(2L)
                .name("Books")
                .build();

        Product product3 = Product.builder()
                .name("Product 3")
                .category(booksCategory)
                .build();

        List<Product> allProducts = Arrays.asList(product1, product2, product3);
        when(productRepository.findAll()).thenReturn(allProducts);

        // When
        var result = productService.getProductsGroupedByCategoryName();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Electronics"));
        assertTrue(result.containsKey("Books"));
        assertEquals(2, result.get("Electronics").size());
        assertEquals(1, result.get("Books").size());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_ChangeCategoryById_Success() {
        // Given
        Category newCategory = Category.builder()
                .id(2L)
                .name("Books")
                .build();

        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .categoryId(2L)
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .category(newCategory)
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(2L, result.getCategoryId());
        assertEquals("Books", result.getCategoryName());
        verify(categoryRepository).findById(2L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_ChangeCategoryByName_Success() {
        // Given
        Category newCategory = Category.builder()
                .id(2L)
                .name("Books")
                .build();

        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .categoryName("Books")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .category(newCategory)
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findBySku("TEST-001")).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findByNameIgnoreCase("Books")).thenReturn(Optional.of(newCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductDTO result = productService.updateProduct(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals(2L, result.getCategoryId());
        assertEquals("Books", result.getCategoryName());
        verify(categoryRepository).findByNameIgnoreCase("Books");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void mapToDTO_WithCategory_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        ProductDTO result = productService.getProductById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(1L, result.getCategoryId());
        assertEquals("Electronics", result.getCategoryName());
        verify(productRepository).findById(1L);
    }

    @Test
    void mapToDTO_WithoutCategory_Success() {
        // Given
        Product productWithoutCategory = Product.builder()
                .id(2L)
                .name("Product Without Category")
                .category(null)
                .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(productWithoutCategory));

        // When
        ProductDTO result = productService.getProductById(2L);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Product Without Category", result.getName());
        assertNull(result.getCategoryId());
        assertNull(result.getCategoryName());
        verify(productRepository).findById(2L);
    }
}
