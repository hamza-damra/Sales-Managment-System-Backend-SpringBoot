package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.DifferentialUpdateDTO;
import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import com.hamza.salesmanagementbackend.repository.ApplicationVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for generating and managing differential updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DifferentialUpdateService {

    private final ApplicationVersionRepository versionRepository;
    private final FileManagementService fileManagementService;

    @Value("${app.updates.storage-path:./versions}")
    private String storagePath;

    @Value("${app.updates.delta.max-size-mb:100}")
    private long maxDeltaSizeMB;

    @Value("${app.updates.delta.compression-threshold:0.3}")
    private double compressionThreshold; // Only create delta if it's at least 30% smaller

    /**
     * Generate differential update between two versions
     */
    public DifferentialUpdateDTO generateDifferentialUpdate(String fromVersion, String toVersion) {
        log.info("Generating differential update from {} to {}", fromVersion, toVersion);

        try {
            // Get version information
            ApplicationVersion fromVersionEntity = versionRepository.findByVersionNumber(fromVersion)
                .orElseThrow(() -> new IllegalArgumentException("Source version not found: " + fromVersion));
            
            ApplicationVersion toVersionEntity = versionRepository.findByVersionNumber(toVersion)
                .orElseThrow(() -> new IllegalArgumentException("Target version not found: " + toVersion));

            // Get JAR file paths
            Path fromJarPath = getVersionFilePath(fromVersionEntity);
            Path toJarPath = getVersionFilePath(toVersionEntity);

            if (!Files.exists(fromJarPath) || !Files.exists(toJarPath)) {
                return createFallbackResponse(fromVersion, toVersion, "Source or target JAR file not found");
            }

            // Analyze differences between JARs
            JarDifference jarDiff = analyzeJarDifferences(fromJarPath, toJarPath);

            // Check if delta is worth creating
            long deltaSize = estimateDeltaSize(jarDiff);
            long fullSize = Files.size(toJarPath);
            double compressionRatio = 1.0 - ((double) deltaSize / fullSize);

            if (compressionRatio < compressionThreshold || deltaSize > maxDeltaSizeMB * 1024 * 1024) {
                return createFallbackResponse(fromVersion, toVersion, 
                    "Delta update not beneficial (compression ratio: " + String.format("%.2f", compressionRatio) + ")");
            }

            // Generate delta file
            Path deltaPath = generateDeltaFile(jarDiff, fromVersion, toVersion);
            String deltaChecksum = calculateFileChecksum(deltaPath);

            // Create download URLs
            String deltaDownloadUrl = ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DELTA_ENDPOINT + 
                                    "/" + fromVersion + "/" + toVersion;
            String fullDownloadUrl = ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + 
                                   "/" + toVersion;

            return DifferentialUpdateDTO.builder()
                .fromVersion(fromVersion)
                .toVersion(toVersion)
                .deltaAvailable(true)
                .deltaSize(Files.size(deltaPath))
                .fullUpdateSize(fullSize)
                .compressionRatio(compressionRatio)
                .deltaChecksum(deltaChecksum)
                .deltaDownloadUrl(deltaDownloadUrl)
                .fullDownloadUrl(fullDownloadUrl)
                .changedFiles(jarDiff.getChangedFiles())
                .patchInstructions(jarDiff.getPatchInstructions())
                .fallbackToFull(false)
                .estimatedApplyTimeSeconds(estimateApplyTime(jarDiff))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30)) // Delta expires in 30 days
                .build();

        } catch (Exception e) {
            log.error("Error generating differential update from {} to {}", fromVersion, toVersion, e);
            return createFallbackResponse(fromVersion, toVersion, "Error generating delta: " + e.getMessage());
        }
    }

    /**
     * Get delta file as resource for download
     */
    public Resource getDeltaFile(String fromVersion, String toVersion) throws IOException {
        Path deltaPath = getDeltaFilePath(fromVersion, toVersion);
        
        if (!Files.exists(deltaPath)) {
            throw new FileNotFoundException("Delta file not found for " + fromVersion + " to " + toVersion);
        }

        return new UrlResource(deltaPath.toUri());
    }

    /**
     * Analyze differences between two JAR files
     */
    private JarDifference analyzeJarDifferences(Path fromJarPath, Path toJarPath) throws IOException {
        Map<String, String> fromEntries = getJarEntryChecksums(fromJarPath);
        Map<String, String> toEntries = getJarEntryChecksums(toJarPath);

        List<DifferentialUpdateDTO.ChangedFile> changedFiles = new ArrayList<>();
        List<DifferentialUpdateDTO.PatchInstruction> patchInstructions = new ArrayList<>();
        int instructionOrder = 1;

        // Find added and modified files
        for (Map.Entry<String, String> toEntry : toEntries.entrySet()) {
            String entryName = toEntry.getKey();
            String toChecksum = toEntry.getValue();
            String fromChecksum = fromEntries.get(entryName);

            if (fromChecksum == null) {
                // File added
                changedFiles.add(DifferentialUpdateDTO.ChangedFile.builder()
                    .path(entryName)
                    .operation(DifferentialUpdateDTO.FileOperation.ADDED)
                    .newChecksum(toChecksum)
                    .size(getJarEntrySize(toJarPath, entryName))
                    .build());

                patchInstructions.add(DifferentialUpdateDTO.PatchInstruction.builder()
                    .order(instructionOrder++)
                    .operation(DifferentialUpdateDTO.PatchOperation.EXTRACT)
                    .target(entryName)
                    .source("delta.zip:" + entryName)
                    .checksum(toChecksum)
                    .build());

            } else if (!fromChecksum.equals(toChecksum)) {
                // File modified
                changedFiles.add(DifferentialUpdateDTO.ChangedFile.builder()
                    .path(entryName)
                    .operation(DifferentialUpdateDTO.FileOperation.MODIFIED)
                    .oldChecksum(fromChecksum)
                    .newChecksum(toChecksum)
                    .size(getJarEntrySize(toJarPath, entryName))
                    .build());

                patchInstructions.add(DifferentialUpdateDTO.PatchInstruction.builder()
                    .order(instructionOrder++)
                    .operation(DifferentialUpdateDTO.PatchOperation.EXTRACT)
                    .target(entryName)
                    .source("delta.zip:" + entryName)
                    .checksum(toChecksum)
                    .build());
            }
        }

        // Find deleted files
        for (String fromEntry : fromEntries.keySet()) {
            if (!toEntries.containsKey(fromEntry)) {
                changedFiles.add(DifferentialUpdateDTO.ChangedFile.builder()
                    .path(fromEntry)
                    .operation(DifferentialUpdateDTO.FileOperation.DELETED)
                    .oldChecksum(fromEntries.get(fromEntry))
                    .build());

                patchInstructions.add(DifferentialUpdateDTO.PatchInstruction.builder()
                    .order(instructionOrder++)
                    .operation(DifferentialUpdateDTO.PatchOperation.DELETE)
                    .target(fromEntry)
                    .build());
            }
        }

        return new JarDifference(changedFiles, patchInstructions);
    }

    /**
     * Get checksums for all entries in a JAR file
     */
    private Map<String, String> getJarEntryChecksums(Path jarPath) throws IOException {
        Map<String, String> checksums = new HashMap<>();
        
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        String checksum = calculateStreamChecksum(is);
                        checksums.put(entry.getName(), checksum);
                    }
                }
            }
        }
        
        return checksums;
    }

    /**
     * Get size of a specific entry in JAR file
     */
    private Long getJarEntrySize(Path jarPath, String entryName) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryName);
            return entry != null ? entry.getSize() : 0L;
        } catch (IOException e) {
            log.warn("Error getting size for entry {} in {}", entryName, jarPath, e);
            return 0L;
        }
    }

    /**
     * Generate delta file containing only changed entries
     */
    private Path generateDeltaFile(JarDifference jarDiff, String fromVersion, String toVersion) throws IOException {
        Path deltaPath = getDeltaFilePath(fromVersion, toVersion);
        Files.createDirectories(deltaPath.getParent());

        // Get target JAR path
        ApplicationVersion toVersionEntity = versionRepository.findByVersionNumber(toVersion).orElseThrow();
        Path toJarPath = getVersionFilePath(toVersionEntity);

        try (ZipOutputStream deltaZip = new ZipOutputStream(Files.newOutputStream(deltaPath));
             JarFile toJarFile = new JarFile(toJarPath.toFile())) {

            // Add changed/added files to delta
            for (DifferentialUpdateDTO.ChangedFile changedFile : jarDiff.getChangedFiles()) {
                if (changedFile.getOperation() != DifferentialUpdateDTO.FileOperation.DELETED) {
                    JarEntry sourceEntry = toJarFile.getJarEntry(changedFile.getPath());
                    if (sourceEntry != null) {
                        ZipEntry deltaEntry = new ZipEntry(changedFile.getPath());
                        deltaZip.putNextEntry(deltaEntry);

                        try (InputStream sourceStream = toJarFile.getInputStream(sourceEntry)) {
                            sourceStream.transferTo(deltaZip);
                        }

                        deltaZip.closeEntry();
                    }
                }
            }

            // Add patch instructions as metadata
            ZipEntry instructionsEntry = new ZipEntry("META-INF/patch-instructions.json");
            deltaZip.putNextEntry(instructionsEntry);
            
            String instructionsJson = createPatchInstructionsJson(jarDiff.getPatchInstructions());
            deltaZip.write(instructionsJson.getBytes());
            deltaZip.closeEntry();
        }

        return deltaPath;
    }

    /**
     * Create JSON representation of patch instructions
     */
    private String createPatchInstructionsJson(List<DifferentialUpdateDTO.PatchInstruction> instructions) {
        StringBuilder json = new StringBuilder();
        json.append("{\"instructions\":[");
        
        for (int i = 0; i < instructions.size(); i++) {
            if (i > 0) json.append(",");
            DifferentialUpdateDTO.PatchInstruction instruction = instructions.get(i);
            json.append("{")
                .append("\"order\":").append(instruction.getOrder()).append(",")
                .append("\"operation\":\"").append(instruction.getOperation()).append("\",")
                .append("\"target\":\"").append(instruction.getTarget()).append("\"");
            
            if (instruction.getSource() != null) {
                json.append(",\"source\":\"").append(instruction.getSource()).append("\"");
            }
            if (instruction.getChecksum() != null) {
                json.append(",\"checksum\":\"").append(instruction.getChecksum()).append("\"");
            }
            
            json.append("}");
        }
        
        json.append("]}");
        return json.toString();
    }

    /**
     * Estimate delta file size
     */
    private long estimateDeltaSize(JarDifference jarDiff) {
        return jarDiff.getChangedFiles().stream()
            .filter(f -> f.getOperation() != DifferentialUpdateDTO.FileOperation.DELETED)
            .mapToLong(f -> f.getSize() != null ? f.getSize() : 0L)
            .sum() + 1024; // Add overhead for metadata
    }

    /**
     * Estimate time to apply delta in seconds
     */
    private Integer estimateApplyTime(JarDifference jarDiff) {
        // Rough estimate: 1 second per 10 changed files + base time
        int changedFileCount = jarDiff.getChangedFiles().size();
        return Math.max(5, changedFileCount / 10 + 2);
    }

    /**
     * Create fallback response when delta is not available
     */
    private DifferentialUpdateDTO createFallbackResponse(String fromVersion, String toVersion, String reason) {
        ApplicationVersion toVersionEntity = versionRepository.findByVersionNumber(toVersion).orElse(null);
        Long fullSize = null;
        
        if (toVersionEntity != null) {
            fullSize = toVersionEntity.getFileSize();
        }

        String fullDownloadUrl = ApplicationConstants.API_V1_UPDATES + ApplicationConstants.DOWNLOAD_ENDPOINT + 
                               "/" + toVersion;

        return DifferentialUpdateDTO.builder()
            .fromVersion(fromVersion)
            .toVersion(toVersion)
            .deltaAvailable(false)
            .fullUpdateSize(fullSize)
            .fullDownloadUrl(fullDownloadUrl)
            .fallbackToFull(true)
            .fallbackReason(reason)
            .createdAt(LocalDateTime.now())
            .build();
    }

    /**
     * Get file path for a version
     */
    private Path getVersionFilePath(ApplicationVersion version) {
        return Paths.get(storagePath, version.getVersionNumber(), version.getFileName());
    }

    /**
     * Get delta file path
     */
    private Path getDeltaFilePath(String fromVersion, String toVersion) {
        return Paths.get(storagePath, "deltas", fromVersion + "_to_" + toVersion + ".delta");
    }

    /**
     * Calculate file checksum
     */
    private String calculateFileChecksum(Path filePath) throws IOException {
        try (InputStream is = Files.newInputStream(filePath)) {
            return calculateStreamChecksum(is);
        }
    }

    /**
     * Calculate stream checksum
     */
    private String calculateStreamChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
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
            
            return hexString.toString();
        } catch (Exception e) {
            throw new IOException("Error calculating checksum", e);
        }
    }

    /**
     * Inner class to hold JAR difference analysis results
     */
    private static class JarDifference {
        private final List<DifferentialUpdateDTO.ChangedFile> changedFiles;
        private final List<DifferentialUpdateDTO.PatchInstruction> patchInstructions;

        public JarDifference(List<DifferentialUpdateDTO.ChangedFile> changedFiles,
                           List<DifferentialUpdateDTO.PatchInstruction> patchInstructions) {
            this.changedFiles = changedFiles;
            this.patchInstructions = patchInstructions;
        }

        public List<DifferentialUpdateDTO.ChangedFile> getChangedFiles() {
            return changedFiles;
        }

        public List<DifferentialUpdateDTO.PatchInstruction> getPatchInstructions() {
            return patchInstructions;
        }
    }
}
