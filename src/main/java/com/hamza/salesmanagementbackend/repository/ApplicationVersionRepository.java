package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.ApplicationVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ApplicationVersion entity
 */
@Repository
public interface ApplicationVersionRepository extends JpaRepository<ApplicationVersion, Long> {

    /**
     * Find application version by version number
     * Uses List to handle potential duplicates and returns the most recent one
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.versionNumber = :versionNumber ORDER BY av.releaseDate DESC, av.id DESC")
    List<ApplicationVersion> findByVersionNumberList(@Param("versionNumber") String versionNumber);

    /**
     * Find application version by version number
     * This method handles potential duplicates by returning the most recent one
     */
    default Optional<ApplicationVersion> findByVersionNumber(String versionNumber) {
        List<ApplicationVersion> versions = findByVersionNumberList(versionNumber);
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }

    /**
     * Find the latest active version ordered by release date
     * Uses Pageable to ensure only one result is returned
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.isActive = true ORDER BY av.releaseDate DESC, av.id DESC")
    List<ApplicationVersion> findLatestActiveVersions(Pageable pageable);

    /**
     * Find the latest active version ordered by release date
     * This method handles multiple results by returning the first one
     */
    default Optional<ApplicationVersion> findLatestActiveVersion() {
        List<ApplicationVersion> versions = findLatestActiveVersions(PageRequest.of(0, 1));
        return versions.isEmpty() ? Optional.empty() : Optional.of(versions.get(0));
    }

    /**
     * Find all active versions ordered by release date descending
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.isActive = true ORDER BY av.releaseDate DESC")
    List<ApplicationVersion> findAllActiveVersionsOrderByReleaseDateDesc();

    /**
     * Find all versions ordered by release date descending
     */
    @Query("SELECT av FROM ApplicationVersion av ORDER BY av.releaseDate DESC")
    Page<ApplicationVersion> findAllOrderByReleaseDateDesc(Pageable pageable);

    /**
     * Find active versions by mandatory status
     */
    List<ApplicationVersion> findByIsActiveAndIsMandatoryOrderByReleaseDateDesc(Boolean isActive, Boolean isMandatory);

    /**
     * Find versions released after a specific date
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.isActive = true AND av.releaseDate > :date ORDER BY av.releaseDate DESC")
    List<ApplicationVersion> findActiveVersionsReleasedAfter(@Param("date") LocalDateTime date);

    /**
     * Check if version number already exists
     */
    boolean existsByVersionNumber(String versionNumber);

    /**
     * Count total active versions
     */
    long countByIsActive(Boolean isActive);

    /**
     * Count mandatory versions
     */
    long countByIsActiveAndIsMandatory(Boolean isActive, Boolean isMandatory);

    /**
     * Find versions by file name pattern
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.fileName LIKE %:pattern% ORDER BY av.releaseDate DESC")
    List<ApplicationVersion> findByFileNameContaining(@Param("pattern") String pattern);

    /**
     * Find versions created by specific user
     */
    List<ApplicationVersion> findByCreatedByOrderByReleaseDateDesc(String createdBy);

    /**
     * Find versions within a date range
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.releaseDate BETWEEN :startDate AND :endDate ORDER BY av.releaseDate DESC")
    List<ApplicationVersion> findVersionsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Find the most recent version before a specific version
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.isActive = true AND av.releaseDate < " +
           "(SELECT av2.releaseDate FROM ApplicationVersion av2 WHERE av2.versionNumber = :versionNumber) " +
           "ORDER BY av.releaseDate DESC")
    Optional<ApplicationVersion> findPreviousVersion(@Param("versionNumber") String versionNumber);

    /**
     * Find versions with file size greater than specified size
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.fileSize > :minSize ORDER BY av.fileSize DESC")
    List<ApplicationVersion> findVersionsWithFileSizeGreaterThan(@Param("minSize") Long minSize);

    /**
     * Get version statistics
     */
    @Query("SELECT COUNT(av) as totalVersions, " +
           "SUM(CASE WHEN av.isActive = true THEN 1 ELSE 0 END) as activeVersions, " +
           "SUM(CASE WHEN av.isMandatory = true THEN 1 ELSE 0 END) as mandatoryVersions, " +
           "AVG(av.fileSize) as averageFileSize " +
           "FROM ApplicationVersion av")
    Object[] getVersionStatistics();

    /**
     * Find versions that need cleanup (inactive and old)
     */
    @Query("SELECT av FROM ApplicationVersion av WHERE av.isActive = false AND av.updatedAt < :cutoffDate")
    List<ApplicationVersion> findVersionsForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
}
