package com.hamza.salesmanagementbackend.config;

import com.hamza.salesmanagementbackend.security.JwtTokenProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
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

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.disable());

        return http.build();
    }
}

