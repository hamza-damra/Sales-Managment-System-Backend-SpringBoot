package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryMigrationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryMigrationService categoryMigrationService;

    private Product testProduct1;
    private Product testProduct2;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testProduct1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(50)
                .sku("LAP001")
                .category(null) // No category assigned yet
                .build();

        testProduct2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(BigDecimal.valueOf(29.99))
                .stockQuantity(100)
                .sku("MOU001")
                .category(null) // No category assigned yet
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(1)
                .build();
    }

    @Test
    void migrateStringCategoriesToEntities_Success() {
        // Given
        List<Product> productsWithoutCategory = Arrays.asList(testProduct1, testProduct2);
        
        when(productRepository.findAll()).thenReturn(productsWithoutCategory);
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct1);

        // When
        categoryMigrationService.migrateStringCategoriesToEntities();

        // Then
        verify(productRepository).findAll();
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
        verify(categoryRepository).save(any(Category.class));
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void migrateStringCategoriesToEntities_ExistingCategory() {
        // Given
        List<Product> productsWithoutCategory = Arrays.asList(testProduct1, testProduct2);
        
        when(productRepository.findAll()).thenReturn(productsWithoutCategory);
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct1);

        // When
        categoryMigrationService.migrateStringCategoriesToEntities();

        // Then
        verify(productRepository).findAll();
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
        verify(categoryRepository, never()).save(any(Category.class)); // Should not create new category
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void migrateStringCategoriesToEntities_ProductsAlreadyHaveCategory() {
        // Given
        testProduct1.setCategory(testCategory);
        testProduct2.setCategory(testCategory);
        List<Product> productsWithCategory = Arrays.asList(testProduct1, testProduct2);
        
        when(productRepository.findAll()).thenReturn(productsWithCategory);

        // When
        categoryMigrationService.migrateStringCategoriesToEntities();

        // Then
        verify(productRepository).findAll();
        verify(categoryRepository, never()).save(any(Category.class));
        verify(productRepository, never()).save(any(Product.class)); // No products to migrate
    }

    @Test
    void migrateStringCategoriesToEntities_Exception() {
        // Given
        when(productRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> categoryMigrationService.migrateStringCategoriesToEntities());
        assertEquals("Category migration failed", exception.getMessage());
        
        verify(productRepository).findAll();
        verify(categoryRepository, never()).save(any(Category.class));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createDefaultCategories_NoCategoriesExist() {
        // Given
        when(categoryRepository.count()).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        categoryMigrationService.createDefaultCategories();

        // Then
        verify(categoryRepository).count();
        verify(categoryRepository, times(10)).save(any(Category.class)); // 10 default categories
    }

    @Test
    void createDefaultCategories_CategoriesAlreadyExist() {
        // Given
        when(categoryRepository.count()).thenReturn(5L);

        // When
        categoryMigrationService.createDefaultCategories();

        // Then
        verify(categoryRepository).count();
        verify(categoryRepository, never()).save(any(Category.class)); // Should not create categories
    }

    @Test
    void validateCategoryMigration_AllProductsHaveCategories() {
        // Given
        testProduct1.setCategory(testCategory);
        testProduct2.setCategory(testCategory);
        List<Product> productsWithCategories = Arrays.asList(testProduct1, testProduct2);
        
        when(productRepository.findAll()).thenReturn(productsWithCategories);

        // When
        assertDoesNotThrow(() -> categoryMigrationService.validateCategoryMigration());

        // Then
        verify(productRepository).findAll();
    }

    @Test
    void validateCategoryMigration_SomeProductsWithoutCategories() {
        // Given
        testProduct1.setCategory(testCategory); // Has category
        // testProduct2 has no category
        List<Product> mixedProducts = Arrays.asList(testProduct1, testProduct2);
        
        when(productRepository.findAll()).thenReturn(mixedProducts);

        // When
        assertDoesNotThrow(() -> categoryMigrationService.validateCategoryMigration());

        // Then
        verify(productRepository).findAll();
    }

    @Test
    void assignUncategorizedProducts_Success() {
        // Given
        List<Product> productsWithoutCategory = Arrays.asList(testProduct1, testProduct2);
        Category otherCategory = Category.builder()
                .id(2L)
                .name("Other")
                .description("Default category for uncategorized products")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(999)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.of(otherCategory));
        when(productRepository.findAll()).thenReturn(productsWithoutCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct1);

        // When
        categoryMigrationService.assignUncategorizedProducts();

        // Then
        verify(categoryRepository).findByNameIgnoreCase("Other");
        verify(productRepository).findAll();
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void assignUncategorizedProducts_CreateOtherCategory() {
        // Given
        List<Product> productsWithoutCategory = Arrays.asList(testProduct1, testProduct2);
        Category otherCategory = Category.builder()
                .id(2L)
                .name("Other")
                .description("Default category for uncategorized products")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(999)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(otherCategory);
        when(productRepository.findAll()).thenReturn(productsWithoutCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct1);

        // When
        categoryMigrationService.assignUncategorizedProducts();

        // Then
        verify(categoryRepository).findByNameIgnoreCase("Other");
        verify(categoryRepository).save(any(Category.class)); // Should create "Other" category
        verify(productRepository).findAll();
        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    void assignUncategorizedProducts_AllProductsHaveCategories() {
        // Given
        testProduct1.setCategory(testCategory);
        testProduct2.setCategory(testCategory);
        List<Product> productsWithCategories = Arrays.asList(testProduct1, testProduct2);
        Category otherCategory = Category.builder()
                .id(2L)
                .name("Other")
                .build();

        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.of(otherCategory));
        when(productRepository.findAll()).thenReturn(productsWithCategories);

        // When
        categoryMigrationService.assignUncategorizedProducts();

        // Then
        verify(categoryRepository).findByNameIgnoreCase("Other");
        verify(productRepository).findAll();
        verify(productRepository, never()).save(any(Product.class)); // No products to assign
    }

    @Test
    void assignUncategorizedProducts_MixedProducts() {
        // Given
        testProduct1.setCategory(testCategory); // Already has category
        // testProduct2 has no category
        List<Product> mixedProducts = Arrays.asList(testProduct1, testProduct2);
        Category otherCategory = Category.builder()
                .id(2L)
                .name("Other")
                .build();

        when(categoryRepository.findByNameIgnoreCase("Other")).thenReturn(Optional.of(otherCategory));
        when(productRepository.findAll()).thenReturn(mixedProducts);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct2);

        // When
        categoryMigrationService.assignUncategorizedProducts();

        // Then
        verify(categoryRepository).findByNameIgnoreCase("Other");
        verify(productRepository).findAll();
        verify(productRepository, times(1)).save(any(Product.class)); // Only testProduct2 should be saved
    }
}
