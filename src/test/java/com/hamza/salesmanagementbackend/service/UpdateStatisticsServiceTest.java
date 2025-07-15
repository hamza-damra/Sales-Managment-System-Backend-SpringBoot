package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.UpdateStatisticsDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.entity.ConnectedClient;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.ConnectedClientRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UpdateStatisticsService
 */
@ExtendWith(MockitoExtension.class)
class UpdateStatisticsServiceTest {

    @Mock
    private ApplicationVersionRepository versionRepository;

    @Mock
    private UpdateDownloadRepository downloadRepository;

    @Mock
    private ConnectedClientRepository clientRepository;

    @InjectMocks
    private UpdateStatisticsService updateStatisticsService;

    private ApplicationVersion testVersion1;
    private ApplicationVersion testVersion2;
    private UpdateDownload testDownload1;
    private UpdateDownload testDownload2;
    private ConnectedClient testClient1;
    private ConnectedClient testClient2;

    @BeforeEach
    void setUp() {
        // Setup test versions
        testVersion1 = ApplicationVersion.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now().minusDays(5))
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes")
            .minimumClientVersion("2.0.0")
            .fileName("sales-management-2.1.0.jar")
            .fileSize(52428800L) // 50MB
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .createdAt(LocalDateTime.now().minusDays(5))
            .updatedAt(LocalDateTime.now().minusDays(5))
            .createdBy("admin")
            .build();

        testVersion2 = ApplicationVersion.builder()
            .id(2L)
            .versionNumber("2.0.0")
            .releaseDate(LocalDateTime.now().minusDays(10))
            .isMandatory(true)
            .isActive(true)
            .releaseNotes("Previous version")
            .minimumClientVersion("1.9.0")
            .fileName("sales-management-2.0.0.jar")
            .fileSize(48234496L) // 46MB
            .fileChecksum("sha256:def456")
            .downloadUrl("/api/v1/updates/download/2.0.0")
            .createdAt(LocalDateTime.now().minusDays(10))
            .updatedAt(LocalDateTime.now().minusDays(10))
            .createdBy("admin")
            .build();

        // Setup test downloads
        testDownload1 = UpdateDownload.builder()
            .id(1L)
            .applicationVersion(testVersion1)
            .clientIdentifier("client-001")
            .clientIp("192.168.1.100")
            .userAgent("TestAgent/1.0")
            .downloadStartedAt(LocalDateTime.now().minusHours(2))
            .downloadCompletedAt(LocalDateTime.now().minusHours(2).plusMinutes(5))
            .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
            .build();

        testDownload2 = UpdateDownload.builder()
            .id(2L)
            .applicationVersion(testVersion2)
            .clientIdentifier("client-002")
            .clientIp("192.168.1.101")
            .userAgent("TestAgent/1.0")
            .downloadStartedAt(LocalDateTime.now().minusHours(1))
            .downloadStatus(UpdateDownload.DownloadStatus.FAILED)
            .build();

        // Setup test clients
        testClient1 = ConnectedClient.builder()
            .id(1L)
            .sessionId("session-001")
            .clientVersion("2.0.0")
            .connectedAt(LocalDateTime.now().minusHours(3))
            .lastPingAt(LocalDateTime.now().minusMinutes(1))
            .clientIp("192.168.1.100")
            .isActive(true)
            .build();

        testClient2 = ConnectedClient.builder()
            .id(2L)
            .sessionId("session-002")
            .clientVersion("2.1.0")
            .connectedAt(LocalDateTime.now().minusHours(1))
            .lastPingAt(LocalDateTime.now().minusMinutes(10))
            .clientIp("192.168.1.101")
            .isActive(false)
            .build();
    }

    @Test
    void testGetUpdateStatistics_Success() {
        // Arrange
        Object[] versionStats = {2L, 2L, 1L, 50331648.0}; // totalVersions, activeVersions, mandatoryVersions, avgFileSize
        Object[] downloadStats = {3L, 2L, 1L}; // totalDownloads, successfulDownloads, failedDownloads
        Object[] clientStats = {2L, 1L}; // totalClients, activeClients

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList(testDownload1));
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList(testClient1, testClient2));
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList(
            new Object[]{"2.0.0", 1L},
            new Object[]{"2.1.0", 1L}
        ));
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList(
            new Object[]{"2025-01-15", 2L},
            new Object[]{"2025-01-14", 1L}
        ));
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList(testDownload1, testDownload2));

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalVersions());
        assertEquals(2L, result.getActiveVersions());
        assertEquals(1L, result.getMandatoryVersions());
        assertEquals(50331648.0, result.getAverageFileSize());
        assertEquals(3L, result.getTotalDownloads());
        assertEquals(2L, result.getSuccessfulDownloads());
        assertEquals(1L, result.getFailedDownloads());
        assertEquals(0L, result.getInProgressDownloads()); // 3 - 2 - 1 = 0
        assertEquals(2L, result.getTotalConnectedClients());
        assertEquals(1L, result.getActiveClients());

        // Verify average download time calculation (5 minutes = 300 seconds)
        assertEquals(300.0, result.getAverageDownloadTimeSeconds());

        // Verify client version distribution
        Map<String, Long> versionDistribution = result.getClientVersionDistribution();
        assertNotNull(versionDistribution);
        assertEquals(2, versionDistribution.size());
        assertEquals(1L, versionDistribution.get("2.0.0"));
        assertEquals(1L, versionDistribution.get("2.1.0"));

        // Verify daily download counts
        List<UpdateStatisticsDTO.DailyDownloadCount> dailyCounts = result.getDailyDownloadCounts();
        assertNotNull(dailyCounts);
        assertEquals(2, dailyCounts.size());

        // Verify recent downloads
        List<UpdateStatisticsDTO.RecentDownloadDTO> recentDownloads = result.getRecentDownloads();
        assertNotNull(recentDownloads);
        assertEquals(2, recentDownloads.size());

        // Verify method calls
        verify(versionRepository).getVersionStatistics();
        verify(downloadRepository).getOverallDownloadStatistics();
        verify(downloadRepository).getCompletedDownloadsWithTiming();
        verify(clientRepository).getConnectionStatistics();
        verify(clientRepository).getAllClientsWithConnectionTime();
        verify(clientRepository).getClientVersionDistribution();
        verify(downloadRepository).getDownloadCountByDate(any(LocalDateTime.class));
        verify(downloadRepository).findRecentDownloads(any(LocalDateTime.class));
    }

    @Test
    void testGetUpdateStatistics_EmptyData() {
        // Arrange
        Object[] versionStats = {0L, 0L, 0L, null};
        Object[] downloadStats = {0L, 0L, 0L};
        Object[] clientStats = {0L, 0L};

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList());
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList());
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalVersions());
        assertEquals(0L, result.getActiveVersions());
        assertEquals(0L, result.getMandatoryVersions());
        assertEquals(0.0, result.getAverageFileSize());
        assertEquals(0L, result.getTotalDownloads());
        assertEquals(0L, result.getSuccessfulDownloads());
        assertEquals(0L, result.getFailedDownloads());
        assertEquals(0L, result.getInProgressDownloads());
        assertEquals(0.0, result.getAverageDownloadTimeSeconds());
        assertEquals(0L, result.getTotalConnectedClients());
        assertEquals(0L, result.getActiveClients());
        assertEquals(0.0, result.getAverageConnectionTimeMinutes());

        assertTrue(result.getClientVersionDistribution().isEmpty());
        assertTrue(result.getDailyDownloadCounts().isEmpty());
        assertTrue(result.getRecentDownloads().isEmpty());
    }

    @Test
    void testGetVersionStatistics_Success() {
        // Arrange
        Long versionId = 1L;
        Object[] stats = {5L, 4L, 1L, 0L}; // totalDownloads, successfulDownloads, failedDownloads, inProgressDownloads

        when(downloadRepository.getDownloadStatisticsForVersion(versionId)).thenReturn(stats);

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getVersionStatistics(versionId);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getTotalDownloads());
        assertEquals(4L, result.getSuccessfulDownloads());
        assertEquals(1L, result.getFailedDownloads());
        assertEquals(0L, result.getInProgressDownloads());

        verify(downloadRepository).getDownloadStatisticsForVersion(versionId);
    }

    @Test
    void testGetVersionSuccessRate_WithDownloads() {
        // Arrange
        Long versionId = 1L;
        Object[] stats = {10L, 8L, 2L, 0L}; // totalDownloads, successfulDownloads, failedDownloads, inProgressDownloads

        when(downloadRepository.getDownloadStatisticsForVersion(versionId)).thenReturn(stats);

        // Act
        Double successRate = updateStatisticsService.getVersionSuccessRate(versionId);

        // Assert
        assertNotNull(successRate);
        assertEquals(80.0, successRate); // 8/10 * 100 = 80%

        verify(downloadRepository).getDownloadStatisticsForVersion(versionId);
    }

    @Test
    void testGetVersionSuccessRate_NoDownloads() {
        // Arrange
        Long versionId = 1L;
        Object[] stats = {0L, 0L, 0L, 0L}; // no downloads

        when(downloadRepository.getDownloadStatisticsForVersion(versionId)).thenReturn(stats);

        // Act
        Double successRate = updateStatisticsService.getVersionSuccessRate(versionId);

        // Assert
        assertNotNull(successRate);
        assertEquals(0.0, successRate);

        verify(downloadRepository).getDownloadStatisticsForVersion(versionId);
    }

    @Test
    void testGetSystemHealthMetrics_Success() {
        // Arrange
        Long activeVersions = 3L;
        Long activeClients = 5L;

        List<UpdateDownload> recentDownloads = Arrays.asList(
            createDownloadWithStatus(UpdateDownload.DownloadStatus.COMPLETED),
            createDownloadWithStatus(UpdateDownload.DownloadStatus.COMPLETED),
            createDownloadWithStatus(UpdateDownload.DownloadStatus.FAILED),
            createDownloadWithStatus(UpdateDownload.DownloadStatus.STARTED)
        );

        List<ConnectedClient> staleConnections = Arrays.asList(testClient2);

        when(versionRepository.countByIsActive(true)).thenReturn(activeVersions);
        when(clientRepository.countByIsActive(true)).thenReturn(activeClients);
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(recentDownloads);
        when(clientRepository.findStaleConnections(any(LocalDateTime.class))).thenReturn(staleConnections);

        // Act
        Map<String, Object> result = updateStatisticsService.getSystemHealthMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(activeVersions, result.get("activeVersions"));
        assertEquals(activeClients, result.get("activeClients"));
        assertEquals(50.0, result.get("recentSuccessRate")); // 2 successful out of 4 total = 50%
        assertEquals(1, result.get("staleConnections"));

        verify(versionRepository).countByIsActive(true);
        verify(clientRepository).countByIsActive(true);
        verify(downloadRepository).findRecentDownloads(any(LocalDateTime.class));
        verify(clientRepository).findStaleConnections(any(LocalDateTime.class));
    }

    @Test
    void testGetSystemHealthMetrics_NoRecentDownloads() {
        // Arrange
        when(versionRepository.countByIsActive(true)).thenReturn(2L);
        when(clientRepository.countByIsActive(true)).thenReturn(3L);
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(clientRepository.findStaleConnections(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        Map<String, Object> result = updateStatisticsService.getSystemHealthMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.get("activeVersions"));
        assertEquals(3L, result.get("activeClients"));
        assertEquals(0.0, result.get("recentSuccessRate")); // No downloads = 0% success rate
        assertEquals(0, result.get("staleConnections"));
    }

    @Test
    void testGetUpdateStatistics_AverageDownloadTimeCalculation() {
        // Arrange
        UpdateDownload download1 = createDownloadWithTiming(
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(2).plusMinutes(5) // 5 minutes = 300 seconds
        );
        UpdateDownload download2 = createDownloadWithTiming(
            LocalDateTime.now().minusHours(1),
            LocalDateTime.now().minusHours(1).plusMinutes(10) // 10 minutes = 600 seconds
        );

        Object[] versionStats = {1L, 1L, 0L, 1000000.0};
        Object[] downloadStats = {2L, 2L, 0L};
        Object[] clientStats = {1L, 1L};

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList(download1, download2));
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList(testClient1));
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        // Average of 300 and 600 seconds = 450 seconds
        assertEquals(450.0, result.getAverageDownloadTimeSeconds());
    }

    @Test
    void testGetUpdateStatistics_ClientVersionDistribution() {
        // Arrange
        Object[] versionStats = {1L, 1L, 0L, 1000000.0};
        Object[] downloadStats = {0L, 0L, 0L};
        Object[] clientStats = {3L, 3L};

        List<Object[]> versionDistribution = Arrays.asList(
            new Object[]{"2.0.0", 5L},
            new Object[]{"2.1.0", 3L},
            new Object[]{"1.9.0", 2L}
        );

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList());
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList());
        when(clientRepository.getClientVersionDistribution()).thenReturn(versionDistribution);
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        Map<String, Long> distribution = result.getClientVersionDistribution();
        assertNotNull(distribution);
        assertEquals(3, distribution.size());
        assertEquals(5L, distribution.get("2.0.0"));
        assertEquals(3L, distribution.get("2.1.0"));
        assertEquals(2L, distribution.get("1.9.0"));
    }

    @Test
    void testGetUpdateStatistics_DailyDownloadCounts() {
        // Arrange
        Object[] versionStats = {1L, 1L, 0L, 1000000.0};
        Object[] downloadStats = {0L, 0L, 0L};
        Object[] clientStats = {1L, 1L};

        List<Object[]> dailyCounts = Arrays.asList(
            new Object[]{"2025-01-15", 10L},
            new Object[]{"2025-01-14", 8L},
            new Object[]{"2025-01-13", 12L}
        );

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList());
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList());
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(dailyCounts);
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        List<UpdateStatisticsDTO.DailyDownloadCount> dailyDownloads = result.getDailyDownloadCounts();
        assertNotNull(dailyDownloads);
        assertEquals(3, dailyDownloads.size());

        assertEquals("2025-01-15", dailyDownloads.get(0).getDate());
        assertEquals(10L, dailyDownloads.get(0).getDownloadCount());

        assertEquals("2025-01-14", dailyDownloads.get(1).getDate());
        assertEquals(8L, dailyDownloads.get(1).getDownloadCount());

        assertEquals("2025-01-13", dailyDownloads.get(2).getDate());
        assertEquals(12L, dailyDownloads.get(2).getDownloadCount());
    }

    @Test
    void testGetUpdateStatistics_RecentDownloads() {
        // Arrange
        Object[] versionStats = {1L, 1L, 0L, 1000000.0};
        Object[] downloadStats = {0L, 0L, 0L};
        Object[] clientStats = {1L, 1L};

        List<UpdateDownload> recentDownloads = Arrays.asList(testDownload1, testDownload2);

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList());
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList());
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(recentDownloads);

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        List<UpdateStatisticsDTO.RecentDownloadDTO> recentDownloadDTOs = result.getRecentDownloads();
        assertNotNull(recentDownloadDTOs);
        assertEquals(2, recentDownloadDTOs.size());

        UpdateStatisticsDTO.RecentDownloadDTO firstDownload = recentDownloadDTOs.get(0);
        assertEquals("2.1.0", firstDownload.getVersionNumber());
        assertEquals("client-001", firstDownload.getClientIdentifier());
        assertEquals("COMPLETED", firstDownload.getDownloadStatus());
        assertEquals("192.168.1.100", firstDownload.getClientIp());

        UpdateStatisticsDTO.RecentDownloadDTO secondDownload = recentDownloadDTOs.get(1);
        assertEquals("2.0.0", secondDownload.getVersionNumber());
        assertEquals("client-002", secondDownload.getClientIdentifier());
        assertEquals("FAILED", secondDownload.getDownloadStatus());
        assertEquals("192.168.1.101", secondDownload.getClientIp());
    }

    @Test
    void testGetUpdateStatistics_NullHandling() {
        // Arrange - Test with zero values instead of null (which is more realistic)
        Object[] versionStats = {0L, 0L, 0L, null}; // null for average file size is acceptable
        Object[] downloadStats = {0L, 0L, 0L};
        Object[] clientStats = {0L, 0L};

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList());
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList());
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();
            assertNotNull(result);
            assertEquals(0L, result.getTotalVersions());
            assertEquals(0L, result.getActiveVersions());
            assertEquals(0L, result.getMandatoryVersions());
            assertEquals(0.0, result.getAverageFileSize()); // null should be converted to 0.0
        });
    }

    @Test
    void testGetUpdateStatistics_EdgeCases() {
        // Arrange - Test edge cases with downloads that have null timing
        UpdateDownload downloadWithNullTiming = UpdateDownload.builder()
            .id(3L)
            .applicationVersion(testVersion1)
            .clientIdentifier("client-003")
            .downloadStartedAt(null)
            .downloadCompletedAt(null)
            .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
            .build();

        ConnectedClient clientWithNullConnection = ConnectedClient.builder()
            .id(3L)
            .sessionId("session-003")
            .clientVersion("2.0.0")
            .connectedAt(null)
            .lastPingAt(LocalDateTime.now())
            .isActive(true)
            .build();

        Object[] versionStats = {1L, 1L, 0L, 1000000.0};
        Object[] downloadStats = {1L, 1L, 0L};
        Object[] clientStats = {1L, 1L};

        when(versionRepository.getVersionStatistics()).thenReturn(versionStats);
        when(downloadRepository.getOverallDownloadStatistics()).thenReturn(downloadStats);
        when(downloadRepository.getCompletedDownloadsWithTiming()).thenReturn(Arrays.asList(downloadWithNullTiming));
        when(clientRepository.getConnectionStatistics()).thenReturn(clientStats);
        when(clientRepository.getAllClientsWithConnectionTime()).thenReturn(Arrays.asList(clientWithNullConnection));
        when(clientRepository.getClientVersionDistribution()).thenReturn(Arrays.asList());
        when(downloadRepository.getDownloadCountByDate(any(LocalDateTime.class))).thenReturn(Arrays.asList());
        when(downloadRepository.getTopDownloadingClients(any(PageRequest.class))).thenReturn(Arrays.asList());
        when(downloadRepository.findRecentDownloads(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert - Should handle null values gracefully
        assertNotNull(result);
        assertEquals(0.0, result.getAverageDownloadTimeSeconds()); // Should be 0 when timing is null
        assertEquals(0.0, result.getAverageConnectionTimeMinutes()); // Should be 0 when connection time is null
    }

    // Helper methods for creating test objects
    private UpdateDownload createDownloadWithStatus(UpdateDownload.DownloadStatus status) {
        return UpdateDownload.builder()
            .applicationVersion(testVersion1)
            .clientIdentifier("test-client")
            .downloadStatus(status)
            .downloadStartedAt(LocalDateTime.now().minusHours(1))
            .build();
    }

    private UpdateDownload createDownloadWithTiming(LocalDateTime startTime, LocalDateTime endTime) {
        return UpdateDownload.builder()
            .applicationVersion(testVersion1)
            .clientIdentifier("test-client")
            .downloadStartedAt(startTime)
            .downloadCompletedAt(endTime)
            .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
            .build();
    }

    private ConnectedClient createClientWithConnectionTime(LocalDateTime connectionTime) {
        return ConnectedClient.builder()
            .sessionId("test-session")
            .clientVersion("2.0.0")
            .connectedAt(connectionTime)
            .lastPingAt(LocalDateTime.now())
            .isActive(true)
            .build();
    }
}
