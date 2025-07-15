package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for ApplicationVersion entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationVersionDTO {

    private Long id;

    @NotBlank(message = "Version number is required")
    @Size(max = 20, message = "Version number must not exceed 20 characters")
    @JsonProperty("versionNumber")
    private String versionNumber;

    @NotNull(message = "Release date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("releaseDate")
    private LocalDateTime releaseDate;

    @JsonProperty("isMandatory")
    private Boolean isMandatory;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("releaseNotes")
    private String releaseNotes;

    @Size(max = 20, message = "Minimum client version must not exceed 20 characters")
    @JsonProperty("minimumClientVersion")
    private String minimumClientVersion;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @JsonProperty("fileName")
    private String fileName;

    @NotNull(message = "File size is required")
    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("formattedFileSize")
    private String formattedFileSize;

    @NotBlank(message = "File checksum is required")
    @Size(max = 64, message = "File checksum must not exceed 64 characters")
    @JsonProperty("checksum")
    private String fileChecksum;

    @NotBlank(message = "Download URL is required")
    @Size(max = 500, message = "Download URL must not exceed 500 characters")
    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @Size(max = 100, message = "Created by must not exceed 100 characters")
    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("releaseChannel")
    private String releaseChannel;

    // Statistics fields (populated when needed)
    @JsonProperty("downloadCount")
    private Long downloadCount;

    @JsonProperty("successfulDownloads")
    private Long successfulDownloads;

    @JsonProperty("failedDownloads")
    private Long failedDownloads;

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

    /**
     * Check if this version is newer than the given version
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
}
