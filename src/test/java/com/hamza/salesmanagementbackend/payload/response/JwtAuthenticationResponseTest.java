package com.hamza.salesmanagementbackend.payload.response;

import com.hamza.salesmanagementbackend.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationResponseTest {

    @Test
    void testJwtAuthenticationResponseWithoutUser() {
        // Given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        // When
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(accessToken, refreshToken);

        // Then
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNull(response.getUser());
    }

    @Test
    void testJwtAuthenticationResponseWithUser() {
        // Given
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(User.Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        JwtAuthenticationResponse response = new JwtAuthenticationResponse(accessToken, refreshToken, user);

        // Then
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("Test", response.getUser().getFirstName());
        assertEquals("User", response.getUser().getLastName());
        assertEquals("USER", response.getUser().getRole());
        assertNotNull(response.getUser().getCreatedAt());
    }

    @Test
    void testUserInfoConstructor() {
        // Given
        User user = User.builder()
                .id(2L)
                .username("admin")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(User.Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(user);

        // Then
        assertEquals(2L, userInfo.getId());
        assertEquals("admin", userInfo.getUsername());
        assertEquals("admin@example.com", userInfo.getEmail());
        assertEquals("Admin", userInfo.getFirstName());
        assertEquals("User", userInfo.getLastName());
        assertEquals("ADMIN", userInfo.getRole());
        assertNotNull(userInfo.getCreatedAt());
    }
}
