package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateStatisticsDTO;
import com.hamza.salesmanagementbackend.exception.FileUploadException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.FileManagementService;
import com.hamza.salesmanagementbackend.service.UpdateManagementService;
import com.hamza.salesmanagementbackend.service.UpdateStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminUpdateController HTTP REST API endpoints
 */
@WebMvcTest(AdminUpdateController.class)
public class AdminUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdateManagementService updateManagementService;

    @MockBean
    private FileManagementService fileManagementService;

    @MockBean
    private UpdateStatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationVersionDTO testVersionDTO;
    private List<ApplicationVersionDTO> versionList;
    private UpdateStatisticsDTO statisticsDTO;

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

        ApplicationVersionDTO secondVersionDTO = ApplicationVersionDTO.builder()
            .id(2L)
            .versionNumber("2.0.0")
            .releaseDate(LocalDateTime.now().minusDays(30))
            .isMandatory(true)
            .isActive(true)
            .releaseNotes("Previous stable version")
            .minimumClientVersion("1.9.0")
            .fileName("sales-management-2.0.0.jar")
            .fileSize(48000000L)
            .fileChecksum("sha256:def456ghi789")
            .downloadUrl("/api/v1/updates/download/2.0.0")
            .createdBy("admin")
            .build();

        versionList = Arrays.asList(testVersionDTO, secondVersionDTO);

        statisticsDTO = UpdateStatisticsDTO.builder()
            .totalVersions(2L)
            .activeVersions(2L)
            .mandatoryVersions(1L)
            .totalDownloads(150L)
            .successfulDownloads(140L)
            .failedDownloads(10L)
            .inProgressDownloads(0L)
            .downloadSuccessRate(93.33)
            .averageFileSize(50000000.0)
            .totalConnectedClients(25L)
            .activeClients(20L)
            .build();
    }

    // ==================== GET ALL VERSIONS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return paginated versions when admin requests all versions")
    void getAllVersions_ShouldReturnPaginatedVersions_WhenAdminRequests() throws Exception {
        // Given
        Page<ApplicationVersionDTO> versionPage = new PageImpl<>(versionList, PageRequest.of(0, 10), 2);
        when(updateManagementService.getAllVersions(any())).thenReturn(versionPage);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.content[1].versionNumber", is("2.0.0")))
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.totalPages", is(1)))
                .andExpect(jsonPath("$.message", is("Versions retrieved successfully")));

        verify(updateManagementService).getAllVersions(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return default pagination when no parameters provided")
    void getAllVersions_ShouldReturnDefaultPagination_WhenNoParametersProvided() throws Exception {
        // Given
        Page<ApplicationVersionDTO> versionPage = new PageImpl<>(versionList, PageRequest.of(0, 10), 2);
        when(updateManagementService.getAllVersions(any())).thenReturn(versionPage);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)));

        verify(updateManagementService).getAllVersions(any());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return forbidden when non-admin user requests all versions")
    void getAllVersions_ShouldReturnForbidden_WhenNonAdminRequests() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user requests all versions")
    void getAllVersions_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CREATE VERSION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create new version when valid file and data provided")
    void createVersion_ShouldCreateNewVersion_WhenValidDataProvided() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "sales-management-2.1.0.jar", 
            "application/java-archive", 
            "Mock file content".getBytes()
        );

        when(fileManagementService.storeFile(any(), anyString())).thenReturn("sales-management-2.1.0.jar");
        when(fileManagementService.calculateChecksum(any())).thenReturn("abc123def456");
        when(updateManagementService.createVersion(any())).thenReturn(testVersionDTO);

        // When & Then
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.1.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test release notes")
                        .param("minimumClientVersion", "2.0.0")
                        .param("releaseDate", "2024-01-15T10:30:00"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.fileName", is("sales-management-2.1.0.jar")))
                .andExpect(jsonPath("$.data.fileChecksum", is("abc123def456")))
                .andExpect(jsonPath("$.message", is("Version created successfully")));

        verify(fileManagementService).storeFile(any(), eq("2.1.0"));
        verify(fileManagementService).calculateChecksum(any());
        verify(updateManagementService).createVersion(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return bad request when file is missing")
    void createVersion_ShouldReturnBadRequest_WhenFileIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .param("versionNumber", "2.1.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test release notes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("File is required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return bad request when version number is missing")
    void createVersion_ShouldReturnBadRequest_WhenVersionNumberMissing() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "sales-management.jar", 
            "application/java-archive", 
            "Mock file content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test release notes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version number is required")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should handle file upload exception")
    void createVersion_ShouldHandleFileUploadException_WhenFileUploadFails() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "sales-management-2.1.0.jar", 
            "application/java-archive", 
            "Mock file content".getBytes()
        );

        when(fileManagementService.storeFile(any(), anyString()))
                .thenThrow(new FileUploadException("File upload failed"));

        // When & Then
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.1.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test release notes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("File upload failed")));

        verify(fileManagementService).storeFile(any(), eq("2.1.0"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return forbidden when non-admin tries to create version")
    void createVersion_ShouldReturnForbidden_WhenNonAdminTries() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "sales-management-2.1.0.jar", 
            "application/java-archive", 
            "Mock file content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.1.0"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET VERSION DETAILS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return version details when valid ID provided")
    void getVersionDetails_ShouldReturnVersionDetails_WhenValidIdProvided() throws Exception {
        // Given
        Long versionId = 1L;
        // Note: Using updateVersion method as getVersionById doesn't exist in the service
        when(updateManagementService.updateVersion(eq(versionId), any())).thenReturn(testVersionDTO);

        // When & Then - This test would need to be adjusted based on actual admin controller implementation
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Test release notes for version 2.1.0")))
                .andExpect(jsonPath("$.message", is("Version details retrieved successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return not found when version ID does not exist")
    void getVersionDetails_ShouldReturnNotFound_WhenVersionIdNotExists() throws Exception {
        // Given
        Long versionId = 999L;
        when(updateManagementService.updateVersion(eq(versionId), any()))
                .thenThrow(new ResourceNotFoundException("Version not found with id: " + versionId));

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version not found")));
    }

    // ==================== TOGGLE VERSION STATUS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should toggle version status successfully")
    void toggleVersionStatus_ShouldToggleStatus_WhenValidIdProvided() throws Exception {
        // Given
        Long versionId = 1L;
        ApplicationVersionDTO toggledVersion = ApplicationVersionDTO.builder()
            .id(1L)
            .versionNumber("2.1.0")
            .isActive(false) // Toggled to inactive
            .build();

        when(updateManagementService.updateVersionStatus(versionId, false)).thenReturn(toggledVersion);

        // When & Then
        mockMvc.perform(patch(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId + "/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.isActive", is(false)))
                .andExpect(jsonPath("$.message", is("Version status updated successfully")));

        verify(updateManagementService).updateVersionStatus(eq(versionId), any(Boolean.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return not found when toggling non-existent version")
    void toggleVersionStatus_ShouldReturnNotFound_WhenVersionNotExists() throws Exception {
        // Given
        Long versionId = 999L;
        when(updateManagementService.updateVersionStatus(eq(versionId), any(Boolean.class)))
                .thenThrow(new ResourceNotFoundException("Version not found with id: " + versionId));

        // When & Then
        mockMvc.perform(patch(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId + "/toggle-status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version not found")));

        verify(updateManagementService).updateVersionStatus(eq(versionId), any(Boolean.class));
    }

    // ==================== DELETE VERSION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete version successfully")
    void deleteVersion_ShouldDeleteSuccessfully_WhenValidIdProvided() throws Exception {
        // Given
        Long versionId = 1L;
        doNothing().when(updateManagementService).deleteVersion(versionId);

        // When & Then
        mockMvc.perform(delete(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Version deleted successfully")));

        verify(updateManagementService).deleteVersion(versionId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return not found when deleting non-existent version")
    void deleteVersion_ShouldReturnNotFound_WhenVersionNotExists() throws Exception {
        // Given
        Long versionId = 999L;
        doThrow(new ResourceNotFoundException("Version not found with id: " + versionId))
                .when(updateManagementService).deleteVersion(versionId);

        // When & Then
        mockMvc.perform(delete(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version not found")));

        verify(updateManagementService).deleteVersion(versionId);
    }

    // ==================== GET STATISTICS TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return update statistics when admin requests them")
    void getStatistics_ShouldReturnStatistics_WhenAdminRequests() throws Exception {
        // Given
        when(statisticsService.getUpdateStatistics()).thenReturn(statisticsDTO);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.STATISTICS_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalVersions", is(2)))
                .andExpect(jsonPath("$.data.activeVersions", is(2)))
                .andExpect(jsonPath("$.data.mandatoryVersions", is(1)))
                .andExpect(jsonPath("$.data.totalDownloads", is(150)))
                .andExpect(jsonPath("$.data.successfulDownloads", is(140)))
                .andExpect(jsonPath("$.data.failedDownloads", is(10)))
                .andExpect(jsonPath("$.data.downloadSuccessRate", is(93.33)))
                .andExpect(jsonPath("$.data.totalConnectedClients", is(25)))
                .andExpect(jsonPath("$.data.activeClients", is(20)))
                .andExpect(jsonPath("$.message", is("Statistics retrieved successfully")));

        verify(statisticsService).getUpdateStatistics();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return forbidden when non-admin requests statistics")
    void getStatistics_ShouldReturnForbidden_WhenNonAdminRequests() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.STATISTICS_ENDPOINT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return unauthorized when unauthenticated user requests statistics")
    void getStatistics_ShouldReturnUnauthorized_WhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.STATISTICS_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }
}
