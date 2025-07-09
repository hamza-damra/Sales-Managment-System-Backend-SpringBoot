package com.hamza.salesmanagementbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Metadata for all report responses providing standardized information
 * about report generation, filtering, and pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMetadata {
    
    private String reportType;
    private String reportName;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private ReportPeriod period;
    private Map<String, Object> appliedFilters;
    private PaginationInfo pagination;
    private Long totalRecords;
    private Long executionTimeMs;
    private String version;
    private Boolean fromCache;
    private LocalDateTime cacheExpiry;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportPeriod {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String description;
        private Integer daysIncluded;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaginationInfo {
        private Integer page;
        private Integer size;
        private Integer totalPages;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private String sortBy;
        private String sortDirection;
    }
}
