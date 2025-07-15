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
 * Entity representing version history for rollback tracking
 */
@Entity
@Table(name = "version_history",
       indexes = {
           @Index(name = "idx_version_number", columnList = "version_number"),
           @Index(name = "idx_action_type", columnList = "action_type"),
           @Index(name = "idx_action_timestamp", columnList = "action_timestamp"),
           @Index(name = "idx_client_identifier", columnList = "client_identifier")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    @NotNull(message = "Application version is required")
    private ApplicationVersion applicationVersion;

    @NotBlank(message = "Version number is required")
    @Size(max = 20, message = "Version number must not exceed 20 characters")
    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    @NotNull(message = "Action type is required")
    private ActionType actionType;

    @Column(name = "action_timestamp", nullable = false)
    @Builder.Default
    private LocalDateTime actionTimestamp = LocalDateTime.now();

    @Size(max = 255, message = "Client identifier must not exceed 255 characters")
    @Column(name = "client_identifier", length = 255)
    private String clientIdentifier;

    @Size(max = 45, message = "Client IP must not exceed 45 characters")
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Size(max = 500, message = "User agent must not exceed 500 characters")
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Size(max = 20, message = "Previous version must not exceed 20 characters")
    @Column(name = "previous_version", length = 20)
    private String previousVersion;

    @Size(max = 20, message = "Target version must not exceed 20 characters")
    @Column(name = "target_version", length = 20)
    private String targetVersion;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Size(max = 100, message = "Initiated by must not exceed 100 characters")
    @Column(name = "initiated_by", length = 100)
    private String initiatedBy;

    @PrePersist
    protected void onCreate() {
        if (actionTimestamp == null) {
            actionTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Action types for version history
     */
    public enum ActionType {
        UPDATE("Update to new version"),
        ROLLBACK("Rollback to previous version"),
        INSTALL("Initial installation"),
        UNINSTALL("Application uninstallation"),
        REPAIR("Repair installation"),
        VERIFY("Version verification");

        private final String description;

        ActionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Mark action as successful
     */
    public void markSuccess(Integer durationSeconds) {
        this.success = true;
        this.durationSeconds = durationSeconds;
        this.errorMessage = null;
    }

    /**
     * Mark action as failed
     */
    public void markFailure(String errorMessage, Integer durationSeconds) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.durationSeconds = durationSeconds;
    }

    /**
     * Check if this is a rollback action
     */
    public boolean isRollback() {
        return actionType == ActionType.ROLLBACK;
    }

    /**
     * Check if this is an update action
     */
    public boolean isUpdate() {
        return actionType == ActionType.UPDATE;
    }

    /**
     * Get action duration in minutes
     */
    public Double getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60.0 : null;
    }
}
