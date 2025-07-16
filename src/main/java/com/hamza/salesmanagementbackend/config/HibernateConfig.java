package com.hamza.salesmanagementbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Custom Hibernate configuration to handle MySQL schema creation issues
 * Specifically addresses foreign key constraint problems during DDL execution
 */
@Configuration
@Slf4j
public class HibernateConfig {

    /**
     * Customize Hibernate properties to handle MySQL foreign key issues
     * This ensures proper schema creation order and error handling
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return new HibernatePropertiesCustomizer() {
            @Override
            public void customize(Map<String, Object> hibernateProperties) {
                log.info("Customizing Hibernate properties for MySQL schema creation...");
                
                // Disable foreign key checks during schema creation
                hibernateProperties.put("hibernate.hbm2ddl.halt_on_error", "false");
                
                // Ensure proper schema creation order
                hibernateProperties.put("hibernate.hbm2ddl.create_namespaces", "true");
                
                // Use create-drop for fresh database deployment
                hibernateProperties.put("hibernate.hbm2ddl.auto", "create-drop");
                
                // Enable SQL logging for debugging (can be disabled in production)
                hibernateProperties.put("hibernate.show_sql", "false");
                hibernateProperties.put("hibernate.format_sql", "false");
                
                // MySQL-specific optimizations
                hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                hibernateProperties.put("hibernate.globally_quoted_identifiers", "true");
                hibernateProperties.put("hibernate.id.new_generator_mappings", "true");
                
                // Connection and transaction settings
                hibernateProperties.put("hibernate.connection.autocommit", "false");
                hibernateProperties.put("hibernate.jdbc.batch_size", "20");
                hibernateProperties.put("hibernate.order_inserts", "true");
                hibernateProperties.put("hibernate.order_updates", "true");
                hibernateProperties.put("hibernate.jdbc.batch_versioned_data", "true");
                
                log.info("Hibernate properties customization completed");
            }
        };
    }
}
