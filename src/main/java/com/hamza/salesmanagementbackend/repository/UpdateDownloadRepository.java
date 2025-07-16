package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.entity.UpdateDownload.DownloadStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UpdateDownload entity
 */
@Repository
public interface UpdateDownloadRepository extends JpaRepository<UpdateDownload, Long> {

    /**
     * Find downloads by version ID
     */
    List<UpdateDownload> findByApplicationVersionIdOrderByDownloadStartedAtDesc(Long versionId);

    /**
     * Find downloads by client identifier
     */
    List<UpdateDownload> findByClientIdentifierOrderByDownloadStartedAtDesc(String clientIdentifier);

    /**
     * Find downloads by status
     */
    List<UpdateDownload> findByDownloadStatusOrderByDownloadStartedAtDesc(DownloadStatus status);

    /**
     * Find downloads by version and status
     */
    List<UpdateDownload> findByApplicationVersionIdAndDownloadStatus(Long versionId, DownloadStatus status);

    /**
     * Find downloads by client and status
     */
    List<UpdateDownload> findByClientIdentifierAndDownloadStatus(String clientIdentifier, DownloadStatus status);

    /**
     * Find recent downloads within time period
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.downloadStartedAt >= :since ORDER BY ud.downloadStartedAt DESC")
    List<UpdateDownload> findRecentDownloads(@Param("since") LocalDateTime since);

    /**
     * Find downloads in date range
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.downloadStartedAt BETWEEN :startDate AND :endDate ORDER BY ud.downloadStartedAt DESC")
    List<UpdateDownload> findDownloadsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count downloads by version
     */
    long countByApplicationVersionId(Long versionId);

    /**
     * Count downloads by status
     */
    long countByDownloadStatus(DownloadStatus status);

    /**
     * Count successful downloads for a version
     */
    long countByApplicationVersionIdAndDownloadStatus(Long versionId, DownloadStatus status);

    /**
     * Find latest download by client
     */
    Optional<UpdateDownload> findFirstByClientIdentifierOrderByDownloadStartedAtDesc(String clientIdentifier);

    /**
     * Find downloads by IP address
     */
    List<UpdateDownload> findByClientIpOrderByDownloadStartedAtDesc(String clientIp);

    /**
     * Get download statistics for a version
     */
    @Query("SELECT COUNT(ud) as totalDownloads, " +
           "SUM(CASE WHEN ud.downloadStatus = 'COMPLETED' THEN 1 ELSE 0 END) as successfulDownloads, " +
           "SUM(CASE WHEN ud.downloadStatus = 'FAILED' THEN 1 ELSE 0 END) as failedDownloads, " +
           "SUM(CASE WHEN ud.downloadStatus = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgressDownloads " +
           "FROM UpdateDownload ud WHERE ud.applicationVersion.id = :versionId")
    Object[] getDownloadStatisticsForVersion(@Param("versionId") Long versionId);

    /**
     * Get overall download statistics
     */
    @Query("SELECT COUNT(ud) as totalDownloads, " +
           "SUM(CASE WHEN ud.downloadStatus = 'COMPLETED' THEN 1 ELSE 0 END) as successfulDownloads, " +
           "SUM(CASE WHEN ud.downloadStatus = 'FAILED' THEN 1 ELSE 0 END) as failedDownloads " +
           "FROM UpdateDownload ud")
    Object[] getOverallDownloadStatistics();

    /**
     * Get completed downloads with timing information for average calculation
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.downloadStatus = 'COMPLETED' " +
           "AND ud.downloadCompletedAt IS NOT NULL AND ud.downloadStartedAt IS NOT NULL")
    List<UpdateDownload> getCompletedDownloadsWithTiming();

    /**
     * Find downloads that are stuck in progress (older than timeout)
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.downloadStatus = 'IN_PROGRESS' AND ud.downloadStartedAt < :timeout")
    List<UpdateDownload> findStuckDownloads(@Param("timeout") LocalDateTime timeout);

    /**
     * Get download count by date (for charts)
     */
    @Query("SELECT CAST(ud.downloadStartedAt AS date) as downloadDate, COUNT(ud) as downloadCount " +
           "FROM UpdateDownload ud WHERE ud.downloadStartedAt >= :since " +
           "GROUP BY CAST(ud.downloadStartedAt AS date) ORDER BY downloadDate")
    List<Object[]> getDownloadCountByDate(@Param("since") LocalDateTime since);

    /**
     * Get top downloading clients
     */
    @Query("SELECT ud.clientIdentifier, COUNT(ud) as downloadCount " +
           "FROM UpdateDownload ud WHERE ud.clientIdentifier IS NOT NULL " +
           "GROUP BY ud.clientIdentifier ORDER BY downloadCount DESC")
    List<Object[]> getTopDownloadingClients(Pageable pageable);

    /**
     * Find downloads by user agent pattern
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.userAgent LIKE %:pattern% ORDER BY ud.downloadStartedAt DESC")
    List<UpdateDownload> findByUserAgentContaining(@Param("pattern") String pattern);

    /**
     * Check if client has already downloaded a specific version
     */
    boolean existsByClientIdentifierAndApplicationVersionIdAndDownloadStatus(
        String clientIdentifier, Long versionId, DownloadStatus status);

    /**
     * Find downloads for cleanup (old completed/failed downloads)
     */
    @Query("SELECT ud FROM UpdateDownload ud WHERE ud.downloadStatus IN ('COMPLETED', 'FAILED') " +
           "AND ud.downloadStartedAt < :cutoffDate")
    List<UpdateDownload> findDownloadsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
}
