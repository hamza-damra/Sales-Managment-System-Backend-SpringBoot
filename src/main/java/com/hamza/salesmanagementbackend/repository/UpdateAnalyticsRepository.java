package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.ReleaseChannel;
import com.hamza.salesmanagementbackend.entity.UpdateAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for UpdateAnalytics entity
 */
@Repository
public interface UpdateAnalyticsRepository extends JpaRepository<UpdateAnalytics, Long> {

    /**
     * Find analytics by event type
     */
    List<UpdateAnalytics> findByEventTypeOrderByEventTimestampDesc(UpdateAnalytics.EventType eventType);

    /**
     * Find analytics by client identifier
     */
    List<UpdateAnalytics> findByClientIdentifierOrderByEventTimestampDesc(String clientIdentifier);

    /**
     * Find analytics by version number
     */
    List<UpdateAnalytics> findByVersionNumberOrderByEventTimestampDesc(String versionNumber);

    /**
     * Find analytics by release channel
     */
    List<UpdateAnalytics> findByReleaseChannelOrderByEventTimestampDesc(ReleaseChannel releaseChannel);

    /**
     * Find analytics within date range
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.eventTimestamp BETWEEN :startDate AND :endDate ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find successful downloads
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' AND ua.success = true ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findSuccessfulDownloads();

    /**
     * Find failed downloads
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.eventType IN ('DOWNLOAD_FAILED', 'UPDATE_FAILED') AND ua.success = false ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findFailedDownloads();

    /**
     * Get download statistics
     */
    @Query("SELECT " +
           "COUNT(ua) as totalDownloads, " +
           "SUM(CASE WHEN ua.success = true THEN 1 ELSE 0 END) as successfulDownloads, " +
           "SUM(CASE WHEN ua.success = false THEN 1 ELSE 0 END) as failedDownloads, " +
           "AVG(ua.downloadDurationSeconds) as avgDurationSeconds, " +
           "AVG(ua.downloadSpeedMbps) as avgSpeedMbps " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED'")
    Object[] getDownloadStatistics();

    /**
     * Get download statistics by version
     */
    @Query("SELECT ua.versionNumber, " +
           "COUNT(ua) as totalDownloads, " +
           "SUM(CASE WHEN ua.success = true THEN 1 ELSE 0 END) as successfulDownloads, " +
           "AVG(ua.downloadDurationSeconds) as avgDurationSeconds " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' " +
           "GROUP BY ua.versionNumber")
    List<Object[]> getDownloadStatisticsByVersion();

    /**
     * Get download statistics by channel
     */
    @Query("SELECT ua.releaseChannel, " +
           "COUNT(ua) as totalDownloads, " +
           "SUM(CASE WHEN ua.success = true THEN 1 ELSE 0 END) as successfulDownloads, " +
           "AVG(ua.downloadSpeedMbps) as avgSpeedMbps " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' " +
           "GROUP BY ua.releaseChannel")
    List<Object[]> getDownloadStatisticsByChannel();

    /**
     * Get error statistics
     */
    @Query("SELECT ua.errorCode, COUNT(ua) as errorCount " +
           "FROM UpdateAnalytics ua WHERE ua.success = false AND ua.errorCode IS NOT NULL " +
           "GROUP BY ua.errorCode ORDER BY COUNT(ua) DESC")
    List<Object[]> getErrorStatistics();

    /**
     * Get geographic distribution
     */
    @Query("SELECT ua.countryCode, ua.region, COUNT(ua) as downloadCount, AVG(ua.downloadSpeedMbps) as avgSpeed " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' AND ua.countryCode IS NOT NULL " +
           "GROUP BY ua.countryCode, ua.region ORDER BY COUNT(ua) DESC")
    List<Object[]> getGeographicDistribution();

    /**
     * Get daily download counts
     */
    @Query("SELECT CAST(ua.eventTimestamp AS date) as downloadDate, COUNT(ua) as downloadCount " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' " +
           "AND ua.eventTimestamp >= :startDate " +
           "GROUP BY CAST(ua.eventTimestamp AS date) ORDER BY CAST(ua.eventTimestamp AS date) DESC")
    List<Object[]> getDailyDownloadCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Get hourly download distribution
     */
    @Query("SELECT EXTRACT(HOUR FROM ua.eventTimestamp) as hour, COUNT(ua) as downloadCount " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' " +
           "AND ua.eventTimestamp >= :startDate " +
           "GROUP BY EXTRACT(HOUR FROM ua.eventTimestamp) ORDER BY hour")
    List<Object[]> getHourlyDownloadDistribution(@Param("startDate") LocalDateTime startDate);

    /**
     * Find resumed downloads
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.resumedDownload = true ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findResumedDownloads();

    /**
     * Get delta update statistics
     */
    @Query("SELECT " +
           "COUNT(ua) as totalDeltaUpdates, " +
           "AVG(ua.deltaCompressionRatio) as avgCompressionRatio, " +
           "SUM(CASE WHEN ua.success = true THEN 1 ELSE 0 END) as successfulDeltaUpdates " +
           "FROM UpdateAnalytics ua WHERE ua.isDeltaUpdate = true")
    Object[] getDeltaUpdateStatistics();

    /**
     * Find clients with high retry counts
     */
    @Query("SELECT ua.clientIdentifier, AVG(ua.retryCount) as avgRetryCount, COUNT(ua) as totalAttempts " +
           "FROM UpdateAnalytics ua WHERE ua.retryCount > 0 " +
           "GROUP BY ua.clientIdentifier " +
           "HAVING AVG(ua.retryCount) > :minRetryCount " +
           "ORDER BY AVG(ua.retryCount) DESC")
    List<Object[]> findClientsWithHighRetryCount(@Param("minRetryCount") double minRetryCount);

    /**
     * Get performance metrics
     */
    @Query("SELECT " +
           "AVG(ua.downloadSpeedMbps) as avgSpeedMbps, " +
           "MIN(ua.downloadSpeedMbps) as minSpeedMbps, " +
           "MAX(ua.downloadSpeedMbps) as maxSpeedMbps, " +
           "AVG(ua.downloadDurationSeconds) as avgDurationSeconds " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'DOWNLOAD_COMPLETED' AND ua.success = true")
    Object[] getPerformanceMetrics();

    /**
     * Find analytics by connection type
     */
    @Query("SELECT ua.connectionType, COUNT(ua) as count, AVG(ua.downloadSpeedMbps) as avgSpeed " +
           "FROM UpdateAnalytics ua WHERE ua.connectionType IS NOT NULL " +
           "GROUP BY ua.connectionType ORDER BY COUNT(ua) DESC")
    List<Object[]> getAnalyticsByConnectionType();

    /**
     * Count events by type within date range
     */
    @Query("SELECT ua.eventType, COUNT(ua) FROM UpdateAnalytics ua " +
           "WHERE ua.eventTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY ua.eventType")
    List<Object[]> countEventsByTypeInDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Find rate limited requests
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.eventType = 'RATE_LIMITED' ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findRateLimitedRequests();

    /**
     * Get rate limiting statistics
     */
    @Query("SELECT ua.clientIdentifier, COUNT(ua) as rateLimitCount " +
           "FROM UpdateAnalytics ua WHERE ua.eventType = 'RATE_LIMITED' " +
           "GROUP BY ua.clientIdentifier ORDER BY COUNT(ua) DESC")
    List<Object[]> getRateLimitingStatistics();

    /**
     * Delete old analytics records (older than specified date)
     */
    @Query("DELETE FROM UpdateAnalytics ua WHERE ua.eventTimestamp < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find analytics with pagination
     */
    Page<UpdateAnalytics> findByEventTypeOrderByEventTimestampDesc(UpdateAnalytics.EventType eventType, Pageable pageable);

    /**
     * Find recent analytics (last N hours)
     */
    @Query("SELECT ua FROM UpdateAnalytics ua WHERE ua.eventTimestamp >= :sinceTime ORDER BY ua.eventTimestamp DESC")
    List<UpdateAnalytics> findRecentAnalytics(@Param("sinceTime") LocalDateTime sinceTime);
}
