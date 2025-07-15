package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CompatibilityCheckDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateCompatibilityService
 */
@ExtendWith(MockitoExtension.class)
class UpdateCompatibilityServiceTest {

    @Mock
    private ApplicationVersionRepository versionRepository;

    @InjectMocks
    private UpdateCompatibilityService compatibilityService;

    private ApplicationVersion testVersion;
    private Map<String, String> systemInfo;

    @BeforeEach
    void setUp() {
        testVersion = ApplicationVersion.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release")
            .minimumClientVersion("2.0.0")
            .fileName("test-app-2.1.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .createdBy("admin")
            .build();

        systemInfo = new HashMap<>();
        systemInfo.put("java.version", "17.0.1");
        systemInfo.put("java.vendor", "Eclipse Adoptium");
        systemInfo.put("os.name", "Windows 10");
        systemInfo.put("os.version", "10.0");
        systemInfo.put("os.arch", "amd64");
        systemInfo.put("available.memory.mb", "8192");
        systemInfo.put("available.disk.mb", "50000");
    }

    @Test
    void checkCompatibility_ShouldReturnCompatible_WhenAllRequirementsMet() {
        // Given
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompatible());
        assertTrue(result.getCanProceed());
        assertEquals("2.1.0", result.getTargetVersion());
        assertEquals("2.0.0", result.getClientVersion());
        assertEquals("2.0.0", result.getMinimumRequiredVersion());
        assertEquals(CompatibilityCheckDTO.WarningLevel.NONE, result.getWarningLevel());
        assertTrue(result.getCompatibilityIssues().isEmpty());
        
        // Verify Java version info
        assertNotNull(result.getJavaVersion());
        assertTrue(result.getJavaVersion().getIsCompatible());
        assertEquals("17.0.1", result.getJavaVersion().getDetected());
        assertEquals("Eclipse Adoptium", result.getJavaVersion().getVendor());
        
        // Verify OS info
        assertNotNull(result.getOperatingSystem());
        assertTrue(result.getOperatingSystem().getIsSupported());
        assertEquals("Windows 10", result.getOperatingSystem().getName());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnIncompatible_WhenVersionNotFound() {
        // Given
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.empty());

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsCompatible());
        assertFalse(result.getCanProceed());
        assertEquals("2.1.0", result.getTargetVersion());
        assertEquals("2.0.0", result.getClientVersion());
        assertEquals(CompatibilityCheckDTO.WarningLevel.CRITICAL, result.getWarningLevel());
        assertFalse(result.getCompatibilityIssues().isEmpty());
        
        CompatibilityCheckDTO.CompatibilityIssue issue = result.getCompatibilityIssues().get(0);
        assertEquals(CompatibilityCheckDTO.IssueType.CONFIGURATION, issue.getType());
        assertEquals(CompatibilityCheckDTO.IssueSeverity.CRITICAL, issue.getSeverity());
        assertTrue(issue.getDescription().contains("Target version not found"));
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnIncompatible_WhenClientVersionTooOld() {
        // Given
        testVersion.setMinimumClientVersion("2.1.0");
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsCompatible());
        assertFalse(result.getCanProceed());
        assertEquals(CompatibilityCheckDTO.WarningLevel.CRITICAL, result.getWarningLevel());
        assertFalse(result.getCompatibilityIssues().isEmpty());
        
        CompatibilityCheckDTO.CompatibilityIssue issue = result.getCompatibilityIssues().get(0);
        assertEquals(CompatibilityCheckDTO.IssueType.JAVA_VERSION, issue.getType());
        assertEquals(CompatibilityCheckDTO.IssueSeverity.CRITICAL, issue.getSeverity());
        assertTrue(issue.getDescription().contains("below minimum required version"));
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnIncompatible_WhenJavaVersionTooOld() {
        // Given
        systemInfo.put("java.version", "8.0.301");
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsCompatible());
        assertFalse(result.getCanProceed());
        assertEquals(CompatibilityCheckDTO.WarningLevel.CRITICAL, result.getWarningLevel());
        
        // Should have Java version issue
        boolean hasJavaIssue = result.getCompatibilityIssues().stream()
            .anyMatch(issue -> issue.getType() == CompatibilityCheckDTO.IssueType.JAVA_VERSION &&
                             issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.CRITICAL);
        assertTrue(hasJavaIssue);
        
        // Verify Java version info
        assertNotNull(result.getJavaVersion());
        assertFalse(result.getJavaVersion().getIsCompatible());
        assertEquals("8.0.301", result.getJavaVersion().getDetected());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnWarning_WhenUnsupportedOS() {
        // Given
        systemInfo.put("os.name", "FreeBSD");
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompatible()); // Still compatible but with warnings
        assertTrue(result.getCanProceed());
        assertEquals(CompatibilityCheckDTO.WarningLevel.MEDIUM, result.getWarningLevel());
        
        // Should have OS warning
        boolean hasOSWarning = result.getCompatibilityIssues().stream()
            .anyMatch(issue -> issue.getType() == CompatibilityCheckDTO.IssueType.OPERATING_SYSTEM &&
                             issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.WARNING);
        assertTrue(hasOSWarning);
        
        // Verify OS info
        assertNotNull(result.getOperatingSystem());
        assertFalse(result.getOperatingSystem().getIsSupported());
        assertEquals("FreeBSD", result.getOperatingSystem().getName());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnWarning_WhenLowMemory() {
        // Given
        systemInfo.put("available.memory.mb", "256"); // Below 512MB minimum
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompatible()); // Still compatible but with warnings
        assertTrue(result.getCanProceed());
        assertEquals(CompatibilityCheckDTO.WarningLevel.MEDIUM, result.getWarningLevel());
        
        // Should have memory warning
        boolean hasMemoryWarning = result.getCompatibilityIssues().stream()
            .anyMatch(issue -> issue.getType() == CompatibilityCheckDTO.IssueType.MEMORY &&
                             issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.WARNING);
        assertTrue(hasMemoryWarning);
        
        // Verify system requirements
        assertNotNull(result.getSystemRequirements());
        assertEquals(Long.valueOf(256), result.getSystemRequirements().getAvailableMemoryMB());
        assertEquals(Long.valueOf(512), result.getSystemRequirements().getMinimumMemoryMB());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldReturnCritical_WhenInsufficientDiskSpace() {
        // Given
        systemInfo.put("available.disk.mb", "500"); // Below 1024MB minimum
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompatible()); // Still compatible but with critical disk space issue
        assertFalse(result.getCanProceed()); // Cannot proceed due to critical disk space issue
        assertEquals(CompatibilityCheckDTO.WarningLevel.CRITICAL, result.getWarningLevel());
        
        // Should have disk space critical issue
        boolean hasDiskIssue = result.getCompatibilityIssues().stream()
            .anyMatch(issue -> issue.getType() == CompatibilityCheckDTO.IssueType.DISK_SPACE &&
                             issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.CRITICAL);
        assertTrue(hasDiskIssue);
        
        // Verify system requirements
        assertNotNull(result.getSystemRequirements());
        assertEquals(Long.valueOf(500), result.getSystemRequirements().getAvailableDiskSpaceMB());
        assertEquals(Long.valueOf(1024), result.getSystemRequirements().getMinimumDiskSpaceMB());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldHandleMissingSystemInfo() {
        // Given
        Map<String, String> emptySystemInfo = new HashMap<>();
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", emptySystemInfo);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsCompatible()); // Should still be compatible with missing info
        assertTrue(result.getCanProceed());
        
        // Java version should be null but still compatible
        assertNotNull(result.getJavaVersion());
        assertNull(result.getJavaVersion().getDetected());
        assertTrue(result.getJavaVersion().getIsCompatible());
        
        // OS info should be null but still supported
        assertNotNull(result.getOperatingSystem());
        assertNull(result.getOperatingSystem().getName());
        assertTrue(result.getOperatingSystem().getIsSupported());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }

    @Test
    void checkCompatibility_ShouldHandleJava8VersionFormat() {
        // Given
        systemInfo.put("java.version", "1.8.0_301");
        when(versionRepository.findByVersionNumber("2.1.0")).thenReturn(Optional.of(testVersion));

        // When
        CompatibilityCheckDTO result = compatibilityService.checkCompatibility("2.1.0", "2.0.0", systemInfo);

        // Then
        assertNotNull(result);
        assertFalse(result.getIsCompatible()); // Java 8 should be incompatible
        assertFalse(result.getCanProceed());
        
        // Should have Java version issue
        boolean hasJavaIssue = result.getCompatibilityIssues().stream()
            .anyMatch(issue -> issue.getType() == CompatibilityCheckDTO.IssueType.JAVA_VERSION &&
                             issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.CRITICAL);
        assertTrue(hasJavaIssue);
        
        // Verify Java version info
        assertNotNull(result.getJavaVersion());
        assertFalse(result.getJavaVersion().getIsCompatible());
        assertEquals("1.8.0_301", result.getJavaVersion().getDetected());
        
        verify(versionRepository).findByVersionNumber("2.1.0");
    }
}
