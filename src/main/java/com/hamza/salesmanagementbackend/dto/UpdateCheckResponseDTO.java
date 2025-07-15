package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for update check response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCheckResponseDTO {

    @JsonProperty("updateAvailable")
    private Boolean updateAvailable;

    @JsonProperty("latestVersion")
    private String latestVersion;

    @JsonProperty("currentVersion")
    private String currentVersion;

    @JsonProperty("isMandatory")
    private Boolean isMandatory;

    @JsonProperty("releaseNotes")
    private String releaseNotes;

    @JsonProperty("downloadUrl")
    private String downloadUrl;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("formattedFileSize")
    private String formattedFileSize;

    @JsonProperty("checksum")
    private String checksum;

    @JsonProperty("minimumClientVersion")
    private String minimumClientVersion;

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
