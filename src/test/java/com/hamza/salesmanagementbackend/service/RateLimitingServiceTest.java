package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import com.hamza.salesmanagementbackend.entity.UpdateAnalytics;
import com.hamza.salesmanagementbackend.repository.RateLimitTrackerRepository;
import com.hamza.salesmanagementbackend.repository.UpdateAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitingService
 */
@ExtendWith(MockitoExtension.class)
class RateLimitingServiceTest {

    @Mock
    private RateLimitTrackerRepository rateLimitRepository;

    @Mock
    private UpdateAnalyticsRepository analyticsRepository;

    @InjectMocks
    private RateLimitingService rateLimitingService;

    private String testClientId;
    private String testClientIp;
    private RateLimitTracker.EndpointType testEndpointType;

    @BeforeEach
    void setUp() {
        testClientId = "test-client-123";
        testClientIp = "192.168.1.100";
        testEndpointType = RateLimitTracker.EndpointType.DOWNLOAD;
        
        // Set global rate limit for testing
        ReflectionTestUtils.setField(rateLimitingService, "globalRateLimit", 10);
    }

    @Test
    void checkRateLimit_ShouldAllowRequest_WhenNewClient() {
        // Given
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.empty());
        
        RateLimitTracker newTracker = createNewTracker();
        when(rateLimitRepository.save(any(RateLimitTracker.class))).thenReturn(newTracker);

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertTrue(result.isAllowed());
        assertEquals(4, result.getRemainingRequests()); // 5 max - 1 used = 4 remaining
        assertTrue(result.getResetTimeSeconds() > 0);
        assertEquals("Request allowed", result.getMessage());

        verify(rateLimitRepository).findByClientIdentifierAndEndpointType(testClientId, testEndpointType);
        verify(rateLimitRepository, times(2)).save(any(RateLimitTracker.class)); // Create + increment
        verify(analyticsRepository, never()).save(any(UpdateAnalytics.class)); // No analytics for allowed requests
    }

    @Test
    void checkRateLimit_ShouldAllowRequest_WhenWithinLimit() {
        // Given
        RateLimitTracker existingTracker = createExistingTracker(2); // 2 requests used out of 5
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.of(existingTracker));

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertTrue(result.isAllowed());
        assertEquals(2, result.getRemainingRequests()); // 5 max - 3 used = 2 remaining
        assertTrue(result.getResetTimeSeconds() > 0);

        verify(rateLimitRepository).save(existingTracker);
        assertEquals(3, existingTracker.getRequestCount()); // Should be incremented
    }

    @Test
    void checkRateLimit_ShouldBlockRequest_WhenLimitExceeded() {
        // Given
        RateLimitTracker existingTracker = createExistingTracker(5); // At limit (5/5)
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.of(existingTracker));

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemainingRequests());
        assertTrue(result.getResetTimeSeconds() > 0);
        assertTrue(result.getMessage().contains("Rate limit exceeded"));

        verify(rateLimitRepository).save(existingTracker);
        assertNotNull(existingTracker.getBlockedUntil());
        assertEquals(1, existingTracker.getViolationCount());
        verify(analyticsRepository).save(any(UpdateAnalytics.class)); // Should record analytics
    }

    @Test
    void checkRateLimit_ShouldBlockRequest_WhenClientAlreadyBlocked() {
        // Given
        RateLimitTracker blockedTracker = createBlockedTracker();
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.of(blockedTracker));

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertFalse(result.isAllowed());
        assertEquals(0, result.getRemainingRequests());
        assertTrue(result.getResetTimeSeconds() > 0);
        assertTrue(result.getMessage().contains("temporarily blocked"));

        verify(rateLimitRepository).save(blockedTracker);
        verify(analyticsRepository).save(any(UpdateAnalytics.class));
    }

    @Test
    void checkRateLimit_ShouldResetWindow_WhenWindowExpired() {
        // Given
        RateLimitTracker expiredTracker = createExpiredWindowTracker();
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.of(expiredTracker));

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertTrue(result.isAllowed());
        assertEquals(4, result.getRemainingRequests()); // Should be reset to 4 (5-1)
        
        // Window should be reset
        assertTrue(expiredTracker.getWindowStart().isAfter(LocalDateTime.now().minusMinutes(1)));
        assertEquals(1, expiredTracker.getRequestCount());
    }

    @Test
    void checkRateLimit_ShouldUseExponentialBackoff_ForRepeatedViolations() {
        // Given
        RateLimitTracker violatorTracker = createRepeatedViolatorTracker(3); // 3 previous violations
        when(rateLimitRepository.findByClientIdentifierAndEndpointType(testClientId, testEndpointType))
            .thenReturn(Optional.of(violatorTracker));

        // When
        RateLimitingService.RateLimitResult result = rateLimitingService
            .checkRateLimit(testClientId, testClientIp, testEndpointType);

        // Then
        assertFalse(result.isAllowed());
        
        // Block duration should be exponential: 1, 2, 4, 8 minutes for violations 0, 1, 2, 3
        // For 3rd violation, should be blocked for 8 minutes = 480 seconds
        assertTrue(result.getResetTimeSeconds() >= 480);
        assertEquals(4, violatorTracker.getViolationCount()); // Should be incremented
    }

    @Test
    void resetRateLimits_ShouldClearAllLimitsForClient() {
        // Given
        RateLimitTracker tracker1 = createBlockedTracker();
        RateLimitTracker tracker2 = createExistingTracker(5);
        when(rateLimitRepository.findByClientIdentifier(testClientId))
            .thenReturn(java.util.Arrays.asList(tracker1, tracker2));

        // When
        rateLimitingService.resetRateLimits(testClientId);

        // Then
        verify(rateLimitRepository).findByClientIdentifier(testClientId);
        verify(rateLimitRepository).saveAll(any());
        
        // Both trackers should be reset
        assertNull(tracker1.getBlockedUntil());
        assertEquals(0, tracker1.getViolationCount());
        assertNull(tracker1.getFirstViolationTime());
        
        assertEquals(0, tracker2.getRequestCount());
        assertEquals(0, tracker2.getViolationCount());
    }

    @Test
    void getRateLimitStatus_ShouldReturnCorrectStatus() {
        // Given
        RateLimitTracker blockedTracker = createBlockedTracker();
        RateLimitTracker normalTracker = createExistingTracker(2);
        when(rateLimitRepository.findByClientIdentifier(testClientId))
            .thenReturn(java.util.Arrays.asList(blockedTracker, normalTracker));

        // When
        RateLimitingService.RateLimitStatus status = rateLimitingService.getRateLimitStatus(testClientId);

        // Then
        assertNotNull(status);
        assertEquals(testClientId, status.getClientIdentifier());
        assertTrue(status.isBlocked()); // Should be blocked due to blockedTracker
        assertTrue(status.getBlockedUntilSeconds() > 0);
        assertEquals(2, status.getTrackers().size());
    }

    // Helper methods to create test data

    private RateLimitTracker createNewTracker() {
        return RateLimitTracker.builder()
            .clientIdentifier(testClientId)
            .clientIp(testClientIp)
            .endpointType(testEndpointType)
            .requestCount(1)
            .windowStart(LocalDateTime.now())
            .lastRequestTime(LocalDateTime.now())
            .totalBlockedRequests(0L)
            .totalAllowedRequests(1L)
            .violationCount(0)
            .build();
    }

    private RateLimitTracker createExistingTracker(int requestCount) {
        return RateLimitTracker.builder()
            .id(1L)
            .clientIdentifier(testClientId)
            .clientIp(testClientIp)
            .endpointType(testEndpointType)
            .requestCount(requestCount)
            .windowStart(LocalDateTime.now().minusMinutes(30)) // Within window
            .lastRequestTime(LocalDateTime.now().minusMinutes(5))
            .totalBlockedRequests(0L)
            .totalAllowedRequests((long) requestCount)
            .violationCount(0)
            .build();
    }

    private RateLimitTracker createBlockedTracker() {
        return RateLimitTracker.builder()
            .id(2L)
            .clientIdentifier(testClientId)
            .clientIp(testClientIp)
            .endpointType(testEndpointType)
            .requestCount(5)
            .windowStart(LocalDateTime.now().minusMinutes(30))
            .lastRequestTime(LocalDateTime.now().minusMinutes(5))
            .blockedUntil(LocalDateTime.now().plusMinutes(5)) // Blocked for 5 more minutes
            .totalBlockedRequests(1L)
            .totalAllowedRequests(5L)
            .violationCount(1)
            .firstViolationTime(LocalDateTime.now().minusMinutes(5))
            .build();
    }

    private RateLimitTracker createExpiredWindowTracker() {
        return RateLimitTracker.builder()
            .id(3L)
            .clientIdentifier(testClientId)
            .clientIp(testClientIp)
            .endpointType(testEndpointType)
            .requestCount(5)
            .windowStart(LocalDateTime.now().minusMinutes(70)) // Expired window (>60 minutes)
            .lastRequestTime(LocalDateTime.now().minusMinutes(70))
            .totalBlockedRequests(0L)
            .totalAllowedRequests(5L)
            .violationCount(0)
            .build();
    }

    private RateLimitTracker createRepeatedViolatorTracker(int violationCount) {
        return RateLimitTracker.builder()
            .id(4L)
            .clientIdentifier(testClientId)
            .clientIp(testClientIp)
            .endpointType(testEndpointType)
            .requestCount(5) // At limit
            .windowStart(LocalDateTime.now().minusMinutes(30))
            .lastRequestTime(LocalDateTime.now().minusMinutes(5))
            .totalBlockedRequests((long) violationCount)
            .totalAllowedRequests(5L)
            .violationCount(violationCount)
            .firstViolationTime(LocalDateTime.now().minusHours(1))
            .build();
    }
}
