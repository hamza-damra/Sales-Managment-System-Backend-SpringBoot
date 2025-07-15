package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for differential update information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DifferentialUpdateDTO {

    @JsonProperty("fromVersion")
    private String fromVersion;

    @JsonProperty("toVersion")
    private String toVersion;

    @JsonProperty("deltaAvailable")
    private Boolean deltaAvailable;

    @JsonProperty("deltaSize")
    private Long deltaSize;

    @JsonProperty("formattedDeltaSize")
    private String formattedDeltaSize;

    @JsonProperty("fullUpdateSize")
    private Long fullUpdateSize;

    @JsonProperty("formattedFullUpdateSize")
    private String formattedFullUpdateSize;

    @JsonProperty("compressionRatio")
    private Double compressionRatio;

    @JsonProperty("deltaChecksum")
    private String deltaChecksum;

    @JsonProperty("deltaDownloadUrl")
    private String deltaDownloadUrl;

    @JsonProperty("fullDownloadUrl")
    private String fullDownloadUrl;

    @JsonProperty("changedFiles")
    private List<ChangedFile> changedFiles;

    @JsonProperty("patchInstructions")
    private List<PatchInstruction> patchInstructions;

    @JsonProperty("fallbackToFull")
    private Boolean fallbackToFull;

    @JsonProperty("fallbackReason")
    private String fallbackReason;

    @JsonProperty("estimatedApplyTimeSeconds")
    private Integer estimatedApplyTimeSeconds;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    /**
     * Information about changed files
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChangedFile {
        @JsonProperty("path")
        private String path;

        @JsonProperty("operation")
        private FileOperation operation;

        @JsonProperty("oldChecksum")
        private String oldChecksum;

        @JsonProperty("newChecksum")
        private String newChecksum;

        @JsonProperty("size")
        private Long size;
    }

    /**
     * Patch instruction for applying delta
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatchInstruction {
        @JsonProperty("order")
        private Integer order;

        @JsonProperty("operation")
        private PatchOperation operation;

        @JsonProperty("target")
        private String target;

        @JsonProperty("source")
        private String source;

        @JsonProperty("checksum")
        private String checksum;
    }

    /**
     * File operations in delta
     */
    public enum FileOperation {
        ADDED,
        MODIFIED,
        DELETED,
        MOVED,
        RENAMED
    }

    /**
     * Patch operations
     */
    public enum PatchOperation {
        COPY,
        EXTRACT,
        DELETE,
        MOVE,
        VERIFY
    }

    /**
     * Get formatted delta size
     */
    public String getFormattedDeltaSize() {
        if (deltaSize == null) {
            return "Unknown";
        }
        return formatBytes(deltaSize);
    }

    /**
     * Get formatted full update size
     */
    public String getFormattedFullUpdateSize() {
        if (fullUpdateSize == null) {
            return "Unknown";
        }
        return formatBytes(fullUpdateSize);
    }

    /**
     * Calculate compression ratio percentage
     */
    public Double getCompressionRatioPercentage() {
        if (deltaSize == null || fullUpdateSize == null || fullUpdateSize == 0) {
            return null;
        }
        return (1.0 - (deltaSize.doubleValue() / fullUpdateSize.doubleValue())) * 100.0;
    }

    /**
     * Format bytes to human readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
