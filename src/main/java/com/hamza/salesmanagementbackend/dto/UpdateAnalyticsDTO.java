package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for comprehensive update analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAnalyticsDTO {

    @JsonProperty("reportGeneratedAt")
    private LocalDateTime reportGeneratedAt;

    @JsonProperty("reportPeriod")
    private ReportPeriod reportPeriod;

    @JsonProperty("successfulUpdates")
    private UpdateSuccessMetrics successfulUpdates;

    @JsonProperty("failedUpdates")
    private UpdateFailureMetrics failedUpdates;

    @JsonProperty("rollbackStatistics")
    private RollbackStatistics rollbackStatistics;

    @JsonProperty("downloadCompletionRates")
    private DownloadCompletionMetrics downloadCompletionRates;

    @JsonProperty("versionAdoption")
    private List<VersionAdoptionMetric> versionAdoption;

    @JsonProperty("channelStatistics")
    private Map<String, ChannelStatistics> channelStatistics;

    @JsonProperty("geographicDistribution")
    private List<GeographicMetric> geographicDistribution;

    @JsonProperty("performanceMetrics")
    private PerformanceMetrics performanceMetrics;

    @JsonProperty("errorCategories")
    private List<ErrorCategoryMetric> errorCategories;

    /**
     * Report period information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportPeriod {
        @JsonProperty("startDate")
        private LocalDateTime startDate;

        @JsonProperty("endDate")
        private LocalDateTime endDate;

        @JsonProperty("periodType")
        private String periodType; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY

        @JsonProperty("totalDays")
        private Integer totalDays;
    }

    /**
     * Successful update metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateSuccessMetrics {
        @JsonProperty("totalSuccessful")
        private Long totalSuccessful;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageDownloadTimeMinutes")
        private Double averageDownloadTimeMinutes;

        @JsonProperty("fastestUpdateMinutes")
        private Double fastestUpdateMinutes;

        @JsonProperty("slowestUpdateMinutes")
        private Double slowestUpdateMinutes;

        @JsonProperty("successByVersion")
        private Map<String, Long> successByVersion;
    }

    /**
     * Failed update metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateFailureMetrics {
        @JsonProperty("totalFailed")
        private Long totalFailed;

        @JsonProperty("failureRate")
        private Double failureRate;

        @JsonProperty("failuresByReason")
        private Map<String, Long> failuresByReason;

        @JsonProperty("failuresByVersion")
        private Map<String, Long> failuresByVersion;

        @JsonProperty("retryAttempts")
        private Long retryAttempts;

        @JsonProperty("averageRetryCount")
        private Double averageRetryCount;
    }

    /**
     * Rollback statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RollbackStatistics {
        @JsonProperty("totalRollbacks")
        private Long totalRollbacks;

        @JsonProperty("rollbackRate")
        private Double rollbackRate;

        @JsonProperty("rollbacksByVersion")
        private Map<String, Long> rollbacksByVersion;

        @JsonProperty("rollbackReasons")
        private Map<String, Long> rollbackReasons;

        @JsonProperty("averageTimeToRollbackMinutes")
        private Double averageTimeToRollbackMinutes;
    }

    /**
     * Download completion metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DownloadCompletionMetrics {
        @JsonProperty("totalDownloads")
        private Long totalDownloads;

        @JsonProperty("completedDownloads")
        private Long completedDownloads;

        @JsonProperty("partialDownloads")
        private Long partialDownloads;

        @JsonProperty("failedDownloads")
        private Long failedDownloads;

        @JsonProperty("completionRate")
        private Double completionRate;

        @JsonProperty("averageDownloadSpeedMbps")
        private Double averageDownloadSpeedMbps;

        @JsonProperty("resumedDownloads")
        private Long resumedDownloads;
    }

    /**
     * Version adoption metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VersionAdoptionMetric {
        @JsonProperty("version")
        private String version;

        @JsonProperty("adoptionCount")
        private Long adoptionCount;

        @JsonProperty("adoptionPercentage")
        private Double adoptionPercentage;

        @JsonProperty("releaseDate")
        private LocalDateTime releaseDate;

        @JsonProperty("daysToAdoption")
        private Integer daysToAdoption;

        @JsonProperty("channel")
        private String channel;
    }

    /**
     * Channel-specific statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelStatistics {
        @JsonProperty("channel")
        private String channel;

        @JsonProperty("subscriberCount")
        private Long subscriberCount;

        @JsonProperty("activeUsers")
        private Long activeUsers;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageAdoptionDays")
        private Double averageAdoptionDays;

        @JsonProperty("totalReleases")
        private Long totalReleases;
    }

    /**
     * Geographic distribution metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeographicMetric {
        @JsonProperty("country")
        private String country;

        @JsonProperty("region")
        private String region;

        @JsonProperty("downloadCount")
        private Long downloadCount;

        @JsonProperty("successRate")
        private Double successRate;

        @JsonProperty("averageSpeedMbps")
        private Double averageSpeedMbps;
    }

    /**
     * Performance metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceMetrics {
        @JsonProperty("averageResponseTimeMs")
        private Double averageResponseTimeMs;

        @JsonProperty("peakConcurrentDownloads")
        private Long peakConcurrentDownloads;

        @JsonProperty("bandwidthUtilizationMbps")
        private Double bandwidthUtilizationMbps;

        @JsonProperty("serverUptimePercentage")
        private Double serverUptimePercentage;

        @JsonProperty("cacheHitRate")
        private Double cacheHitRate;
    }

    /**
     * Error category metrics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorCategoryMetric {
        @JsonProperty("category")
        private String category;

        @JsonProperty("errorCount")
        private Long errorCount;

        @JsonProperty("percentage")
        private Double percentage;

        @JsonProperty("topErrors")
        private List<String> topErrors;

        @JsonProperty("resolution")
        private String resolution;
    }
}
