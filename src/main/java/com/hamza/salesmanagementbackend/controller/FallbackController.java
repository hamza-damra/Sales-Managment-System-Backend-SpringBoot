package com.hamza.salesmanagementbackend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Fallback controller to handle requests that might be going to wrong paths
 */
@RestController
@Slf4j
public class FallbackController {

    /**
     * Handle requests to /auth/* (without /api prefix)
     */
    @RequestMapping(value = "/auth/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Map<String, Object>> handleAuthRequests() {
        log.warn("Request received at /auth/* - should be /api/v1/auth/*");

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid endpoint",
            "message", "Authentication endpoints are available at /api/v1/auth/*",
            "correctEndpoints", Map.of(
                "login", "POST /api/v1/auth/login",
                "signup", "POST /api/v1/auth/signup",
                "refresh", "POST /api/v1/auth/refresh",
                "test", "GET /api/v1/auth/test"
            )
        ));
    }

    /**
     * Handle requests to /signup (without any prefix)
     */
    @RequestMapping(value = "/signup", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> handleSignupRequests() {
        log.warn("Request received at /signup - should be /api/v1/auth/signup");

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid endpoint",
            "message", "Signup endpoint is available at /api/v1/auth/signup",
            "correctEndpoint", "POST /api/v1/auth/signup",
            "requiredHeaders", Map.of(
                "Content-Type", "application/json"
            ),
            "samplePayload", Map.of(
                "username", "string",
                "email", "string",
                "password", "string",
                "firstName", "string",
                "lastName", "string",
                "role", "USER|ADMIN|MANAGER|etc"
            )
        ));
    }
}
