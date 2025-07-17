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

                // CRITICAL: Disable foreign key checks during schema creation
                hibernateProperties.put("hibernate.hbm2ddl.halt_on_error", "false");
                hibernateProperties.put("hibernate.hbm2ddl.import_files_sql_extractor", "org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");

                // Ensure proper schema creation order
                hibernateProperties.put("hibernate.hbm2ddl.create_namespaces", "true");

                // Let Spring Boot handle DDL auto configuration from application.properties
                // hibernateProperties.put("hibernate.hbm2ddl.auto", "update");

                // Additional MySQL-specific settings for better schema creation
                hibernateProperties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
                hibernateProperties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");

                // Let Spring Boot handle these configurations from application.properties
                // hibernateProperties.put("hibernate.show_sql", "false");
                // hibernateProperties.put("hibernate.format_sql", "false");
                // hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                // hibernateProperties.put("hibernate.globally_quoted_identifiers", "true");
                // hibernateProperties.put("hibernate.id.new_generator_mappings", "true");
                // hibernateProperties.put("hibernate.connection.autocommit", "false");
                // hibernateProperties.put("hibernate.jdbc.batch_size", "20");
                // hibernateProperties.put("hibernate.order_inserts", "true");
                // hibernateProperties.put("hibernate.order_updates", "true");
                // hibernateProperties.put("hibernate.jdbc.batch_versioned_data", "true");

                // CRITICAL: Add custom SQL to disable foreign key checks
                hibernateProperties.put("hibernate.hbm2ddl.import_files", "disable-fk-checks.sql");

                log.info("Hibernate properties customization completed");
            }
        };
    }
}
