package com.hamza.salesmanagementbackend.exception;

/**
 * Exception thrown when file upload operations fail
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public static FileUploadException invalidFileType(String fileName, String allowedTypes) {
        return new FileUploadException(
            "Invalid file type for '" + fileName + "'. Allowed types: " + allowedTypes
        );
    }

    public static FileUploadException fileTooLarge(String fileName, long fileSize, long maxSize) {
        return new FileUploadException(
            "File '" + fileName + "' is too large (" + formatFileSize(fileSize) + 
            "). Maximum allowed size: " + formatFileSize(maxSize)
        );
    }

    public static FileUploadException storageError(String fileName, Throwable cause) {
        return new FileUploadException(
            "Failed to store file '" + fileName + "'", cause
        );
    }

    public static FileUploadException checksumMismatch(String fileName, String expected, String actual) {
        return new FileUploadException(
            "Checksum mismatch for file '" + fileName + "'. Expected: " + expected + ", Actual: " + actual
        );
    }

    public static FileUploadException emptyFile(String fileName) {
        return new FileUploadException("File '" + fileName + "' is empty");
    }

    public static FileUploadException fileNotFound(String fileName) {
        return new FileUploadException("File '" + fileName + "' not found");
    }

    public static FileUploadException directoryCreationFailed(String directory) {
        return new FileUploadException("Failed to create directory: " + directory);
    }

    public static FileUploadException invalidFileName(String message) {
        return new FileUploadException("Invalid file name: " + message);
    }

    public static FileUploadException invalidMimeType(String fileName, String actualMimeType, String expectedMimeType) {
        return new FileUploadException(
            "Invalid MIME type for file '" + fileName + "'. Expected: " + expectedMimeType +
            ", but got: " + actualMimeType + ". Only JAR files are allowed for application updates."
        );
    }

    public static FileUploadException invalidFileStructure(String fileName, String reason) {
        return new FileUploadException(
            "Invalid file structure for '" + fileName + "': " + reason +
            ". Only valid JAR files are allowed for application updates."
        );
    }

    public static FileUploadException invalidJarManifest(String fileName, String reason) {
        return new FileUploadException(
            "Invalid JAR manifest for '" + fileName + "': " + reason +
            ". The JAR file must contain a valid MANIFEST.MF file."
        );
    }

    public static FileUploadException corruptedJarFile(String fileName, String reason) {
        return new FileUploadException(
            "Corrupted JAR file '" + fileName + "': " + reason +
            ". Please ensure the file is a valid, uncorrupted JAR archive."
        );
    }

    public static FileUploadException suspiciousJarContent(String fileName, String reason) {
        return new FileUploadException(
            "Suspicious content detected in JAR file '" + fileName + "': " + reason +
            ". The file may contain potentially harmful content."
        );
    }

    public static FileUploadException jarValidationFailed(String fileName, String reason) {
        return new FileUploadException(
            "JAR validation failed for '" + fileName + "': " + reason +
            ". Please ensure the file is a valid Java application archive."
        );
    }

    public static FileUploadException unsupportedJarVersion(String fileName, String version) {
        return new FileUploadException(
            "Unsupported JAR version for '" + fileName + "': " + version +
            ". Please use a JAR file compatible with the current Java runtime."
        );
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
