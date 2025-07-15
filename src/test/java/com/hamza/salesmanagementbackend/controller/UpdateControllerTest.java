package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateCheckResponseDTO;
import com.hamza.salesmanagementbackend.exception.UpdateNotFoundException;
import com.hamza.salesmanagementbackend.service.FileManagementService;
import com.hamza.salesmanagementbackend.service.UpdateManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UpdateController HTTP REST API endpoints
 */
@WebMvcTest(UpdateController.class)
public class UpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateManagementService updateManagementService;

    @MockBean
    private FileManagementService fileManagementService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationVersionDTO testVersionDTO;
    private UpdateCheckResponseDTO updateCheckResponseDTO;

    @BeforeEach
    void setUp() {
        testVersionDTO = ApplicationVersionDTO.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Test release notes for version 2.1.0")
            .minimumClientVersion("2.0.0")
            .fileName("sales-management-2.1.0.jar")
            .fileSize(52428800L) // 50MB
            .fileChecksum("sha256:abc123def456")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .createdBy("admin")
            .build();

        updateCheckResponseDTO = UpdateCheckResponseDTO.builder()
            .updateAvailable(true)
            .latestVersion("2.1.0")
            .currentVersion("2.0.0")
            .isMandatory(false)
            .releaseNotes("Test release notes for version 2.1.0")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .fileSize(52428800L)
            .checksum("sha256:abc123def456")
            .build();
    }

    // ==================== GET LATEST VERSION TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return latest version when authenticated user requests it")
    void getLatestVersion_ShouldReturnLatestVersion_WhenUserAuthenticated() throws Exception {
        // Given
        when(updateManagementService.getLatestVersion()).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.LATEST_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Test release notes for version 2.1.0")))
                .andExpect(jsonPath("$.data.fileName", is("sales-management-2.1.0.jar")))
                .andExpect(jsonPath("$.data.fileSize", is(52428800)))
                .andExpect(jsonPath("$.data.downloadUrl", is("/api/v1/updates/download/2.1.0")))
                .andExpect(jsonPath("$.message", is("Latest version retrieved successfully")));

        verify(updateManagementService).getLatestVersion();
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user requests latest version")
    void getLatestVersion_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.LATEST_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return error when no version is available")
    void getLatestVersion_ShouldReturnError_WhenNoVersionAvailable() throws Exception {
        // Given
        when(updateManagementService.getLatestVersion())
                .thenThrow(new UpdateNotFoundException("No active version found"));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.LATEST_ENDPOINT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("No active version found")));

        verify(updateManagementService).getLatestVersion();
    }

    // ==================== CHECK FOR UPDATES TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return update available when newer version exists")
    void checkForUpdates_ShouldReturnUpdateAvailable_WhenNewerVersionExists() throws Exception {
        // Given
        String currentVersion = "2.0.0";
        when(updateManagementService.checkForUpdates(currentVersion)).thenReturn(updateCheckResponseDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", currentVersion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.updateAvailable", is(true)))
                .andExpect(jsonPath("$.data.latestVersion", is("2.1.0")))
                .andExpect(jsonPath("$.data.currentVersion", is("2.0.0")))
                .andExpect(jsonPath("$.data.isMandatory", is(false)))
                .andExpect(jsonPath("$.data.downloadUrl", is("/api/v1/updates/download/2.1.0")))
                .andExpect(jsonPath("$.message", is("Update check completed successfully")));

        verify(updateManagementService).checkForUpdates(currentVersion);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return no update when current version is latest")
    void checkForUpdates_ShouldReturnNoUpdate_WhenCurrentVersionIsLatest() throws Exception {
        // Given
        String currentVersion = "2.1.0";
        UpdateCheckResponseDTO noUpdateResponse = UpdateCheckResponseDTO.builder()
            .updateAvailable(false)
            .latestVersion("2.1.0")
            .currentVersion("2.1.0")
            .isMandatory(false)
            .build();
        
        when(updateManagementService.checkForUpdates(currentVersion)).thenReturn(noUpdateResponse);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", currentVersion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.updateAvailable", is(false)))
                .andExpect(jsonPath("$.data.latestVersion", is("2.1.0")))
                .andExpect(jsonPath("$.data.currentVersion", is("2.1.0")));

        verify(updateManagementService).checkForUpdates(currentVersion);
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user checks for updates")
    void checkForUpdates_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", "2.0.0"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== DOWNLOAD UPDATE TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should download update file when valid version requested")
    void downloadUpdate_ShouldReturnFile_WhenValidVersionRequested() throws Exception {
        // Given
        String version = "2.1.0";
        byte[] fileContent = "Mock file content".getBytes();
        Resource mockResource = new ByteArrayResource(fileContent);
        
        when(fileManagementService.loadFileAsResource(anyString())).thenReturn(mockResource);
        when(updateManagementService.getVersionByNumber(version)).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/" + version))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("Content-Disposition", containsString("sales-management-2.1.0.jar")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(fileManagementService).loadFileAsResource(anyString());
        verify(updateManagementService).getVersionByNumber(version);
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user tries to download")
    void downloadUpdate_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/2.1.0"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET VERSION INFORMATION TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return version information when valid version requested")
    void getVersionInfo_ShouldReturnVersionInfo_WhenValidVersionRequested() throws Exception {
        // Given
        String version = "2.1.0";
        when(updateManagementService.getVersionByNumber(version)).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + "/version/" + version))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Test release notes for version 2.1.0")))
                .andExpect(jsonPath("$.data.fileSize", is(52428800)))
                .andExpect(jsonPath("$.message", is("Version information retrieved successfully")));

        verify(updateManagementService).getVersionByNumber(version);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return not found when version does not exist")
    void getVersionInfo_ShouldReturnNotFound_WhenVersionNotExists() throws Exception {
        // Given
        String version = "999.0.0";
        when(updateManagementService.getVersionByNumber(version))
                .thenThrow(new UpdateNotFoundException("Version not found: " + version));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + "/version/" + version))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version not found")));

        verify(updateManagementService).getVersionByNumber(version);
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user requests version info")
    void getVersionInfo_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + "/version/2.1.0"))
                .andExpect(status().isUnauthorized());
    }
}
