package com.hamza.salesmanagementbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Update System Configuration Properties
 * Configures the application update system including file handling, security, and WebSocket settings
 */
@Configuration
@ConfigurationProperties(prefix = "app.updates")
@Data
public class UpdateProperties {

    /**
     * Directory path for storing update files.
     * Default is "./versions" for local development.
     */
    private String storagePath = "./versions";

    /**
     * Maximum allowed file size for updates in bytes.
     * Default is 500MB (524288000 bytes).
     */
    private long maxFileSize = 524288000L; // 500MB

    /**
     * Comma-separated list of allowed file extensions for updates.
     * Default is "jar" for Java application updates.
     */
    private String allowedExtensions = "jar";

    /**
     * Enable resumable download functionality for update files.
     * Allows clients to resume interrupted downloads.
     */
    private boolean enableResumableDownloads = true;

    /**
     * Enable automatic cleanup of orphaned update files.
     * Removes files that are no longer referenced.
     */
    private boolean cleanupOrphanedFiles = true;
    
    private WebSocket websocket = new WebSocket();
    private Security security = new Security();
    private JarValidation jarValidation = new JarValidation();

    /**
     * WebSocket configuration for real-time update notifications
     */
    @Data
    public static class WebSocket {
        /**
         * WebSocket heartbeat interval in milliseconds.
         * Default is 30 seconds (30000 ms).
         */
        private long heartbeatInterval = 30000L;

        /**
         * WebSocket connection timeout in milliseconds.
         * Default is 5 minutes (300000 ms).
         */
        private long connectionTimeout = 300000L;
    }

    /**
     * Security configuration for update operations
     */
    @Data
    public static class Security {
        /**
         * Required admin role for update operations.
         * Default is "ADMIN".
         */
        private String adminRole = "ADMIN";

        /**
         * Rate limit for update operations per user.
         * Default is 10 operations per time window.
         */
        private int rateLimit = 10;
    }

    /**
     * JAR file validation configuration
     */
    @Data
    public static class JarValidation {
        /**
         * Enable strict MIME type validation for JAR files.
         * Ensures uploaded files have correct MIME type.
         */
        private boolean strictMimeType = true;

        /**
         * Require manifest file in JAR uploads.
         * Default is false to allow simple JAR files.
         */
        private boolean requireManifest = false;

        /**
         * Maximum number of entries allowed in JAR files.
         * Prevents zip bombs and oversized archives.
         */
        private int maxEntries = 10000;

        /**
         * Maximum manifest file size in bytes.
         * Default is 64KB (65536 bytes).
         */
        private int maxManifestSize = 65536;
    }
}
