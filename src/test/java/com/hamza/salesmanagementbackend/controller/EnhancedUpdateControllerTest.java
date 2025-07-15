package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.*;
import com.hamza.salesmanagementbackend.entity.RateLimitTracker;
import com.hamza.salesmanagementbackend.exception.UpdateNotFoundException;
import com.hamza.salesmanagementbackend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for enhanced UpdateController endpoints
 */
@WebMvcTest(UpdateController.class)
class EnhancedUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpdateManagementService updateManagementService;

    @MockBean
    private FileManagementService fileManagementService;

    @MockBean
    private UpdateCompatibilityService compatibilityService;

    @MockBean
    private DifferentialUpdateService differentialUpdateService;

    @MockBean
    private RateLimitingService rateLimitingService;

    private ApplicationVersionDTO testVersionDTO;
    private UpdateMetadataDTO testMetadataDTO;
    private CompatibilityCheckDTO testCompatibilityDTO;
    private DifferentialUpdateDTO testDifferentialDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testVersionDTO = ApplicationVersionDTO.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes")
            .minimumClientVersion("2.0.0")
            .fileName("test-app-2.1.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .releaseChannel("STABLE")
            .createdBy("admin")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testMetadataDTO = UpdateMetadataDTO.builder()
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes")
            .minimumClientVersion("2.0.0")
            .fileName("test-app-2.1.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:abc123")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .releaseChannel("STABLE")
            .createdBy("admin")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testCompatibilityDTO = CompatibilityCheckDTO.builder()
            .isCompatible(true)
            .targetVersion("2.1.0")
            .clientVersion("2.0.0")
            .minimumRequiredVersion("2.0.0")
            .javaVersion(CompatibilityCheckDTO.JavaVersionInfo.builder()
                .required("11+")
                .detected("17.0.1")
                .vendor("Eclipse Adoptium")
                .isCompatible(true)
                .build())
            .operatingSystem(CompatibilityCheckDTO.OperatingSystemInfo.builder()
                .name("Windows 10")
                .version("10.0")
                .architecture("amd64")
                .isSupported(true)
                .build())
            .compatibilityIssues(java.util.Collections.emptyList())
            .recommendations(java.util.Collections.emptyList())
            .canProceed(true)
            .warningLevel(CompatibilityCheckDTO.WarningLevel.NONE)
            .build();

        testDifferentialDTO = DifferentialUpdateDTO.builder()
            .fromVersion("2.0.0")
            .toVersion("2.1.0")
            .deltaAvailable(true)
            .deltaSize(5242880L)
            .fullUpdateSize(52428800L)
            .compressionRatio(0.9)
            .deltaChecksum("sha256:def456")
            .deltaDownloadUrl("/api/v1/updates/delta/2.0.0/2.1.0/download")
            .fullDownloadUrl("/api/v1/updates/download/2.1.0")
            .changedFiles(java.util.Collections.emptyList())
            .patchInstructions(java.util.Collections.emptyList())
            .fallbackToFull(false)
            .estimatedApplyTimeSeconds(15)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();

        // Setup default rate limiting behavior (allow requests)
        RateLimitingService.RateLimitResult allowedResult = 
            RateLimitingService.RateLimitResult.allowed(10, 3600);
        when(rateLimitingService.checkRateLimit(anyString(), anyString(), any(RateLimitTracker.EndpointType.class)))
            .thenReturn(allowedResult);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getVersionMetadata_ShouldReturnMetadata_WhenVersionExists() throws Exception {
        // Given
        when(updateManagementService.getVersionByNumber("2.1.0")).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.METADATA_ENDPOINT + "/2.1.0")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionNumber").value("2.1.0"))
                .andExpect(jsonPath("$.data.fileName").value("test-app-2.1.0.jar"))
                .andExpect(jsonPath("$.data.fileSize").value(52428800))
                .andExpect(jsonPath("$.data.fileChecksum").value("sha256:abc123"))
                .andExpect(jsonPath("$.data.releaseChannel").value("STABLE"))
                .andExpect(jsonPath("$.message").value("Version metadata retrieved successfully"))
                .andExpect(header().string("X-RateLimit-Remaining", "10"))
                .andExpect(header().string("X-RateLimit-Reset", "3600"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getVersionMetadata_ShouldReturnNotFound_WhenVersionDoesNotExist() throws Exception {
        // Given
        when(updateManagementService.getVersionByNumber("2.1.0"))
            .thenThrow(new UpdateNotFoundException("Version not found"));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.METADATA_ENDPOINT + "/2.1.0")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getVersionMetadata_ShouldReturnTooManyRequests_WhenRateLimited() throws Exception {
        // Given
        RateLimitingService.RateLimitResult blockedResult = 
            RateLimitingService.RateLimitResult.blocked(300, "Rate limit exceeded");
        when(rateLimitingService.checkRateLimit(anyString(), anyString(), eq(RateLimitTracker.EndpointType.METADATA)))
            .thenReturn(blockedResult);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.METADATA_ENDPOINT + "/2.1.0")
                .with(csrf()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded"))
                .andExpect(header().string("X-RateLimit-Remaining", "0"))
                .andExpect(header().string("X-RateLimit-Reset", "300"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void checkCompatibility_ShouldReturnCompatibilityInfo_WhenValidRequest() throws Exception {
        // Given
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("clientVersion", "2.0.0");
        systemInfo.put("java.version", "17.0.1");
        systemInfo.put("os.name", "Windows 10");
        
        when(compatibilityService.checkCompatibility(eq("2.1.0"), eq("2.0.0"), any(Map.class)))
            .thenReturn(testCompatibilityDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.COMPATIBILITY_ENDPOINT + "/2.1.0")
                .param("clientVersion", "2.0.0")
                .param("java.version", "17.0.1")
                .param("os.name", "Windows 10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isCompatible").value(true))
                .andExpect(jsonPath("$.data.targetVersion").value("2.1.0"))
                .andExpect(jsonPath("$.data.clientVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.canProceed").value(true))
                .andExpect(jsonPath("$.data.warningLevel").value("NONE"))
                .andExpect(jsonPath("$.data.javaVersion.isCompatible").value(true))
                .andExpect(jsonPath("$.data.operatingSystem.isSupported").value(true))
                .andExpect(jsonPath("$.message").value("Compatibility check completed successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDifferentialUpdate_ShouldReturnDeltaInfo_WhenDeltaAvailable() throws Exception {
        // Given
        when(differentialUpdateService.generateDifferentialUpdate("2.0.0", "2.1.0"))
            .thenReturn(testDifferentialDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DELTA_ENDPOINT + "/2.0.0/2.1.0")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fromVersion").value("2.0.0"))
                .andExpect(jsonPath("$.data.toVersion").value("2.1.0"))
                .andExpect(jsonPath("$.data.deltaAvailable").value(true))
                .andExpect(jsonPath("$.data.deltaSize").value(5242880))
                .andExpect(jsonPath("$.data.fullUpdateSize").value(52428800))
                .andExpect(jsonPath("$.data.compressionRatio").value(0.9))
                .andExpect(jsonPath("$.data.fallbackToFull").value(false))
                .andExpect(jsonPath("$.message").value("Differential update information retrieved successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void downloadDifferentialUpdate_ShouldReturnFile_WhenDeltaExists() throws Exception {
        // Given
        byte[] deltaContent = "delta file content".getBytes();
        Resource deltaResource = new ByteArrayResource(deltaContent);
        when(differentialUpdateService.getDeltaFile("2.0.0", "2.1.0")).thenReturn(deltaResource);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DELTA_ENDPOINT + "/2.0.0/2.1.0/download")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"delta_2.0.0_to_2.1.0.delta\""))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(deltaContent));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAvailableChannels_ShouldReturnChannelInfo() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHANNELS_ENDPOINT)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stable").exists())
                .andExpect(jsonPath("$.data.beta").exists())
                .andExpect(jsonPath("$.data.nightly").exists())
                .andExpect(jsonPath("$.data.lts").exists())
                .andExpect(jsonPath("$.data.hotfix").exists())
                .andExpect(jsonPath("$.data.stable.channel").value("STABLE"))
                .andExpect(jsonPath("$.data.stable.autoUpdateEnabled").value(true))
                .andExpect(jsonPath("$.data.beta.requiresApproval").value(true))
                .andExpect(jsonPath("$.message").value("Available release channels retrieved successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLatestVersionForChannel_ShouldReturnLatestVersion() throws Exception {
        // Given
        when(updateManagementService.getLatestVersion()).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHANNELS_ENDPOINT + "/stable" + ApplicationConstants.LATEST_ENDPOINT)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionNumber").value("2.1.0"))
                .andExpect(jsonPath("$.data.releaseChannel").value("STABLE"))
                .andExpect(jsonPath("$.message").value("Latest version for channel 'stable' retrieved successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLatestVersionForChannel_ShouldReturnNotFound_WhenNoVersionsExist() throws Exception {
        // Given
        when(updateManagementService.getLatestVersion())
            .thenThrow(new UpdateNotFoundException("No versions found"));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHANNELS_ENDPOINT + "/stable" + ApplicationConstants.LATEST_ENDPOINT)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getVersionMetadata_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.METADATA_ENDPOINT + "/2.1.0"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void checkCompatibility_ShouldReturnBadRequest_WhenMissingClientVersion() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.COMPATIBILITY_ENDPOINT + "/2.1.0")
                .param("java.version", "17.0.1")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getDifferentialUpdate_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Given
        when(differentialUpdateService.generateDifferentialUpdate("2.0.0", "2.1.0"))
            .thenThrow(new RuntimeException("Delta generation failed"));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DELTA_ENDPOINT + "/2.0.0/2.1.0")
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Error generating differential update: Delta generation failed"));
    }
}
