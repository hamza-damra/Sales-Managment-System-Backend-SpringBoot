package com.hamza.salesmanagementbackend.controller;
import com.hamza.salesmanagementbackend.dto.*;
import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import com.hamza.salesmanagementbackend.entity.ReleaseChannel;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.exception.UpdateNotFoundException;
import com.hamza.salesmanagementbackend.payload.response.ApiResponse;
import com.hamza.salesmanagementbackend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * REST Controller for client-facing update operations
 */
@RestController
@RequestMapping("/api/v1/updates")
@RequiredArgsConstructor
@Slf4j
public class UpdateController {

    private final UpdateManagementService updateManagementService;
    private final FileManagementService fileManagementService;
    private final UpdateCompatibilityService compatibilityService;
    private final DifferentialUpdateService differentialUpdateService;
    private final RateLimitingService rateLimitingService;

    /**
     * Get the latest available version
     * GET /api/v1/updates/latest
     */
    @GetMapping("/latest")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> getLatestVersion() {
        log.info("Request received to get latest version");
        
        try {
            ApplicationVersionDTO latestVersion = updateManagementService.getLatestVersion();
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(latestVersion)
                .message("Latest version retrieved successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (UpdateNotFoundException e) {
            log.warn("No active versions found: {}", e.getMessage());
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message("No active versions available")
                .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Check for updates for a specific client version
     * GET /api/v1/updates/check?currentVersion={version}
     */
    @GetMapping("/check")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UpdateCheckResponseDTO>> checkForUpdates(
            @RequestParam("currentVersion") String currentVersion) {
        
        log.info("Update check request for client version: {}", currentVersion);
        
        try {
            UpdateCheckResponseDTO updateCheck = updateManagementService.checkForUpdates(currentVersion);
            
            ApiResponse<UpdateCheckResponseDTO> response = ApiResponse.<UpdateCheckResponseDTO>builder()
                .success(true)
                .data(updateCheck)
                .message("Update check completed successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during update check for version {}: {}", currentVersion, e.getMessage());
            
            ApiResponse<UpdateCheckResponseDTO> response = ApiResponse.<UpdateCheckResponseDTO>builder()
                .success(false)
                .message("Failed to check for updates: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Download update package for a specific version
     * GET /api/v1/updates/download/{version}
     * Supports resumable downloads with Range header
     */
    @GetMapping("/download/{version}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadUpdate(
            @PathVariable String version,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletRequest request) {

        log.info("Download request for version: {} from IP: {} (Range: {})",
                version, getClientIpAddress(request), rangeHeader);

        try {
            // Get version information
            ApplicationVersionDTO versionInfo = updateManagementService.getVersionByNumber(version);

            // Record download start
            String clientIdentifier = extractClientIdentifier(request);
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            UpdateDownload download = updateManagementService.recordDownloadStart(
                version, clientIdentifier, clientIp, userAgent);

            // Load file as resource (using relative path from database)
            String filePath = versionInfo.getFileName();
            Resource resource;

            // Handle range requests for resumable downloads
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                resource = handleRangeRequest(filePath, rangeHeader);
            } else {
                resource = fileManagementService.loadFileAsResource(filePath);
            }

            // Determine file's content type
            String contentType = determineContentType(request, versionInfo.getFileName());

            // Build response headers
            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                       "attachment; filename=\"" + extractFileName(versionInfo.getFileName()) + "\"")
                .header("Content-Length", String.valueOf(versionInfo.getFileSize()))
                .header("X-Checksum", versionInfo.getFileChecksum())
                .header("X-Version", version)
                .header("Accept-Ranges", "bytes")
                .header("X-Download-ID", download.getId().toString());

            // Add cache control headers
            responseBuilder.header("Cache-Control", "private, max-age=3600")
                          .header("ETag", "\"" + versionInfo.getFileChecksum() + "\"");

            log.info("Starting download for version {} (Download ID: {})", version, download.getId());

            return responseBuilder.body(resource);

        } catch (UpdateNotFoundException e) {
            log.warn("Version not found for download: {}", version);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error during file download for version {}: {}", version, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get version information by version number
     * GET /api/v1/updates/version/{version}
     */
    @GetMapping("/version/{version}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> getVersionInfo(@PathVariable String version) {
        log.info("Request for version information: {}", version);
        
        try {
            ApplicationVersionDTO versionInfo = updateManagementService.getVersionByNumber(version);
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(versionInfo)
                .message("Version information retrieved successfully")
                .build();
            
            return ResponseEntity.ok(response);
            
        } catch (UpdateNotFoundException e) {
            log.warn("Version not found: {}", version);
            
            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(false)
                .message("Version not found: " + version)
                .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Health check endpoint for update service
     * GET /api/v1/updates/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.debug("Update service health check");
        
        ApiResponse<String> response = ApiResponse.<String>builder()
            .success(true)
            .data("Update service is running")
            .message("Health check successful")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Extract client identifier from request headers
     */
    private String extractClientIdentifier(HttpServletRequest request) {
        // Try to get client identifier from custom header
        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }

        // Fallback to user agent + IP combination
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);

        return String.format("%s_%s",
            userAgent != null ? userAgent.hashCode() : "unknown",
            clientIp != null ? clientIp.hashCode() : "unknown");
    }

    /**
     * Get client identifier from request headers (alias for extractClientIdentifier)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        return extractClientIdentifier(request);
    }

    /**
     * Handle range requests for resumable downloads
     */
    private Resource handleRangeRequest(String filePath, String rangeHeader) {
        try {
            // TODO: Implement partial content support using range header values
            // Parse range header (e.g., "bytes=0-1023")
            // String[] ranges = rangeHeader.substring(6).split("-");
            // long start = Long.parseLong(ranges[0]);
            // long end = ranges.length > 1 && !ranges[1].isEmpty() ?
            //           Long.parseLong(ranges[1]) : -1;

            // For now, return full resource (actual implementation would return partial content)
            // In production, you'd implement a custom Resource that supports byte ranges
            return fileManagementService.loadFileAsResource(filePath);

        } catch (Exception ex) {
            log.warn("Failed to parse range header: {}", rangeHeader, ex);
            return fileManagementService.loadFileAsResource(filePath);
        }
    }

    /**
     * Determine content type for file
     */
    private String determineContentType(HttpServletRequest request, String fileName) {
        String contentType = null;

        try {
            // Try to determine from servlet context
            contentType = request.getServletContext().getMimeType(fileName);
        } catch (Exception ex) {
            log.debug("Could not determine file type for {}", fileName);
        }

        // Fallback based on file extension
        if (contentType == null) {
            String extension = getFileExtension(fileName);
            switch (extension.toLowerCase()) {
                case "jar":
                    contentType = "application/java-archive";
                    break;
                case "exe":
                    contentType = "application/vnd.microsoft.portable-executable";
                    break;
                case "msi":
                    contentType = "application/x-msi";
                    break;
                case "dmg":
                    contentType = "application/x-apple-diskimage";
                    break;
                case "deb":
                    contentType = "application/vnd.debian.binary-package";
                    break;
                case "rpm":
                    contentType = "application/x-rpm";
                    break;
                default:
                    contentType = "application/octet-stream";
            }
        }

        return contentType;
    }

    /**
     * Extract file name from path
     */
    private String extractFileName(String filePath) {
        if (filePath == null) {
            return "download";
        }

        // Handle both forward and backward slashes
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }

    // ==================== NEW ENHANCED UPDATE ENDPOINTS ====================

    /**
     * Get metadata for a specific version without initiating download
     * GET /api/v1/updates/metadata/{version}
     */
    @GetMapping("/metadata/{version}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UpdateMetadataDTO>> getVersionMetadata(
            @PathVariable String version,
            HttpServletRequest request) {

        log.info("Metadata request for version: {} from IP: {}", version, getClientIpAddress(request));

        // Check rate limiting
        String clientId = getClientIdentifier(request);
        String clientIp = getClientIpAddress(request);
        RateLimitingService.RateLimitResult rateLimitResult = rateLimitingService
            .checkRateLimit(clientId, clientIp, RateLimitTracker.EndpointType.METADATA);

        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(ApiResponse.<UpdateMetadataDTO>builder()
                    .success(false)
                    .message(rateLimitResult.getMessage())
                    .build());
        }

        try {
            ApplicationVersionDTO versionDTO = updateManagementService.getVersionByNumber(version);

            UpdateMetadataDTO metadata = UpdateMetadataDTO.builder()
                .versionNumber(versionDTO.getVersionNumber())
                .releaseDate(versionDTO.getReleaseDate())
                .isMandatory(versionDTO.getIsMandatory())
                .isActive(versionDTO.getIsActive())
                .releaseNotes(versionDTO.getReleaseNotes())
                .minimumClientVersion(versionDTO.getMinimumClientVersion())
                .fileName(versionDTO.getFileName())
                .fileSize(versionDTO.getFileSize())
                .fileChecksum(versionDTO.getFileChecksum())
                .downloadUrl(versionDTO.getDownloadUrl())
                .releaseChannel(versionDTO.getReleaseChannel())
                .createdBy(versionDTO.getCreatedBy())
                .createdAt(versionDTO.getCreatedAt())
                .updatedAt(versionDTO.getUpdatedAt())
                .build();

            ApiResponse<UpdateMetadataDTO> response = ApiResponse.<UpdateMetadataDTO>builder()
                .success(true)
                .data(metadata)
                .message("Version metadata retrieved successfully")
                .build();

            return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(rateLimitResult.getRemainingRequests()))
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(response);

        } catch (UpdateNotFoundException e) {
            log.warn("Version not found: {}", version);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving metadata for version: {}", version, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<UpdateMetadataDTO>builder()
                    .success(false)
                    .message("Error retrieving version metadata: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Check system compatibility for a specific version
     * GET /api/v1/updates/compatibility/{version}
     */
    @GetMapping("/compatibility/{version}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CompatibilityCheckDTO>> checkCompatibility(
            @PathVariable String version,
            @RequestParam("clientVersion") String clientVersion,
            @RequestParam Map<String, String> systemInfo,
            HttpServletRequest request) {

        log.info("Compatibility check for version: {} from client version: {}", version, clientVersion);

        // Check rate limiting
        String clientId = getClientIdentifier(request);
        String clientIp = getClientIpAddress(request);
        RateLimitingService.RateLimitResult rateLimitResult = rateLimitingService
            .checkRateLimit(clientId, clientIp, RateLimitTracker.EndpointType.COMPATIBILITY);

        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(ApiResponse.<CompatibilityCheckDTO>builder()
                    .success(false)
                    .message(rateLimitResult.getMessage())
                    .build());
        }

        try {
            CompatibilityCheckDTO compatibility = compatibilityService
                .checkCompatibility(version, clientVersion, systemInfo);

            ApiResponse<CompatibilityCheckDTO> response = ApiResponse.<CompatibilityCheckDTO>builder()
                .success(true)
                .data(compatibility)
                .message("Compatibility check completed successfully")
                .build();

            return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(rateLimitResult.getRemainingRequests()))
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(response);

        } catch (Exception e) {
            log.error("Error checking compatibility for version: {}", version, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<CompatibilityCheckDTO>builder()
                    .success(false)
                    .message("Error checking compatibility: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Get differential update between two versions
     * GET /api/v1/updates/delta/{fromVersion}/{toVersion}
     */
    @GetMapping("/delta/{fromVersion}/{toVersion}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DifferentialUpdateDTO>> getDifferentialUpdate(
            @PathVariable String fromVersion,
            @PathVariable String toVersion,
            HttpServletRequest request) {

        log.info("Differential update request from {} to {}", fromVersion, toVersion);

        // Check rate limiting
        String clientId = getClientIdentifier(request);
        String clientIp = getClientIpAddress(request);
        RateLimitingService.RateLimitResult rateLimitResult = rateLimitingService
            .checkRateLimit(clientId, clientIp, RateLimitTracker.EndpointType.DELTA);

        if (!rateLimitResult.isAllowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(ApiResponse.<DifferentialUpdateDTO>builder()
                    .success(false)
                    .message(rateLimitResult.getMessage())
                    .build());
        }

        try {
            DifferentialUpdateDTO differentialUpdate = differentialUpdateService
                .generateDifferentialUpdate(fromVersion, toVersion);

            ApiResponse<DifferentialUpdateDTO> response = ApiResponse.<DifferentialUpdateDTO>builder()
                .success(true)
                .data(differentialUpdate)
                .message("Differential update information retrieved successfully")
                .build();

            return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(rateLimitResult.getRemainingRequests()))
                .header("X-RateLimit-Reset", String.valueOf(rateLimitResult.getResetTimeSeconds()))
                .body(response);

        } catch (Exception e) {
            log.error("Error generating differential update from {} to {}", fromVersion, toVersion, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<DifferentialUpdateDTO>builder()
                    .success(false)
                    .message("Error generating differential update: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Download differential update file
     * GET /api/v1/updates/delta/{fromVersion}/{toVersion}/download
     */
    @GetMapping("/delta/{fromVersion}/{toVersion}/download")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadDifferentialUpdate(
            @PathVariable String fromVersion,
            @PathVariable String toVersion,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletRequest request) {

        log.info("Delta download request from {} to {} from IP: {}",
                fromVersion, toVersion, getClientIpAddress(request));

        try {
            Resource deltaFile = differentialUpdateService.getDeltaFile(fromVersion, toVersion);

            if (!deltaFile.exists()) {
                log.warn("Delta file not found for {} to {}", fromVersion, toVersion);
                return ResponseEntity.notFound().build();
            }

            String fileName = "delta_" + fromVersion + "_to_" + toVersion + ".delta";

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            headers.add("X-Content-Type-Options", "nosniff");

            return ResponseEntity.ok()
                .headers(headers)
                .body(deltaFile);

        } catch (IOException e) {
            log.error("Error downloading delta file from {} to {}", fromVersion, toVersion, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error downloading delta file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get available release channels
     * GET /api/v1/updates/channels
     */
    @GetMapping("/channels")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, ReleaseChannelDTO>>> getAvailableChannels() {
        log.info("Request for available release channels");

        try {
            Map<String, ReleaseChannelDTO> channels = new java.util.HashMap<>();

            for (ReleaseChannel channel : ReleaseChannel.values()) {
                ReleaseChannelDTO channelDTO = ReleaseChannelDTO.builder()
                    .channel(mapToReleaseChannelDTO(channel))
                    .description(channel.getDescription())
                    .isActive(true)
                    .stabilityLevel(mapToStabilityLevel(channel))
                    .autoUpdateEnabled(channel.isProductionReady())
                    .requiresApproval(channel.requiresApproval())
                    .build();

                channels.put(channel.getChannelName(), channelDTO);
            }

            ApiResponse<Map<String, ReleaseChannelDTO>> response = ApiResponse.<Map<String, ReleaseChannelDTO>>builder()
                .success(true)
                .data(channels)
                .message("Available release channels retrieved successfully")
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving release channels", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<Map<String, ReleaseChannelDTO>>builder()
                    .success(false)
                    .message("Error retrieving release channels: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Get latest version for specific channel
     * GET /api/v1/updates/channels/{channel}/latest
     */
    @GetMapping("/channels/{channel}/latest")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApplicationVersionDTO>> getLatestVersionForChannel(
            @PathVariable String channel) {

        log.info("Request for latest version in channel: {}", channel);

        try {
            // For now, return the latest version regardless of channel
            // In a full implementation, you'd filter by channel
            ApplicationVersionDTO latestVersion = updateManagementService.getLatestVersion();

            ApiResponse<ApplicationVersionDTO> response = ApiResponse.<ApplicationVersionDTO>builder()
                .success(true)
                .data(latestVersion)
                .message("Latest version for channel '" + channel + "' retrieved successfully")
                .build();

            return ResponseEntity.ok(response);

        } catch (UpdateNotFoundException e) {
            log.warn("No versions found for channel: {}", channel);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving latest version for channel: {}", channel, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<ApplicationVersionDTO>builder()
                    .success(false)
                    .message("Error retrieving latest version for channel: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Helper method to map entity ReleaseChannel to DTO ReleaseChannel
     */
    private ReleaseChannelDTO.ReleaseChannel mapToReleaseChannelDTO(ReleaseChannel channel) {
        switch (channel) {
            case NIGHTLY:
                return ReleaseChannelDTO.ReleaseChannel.NIGHTLY;
            case BETA:
                return ReleaseChannelDTO.ReleaseChannel.BETA;
            case STABLE:
                return ReleaseChannelDTO.ReleaseChannel.STABLE;
            case LTS:
                return ReleaseChannelDTO.ReleaseChannel.LTS;
            case HOTFIX:
                return ReleaseChannelDTO.ReleaseChannel.HOTFIX;
            default:
                return ReleaseChannelDTO.ReleaseChannel.STABLE;
        }
    }

    /**
     * Helper method to map release channel to stability level
     */
    private ReleaseChannelDTO.StabilityLevel mapToStabilityLevel(ReleaseChannel channel) {
        switch (channel) {
            case NIGHTLY:
                return ReleaseChannelDTO.StabilityLevel.EXPERIMENTAL;
            case BETA:
                return ReleaseChannelDTO.StabilityLevel.BETA;
            case STABLE:
                return ReleaseChannelDTO.StabilityLevel.STABLE;
            case LTS:
                return ReleaseChannelDTO.StabilityLevel.LTS;
            case HOTFIX:
                return ReleaseChannelDTO.StabilityLevel.RC;
            default:
                return ReleaseChannelDTO.StabilityLevel.STABLE;
        }
    }
}
