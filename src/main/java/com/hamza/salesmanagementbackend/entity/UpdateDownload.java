package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing download tracking information for application updates
 */
@Entity
@Table(name = "update_downloads",
       indexes = {
           @Index(name = "idx_version_id", columnList = "version_id"),
           @Index(name = "idx_download_status", columnList = "download_status"),
           @Index(name = "idx_client_identifier", columnList = "client_identifier")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false, foreignKey = @ForeignKey(name = "fk_update_download_version"))
    private ApplicationVersion applicationVersion;

    @Size(max = 255, message = "Client identifier must not exceed 255 characters")
    @Column(name = "client_identifier", length = 255)
    private String clientIdentifier;

    @Column(name = "download_started_at")
    @Builder.Default
    private LocalDateTime downloadStartedAt = LocalDateTime.now();

    @Column(name = "download_completed_at")
    private LocalDateTime downloadCompletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "download_status", nullable = false)
    @Builder.Default
    private DownloadStatus downloadStatus = DownloadStatus.STARTED;

    @Size(max = 45, message = "Client IP must not exceed 45 characters")
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Enumeration for download status
     */
    public enum DownloadStatus {
        STARTED("Download initiated"),
        IN_PROGRESS("Download in progress"),
        COMPLETED("Download completed successfully"),
        FAILED("Download failed");

        private final String description;

        DownloadStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        if (downloadStartedAt == null) {
            downloadStartedAt = LocalDateTime.now();
        }
    }

    /**
     * Mark download as completed
     */
    public void markAsCompleted() {
        this.downloadStatus = DownloadStatus.COMPLETED;
        this.downloadCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark download as failed
     */
    public void markAsFailed() {
        this.downloadStatus = DownloadStatus.FAILED;
        this.downloadCompletedAt = LocalDateTime.now();
    }

    /**
     * Check if download is completed
     */
    public boolean isCompleted() {
        return downloadStatus == DownloadStatus.COMPLETED;
    }

    /**
     * Get download duration in milliseconds
     */
    public Long getDownloadDurationMs() {
        if (downloadStartedAt == null || downloadCompletedAt == null) {
            return null;
        }
        return java.time.Duration.between(downloadStartedAt, downloadCompletedAt).toMillis();
    }
}
