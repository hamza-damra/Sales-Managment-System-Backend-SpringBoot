package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CompatibilityCheckDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for validating system compatibility for updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UpdateCompatibilityService {

    private final ApplicationVersionRepository versionRepository;

    /**
     * Check compatibility for a specific version
     */
    public CompatibilityCheckDTO checkCompatibility(String targetVersion, String clientVersion, 
                                                   Map<String, String> systemInfo) {
        log.debug("Checking compatibility for target version: {} from client version: {}", 
                 targetVersion, clientVersion);

        ApplicationVersion version = versionRepository.findByVersionNumber(targetVersion)
            .orElse(null);

        if (version == null) {
            return createIncompatibleResponse(targetVersion, clientVersion, 
                "Target version not found", CompatibilityCheckDTO.WarningLevel.CRITICAL);
        }

        List<CompatibilityCheckDTO.CompatibilityIssue> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // Check minimum client version requirement
        boolean versionCompatible = checkVersionCompatibility(version, clientVersion, issues, recommendations);
        
        // Check Java version
        CompatibilityCheckDTO.JavaVersionInfo javaInfo = checkJavaCompatibility(systemInfo, issues, recommendations);
        
        // Check operating system
        CompatibilityCheckDTO.OperatingSystemInfo osInfo = checkOperatingSystemCompatibility(systemInfo, issues, recommendations);
        
        // Check system requirements
        CompatibilityCheckDTO.SystemRequirements sysReq = checkSystemRequirements(systemInfo, issues, recommendations);
        
        // Determine overall compatibility
        boolean isCompatible = versionCompatible && 
                              (javaInfo.getIsCompatible() != null ? javaInfo.getIsCompatible() : true) &&
                              (osInfo.getIsSupported() != null ? osInfo.getIsSupported() : true);
        
        // Determine warning level
        CompatibilityCheckDTO.WarningLevel warningLevel = determineWarningLevel(issues);
        
        // Can proceed if compatible or only has warnings/info issues
        boolean canProceed = isCompatible || warningLevel == CompatibilityCheckDTO.WarningLevel.NONE ||
                           warningLevel == CompatibilityCheckDTO.WarningLevel.LOW;

        return CompatibilityCheckDTO.builder()
            .isCompatible(isCompatible)
            .targetVersion(targetVersion)
            .clientVersion(clientVersion)
            .minimumRequiredVersion(version.getMinimumClientVersion())
            .javaVersion(javaInfo)
            .operatingSystem(osInfo)
            .systemRequirements(sysReq)
            .compatibilityIssues(issues)
            .recommendations(recommendations)
            .canProceed(canProceed)
            .warningLevel(warningLevel)
            .build();
    }

    /**
     * Check version compatibility
     */
    private boolean checkVersionCompatibility(ApplicationVersion targetVersion, String clientVersion,
                                            List<CompatibilityCheckDTO.CompatibilityIssue> issues,
                                            List<String> recommendations) {
        
        String minimumRequired = targetVersion.getMinimumClientVersion();
        if (minimumRequired == null || minimumRequired.trim().isEmpty()) {
            return true; // No minimum version requirement
        }

        try {
            if (!isVersionGreaterOrEqual(clientVersion, minimumRequired)) {
                issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                    .type(CompatibilityCheckDTO.IssueType.JAVA_VERSION)
                    .severity(CompatibilityCheckDTO.IssueSeverity.CRITICAL)
                    .description(String.format("Client version %s is below minimum required version %s", 
                               clientVersion, minimumRequired))
                    .resolution("Update to a newer client version before applying this update")
                    .component("Client Version")
                    .build());
                
                recommendations.add("Please update your client to version " + minimumRequired + " or higher");
                return false;
            }
        } catch (Exception e) {
            log.warn("Error comparing versions: {} vs {}", clientVersion, minimumRequired, e);
            issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                .type(CompatibilityCheckDTO.IssueType.CONFIGURATION)
                .severity(CompatibilityCheckDTO.IssueSeverity.WARNING)
                .description("Unable to verify version compatibility")
                .resolution("Manual verification may be required")
                .component("Version Comparison")
                .build());
        }

        return true;
    }

    /**
     * Check Java version compatibility
     */
    private CompatibilityCheckDTO.JavaVersionInfo checkJavaCompatibility(Map<String, String> systemInfo,
                                                                        List<CompatibilityCheckDTO.CompatibilityIssue> issues,
                                                                        List<String> recommendations) {
        
        String detectedJava = systemInfo.get("java.version");
        String requiredJava = "11"; // Minimum Java version for the application
        String vendor = systemInfo.get("java.vendor");
        
        boolean isCompatible = true;
        
        if (detectedJava != null) {
            try {
                int detectedMajor = extractMajorVersion(detectedJava);
                int requiredMajor = Integer.parseInt(requiredJava);
                
                if (detectedMajor < requiredMajor) {
                    isCompatible = false;
                    issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                        .type(CompatibilityCheckDTO.IssueType.JAVA_VERSION)
                        .severity(CompatibilityCheckDTO.IssueSeverity.CRITICAL)
                        .description(String.format("Java %d detected, but Java %d or higher is required", 
                                   detectedMajor, requiredMajor))
                        .resolution("Install Java " + requiredMajor + " or higher")
                        .component("Java Runtime")
                        .build());
                    
                    recommendations.add("Please install Java " + requiredMajor + " or higher");
                }
            } catch (Exception e) {
                log.warn("Error parsing Java version: {}", detectedJava, e);
                issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                    .type(CompatibilityCheckDTO.IssueType.JAVA_VERSION)
                    .severity(CompatibilityCheckDTO.IssueSeverity.WARNING)
                    .description("Unable to verify Java version compatibility")
                    .resolution("Manual verification of Java version may be required")
                    .component("Java Runtime")
                    .build());
            }
        }

        return CompatibilityCheckDTO.JavaVersionInfo.builder()
            .required(requiredJava + "+")
            .detected(detectedJava)
            .vendor(vendor)
            .isCompatible(isCompatible)
            .build();
    }

    /**
     * Check operating system compatibility
     */
    private CompatibilityCheckDTO.OperatingSystemInfo checkOperatingSystemCompatibility(Map<String, String> systemInfo,
                                                                                       List<CompatibilityCheckDTO.CompatibilityIssue> issues,
                                                                                       List<String> recommendations) {
        
        String osName = systemInfo.get("os.name");
        String osVersion = systemInfo.get("os.version");
        String osArch = systemInfo.get("os.arch");
        
        boolean isSupported = true;
        
        // Check for supported operating systems
        if (osName != null) {
            String osLower = osName.toLowerCase();
            if (!osLower.contains("windows") && !osLower.contains("linux") && 
                !osLower.contains("mac") && !osLower.contains("darwin")) {
                
                isSupported = false;
                issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                    .type(CompatibilityCheckDTO.IssueType.OPERATING_SYSTEM)
                    .severity(CompatibilityCheckDTO.IssueSeverity.WARNING)
                    .description("Operating system " + osName + " may not be fully supported")
                    .resolution("Consider using Windows, macOS, or Linux for best compatibility")
                    .component("Operating System")
                    .build());
                
                recommendations.add("This operating system may not be fully supported");
            }
        }

        return CompatibilityCheckDTO.OperatingSystemInfo.builder()
            .name(osName)
            .version(osVersion)
            .architecture(osArch)
            .isSupported(isSupported)
            .build();
    }

    /**
     * Check system requirements
     */
    private CompatibilityCheckDTO.SystemRequirements checkSystemRequirements(Map<String, String> systemInfo,
                                                                            List<CompatibilityCheckDTO.CompatibilityIssue> issues,
                                                                            List<String> recommendations) {
        
        Long minimumMemoryMB = 512L; // 512 MB minimum
        Long minimumDiskSpaceMB = 1024L; // 1 GB minimum
        
        // Parse available memory (if provided)
        Long availableMemoryMB = null;
        String memoryStr = systemInfo.get("available.memory.mb");
        if (memoryStr != null) {
            try {
                availableMemoryMB = Long.parseLong(memoryStr);
                if (availableMemoryMB < minimumMemoryMB) {
                    issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                        .type(CompatibilityCheckDTO.IssueType.MEMORY)
                        .severity(CompatibilityCheckDTO.IssueSeverity.WARNING)
                        .description(String.format("Available memory (%d MB) is below recommended minimum (%d MB)", 
                                   availableMemoryMB, minimumMemoryMB))
                        .resolution("Close other applications or add more RAM")
                        .component("System Memory")
                        .build());
                    
                    recommendations.add("Consider closing other applications to free up memory");
                }
            } catch (NumberFormatException e) {
                log.warn("Error parsing memory value: {}", memoryStr);
            }
        }

        // Parse available disk space (if provided)
        Long availableDiskSpaceMB = null;
        String diskStr = systemInfo.get("available.disk.mb");
        if (diskStr != null) {
            try {
                availableDiskSpaceMB = Long.parseLong(diskStr);
                if (availableDiskSpaceMB < minimumDiskSpaceMB) {
                    issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
                        .type(CompatibilityCheckDTO.IssueType.DISK_SPACE)
                        .severity(CompatibilityCheckDTO.IssueSeverity.CRITICAL)
                        .description(String.format("Available disk space (%d MB) is below minimum required (%d MB)", 
                                   availableDiskSpaceMB, minimumDiskSpaceMB))
                        .resolution("Free up disk space before proceeding")
                        .component("Disk Storage")
                        .build());
                    
                    recommendations.add("Please free up disk space before installing the update");
                }
            } catch (NumberFormatException e) {
                log.warn("Error parsing disk space value: {}", diskStr);
            }
        }

        Map<String, String> additionalRequirements = new HashMap<>();
        additionalRequirements.put("Network Connection", "Required for download");
        additionalRequirements.put("Administrator Rights", "May be required for installation");

        return CompatibilityCheckDTO.SystemRequirements.builder()
            .minimumMemoryMB(minimumMemoryMB)
            .availableMemoryMB(availableMemoryMB)
            .minimumDiskSpaceMB(minimumDiskSpaceMB)
            .availableDiskSpaceMB(availableDiskSpaceMB)
            .additionalRequirements(additionalRequirements)
            .build();
    }

    /**
     * Determine overall warning level based on issues
     */
    private CompatibilityCheckDTO.WarningLevel determineWarningLevel(List<CompatibilityCheckDTO.CompatibilityIssue> issues) {
        if (issues.isEmpty()) {
            return CompatibilityCheckDTO.WarningLevel.NONE;
        }

        boolean hasCritical = issues.stream()
            .anyMatch(issue -> issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.CRITICAL);
        
        if (hasCritical) {
            return CompatibilityCheckDTO.WarningLevel.CRITICAL;
        }

        boolean hasWarning = issues.stream()
            .anyMatch(issue -> issue.getSeverity() == CompatibilityCheckDTO.IssueSeverity.WARNING);
        
        if (hasWarning) {
            return CompatibilityCheckDTO.WarningLevel.MEDIUM;
        }

        return CompatibilityCheckDTO.WarningLevel.LOW;
    }

    /**
     * Create incompatible response
     */
    private CompatibilityCheckDTO createIncompatibleResponse(String targetVersion, String clientVersion, 
                                                           String reason, CompatibilityCheckDTO.WarningLevel warningLevel) {
        List<CompatibilityCheckDTO.CompatibilityIssue> issues = new ArrayList<>();
        issues.add(CompatibilityCheckDTO.CompatibilityIssue.builder()
            .type(CompatibilityCheckDTO.IssueType.CONFIGURATION)
            .severity(CompatibilityCheckDTO.IssueSeverity.CRITICAL)
            .description(reason)
            .resolution("Contact support for assistance")
            .component("Version Validation")
            .build());

        return CompatibilityCheckDTO.builder()
            .isCompatible(false)
            .targetVersion(targetVersion)
            .clientVersion(clientVersion)
            .compatibilityIssues(issues)
            .canProceed(false)
            .warningLevel(warningLevel)
            .build();
    }

    /**
     * Check if version1 is greater than or equal to version2
     */
    private boolean isVersionGreaterOrEqual(String version1, String version2) {
        if (version1 == null || version2 == null) {
            return true;
        }

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");
        
        int maxLength = Math.max(parts1.length, parts2.length);
        
        for (int i = 0; i < maxLength; i++) {
            int v1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int v2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            
            if (v1 > v2) {
                return true;
            } else if (v1 < v2) {
                return false;
            }
        }
        
        return true; // Versions are equal
    }

    /**
     * Extract major version number from Java version string
     */
    private int extractMajorVersion(String javaVersion) {
        if (javaVersion.startsWith("1.")) {
            // Java 8 and earlier format: 1.8.0_xxx
            return Integer.parseInt(javaVersion.split("\\.")[1]);
        } else {
            // Java 9+ format: 11.0.1, 17.0.2, etc.
            return Integer.parseInt(javaVersion.split("\\.")[0]);
        }
    }
}
