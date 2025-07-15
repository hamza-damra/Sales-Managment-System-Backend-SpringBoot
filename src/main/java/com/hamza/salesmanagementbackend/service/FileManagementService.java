package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Service for managing file uploads, downloads, and storage for the update system
 */
@Service
@Slf4j
public class FileManagementService {

    @Value("${app.updates.storage-path:./versions}")
    private String storagePath;

    @Value("${app.updates.max-file-size:524288000}") // 500MB in bytes
    private long maxFileSize;

    @Value("${app.updates.allowed-extensions:jar,exe,msi,dmg,deb,rpm}")
    private String allowedExtensions;

    @Value("${app.updates.enable-resumable-downloads:true}")
    private boolean enableResumableDownloads;

    @Value("${app.updates.cleanup-orphaned-files:true}")
    private boolean cleanupOrphanedFiles;

    private Path storageLocation;
    private List<String> allowedExtensionsList;

    @PostConstruct
    public void init() {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        this.allowedExtensionsList = Arrays.asList(allowedExtensions.split(","));

        try {
            Files.createDirectories(this.storageLocation);
            log.info("Initialized versioned file storage at: {}", this.storageLocation);

            // Cleanup orphaned files if enabled
            if (cleanupOrphanedFiles) {
                scheduleOrphanedFileCleanup();
            }
        } catch (Exception ex) {
            throw new FileUploadException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store uploaded file in versioned directory structure
     * Creates directory structure: versions/{versionNumber}/sales-management-{versionNumber}.{extension}
     */
    public String storeFile(MultipartFile file, String versionNumber) {
        log.info("Storing file for version: {} in versioned directory structure", versionNumber);

        // Validate file and version number
        validateFile(file);
        validateVersionNumber(versionNumber);

        // Generate filename and paths
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = String.format("sales-management-%s.%s", versionNumber, fileExtension);

        try {
            // Create version-specific directory
            Path versionDirectory = createVersionDirectory(versionNumber);

            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new FileUploadException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the version-specific directory
            Path targetLocation = versionDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Calculate and verify file integrity
            String storedChecksum = calculateStoredFileChecksum(targetLocation);
            String uploadChecksum = calculateChecksum(file);

            if (!storedChecksum.equals(uploadChecksum)) {
                Files.deleteIfExists(targetLocation);
                throw new FileUploadException("File integrity check failed for: " + fileName);
            }

            log.info("Successfully stored file: {} in version directory: {}", fileName, versionNumber);
            return getRelativeFilePath(versionNumber, fileName);

        } catch (IOException ex) {
            throw FileUploadException.storageError(fileName, ex);
        }
    }

    /**
     * Load file as Resource from versioned directory structure
     * Supports both relative paths (versions/2.1.0/file.jar) and direct file names
     */
    public Resource loadFileAsResource(String filePath) {
        log.debug("Loading file as resource: {}", filePath);

        try {
            Path resolvedPath;

            // Handle relative paths from versions directory
            if (filePath.startsWith("versions/")) {
                resolvedPath = this.storageLocation.getParent().resolve(filePath).normalize();
            } else {
                // Legacy support: try to find file in version directories
                resolvedPath = findFileInVersionDirectories(filePath);
                if (resolvedPath == null) {
                    // Fallback to old flat structure
                    resolvedPath = this.storageLocation.resolve(filePath).normalize();
                }
            }

            // Security check: ensure the resolved path is within allowed directories
            if (!isPathSecure(resolvedPath)) {
                throw new FileUploadException("Access denied: Invalid file path " + filePath);
            }

            Resource resource = new UrlResource(resolvedPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw FileUploadException.fileNotFound(filePath);
            }
        } catch (MalformedURLException ex) {
            throw FileUploadException.fileNotFound(filePath);
        }
    }

    /**
     * Calculate SHA-256 checksum of uploaded file
     */
    public String calculateChecksum(MultipartFile file) {
        log.debug("Calculating checksum for file: {}", file.getOriginalFilename());
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String checksum = hexString.toString();
            log.debug("Calculated SHA-256 checksum: {}", checksum);
            return checksum;
            
        } catch (NoSuchAlgorithmException | IOException ex) {
            throw new FileUploadException("Failed to calculate file checksum", ex);
        }
    }

    /**
     * Delete file from versioned storage
     * Supports both relative paths and version-specific deletion
     */
    public void deleteFile(String filePath) {
        log.info("Deleting file: {}", filePath);

        try {
            Path resolvedPath;

            if (filePath.startsWith("versions/")) {
                resolvedPath = this.storageLocation.getParent().resolve(filePath).normalize();
            } else {
                resolvedPath = this.storageLocation.resolve(filePath).normalize();
            }

            // Security check
            if (!isPathSecure(resolvedPath)) {
                throw new FileUploadException("Access denied: Invalid file path " + filePath);
            }

            Files.deleteIfExists(resolvedPath);
            log.info("Successfully deleted file: {}", filePath);

        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filePath, ex);
            throw new FileUploadException("Failed to delete file: " + filePath, ex);
        }
    }

    /**
     * Delete entire version directory and all its contents
     */
    public void deleteVersionDirectory(String versionNumber) {
        log.info("Deleting version directory: {}", versionNumber);

        try {
            Path versionDir = this.storageLocation.resolve(versionNumber).normalize();

            if (Files.exists(versionDir)) {
                // Delete directory and all contents recursively
                Files.walk(versionDir)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

                log.info("Successfully deleted version directory: {}", versionNumber);
            }

        } catch (IOException ex) {
            log.error("Failed to delete version directory: {}", versionNumber, ex);
            throw new FileUploadException("Failed to delete version directory: " + versionNumber, ex);
        }
    }

    /**
     * Check if file exists in versioned storage
     */
    public boolean fileExists(String filePath) {
        try {
            Path resolvedPath;

            if (filePath.startsWith("versions/")) {
                resolvedPath = this.storageLocation.getParent().resolve(filePath).normalize();
            } else {
                resolvedPath = findFileInVersionDirectories(filePath);
                if (resolvedPath == null) {
                    resolvedPath = this.storageLocation.resolve(filePath).normalize();
                }
            }

            return resolvedPath != null && Files.exists(resolvedPath) && isPathSecure(resolvedPath);
        } catch (Exception ex) {
            log.debug("Error checking file existence for: {}", filePath, ex);
            return false;
        }
    }

    /**
     * Get file size for a given file path
     */
    public long getFileSize(String filePath) {
        try {
            Path resolvedPath;

            if (filePath.startsWith("versions/")) {
                resolvedPath = this.storageLocation.getParent().resolve(filePath).normalize();
            } else {
                resolvedPath = findFileInVersionDirectories(filePath);
                if (resolvedPath == null) {
                    resolvedPath = this.storageLocation.resolve(filePath).normalize();
                }
            }

            if (resolvedPath != null && Files.exists(resolvedPath) && isPathSecure(resolvedPath)) {
                return Files.size(resolvedPath);
            }

            throw new FileUploadException("File not found: " + filePath);

        } catch (IOException ex) {
            throw new FileUploadException("Failed to get file size: " + filePath, ex);
        }
    }



    /**
     * Validate uploaded file - Only JAR files are allowed with comprehensive validation
     */
    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw FileUploadException.emptyFile(file.getOriginalFilename());
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw FileUploadException.fileTooLarge(file.getOriginalFilename(), file.getSize(), maxFileSize);
        }

        // Validate file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName == null || fileName.trim().isEmpty()) {
            throw FileUploadException.invalidFileName("File name cannot be empty");
        }

        // Check for path traversal attempts in filename
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw FileUploadException.invalidFileName("File name contains invalid path characters");
        }

        // Check file extension - Only JAR files allowed (case-insensitive)
        String fileExtension = getFileExtension(fileName);
        if (!"jar".equalsIgnoreCase(fileExtension)) {
            throw FileUploadException.invalidFileType(fileName, "Only JAR files are allowed for application updates");
        }

        // Validate MIME type for additional security (stricter validation)
        validateJarMimeType(file, fileName);

        // Perform comprehensive JAR structure validation
        validateJarStructure(file, fileName);
    }

    /**
     * Validate MIME type specifically for JAR files (stricter validation)
     */
    private void validateJarMimeType(MultipartFile file, String fileName) {
        String contentType = file.getContentType();

        // List of acceptable MIME types for JAR files (more restrictive)
        List<String> acceptableMimeTypes = Arrays.asList(
            "application/java-archive",
            "application/x-java-archive",
            "application/zip",  // JAR files are ZIP archives
            "application/octet-stream"  // Generic binary type
        );

        // If content type is provided, validate it
        if (contentType != null && !contentType.trim().isEmpty()) {
            boolean isValidMimeType = acceptableMimeTypes.stream()
                .anyMatch(type -> type.equalsIgnoreCase(contentType.trim()));

            if (!isValidMimeType) {
                log.warn("Invalid MIME type for JAR file: {} ({})", fileName, contentType);
                throw FileUploadException.invalidMimeType(fileName, contentType, "application/java-archive or application/x-java-archive");
            }
        } else {
            log.warn("No MIME type provided for file: {}", fileName);
            // Continue validation but log the warning
        }
    }

    /**
     * Comprehensive JAR structure validation
     */
    private void validateJarStructure(MultipartFile file, String fileName) {
        try {
            // First, validate ZIP magic bytes
            validateZipMagicBytes(file, fileName);

            // Validate JAR structure using ZipInputStream
            validateJarContents(file, fileName);

        } catch (IOException e) {
            log.error("Failed to validate JAR structure for file: {}", fileName, e);
            throw FileUploadException.jarValidationFailed(fileName, "Unable to read file contents for validation");
        }
    }

    /**
     * Validate ZIP magic bytes (JAR files are ZIP archives)
     */
    private void validateZipMagicBytes(MultipartFile file, String fileName) throws IOException {
        byte[] header = new byte[4];
        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.read(header);

            if (bytesRead < 4) {
                throw FileUploadException.invalidFileStructure(fileName, "File is too small to be a valid JAR archive");
            }

            // ZIP magic bytes: 0x50 0x4B (PK) followed by version info
            // Common signatures: PK\003\004 (local file), PK\001\002 (central dir), PK\005\006 (end central dir)
            if (!(header[0] == 0x50 && header[1] == 0x4B)) {
                throw FileUploadException.invalidFileStructure(fileName, "File does not have valid ZIP/JAR magic bytes");
            }

            // Check for valid ZIP signature combinations
            boolean validSignature = (header[2] == 0x03 && header[3] == 0x04) ||  // Local file header
                                   (header[2] == 0x01 && header[3] == 0x02) ||  // Central directory
                                   (header[2] == 0x05 && header[3] == 0x06) ||  // End of central directory
                                   (header[2] == 0x07 && header[3] == 0x08);    // Data descriptor

            if (!validSignature) {
                throw FileUploadException.invalidFileStructure(fileName, "Invalid ZIP signature in JAR file");
            }
        }
    }

    /**
     * Validate JAR contents and structure
     */
    private void validateJarContents(MultipartFile file, String fileName) throws IOException {
        boolean hasManifest = false;
        boolean hasClassFiles = false;
        int entryCount = 0;
        Set<String> suspiciousPatterns = new HashSet<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null && entryCount < 10000) { // Limit entries to prevent DoS
                entryCount++;
                String entryName = entry.getName();

                // Check for manifest file
                if ("META-INF/MANIFEST.MF".equals(entryName)) {
                    hasManifest = true;
                    validateManifestEntry(zipInputStream, fileName);
                }

                // Check for class files (indicates it's a Java application)
                if (entryName.endsWith(".class")) {
                    hasClassFiles = true;
                }

                // Security checks for suspicious patterns
                validateEntryName(entryName, fileName, suspiciousPatterns);

                // Validate entry size to prevent zip bombs
                if (entry.getSize() > maxFileSize) {
                    throw FileUploadException.suspiciousJarContent(fileName,
                        "Entry '" + entryName + "' is suspiciously large (" + entry.getSize() + " bytes)");
                }

                zipInputStream.closeEntry();
            }

            // Validate that we found essential JAR components
            if (!hasManifest) {
                log.warn("JAR file {} does not contain MANIFEST.MF", fileName);
                // Don't fail for missing manifest as some JARs might not have it
            }

            if (entryCount == 0) {
                throw FileUploadException.invalidFileStructure(fileName, "JAR file appears to be empty");
            }

            if (entryCount >= 10000) {
                throw FileUploadException.suspiciousJarContent(fileName, "JAR file contains too many entries (potential zip bomb)");
            }

            // Log suspicious patterns found
            if (!suspiciousPatterns.isEmpty()) {
                log.warn("Suspicious patterns found in JAR file {}: {}", fileName, suspiciousPatterns);
            }

        } catch (IOException e) {
            throw FileUploadException.corruptedJarFile(fileName, "Unable to read JAR contents: " + e.getMessage());
        }
    }

    /**
     * Validate manifest entry content
     */
    private void validateManifestEntry(ZipInputStream zipInputStream, String fileName) throws IOException {
        try {
            // Read manifest content (limit to prevent DoS)
            byte[] manifestBytes = new byte[65536]; // 64KB limit for manifest
            int totalBytesRead = 0;
            int bytesRead;

            while ((bytesRead = zipInputStream.read(manifestBytes, totalBytesRead,
                   manifestBytes.length - totalBytesRead)) != -1 && totalBytesRead < manifestBytes.length) {
                totalBytesRead += bytesRead;
            }

            if (totalBytesRead >= manifestBytes.length) {
                throw FileUploadException.invalidJarManifest(fileName, "Manifest file is too large");
            }

            String manifestContent = new String(manifestBytes, 0, totalBytesRead, StandardCharsets.UTF_8);

            // Basic manifest validation
            if (!manifestContent.startsWith("Manifest-Version:")) {
                throw FileUploadException.invalidJarManifest(fileName, "Invalid manifest format - missing Manifest-Version");
            }

            // Check for suspicious manifest entries
            String[] suspiciousAttributes = {
                "Agent-Class:", "Premain-Class:", "Boot-Class-Path:", "Can-Redefine-Classes:", "Can-Retransform-Classes:"
            };

            for (String suspicious : suspiciousAttributes) {
                if (manifestContent.contains(suspicious)) {
                    log.warn("Potentially suspicious manifest attribute found in {}: {}", fileName, suspicious);
                }
            }

        } catch (IOException e) {
            throw FileUploadException.invalidJarManifest(fileName, "Unable to read manifest content: " + e.getMessage());
        }
    }

    /**
     * Validate individual entry names for security issues
     */
    private void validateEntryName(String entryName, String fileName, Set<String> suspiciousPatterns) {
        // Check for path traversal attempts
        if (entryName.contains("../") || entryName.contains("..\\")) {
            suspiciousPatterns.add("path-traversal");
            throw FileUploadException.suspiciousJarContent(fileName,
                "Entry contains path traversal sequence: " + entryName);
        }

        // Check for absolute paths
        if (entryName.startsWith("/") || entryName.startsWith("\\") ||
            (entryName.length() > 1 && entryName.charAt(1) == ':')) {
            suspiciousPatterns.add("absolute-path");
            throw FileUploadException.suspiciousJarContent(fileName,
                "Entry contains absolute path: " + entryName);
        }

        // Check for suspicious file types
        String lowerEntryName = entryName.toLowerCase();
        if (lowerEntryName.endsWith(".exe") || lowerEntryName.endsWith(".dll") ||
            lowerEntryName.endsWith(".so") || lowerEntryName.endsWith(".dylib") ||
            lowerEntryName.endsWith(".bat") || lowerEntryName.endsWith(".sh") ||
            lowerEntryName.endsWith(".cmd") || lowerEntryName.endsWith(".ps1")) {
            suspiciousPatterns.add("executable-files");
            log.warn("Suspicious executable file found in JAR {}: {}", fileName, entryName);
        }

        // Check for overly long entry names (potential DoS)
        if (entryName.length() > 1024) {
            suspiciousPatterns.add("long-filename");
            throw FileUploadException.suspiciousJarContent(fileName,
                "Entry name is too long: " + entryName.substring(0, 100) + "...");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Get storage location path
     */
    public Path getStorageLocation() {
        return storageLocation;
    }

    /**
     * Get max file size
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Get allowed extensions
     */
    public List<String> getAllowedExtensions() {
        return allowedExtensionsList;
    }

    // ==================== NEW HELPER METHODS FOR VERSIONED STORAGE ====================

    /**
     * Create version-specific directory
     */
    private Path createVersionDirectory(String versionNumber) throws IOException {
        Path versionDir = this.storageLocation.resolve(versionNumber).normalize();
        Files.createDirectories(versionDir);
        log.debug("Created version directory: {}", versionDir);
        return versionDir;
    }

    /**
     * Get relative file path for database storage
     */
    private String getRelativeFilePath(String versionNumber, String fileName) {
        return String.format("versions/%s/%s", versionNumber, fileName);
    }

    /**
     * Validate version number format
     */
    private void validateVersionNumber(String versionNumber) {
        if (versionNumber == null || versionNumber.trim().isEmpty()) {
            throw new FileUploadException("Version number cannot be null or empty");
        }

        // Basic version number validation (semantic versioning pattern)
        if (!versionNumber.matches("^\\d+\\.\\d+\\.\\d+(-[a-zA-Z0-9]+)?$")) {
            throw new FileUploadException("Invalid version number format: " + versionNumber);
        }

        // Check for path traversal attempts
        if (versionNumber.contains("..") || versionNumber.contains("/") || versionNumber.contains("\\")) {
            throw new FileUploadException("Version number contains invalid characters: " + versionNumber);
        }
    }

    /**
     * Calculate checksum of stored file
     */
    private String calculateStoredFileChecksum(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new FileUploadException("Failed to calculate file checksum", ex);
        }
    }

    /**
     * Find file in version directories (for backward compatibility)
     */
    private Path findFileInVersionDirectories(String fileName) {
        try {
            return Files.walk(this.storageLocation, 2)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(fileName))
                .findFirst()
                .orElse(null);
        } catch (IOException ex) {
            log.debug("Error searching for file in version directories: {}", fileName, ex);
            return null;
        }
    }

    /**
     * Security check to ensure path is within allowed directories
     */
    private boolean isPathSecure(Path path) {
        try {
            Path normalizedPath = path.normalize().toAbsolutePath();
            Path allowedRoot = this.storageLocation.getParent().normalize().toAbsolutePath();

            return normalizedPath.startsWith(allowedRoot);
        } catch (Exception ex) {
            log.warn("Security check failed for path: {}", path, ex);
            return false;
        }
    }

    /**
     * Schedule cleanup of orphaned files
     */
    private void scheduleOrphanedFileCleanup() {
        // This would typically be implemented with @Scheduled annotation
        // For now, we'll just log that cleanup is enabled
        log.info("Orphaned file cleanup is enabled");
    }

    /**
     * Support for resumable downloads - get file range
     */
    public Resource getFileRange(String filePath, long start, long end) {
        log.debug("Getting file range for: {} (bytes {}-{})", filePath, start, end);

        if (!enableResumableDownloads) {
            throw new FileUploadException("Resumable downloads are not enabled");
        }

        try {
            Resource fullResource = loadFileAsResource(filePath);
            long fileSize = fullResource.contentLength();

            // Validate range
            if (start < 0 || end >= fileSize || start > end) {
                throw new FileUploadException("Invalid byte range: " + start + "-" + end);
            }

            // For simplicity, return full resource (actual implementation would return partial content)
            // In a production environment, you'd implement a custom Resource that supports ranges
            return fullResource;

        } catch (IOException ex) {
            throw new FileUploadException("Failed to get file range", ex);
        }
    }

    /**
     * Get version directory listing
     */
    public List<String> getVersionDirectories() {
        try {
            return Files.list(this.storageLocation)
                .filter(Files::isDirectory)
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error("Failed to list version directories", ex);
            return Collections.emptyList();
        }
    }

    /**
     * Get files in a specific version directory
     */
    public List<String> getFilesInVersion(String versionNumber) {
        try {
            Path versionDir = this.storageLocation.resolve(versionNumber);
            if (!Files.exists(versionDir)) {
                return Collections.emptyList();
            }

            return Files.list(versionDir)
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error("Failed to list files in version directory: {}", versionNumber, ex);
            return Collections.emptyList();
        }
    }
}
