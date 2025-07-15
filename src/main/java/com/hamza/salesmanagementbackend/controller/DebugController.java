package com.hamza.salesmanagementbackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Debug Controller for troubleshooting deployment issues
 * This controller provides endpoints to check configuration and connectivity
 * without requiring full application startup
 */
@RestController
@RequestMapping("/debug")
public class DebugController {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Basic health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("profile", activeProfile);
        return ResponseEntity.ok(response);
    }

    /**
     * Configuration check endpoint
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        Map<String, Object> response = new HashMap<>();
        response.put("profile", activeProfile);
        response.put("databaseUrl", databaseUrl);
        response.put("username", username);
        response.put("passwordLength", password != null ? password.length() : 0);
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("osName", System.getProperty("os.name"));
        
        // Environment variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("DB_HOST", System.getenv("DB_HOST"));
        envVars.put("DB_PORT", System.getenv("DB_PORT"));
        envVars.put("DB_NAME", System.getenv("DB_NAME"));
        envVars.put("DB_USERNAME", System.getenv("DB_USERNAME"));
        envVars.put("DB_PASSWORD", System.getenv("DB_PASSWORD") != null ? "[SET]" : "[NOT SET]");
        envVars.put("SPRING_PROFILES_ACTIVE", System.getenv("SPRING_PROFILES_ACTIVE"));
        
        response.put("environmentVariables", envVars);
        return ResponseEntity.ok(response);
    }

    /**
     * Database connection test endpoint
     */
    @GetMapping("/db-test")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            response.put("driverLoaded", true);

            // Test connection
            try (Connection connection = DriverManager.getConnection(databaseUrl, username, password)) {
                response.put("connectionStatus", "SUCCESS");
                response.put("databaseProduct", connection.getMetaData().getDatabaseProductName());
                response.put("databaseVersion", connection.getMetaData().getDatabaseProductVersion());
                response.put("driverName", connection.getMetaData().getDriverName());
                response.put("driverVersion", connection.getMetaData().getDriverVersion());
                response.put("connectionValid", connection.isValid(10));
            }

        } catch (Exception e) {
            response.put("connectionStatus", "FAILED");
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            
            // Provide troubleshooting hints
            String error = e.getMessage().toLowerCase();
            if (error.contains("timeout")) {
                response.put("hint", "Connection timeout - Check network connectivity");
            } else if (error.contains("access denied")) {
                response.put("hint", "Access denied - Check username and password");
            } else if (error.contains("unknown database")) {
                response.put("hint", "Database not found - Check database name");
            } else if (error.contains("communications link failure")) {
                response.put("hint", "Network issue - Check host and port");
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Environment variables endpoint
     */
    @GetMapping("/env")
    public ResponseEntity<Map<String, Object>> environment() {
        Map<String, Object> response = new HashMap<>();
        
        // System properties
        Map<String, String> systemProps = new HashMap<>();
        systemProps.put("java.version", System.getProperty("java.version"));
        systemProps.put("java.vendor", System.getProperty("java.vendor"));
        systemProps.put("os.name", System.getProperty("os.name"));
        systemProps.put("os.arch", System.getProperty("os.arch"));
        systemProps.put("user.timezone", System.getProperty("user.timezone"));
        
        response.put("systemProperties", systemProps);
        
        // Key environment variables (without sensitive data)
        Map<String, String> envVars = new HashMap<>();
        envVars.put("PORT", System.getenv("PORT"));
        envVars.put("SPRING_PROFILES_ACTIVE", System.getenv("SPRING_PROFILES_ACTIVE"));
        envVars.put("DB_HOST", System.getenv("DB_HOST"));
        envVars.put("DB_PORT", System.getenv("DB_PORT"));
        envVars.put("DB_NAME", System.getenv("DB_NAME"));
        envVars.put("DB_USERNAME", System.getenv("DB_USERNAME"));
        envVars.put("DB_PASSWORD_SET", System.getenv("DB_PASSWORD") != null ? "YES" : "NO");
        
        response.put("environmentVariables", envVars);
        
        return ResponseEntity.ok(response);
    }
}
