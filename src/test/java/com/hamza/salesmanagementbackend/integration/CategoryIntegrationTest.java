package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.dto.CategoryDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clean up database
        categoryRepository.deleteAll();

        // Create test category
        testCategory = Category.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/electronics.jpg")
                .icon("electronics-icon")
                .colorCode("#007bff")
                .build();

        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void getAllCategories_Integration_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "displayOrder")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.content[0].name", is("Electronics")))
                .andExpect(jsonPath("$.content[0].description", is("Electronic devices and accessories")))
                .andExpect(jsonPath("$.content[0].displayOrder", is(1)))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.content[0].imageUrl", is("https://example.com/electronics.jpg")))
                .andExpect(jsonPath("$.content[0].icon", is("electronics-icon")))
                .andExpect(jsonPath("$.content[0].colorCode", is("#007bff")))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getAllActiveCategories_Integration_Success() throws Exception {
        // Given - Create an inactive category
        Category inactiveCategory = Category.builder()
                .name("Inactive Category")
                .description("This category is inactive")
                .displayOrder(2)
                .status(Category.CategoryStatus.INACTIVE)
                .build();
        categoryRepository.save(inactiveCategory);

        // When & Then
        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }

    @Test
    void getCategoryById_Integration_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Electronics")))
                .andExpect(jsonPath("$.description", is("Electronic devices and accessories")));
    }

    @Test
    void getCategoryById_Integration_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoryByName_Integration_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/name/{name}", "Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Electronics")));
    }

    @Test
    void createCategory_Integration_Success() throws Exception {
        // Given
        CategoryDTO newCategoryDTO = CategoryDTO.builder()
                .name("Books")
                .description("Books and literature")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/books.jpg")
                .icon("books-icon")
                .colorCode("#28a745")
                .build();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Books")))
                .andExpect(jsonPath("$.description", is("Books and literature")))
                .andExpect(jsonPath("$.displayOrder", is(2)))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/books.jpg")))
                .andExpect(jsonPath("$.icon", is("books-icon")))
                .andExpect(jsonPath("$.colorCode", is("#28a745")))
                .andExpect(jsonPath("$.id", notNullValue()));

        // Verify in database
        assert categoryRepository.findByNameIgnoreCase("Books").isPresent();
    }

    @Test
    void createCategory_Integration_DuplicateName() throws Exception {
        // Given
        CategoryDTO duplicateCategoryDTO = CategoryDTO.builder()
                .name("Electronics") // Same name as existing category
                .description("Duplicate category")
                .build();

        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateCategoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_Integration_Success() throws Exception {
        // Given
        CategoryDTO updateDTO = CategoryDTO.builder()
                .name("Electronics & Technology")
                .description("Updated description for electronics")
                .displayOrder(5)
                .status(Category.CategoryStatus.ACTIVE)
                .imageUrl("https://example.com/electronics-updated.jpg")
                .icon("tech-icon")
                .colorCode("#0056b3")
                .build();

        // When & Then
        mockMvc.perform(put("/api/categories/{id}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Electronics & Technology")))
                .andExpect(jsonPath("$.description", is("Updated description for electronics")))
                .andExpect(jsonPath("$.displayOrder", is(5)))
                .andExpect(jsonPath("$.imageUrl", is("https://example.com/electronics-updated.jpg")))
                .andExpect(jsonPath("$.icon", is("tech-icon")))
                .andExpect(jsonPath("$.colorCode", is("#0056b3")));

        // Verify in database
        Category updatedCategory = categoryRepository.findById(testCategory.getId()).orElseThrow();
        assert updatedCategory.getName().equals("Electronics & Technology");
        assert updatedCategory.getDisplayOrder().equals(5);
    }

    @Test
    void deleteCategory_Integration_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());

        // Verify in database
        assert categoryRepository.findById(testCategory.getId()).isEmpty();
    }

    @Test
    void searchCategories_Integration_Success() throws Exception {
        // Given - Create additional categories
        Category booksCategory = Category.builder()
                .name("Books")
                .description("Books and literature")
                .displayOrder(2)
                .status(Category.CategoryStatus.ACTIVE)
                .build();
        categoryRepository.save(booksCategory);

        // When & Then
        mockMvc.perform(get("/api/categories/search")
                        .param("query", "Electronics")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Electronics")));
    }

    @Test
    void getCategoriesByStatus_Integration_Success() throws Exception {
        // Given - Create categories with different statuses
        Category inactiveCategory = Category.builder()
                .name("Inactive Category")
                .description("This category is inactive")
                .displayOrder(2)
                .status(Category.CategoryStatus.INACTIVE)
                .build();
        categoryRepository.save(inactiveCategory);

        Category archivedCategory = Category.builder()
                .name("Archived Category")
                .description("This category is archived")
                .displayOrder(3)
                .status(Category.CategoryStatus.ARCHIVED)
                .build();
        categoryRepository.save(archivedCategory);

        // Test ACTIVE status
        mockMvc.perform(get("/api/categories/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")));

        // Test INACTIVE status
        mockMvc.perform(get("/api/categories/status/INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Inactive Category")));

        // Test ARCHIVED status
        mockMvc.perform(get("/api/categories/status/ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Archived Category")));
    }

    @Test
    void getEmptyCategories_Integration_Success() throws Exception {
        // Given - testCategory has no products, so it should be considered empty
        
        // When & Then
        mockMvc.perform(get("/api/categories/empty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")));
    }

    @Test
    void updateCategoryStatus_Integration_Success() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/categories/{id}/status", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        // Verify in database
        Category updatedCategory = categoryRepository.findById(testCategory.getId()).orElseThrow();
        assert updatedCategory.getStatus() == Category.CategoryStatus.INACTIVE;
    }

    @Test
    void categoryPagination_Integration_Success() throws Exception {
        // Given - Create multiple categories
        for (int i = 1; i <= 15; i++) {
            Category category = Category.builder()
                    .name("Category " + i)
                    .description("Description " + i)
                    .displayOrder(i + 1)
                    .status(Category.CategoryStatus.ACTIVE)
                    .build();
            categoryRepository.save(category);
        }

        // Test first page
        mockMvc.perform(get("/api/categories")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "displayOrder")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(16))) // 15 + 1 original
                .andExpect(jsonPath("$.totalPages", is(4)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)));

        // Test last page
        mockMvc.perform(get("/api/categories")
                        .param("page", "3")
                        .param("size", "5")
                        .param("sortBy", "displayOrder")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.first", is(false)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void categorySorting_Integration_Success() throws Exception {
        // Given - Create categories with different display orders
        Category category1 = Category.builder()
                .name("ZZZ Category")
                .description("Last alphabetically")
                .displayOrder(1)
                .status(Category.CategoryStatus.ACTIVE)
                .build();
        categoryRepository.save(category1);

        Category category2 = Category.builder()
                .name("AAA Category")
                .description("First alphabetically")
                .displayOrder(3)
                .status(Category.CategoryStatus.ACTIVE)
                .build();
        categoryRepository.save(category2);

        // Test sorting by name ascending
        mockMvc.perform(get("/api/categories")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("AAA Category")))
                .andExpect(jsonPath("$.content[1].name", is("Electronics")))
                .andExpect(jsonPath("$.content[2].name", is("ZZZ Category")));

        // Test sorting by displayOrder ascending
        mockMvc.perform(get("/api/categories")
                        .param("sortBy", "displayOrder")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].displayOrder", is(1)))
                .andExpect(jsonPath("$.content[1].displayOrder", is(1))) // testCategory
                .andExpect(jsonPath("$.content[2].displayOrder", is(3)));
    }
}
