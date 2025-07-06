package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.TestSecurityConfig;
import com.hamza.salesmanagementbackend.dto.CategoryDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.CategoryService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testCategoryDTO = CategoryDTO.builder()
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
                .productCount(5)
                .build();
    }

    @Test
    void getAllCategories_Success() throws Exception {
        // Given
        List<CategoryDTO> categories = Arrays.asList(testCategoryDTO);
        Page<CategoryDTO> categoryPage = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(categoryService.getAllCategories(any())).thenReturn(categoryPage);

        // When & Then
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "displayOrder")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                .andExpect(jsonPath("$.content[0].productCount").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(categoryService).getAllCategories(any());
    }

    @Test
    void getAllActiveCategories_Success() throws Exception {
        // Given
        List<CategoryDTO> activeCategories = Arrays.asList(testCategoryDTO);
        when(categoryService.getAllActiveCategories()).thenReturn(activeCategories);

        // When & Then
        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(categoryService).getAllActiveCategories();
    }

    @Test
    void getCategoryById_Success() throws Exception {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(testCategoryDTO);

        // When & Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic devices and accessories"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void getCategoryById_NotFound() throws Exception {
        // Given
        when(categoryService.getCategoryById(999L)).thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategoryById(999L);
    }

    @Test
    void getCategoryById_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/0"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategoryById(anyLong());
    }

    @Test
    void getCategoryByName_Success() throws Exception {
        // Given
        when(categoryService.getCategoryByName("Electronics")).thenReturn(testCategoryDTO);

        // When & Then
        mockMvc.perform(get("/api/categories/name/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).getCategoryByName("Electronics");
    }

    @Test
    void getCategoryByName_NotFound() throws Exception {
        // Given
        when(categoryService.getCategoryByName("NonExistent"))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(get("/api/categories/name/NonExistent"))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategoryByName("NonExistent");
    }

    @Test
    void getCategoryByName_EmptyName() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/name/ "))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategoryByName(anyString());
    }

    @Test
    void createCategory_Success() throws Exception {
        // Given
        CategoryDTO inputDTO = CategoryDTO.builder()
                .name("Books")
                .description("Books and literature")
                .build();

        CategoryDTO createdDTO = CategoryDTO.builder()
                .id(2L)
                .name("Books")
                .description("Books and literature")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(0)
                .productCount(0)
                .build();

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(createdDTO);

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Books"))
                .andExpect(jsonPath("$.description").value("Books and literature"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    void createCategory_DuplicateName() throws Exception {
        // Given
        CategoryDTO inputDTO = CategoryDTO.builder()
                .name("Electronics")
                .description("Duplicate category")
                .build();

        when(categoryService.createCategory(any(CategoryDTO.class)))
                .thenThrow(new BusinessLogicException("Category with name 'Electronics' already exists"));

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    void createCategory_InvalidInput() throws Exception {
        // Given
        CategoryDTO invalidDTO = CategoryDTO.builder()
                .name("") // Empty name should fail validation
                .description("Test description")
                .build();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryDTO.class));
    }

    @Test
    void updateCategory_Success() throws Exception {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Electronics & Technology")
                .description("Updated description")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .build();

        CategoryDTO updatedDTO = CategoryDTO.builder()
                .id(1L)
                .name("Electronics & Technology")
                .description("Updated description")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .productCount(5)
                .build();

        when(categoryService.updateCategory(eq(1L), any(CategoryDTO.class))).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics & Technology"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.displayOrder").value(2));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDTO.class));
    }

    @Test
    void updateCategory_NotFound() throws Exception {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Updated Name")
                .build();

        when(categoryService.updateCategory(eq(999L), any(CategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(put("/api/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(categoryService).updateCategory(eq(999L), any(CategoryDTO.class));
    }

    @Test
    void updateCategory_InvalidId() throws Exception {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Updated Name")
                .build();

        // When & Then
        mockMvc.perform(put("/api/categories/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategory(anyLong(), any(CategoryDTO.class));
    }

    @Test
    void deleteCategory_Success() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_NotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Category not found"))
                .when(categoryService).deleteCategory(999L);

        // When & Then
        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound());

        verify(categoryService).deleteCategory(999L);
    }

    @Test
    void deleteCategory_WithProducts() throws Exception {
        // Given
        doThrow(new BusinessLogicException("Cannot delete category with associated products"))
                .when(categoryService).deleteCategory(1L);

        // When & Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isBadRequest());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void searchCategories_Success() throws Exception {
        // Given
        List<CategoryDTO> categories = Arrays.asList(testCategoryDTO);
        Page<CategoryDTO> categoryPage = new PageImpl<>(categories, PageRequest.of(0, 10), 1);

        when(categoryService.searchCategories(eq("electronics"), any())).thenReturn(categoryPage);

        // When & Then
        mockMvc.perform(get("/api/categories/search")
                        .param("query", "electronics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(categoryService).searchCategories(eq("electronics"), any());
    }

    @Test
    void getCategoriesByStatus_Success() throws Exception {
        // Given
        List<CategoryDTO> activeCategories = Arrays.asList(testCategoryDTO);
        when(categoryService.getCategoriesByStatus(Category.CategoryStatus.ACTIVE)).thenReturn(activeCategories);

        // When & Then
        mockMvc.perform(get("/api/categories/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(categoryService).getCategoriesByStatus(Category.CategoryStatus.ACTIVE);
    }

    @Test
    void getCategoriesByStatus_InvalidStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/status/INVALID"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategoriesByStatus(any());
    }

    @Test
    void getEmptyCategories_Success() throws Exception {
        // Given
        List<CategoryDTO> emptyCategories = Arrays.asList(testCategoryDTO);
        when(categoryService.getEmptyCategories()).thenReturn(emptyCategories);

        // When & Then
        mockMvc.perform(get("/api/categories/empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Electronics"));

        verify(categoryService).getEmptyCategories();
    }

    @Test
    void updateCategoryStatus_Success() throws Exception {
        // Given
        CategoryDTO updatedDTO = CategoryDTO.builder()
                .id(1L)
                .name("Electronics")
                .status(Category.CategoryStatus.INACTIVE)
                .build();

        when(categoryService.updateCategoryStatus(1L, Category.CategoryStatus.INACTIVE)).thenReturn(updatedDTO);

        // When & Then
        mockMvc.perform(put("/api/categories/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(categoryService).updateCategoryStatus(1L, Category.CategoryStatus.INACTIVE);
    }

    @Test
    void updateCategoryStatus_InvalidStatus() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/categories/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INVALID\"}"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).updateCategoryStatus(anyLong(), any());
    }

    @Test
    void updateCategoryStatus_NotFound() throws Exception {
        // Given
        when(categoryService.updateCategoryStatus(999L, Category.CategoryStatus.INACTIVE))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(put("/api/categories/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isNotFound());

        verify(categoryService).updateCategoryStatus(999L, Category.CategoryStatus.INACTIVE);
    }
}
