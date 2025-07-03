package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.payload.request.SignInRequest;
import com.hamza.salesmanagementbackend.payload.request.SignUpRequest;
import com.hamza.salesmanagementbackend.payload.request.TokenRefreshRequest;
import com.hamza.salesmanagementbackend.payload.response.JwtAuthenticationResponse;
import com.hamza.salesmanagementbackend.payload.response.SignUpResponse;
import com.hamza.salesmanagementbackend.payload.response.TokenRefreshResponse;

public interface AuthService {
    JwtAuthenticationResponse signIn(SignInRequest signInRequest);
    SignUpResponse signUp(SignUpRequest signUpRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest);
}
