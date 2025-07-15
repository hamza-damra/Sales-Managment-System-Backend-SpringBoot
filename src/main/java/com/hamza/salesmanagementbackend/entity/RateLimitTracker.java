package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for tracking rate limiting per client
 */
@Entity
@Table(name = "rate_limit_tracker",
       uniqueConstraints = @UniqueConstraint(columnNames = {"client_identifier", "endpoint_type"}),
       indexes = {
           @Index(name = "idx_client_identifier", columnList = "client_identifier"),
           @Index(name = "idx_endpoint_type", columnList = "endpoint_type"),
           @Index(name = "idx_window_start", columnList = "window_start"),
           @Index(name = "idx_last_request", columnList = "last_request_time")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RateLimitTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Client identifier is required")
    @Size(max = 255, message = "Client identifier must not exceed 255 characters")
    @Column(name = "client_identifier", nullable = false, length = 255)
    private String clientIdentifier;

    @Size(max = 45, message = "Client IP must not exceed 45 characters")
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "endpoint_type", nullable = false, length = 30)
    @NotNull(message = "Endpoint type is required")
    private EndpointType endpointType;

    @Column(name = "request_count", nullable = false)
    @Builder.Default
    private Integer requestCount = 0;

    @Column(name = "window_start", nullable = false)
    @Builder.Default
    private LocalDateTime windowStart = LocalDateTime.now();

    @Column(name = "last_request_time", nullable = false)
    @Builder.Default
    private LocalDateTime lastRequestTime = LocalDateTime.now();

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    @Column(name = "total_blocked_requests")
    @Builder.Default
    private Long totalBlockedRequests = 0L;

    @Column(name = "total_allowed_requests")
    @Builder.Default
    private Long totalAllowedRequests = 0L;

    @Column(name = "first_violation_time")
    private LocalDateTime firstViolationTime;

    @Column(name = "violation_count")
    @Builder.Default
    private Integer violationCount = 0;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Endpoint types for rate limiting
     */
    public enum EndpointType {
        UPDATE_CHECK("Update check endpoint", 20, 60), // 20 requests per minute
        DOWNLOAD("Download endpoint", 5, 60), // 5 downloads per minute
        METADATA("Metadata endpoint", 30, 60), // 30 requests per minute
        COMPATIBILITY("Compatibility check endpoint", 10, 60), // 10 requests per minute
        ANALYTICS("Analytics endpoint", 15, 60), // 15 requests per minute
        ROLLBACK("Rollback endpoint", 3, 60), // 3 rollbacks per minute
        DELTA("Delta update endpoint", 5, 60), // 5 delta requests per minute
        WEBSOCKET("WebSocket connection", 10, 60); // 10 connections per minute

        private final String description;
        private final int maxRequests;
        private final int windowMinutes;

        EndpointType(String description, int maxRequests, int windowMinutes) {
            this.description = description;
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
        }

        public String getDescription() {
            return description;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public int getWindowMinutes() {
            return windowMinutes;
        }
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (windowStart == null) {
            windowStart = now;
        }
        if (lastRequestTime == null) {
            lastRequestTime = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if the rate limit window has expired
     */
    public boolean isWindowExpired() {
        if (windowStart == null) {
            return true;
        }
        
        LocalDateTime windowEnd = windowStart.plusMinutes(endpointType.getWindowMinutes());
        return LocalDateTime.now().isAfter(windowEnd);
    }

    /**
     * Check if client is currently blocked
     */
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    /**
     * Reset the rate limit window
     */
    public void resetWindow() {
        this.windowStart = LocalDateTime.now();
        this.requestCount = 0;
        this.lastRequestTime = LocalDateTime.now();
    }

    /**
     * Increment request count
     */
    public void incrementRequestCount() {
        this.requestCount = (this.requestCount != null ? this.requestCount : 0) + 1;
        this.lastRequestTime = LocalDateTime.now();
        this.totalAllowedRequests = (this.totalAllowedRequests != null ? this.totalAllowedRequests : 0L) + 1;
    }

    /**
     * Block client for specified minutes
     */
    public void blockClient(int blockMinutes) {
        this.blockedUntil = LocalDateTime.now().plusMinutes(blockMinutes);
        this.violationCount = (this.violationCount != null ? this.violationCount : 0) + 1;
        
        if (this.firstViolationTime == null) {
            this.firstViolationTime = LocalDateTime.now();
        }
    }

    /**
     * Record blocked request
     */
    public void recordBlockedRequest() {
        this.totalBlockedRequests = (this.totalBlockedRequests != null ? this.totalBlockedRequests : 0L) + 1;
    }

    /**
     * Check if rate limit is exceeded
     */
    public boolean isRateLimitExceeded() {
        if (isWindowExpired()) {
            return false; // Window expired, rate limit reset
        }
        
        return requestCount != null && requestCount >= endpointType.getMaxRequests();
    }

    /**
     * Get remaining requests in current window
     */
    public int getRemainingRequests() {
        if (isWindowExpired()) {
            return endpointType.getMaxRequests();
        }
        
        int used = requestCount != null ? requestCount : 0;
        return Math.max(0, endpointType.getMaxRequests() - used);
    }

    /**
     * Get time until window reset in seconds
     */
    public long getTimeUntilResetSeconds() {
        if (isWindowExpired()) {
            return 0;
        }
        
        LocalDateTime windowEnd = windowStart.plusMinutes(endpointType.getWindowMinutes());
        return java.time.Duration.between(LocalDateTime.now(), windowEnd).getSeconds();
    }

    /**
     * Get violation rate (violations per total requests)
     */
    public double getViolationRate() {
        long totalRequests = (totalAllowedRequests != null ? totalAllowedRequests : 0L) + 
                           (totalBlockedRequests != null ? totalBlockedRequests : 0L);
        
        if (totalRequests == 0) {
            return 0.0;
        }
        
        return (totalBlockedRequests != null ? totalBlockedRequests.doubleValue() : 0.0) / totalRequests;
    }
}
