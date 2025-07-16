package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CategoryDTO;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Inventory;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.InventoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public CategoryService(CategoryRepository categoryRepository, InventoryRepository inventoryRepository) {
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Creates a new category after validating name uniqueness
     */
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        validateCategoryNameUniqueness(categoryDTO.getName(), null);
        validateCategoryData(categoryDTO);
        Category category = mapToEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    /**
     * Gets all categories with pagination
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets all active categories ordered by display order
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByStatusOrderedByDisplayOrder(Category.CategoryStatus.ACTIVE)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets category by ID
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return mapToDTO(category);
    }

    /**
     * Gets category by name
     */
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String name) {
        Category category = categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return mapToDTO(category);
    }

    /**
     * Updates category information with validation
     */
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        validateCategoryNameUniqueness(categoryDTO.getName(), id);
        validateCategoryData(categoryDTO);
        updateCategoryFields(existingCategory, categoryDTO);
        Category savedCategory = categoryRepository.save(existingCategory);
        return mapToDTO(savedCategory);
    }

    /**
     * Deletes a category by ID
     */
    public void deleteCategory(Long id) {
        // Verify category exists
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Check if category has products
        Long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw DataIntegrityException.categoryHasProducts(id, productCount.intValue());
        }

        categoryRepository.deleteById(id);
    }

    /**
     * Searches categories with pagination
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> searchCategories(String searchTerm, Pageable pageable) {
        return categoryRepository.searchCategories(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets categories by status
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByStatus(Category.CategoryStatus status) {
        return categoryRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets empty categories (categories with no products)
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getEmptyCategories() {
        return categoryRepository.findEmptyCategories()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets categories by inventory ID
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByInventoryId(Long inventoryId) {
        // Verify inventory exists
        inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + inventoryId));

        return categoryRepository.findByInventoryId(inventoryId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets categories without inventory assignment
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesWithoutInventory() {
        return categoryRepository.findByInventoryIsNull()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates category status
     */
    public CategoryDTO updateCategoryStatus(Long id, Category.CategoryStatus status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setStatus(status);
        Category savedCategory = categoryRepository.save(category);
        return mapToDTO(savedCategory);
    }

    // Private helper methods
    private void validateCategoryNameUniqueness(String name, Long excludeId) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessLogicException("Category name cannot be empty");
        }

        boolean exists = categoryRepository.findByNameIgnoreCase(name.trim())
                .filter(category -> excludeId == null || !category.getId().equals(excludeId))
                .isPresent();

        if (exists) {
            throw new BusinessLogicException("Category with name '" + name + "' already exists");
        }
    }

    private void validateCategoryData(CategoryDTO categoryDTO) {
        if (categoryDTO.getName() == null || categoryDTO.getName().trim().isEmpty()) {
            throw new BusinessLogicException("Category name is required");
        }

        if (categoryDTO.getDisplayOrder() != null && categoryDTO.getDisplayOrder() < 0) {
            throw new BusinessLogicException("Display order cannot be negative");
        }
    }

    private void updateCategoryFields(Category existingCategory, CategoryDTO categoryDTO) {
        existingCategory.setName(categoryDTO.getName().trim());
        existingCategory.setDescription(categoryDTO.getDescription());
        existingCategory.setDisplayOrder(categoryDTO.getDisplayOrder() != null ? categoryDTO.getDisplayOrder() : 0);
        existingCategory.setImageUrl(categoryDTO.getImageUrl());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setColorCode(categoryDTO.getColorCode());

        // Update inventory relationship
        if (categoryDTO.getInventoryId() != null) {
            Inventory inventory = inventoryRepository.findById(categoryDTO.getInventoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + categoryDTO.getInventoryId()));
            existingCategory.setInventory(inventory);
        } else {
            existingCategory.setInventory(null);
        }

        if (categoryDTO.getStatus() != null) {
            existingCategory.setStatus(categoryDTO.getStatus());
        }
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .status(category.getStatus())
                .imageUrl(category.getImageUrl())
                .icon(category.getIcon())
                .colorCode(category.getColorCode())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .inventoryId(category.getInventory() != null ? category.getInventory().getId() : null)
                .inventoryName(category.getInventory() != null ? category.getInventory().getName() : null)
                .productCount(category.getProductCount())
                .build();
        return dto;
    }

    private Category mapToEntity(CategoryDTO categoryDTO) {
        Category.CategoryBuilder builder = Category.builder()
                .name(categoryDTO.getName() != null ? categoryDTO.getName().trim() : null)
                .description(categoryDTO.getDescription())
                .displayOrder(categoryDTO.getDisplayOrder() != null ? categoryDTO.getDisplayOrder() : 0)
                .status(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : Category.CategoryStatus.ACTIVE)
                .imageUrl(categoryDTO.getImageUrl())
                .icon(categoryDTO.getIcon())
                .colorCode(categoryDTO.getColorCode());

        // Set inventory relationship if provided
        if (categoryDTO.getInventoryId() != null) {
            Inventory inventory = inventoryRepository.findById(categoryDTO.getInventoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + categoryDTO.getInventoryId()));
            builder.inventory(inventory);
        }

        return builder.build();
    }
}
