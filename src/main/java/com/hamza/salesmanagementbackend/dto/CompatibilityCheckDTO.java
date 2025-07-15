package com.hamza.salesmanagementbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for system compatibility validation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompatibilityCheckDTO {

    @JsonProperty("isCompatible")
    private Boolean isCompatible;

    @JsonProperty("targetVersion")
    private String targetVersion;

    @JsonProperty("clientVersion")
    private String clientVersion;

    @JsonProperty("minimumRequiredVersion")
    private String minimumRequiredVersion;

    @JsonProperty("javaVersion")
    private JavaVersionInfo javaVersion;

    @JsonProperty("operatingSystem")
    private OperatingSystemInfo operatingSystem;

    @JsonProperty("systemRequirements")
    private SystemRequirements systemRequirements;

    @JsonProperty("compatibilityIssues")
    private List<CompatibilityIssue> compatibilityIssues;

    @JsonProperty("recommendations")
    private List<String> recommendations;

    @JsonProperty("canProceed")
    private Boolean canProceed;

    @JsonProperty("warningLevel")
    private WarningLevel warningLevel;

    /**
     * Java version information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JavaVersionInfo {
        @JsonProperty("required")
        private String required;

        @JsonProperty("detected")
        private String detected;

        @JsonProperty("isCompatible")
        private Boolean isCompatible;

        @JsonProperty("vendor")
        private String vendor;
    }

    /**
     * Operating system information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperatingSystemInfo {
        @JsonProperty("name")
        private String name;

        @JsonProperty("version")
        private String version;

        @JsonProperty("architecture")
        private String architecture;

        @JsonProperty("isSupported")
        private Boolean isSupported;
    }

    /**
     * System requirements
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SystemRequirements {
        @JsonProperty("minimumMemoryMB")
        private Long minimumMemoryMB;

        @JsonProperty("availableMemoryMB")
        private Long availableMemoryMB;

        @JsonProperty("minimumDiskSpaceMB")
        private Long minimumDiskSpaceMB;

        @JsonProperty("availableDiskSpaceMB")
        private Long availableDiskSpaceMB;

        @JsonProperty("additionalRequirements")
        private Map<String, String> additionalRequirements;
    }

    /**
     * Compatibility issue
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompatibilityIssue {
        @JsonProperty("type")
        private IssueType type;

        @JsonProperty("severity")
        private IssueSeverity severity;

        @JsonProperty("description")
        private String description;

        @JsonProperty("resolution")
        private String resolution;

        @JsonProperty("component")
        private String component;
    }

    /**
     * Issue types
     */
    public enum IssueType {
        JAVA_VERSION,
        OPERATING_SYSTEM,
        MEMORY,
        DISK_SPACE,
        ARCHITECTURE,
        DEPENDENCY,
        CONFIGURATION
    }

    /**
     * Issue severity levels
     */
    public enum IssueSeverity {
        CRITICAL,
        WARNING,
        INFO
    }

    /**
     * Warning levels
     */
    public enum WarningLevel {
        NONE,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
