package com.hamza.salesmanagementbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Database configuration to ensure proper MySQL settings for schema creation
 * This runs before any other database operations to set required session variables
 */
@Component
@Slf4j
@Order(-1) // Run before all other CommandLineRunners
public class DatabaseConfig implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== CONFIGURING DATABASE SESSION VARIABLES ===");
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Disable foreign key checks to allow schema creation in any order
            log.info("Disabling foreign key checks...");
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Disable primary key requirement for collection tables
            log.info("Disabling primary key requirement...");
            statement.execute("SET sql_require_primary_key = 0");
            
            // Set other MySQL session variables for better compatibility
            statement.execute("SET sql_mode = 'TRADITIONAL'");
            // Remove autocommit setting to let Spring manage transactions
            // statement.execute("SET autocommit = 1");
            
            log.info("Database session variables configured successfully");
            log.info("=== DATABASE CONFIGURATION COMPLETED ===");
            
        } catch (Exception e) {
            log.error("Failed to configure database session variables", e);
            // Don't throw exception to prevent application startup failure
        }
    }
}
