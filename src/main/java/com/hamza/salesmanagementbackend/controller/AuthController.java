package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.payload.request.SignInRequest;
import com.hamza.salesmanagementbackend.payload.request.SignUpRequest;
import com.hamza.salesmanagementbackend.payload.request.TokenRefreshRequest;
import com.hamza.salesmanagementbackend.payload.response.JwtAuthenticationResponse;
import com.hamza.salesmanagementbackend.payload.response.SignUpResponse;
import com.hamza.salesmanagementbackend.payload.response.TokenRefreshResponse;
import com.hamza.salesmanagementbackend.service.AuthService;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        log.info("Auth controller test endpoint accessed");
        return ResponseEntity.ok(Map.of(
            "message", "Auth controller is working",
            "endpoint", "/api/v1/auth/test",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        log.info("Login attempt for user: {}", signInRequest.getUsername());
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Signup attempt for user: {} with email: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
        return ResponseEntity.ok(authService.signUp(signUpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        log.info("Token refresh attempt");
        return ResponseEntity.ok(authService.refreshToken(tokenRefreshRequest));
    }
}
