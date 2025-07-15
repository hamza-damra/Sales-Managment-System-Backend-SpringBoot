package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.VersionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for VersionHistory entity
 */
@Repository
public interface VersionHistoryRepository extends JpaRepository<VersionHistory, Long> {

    /**
     * Find version history by client identifier
     */
    List<VersionHistory> findByClientIdentifierOrderByActionTimestampDesc(String clientIdentifier);

    /**
     * Find version history by action type
     */
    List<VersionHistory> findByActionTypeOrderByActionTimestampDesc(VersionHistory.ActionType actionType);

    /**
     * Find version history by version number
     */
    List<VersionHistory> findByVersionNumberOrderByActionTimestampDesc(String versionNumber);

    /**
     * Find rollback history
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' ORDER BY vh.actionTimestamp DESC")
    List<VersionHistory> findRollbackHistory();

    /**
     * Find rollback history with pagination
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' ORDER BY vh.actionTimestamp DESC")
    Page<VersionHistory> findRollbackHistory(Pageable pageable);

    /**
     * Find successful rollbacks
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' AND vh.success = true ORDER BY vh.actionTimestamp DESC")
    List<VersionHistory> findSuccessfulRollbacks();

    /**
     * Find failed rollbacks
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' AND vh.success = false ORDER BY vh.actionTimestamp DESC")
    List<VersionHistory> findFailedRollbacks();

    /**
     * Find version history within date range
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionTimestamp BETWEEN :startDate AND :endDate ORDER BY vh.actionTimestamp DESC")
    List<VersionHistory> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find latest action for client
     */
    Optional<VersionHistory> findFirstByClientIdentifierOrderByActionTimestampDesc(String clientIdentifier);

    /**
     * Find version history for specific client and version
     */
    List<VersionHistory> findByClientIdentifierAndVersionNumberOrderByActionTimestampDesc(
            String clientIdentifier, String versionNumber);

    /**
     * Count rollbacks by version
     */
    @Query("SELECT vh.versionNumber, COUNT(vh) FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' GROUP BY vh.versionNumber")
    List<Object[]> countRollbacksByVersion();

    /**
     * Count successful updates by version
     */
    @Query("SELECT vh.versionNumber, COUNT(vh) FROM VersionHistory vh WHERE vh.actionType = 'UPDATE' AND vh.success = true GROUP BY vh.versionNumber")
    List<Object[]> countSuccessfulUpdatesByVersion();

    /**
     * Get rollback statistics
     */
    @Query("SELECT " +
           "COUNT(vh) as totalRollbacks, " +
           "AVG(vh.durationSeconds) as avgDurationSeconds, " +
           "SUM(CASE WHEN vh.success = true THEN 1 ELSE 0 END) as successfulRollbacks " +
           "FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK'")
    Object[] getRollbackStatistics();

    /**
     * Get update statistics
     */
    @Query("SELECT " +
           "COUNT(vh) as totalUpdates, " +
           "AVG(vh.durationSeconds) as avgDurationSeconds, " +
           "SUM(CASE WHEN vh.success = true THEN 1 ELSE 0 END) as successfulUpdates " +
           "FROM VersionHistory vh WHERE vh.actionType = 'UPDATE'")
    Object[] getUpdateStatistics();

    /**
     * Find recent rollbacks (last 30 days)
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.actionType = 'ROLLBACK' AND vh.actionTimestamp >= :thirtyDaysAgo ORDER BY vh.actionTimestamp DESC")
    List<VersionHistory> findRecentRollbacks(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    /**
     * Find clients with multiple rollbacks
     */
    @Query("SELECT vh.clientIdentifier, COUNT(vh) as rollbackCount FROM VersionHistory vh " +
           "WHERE vh.actionType = 'ROLLBACK' " +
           "GROUP BY vh.clientIdentifier " +
           "HAVING COUNT(vh) > :minRollbacks " +
           "ORDER BY rollbackCount DESC")
    List<Object[]> findClientsWithMultipleRollbacks(@Param("minRollbacks") int minRollbacks);

    /**
     * Find version history by IP address
     */
    List<VersionHistory> findByClientIpOrderByActionTimestampDesc(String clientIp);

    /**
     * Count actions by type within date range
     */
    @Query("SELECT vh.actionType, COUNT(vh) FROM VersionHistory vh " +
           "WHERE vh.actionTimestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY vh.actionType")
    List<Object[]> countActionsByTypeInDateRange(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find longest running operations
     */
    @Query("SELECT vh FROM VersionHistory vh WHERE vh.durationSeconds IS NOT NULL ORDER BY vh.durationSeconds DESC")
    List<VersionHistory> findLongestRunningOperations(Pageable pageable);

    /**
     * Delete old history records (older than specified date)
     */
    @Query("DELETE FROM VersionHistory vh WHERE vh.actionTimestamp < :cutoffDate")
    void deleteOldRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
}
