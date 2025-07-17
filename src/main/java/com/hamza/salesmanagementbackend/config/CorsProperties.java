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

    /**  jdbc:mysql://mysql-28deff92-hamzatemp3123-95b3.e.aivencloud.com:26632/defaultdb?createDatabaseIfNotExist=true&ssl-mode=REQUIRED&useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&maxReconnects=10&connectTimeout=60000&socketTimeout=60000
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

