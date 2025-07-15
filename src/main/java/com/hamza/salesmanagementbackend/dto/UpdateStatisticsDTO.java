package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for update system statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStatisticsDTO {

    // Version Statistics
    @JsonProperty("totalVersions")
    private Long totalVersions;

    @JsonProperty("activeVersions")
    private Long activeVersions;

    @JsonProperty("mandatoryVersions")
    private Long mandatoryVersions;

    @JsonProperty("averageFileSize")
    private Double averageFileSize;

    // Download Statistics
    @JsonProperty("totalDownloads")
    private Long totalDownloads;

    @JsonProperty("successfulDownloads")
    private Long successfulDownloads;

    @JsonProperty("failedDownloads")
    private Long failedDownloads;

    @JsonProperty("inProgressDownloads")
    private Long inProgressDownloads;

    @JsonProperty("averageDownloadTimeSeconds")
    private Double averageDownloadTimeSeconds;

    @JsonProperty("downloadSuccessRate")
    private Double downloadSuccessRate;

    // Client Statistics
    @JsonProperty("totalConnectedClients")
    private Long totalConnectedClients;

    @JsonProperty("activeClients")
    private Long activeClients;

    @JsonProperty("averageConnectionTimeMinutes")
    private Double averageConnectionTimeMinutes;

    // Version Distribution
    @JsonProperty("clientVersionDistribution")
    private Map<String, Long> clientVersionDistribution;

    // Download Trends (last 30 days)
    @JsonProperty("dailyDownloadCounts")
    private List<DailyDownloadCount> dailyDownloadCounts;

    // Top Versions by Downloads
    @JsonProperty("topVersionsByDownloads")
    private List<VersionDownloadStat> topVersionsByDownloads;

    // Recent Activity
    @JsonProperty("recentDownloads")
    private List<RecentDownloadDTO> recentDownloads;

    /**
     * Calculate download success rate
     */
    public Double getDownloadSuccessRate() {
        if (totalDownloads == null || totalDownloads == 0) {
            return 0.0;
        }
        if (successfulDownloads == null) {
            return 0.0;
        }
        return (successfulDownloads.doubleValue() / totalDownloads.doubleValue()) * 100.0;
    }

    /**
     * Get formatted average file size
     */
    public String getFormattedAverageFileSize() {
        if (averageFileSize == null) {
            return "Unknown";
        }
        
        long bytes = averageFileSize.longValue();
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Inner class for daily download counts
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyDownloadCount {
        @JsonProperty("date")
        private String date;

        @JsonProperty("downloadCount")
        private Long downloadCount;
    }

    /**
     * Inner class for version download statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VersionDownloadStat {
        @JsonProperty("versionNumber")
        private String versionNumber;

        @JsonProperty("downloadCount")
        private Long downloadCount;

        @JsonProperty("successfulDownloads")
        private Long successfulDownloads;

        @JsonProperty("failedDownloads")
        private Long failedDownloads;

        @JsonProperty("successRate")
        private Double successRate;
    }

    /**
     * Inner class for recent download information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentDownloadDTO {
        @JsonProperty("versionNumber")
        private String versionNumber;

        @JsonProperty("clientIdentifier")
        private String clientIdentifier;

        @JsonProperty("downloadStatus")
        private String downloadStatus;

        @JsonProperty("downloadStartedAt")
        private String downloadStartedAt;

        @JsonProperty("clientIp")
        private String clientIp;
    }
}
