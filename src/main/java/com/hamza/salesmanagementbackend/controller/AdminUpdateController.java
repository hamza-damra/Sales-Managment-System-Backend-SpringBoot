package com.hamza.salesmanagementbackend.controller;
import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateStatisticsDTO;
import com.hamza.salesmanagementbackend.exception.FileUploadException;
import com.hamza.salesmanagementbackend.payload.response.ApiResponse;
import com.hamza.salesmanagementbackend.service.FileManagementService;
import com.hamza.salesmanagementbackend.service.UpdateManagementService;
import com.hamza.salesmanagementbackend.service.UpdateStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for admin update management operations
 */
@RestController
@RequestMapping("/api/v1/admin/updates")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUpdateController {

    private final UpdateManagementService updateManagementService;
    private final FileManagementService fileManagementService;
    private final UpdateStatisticsService statisticsService;

    /**
     * Get all versions with pagination
     * GET /api/v1/admin/updates/versions
     */
    @GetMapping("/versions")
    public ResponseEntity<ApiResponse<Page<ApplicationVersionDTO>>> getAllVersions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Admin request to get all versions - page: {}, size: {}", page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationVersionDTO> versions = updateManagementService.getAllVersions(pageable);
        
        ApiResponse<Page<ApplicationVersionDTO>> response = ApiResponse.<Page<ApplicationVersionDTO>>builder()
            .success(true)
            .data(versions)
            .message("Versions retrieved successfully")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all active versions
     * GET /api/v1/admin/updates/versions/active
     */
    @GetMapping("/versions/active")
    public ResponseEntity<ApiResponse<List<ApplicationVersionDTO>>> getAllActiveVersions() {
        log.info("Admin request to get all active versions");
        
        List<ApplicationVersionDTO> versions = updateManagementService.getAllActiveVersions();
        
        ApiResponse<List<ApplicationVersionDTO>> response = ApiResponse.<List<ApplicationVersionDTO>>builder()
            .success(true)
            .data(versions)
            .message("Active versions retrieved successfully")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create new version with file upload
     * POST /api/v1/admin/updates/versions
     */
    @PostMapping("/versions")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> createVersion(
            @RequestParam("versionNumber") String versionNumber,
            @RequestParam(value = "isMandatory", required = false, defaultValue = "false") Boolean isMandatory,
            @RequestParam("releaseNotes") String releaseNotes,
            @RequestParam(value = "minimumClientVersion", required = false) String minimumClientVersion,
            @RequestParam(value = "releaseDate", required = false) String releaseDateStr,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Admin request to create new version: {} by user: {}", versionNumber, authentication.getName());

        try {
            // Parse release date or use current time
            LocalDateTime releaseDate;
            if (releaseDateStr != null && !releaseDateStr.trim().isEmpty()) {
                try {
                    releaseDate = LocalDateTime.parse(releaseDateStr);
                } catch (Exception e) {
                    log.warn("Invalid release date format: {}, using current time", releaseDateStr);
                    releaseDate = LocalDateTime.now();
                }
            } else {
                releaseDate = LocalDateTime.now();
            }

            // Store the uploaded file
            String fileName = fileManagementService.storeFile(file, versionNumber);
            String checksum = fileManagementService.calculateChecksum(file);
            String downloadUrl = "/api/v1/updates/download/" + versionNumber;

            // Create version DTO
            ApplicationVersionDTO versionDTO = ApplicationVersionDTO.builder()
                .versionNumber(versionNumber)
                .releaseDate(releaseDate)
                .isMandatory(isMandatory)
                .isActive(true)
                .releaseNotes(releaseNotes)
                .minimumClientVersion(minimumClientVersion)
                .fileName(fileName)
                .fileSize(file.getSize())
                .fileChecksum(checksum)
                .downloadUrl(downloadUrl)
                .createdBy(authentication.getName())
                .build();
            
            ApplicationVersionDTO createdVersion = updateManagementService.createVersion(versionDTO);
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(createdVersion)
                .message("Version created successfully")
                .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (FileUploadException e) {
            log.error("File upload error for version {}: {}", versionNumber, e.getMessage());
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message("File upload failed: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Version creation error: {}", e.getMessage());
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message(e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    /**
     * Update version information
     * PUT /api/v1/admin/updates/versions/{id}
     */
    @PutMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> updateVersion(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationVersionDTO versionDTO) {
        
        log.info("Admin request to update version ID: {}", id);
        
        try {
            ApplicationVersionDTO updatedVersion = updateManagementService.updateVersion(id, versionDTO);
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(updatedVersion)
                .message("Version updated successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating version {}: {}", id, e.getMessage());
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message("Failed to update version: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update version status (active/inactive)
     * PUT /api/v1/admin/updates/versions/{id}/status
     */
    @PutMapping("/versions/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> updateVersionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusUpdate) {
        
        Boolean isActive = statusUpdate.get("isActive");
        log.info("Admin request to update version {} status to: {}", id, isActive);
        
        try {
            ApplicationVersionDTO updatedVersion = updateManagementService.updateVersionStatus(id, isActive);
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(updatedVersion)
                .message("Version status updated successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating version status {}: {}", id, e.getMessage());
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message("Failed to update version status: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete version
     * DELETE /api/v1/admin/updates/versions/{id}
     */
    @DeleteMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<String>> deleteVersion(@PathVariable Long id) {
        log.info("Admin request to delete version ID: {}", id);
        
        try {
            updateManagementService.deleteVersion(id);
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .data("Version deleted successfully")
                .message("Version deleted successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting version {}: {}", id, e.getMessage());
            
            ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .message("Failed to delete version: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get update system statistics
     * GET /api/v1/admin/updates/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UpdateStatisticsDTO>> getUpdateStatistics() {
        log.info("Admin request for update statistics");
        
        try {
            UpdateStatisticsDTO statistics = statisticsService.getUpdateStatistics();
            
            ApiResponse<UpdateStatisticsDTO> response = ApiResponse.<UpdateStatisticsDTO>builder()
                .success(true)
                .data(statistics)
                .message("Statistics retrieved successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving statistics: {}", e.getMessage());
            
            ApiResponse<UpdateStatisticsDTO> response = ApiResponse.<UpdateStatisticsDTO>builder()
                .success(false)
                .message("Failed to retrieve statistics: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get system health metrics
     * GET /api/v1/admin/updates/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        log.info("Admin request for system health metrics");
        
        try {
            Map<String, Object> healthMetrics = statisticsService.getSystemHealthMetrics();
            
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(healthMetrics)
                .message("System health metrics retrieved successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving health metrics: {}", e.getMessage());
            
            ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .success(false)
                .message("Failed to retrieve health metrics: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
