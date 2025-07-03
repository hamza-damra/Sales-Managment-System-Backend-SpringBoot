package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.config.JwtConfig;
import com.hamza.salesmanagementbackend.entity.RefreshToken;
import com.hamza.salesmanagementbackend.entity.User;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.repository.RefreshTokenRepository;
import com.hamza.salesmanagementbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final JwtConfig jwtConfig;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public RefreshTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("User not found with id: " + userId));

        // Delete any existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        // Flush the delete operation to ensure it's committed before insert
        entityManager.flush();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        // Use JWT expiration * 7 for refresh token (7 times longer than access token)
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtConfig.getExpiration() * 7));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BusinessLogicException("Refresh token was expired. Please make a new sign in request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessLogicException("User not found with id: " + userId));
        return refreshTokenRepository.deleteByUser(user);
    }
}
