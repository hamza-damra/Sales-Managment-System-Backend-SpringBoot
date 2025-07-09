package com.hamza.salesmanagementbackend.config;

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
    @Bean
    @Primary
    public DataInitializer testDataInitializer() {
        return new DataInitializer() {
            @Override
            public void run(String... args) throws Exception {
                // Do nothing during tests - prevent data initialization
            }
        };
    }

    /**
     * Override CategoryMigrationService to prevent migration during tests
     */
    @Bean
    @Primary
    public com.hamza.salesmanagementbackend.service.CategoryMigrationService testCategoryMigrationService() {
        return new com.hamza.salesmanagementbackend.service.CategoryMigrationService(null, null) {
            @Override
            public void createDefaultCategories() {
                // Do nothing during tests
            }

            @Override
            public void migrateStringCategoriesToEntities() {
                // Do nothing during tests
            }

            @Override
            public void assignUncategorizedProducts() {
                // Do nothing during tests
            }

            @Override
            public void validateCategoryMigration() {
                // Do nothing during tests
            }
        };
    }
}
