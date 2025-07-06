package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CategoryDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/electronics.jpg")
                .icon("electronics-icon")
                .colorCode("#007bff")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testCategoryDTO = CategoryDTO.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/electronics.jpg")
                .icon("electronics-icon")
                .colorCode("#007bff")
                .productCount(0)
                .build();
    }

    @Test
    void createCategory_Success() {
        // Given
        CategoryDTO inputDTO = CategoryDTO.builder()
                .name("Books")
                .description("Books and literature")
                .build();

        Category savedCategory = Category.builder()
                .id(2L)
                .name("Books")
                .description("Books and literature")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(0)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Books")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryDTO result = categoryService.createCategory(inputDTO);

        // Then
        assertNotNull(result);
        assertEquals("Books", result.getName());
        assertEquals("Books and literature", result.getDescription());
        assertEquals(Category.CategoryStatus.ACTIVE, result.getStatus());
        verify(categoryRepository).findByNameIgnoreCase("Books");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        // Given
        CategoryDTO inputDTO = CategoryDTO.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(testCategory));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> categoryService.createCategory(inputDTO));
        assertEquals("Category with name 'Electronics' already exists", exception.getMessage());
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createCategory_EmptyName_ThrowsException() {
        // Given
        CategoryDTO inputDTO = CategoryDTO.builder()
                .name("")
                .description("Test description")
                .build();

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> categoryService.createCategory(inputDTO));
        assertEquals("Category name cannot be empty", exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getAllCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

        // When
        Page<CategoryDTO> result = categoryService.getAllCategories(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Electronics", result.getContent().get(0).getName());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getAllActiveCategories_Success() {
        // Given
        List<Category> activeCategories = Arrays.asList(testCategory);
        when(categoryRepository.findByStatusOrderedByDisplayOrder(Category.CategoryStatus.ACTIVE))
                .thenReturn(activeCategories);

        // When
        List<CategoryDTO> result = categoryService.getAllActiveCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository).findByStatusOrderedByDisplayOrder(Category.CategoryStatus.ACTIVE);
    }

    @Test
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        CategoryDTO result = categoryService.getCategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(999L));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void getCategoryByName_Success() {
        // Given
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(testCategory));

        // When
        CategoryDTO result = categoryService.getCategoryByName("Electronics");

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).findByNameIgnoreCase("Electronics");
    }

    @Test
    void getCategoryByName_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findByNameIgnoreCase("NonExistent")).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryByName("NonExistent"));
        assertEquals("Category not found with name: NonExistent", exception.getMessage());
        verify(categoryRepository).findByNameIgnoreCase("NonExistent");
    }

    @Test
    void updateCategory_Success() {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Electronics & Technology")
                .description("Updated description")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Electronics & Technology")
                .description("Updated description")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameIgnoreCase("Electronics & Technology")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryDTO result = categoryService.updateCategory(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("Electronics & Technology", result.getName());
        assertEquals("Updated description", result.getDescription());
        assertEquals(2, result.getDisplayOrder());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_NotFound_ThrowsException() {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Updated Name")
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, updateDTO));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.countProductsByCategoryId(1L)).thenReturn(0L);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countProductsByCategoryId(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_WithProducts_ThrowsException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.countProductsByCategoryId(1L)).thenReturn(5L);

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> categoryService.deleteCategory(1L));
        assertEquals("Cannot delete category with 5 associated products", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).countProductsByCategoryId(1L);
        verify(categoryRepository, never()).deleteById(1L);
    }

    @Test
    void deleteCategory_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(999L));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).deleteById(999L);
    }

    @Test
    void searchCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10);

        when(categoryRepository.searchCategories("electronics", pageable)).thenReturn(categoryPage);

        // When
        Page<CategoryDTO> result = categoryService.searchCategories("electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Electronics", result.getContent().get(0).getName());
        verify(categoryRepository).searchCategories("electronics", pageable);
    }

    @Test
    void getCategoriesByStatus_Success() {
        // Given
        List<Category> activeCategories = Arrays.asList(testCategory);
        when(categoryRepository.findByStatus(Category.CategoryStatus.ACTIVE)).thenReturn(activeCategories);

        // When
        List<CategoryDTO> result = categoryService.getCategoriesByStatus(Category.CategoryStatus.ACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository).findByStatus(Category.CategoryStatus.ACTIVE);
    }

    @Test
    void getEmptyCategories_Success() {
        // Given
        List<Category> emptyCategories = Arrays.asList(testCategory);
        when(categoryRepository.findEmptyCategories()).thenReturn(emptyCategories);

        // When
        List<CategoryDTO> result = categoryService.getEmptyCategories();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
        verify(categoryRepository).findEmptyCategories();
    }

    @Test
    void updateCategoryStatus_Success() {
        // Given
        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .status(Category.CategoryStatus.INACTIVE)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryDTO result = categoryService.updateCategoryStatus(1L, Category.CategoryStatus.INACTIVE);

        // Then
        assertNotNull(result);
        assertEquals(Category.CategoryStatus.INACTIVE, result.getStatus());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategoryStatus_NotFound_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategoryStatus(999L, Category.CategoryStatus.INACTIVE));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
