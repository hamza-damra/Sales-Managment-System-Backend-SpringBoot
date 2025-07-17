package com.hamza.salesmanagementbackend.service.impl;

import com.hamza.salesmanagementbackend.entity.RefreshToken;
import com.hamza.salesmanagementbackend.entity.User;
import com.hamza.salesmanagementbackend.exception.AuthenticationFailedException;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.payload.request.SignInRequest;
import com.hamza.salesmanagementbackend.payload.request.SignUpRequest;
import com.hamza.salesmanagementbackend.payload.request.TokenRefreshRequest;
import com.hamza.salesmanagementbackend.payload.response.JwtAuthenticationResponse;
import com.hamza.salesmanagementbackend.payload.response.SignUpResponse;
import com.hamza.salesmanagementbackend.payload.response.TokenRefreshResponse;
import com.hamza.salesmanagementbackend.repository.UserRepository;
import com.hamza.salesmanagementbackend.security.JwtTokenProvider;
import com.hamza.salesmanagementbackend.service.AuthService;
import com.hamza.salesmanagementbackend.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest signInRequest) {
        try {
            log.debug("Attempting to authenticate user: {}", signInRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            log.debug("User authenticated successfully: {}", user.getUsername());

            String accessToken = jwtTokenProvider.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

            log.debug("Tokens generated successfully for user: {}", user.getUsername());
            return new JwtAuthenticationResponse(accessToken, refreshToken, user);

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", signInRequest.getUsername());
            throw AuthenticationFailedException.invalidCredentials();
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", signInRequest.getUsername(), e);
            throw new AuthenticationFailedException("AUTHENTICATION_FAILED", "Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user: {}", signInRequest.getUsername(), e);
            throw new BusinessLogicException("An error occurred during authentication. Please try again.");
        }
    }

    @Override
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BusinessLogicException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BusinessLogicException("Email is already in use!");
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .role(signUpRequest.getRole())
                .build();

        User savedUser = userRepository.save(user);

        // Generate tokens for the newly created user
        String accessToken = jwtTokenProvider.generateToken(savedUser);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser.getId()).getToken();

        return new SignUpResponse(accessToken, refreshToken, savedUser);
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        String requestRefreshToken = tokenRefreshRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BusinessLogicException("Refresh token is not in database!"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId()).getToken();

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }
}
