package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateCheckResponseDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.exception.UpdateNotFoundException;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateManagementService
 */
@ExtendWith(MockitoExtension.class)
class UpdateManagementServiceTest {

    @Mock
    private ApplicationVersionRepository versionRepository;

    @Mock
    private UpdateDownloadRepository downloadRepository;

    @InjectMocks
    private UpdateManagementService updateManagementService;

    private ApplicationVersion testVersion;

    @BeforeEach
    void setUp() {
        testVersion = ApplicationVersion.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes")
            .minimumClientVersion("2.0.0")
            .fileName("sales-management-2.1.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .createdBy("admin")
            .build();
    }

    @Test
    void getLatestVersion_ShouldReturnLatestVersion_WhenVersionExists() {
        // Given
        when(versionRepository.findLatestActiveVersion()).thenReturn(Optional.of(testVersion));

        // When
        ApplicationVersionDTO result = updateManagementService.getLatestVersion();

        // Then
        assertNotNull(result);
        assertEquals("2.1.0", result.getVersionNumber());
        assertEquals(testVersion.getFileSize(), result.getFileSize());
        assertEquals(testVersion.getFileChecksum(), result.getFileChecksum());
        verify(versionRepository).findLatestActiveVersion();
    }

    @Test
    void getLatestVersion_ShouldThrowException_WhenNoVersionExists() {
        // Given
        when(versionRepository.findLatestActiveVersion()).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateManagementService.getLatestVersion();
        });
        assertTrue(exception.getMessage().contains("Failed to fetch latest version"));
        assertTrue(exception.getCause() instanceof UpdateNotFoundException);
        verify(versionRepository).findLatestActiveVersion();
    }

    @Test
    void checkForUpdates_ShouldReturnUpdateAvailable_WhenNewerVersionExists() {
        // Given
        String currentVersion = "2.0.0";
        when(versionRepository.findLatestActiveVersion()).thenReturn(Optional.of(testVersion));

        // When
        UpdateCheckResponseDTO result = updateManagementService.checkForUpdates(currentVersion);

        // Then
        assertNotNull(result);
        assertTrue(result.getUpdateAvailable());
        assertEquals("2.1.0", result.getLatestVersion());
        assertEquals("2.0.0", result.getCurrentVersion());
        assertEquals(testVersion.getIsMandatory(), result.getIsMandatory());
        assertEquals(testVersion.getReleaseNotes(), result.getReleaseNotes());
        verify(versionRepository).findLatestActiveVersion();
    }

    @Test
    void checkForUpdates_ShouldReturnNoUpdate_WhenCurrentVersionIsSame() {
        // Given
        String currentVersion = "2.1.0";
        when(versionRepository.findLatestActiveVersion()).thenReturn(Optional.of(testVersion));

        // When
        UpdateCheckResponseDTO result = updateManagementService.checkForUpdates(currentVersion);

        // Then
        assertNotNull(result);
        assertFalse(result.getUpdateAvailable());
        assertEquals("2.1.0", result.getLatestVersion());
        assertEquals("2.1.0", result.getCurrentVersion());
        verify(versionRepository).findLatestActiveVersion();
    }

    @Test
    void checkForUpdates_ShouldReturnNoUpdate_WhenNoActiveVersions() {
        // Given
        String currentVersion = "2.0.0";
        when(versionRepository.findLatestActiveVersion()).thenReturn(Optional.empty());

        // When
        UpdateCheckResponseDTO result = updateManagementService.checkForUpdates(currentVersion);

        // Then
        assertNotNull(result);
        assertFalse(result.getUpdateAvailable());
        assertEquals("2.0.0", result.getCurrentVersion());
        assertNull(result.getLatestVersion());
        verify(versionRepository).findLatestActiveVersion();
    }

    @Test
    void getVersionByNumber_ShouldReturnVersion_WhenVersionExists() {
        // Given
        String versionNumber = "2.1.0";
        when(versionRepository.findByVersionNumber(versionNumber)).thenReturn(Optional.of(testVersion));

        // When
        ApplicationVersionDTO result = updateManagementService.getVersionByNumber(versionNumber);

        // Then
        assertNotNull(result);
        assertEquals(versionNumber, result.getVersionNumber());
        assertEquals(testVersion.getFileSize(), result.getFileSize());
        verify(versionRepository).findByVersionNumber(versionNumber);
    }

    @Test
    void getVersionByNumber_ShouldHandleMultipleResults_WhenDuplicatesExist() {
        // Given
        String versionNumber = "2.1.0";
        // Mock the repository to return the first result from a list
        when(versionRepository.findByVersionNumber(versionNumber)).thenReturn(Optional.of(testVersion));

        // When
        ApplicationVersionDTO result = updateManagementService.getVersionByNumber(versionNumber);

        // Then
        assertNotNull(result);
        assertEquals(versionNumber, result.getVersionNumber());
        verify(versionRepository).findByVersionNumber(versionNumber);
    }

    @Test
    void getVersionByNumber_ShouldThrowException_WhenVersionNotFound() {
        // Given
        String versionNumber = "2.1.0";
        when(versionRepository.findByVersionNumber(versionNumber)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateManagementService.getVersionByNumber(versionNumber);
        });
        assertTrue(exception.getMessage().contains("Failed to fetch version"));
        assertTrue(exception.getCause() instanceof UpdateNotFoundException);
        verify(versionRepository).findByVersionNumber(versionNumber);
    }

    @Test
    void createVersion_ShouldCreateVersion_WhenValidData() {
        // Given
        ApplicationVersionDTO versionDTO = ApplicationVersionDTO.builder()
            .versionNumber("2.2.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("New version")
            .fileName("sales-management-2.2.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:def456")
            .downloadUrl("/api/v1/updates/download/2.2.0")
            .createdBy("admin")
            .build();

        ApplicationVersion savedVersion = ApplicationVersion.builder()
            .id(2L)
            .versionNumber("2.2.0")
            .releaseDate(versionDTO.getReleaseDate())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("New version")
            .fileName("sales-management-2.2.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:def456")
            .downloadUrl("/api/v1/updates/download/2.2.0")
            .createdBy("admin")
            .build();

        when(versionRepository.existsByVersionNumber("2.2.0")).thenReturn(false);
        when(versionRepository.save(any(ApplicationVersion.class))).thenReturn(savedVersion);

        // When
        ApplicationVersionDTO result = updateManagementService.createVersion(versionDTO);

        // Then
        assertNotNull(result);
        assertEquals("2.2.0", result.getVersionNumber());
        assertEquals(2L, result.getId());
        verify(versionRepository).existsByVersionNumber("2.2.0");
        verify(versionRepository).save(any(ApplicationVersion.class));
    }

    @Test
    void createVersion_ShouldThrowException_WhenVersionAlreadyExists() {
        // Given
        ApplicationVersionDTO versionDTO = ApplicationVersionDTO.builder()
            .versionNumber("2.1.0")
            .build();

        when(versionRepository.existsByVersionNumber("2.1.0")).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            updateManagementService.createVersion(versionDTO);
        });
        verify(versionRepository).existsByVersionNumber("2.1.0");
        verify(versionRepository, never()).save(any(ApplicationVersion.class));
    }

    @Test
    void createVersion_ShouldThrowException_WhenVersionNumberIsInvalid() {
        // Given
        ApplicationVersionDTO versionDTO = ApplicationVersionDTO.builder()
            .versionNumber("invalid-version")
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updateManagementService.createVersion(versionDTO);
        });

        assertTrue(exception.getMessage().contains("Invalid version number format"));
        verify(versionRepository, never()).existsByVersionNumber(anyString());
        verify(versionRepository, never()).save(any(ApplicationVersion.class));
    }

    @Test
    void createVersion_ShouldDeactivateOtherVersions_WhenNewVersionIsActive() {
        // Given
        ApplicationVersionDTO versionDTO = ApplicationVersionDTO.builder()
            .versionNumber("2.2.0")
            .isActive(true)
            .fileName("test-file.jar")
            .fileSize(1000L)
            .fileChecksum("checksum")
            .downloadUrl("/download/2.2.0")
            .build();

        when(versionRepository.existsByVersionNumber("2.2.0")).thenReturn(false);
        when(versionRepository.findByVersionNumber("2.2.0")).thenReturn(Optional.empty());
        when(versionRepository.findByIsActiveAndIsMandatoryOrderByReleaseDateDesc(true, null))
            .thenReturn(Arrays.asList(testVersion));
        when(versionRepository.save(any(ApplicationVersion.class))).thenReturn(testVersion);

        // When
        updateManagementService.createVersion(versionDTO);

        // Then
        verify(versionRepository).findByIsActiveAndIsMandatoryOrderByReleaseDateDesc(true, null);
        verify(versionRepository, atLeast(2)).save(any(ApplicationVersion.class)); // Once for deactivation, once for new version
    }

    @Test
    void updateVersionStatus_ShouldUpdateStatus_WhenVersionExists() {
        // Given
        Long versionId = 1L;
        Boolean newStatus = false;
        
        ApplicationVersion updatedVersion = ApplicationVersion.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .isActive(false)
            .build();

        when(versionRepository.findById(versionId)).thenReturn(Optional.of(testVersion));
        when(versionRepository.save(any(ApplicationVersion.class))).thenReturn(updatedVersion);

        // When
        ApplicationVersionDTO result = updateManagementService.updateVersionStatus(versionId, newStatus);

        // Then
        assertNotNull(result);
        assertEquals(false, result.getIsActive());
        verify(versionRepository).findById(versionId);
        verify(versionRepository).save(any(ApplicationVersion.class));
    }

    @Test
    void deleteVersion_ShouldDeleteVersion_WhenVersionExists() {
        // Given
        Long versionId = 1L;
        when(versionRepository.findById(versionId)).thenReturn(Optional.of(testVersion));

        // When
        updateManagementService.deleteVersion(versionId);

        // Then
        verify(versionRepository).findById(versionId);
        verify(versionRepository).delete(testVersion);
    }

    @Test
    void deleteVersion_ShouldThrowException_WhenVersionNotFound() {
        // Given
        Long versionId = 1L;
        when(versionRepository.findById(versionId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UpdateNotFoundException.class, () -> {
            updateManagementService.deleteVersion(versionId);
        });
        verify(versionRepository).findById(versionId);
        verify(versionRepository, never()).delete(any(ApplicationVersion.class));
    }
}
