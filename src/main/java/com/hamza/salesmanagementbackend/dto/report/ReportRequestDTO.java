package com.hamza.salesmanagementbackend.dto.report;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized request DTO for report generation with filtering and pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDTO {
    
    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
    
    @Min(value = 0, message = "Page number must be non-negative")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    @Builder.Default
    private Integer size = 20;
    
    private String sortBy;
    
    @Builder.Default
    private String sortDirection = "DESC";
    
    private List<Long> customerIds;
    private List<Long> productIds;
    private List<Long> categoryIds;
    private List<String> regions;
    private List<String> paymentMethods;
    private List<String> statuses;
    
    private BigDecimalRange amountRange;
    private BigDecimalRange discountRange;
    
    private Boolean includeReturns;
    private Boolean includePromotions;
    private Boolean groupByDay;
    private Boolean groupByWeek;
    private Boolean groupByMonth;
    
    private String exportFormat; // PDF, EXCEL, CSV, JSON
    private Boolean useCache;
    private Map<String, Object> additionalFilters;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BigDecimalRange {
        private java.math.BigDecimal min;
        private java.math.BigDecimal max;
    }
    
    // Validation methods
    public boolean isValidDateRange() {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
    
    public boolean isExportRequest() {
        return exportFormat != null && !exportFormat.trim().isEmpty();
    }
    
    public String getEffectiveSortBy() {
        return sortBy != null ? sortBy : "createdAt";
    }
    
    public String getEffectiveSortDirection() {
        return sortDirection != null ? sortDirection.toUpperCase() : "DESC";
    }
}
