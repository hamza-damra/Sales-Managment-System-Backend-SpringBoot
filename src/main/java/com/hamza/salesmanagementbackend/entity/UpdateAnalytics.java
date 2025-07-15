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
 * Entity for storing detailed update analytics
 */
@Entity
@Table(name = "update_analytics",
       indexes = {
           @Index(name = "idx_event_type", columnList = "event_type"),
           @Index(name = "idx_event_timestamp", columnList = "event_timestamp"),
           @Index(name = "idx_version_number", columnList = "version_number"),
           @Index(name = "idx_client_identifier", columnList = "client_identifier"),
           @Index(name = "idx_release_channel", columnList = "release_channel")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    @NotNull(message = "Event type is required")
    private EventType eventType;

    @Column(name = "event_timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime eventTimestamp = LocalDateTime.now();

    @Size(max = 20, message = "Version number must not exceed 20 characters")
    @Column(name = "version_number", length = 20)
    private String versionNumber;

    @Size(max = 255, message = "Client identifier must not exceed 255 characters")
    @Column(name = "client_identifier", length = 255)
    private String clientIdentifier;

    @Size(max = 45, message = "Client IP must not exceed 45 characters")
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_channel", length = 20)
    private ReleaseChannel releaseChannel;

    @Column(name = "download_size_bytes")
    private Long downloadSizeBytes;

    @Column(name = "download_duration_seconds")
    private Integer downloadDurationSeconds;

    @Column(name = "download_speed_mbps")
    private Double downloadSpeedMbps;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "resumed_download")
    @Builder.Default
    private Boolean resumedDownload = false;

    @Column(name = "bytes_already_downloaded")
    private Long bytesAlreadyDownloaded;

    @Size(max = 100, message = "Country code must not exceed 100 characters")
    @Column(name = "country_code", length = 100)
    private String countryCode;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "connection_type", length = 50)
    private String connectionType;

    @Column(name = "is_delta_update")
    @Builder.Default
    private Boolean isDeltaUpdate = false;

    @Column(name = "delta_compression_ratio")
    private Double deltaCompressionRatio;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @PrePersist
    protected void onCreate() {
        if (eventTimestamp == null) {
            eventTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Event types for analytics
     */
    public enum EventType {
        UPDATE_CHECK("Client checked for updates"),
        DOWNLOAD_STARTED("Download started"),
        DOWNLOAD_PROGRESS("Download progress update"),
        DOWNLOAD_COMPLETED("Download completed successfully"),
        DOWNLOAD_FAILED("Download failed"),
        DOWNLOAD_RESUMED("Download resumed after interruption"),
        UPDATE_APPLIED("Update applied successfully"),
        UPDATE_FAILED("Update application failed"),
        ROLLBACK_INITIATED("Rollback initiated"),
        ROLLBACK_COMPLETED("Rollback completed"),
        COMPATIBILITY_CHECK("Compatibility check performed"),
        METADATA_REQUEST("Metadata requested"),
        DELTA_GENERATED("Delta update generated"),
        DELTA_APPLIED("Delta update applied"),
        CHANNEL_SUBSCRIPTION("Client subscribed to channel"),
        RATE_LIMITED("Request was rate limited");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Calculate download speed in Mbps
     */
    public void calculateDownloadSpeed() {
        if (downloadSizeBytes != null && downloadDurationSeconds != null && downloadDurationSeconds > 0) {
            double megabits = (downloadSizeBytes * 8.0) / (1024.0 * 1024.0);
            this.downloadSpeedMbps = megabits / downloadDurationSeconds;
        }
    }

    /**
     * Mark event as successful
     */
    public void markSuccess() {
        this.success = true;
        this.errorCode = null;
        this.errorMessage = null;
    }

    /**
     * Mark event as failed
     */
    public void markFailure(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
    }

    /**
     * Check if this is a download event
     */
    public boolean isDownloadEvent() {
        return eventType == EventType.DOWNLOAD_STARTED ||
               eventType == EventType.DOWNLOAD_PROGRESS ||
               eventType == EventType.DOWNLOAD_COMPLETED ||
               eventType == EventType.DOWNLOAD_FAILED ||
               eventType == EventType.DOWNLOAD_RESUMED;
    }

    /**
     * Check if this is an update event
     */
    public boolean isUpdateEvent() {
        return eventType == EventType.UPDATE_APPLIED ||
               eventType == EventType.UPDATE_FAILED;
    }
}
