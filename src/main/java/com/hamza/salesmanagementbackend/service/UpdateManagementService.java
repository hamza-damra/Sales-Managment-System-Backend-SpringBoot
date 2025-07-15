package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateCheckResponseDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.exception.UpdateNotFoundException;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing application updates and version control
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateManagementService {

    private final ApplicationVersionRepository versionRepository;
    private final UpdateDownloadRepository downloadRepository;
    private final FileManagementService fileManagementService;

    /**
     * Get the latest active version
     */
    @Transactional(readOnly = true)
    public ApplicationVersionDTO getLatestVersion() {
        log.debug("Fetching latest active version");

        try {
            ApplicationVersion latestVersion = versionRepository.findLatestActiveVersion()
                .orElseThrow(UpdateNotFoundException::noActiveVersions);

            log.debug("Found latest active version: {}", latestVersion.getVersionNumber());
            return convertToDTO(latestVersion);
        } catch (Exception ex) {
            log.error("Error fetching latest active version", ex);
            throw new RuntimeException("Failed to fetch latest version", ex);
        }
    }

    /**
     * Check for updates for a given client version
     */
    @Transactional(readOnly = true)
    public UpdateCheckResponseDTO checkForUpdates(String currentVersion) {
        log.debug("Checking for updates for client version: {}", currentVersion);

        try {
            Optional<ApplicationVersion> latestVersionOpt = versionRepository.findLatestActiveVersion();

            if (latestVersionOpt.isEmpty()) {
                log.warn("No active versions available for update check");
                return UpdateCheckResponseDTO.builder()
                    .updateAvailable(false)
                    .currentVersion(currentVersion)
                    .build();
            }

            ApplicationVersion latestVersion = latestVersionOpt.get();
            boolean updateAvailable = latestVersion.isNewerThan(currentVersion);

            log.debug("Update check result - Current: {}, Latest: {}, Update Available: {}",
                     currentVersion, latestVersion.getVersionNumber(), updateAvailable);

            return UpdateCheckResponseDTO.builder()
                .updateAvailable(updateAvailable)
                .latestVersion(latestVersion.getVersionNumber())
                .currentVersion(currentVersion)
                .isMandatory(latestVersion.getIsMandatory())
                .releaseNotes(latestVersion.getReleaseNotes())
                .downloadUrl(latestVersion.getDownloadUrl())
                .fileSize(latestVersion.getFileSize())
                .checksum(latestVersion.getFileChecksum())
                .minimumClientVersion(latestVersion.getMinimumClientVersion())
                .build();
        } catch (Exception ex) {
            log.error("Error during update check for version {}", currentVersion, ex);
            throw new RuntimeException("Failed to check for updates", ex);
        }
    }

    /**
     * Get version by version number
     */
    @Transactional(readOnly = true)
    public ApplicationVersionDTO getVersionByNumber(String versionNumber) {
        log.debug("Fetching version by number: {}", versionNumber);

        try {
            ApplicationVersion version = versionRepository.findByVersionNumber(versionNumber)
                .orElseThrow(() -> UpdateNotFoundException.forVersion(versionNumber));

            return convertToDTO(version);
        } catch (Exception ex) {
            log.error("Error fetching version by number: {}", versionNumber, ex);
            throw new RuntimeException("Failed to fetch version: " + versionNumber, ex);
        }
    }

    /**
     * Get all versions with pagination
     */
    @Transactional(readOnly = true)
    public Page<ApplicationVersionDTO> getAllVersions(Pageable pageable) {
        log.debug("Fetching all versions with pagination: {}", pageable);
        
        Page<ApplicationVersion> versions = versionRepository.findAllOrderByReleaseDateDesc(pageable);
        return versions.map(this::convertToDTO);
    }

    /**
     * Get all active versions
     */
    @Transactional(readOnly = true)
    public List<ApplicationVersionDTO> getAllActiveVersions() {
        log.debug("Fetching all active versions");
        
        List<ApplicationVersion> versions = versionRepository.findAllActiveVersionsOrderByReleaseDateDesc();
        return versions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Create a new version with enhanced file storage integration
     */
    @Transactional
    public ApplicationVersionDTO createVersion(ApplicationVersionDTO versionDTO) {
        log.info("Creating new version: {} with file path: {}",
                versionDTO.getVersionNumber(), versionDTO.getFileName());

        // Validate version number format
        if (!isValidVersionNumber(versionDTO.getVersionNumber())) {
            throw new IllegalArgumentException("Invalid version number format: " + versionDTO.getVersionNumber() +
                ". Expected format: x.y.z (e.g., 2.1.0)");
        }

        // Check if version already exists
        if (versionRepository.existsByVersionNumber(versionDTO.getVersionNumber())) {
            throw new IllegalArgumentException("Version " + versionDTO.getVersionNumber() + " already exists");
        }

        // Additional check using our new method to handle potential race conditions
        Optional<ApplicationVersion> existingVersion = versionRepository.findByVersionNumber(versionDTO.getVersionNumber());
        if (existingVersion.isPresent()) {
            throw new IllegalArgumentException("Version " + versionDTO.getVersionNumber() + " already exists");
        }

        try {
            ApplicationVersion version = convertToEntity(versionDTO);
            version.setCreatedAt(LocalDateTime.now());
            version.setUpdatedAt(LocalDateTime.now());

            // If this is set to active, deactivate other versions first
            if (Boolean.TRUE.equals(versionDTO.getIsActive())) {
                deactivateAllVersions();
            }

            ApplicationVersion savedVersion = versionRepository.save(version);
            log.info("Successfully created version: {} with file stored at: {}",
                    savedVersion.getVersionNumber(), savedVersion.getFileName());

            return convertToDTO(savedVersion);

        } catch (Exception ex) {
            log.error("Failed to create version: {}", versionDTO.getVersionNumber(), ex);
            throw new RuntimeException("Failed to create version", ex);
        }
    }

    /**
     * Validate version number format (semantic versioning)
     */
    private boolean isValidVersionNumber(String versionNumber) {
        if (versionNumber == null || versionNumber.trim().isEmpty()) {
            return false;
        }
        // Basic semantic versioning pattern: x.y.z or x.y.z-suffix
        return versionNumber.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$");
    }

    /**
     * Deactivate all versions (used when activating a new version)
     */
    private void deactivateAllVersions() {
        log.debug("Deactivating all versions");
        List<ApplicationVersion> activeVersions = versionRepository.findByIsActiveAndIsMandatoryOrderByReleaseDateDesc(true, null);
        for (ApplicationVersion version : activeVersions) {
            version.setIsActive(false);
            version.setUpdatedAt(LocalDateTime.now());
            versionRepository.save(version);
        }
    }

    /**
     * Update an existing version
     */
    public ApplicationVersionDTO updateVersion(Long id, ApplicationVersionDTO versionDTO) {
        log.info("Updating version with ID: {}", id);
        
        ApplicationVersion existingVersion = versionRepository.findById(id)
            .orElseThrow(() -> UpdateNotFoundException.forId(id));
        
        // Update fields
        existingVersion.setIsMandatory(versionDTO.getIsMandatory());
        existingVersion.setIsActive(versionDTO.getIsActive());
        existingVersion.setReleaseNotes(versionDTO.getReleaseNotes());
        existingVersion.setMinimumClientVersion(versionDTO.getMinimumClientVersion());
        existingVersion.setUpdatedAt(LocalDateTime.now());
        
        ApplicationVersion savedVersion = versionRepository.save(existingVersion);
        log.info("Successfully updated version: {}", savedVersion.getVersionNumber());
        
        return convertToDTO(savedVersion);
    }

    /**
     * Update version status (active/inactive)
     */
    public ApplicationVersionDTO updateVersionStatus(Long id, Boolean isActive) {
        log.info("Updating version status - ID: {}, Active: {}", id, isActive);
        
        ApplicationVersion version = versionRepository.findById(id)
            .orElseThrow(() -> UpdateNotFoundException.forId(id));
        
        version.setIsActive(isActive);
        version.setUpdatedAt(LocalDateTime.now());
        
        ApplicationVersion savedVersion = versionRepository.save(version);
        log.info("Successfully updated version status: {}", savedVersion.getVersionNumber());
        
        return convertToDTO(savedVersion);
    }

    /**
     * Delete a version with file cleanup
     */
    @Transactional
    public void deleteVersion(Long id) {
        log.info("Deleting version with ID: {}", id);

        ApplicationVersion version = versionRepository.findById(id)
            .orElseThrow(() -> UpdateNotFoundException.forId(id));

        String versionNumber = version.getVersionNumber();
        String fileName = version.getFileName();

        try {
            // Delete the version from database
            versionRepository.delete(version);

            // Delete the associated file(s)
            if (fileName != null && !fileName.isEmpty()) {
                try {
                    fileManagementService.deleteFile(fileName);
                    log.info("Successfully deleted file: {}", fileName);
                } catch (Exception ex) {
                    log.warn("Failed to delete file: {} (version still deleted from database)", fileName, ex);
                }
            }

            // Optionally delete the entire version directory if it's empty
            try {
                List<String> filesInVersion = fileManagementService.getFilesInVersion(versionNumber);
                if (filesInVersion.isEmpty()) {
                    fileManagementService.deleteVersionDirectory(versionNumber);
                    log.info("Deleted empty version directory: {}", versionNumber);
                }
            } catch (Exception ex) {
                log.debug("Could not clean up version directory: {}", versionNumber, ex);
            }

            log.info("Successfully deleted version: {} and associated files", versionNumber);

        } catch (Exception ex) {
            log.error("Error during version deletion: {}", versionNumber, ex);
            throw new RuntimeException("Failed to delete version completely", ex);
        }
    }

    /**
     * Record download start
     */
    public UpdateDownload recordDownloadStart(String versionNumber, String clientIdentifier, 
                                            String clientIp, String userAgent) {
        log.debug("Recording download start - Version: {}, Client: {}", versionNumber, clientIdentifier);
        
        ApplicationVersion version = versionRepository.findByVersionNumber(versionNumber)
            .orElseThrow(() -> UpdateNotFoundException.forVersion(versionNumber));
        
        UpdateDownload download = UpdateDownload.builder()
            .applicationVersion(version)
            .clientIdentifier(clientIdentifier)
            .clientIp(clientIp)
            .userAgent(userAgent)
            .downloadStartedAt(LocalDateTime.now())
            .downloadStatus(UpdateDownload.DownloadStatus.STARTED)
            .build();
        
        return downloadRepository.save(download);
    }

    /**
     * Convert entity to DTO
     */
    private ApplicationVersionDTO convertToDTO(ApplicationVersion version) {
        return ApplicationVersionDTO.builder()
            .id(version.getId())
            .versionNumber(version.getVersionNumber())
            .releaseDate(version.getReleaseDate())
            .isMandatory(version.getIsMandatory())
            .isActive(version.getIsActive())
            .releaseNotes(version.getReleaseNotes())
            .minimumClientVersion(version.getMinimumClientVersion())
            .fileName(version.getFileName())
            .fileSize(version.getFileSize())
            .fileChecksum(version.getFileChecksum())
            .downloadUrl(version.getDownloadUrl())
            .createdAt(version.getCreatedAt())
            .updatedAt(version.getUpdatedAt())
            .createdBy(version.getCreatedBy())
            .build();
    }

    /**
     * Convert DTO to entity
     */
    private ApplicationVersion convertToEntity(ApplicationVersionDTO dto) {
        return ApplicationVersion.builder()
            .versionNumber(dto.getVersionNumber())
            .releaseDate(dto.getReleaseDate())
            .isMandatory(dto.getIsMandatory())
            .isActive(dto.getIsActive())
            .releaseNotes(dto.getReleaseNotes())
            .minimumClientVersion(dto.getMinimumClientVersion())
            .fileName(dto.getFileName())
            .fileSize(dto.getFileSize())
            .fileChecksum(dto.getFileChecksum())
            .downloadUrl(dto.getDownloadUrl())
            .createdBy(dto.getCreatedBy())
            .build();
    }
}
