package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing connected clients for WebSocket update notifications
 */
@Entity
@Table(name = "connected_clients",
       uniqueConstraints = @UniqueConstraint(columnNames = "session_id"),
       indexes = {
           @Index(name = "idx_session_id", columnList = "session_id"),
           @Index(name = "idx_client_version", columnList = "client_version"),
           @Index(name = "idx_is_active", columnList = "is_active")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectedClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Session ID is required")
    @Size(max = 255, message = "Session ID must not exceed 255 characters")
    @Column(name = "session_id", nullable = false, unique = true, length = 255)
    private String sessionId;

    @Size(max = 20, message = "Client version must not exceed 20 characters")
    @Column(name = "client_version", length = 20)
    private String clientVersion;

    @Column(name = "connected_at")
    @Builder.Default
    private LocalDateTime connectedAt = LocalDateTime.now();

    @Column(name = "last_ping_at")
    @Builder.Default
    private LocalDateTime lastPingAt = LocalDateTime.now();

    @Size(max = 45, message = "Client IP must not exceed 45 characters")
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (connectedAt == null) {
            connectedAt = now;
        }
        if (lastPingAt == null) {
            lastPingAt = now;
        }
    }

    /**
     * Update the last ping timestamp to current time
     */
    public void updateLastPing() {
        this.lastPingAt = LocalDateTime.now();
    }

    /**
     * Mark client as disconnected
     */
    public void disconnect() {
        this.isActive = false;
    }

    /**
     * Mark client as connected
     */
    public void connect() {
        this.isActive = true;
        this.connectedAt = LocalDateTime.now();
        this.lastPingAt = LocalDateTime.now();
    }

    /**
     * Check if client connection is stale based on last ping time
     * @param timeoutMinutes timeout in minutes
     * @return true if connection is stale
     */
    public boolean isStale(long timeoutMinutes) {
        if (lastPingAt == null) {
            return true;
        }
        
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(timeoutMinutes);
        return lastPingAt.isBefore(timeout);
    }

    /**
     * Get connection duration in minutes
     */
    public long getConnectionDurationMinutes() {
        if (connectedAt == null) {
            return 0;
        }
        
        return java.time.Duration.between(connectedAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Get time since last ping in minutes
     */
    public long getTimeSinceLastPingMinutes() {
        if (lastPingAt == null) {
            return Long.MAX_VALUE;
        }
        
        return java.time.Duration.between(lastPingAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Check if client needs an update based on version comparison
     */
    public boolean needsUpdate(String latestVersion) {
        if (clientVersion == null || latestVersion == null) {
            return false;
        }
        
        try {
            String[] clientParts = clientVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");
            
            int maxLength = Math.max(clientParts.length, latestParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int clientPart = i < clientParts.length ? Integer.parseInt(clientParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                
                if (latestPart > clientPart) {
                    return true;
                } else if (latestPart < clientPart) {
                    return false;
                }
            }
            
            return false; // Versions are equal
        } catch (NumberFormatException e) {
            // Fallback to string comparison if parsing fails
            return latestVersion.compareTo(clientVersion) > 0;
        }
    }
}
