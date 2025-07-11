package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
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
    @RequestMapping(value = ApplicationConstants.AUTH_BASE + "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<Map<String, Object>> handleAuthRequests() {
        log.warn("Request received at /auth/* - should be " + ApplicationConstants.API_AUTH + "/*");

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid endpoint",
            "message", "Authentication endpoints are available at " + ApplicationConstants.API_AUTH + "/*",
            "correctEndpoints", Map.of(
                "login", "POST " + ApplicationConstants.AUTH_LOGIN_ENDPOINT,
                "signup", "POST " + ApplicationConstants.AUTH_SIGNUP_ENDPOINT,
                "refresh", "POST " + ApplicationConstants.AUTH_REFRESH_ENDPOINT,
                "test", "GET " + ApplicationConstants.AUTH_TEST_ENDPOINT
            )
        ));
    }

    /**
     * Handle requests to /signup (without any prefix)
     */
    @RequestMapping(value = ApplicationConstants.SIGNUP_ENDPOINT, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> handleSignupRequests() {
        log.warn("Request received at /signup - should be " + ApplicationConstants.AUTH_SIGNUP_ENDPOINT);

        return ResponseEntity.badRequest().body(Map.of(
            "error", "Invalid endpoint",
            "message", "Signup endpoint is available at " + ApplicationConstants.AUTH_SIGNUP_ENDPOINT,
            "correctEndpoint", "POST " + ApplicationConstants.AUTH_SIGNUP_ENDPOINT,
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
