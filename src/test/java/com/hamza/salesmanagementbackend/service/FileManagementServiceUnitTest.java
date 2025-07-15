package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.exception.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileManagementService JAR validation functionality
 * Tests only the validation logic without Spring context
 */
public class FileManagementServiceUnitTest {

    private FileManagementService fileManagementService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileManagementService = new FileManagementService();
        
        // Set test configuration using reflection
        ReflectionTestUtils.setField(fileManagementService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(fileManagementService, "maxFileSize", 104857600L); // 100MB
        ReflectionTestUtils.setField(fileManagementService, "allowedExtensions", "jar");
        ReflectionTestUtils.setField(fileManagementService, "enableResumableDownloads", true);
        ReflectionTestUtils.setField(fileManagementService, "cleanupOrphanedFiles", false);
        
        // Initialize the service
        fileManagementService.init();
    }

    // ==================== JAR EXTENSION VALIDATION TESTS ====================

    @Test
    @DisplayName("Should accept valid JAR file extensions (case-insensitive)")
    void storeFile_ShouldAcceptValidJarExtensions() {
        String[] validExtensions = {"jar", "JAR", "Jar", "jAr"};
        
        for (String ext : validExtensions) {
            MockMultipartFile validFile = createValidJarFile("test-app." + ext);
            
            assertDoesNotThrow(() -> {
                fileManagementService.storeFile(validFile, "2.1.0");
            }, "Should accept JAR extension: " + ext);
        }
    }

    @Test
    @DisplayName("Should reject non-JAR file extensions")
    void storeFile_ShouldRejectNonJarExtensions() {
        String[] invalidExtensions = {"txt", "pdf", "doc", "zip", "tar", "exe", "msi", "dmg", "deb", "rpm"};

        for (String ext : invalidExtensions) {
            MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test-application." + ext,
                "application/octet-stream",
                "Test content".getBytes()
            );

            FileUploadException exception = assertThrows(FileUploadException.class, () -> {
                fileManagementService.storeFile(invalidFile, "2.1.0");
            }, "Should reject extension: " + ext);
            
            assertTrue(exception.getMessage().contains("Only JAR files are allowed"));
        }
    }

    // ==================== MIME TYPE VALIDATION TESTS ====================

    @Test
    @DisplayName("Should accept valid MIME types for JAR files")
    void storeFile_ShouldAcceptValidJarMimeTypes() {
        String[] validMimeTypes = {
            "application/java-archive",
            "application/x-java-archive",
            "application/zip",
            "application/octet-stream"
        };

        for (String mimeType : validMimeTypes) {
            MockMultipartFile validFile = createValidJarFile("test-app.jar", mimeType);
            
            assertDoesNotThrow(() -> {
                fileManagementService.storeFile(validFile, "2.1.0");
            }, "Should accept MIME type: " + mimeType);
        }
    }

    @Test
    @DisplayName("Should reject invalid MIME types for JAR files")
    void storeFile_ShouldRejectInvalidMimeTypes() {
        String[] invalidMimeTypes = {
            "text/plain",
            "application/pdf",
            "image/jpeg",
            "application/x-executable",
            "application/x-msdownload"
        };

        for (String mimeType : invalidMimeTypes) {
            MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test-app.jar",
                mimeType,
                createValidJarBytes()
            );

            FileUploadException exception = assertThrows(FileUploadException.class, () -> {
                fileManagementService.storeFile(invalidFile, "2.1.0");
            }, "Should reject MIME type: " + mimeType);
            
            assertTrue(exception.getMessage().contains("Invalid MIME type"));
        }
    }

    // ==================== FILE STRUCTURE VALIDATION TESTS ====================

    @Test
    @DisplayName("Should reject files with invalid magic bytes")
    void storeFile_ShouldRejectInvalidMagicBytes() {
        byte[] invalidBytes = "This is not a JAR file".getBytes();
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "test-app.jar",
            "application/java-archive",
            invalidBytes
        );

        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(invalidFile, "2.1.0");
        });
        
        assertTrue(exception.getMessage().contains("does not have valid ZIP/JAR magic bytes"));
    }

    @Test
    @DisplayName("Should accept valid JAR structure")
    void storeFile_ShouldAcceptValidJarStructure() {
        MockMultipartFile validJar = createValidJarFile("valid-app.jar");
        
        assertDoesNotThrow(() -> {
            fileManagementService.storeFile(validJar, "2.1.0");
        });
    }

    @Test
    @DisplayName("Should reject JAR with path traversal entries")
    void storeFile_ShouldRejectPathTraversal() {
        MockMultipartFile maliciousJar = createJarWithPathTraversal("malicious-app.jar");
        
        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(maliciousJar, "2.1.0");
        });
        
        assertTrue(exception.getMessage().contains("path traversal"));
    }

    @Test
    @DisplayName("Should reject empty JAR files")
    void storeFile_ShouldRejectEmptyJar() {
        MockMultipartFile emptyJar = createEmptyJarFile("empty-app.jar");
        
        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(emptyJar, "2.1.0");
        });
        
        assertTrue(exception.getMessage().contains("appears to be empty"));
    }

    @Test
    @DisplayName("Should reject files with invalid filename patterns")
    void storeFile_ShouldRejectInvalidFilenames() {
        String[] invalidFilenames = {
            "../malicious.jar",
            "..\\malicious.jar",
            "/absolute/path.jar",
            "C:\\absolute\\path.jar",
            "file/with/slash.jar",
            "file\\with\\backslash.jar"
        };

        for (String filename : invalidFilenames) {
            MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                filename,
                "application/java-archive",
                createValidJarBytes()
            );

            FileUploadException exception = assertThrows(FileUploadException.class, () -> {
                fileManagementService.storeFile(invalidFile, "2.1.0");
            }, "Should reject filename: " + filename);
            
            assertTrue(exception.getMessage().contains("invalid path characters"));
        }
    }

    // ==================== HELPER METHODS ====================

    private MockMultipartFile createValidJarFile(String filename) {
        return createValidJarFile(filename, "application/java-archive");
    }

    private MockMultipartFile createValidJarFile(String filename, String mimeType) {
        return new MockMultipartFile(
            "file",
            filename,
            mimeType,
            createValidJarBytes()
        );
    }

    private byte[] createValidJarBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // Add manifest file
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);
            String manifest = "Manifest-Version: 1.0\n" +
                            "Main-Class: com.example.TestApp\n" +
                            "Created-By: Test\n\n";
            zos.write(manifest.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            
            // Add a sample class file
            ZipEntry classEntry = new ZipEntry("com/example/TestApp.class");
            zos.putNextEntry(classEntry);
            zos.write("fake class file content".getBytes());
            zos.closeEntry();
            
            zos.finish();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test JAR", e);
        }
    }

    private MockMultipartFile createJarWithPathTraversal(String filename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // Add manifest
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);
            zos.write("Manifest-Version: 1.0\n".getBytes());
            zos.closeEntry();
            
            // Add malicious entry with path traversal
            ZipEntry maliciousEntry = new ZipEntry("../../../malicious.txt");
            zos.putNextEntry(maliciousEntry);
            zos.write("malicious content".getBytes());
            zos.closeEntry();
            
            zos.finish();
            return new MockMultipartFile(
                "file",
                filename,
                "application/java-archive",
                baos.toByteArray()
            );
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to create malicious JAR", e);
        }
    }

    private MockMultipartFile createEmptyJarFile(String filename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            
            // Create empty ZIP/JAR
            zos.finish();
            return new MockMultipartFile(
                "file",
                filename,
                "application/java-archive",
                baos.toByteArray()
            );
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to create empty JAR", e);
        }
    }
}
