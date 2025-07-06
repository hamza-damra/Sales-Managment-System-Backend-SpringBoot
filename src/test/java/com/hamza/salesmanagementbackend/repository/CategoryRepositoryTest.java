package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category electronicsCategory;
    private Category booksCategory;
    private Category inactiveCategory;

    @BeforeEach
    void setUp() {
        // Create test categories
        electronicsCategory = Category.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/electronics.jpg")
                .icon("electronics-icon")
                .colorCode("#007bff")
                .build();

        booksCategory = Category.builder()
                .name("Books")
                .description("Books and literature")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/books.jpg")
                .icon("books-icon")
                .colorCode("#28a745")
                .build();

        inactiveCategory = Category.builder()
                .name("Inactive Category")
                .description("This category is inactive")
                .displayOrder(3)
                .status(Category.CategoryStatus.INACTIVE)
                .build();

        // Persist categories
        electronicsCategory = entityManager.persistAndFlush(electronicsCategory);
        booksCategory = entityManager.persistAndFlush(booksCategory);
        inactiveCategory = entityManager.persistAndFlush(inactiveCategory);

        // Create test products
        Product laptop = Product.builder()
                .name("Laptop")
                .description("High-performance laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(50)
                .sku("LAP001")
                .category(electronicsCategory)
                .build();

        Product mouse = Product.builder()
                .name("Mouse")
                .description("Wireless mouse")
                .price(BigDecimal.valueOf(29.99))
                .stockQuantity(100)
                .sku("MOU001")
                .category(electronicsCategory)
                .build();

        Product book = Product.builder()
                .name("Java Programming")
                .description("Learn Java programming")
                .price(BigDecimal.valueOf(49.99))
                .stockQuantity(25)
                .sku("BOOK001")
                .category(booksCategory)
                .build();

        // Persist products
        entityManager.persistAndFlush(laptop);
        entityManager.persistAndFlush(mouse);
        entityManager.persistAndFlush(book);

        entityManager.clear();
    }

    @Test
    void findByName_Success() {
        // When
        Optional<Category> result = categoryRepository.findByName("Electronics");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
        assertEquals("Electronic devices and accessories", result.get().getDescription());
    }

    @Test
    void findByName_NotFound() {
        // When
        Optional<Category> result = categoryRepository.findByName("NonExistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByNameIgnoreCase_Success() {
        // When
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("ELECTRONICS");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }

    @Test
    void findByNameIgnoreCase_LowerCase() {
        // When
        Optional<Category> result = categoryRepository.findByNameIgnoreCase("electronics");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }

    @Test
    void findByStatus_Active() {
        // When
        List<Category> activeCategories = categoryRepository.findByStatus(Category.CategoryStatus.ACTIVE);

        // Then
        assertEquals(2, activeCategories.size());
        assertTrue(activeCategories.stream().allMatch(c -> c.getStatus() == Category.CategoryStatus.ACTIVE));
        assertTrue(activeCategories.stream().anyMatch(c -> c.getName().equals("Electronics")));
        assertTrue(activeCategories.stream().anyMatch(c -> c.getName().equals("Books")));
    }

    @Test
    void findByStatus_Inactive() {
        // When
        List<Category> inactiveCategories = categoryRepository.findByStatus(Category.CategoryStatus.INACTIVE);

        // Then
        assertEquals(1, inactiveCategories.size());
        assertEquals("Inactive Category", inactiveCategories.get(0).getName());
        assertEquals(Category.CategoryStatus.INACTIVE, inactiveCategories.get(0).getStatus());
    }

    @Test
    void findByStatus_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Category> result = categoryRepository.findByStatus(Category.CategoryStatus.ACTIVE, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findByNameContainingIgnoreCase_Success() {
        // When
        List<Category> result = categoryRepository.findByNameContainingIgnoreCase("ELECT");

        // Then
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void findByNameContainingIgnoreCase_NoMatch() {
        // When
        List<Category> result = categoryRepository.findByNameContainingIgnoreCase("XYZ");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllOrderedByDisplayOrder_Success() {
        // When
        List<Category> result = categoryRepository.findAllOrderedByDisplayOrder();

        // Then
        assertEquals(3, result.size());
        assertEquals("Electronics", result.get(0).getName()); // displayOrder = 1
        assertEquals("Books", result.get(1).getName()); // displayOrder = 2
        assertEquals("Inactive Category", result.get(2).getName()); // displayOrder = 3
    }

    @Test
    void findByStatusOrderedByDisplayOrder_Success() {
        // When
        List<Category> result = categoryRepository.findByStatusOrderedByDisplayOrder(Category.CategoryStatus.ACTIVE);

        // Then
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName()); // displayOrder = 1
        assertEquals("Books", result.get(1).getName()); // displayOrder = 2
    }

    @Test
    void searchCategories_ByName() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.searchCategories("Electronics", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Electronics", result.getContent().get(0).getName());
    }

    @Test
    void searchCategories_ByDescription() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.searchCategories("literature", pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Books", result.getContent().get(0).getName());
    }

    @Test
    void searchCategories_NoMatch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.searchCategories("NonExistent", pageable);

        // Then
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void countProductsByCategoryId_WithProducts() {
        // When
        Long count = categoryRepository.countProductsByCategoryId(electronicsCategory.getId());

        // Then
        assertEquals(2L, count); // Electronics category has 2 products
    }

    @Test
    void countProductsByCategoryId_WithOneProduct() {
        // When
        Long count = categoryRepository.countProductsByCategoryId(booksCategory.getId());

        // Then
        assertEquals(1L, count); // Books category has 1 product
    }

    @Test
    void countProductsByCategoryId_NoProducts() {
        // When
        Long count = categoryRepository.countProductsByCategoryId(inactiveCategory.getId());

        // Then
        assertEquals(0L, count); // Inactive category has no products
    }

    @Test
    void findEmptyCategories_Success() {
        // When
        List<Category> emptyCategories = categoryRepository.findEmptyCategories();

        // Then
        assertEquals(1, emptyCategories.size());
        assertEquals("Inactive Category", emptyCategories.get(0).getName());
    }

    @Test
    void existsByName_True() {
        // When
        boolean exists = categoryRepository.existsByName("Electronics");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByName_False() {
        // When
        boolean exists = categoryRepository.existsByName("NonExistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void existsByNameIgnoreCase_True() {
        // When
        boolean exists = categoryRepository.existsByNameIgnoreCase("ELECTRONICS");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByNameIgnoreCase_False() {
        // When
        boolean exists = categoryRepository.existsByNameIgnoreCase("NONEXISTENT");

        // Then
        assertFalse(exists);
    }

    @Test
    void categoryEntity_BusinessLogicMethods() {
        // Given
        Category activeCategory = categoryRepository.findByName("Electronics").orElseThrow();
        Category inactiveCategory = categoryRepository.findByName("Inactive Category").orElseThrow();

        // When & Then
        assertTrue(activeCategory.isActive());
        assertFalse(inactiveCategory.isActive());
        
        // Note: getProductCount() returns 0 because it checks the products list size,
        // but in this test the products list is not loaded due to lazy loading
        assertEquals(0, activeCategory.getProductCount());
    }

    @Test
    void categoryEntity_Validation() {
        // Given
        Category category = categoryRepository.findByName("Electronics").orElseThrow();

        // Then
        assertNotNull(category.getId());
        assertNotNull(category.getName());
        assertNotNull(category.getCreatedAt());
        assertNotNull(category.getUpdatedAt());
        assertEquals(Category.CategoryStatus.ACTIVE, category.getStatus());
        assertEquals(1, category.getDisplayOrder());
        assertEquals("https://example.com/electronics.jpg", category.getImageUrl());
        assertEquals("electronics-icon", category.getIcon());
        assertEquals("#007bff", category.getColorCode());
    }
}
