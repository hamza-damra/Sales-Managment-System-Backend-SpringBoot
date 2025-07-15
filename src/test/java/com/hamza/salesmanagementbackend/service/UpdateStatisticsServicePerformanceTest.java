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
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for UpdateStatisticsService
 * Tests service performance with large datasets
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UpdateStatisticsServicePerformanceTest {

    @Autowired
    private UpdateStatisticsService updateStatisticsService;

    @Autowired
    private ApplicationVersionRepository versionRepository;

    @Autowired
    private UpdateDownloadRepository downloadRepository;

    @Autowired
    private ConnectedClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        // Clean up existing data
        downloadRepository.deleteAll();
        clientRepository.deleteAll();
        versionRepository.deleteAll();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testGetUpdateStatistics_LargeDataset_Performance() {
        // Arrange - Create large dataset
        createLargeDataset(100, 1000, 500); // 100 versions, 1000 downloads, 500 clients

        // Act & Assert - Should complete within 10 seconds
        long startTime = System.currentTimeMillis();
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert results
        assertNotNull(result);
        assertEquals(100L, result.getTotalVersions());
        assertEquals(1000L, result.getTotalDownloads());
        assertEquals(500L, result.getTotalConnectedClients());

        // Log performance metrics
        System.out.println("Execution time for large dataset: " + executionTime + "ms");
        assertTrue(executionTime < 10000, "Statistics generation should complete within 10 seconds");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testGetVersionStatistics_Performance() {
        // Arrange
        ApplicationVersion version = createTestVersion("2.1.0");
        createDownloadsForVersion(version, 500); // 500 downloads for one version

        // Act & Assert - Should complete within 5 seconds
        long startTime = System.currentTimeMillis();
        UpdateStatisticsDTO result = updateStatisticsService.getVersionStatistics(version.getId());
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert results
        assertNotNull(result);
        assertEquals(500L, result.getTotalDownloads());

        // Log performance metrics
        System.out.println("Execution time for version statistics: " + executionTime + "ms");
        assertTrue(executionTime < 5000, "Version statistics should complete within 5 seconds");
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testGetSystemHealthMetrics_Performance() {
        // Arrange
        createLargeDataset(50, 500, 250); // Moderate dataset

        // Act & Assert - Should complete within 3 seconds
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = updateStatisticsService.getSystemHealthMetrics();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert results
        assertNotNull(result);
        assertTrue(result.containsKey("activeVersions"));
        assertTrue(result.containsKey("activeClients"));
        assertTrue(result.containsKey("recentSuccessRate"));
        assertTrue(result.containsKey("staleConnections"));

        // Log performance metrics
        System.out.println("Execution time for system health metrics: " + executionTime + "ms");
        assertTrue(executionTime < 3000, "System health metrics should complete within 3 seconds");
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testGetVersionSuccessRate_Performance() {
        // Arrange
        ApplicationVersion version = createTestVersion("2.1.0");
        createDownloadsForVersion(version, 1000); // 1000 downloads

        // Act & Assert - Should complete within 2 seconds
        long startTime = System.currentTimeMillis();
        Double successRate = updateStatisticsService.getVersionSuccessRate(version.getId());
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert results
        assertNotNull(successRate);
        assertTrue(successRate >= 0.0 && successRate <= 100.0);

        // Log performance metrics
        System.out.println("Execution time for version success rate: " + executionTime + "ms");
        assertTrue(executionTime < 2000, "Version success rate should complete within 2 seconds");
    }

    @Test
    void testMemoryUsage_LargeDataset() {
        // Arrange
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Force garbage collection before test
        
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Create large dataset
        createLargeDataset(200, 2000, 1000); // Large dataset
        
        // Act
        UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        // Assert
        assertNotNull(result);
        
        // Log memory usage
        System.out.println("Memory used: " + (memoryUsed / 1024 / 1024) + " MB");
        
        // Memory usage should be reasonable (less than 100MB for this test)
        assertTrue(memoryUsed < 100 * 1024 * 1024, "Memory usage should be less than 100MB");
    }

    @Test
    void testConcurrentAccess_Performance() throws InterruptedException {
        // Arrange
        createLargeDataset(50, 500, 250);
        
        List<Thread> threads = new ArrayList<>();
        List<Long> executionTimes = new ArrayList<>();
        
        // Act - Create multiple threads accessing the service concurrently
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();
                long endTime = System.currentTimeMillis();
                
                synchronized (executionTimes) {
                    executionTimes.add(endTime - startTime);
                }
                
                assertNotNull(result);
            });
            threads.add(thread);
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(10000); // 10 second timeout
        }
        
        // Assert
        assertEquals(5, executionTimes.size());
        
        // Calculate average execution time
        double averageTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.println("Average execution time under concurrent access: " + averageTime + "ms");
        
        // All requests should complete within reasonable time
        assertTrue(averageTime < 5000, "Average execution time should be less than 5 seconds");
    }

    @Test
    void testScalability_IncreasingDataSize() {
        // Test with increasing data sizes to verify scalability
        int[] dataSizes = {10, 50, 100, 200};
        
        for (int size : dataSizes) {
            // Clean up before each test
            downloadRepository.deleteAll();
            clientRepository.deleteAll();
            versionRepository.deleteAll();
            
            // Create dataset of current size
            createLargeDataset(size / 10, size, size / 2);
            
            // Measure execution time
            long startTime = System.currentTimeMillis();
            UpdateStatisticsDTO result = updateStatisticsService.getUpdateStatistics();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            // Assert and log
            assertNotNull(result);
            System.out.println("Data size: " + size + ", Execution time: " + executionTime + "ms");
            
            // Execution time should scale reasonably (not exponentially)
            assertTrue(executionTime < size * 50, "Execution time should scale linearly with data size");
        }
    }

    // Helper methods
    private void createLargeDataset(int versionCount, int downloadCount, int clientCount) {
        // Create versions
        List<ApplicationVersion> versions = new ArrayList<>();
        for (int i = 0; i < versionCount; i++) {
            ApplicationVersion version = createTestVersion("2." + i + ".0");
            versions.add(version);
        }
        
        // Create downloads
        for (int i = 0; i < downloadCount; i++) {
            ApplicationVersion version = versions.get(i % versions.size());
            UpdateDownload download = UpdateDownload.builder()
                .applicationVersion(version)
                .clientIdentifier("client-" + i)
                .clientIp("192.168." + (i / 255) + "." + (i % 255))
                .userAgent("TestAgent/1.0")
                .downloadStartedAt(LocalDateTime.now().minusHours(i % 24))
                .downloadCompletedAt(i % 3 == 0 ? LocalDateTime.now().minusHours(i % 24).plusMinutes(5) : null)
                .downloadStatus(i % 3 == 0 ? UpdateDownload.DownloadStatus.COMPLETED : 
                               i % 3 == 1 ? UpdateDownload.DownloadStatus.FAILED : 
                               UpdateDownload.DownloadStatus.STARTED)
                .build();
            downloadRepository.save(download);
        }
        
        // Create clients
        for (int i = 0; i < clientCount; i++) {
            ConnectedClient client = ConnectedClient.builder()
                .sessionId("session-" + i)
                .clientVersion("2." + (i % 5) + ".0")
                .connectedAt(LocalDateTime.now().minusHours(i % 48))
                .lastPingAt(LocalDateTime.now().minusMinutes(i % 60))
                .clientIp("192.168." + (i / 255) + "." + (i % 255))
                .isActive(i % 4 != 0) // 75% active
                .build();
            clientRepository.save(client);
        }
    }

    private ApplicationVersion createTestVersion(String versionNumber) {
        ApplicationVersion version = ApplicationVersion.builder()
            .versionNumber(versionNumber)
            .releaseDate(LocalDateTime.now().minusDays(5))
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes")
            .minimumClientVersion("2.0.0")
            .fileName("sales-management-" + versionNumber + ".jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/" + versionNumber)
            .createdAt(LocalDateTime.now().minusDays(5))
            .updatedAt(LocalDateTime.now().minusDays(5))
            .createdBy("admin")
            .build();
        return versionRepository.save(version);
    }

    private void createDownloadsForVersion(ApplicationVersion version, int count) {
        for (int i = 0; i < count; i++) {
            UpdateDownload download = UpdateDownload.builder()
                .applicationVersion(version)
                .clientIdentifier("client-" + i)
                .clientIp("192.168." + (i / 255) + "." + (i % 255))
                .userAgent("TestAgent/1.0")
                .downloadStartedAt(LocalDateTime.now().minusHours(i % 24))
                .downloadCompletedAt(i % 2 == 0 ? LocalDateTime.now().minusHours(i % 24).plusMinutes(5) : null)
                .downloadStatus(i % 2 == 0 ? UpdateDownload.DownloadStatus.COMPLETED : UpdateDownload.DownloadStatus.FAILED)
                .build();
            downloadRepository.save(download);
        }
    }
}
