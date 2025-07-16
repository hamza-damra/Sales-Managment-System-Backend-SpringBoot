package com.hamza.salesmanagementbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration Properties
 * Configures JWT token generation and validation settings
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {

    /**
     * JWT secret key for token signing and verification.
     * Should be a Base64 encoded string of at least 256 bits for security.
     */
    private String secret;

    /**
     * JWT token expiration time in milliseconds.
     * Default is 24 hours (86400000 ms).
     */
    private long expiration;
}

