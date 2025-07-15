package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import com.hamza.salesmanagementbackend.entity.UpdateAnalytics;
import com.hamza.salesmanagementbackend.repository.RateLimitTrackerRepository;
import com.hamza.salesmanagementbackend.repository.UpdateAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing rate limiting
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RateLimitingService {

    private final RateLimitTrackerRepository rateLimitRepository;
    private final UpdateAnalyticsRepository analyticsRepository;

    @Value("${app.updates.security.rate-limit:10}")
    private int globalRateLimit;

    /**
     * Check if request is allowed for the given client and endpoint
     */
    public RateLimitResult checkRateLimit(String clientIdentifier, String clientIp, 
                                        RateLimitTracker.EndpointType endpointType) {
        log.debug("Checking rate limit for client: {} on endpoint: {}", clientIdentifier, endpointType);

        // Find or create rate limit tracker
        RateLimitTracker tracker = findOrCreateTracker(clientIdentifier, clientIp, endpointType);

        // Check if client is currently blocked
        if (tracker.isBlocked()) {
            tracker.recordBlockedRequest();
            rateLimitRepository.save(tracker);
            
            // Record analytics
            recordRateLimitEvent(clientIdentifier, clientIp, endpointType, false, "Client is blocked");
            
            return RateLimitResult.blocked(tracker.getTimeUntilResetSeconds(), 
                                         "Client is temporarily blocked due to rate limit violations");
        }

        // Check if window has expired
        if (tracker.isWindowExpired()) {
            tracker.resetWindow();
        }

        // Check if rate limit is exceeded
        if (tracker.isRateLimitExceeded()) {
            // Block client for escalating periods based on violation count
            int blockMinutes = calculateBlockDuration(tracker.getViolationCount());
            tracker.blockClient(blockMinutes);
            tracker.recordBlockedRequest();
            rateLimitRepository.save(tracker);
            
            // Record analytics
            recordRateLimitEvent(clientIdentifier, clientIp, endpointType, false, 
                               "Rate limit exceeded: " + tracker.getRequestCount() + "/" + endpointType.getMaxRequests());
            
            log.warn("Rate limit exceeded for client: {} on endpoint: {}. Blocked for {} minutes", 
                    clientIdentifier, endpointType, blockMinutes);
            
            return RateLimitResult.blocked(blockMinutes * 60L, 
                                         "Rate limit exceeded. Blocked for " + blockMinutes + " minutes");
        }

        // Allow request and increment counter
        tracker.incrementRequestCount();
        rateLimitRepository.save(tracker);

        return RateLimitResult.allowed(tracker.getRemainingRequests(), 
                                     tracker.getTimeUntilResetSeconds());
    }

    /**
     * Find or create rate limit tracker
     */
    private RateLimitTracker findOrCreateTracker(String clientIdentifier, String clientIp, 
                                               RateLimitTracker.EndpointType endpointType) {
        Optional<RateLimitTracker> existing = rateLimitRepository
            .findByClientIdentifierAndEndpointType(clientIdentifier, endpointType);

        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new tracker
        return rateLimitRepository.save(RateLimitTracker.builder()
            .clientIdentifier(clientIdentifier)
            .clientIp(clientIp)
            .endpointType(endpointType)
            .requestCount(0)
            .windowStart(LocalDateTime.now())
            .lastRequestTime(LocalDateTime.now())
            .totalBlockedRequests(0L)
            .totalAllowedRequests(0L)
            .violationCount(0)
            .build());
    }

    /**
     * Calculate block duration based on violation count (exponential backoff)
     */
    private int calculateBlockDuration(Integer violationCount) {
        if (violationCount == null) {
            violationCount = 0;
        }

        // Exponential backoff: 1, 2, 4, 8, 16, max 60 minutes
        int baseDuration = 1;
        for (int i = 0; i < violationCount && baseDuration < 60; i++) {
            baseDuration *= 2;
        }
        
        return Math.min(baseDuration, 60); // Cap at 60 minutes
    }

    /**
     * Record rate limiting event in analytics
     */
    private void recordRateLimitEvent(String clientIdentifier, String clientIp, 
                                    RateLimitTracker.EndpointType endpointType, 
                                    boolean allowed, String reason) {
        try {
            UpdateAnalytics analytics = UpdateAnalytics.builder()
                .eventType(UpdateAnalytics.EventType.RATE_LIMITED)
                .eventTimestamp(LocalDateTime.now())
                .clientIdentifier(clientIdentifier)
                .clientIp(clientIp)
                .success(allowed)
                .errorMessage(allowed ? null : reason)
                .metadata("{\"endpoint\":\"" + endpointType.name() + "\",\"reason\":\"" + reason + "\"}")
                .build();

            analyticsRepository.save(analytics);
        } catch (Exception e) {
            log.error("Error recording rate limit analytics", e);
        }
    }

    /**
     * Get rate limit status for client
     */
    @Transactional(readOnly = true)
    public RateLimitStatus getRateLimitStatus(String clientIdentifier) {
        List<RateLimitTracker> trackers = rateLimitRepository.findByClientIdentifier(clientIdentifier);
        
        boolean isBlocked = trackers.stream().anyMatch(RateLimitTracker::isBlocked);
        long maxBlockTime = trackers.stream()
            .filter(RateLimitTracker::isBlocked)
            .mapToLong(RateLimitTracker::getTimeUntilResetSeconds)
            .max()
            .orElse(0L);

        return RateLimitStatus.builder()
            .clientIdentifier(clientIdentifier)
            .isBlocked(isBlocked)
            .blockedUntilSeconds(maxBlockTime)
            .trackers(trackers)
            .build();
    }

    /**
     * Reset rate limits for a client (admin function)
     */
    public void resetRateLimits(String clientIdentifier) {
        log.info("Resetting rate limits for client: {}", clientIdentifier);
        
        List<RateLimitTracker> trackers = rateLimitRepository.findByClientIdentifier(clientIdentifier);
        for (RateLimitTracker tracker : trackers) {
            tracker.resetWindow();
            tracker.setBlockedUntil(null);
            tracker.setViolationCount(0);
            tracker.setFirstViolationTime(null);
        }
        
        rateLimitRepository.saveAll(trackers);
    }

    /**
     * Get rate limiting statistics
     */
    @Transactional(readOnly = true)
    public RateLimitStatistics getStatistics() {
        Object[] overall = rateLimitRepository.getOverallRateLimitingStatistics(LocalDateTime.now());
        List<Object[]> byEndpoint = rateLimitRepository.getRateLimitingStatisticsByEndpoint();
        List<Object[]> mostActive = rateLimitRepository.findMostActiveClients();
        List<Object[]> highViolations = rateLimitRepository.findClientsWithHighestViolationRates();

        return RateLimitStatistics.builder()
            .totalTrackers(overall[0] != null ? ((Number) overall[0]).longValue() : 0L)
            .totalAllowedRequests(overall[1] != null ? ((Number) overall[1]).longValue() : 0L)
            .totalBlockedRequests(overall[2] != null ? ((Number) overall[2]).longValue() : 0L)
            .currentlyBlockedClients(overall[3] != null ? ((Number) overall[3]).longValue() : 0L)
            .statisticsByEndpoint(byEndpoint)
            .mostActiveClients(mostActive)
            .clientsWithHighViolationRates(highViolations)
            .build();
    }

    /**
     * Cleanup expired blocks and reset windows (scheduled task)
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredLimits() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Clean up expired blocks
            int expiredBlocks = rateLimitRepository.cleanupExpiredBlocks(now);
            if (expiredBlocks > 0) {
                log.debug("Cleaned up {} expired blocks", expiredBlocks);
            }

            // Reset expired windows
            LocalDateTime windowExpiry = now.minusMinutes(60); // Assuming 60-minute windows
            int resetWindows = rateLimitRepository.resetExpiredWindows(now, windowExpiry);
            if (resetWindows > 0) {
                log.debug("Reset {} expired windows", resetWindows);
            }

        } catch (Exception e) {
            log.error("Error during rate limit cleanup", e);
        }
    }

    /**
     * Cleanup inactive trackers (scheduled task)
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupInactiveTrackers() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7); // Remove trackers inactive for 7 days
            int deleted = rateLimitRepository.deleteInactiveTrackers(cutoff);
            if (deleted > 0) {
                log.info("Deleted {} inactive rate limit trackers", deleted);
            }
        } catch (Exception e) {
            log.error("Error during inactive tracker cleanup", e);
        }
    }

    /**
     * Rate limit result
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final int remainingRequests;
        private final long resetTimeSeconds;
        private final String message;

        private RateLimitResult(boolean allowed, int remainingRequests, long resetTimeSeconds, String message) {
            this.allowed = allowed;
            this.remainingRequests = remainingRequests;
            this.resetTimeSeconds = resetTimeSeconds;
            this.message = message;
        }

        public static RateLimitResult allowed(int remainingRequests, long resetTimeSeconds) {
            return new RateLimitResult(true, remainingRequests, resetTimeSeconds, "Request allowed");
        }

        public static RateLimitResult blocked(long resetTimeSeconds, String message) {
            return new RateLimitResult(false, 0, resetTimeSeconds, message);
        }

        // Getters
        public boolean isAllowed() { return allowed; }
        public int getRemainingRequests() { return remainingRequests; }
        public long getResetTimeSeconds() { return resetTimeSeconds; }
        public String getMessage() { return message; }
    }

    /**
     * Rate limit status for a client
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitStatus {
        private String clientIdentifier;
        private boolean isBlocked;
        private long blockedUntilSeconds;
        private List<RateLimitTracker> trackers;
    }

    /**
     * Rate limiting statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitStatistics {
        private Long totalTrackers;
        private Long totalAllowedRequests;
        private Long totalBlockedRequests;
        private Long currentlyBlockedClients;
        private List<Object[]> statisticsByEndpoint;
        private List<Object[]> mostActiveClients;
        private List<Object[]> clientsWithHighViolationRates;
    }
}
