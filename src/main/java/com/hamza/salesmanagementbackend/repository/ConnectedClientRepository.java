package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.ConnectedClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ConnectedClient entity
 */
@Repository
public interface ConnectedClientRepository extends JpaRepository<ConnectedClient, Long> {

    /**
     * Find client by session ID
     */
    Optional<ConnectedClient> findBySessionId(String sessionId);

    /**
     * Find all active clients
     */
    List<ConnectedClient> findByIsActiveOrderByConnectedAtDesc(Boolean isActive);

    /**
     * Find clients by version
     */
    List<ConnectedClient> findByClientVersionOrderByConnectedAtDesc(String clientVersion);

    /**
     * Find active clients by version
     */
    List<ConnectedClient> findByIsActiveAndClientVersionOrderByConnectedAtDesc(Boolean isActive, String clientVersion);

    /**
     * Find clients by IP address
     */
    List<ConnectedClient> findByClientIpOrderByConnectedAtDesc(String clientIp);

    /**
     * Find clients connected after a specific time
     */
    @Query("SELECT cc FROM ConnectedClient cc WHERE cc.connectedAt >= :since ORDER BY cc.connectedAt DESC")
    List<ConnectedClient> findClientsConnectedSince(@Param("since") LocalDateTime since);

    /**
     * Find stale connections (last ping older than timeout)
     */
    @Query("SELECT cc FROM ConnectedClient cc WHERE cc.isActive = true AND cc.lastPingAt < :timeout")
    List<ConnectedClient> findStaleConnections(@Param("timeout") LocalDateTime timeout);

    /**
     * Count active clients
     */
    long countByIsActive(Boolean isActive);

    /**
     * Count clients by version
     */
    long countByClientVersion(String clientVersion);

    /**
     * Count active clients by version
     */
    long countByIsActiveAndClientVersion(Boolean isActive, String clientVersion);

    /**
     * Check if session exists and is active
     */
    boolean existsBySessionIdAndIsActive(String sessionId, Boolean isActive);

    /**
     * Update last ping time for a session
     */
    @Modifying
    @Transactional
    @Query("UPDATE ConnectedClient cc SET cc.lastPingAt = :pingTime WHERE cc.sessionId = :sessionId")
    int updateLastPingTime(@Param("sessionId") String sessionId, @Param("pingTime") LocalDateTime pingTime);

    /**
     * Disconnect client by session ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE ConnectedClient cc SET cc.isActive = false WHERE cc.sessionId = :sessionId")
    int disconnectClient(@Param("sessionId") String sessionId);

    /**
     * Disconnect all stale connections
     */
    @Modifying
    @Transactional
    @Query("UPDATE ConnectedClient cc SET cc.isActive = false WHERE cc.isActive = true AND cc.lastPingAt < :timeout")
    int disconnectStaleClients(@Param("timeout") LocalDateTime timeout);

    /**
     * Get client version distribution
     */
    @Query("SELECT cc.clientVersion, COUNT(cc) as clientCount " +
           "FROM ConnectedClient cc WHERE cc.isActive = true AND cc.clientVersion IS NOT NULL " +
           "GROUP BY cc.clientVersion ORDER BY clientCount DESC")
    List<Object[]> getClientVersionDistribution();

    /**
     * Get connection statistics
     */
    @Query("SELECT COUNT(cc) as totalClients, " +
           "SUM(CASE WHEN cc.isActive = true THEN 1 ELSE 0 END) as activeClients " +
           "FROM ConnectedClient cc")
    Object[] getConnectionStatistics();

    /**
     * Get all connected clients for average connection time calculation
     */
    @Query("SELECT cc FROM ConnectedClient cc WHERE cc.connectedAt IS NOT NULL")
    List<ConnectedClient> getAllClientsWithConnectionTime();

    /**
     * Find clients that need updates (version comparison)
     */
    @Query("SELECT cc FROM ConnectedClient cc WHERE cc.isActive = true AND cc.clientVersion IS NOT NULL " +
           "AND cc.clientVersion != :latestVersion ORDER BY cc.connectedAt DESC")
    List<ConnectedClient> findClientsNeedingUpdate(@Param("latestVersion") String latestVersion);

    /**
     * Get hourly connection count for the last 24 hours
     */
    @Query("SELECT HOUR(cc.connectedAt) as hour, COUNT(cc) as connectionCount " +
           "FROM ConnectedClient cc WHERE cc.connectedAt >= :since " +
           "GROUP BY HOUR(cc.connectedAt) ORDER BY hour")
    List<Object[]> getHourlyConnectionCount(@Param("since") LocalDateTime since);

    /**
     * Find clients for cleanup (old inactive connections)
     */
    @Query("SELECT cc FROM ConnectedClient cc WHERE cc.isActive = false AND cc.lastPingAt < :cutoffDate")
    List<ConnectedClient> findClientsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete old inactive clients
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ConnectedClient cc WHERE cc.isActive = false AND cc.lastPingAt < :cutoffDate")
    int deleteOldInactiveClients(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find duplicate sessions (same IP, different session IDs)
     */
    @Query("SELECT cc.clientIp, COUNT(cc) as sessionCount " +
           "FROM ConnectedClient cc WHERE cc.isActive = true AND cc.clientIp IS NOT NULL " +
           "GROUP BY cc.clientIp HAVING COUNT(cc) > 1")
    List<Object[]> findDuplicateSessions();

    /**
     * Get most active IPs
     */
    @Query("SELECT cc.clientIp, COUNT(cc) as connectionCount " +
           "FROM ConnectedClient cc WHERE cc.clientIp IS NOT NULL " +
           "GROUP BY cc.clientIp ORDER BY connectionCount DESC")
    List<Object[]> getMostActiveIPs();
}
