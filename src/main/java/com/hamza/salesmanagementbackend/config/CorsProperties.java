package com.hamza.salesmanagementbackend.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration Properties
 * Configures Cross-Origin Resource Sharing settings for the application
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Data
@Getter
@Setter
public class CorsProperties {

    /**
     * Comma-separated list of allowed CORS origins.
     * Use "*" to allow all origins (not recommended for production).
     */
    private String allowedOrigins;

    /**
     * Comma-separated list of allowed HTTP methods.
     * Common values: GET,POST,PUT,DELETE,OPTIONS
     */
    private String allowedMethods;

    /**
     * Comma-separated list of allowed request headers.
     * Use "*" to allow all headers.
     */
    private String allowedHeaders;

    /**
     * CORS preflight request cache duration in seconds.
     * Default is 1 hour (3600 seconds).
     */
    private long maxAge;

    // Explicit getters to ensure they're available
    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public String getAllowedMethods() {
        return allowedMethods;
    }

    public String getAllowedHeaders() {
        return allowedHeaders;
    }

    public long getMaxAge() {
        return maxAge;
    }
}

