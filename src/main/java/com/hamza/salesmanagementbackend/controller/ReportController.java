package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.report.*;
import com.hamza.salesmanagementbackend.service.ReportService;
import com.hamza.salesmanagementbackend.service.ReportExportService;
import com.hamza.salesmanagementbackend.service.ReportCacheService;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise-level reporting API for comprehensive business analytics
 * Provides detailed reports across all business entities with advanced features
 * including caching, export functionality, and real-time KPIs.
 */
@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;
    private final ReportCacheService reportCacheService;

    // ==================== UTILITY METHODS ====================

    /**
     * Creates a map with null-safe values for report metadata filters.
     * This prevents NullPointerException when using Map.of() with potentially null values.
     *
     * @param keyValuePairs alternating keys and values
     * @return Map with non-null values only
     */
    private Map<String, Object> createSafeFilterMap(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even in number");
        }

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = (String) keyValuePairs[i];
            Object value = keyValuePairs[i + 1];
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    // ==================== SALES REPORTS ====================

    /**
     * Generate comprehensive sales report with detailed analytics including trends,
     * customer insights, and product performance
     *
     * @param request Report request parameters including date range and filters
     * @return Comprehensive sales analytics data
     */
    @GetMapping("/sales/comprehensive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")
    public ResponseEntity<StandardReportResponse<SalesReportDTO>> getComprehensiveSalesReport(
            @Valid @ModelAttribute ReportRequestDTO request) {

        log.info("Generating comprehensive sales report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        SalesReportDTO report = reportService.generateComprehensiveSalesReport(request);
        long executionTime = System.currentTimeMillis() - startTime;

        // Calculate days included in the report
        int daysIncluded = (int) java.time.temporal.ChronoUnit.DAYS.between(
            request.getStartDate().toLocalDate(),
            request.getEndDate().toLocalDate()
        ) + 1;

        // Create applied filters map
        Map<String, Object> appliedFilters = new HashMap<>();
        if (request.getCustomerIds() != null && !request.getCustomerIds().isEmpty()) {
            appliedFilters.put("customerIds", request.getCustomerIds());
        }
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            appliedFilters.put("productIds", request.getProductIds());
        }
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            appliedFilters.put("categoryIds", request.getCategoryIds());
        }
        if (request.getRegions() != null && !request.getRegions().isEmpty()) {
            appliedFilters.put("regions", request.getRegions());
        }
        if (request.getPaymentMethods() != null && !request.getPaymentMethods().isEmpty()) {
            appliedFilters.put("paymentMethods", request.getPaymentMethods());
        }
        if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
            appliedFilters.put("statuses", request.getStatuses());
        }
        if (request.getAmountRange() != null) {
            appliedFilters.put("amountRange", request.getAmountRange());
        }
        if (request.getDiscountRange() != null) {
            appliedFilters.put("discountRange", request.getDiscountRange());
        }
        if (request.getIncludeReturns() != null) {
            appliedFilters.put("includeReturns", request.getIncludeReturns());
        }
        if (request.getIncludePromotions() != null) {
            appliedFilters.put("includePromotions", request.getIncludePromotions());
        }

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("SALES_COMPREHENSIVE")
                .reportName("Comprehensive Sales Analytics")
                .generatedAt(LocalDateTime.now())
                .period(ReportMetadata.ReportPeriod.builder()
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .description("Custom date range")
                        .daysIncluded(daysIncluded)
                        .build())
                .appliedFilters(appliedFilters.isEmpty() ? null : appliedFilters)
                .totalRecords(report.getSummary() != null ? report.getSummary().getTotalSales() : 0L)
                .executionTimeMs(executionTime)
                .version("1.0")
                .fromCache(false)
                .cacheExpiry(null)
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate sales summary report with quick sales overview and key metrics
     * Supports caching for improved performance
     *
     * @param startDate Start date for the report period
     * @param endDate End date for the report period
     * @param useCache Whether to use cached data if available
     * @return Sales summary with key metrics
     */
    @GetMapping("/sales/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getSalesSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "false") Boolean useCache) {

        log.info("Generating sales summary report for period: {} to {}", startDate, endDate);

        String cacheKey = String.format("sales_summary_%s_%s",
                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        Map<String, Object> report = null;
        boolean fromCache = false;

        if (useCache) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedReport = (Map<String, Object>) reportCacheService.getCachedReport(cacheKey, Map.class);
            report = cachedReport;
            if (report != null) {
                fromCache = true;
                log.debug("Retrieved sales summary from cache");
            }
        }

        if (report == null) {
            long startTime = System.currentTimeMillis();
            report = reportService.generateSalesReport(startDate, endDate);
            long executionTime = System.currentTimeMillis() - startTime;

            if (useCache) {
                reportCacheService.cacheReport(cacheKey, report, 30); // Cache for 30 minutes
            }

            report.put("executionTimeMs", executionTime);
        }

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("SALES_SUMMARY")
                .reportName("Sales Summary Report")
                .generatedAt(LocalDateTime.now())
                .fromCache(fromCache)
                .period(ReportMetadata.ReportPeriod.builder()
                        .startDate(startDate)
                        .endDate(endDate)
                        .build())
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate sales trend analysis with historical sales trends and forecasting
     *
     * @param months Number of months to analyze (1-60)
     * @param groupBy Grouping method (DAY/WEEK/MONTH)
     * @return Sales trends analysis with forecasting data
     */
    @GetMapping("/sales/trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('SALES_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getSalesTrends(
            @RequestParam(defaultValue = "12") @Min(1) @Max(60) int months,
            @RequestParam(defaultValue = "MONTH") String groupBy) {

        log.info("Generating sales trends for {} months grouped by {}", months, groupBy);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateSalesTrendsAnalysis(months, groupBy);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("SALES_TRENDS")
                .reportName("Sales Trend Analysis")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("months", months, "groupBy", groupBy))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== CUSTOMER REPORTS ====================

    /**
     * Generate customer analytics report with comprehensive customer behavior analysis and segmentation
     *
     * @param includeInactive Whether to include inactive customers in analysis
     * @param months Number of months to analyze (1-60)
     * @return Customer analytics with behavior insights and segmentation
     */
    @GetMapping("/customers/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getCustomerAnalytics(
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(defaultValue = "12") @Min(1) @Max(60) int months) {

        log.info("Generating customer analytics report for {} months, includeInactive: {}", months, includeInactive);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateCustomerAnalyticsReport(includeInactive, months);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("CUSTOMER_ANALYTICS")
                .reportName("Customer Analytics Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("includeInactive", includeInactive, "months", months))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate customer lifetime value report with analysis and segmentation
     *
     * @param page Page number for pagination
     * @param size Page size for pagination
     * @param sortBy Field to sort by
     * @return Customer lifetime value analysis with pagination
     */
    @GetMapping("/customers/lifetime-value")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER_ANALYST')")
    public ResponseEntity<StandardReportResponse<Page<Map<String, Object>>>> getCustomerLifetimeValue(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "totalValue") String sortBy) {

        log.info("Generating customer lifetime value report - page: {}, size: {}, sortBy: {}", page, size, sortBy);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));

        long startTime = System.currentTimeMillis();
        Page<Map<String, Object>> report = reportService.generateCustomerLifetimeValueReport(pageable);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("CUSTOMER_LIFETIME_VALUE")
                .reportName("Customer Lifetime Value Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .pagination(ReportMetadata.PaginationInfo.builder()
                        .page(page)
                        .size(size)
                        .totalPages(report.getTotalPages())
                        .totalElements(report.getTotalElements())
                        .hasNext(report.hasNext())
                        .hasPrevious(report.hasPrevious())
                        .sortBy(sortBy)
                        .sortDirection("DESC")
                        .build())
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate customer retention analysis with metrics and cohort analysis
     *
     * @param months Number of months to analyze (1-36)
     * @return Customer retention metrics and cohort analysis
     */
    @GetMapping("/customers/retention")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CUSTOMER_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getCustomerRetention(
            @RequestParam(defaultValue = "12") @Min(1) @Max(36) int months) {

        log.info("Generating customer retention analysis for {} months", months);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateCustomerRetentionReport(months);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("CUSTOMER_RETENTION")
                .reportName("Customer Retention Analysis")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("months", months))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== PRODUCT REPORTS ====================

    /**
     * Generate product performance report with detailed sales performance and profitability analysis
     *
     * @param request Report request parameters including date range and filters
     * @return Product performance analysis with profitability metrics
     */
    @GetMapping("/products/performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('PRODUCT_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getProductPerformance(
            @Valid @ModelAttribute ReportRequestDTO request) {

        log.info("Generating product performance report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateProductPerformanceReport(request);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("PRODUCT_PERFORMANCE")
                .reportName("Product Performance Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .period(ReportMetadata.ReportPeriod.builder()
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build())
                .appliedFilters(createSafeFilterMap(
                        "categoryIds", request.getCategoryIds(),
                        "productIds", request.getProductIds()
                ))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate inventory turnover report with analysis and optimization recommendations
     *
     * @param months Number of months to analyze (1-24)
     * @param categoryIds Optional category filter
     * @return Inventory turnover analysis with optimization recommendations
     */
    @GetMapping("/products/inventory-turnover")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('INVENTORY_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getInventoryTurnover(
            @RequestParam(defaultValue = "12") @Min(1) @Max(24) int months,
            @RequestParam(required = false) List<Long> categoryIds) {

        log.info("Generating inventory turnover report for {} months, categories: {}", months, categoryIds);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateInventoryTurnoverReport(months, categoryIds);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("INVENTORY_TURNOVER")
                .reportName("Inventory Turnover Analysis")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("months", months, "categoryIds", categoryIds))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== INVENTORY REPORTS ====================

    /**
     * Generate inventory status report with current levels, stock alerts, and valuation
     *
     * @param includeInactive Whether to include inactive products
     * @param warehouseIds Optional warehouse filter
     * @return Inventory status with alerts and valuation
     */
    @GetMapping("/inventory/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('INVENTORY_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getInventoryStatus(
            @RequestParam(defaultValue = "false") Boolean includeInactive,
            @RequestParam(required = false) List<Long> warehouseIds) {

        log.info("Generating inventory status report, includeInactive: {}, warehouses: {}", includeInactive, warehouseIds);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateInventoryStatusReport(includeInactive, warehouseIds);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("INVENTORY_STATUS")
                .reportName("Inventory Status Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("includeInactive", includeInactive, "warehouseIds", warehouseIds))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate inventory valuation report by cost and market value
     *
     * @param valuationMethod Valuation method (FIFO/LIFO/AVERAGE)
     * @param categoryIds Optional category filter
     * @return Inventory valuation analysis
     */
    @GetMapping("/inventory/valuation")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getInventoryValuation(
            @RequestParam(defaultValue = "FIFO") String valuationMethod,
            @RequestParam(required = false) List<Long> categoryIds) {

        log.info("Generating inventory valuation report using {} method for categories: {}", valuationMethod, categoryIds);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateInventoryValuationReport(valuationMethod, categoryIds);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("INVENTORY_VALUATION")
                .reportName("Inventory Valuation Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("valuationMethod", valuationMethod, "categoryIds", categoryIds))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== PROMOTION REPORTS ====================

    /**
     * Generate promotion effectiveness report with performance analysis and ROI calculation
     *
     * @param request Report request parameters including date range
     * @return Promotion effectiveness analysis with ROI metrics
     */
    @GetMapping("/promotions/effectiveness")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('MARKETING_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getPromotionEffectiveness(
            @Valid @ModelAttribute ReportRequestDTO request) {

        log.info("Generating promotion effectiveness report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generatePromotionEffectivenessReport(request);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("PROMOTION_EFFECTIVENESS")
                .reportName("Promotion Effectiveness Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .period(ReportMetadata.ReportPeriod.builder()
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build())
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    /**
     * Generate promotion usage statistics with detailed usage patterns and customer behavior
     *
     * @param promotionIds Optional promotion filter
     * @param days Number of days to analyze (1-365)
     * @return Promotion usage statistics and customer behavior analysis
     */
    @GetMapping("/promotions/usage")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('MARKETING_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getPromotionUsage(
            @RequestParam(required = false) List<Long> promotionIds,
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Generating promotion usage report for {} days, promotions: {}", days, promotionIds);

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generatePromotionUsageReport(promotionIds, days);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("PROMOTION_USAGE")
                .reportName("Promotion Usage Statistics")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("promotionIds", promotionIds, "days", days))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== FINANCIAL REPORTS ====================

    /**
     * Generate financial revenue report with comprehensive revenue analysis and profit margins
     *
     * @param request Report request parameters including date range
     * @return Financial revenue analysis with profit margin details
     */
    @GetMapping("/financial/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('FINANCIAL_ANALYST')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getFinancialRevenue(
            @Valid @ModelAttribute ReportRequestDTO request) {

        log.info("Generating financial revenue report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        long startTime = System.currentTimeMillis();
        Map<String, Object> report = reportService.generateFinancialRevenueReport(request);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("FINANCIAL_REVENUE")
                .reportName("Financial Revenue Report")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .period(ReportMetadata.ReportPeriod.builder()
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build())
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(report, metadata));
    }

    // ==================== EXPORT FUNCTIONALITY ====================

    /**
     * Export report in specified format (PDF, Excel, or CSV)
     *
     * @param request Report request with export format specification
     * @return Exported report as byte array
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportReport(
            @Valid @RequestBody ReportRequestDTO request) {

        log.info("Exporting report in {} format for period: {} to {}",
                request.getExportFormat(), request.getStartDate(), request.getEndDate());

        try {
            byte[] exportData = reportExportService.exportReport(request);
            String filename = generateExportFilename(request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", filename);

            MediaType mediaType = getMediaTypeForFormat(request.getExportFormat());
            headers.setContentType(mediaType);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(exportData);

        } catch (Exception e) {
            log.error("Error exporting report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Start asynchronous report export for large datasets
     *
     * @param reportType Type of report to export
     * @param request Report request parameters
     * @return Async export task information
     */
    @GetMapping("/export/async/{reportType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> startAsyncExport(
            @PathVariable String reportType,
            @Valid @ModelAttribute ReportRequestDTO request) {

        log.info("Starting async export for report type: {} in format: {}", reportType, request.getExportFormat());

        CompletableFuture<String> exportTask = reportExportService.startAsyncExport(reportType, request);

        Map<String, Object> response = Map.of(
                "taskId", exportTask.toString(),
                "status", "STARTED",
                "estimatedCompletionTime", LocalDateTime.now().plusMinutes(5),
                "downloadUrl", "/api/v1/reports/export/download/" + exportTask.toString()
        );

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("ASYNC_EXPORT")
                .reportName("Async Export Task")
                .generatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.accepted()
                .body(StandardReportResponse.success(response, metadata, "Export task started successfully"));
    }

    // ==================== DASHBOARD & KPI REPORTS ====================

    /**
     * Generate default dashboard with general business metrics and KPIs
     * This serves as the main dashboard endpoint for general users
     *
     * @param days Number of days to analyze (1-365)
     * @return Default dashboard with general business metrics
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getDefaultDashboard(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Generating default dashboard for {} days", days);

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateDefaultDashboard(days);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("DEFAULT_DASHBOARD")
                .reportName("Default Dashboard")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("days", days))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Generate executive dashboard with high-level KPIs and metrics for executive overview
     *
     * @param days Number of days to analyze (1-365)
     * @return Executive dashboard with high-level KPIs
     */
    @GetMapping("/dashboard/executive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getExecutiveDashboard(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Generating executive dashboard for {} days", days);

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateExecutiveDashboard(days);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("EXECUTIVE_DASHBOARD")
                .reportName("Executive Dashboard")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(createSafeFilterMap("days", days))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Generate operational dashboard with operational metrics for day-to-day management
     *
     * @return Operational dashboard with daily management metrics
     */
    @GetMapping("/dashboard/operational")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATIONS')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getOperationalDashboard() {

        log.info("Generating operational dashboard");

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateOperationalDashboard();
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("OPERATIONAL_DASHBOARD")
                .reportName("Operational Dashboard")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Get real-time key performance indicators
     *
     * @return Real-time KPIs and metrics
     */
    @GetMapping("/kpi/real-time")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getRealTimeKPIs() {

        log.info("Fetching real-time KPIs");

        long startTime = System.currentTimeMillis();
        Map<String, Object> kpis = reportService.generateRealTimeKPIs();
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("REAL_TIME_KPI")
                .reportName("Real-time KPIs")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(kpis, metadata));
    }

    // ==================== UTILITY METHODS ====================

    private String generateExportFilename(ReportRequestDTO request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(request.getExportFormat());
        return String.format("report_%s.%s", timestamp, extension);
    }

    private String getFileExtension(String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return "pdf";
            case "EXCEL":
                return "xlsx";
            case "CSV":
                return "csv";
            default:
                return "json";
        }
    }

    private MediaType getMediaTypeForFormat(String format) {
        switch (format.toUpperCase()) {
            case "PDF":
                return MediaType.APPLICATION_PDF;
            case "EXCEL":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "CSV":
                return MediaType.parseMediaType("text/csv");
            default:
                return MediaType.APPLICATION_JSON;
        }
    }
}
