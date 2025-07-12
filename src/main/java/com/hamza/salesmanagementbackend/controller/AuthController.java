package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
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
@RequestMapping(ApplicationConstants.API_AUTH)
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping(ApplicationConstants.TEST_ENDPOINT)
    public ResponseEntity<Map<String, String>> testEndpoint() {
        log.info("Auth controller test endpoint accessed");
        return ResponseEntity.ok(Map.of(
            "message", "Auth controller is working",
            "endpoint", ApplicationConstants.AUTH_TEST_ENDPOINT,
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @PostMapping(ApplicationConstants.LOGIN_ENDPOINT)
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        log.info("Login attempt for user: {}", signInRequest.getUsername());
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }

    @PostMapping(ApplicationConstants.SIGNUP_ENDPOINT)
    public ResponseEntity<SignUpResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        log.info("Signup attempt for user: {} with email: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
        return ResponseEntity.ok(authService.signUp(signUpRequest));
    }

    @PostMapping(ApplicationConstants.REFRESH_ENDPOINT)
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        log.info("Token refresh attempt");
        return ResponseEntity.ok(authService.refreshToken(tokenRefreshRequest));
    }
}
