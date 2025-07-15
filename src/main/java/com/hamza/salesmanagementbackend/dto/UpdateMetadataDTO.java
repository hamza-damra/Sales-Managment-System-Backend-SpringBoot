package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for update metadata without initiating download
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMetadataDTO {

    @JsonProperty("versionNumber")
    private String versionNumber;

    @JsonProperty("releaseDate")
    private LocalDateTime releaseDate;

    @JsonProperty("isMandatory")
    private Boolean isMandatory;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("releaseNotes")
    private String releaseNotes;

    @JsonProperty("minimumClientVersion")
    private String minimumClientVersion;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("formattedFileSize")
    private String formattedFileSize;

    @JsonProperty("fileChecksum")
    private String fileChecksum;

    @JsonProperty("checksumAlgorithm")
    @Builder.Default
    private String checksumAlgorithm = "SHA-256";

    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @JsonProperty("releaseChannel")
    private String releaseChannel;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Get formatted file size
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
