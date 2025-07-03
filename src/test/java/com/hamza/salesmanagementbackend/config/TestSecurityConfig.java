package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public JwtConfig jwtConfig() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret("mySecretKey12345678901234567890123456789012345678901234567890");
        jwtConfig.setExpiration(86400000);
        return jwtConfig;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtConfig jwtConfig) {
        return new JwtTokenProvider(jwtConfig);
    }
}

