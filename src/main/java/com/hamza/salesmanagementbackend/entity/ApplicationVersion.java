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
import java.util.List;

/**
 * Entity representing application version information for the update system
 */
@Entity
@Table(name = "application_versions", 
       uniqueConstraints = @UniqueConstraint(columnNames = "version_number"),
       indexes = {
           @Index(name = "idx_version_number", columnList = "version_number"),
           @Index(name = "idx_is_active", columnList = "is_active"),
           @Index(name = "idx_release_date", columnList = "release_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Version number is required")
    @Size(max = 20, message = "Version number must not exceed 20 characters")
    @Column(name = "version_number", nullable = false, unique = true, length = 20)
    private String versionNumber;

    @NotNull(message = "Release date is required")
    @Column(name = "release_date", nullable = false)
    @Builder.Default
    private LocalDateTime releaseDate = LocalDateTime.now();

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "release_notes", columnDefinition = "TEXT")
    private String releaseNotes;

    @Size(max = 20, message = "Minimum client version must not exceed 20 characters")
    @Column(name = "minimum_client_version", length = 20)
    private String minimumClientVersion;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "File checksum is required")
    @Size(max = 64, message = "File checksum must not exceed 64 characters")
    @Column(name = "file_checksum", nullable = false, length = 64)
    private String fileChecksum;

    @NotBlank(message = "Download URL is required")
    @Size(max = 500, message = "Download URL must not exceed 500 characters")
    @Column(name = "download_url", nullable = false, length = 500)
    private String downloadUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_channel", length = 20)
    @Builder.Default
    private ReleaseChannel releaseChannel = ReleaseChannel.STABLE;

    // Relationship with UpdateDownload
    @OneToMany(mappedBy = "applicationVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UpdateDownload> downloads;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (releaseDate == null) {
            releaseDate = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this version is newer than the given version
     * Simple semantic version comparison (assumes format like "2.1.0")
     */
    public boolean isNewerThan(String otherVersion) {
        if (otherVersion == null || otherVersion.trim().isEmpty()) {
            return true;
        }
        
        try {
            String[] thisParts = this.versionNumber.split("\\.");
            String[] otherParts = otherVersion.split("\\.");
            
            int maxLength = Math.max(thisParts.length, otherParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
                int otherPart = i < otherParts.length ? Integer.parseInt(otherParts[i]) : 0;
                
                if (thisPart > otherPart) {
                    return true;
                } else if (thisPart < otherPart) {
                    return false;
                }
            }
            
            return false; // Versions are equal
        } catch (NumberFormatException e) {
            // Fallback to string comparison if parsing fails
            return this.versionNumber.compareTo(otherVersion) > 0;
        }
    }

    /**
     * Get formatted file size in human-readable format
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
