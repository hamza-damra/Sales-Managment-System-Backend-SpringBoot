package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.websocket.UpdateWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for managing WebSocket update notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketUpdateService {

    private final UpdateWebSocketHandler webSocketHandler;

    /**
     * Notify all clients about a new version release
     */
    public void notifyNewVersionRelease(ApplicationVersionDTO version) {
        log.info("Notifying clients about new version release: {}", version.getVersionNumber());

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "NEW_VERSION_AVAILABLE",
            Map.of(
                "version", version.getVersionNumber(),
                "releaseDate", version.getReleaseDate().toString(),
                "isMandatory", version.getIsMandatory(),
                "releaseNotes", version.getReleaseNotes() != null ? version.getReleaseNotes() : "",
                "downloadUrl", version.getDownloadUrl(),
                "fileSize", version.getFileSize(),
                "formattedFileSize", version.getFormattedFileSize(),
                "checksum", version.getFileChecksum(),
                "releaseChannel", version.getReleaseChannel() != null ? version.getReleaseChannel() : "STABLE",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Notify clients subscribed to specific channel about new version
     */
    public void notifyChannelSubscribers(String channel, ApplicationVersionDTO version) {
        log.info("Notifying channel '{}' subscribers about new version: {}", channel, version.getVersionNumber());

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "CHANNEL_VERSION_AVAILABLE",
            Map.of(
                "channel", channel,
                "version", version.getVersionNumber(),
                "releaseDate", version.getReleaseDate().toString(),
                "isMandatory", version.getIsMandatory(),
                "releaseNotes", version.getReleaseNotes() != null ? version.getReleaseNotes() : "",
                "downloadUrl", version.getDownloadUrl(),
                "fileSize", version.getFileSize(),
                "formattedFileSize", version.getFormattedFileSize(),
                "checksum", version.getFileChecksum(),
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastToChannel(channel, message);
    }

    /**
     * Notify about download progress
     */
    public void notifyDownloadProgress(String clientId, String version, int progressPercentage, 
                                     long bytesDownloaded, long totalBytes) {
        log.debug("Notifying download progress for client {}: {}% ({}/{})", 
                 clientId, progressPercentage, bytesDownloaded, totalBytes);

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "DOWNLOAD_PROGRESS",
            Map.of(
                "clientId", clientId,
                "version", version,
                "progressPercentage", progressPercentage,
                "bytesDownloaded", bytesDownloaded,
                "totalBytes", totalBytes,
                "timestamp", LocalDateTime.now().toString()
            )
        );

        // Note: In a real implementation, you'd want to send this only to the specific client
        // This would require maintaining a mapping of client IDs to WebSocket sessions
        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Notify about download completion
     */
    public void notifyDownloadCompleted(String clientId, String version, boolean success, String message) {
        log.info("Notifying download completion for client {}: {} - {}", clientId, version, 
                success ? "SUCCESS" : "FAILED");

        UpdateWebSocketHandler.WebSocketMessage wsMessage = new UpdateWebSocketHandler.WebSocketMessage(
            "DOWNLOAD_COMPLETED",
            Map.of(
                "clientId", clientId,
                "version", version,
                "success", success,
                "message", message != null ? message : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(wsMessage);
    }

    /**
     * Notify about update installation progress
     */
    public void notifyInstallationProgress(String clientId, String version, String phase, 
                                         int progressPercentage, String currentOperation) {
        log.debug("Notifying installation progress for client {}: {} - {} ({}%)", 
                 clientId, version, phase, progressPercentage);

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "INSTALLATION_PROGRESS",
            Map.of(
                "clientId", clientId,
                "version", version,
                "phase", phase,
                "progressPercentage", progressPercentage,
                "currentOperation", currentOperation != null ? currentOperation : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Notify about update installation completion
     */
    public void notifyInstallationCompleted(String clientId, String version, boolean success, 
                                          String message, String previousVersion) {
        log.info("Notifying installation completion for client {}: {} - {}", clientId, version, 
                success ? "SUCCESS" : "FAILED");

        UpdateWebSocketHandler.WebSocketMessage wsMessage = new UpdateWebSocketHandler.WebSocketMessage(
            "INSTALLATION_COMPLETED",
            Map.of(
                "clientId", clientId,
                "version", version,
                "previousVersion", previousVersion != null ? previousVersion : "",
                "success", success,
                "message", message != null ? message : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(wsMessage);
    }

    /**
     * Notify about rollback initiation
     */
    public void notifyRollbackInitiated(String clientId, String fromVersion, String toVersion, String reason) {
        log.info("Notifying rollback initiation for client {}: {} -> {}", clientId, fromVersion, toVersion);

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "ROLLBACK_INITIATED",
            Map.of(
                "clientId", clientId,
                "fromVersion", fromVersion,
                "toVersion", toVersion,
                "reason", reason != null ? reason : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Notify about rollback completion
     */
    public void notifyRollbackCompleted(String clientId, String fromVersion, String toVersion, 
                                      boolean success, String message) {
        log.info("Notifying rollback completion for client {}: {} -> {} - {}", 
                clientId, fromVersion, toVersion, success ? "SUCCESS" : "FAILED");

        UpdateWebSocketHandler.WebSocketMessage wsMessage = new UpdateWebSocketHandler.WebSocketMessage(
            "ROLLBACK_COMPLETED",
            Map.of(
                "clientId", clientId,
                "fromVersion", fromVersion,
                "toVersion", toVersion,
                "success", success,
                "message", message != null ? message : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(wsMessage);
    }

    /**
     * Notify about system maintenance
     */
    public void notifySystemMaintenance(String message, LocalDateTime scheduledTime, 
                                      int estimatedDurationMinutes) {
        log.info("Notifying system maintenance: {}", message);

        UpdateWebSocketHandler.WebSocketMessage wsMessage = new UpdateWebSocketHandler.WebSocketMessage(
            "SYSTEM_MAINTENANCE",
            Map.of(
                "message", message,
                "scheduledTime", scheduledTime.toString(),
                "estimatedDurationMinutes", estimatedDurationMinutes,
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(wsMessage);
    }

    /**
     * Notify about server status changes
     */
    public void notifyServerStatus(String status, String message) {
        log.info("Notifying server status change: {} - {}", status, message);

        UpdateWebSocketHandler.WebSocketMessage wsMessage = new UpdateWebSocketHandler.WebSocketMessage(
            "SERVER_STATUS",
            Map.of(
                "status", status,
                "message", message != null ? message : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(wsMessage);
    }

    /**
     * Notify about rate limiting
     */
    public void notifyRateLimited(String clientId, String endpoint, long resetTimeSeconds) {
        log.warn("Notifying rate limit for client {}: {} (reset in {} seconds)", 
                clientId, endpoint, resetTimeSeconds);

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "RATE_LIMITED",
            Map.of(
                "clientId", clientId,
                "endpoint", endpoint,
                "resetTimeSeconds", resetTimeSeconds,
                "message", "Rate limit exceeded. Please wait before making more requests.",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Notify about compatibility issues
     */
    public void notifyCompatibilityIssue(String clientId, String version, String issueType, 
                                       String description, String resolution) {
        log.warn("Notifying compatibility issue for client {}: {} - {}", clientId, version, issueType);

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(
            "COMPATIBILITY_ISSUE",
            Map.of(
                "clientId", clientId,
                "version", version,
                "issueType", issueType,
                "description", description != null ? description : "",
                "resolution", resolution != null ? resolution : "",
                "timestamp", LocalDateTime.now().toString()
            )
        );

        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Send custom notification to all clients
     */
    public void sendCustomNotification(String type, Map<String, Object> data) {
        log.info("Sending custom notification: {}", type);

        // Add timestamp to data
        data.put("timestamp", LocalDateTime.now().toString());

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(type, data);
        webSocketHandler.broadcastMessage(message);
    }

    /**
     * Send custom notification to specific channel
     */
    public void sendCustomNotificationToChannel(String channel, String type, Map<String, Object> data) {
        log.info("Sending custom notification to channel '{}': {}", channel, type);

        // Add timestamp to data
        data.put("timestamp", LocalDateTime.now().toString());

        UpdateWebSocketHandler.WebSocketMessage message = new UpdateWebSocketHandler.WebSocketMessage(type, data);
        webSocketHandler.broadcastToChannel(channel, message);
    }

    /**
     * Get current connection statistics
     */
    public Map<String, Object> getConnectionStatistics() {
        int activeConnections = webSocketHandler.getActiveConnectionCount();
        
        return Map.of(
            "activeConnections", activeConnections,
            "timestamp", LocalDateTime.now().toString()
        );
    }
}
