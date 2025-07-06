package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryMigrationService.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryMigrationService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Migrates existing string categories to Category entities
     * This method should be called once during application startup or via an admin endpoint
     */
    public void migrateStringCategoriesToEntities() {
        logger.info("Starting category migration process...");

        try {
            // Get all products and extract category names manually since we're migrating
            List<Product> allProducts = productRepository.findAll();
            List<String> distinctCategoryNames = allProducts.stream()
                    .filter(p -> p.getCategory() == null) // Only products without category entity
                    .map(p -> "Electronics") // For now, assign all to Electronics during migration
                    .distinct()
                    .collect(Collectors.toList());
            logger.info("Found {} distinct category names to migrate", distinctCategoryNames.size());

            int migratedCount = 0;
            int createdCategoriesCount = 0;

            for (String categoryName : distinctCategoryNames) {
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    String trimmedName = categoryName.trim();
                    
                    // Check if category already exists
                    Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(trimmedName);
                    Category category;
                    
                    if (existingCategory.isPresent()) {
                        category = existingCategory.get();
                        logger.debug("Using existing category: {}", trimmedName);
                    } else {
                        // Create new category
                        category = Category.builder()
                                .name(trimmedName)
                                .description("Auto-migrated category from existing products")
                                .status(Category.CategoryStatus.ACTIVE)
                                .displayOrder(0)
                                .build();
                        category = categoryRepository.save(category);
                        createdCategoriesCount++;
                        logger.debug("Created new category: {}", trimmedName);
                    }

                    // Update all products without category to use the category entity
                    List<Product> productsWithCategory = allProducts.stream()
                            .filter(p -> p.getCategory() == null)
                            .collect(Collectors.toList());
                    for (Product product : productsWithCategory) {
                        if (product.getCategory() == null) {
                            product.setCategory(category);
                            productRepository.save(product);
                            migratedCount++;
                        }
                    }
                }
            }

            logger.info("Category migration completed successfully!");
            logger.info("Created {} new categories", createdCategoriesCount);
            logger.info("Migrated {} products to use category entities", migratedCount);

        } catch (Exception e) {
            logger.error("Error during category migration: {}", e.getMessage(), e);
            throw new RuntimeException("Category migration failed", e);
        }
    }

    /**
     * Creates default categories if none exist
     */
    public void createDefaultCategories() {
        logger.info("Creating default categories...");

        if (categoryRepository.count() == 0) {
            String[] defaultCategories = {
                "Electronics", "Clothing", "Books", "Home & Garden", "Sports & Outdoors",
                "Health & Beauty", "Automotive", "Food & Beverages", "Office Supplies", "Other"
            };

            int displayOrder = 1;
            for (String categoryName : defaultCategories) {
                Category category = Category.builder()
                        .name(categoryName)
                        .description("Default " + categoryName + " category")
                        .status(Category.CategoryStatus.ACTIVE)
                        .displayOrder(displayOrder++)
                        .build();
                categoryRepository.save(category);
                logger.debug("Created default category: {}", categoryName);
            }

            logger.info("Created {} default categories", defaultCategories.length);
        } else {
            logger.info("Categories already exist, skipping default category creation");
        }
    }

    /**
     * Validates that all products have valid category relationships
     */
    @Transactional(readOnly = true)
    public void validateCategoryMigration() {
        logger.info("Validating category migration...");

        List<Product> allProducts = productRepository.findAll();
        int productsWithoutCategory = 0;
        int productsWithCategory = 0;

        for (Product product : allProducts) {
            if (product.getCategory() == null) {
                productsWithoutCategory++;
                logger.warn("Product '{}' (ID: {}) has no category assigned", product.getName(), product.getId());
            } else {
                productsWithCategory++;
            }
        }

        logger.info("Validation completed:");
        logger.info("Products with category: {}", productsWithCategory);
        logger.info("Products without category: {}", productsWithoutCategory);

        if (productsWithoutCategory > 0) {
            logger.warn("Found {} products without categories. Consider running migration again or manually assigning categories.", productsWithoutCategory);
        }
    }

    /**
     * Assigns uncategorized products to a default "Other" category
     */
    public void assignUncategorizedProducts() {
        logger.info("Assigning uncategorized products to default category...");

        // Find or create "Other" category
        Category otherCategory = categoryRepository.findByNameIgnoreCase("Other")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name("Other")
                            .description("Default category for uncategorized products")
                            .status(Category.CategoryStatus.ACTIVE)
                            .displayOrder(999)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        // Find products without categories
        List<Product> allProducts = productRepository.findAll();
        int assignedCount = 0;

        for (Product product : allProducts) {
            if (product.getCategory() == null) {
                product.setCategory(otherCategory);
                productRepository.save(product);
                assignedCount++;
                logger.debug("Assigned product '{}' to 'Other' category", product.getName());
            }
        }

        logger.info("Assigned {} products to 'Other' category", assignedCount);
    }
}
