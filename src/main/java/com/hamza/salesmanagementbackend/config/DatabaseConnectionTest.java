package com.hamza.salesmanagementbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Test Component
 * This component tests the database connection on application startup
 * and provides detailed logging for troubleshooting connection issues.
 */
//@Component  // Temporarily disabled to avoid startup issues
public class DatabaseConnectionTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Database Connection Test ===");
        logger.info("Database URL: {}", databaseUrl);
        logger.info("Username: {}", username);
        logger.info("Password: [HIDDEN - Length: {}]", password != null ? password.length() : 0);

        // Test basic connection
        testDatabaseConnection();
    }

    private void testDatabaseConnection() {
        try {
            logger.info("Attempting to connect to database...");
            
            // Load MySQL driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL driver loaded successfully");

            // Test connection
            try (Connection connection = DriverManager.getConnection(databaseUrl, username, password)) {
                logger.info("✅ Database connection successful!");
                logger.info("Database product name: {}", connection.getMetaData().getDatabaseProductName());
                logger.info("Database product version: {}", connection.getMetaData().getDatabaseProductVersion());
                logger.info("Driver name: {}", connection.getMetaData().getDriverName());
                logger.info("Driver version: {}", connection.getMetaData().getDriverVersion());
                logger.info("Connection valid: {}", connection.isValid(10));
            }

        } catch (ClassNotFoundException e) {
            logger.error("❌ MySQL driver not found: {}", e.getMessage());
        } catch (SQLException e) {
            logger.error("❌ Database connection failed: {}", e.getMessage());
            logger.error("SQL State: {}", e.getSQLState());
            logger.error("Error Code: {}", e.getErrorCode());
            
            // Provide specific troubleshooting based on error
            if (e.getMessage().contains("timeout")) {
                logger.error("🔍 Connection timeout - Check network connectivity and firewall settings");
            } else if (e.getMessage().contains("Access denied")) {
                logger.error("🔍 Access denied - Check username and password");
            } else if (e.getMessage().contains("Unknown database")) {
                logger.error("🔍 Database not found - Check database name");
            } else if (e.getMessage().contains("Communications link failure")) {
                logger.error("🔍 Network issue - Check host and port");
            }
        } catch (Exception e) {
            logger.error("❌ Unexpected error during connection test: {}", e.getMessage(), e);
        }

        logger.info("=== End Database Connection Test ===");
    }
}
