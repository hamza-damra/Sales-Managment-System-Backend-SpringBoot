package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.service.CategoryMigrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestConfig {

    /**
     * Override DataInitializer to prevent it from running during tests
     */
    @Bean("dataInitializer")
    @Primary
    public CommandLineRunner testDataInitializer() {
        return args -> {
            // Do nothing during tests - prevent data initialization
            System.out.println("Test DataInitializer: Skipping data initialization for tests");
        };
    }

    /**
     * Override CategoryMigrationService to prevent migration during tests
     */
    @Bean
    @Primary
    public CategoryMigrationService testCategoryMigrationService() {
        return new CategoryMigrationService(null, null) {
            @Override
            public void createDefaultCategories() {
                // Do nothing during tests
                System.out.println("Test CategoryMigrationService: Skipping category creation for tests");
            }

            @Override
            public void migrateStringCategoriesToEntities() {
                // Do nothing during tests
                System.out.println("Test CategoryMigrationService: Skipping migration for tests");
            }

            @Override
            public void assignUncategorizedProducts() {
                // Do nothing during tests
                System.out.println("Test CategoryMigrationService: Skipping product assignment for tests");
            }

            @Override
            public void validateCategoryMigration() {
                // Do nothing during tests
                System.out.println("Test CategoryMigrationService: Skipping validation for tests");
            }
        };
    }
}
