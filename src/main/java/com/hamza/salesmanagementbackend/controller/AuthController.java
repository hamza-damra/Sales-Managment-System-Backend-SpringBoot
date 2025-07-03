package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.payload.request.SignInRequest;
import com.hamza.salesmanagementbackend.payload.request.SignUpRequest;
import com.hamza.salesmanagementbackend.payload.request.TokenRefreshRequest;
import com.hamza.salesmanagementbackend.payload.response.JwtAuthenticationResponse;
import com.hamza.salesmanagementbackend.payload.response.SignUpResponse;
import com.hamza.salesmanagementbackend.payload.response.TokenRefreshResponse;
import com.hamza.salesmanagementbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody SignInRequest signInRequest) {
        return ResponseEntity.ok(authService.signIn(signInRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.ok(authService.signUp(signUpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
        return ResponseEntity.ok(authService.refreshToken(tokenRefreshRequest));
    }
}
