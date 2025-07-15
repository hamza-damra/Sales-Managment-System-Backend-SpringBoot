package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.dto.UpdateStatisticsDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.entity.ConnectedClient;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.ConnectedClientRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import com.hamza.salesmanagementbackend.service.UpdateStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UpdateStatisticsService
 * Tests the service with real database interactions
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UpdateStatisticsServiceIntegrationTest {

    @Autowired
    private UpdateStatisticsService updateStatisticsService;

    @Autowired
    private ApplicationVersionRepository versionRepository;

    @Autowired
    private UpdateDownloadRepository downloadRepository;

    @Autowired
    private ConnectedClientRepository clientRepository;

    private ApplicationVersion testVersion1;
    private ApplicationVersion testVersion2;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        downloadRepository.deleteAll();
        clientRepository.deleteAll();
        versionRepository.deleteAll();

        // Create test versions
        testVersion1 = ApplicationVersion.builder()
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

        testVersion1 = versionRepository.save(testVersion1);
        testVersion2 = versionRepository.save(testVersion2);
    }

    @Test
    void testGetUpdateStatistics_WithRealData() {
        // Arrange - Create test data
        createTestDownloads();
        createTestClients();

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalVersions());
        assertEquals(2L, result.getActiveVersions());
        assertEquals(1L, result.getMandatoryVersions()); // testVersion2 is mandatory
        assertTrue(result.getAverageFileSize() > 0);
        
        assertEquals(3L, result.getTotalDownloads());
        assertEquals(2L, result.getSuccessfulDownloads());
        assertEquals(1L, result.getFailedDownloads());
        assertEquals(0L, result.getInProgressDownloads());
        
        assertEquals(3L, result.getTotalConnectedClients());
        assertEquals(2L, result.getActiveClients());
        
        // Verify collections are populated
        assertNotNull(result.getClientVersionDistribution());
        assertNotNull(result.getDailyDownloadCounts());
        assertNotNull(result.getRecentDownloads());
        
        // Verify success rate calculation
        assertEquals(66.67, result.getDownloadSuccessRate(), 0.01); // 2/3 * 100 ≈ 66.67%
    }

    @Test
    void testGetVersionStatistics_WithRealData() {
        // Arrange
        createTestDownloads();

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getVersionStatistics(testVersion1.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getTotalDownloads()); // 2 downloads for testVersion1
        assertEquals(1L, result.getSuccessfulDownloads());
        assertEquals(1L, result.getFailedDownloads());
        assertEquals(0L, result.getInProgressDownloads());
    }

    @Test
    void testGetVersionSuccessRate_WithRealData() {
        // Arrange
        createTestDownloads();

        // Act
        Double successRate = updateStatisticsService.getVersionSuccessRate(testVersion1.getId());

        // Assert
        assertNotNull(successRate);
        assertEquals(50.0, successRate); // 1 successful out of 2 total = 50%
    }

    @Test
    void testGetSystemHealthMetrics_WithRealData() {
        // Arrange
        createTestDownloads();
        createTestClients();

        // Act
        Map<String, Object> result = updateStatisticsService.getSystemHealthMetrics();

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.get("activeVersions"));
        assertEquals(2L, result.get("activeClients"));
        assertTrue((Double) result.get("recentSuccessRate") >= 0.0);
        assertTrue((Integer) result.get("staleConnections") >= 0);
    }

    @Test
    void testGetUpdateStatistics_EmptyDatabase() {
        // Act - Test with empty database
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
        assertEquals(0L, result.getTotalConnectedClients());
        assertEquals(0L, result.getActiveClients());
        
        assertTrue(result.getClientVersionDistribution().isEmpty());
        assertTrue(result.getDailyDownloadCounts().isEmpty());
        assertTrue(result.getRecentDownloads().isEmpty());
    }

    @Test
    void testGetVersionSuccessRate_NonExistentVersion() {
        // Act
        Double successRate = updateStatisticsService.getVersionSuccessRate(999L);

        // Assert
        assertNotNull(successRate);
        assertEquals(0.0, successRate);
    }

    @Test
    void testClientVersionDistribution() {
        // Arrange
        createTestClients();

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        Map<String, Long> distribution = result.getClientVersionDistribution();
        assertNotNull(distribution);
        assertTrue(distribution.size() > 0);
        
        // Verify specific version counts
        assertTrue(distribution.containsKey("2.0.0"));
        assertTrue(distribution.containsKey("2.1.0"));
    }

    @Test
    void testRecentDownloadsLimit() {
        // Arrange - Create more than 10 downloads to test the limit
        for (int i = 0; i < 15; i++) {
            UpdateDownload download = UpdateDownload.builder()
                .applicationVersion(testVersion1)
                .clientIdentifier("client-" + i)
                .clientIp("192.168.1." + (100 + i))
                .userAgent("TestAgent/1.0")
                .downloadStartedAt(LocalDateTime.now().minusHours(i))
                .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
                .build();
            downloadRepository.save(download);
        }

        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();

        // Assert
        assertNotNull(result.getRecentDownloads());
        assertTrue(result.getRecentDownloads().size() <= 10); // Should be limited to 10
    }

    private void createTestDownloads() {
        // Create downloads for testVersion1
        UpdateDownload download1 = UpdateDownload.builder()
            .applicationVersion(testVersion1)
            .clientIdentifier("client-001")
            .clientIp("192.168.1.100")
            .userAgent("TestAgent/1.0")
            .downloadStartedAt(LocalDateTime.now().minusHours(2))
            .downloadCompletedAt(LocalDateTime.now().minusHours(2).plusMinutes(5))
            .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
            .build();

        UpdateDownload download2 = UpdateDownload.builder()
            .applicationVersion(testVersion1)
            .clientIdentifier("client-002")
            .clientIp("192.168.1.101")
            .userAgent("TestAgent/1.0")
            .downloadStartedAt(LocalDateTime.now().minusHours(1))
            .downloadStatus(UpdateDownload.DownloadStatus.FAILED)
            .build();

        // Create download for testVersion2
        UpdateDownload download3 = UpdateDownload.builder()
            .applicationVersion(testVersion2)
            .clientIdentifier("client-003")
            .clientIp("192.168.1.102")
            .userAgent("TestAgent/1.0")
            .downloadStartedAt(LocalDateTime.now().minusMinutes(30))
            .downloadCompletedAt(LocalDateTime.now().minusMinutes(25))
            .downloadStatus(UpdateDownload.DownloadStatus.COMPLETED)
            .build();

        downloadRepository.save(download1);
        downloadRepository.save(download2);
        downloadRepository.save(download3);
    }

    private void createTestClients() {
        ConnectedClient client1 = ConnectedClient.builder()
            .sessionId("session-001")
            .clientVersion("2.0.0")
            .connectedAt(LocalDateTime.now().minusHours(3))
            .lastPingAt(LocalDateTime.now().minusMinutes(1))
            .clientIp("192.168.1.100")
            .isActive(true)
            .build();

        ConnectedClient client2 = ConnectedClient.builder()
            .sessionId("session-002")
            .clientVersion("2.1.0")
            .connectedAt(LocalDateTime.now().minusHours(1))
            .lastPingAt(LocalDateTime.now().minusMinutes(2))
            .clientIp("192.168.1.101")
            .isActive(true)
            .build();

        ConnectedClient client3 = ConnectedClient.builder()
            .sessionId("session-003")
            .clientVersion("2.0.0")
            .connectedAt(LocalDateTime.now().minusHours(2))
            .lastPingAt(LocalDateTime.now().minusMinutes(10))
            .clientIp("192.168.1.102")
            .isActive(false)
            .build();

        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);
    }
}
