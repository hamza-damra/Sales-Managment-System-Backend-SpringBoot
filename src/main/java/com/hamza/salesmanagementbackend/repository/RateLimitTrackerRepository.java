package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RateLimitTracker entity
 */
@Repository
public interface RateLimitTrackerRepository extends JpaRepository<RateLimitTracker, Long> {

    /**
     * Find rate limit tracker by client identifier and endpoint type
     */
    Optional<RateLimitTracker> findByClientIdentifierAndEndpointType(
            String clientIdentifier, RateLimitTracker.EndpointType endpointType);

    /**
     * Find rate limit tracker by client IP and endpoint type
     */
    Optional<RateLimitTracker> findByClientIpAndEndpointType(
            String clientIp, RateLimitTracker.EndpointType endpointType);

    /**
     * Find all trackers for a client identifier
     */
    List<RateLimitTracker> findByClientIdentifier(String clientIdentifier);

    /**
     * Find all trackers for a client IP
     */
    List<RateLimitTracker> findByClientIp(String clientIp);

    /**
     * Find currently blocked clients
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil > :currentTime")
    List<RateLimitTracker> findCurrentlyBlockedClients(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find clients blocked for specific endpoint type
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.endpointType = :endpointType AND rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil > :currentTime")
    List<RateLimitTracker> findBlockedClientsForEndpoint(@Param("endpointType") RateLimitTracker.EndpointType endpointType,
                                                        @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find trackers with expired windows
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.windowStart < :expiredBefore")
    List<RateLimitTracker> findExpiredWindows(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Find clients with high violation counts
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.violationCount >= :minViolations ORDER BY rlt.violationCount DESC")
    List<RateLimitTracker> findClientsWithHighViolations(@Param("minViolations") int minViolations);

    /**
     * Get rate limiting statistics by endpoint type
     */
    @Query("SELECT rlt.endpointType, " +
           "COUNT(rlt) as totalTrackers, " +
           "SUM(rlt.totalAllowedRequests) as totalAllowed, " +
           "SUM(rlt.totalBlockedRequests) as totalBlocked, " +
           "AVG(rlt.violationCount) as avgViolations " +
           "FROM RateLimitTracker rlt " +
           "GROUP BY rlt.endpointType")
    List<Object[]> getRateLimitingStatisticsByEndpoint();

    /**
     * Get overall rate limiting statistics
     */
    @Query("SELECT " +
           "COUNT(rlt) as totalTrackers, " +
           "SUM(rlt.totalAllowedRequests) as totalAllowed, " +
           "SUM(rlt.totalBlockedRequests) as totalBlocked, " +
           "COUNT(CASE WHEN rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil > :currentTime THEN 1 END) as currentlyBlocked " +
           "FROM RateLimitTracker rlt")
    Object[] getOverallRateLimitingStatistics(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find most active clients by request count
     */
    @Query("SELECT rlt.clientIdentifier, SUM(rlt.totalAllowedRequests) as totalRequests " +
           "FROM RateLimitTracker rlt " +
           "GROUP BY rlt.clientIdentifier " +
           "ORDER BY totalRequests DESC")
    List<Object[]> findMostActiveClients();

    /**
     * Find clients with highest violation rates
     */
    @Query("SELECT rlt.clientIdentifier, " +
           "SUM(rlt.totalAllowedRequests) as totalAllowed, " +
           "SUM(rlt.totalBlockedRequests) as totalBlocked, " +
           "(SUM(rlt.totalBlockedRequests) * 100.0 / (SUM(rlt.totalAllowedRequests) + SUM(rlt.totalBlockedRequests))) as violationRate " +
           "FROM RateLimitTracker rlt " +
           "WHERE (rlt.totalAllowedRequests + rlt.totalBlockedRequests) > 0 " +
           "GROUP BY rlt.clientIdentifier " +
           "ORDER BY violationRate DESC")
    List<Object[]> findClientsWithHighestViolationRates();

    /**
     * Clean up expired blocks
     */
    @Modifying
    @Query("UPDATE RateLimitTracker rlt SET rlt.blockedUntil = NULL WHERE rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil <= :currentTime")
    int cleanupExpiredBlocks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Reset expired windows
     */
    @Modifying
    @Query("UPDATE RateLimitTracker rlt SET rlt.requestCount = 0, rlt.windowStart = :currentTime WHERE rlt.windowStart < :expiredBefore")
    int resetExpiredWindows(@Param("currentTime") LocalDateTime currentTime, 
                           @Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * Find trackers that haven't been used recently
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.lastRequestTime < :cutoffTime")
    List<RateLimitTracker> findInactiveTrackers(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete inactive trackers
     */
    @Modifying
    @Query("DELETE FROM RateLimitTracker rlt WHERE rlt.lastRequestTime < :cutoffTime")
    int deleteInactiveTrackers(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count active trackers by endpoint type
     */
    @Query("SELECT rlt.endpointType, COUNT(rlt) FROM RateLimitTracker rlt " +
           "WHERE rlt.lastRequestTime >= :activeThreshold " +
           "GROUP BY rlt.endpointType")
    List<Object[]> countActiveTrackersByEndpoint(@Param("activeThreshold") LocalDateTime activeThreshold);

    /**
     * Find trackers by endpoint type
     */
    List<RateLimitTracker> findByEndpointType(RateLimitTracker.EndpointType endpointType);

    /**
     * Find trackers created within date range
     */
    @Query("SELECT rlt FROM RateLimitTracker rlt WHERE rlt.createdAt BETWEEN :startDate AND :endDate ORDER BY rlt.createdAt DESC")
    List<RateLimitTracker> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Get daily violation counts
     */
    @Query("SELECT DATE(rlt.firstViolationTime) as violationDate, COUNT(rlt) as violationCount " +
           "FROM RateLimitTracker rlt WHERE rlt.firstViolationTime IS NOT NULL " +
           "AND rlt.firstViolationTime >= :startDate " +
           "GROUP BY DATE(rlt.firstViolationTime) ORDER BY violationDate DESC")
    List<Object[]> getDailyViolationCounts(@Param("startDate") LocalDateTime startDate);

    /**
     * Check if client is currently blocked for any endpoint
     */
    @Query("SELECT COUNT(rlt) > 0 FROM RateLimitTracker rlt WHERE rlt.clientIdentifier = :clientIdentifier " +
           "AND rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil > :currentTime")
    boolean isClientBlocked(@Param("clientIdentifier") String clientIdentifier, 
                           @Param("currentTime") LocalDateTime currentTime);

    /**
     * Get client's current block status for all endpoints
     */
    @Query("SELECT rlt.endpointType, rlt.blockedUntil FROM RateLimitTracker rlt " +
           "WHERE rlt.clientIdentifier = :clientIdentifier " +
           "AND rlt.blockedUntil IS NOT NULL AND rlt.blockedUntil > :currentTime")
    List<Object[]> getClientBlockStatus(@Param("clientIdentifier") String clientIdentifier,
                                       @Param("currentTime") LocalDateTime currentTime);
}
