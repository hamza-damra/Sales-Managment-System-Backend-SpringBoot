package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.UpdateStatisticsDTO;
import com.hamza.salesmanagementbackend.entity.ConnectedClient;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.ConnectedClientRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating update system statistics and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UpdateStatisticsService {

    private final ApplicationVersionRepository versionRepository;
    private final UpdateDownloadRepository downloadRepository;
    private final ConnectedClientRepository clientRepository;

    /**
     * Get comprehensive update system statistics
     */
    public UpdateStatisticsDTO getUpdateStatistics() {
        log.debug("Generating comprehensive update statistics");

        // Get version statistics
        Object[] versionStats = versionRepository.getVersionStatistics();
        Long totalVersions = ((Number) versionStats[0]).longValue();
        Long activeVersions = ((Number) versionStats[1]).longValue();
        Long mandatoryVersions = ((Number) versionStats[2]).longValue();
        Double averageFileSize = versionStats[3] != null ? ((Number) versionStats[3]).doubleValue() : 0.0;

        // Get download statistics
        Object[] downloadStats = downloadRepository.getOverallDownloadStatistics();
        Long totalDownloads = ((Number) downloadStats[0]).longValue();
        Long successfulDownloads = ((Number) downloadStats[1]).longValue();
        Long failedDownloads = ((Number) downloadStats[2]).longValue();

        // Calculate average download time manually
        List<UpdateDownload> completedDownloads = downloadRepository.getCompletedDownloadsWithTiming();
        Double averageDownloadTime = 0.0;
        if (!completedDownloads.isEmpty()) {
            long totalSeconds = completedDownloads.stream()
                .mapToLong(download -> {
                    if (download.getDownloadStartedAt() != null && download.getDownloadCompletedAt() != null) {
                        return java.time.Duration.between(download.getDownloadStartedAt(), download.getDownloadCompletedAt()).getSeconds();
                    }
                    return 0L;
                })
                .sum();
            averageDownloadTime = (double) totalSeconds / completedDownloads.size();
        }

        // Get client statistics
        Object[] clientStats = clientRepository.getConnectionStatistics();
        Long totalClients = ((Number) clientStats[0]).longValue();
        Long activeClients = ((Number) clientStats[1]).longValue();

        // Calculate average connection time manually
        List<ConnectedClient> allClients = clientRepository.getAllClientsWithConnectionTime();
        Double averageConnectionTime = 0.0;
        if (!allClients.isEmpty()) {
            long totalMinutes = allClients.stream()
                .mapToLong(client -> {
                    if (client.getConnectedAt() != null) {
                        return java.time.Duration.between(client.getConnectedAt(), LocalDateTime.now()).toMinutes();
                    }
                    return 0L;
                })
                .sum();
            averageConnectionTime = (double) totalMinutes / allClients.size();
        }

        // Get client version distribution
        Map<String, Long> versionDistribution = getClientVersionDistribution();

        // Get daily download counts for the last 30 days
        List<UpdateStatisticsDTO.DailyDownloadCount> dailyDownloads = getDailyDownloadCounts();

        // Get top versions by downloads
        List<UpdateStatisticsDTO.VersionDownloadStat> topVersions = getTopVersionsByDownloads();

        // Get recent downloads
        List<UpdateStatisticsDTO.RecentDownloadDTO> recentDownloads = getRecentDownloads();

        // Calculate in-progress downloads
        Long inProgressDownloads = totalDownloads - successfulDownloads - failedDownloads;

        return UpdateStatisticsDTO.builder()
            .totalVersions(totalVersions)
            .activeVersions(activeVersions)
            .mandatoryVersions(mandatoryVersions)
            .averageFileSize(averageFileSize)
            .totalDownloads(totalDownloads)
            .successfulDownloads(successfulDownloads)
            .failedDownloads(failedDownloads)
            .inProgressDownloads(inProgressDownloads)
            .averageDownloadTimeSeconds(averageDownloadTime)
            .totalConnectedClients(totalClients)
            .activeClients(activeClients)
            .averageConnectionTimeMinutes(averageConnectionTime)
            .clientVersionDistribution(versionDistribution)
            .dailyDownloadCounts(dailyDownloads)
            .topVersionsByDownloads(topVersions)
            .recentDownloads(recentDownloads)
            .build();
    }

    /**
     * Get download statistics for a specific version
     */
    public UpdateStatisticsDTO getVersionStatistics(Long versionId) {
        log.debug("Generating statistics for version ID: {}", versionId);

        Object[] stats = downloadRepository.getDownloadStatisticsForVersion(versionId);
        Long totalDownloads = ((Number) stats[0]).longValue();
        Long successfulDownloads = ((Number) stats[1]).longValue();
        Long failedDownloads = ((Number) stats[2]).longValue();
        Long inProgressDownloads = ((Number) stats[3]).longValue();

        return UpdateStatisticsDTO.builder()
            .totalDownloads(totalDownloads)
            .successfulDownloads(successfulDownloads)
            .failedDownloads(failedDownloads)
            .inProgressDownloads(inProgressDownloads)
            .build();
    }

    /**
     * Get client version distribution
     */
    private Map<String, Long> getClientVersionDistribution() {
        List<Object[]> distribution = clientRepository.getClientVersionDistribution();
        Map<String, Long> result = new HashMap<>();
        
        for (Object[] row : distribution) {
            String version = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(version, count);
        }
        
        return result;
    }

    /**
     * Get daily download counts for the last 30 days
     */
    private List<UpdateStatisticsDTO.DailyDownloadCount> getDailyDownloadCounts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyCounts = downloadRepository.getDownloadCountByDate(thirtyDaysAgo);

        return dailyCounts.stream()
            .map(row -> UpdateStatisticsDTO.DailyDownloadCount.builder()
                .date(row[0].toString())
                .downloadCount(((Number) row[1]).longValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get top versions by download count
     */
    private List<UpdateStatisticsDTO.VersionDownloadStat> getTopVersionsByDownloads() {
        // This would require a more complex query, for now we'll return a simplified version
        // In a real implementation, you'd want to join with ApplicationVersion and group by version

        // For now, return empty list - this would need proper implementation
        return List.of();
    }

    /**
     * Get recent downloads (last 10)
     */
    private List<UpdateStatisticsDTO.RecentDownloadDTO> getRecentDownloads() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<UpdateDownload> recentDownloads = downloadRepository.findRecentDownloads(oneDayAgo);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return recentDownloads.stream()
            .limit(10)
            .map(download -> UpdateStatisticsDTO.RecentDownloadDTO.builder()
                .versionNumber(download.getApplicationVersion().getVersionNumber())
                .clientIdentifier(download.getClientIdentifier())
                .downloadStatus(download.getDownloadStatus().name())
                .downloadStartedAt(download.getDownloadStartedAt().format(formatter))
                .clientIp(download.getClientIp())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get download success rate for a specific version
     */
    public Double getVersionSuccessRate(Long versionId) {
        Object[] stats = downloadRepository.getDownloadStatisticsForVersion(versionId);
        Long totalDownloads = ((Number) stats[0]).longValue();
        Long successfulDownloads = ((Number) stats[1]).longValue();
        
        if (totalDownloads == 0) {
            return 0.0;
        }
        
        return (successfulDownloads.doubleValue() / totalDownloads.doubleValue()) * 100.0;
    }

    /**
     * Get overall system health metrics
     */
    public Map<String, Object> getSystemHealthMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Active versions count
        Long activeVersions = versionRepository.countByIsActive(true);
        metrics.put("activeVersions", activeVersions);
        
        // Active clients count
        Long activeClients = clientRepository.countByIsActive(true);
        metrics.put("activeClients", activeClients);
        
        // Recent download success rate (last 24 hours)
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<UpdateDownload> recentDownloads = downloadRepository.findRecentDownloads(oneDayAgo);
        
        long recentTotal = recentDownloads.size();
        long recentSuccessful = recentDownloads.stream()
            .mapToLong(d -> d.getDownloadStatus() == UpdateDownload.DownloadStatus.COMPLETED ? 1 : 0)
            .sum();
        
        double recentSuccessRate = recentTotal > 0 ? (recentSuccessful * 100.0 / recentTotal) : 0.0;
        metrics.put("recentSuccessRate", recentSuccessRate);
        
        // Stale connections (older than 5 minutes)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        List<ConnectedClient> staleConnections = clientRepository.findStaleConnections(fiveMinutesAgo);
        metrics.put("staleConnections", staleConnections.size());
        
        return metrics;
    }
}
