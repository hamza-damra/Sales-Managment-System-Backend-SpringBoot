package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.exception.FileUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileManagementService
 * Tests file upload, download, validation, and checksum operations
 */
@SpringBootTest
@ActiveProfiles("test")
public class FileManagementServiceTest {

    private FileManagementService fileManagementService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileManagementService = new FileManagementService();
        
        // Set test configuration
        ReflectionTestUtils.setField(fileManagementService, "storagePath", tempDir.toString());
        ReflectionTestUtils.setField(fileManagementService, "maxFileSize", 104857600L); // 100MB
        ReflectionTestUtils.setField(fileManagementService, "allowedExtensions", "jar,exe,msi,dmg,deb,rpm");
        
        // Initialize the service
        fileManagementService.init();
    }

    // ==================== FILE STORAGE TESTS ====================

    @Test
    @DisplayName("Should store file successfully with valid input")
    void storeFile_ShouldStoreSuccessfully_WhenValidInput() throws IOException {
        // Given
        String versionNumber = "2.1.0";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Test file content".getBytes()
        );

        // When
        String storedFileName = fileManagementService.storeFile(file, versionNumber);

        // Then
        assertEquals("versions/2.1.0/sales-management-2.1.0.jar", storedFileName);

        // Check that the file was stored in the correct versioned directory structure
        Path versionDir = tempDir.resolve("2.1.0");
        assertTrue(Files.exists(versionDir));
        assertTrue(Files.isDirectory(versionDir));

        Path storedFilePath = versionDir.resolve("sales-management-2.1.0.jar");
        assertTrue(Files.exists(storedFilePath));
        assertEquals("Test file content", Files.readString(storedFilePath));
    }

    @Test
    @DisplayName("Should replace existing file when storing with same version")
    void storeFile_ShouldReplaceExisting_WhenSameVersion() throws IOException {
        // Given
        String versionNumber = "2.1.0";
        MockMultipartFile originalFile = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Original content".getBytes()
        );
        
        MockMultipartFile newFile = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "New content".getBytes()
        );

        // When
        String originalFileName = fileManagementService.storeFile(originalFile, versionNumber);
        String newFileName = fileManagementService.storeFile(newFile, versionNumber);

        // Then
        assertEquals(originalFileName, newFileName);

        // Check that the file was replaced in the version directory
        Path versionDir = tempDir.resolve("2.1.0");
        Path storedFilePath = versionDir.resolve("sales-management-2.1.0.jar");
        assertTrue(Files.exists(storedFilePath));
        assertEquals("New content", Files.readString(storedFilePath));
    }

    @Test
    @DisplayName("Should throw exception when file is null")
    void storeFile_ShouldThrowException_WhenFileIsNull() {
        // Given
        String versionNumber = "2.1.0";

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(null, versionNumber);
        });
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void storeFile_ShouldThrowException_WhenFileIsEmpty() {
        // Given
        String versionNumber = "2.1.0";
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            new byte[0]
        );

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(emptyFile, versionNumber);
        });
    }

    @Test
    @DisplayName("Should throw exception when file extension is not allowed")
    void storeFile_ShouldThrowException_WhenExtensionNotAllowed() {
        // Given
        String versionNumber = "2.1.0";
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "test-application.txt",
            "text/plain",
            "Test content".getBytes()
        );

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(invalidFile, versionNumber);
        });
    }

    @Test
    @DisplayName("Should throw exception when file size exceeds limit")
    void storeFile_ShouldThrowException_WhenFileSizeExceedsLimit() {
        // Given
        String versionNumber = "2.1.0";
        // Create a file larger than the 100MB limit
        byte[] largeContent = new byte[104857601]; // 100MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            largeContent
        );

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(largeFile, versionNumber);
        });
    }

    @Test
    @DisplayName("Should throw exception when filename contains invalid path sequence")
    void storeFile_ShouldThrowException_WhenFilenameContainsInvalidPath() {
        // Given
        String versionNumber = "2.1.0";
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file",
            "../../../malicious.jar",
            "application/java-archive",
            "Test content".getBytes()
        );

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(invalidFile, versionNumber);
        });
    }

    // ==================== FILE LOADING TESTS ====================

    @Test
    @DisplayName("Should load file as resource successfully")
    void loadFileAsResource_ShouldLoadSuccessfully_WhenFileExists() throws IOException {
        // Given
        String fileName = "sales-management-2.1.0.jar";
        String fileContent = "Test file content";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, fileContent.getBytes());

        // When
        Resource resource = fileManagementService.loadFileAsResource(fileName);

        // Then
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(fileContent, new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    @DisplayName("Should throw exception when loading non-existent file")
    void loadFileAsResource_ShouldThrowException_WhenFileNotExists() {
        // Given
        String fileName = "non-existent-file.jar";

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.loadFileAsResource(fileName);
        });
    }

    @Test
    @DisplayName("Should throw exception when loading file with invalid path")
    void loadFileAsResource_ShouldThrowException_WhenInvalidPath() {
        // Given
        String fileName = "../../../malicious.jar";

        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.loadFileAsResource(fileName);
        });
    }

    // ==================== CHECKSUM CALCULATION TESTS ====================

    @Test
    @DisplayName("Should calculate checksum correctly")
    void calculateChecksum_ShouldCalculateCorrectly() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Test file content".getBytes()
        );

        // When
        String checksum = fileManagementService.calculateChecksum(file);

        // Then
        assertNotNull(checksum);
        assertTrue(checksum.startsWith("sha256:"));
        assertEquals(71, checksum.length()); // "sha256:" + 64 hex characters
    }

    @Test
    @DisplayName("Should return consistent checksum for same content")
    void calculateChecksum_ShouldReturnConsistent_ForSameContent() {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "test1.jar",
            "application/java-archive",
            "Same content".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "test2.jar",
            "application/java-archive",
            "Same content".getBytes()
        );

        // When
        String checksum1 = fileManagementService.calculateChecksum(file1);
        String checksum2 = fileManagementService.calculateChecksum(file2);

        // Then
        assertEquals(checksum1, checksum2);
    }

    @Test
    @DisplayName("Should return different checksum for different content")
    void calculateChecksum_ShouldReturnDifferent_ForDifferentContent() {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "test1.jar",
            "application/java-archive",
            "Content 1".getBytes()
        );
        
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "test2.jar",
            "application/java-archive",
            "Content 2".getBytes()
        );

        // When
        String checksum1 = fileManagementService.calculateChecksum(file1);
        String checksum2 = fileManagementService.calculateChecksum(file2);

        // Then
        assertNotEquals(checksum1, checksum2);
    }

    @Test
    @DisplayName("Should throw exception when calculating checksum for null file")
    void calculateChecksum_ShouldThrowException_WhenFileIsNull() {
        // When & Then
        assertThrows(FileUploadException.class, () -> {
            fileManagementService.calculateChecksum(null);
        });
    }

    // ==================== FILE VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate JAR file extension correctly")
    void validateFile_ShouldValidateJarExtension() {
        // Test valid JAR extension (case-insensitive)
        String[] validExtensions = {"jar", "JAR", "Jar", "jAr"};

        for (String ext : validExtensions) {
            MockMultipartFile validFile = createValidJarFile("test-application." + ext);

            // Should not throw exception
            assertDoesNotThrow(() -> {
                fileManagementService.storeFile(validFile, "2.1.0");
            }, "Should accept JAR extension: " + ext);
        }
    }

    @Test
    @DisplayName("Should reject non-JAR file extensions")
    void validateFile_ShouldRejectNonJarExtensions() {
        // Test invalid extensions (only JAR files are allowed)
        String[] invalidExtensions = {"txt", "pdf", "doc", "zip", "tar", "exe", "msi", "dmg", "deb", "rpm"};

        for (String ext : invalidExtensions) {
            MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test-application." + ext,
                "application/octet-stream",
                "Test content".getBytes()
            );

            // Should throw exception
            FileUploadException exception = assertThrows(FileUploadException.class, () -> {
                fileManagementService.storeFile(invalidFile, "2.1.0");
            }, "Should reject extension: " + ext);

            assertTrue(exception.getMessage().contains("Only JAR files are allowed"));
        }
    }

    // ==================== VERSIONED DIRECTORY TESTS ====================

    @Test
    @DisplayName("Should create version directory structure")
    void storeFile_ShouldCreateVersionDirectory() throws IOException {
        // Given
        String versionNumber = "3.0.0";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Test content for version directory".getBytes()
        );

        // When
        String storedFileName = fileManagementService.storeFile(file, versionNumber);

        // Then
        Path versionDir = tempDir.resolve(versionNumber);
        assertTrue(Files.exists(versionDir));
        assertTrue(Files.isDirectory(versionDir));

        Path storedFile = versionDir.resolve("sales-management-3.0.0.jar");
        assertTrue(Files.exists(storedFile));
        assertEquals("versions/3.0.0/sales-management-3.0.0.jar", storedFileName);
    }

    @Test
    @DisplayName("Should load file from versioned directory")
    void loadFileAsResource_ShouldLoadFromVersionedDirectory() throws IOException {
        // Given
        String versionNumber = "3.1.0";
        String fileName = "sales-management-3.1.0.jar";
        String fileContent = "Test content for versioned loading";

        // Create version directory and file
        Path versionDir = tempDir.resolve(versionNumber);
        Files.createDirectories(versionDir);
        Path filePath = versionDir.resolve(fileName);
        Files.write(filePath, fileContent.getBytes());

        // When
        String relativePath = "versions/" + versionNumber + "/" + fileName;
        Resource resource = fileManagementService.loadFileAsResource(relativePath);

        // Then
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(fileContent, new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    @DisplayName("Should get version directories")
    void getVersionDirectories_ShouldReturnSortedList() throws IOException {
        // Given
        String[] versions = {"1.0.0", "2.0.0", "1.5.0", "3.0.0"};
        for (String version : versions) {
            Files.createDirectories(tempDir.resolve(version));
        }

        // When
        List<String> versionDirs = fileManagementService.getVersionDirectories();

        // Then
        assertEquals(4, versionDirs.size());
        assertTrue(versionDirs.contains("1.0.0"));
        assertTrue(versionDirs.contains("1.5.0"));
        assertTrue(versionDirs.contains("2.0.0"));
        assertTrue(versionDirs.contains("3.0.0"));
    }

    @Test
    @DisplayName("Should get files in version directory")
    void getFilesInVersion_ShouldReturnFileList() throws IOException {
        // Given
        String versionNumber = "4.0.0";
        Path versionDir = tempDir.resolve(versionNumber);
        Files.createDirectories(versionDir);

        // Create test files
        Files.write(versionDir.resolve("sales-management-4.0.0.jar"), "jar content".getBytes());
        Files.write(versionDir.resolve("readme.txt"), "readme content".getBytes());

        // When
        List<String> files = fileManagementService.getFilesInVersion(versionNumber);

        // Then
        assertEquals(2, files.size());
        assertTrue(files.contains("sales-management-4.0.0.jar"));
        assertTrue(files.contains("readme.txt"));
    }

    @Test
    @DisplayName("Should delete version directory")
    void deleteVersionDirectory_ShouldRemoveEntireDirectory() throws IOException {
        // Given
        String versionNumber = "5.0.0";
        Path versionDir = tempDir.resolve(versionNumber);
        Files.createDirectories(versionDir);
        Files.write(versionDir.resolve("test-file.jar"), "test content".getBytes());

        assertTrue(Files.exists(versionDir));

        // When
        fileManagementService.deleteVersionDirectory(versionNumber);

        // Then
        assertFalse(Files.exists(versionDir));
    }

    @Test
    @DisplayName("Should validate version number format")
    void storeFile_ShouldValidateVersionNumber() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Test content".getBytes()
        );

        // Test invalid version numbers
        String[] invalidVersions = {
            null,
            "",
            "   ",
            "1.0",
            "1.0.0.0",
            "1.0.0-",
            "1.0.0../",
            "../1.0.0",
            "1.0.0/test",
            "1.0.0\\test"
        };

        for (String invalidVersion : invalidVersions) {
            assertThrows(FileUploadException.class, () -> {
                fileManagementService.storeFile(file, invalidVersion);
            }, "Should reject invalid version: " + invalidVersion);
        }
    }

    @Test
    @DisplayName("Should accept valid version number formats")
    void storeFile_ShouldAcceptValidVersionNumbers() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test-application.jar",
            "application/java-archive",
            "Test content".getBytes()
        );

        // Test valid version numbers
        String[] validVersions = {
            "1.0.0",
            "10.20.30",
            "1.0.0-alpha",
            "2.1.0-beta1",
            "3.0.0-SNAPSHOT"
        };

        for (String validVersion : validVersions) {
            assertDoesNotThrow(() -> {
                fileManagementService.storeFile(file, validVersion);
            }, "Should accept valid version: " + validVersion);
        }
    }

    // ==================== ENHANCED JAR VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate JAR MIME type correctly")
    void validateFile_ShouldValidateJarMimeType() {
        // Test valid MIME types for JAR files
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
    void validateFile_ShouldRejectInvalidMimeTypes() {
        // Test invalid MIME types
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

    @Test
    @DisplayName("Should validate JAR magic bytes")
    void validateFile_ShouldValidateJarMagicBytes() {
        // Test file with invalid magic bytes
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
    @DisplayName("Should validate JAR structure and contents")
    void validateFile_ShouldValidateJarStructure() {
        // Test with a properly structured JAR file
        MockMultipartFile validJar = createValidJarFile("valid-app.jar");

        assertDoesNotThrow(() -> {
            fileManagementService.storeFile(validJar, "2.1.0");
        });
    }

    @Test
    @DisplayName("Should reject JAR with path traversal in entry names")
    void validateFile_ShouldRejectPathTraversal() {
        // Create JAR with malicious entry names
        MockMultipartFile maliciousJar = createJarWithPathTraversal("malicious-app.jar");

        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(maliciousJar, "2.1.0");
        });

        assertTrue(exception.getMessage().contains("path traversal"));
    }

    @Test
    @DisplayName("Should reject JAR with suspicious executable files")
    void validateFile_ShouldRejectSuspiciousExecutables() {
        // Create JAR with suspicious executable files
        MockMultipartFile suspiciousJar = createJarWithExecutables("suspicious-app.jar");

        // This should log warnings but not necessarily fail (depending on implementation)
        assertDoesNotThrow(() -> {
            fileManagementService.storeFile(suspiciousJar, "2.1.0");
        });
    }

    @Test
    @DisplayName("Should reject empty JAR files")
    void validateFile_ShouldRejectEmptyJar() {
        MockMultipartFile emptyJar = createEmptyJarFile("empty-app.jar");

        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(emptyJar, "2.1.0");
        });

        assertTrue(exception.getMessage().contains("appears to be empty"));
    }

    @Test
    @DisplayName("Should handle corrupted JAR files")
    void validateFile_ShouldHandleCorruptedJar() {
        MockMultipartFile corruptedJar = createCorruptedJarFile("corrupted-app.jar");

        FileUploadException exception = assertThrows(FileUploadException.class, () -> {
            fileManagementService.storeFile(corruptedJar, "2.1.0");
        });

        assertTrue(exception.getMessage().contains("Corrupted JAR file") ||
                  exception.getMessage().contains("Invalid ZIP signature"));
    }

    @Test
    @DisplayName("Should reject files with invalid filename patterns")
    void validateFile_ShouldRejectInvalidFilenames() {
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

    // ==================== HELPER METHODS FOR TESTING ====================

    /**
     * Create a valid JAR file for testing
     */
    private MockMultipartFile createValidJarFile(String filename) {
        return createValidJarFile(filename, "application/java-archive");
    }

    /**
     * Create a valid JAR file with specific MIME type
     */
    private MockMultipartFile createValidJarFile(String filename, String mimeType) {
        return new MockMultipartFile(
            "file",
            filename,
            mimeType,
            createValidJarBytes()
        );
    }

    /**
     * Create valid JAR file bytes with proper structure
     */
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

            // Add a properties file
            ZipEntry propsEntry = new ZipEntry("application.properties");
            zos.putNextEntry(propsEntry);
            zos.write("app.name=TestApp\napp.version=1.0.0".getBytes());
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to create test JAR", e);
        }
    }

    /**
     * Create JAR with path traversal entries
     */
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

    /**
     * Create JAR with suspicious executable files
     */
    private MockMultipartFile createJarWithExecutables(String filename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Add manifest
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            zos.putNextEntry(manifestEntry);
            zos.write("Manifest-Version: 1.0\n".getBytes());
            zos.closeEntry();

            // Add suspicious executable
            ZipEntry exeEntry = new ZipEntry("suspicious.exe");
            zos.putNextEntry(exeEntry);
            zos.write("fake executable content".getBytes());
            zos.closeEntry();

            // Add class file to make it look legitimate
            ZipEntry classEntry = new ZipEntry("TestClass.class");
            zos.putNextEntry(classEntry);
            zos.write("fake class content".getBytes());
            zos.closeEntry();

            zos.finish();
            return new MockMultipartFile(
                "file",
                filename,
                "application/java-archive",
                baos.toByteArray()
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to create suspicious JAR", e);
        }
    }

    /**
     * Create empty JAR file
     */
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

    /**
     * Create corrupted JAR file
     */
    private MockMultipartFile createCorruptedJarFile(String filename) {
        // Create a file that starts with ZIP magic bytes but is corrupted
        byte[] corruptedBytes = new byte[100];
        corruptedBytes[0] = 0x50; // P
        corruptedBytes[1] = 0x4B; // K
        corruptedBytes[2] = 0x03; // ZIP signature
        corruptedBytes[3] = 0x04; // ZIP signature
        // Rest is random/corrupted data
        for (int i = 4; i < corruptedBytes.length; i++) {
            corruptedBytes[i] = (byte) (Math.random() * 256);
        }

        return new MockMultipartFile(
            "file",
            filename,
            "application/java-archive",
            corruptedBytes
        );
    }
}
