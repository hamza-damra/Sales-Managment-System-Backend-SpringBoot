package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.CategoryDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.CategoryService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApplicationConstants.API_CATEGORIES)
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createCategorySort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping(ApplicationConstants.ACTIVE_ENDPOINT)
    public ResponseEntity<List<CategoryDTO>> getAllActiveCategories() {
        List<CategoryDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CategoryDTO category = categoryService.getCategoryByName(name.trim());
            return ResponseEntity.ok(category);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
                                                     @Valid @RequestBody CategoryDTO categoryDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
            return ResponseEntity.ok(updatedCategory);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryDTO>> searchCategories(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size);
        Page<CategoryDTO> categories = categoryService.searchCategories(query, pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByStatus(@PathVariable String status) {
        try {
            Category.CategoryStatus categoryStatus = Category.CategoryStatus.valueOf(status.toUpperCase());
            List<CategoryDTO> categories = categoryService.getCategoriesByStatus(categoryStatus);
            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/empty")
    public ResponseEntity<List<CategoryDTO>> getEmptyCategories() {
        List<CategoryDTO> categories = categoryService.getEmptyCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByInventory(@PathVariable Long inventoryId) {
        if (inventoryId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<CategoryDTO> categories = categoryService.getCategoriesByInventoryId(inventoryId);
            return ResponseEntity.ok(categories);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/no-inventory")
    public ResponseEntity<List<CategoryDTO>> getCategoriesWithoutInventory() {
        List<CategoryDTO> categories = categoryService.getCategoriesWithoutInventory();
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CategoryDTO> updateCategoryStatus(@PathVariable Long id,
                                                           @RequestBody Map<String, String> request) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String statusStr = request.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Category.CategoryStatus status = Category.CategoryStatus.valueOf(statusStr.toUpperCase());
            CategoryDTO updatedCategory = categoryService.updateCategoryStatus(id, status);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
