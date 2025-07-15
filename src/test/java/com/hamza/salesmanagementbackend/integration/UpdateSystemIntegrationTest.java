package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.ApplicationVersionDTO;
import com.hamza.salesmanagementbackend.dto.UpdateCheckResponseDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.entity.UpdateDownload;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import com.hamza.salesmanagementbackend.repository.UpdateDownloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Update System HTTP REST API
 * Tests the complete flow from HTTP requests to database operations
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class UpdateSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationVersionRepository versionRepository;

    @Autowired
    private UpdateDownloadRepository downloadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ApplicationVersion testVersion;

    @BeforeEach
    void setUp() {
        // Clean up database
        downloadRepository.deleteAll();
        versionRepository.deleteAll();

        // Create test version
        testVersion = ApplicationVersion.builder()
            .versionNumber("2.1.0")
            .releaseDate(LocalDateTime.now())
            .isMandatory(false)
            .isActive(true)
            .releaseNotes("Integration test version")
            .minimumClientVersion("2.0.0")
            .fileName("sales-management-2.1.0.jar")
            .fileSize(52428800L)
            .fileChecksum("sha256:integration-test-checksum")
            .downloadUrl("/api/v1/updates/download/2.1.0")
            .createdBy("test-admin")
            .build();

        testVersion = versionRepository.save(testVersion);
    }

    // ==================== CLIENT UPDATE ENDPOINTS INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should get latest version from database")
    void getLatestVersion_ShouldReturnFromDatabase() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.LATEST_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Integration test version")))
                .andExpect(jsonPath("$.data.fileName", is("sales-management-2.1.0.jar")))
                .andExpect(jsonPath("$.data.fileSize", is(52428800)))
                .andExpect(jsonPath("$.data.downloadUrl", is("/api/v1/updates/download/2.1.0")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should check for updates and return correct response")
    void checkForUpdates_ShouldReturnCorrectUpdateStatus() throws Exception {
        // Test with older version - should return update available
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", "2.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.updateAvailable", is(true)))
                .andExpect(jsonPath("$.data.latestVersion", is("2.1.0")))
                .andExpect(jsonPath("$.data.currentVersion", is("2.0.0")))
                .andExpect(jsonPath("$.data.isMandatory", is(false)))
                .andExpect(jsonPath("$.data.downloadUrl", is("/api/v1/updates/download/2.1.0")));

        // Test with same version - should return no update
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", "2.1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.updateAvailable", is(false)))
                .andExpect(jsonPath("$.data.latestVersion", is("2.1.0")))
                .andExpect(jsonPath("$.data.currentVersion", is("2.1.0")));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should get version information from database")
    void getVersionInfo_ShouldReturnFromDatabase() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + "/version/2.1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Integration test version")))
                .andExpect(jsonPath("$.data.fileSize", is(52428800)));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should return not found for non-existent version")
    void getVersionInfo_ShouldReturnNotFound_ForNonExistentVersion() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + "/version/999.0.0"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Version not found")));
    }

    // ==================== ADMIN ENDPOINTS INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should get all versions with pagination from database")
    void getAllVersions_ShouldReturnFromDatabase() throws Exception {
        // Create additional version for pagination test
        ApplicationVersion secondVersion = ApplicationVersion.builder()
            .versionNumber("2.0.0")
            .releaseDate(LocalDateTime.now().minusDays(30))
            .isMandatory(true)
            .isActive(true)
            .releaseNotes("Previous version")
            .minimumClientVersion("1.9.0")
            .fileName("sales-management-2.0.0.jar")
            .fileSize(48000000L)
            .fileChecksum("sha256:previous-version-checksum")
            .downloadUrl("/api/v1/updates/download/2.0.0")
            .createdBy("test-admin")
            .build();
        versionRepository.save(secondVersion);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.totalPages", is(1)))
                .andExpect(jsonPath("$.data.content[*].versionNumber", containsInAnyOrder("2.1.0", "2.0.0")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should get version details from database")
    void getVersionDetails_ShouldReturnFromDatabase() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + testVersion.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(testVersion.getId().intValue())))
                .andExpect(jsonPath("$.data.versionNumber", is("2.1.0")))
                .andExpect(jsonPath("$.data.releaseNotes", is("Integration test version")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should toggle version status in database")
    void toggleVersionStatus_ShouldUpdateDatabase() throws Exception {
        // Verify initial state
        assertTrue(testVersion.getIsActive());

        // Toggle status
        mockMvc.perform(patch(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + testVersion.getId() + "/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.isActive", is(false)));

        // Verify database was updated
        ApplicationVersion updatedVersion = versionRepository.findById(testVersion.getId()).orElse(null);
        assertNotNull(updatedVersion);
        assertFalse(updatedVersion.getIsActive());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should delete version from database")
    void deleteVersion_ShouldRemoveFromDatabase() throws Exception {
        // Verify version exists
        assertTrue(versionRepository.existsById(testVersion.getId()));

        // Delete version
        mockMvc.perform(delete(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + testVersion.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Version deleted successfully")));

        // Verify version was deleted
        assertFalse(versionRepository.existsById(testVersion.getId()));
    }

    // ==================== DOWNLOAD TRACKING INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should record download attempt in database")
    void downloadUpdate_ShouldRecordDownloadAttempt() throws Exception {
        // Verify no downloads initially
        assertEquals(0, downloadRepository.count());

        // Attempt download (will fail due to missing file, but should record attempt)
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/2.1.0"))
                .andExpect(status().isNotFound()); // File not found, but download attempt should be recorded

        // Verify download attempt was recorded
        assertEquals(1, downloadRepository.count());
        UpdateDownload download = downloadRepository.findAll().get(0);
        assertEquals(testVersion.getId(), download.getApplicationVersion().getId());
        assertEquals(UpdateDownload.DownloadStatus.STARTED, download.getDownloadStatus());
    }

    // ==================== FILE STORAGE INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should create version with file storage in versioned directory")
    void createVersion_ShouldStoreFileInVersionedDirectory() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales-management-2.2.0.jar",
            "application/java-archive",
            "Test file content for version 2.2.0".getBytes()
        );

        // When
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.2.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test version with file storage")
                        .param("minimumClientVersion", "2.0.0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.versionNumber", is("2.2.0")))
                .andExpect(jsonPath("$.data.fileName", containsString("versions/2.2.0/")));

        // Verify version was created in database
        Optional<ApplicationVersion> createdVersion = versionRepository.findByVersionNumber("2.2.0");
        assertTrue(createdVersion.isPresent());
        assertTrue(createdVersion.get().getFileName().startsWith("versions/2.2.0/"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Integration: Should delete version and cleanup associated files")
    void deleteVersion_ShouldCleanupFiles() throws Exception {
        // Given - create a version with file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales-management-2.3.0.jar",
            "application/java-archive",
            "Test file content for deletion test".getBytes()
        );

        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.3.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test version for deletion")
                        .param("minimumClientVersion", "2.0.0"))
                .andExpect(status().isCreated());

        // Verify version exists
        Optional<ApplicationVersion> createdVersion = versionRepository.findByVersionNumber("2.3.0");
        assertTrue(createdVersion.isPresent());
        Long versionId = createdVersion.get().getId();

        // When - delete the version
        mockMvc.perform(delete(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + versionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Then - verify version and files are deleted
        assertFalse(versionRepository.existsById(versionId));
        // Note: File cleanup verification would require actual file system access
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should support file download with proper headers")
    void downloadUpdate_ShouldReturnProperHeaders() throws Exception {
        // Given - create a version with actual file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales-management-2.4.0.jar",
            "application/java-archive",
            "Test file content for download test".getBytes()
        );

        // Create version as admin
        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.4.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test version for download")
                        .param("minimumClientVersion", "2.0.0")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isCreated());

        // When - download the file as user
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/2.4.0"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().exists("Content-Length"))
                .andExpect(header().exists("X-Checksum"))
                .andExpect(header().exists("X-Version"))
                .andExpect(header().exists("Accept-Ranges"))
                .andExpect(header().exists("X-Download-ID"))
                .andExpect(header().string("X-Version", "2.4.0"))
                .andExpect(header().string("Accept-Ranges", "bytes"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should handle range requests for resumable downloads")
    void downloadUpdate_ShouldHandleRangeRequests() throws Exception {
        // Given - create a version with file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sales-management-2.5.0.jar",
            "application/java-archive",
            "Test file content for range request test".getBytes()
        );

        mockMvc.perform(multipart(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT)
                        .file(file)
                        .param("versionNumber", "2.5.0")
                        .param("isMandatory", "false")
                        .param("releaseNotes", "Test version for range requests")
                        .param("minimumClientVersion", "2.0.0")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isCreated());

        // When - request with Range header
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/2.5.0")
                        .header("Range", "bytes=0-1023"))
                .andExpect(status().isOk())
                .andExpect(header().string("Accept-Ranges", "bytes"));
    }

    // ==================== SECURITY INTEGRATION TESTS ====================

    @Test
    @DisplayName("Integration: Should enforce authentication on all endpoints")
    void shouldEnforceAuthentication() throws Exception {
        // Test client endpoints
        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.LATEST_ENDPOINT))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.CHECK_ENDPOINT)
                        .param("currentVersion", "2.0.0"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + "/2.1.0"))
                .andExpect(status().isUnauthorized());

        // Test admin endpoints
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.STATISTICS_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Integration: Should enforce admin role on admin endpoints")
    void shouldEnforceAdminRole() throws Exception {
        // User should be forbidden from admin endpoints
        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.STATISTICS_ENDPOINT))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete(ApplicationConstants.API_V1_ADMIN_UPDATES + ApplicationConstants.VERSIONS_ENDPOINT + "/" + testVersion.getId()))
                .andExpect(status().isForbidden());
    }
}
