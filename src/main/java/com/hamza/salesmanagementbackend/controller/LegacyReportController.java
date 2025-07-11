package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.report.ReportMetadata;
import com.hamza.salesmanagementbackend.dto.report.StandardReportResponse;
import com.hamza.salesmanagementbackend.service.ReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Legacy Report Controller for backward compatibility
 * Handles requests to /api/reports/* (without /v1)
 * Redirects to the main ReportController functionality
 */
@RestController
@RequestMapping(ApplicationConstants.API_REPORTS)
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Validated
@Slf4j
public class LegacyReportController {

    private final ReportService reportService;

    /**
     * Legacy dashboard endpoint - provides default dashboard
     * Handles requests to /api/reports/dashboard
     *
     * @param days Number of days to analyze (1-365)
     * @return Default dashboard with general business metrics
     */
    @GetMapping(ApplicationConstants.DASHBOARD_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getLegacyDashboard(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Legacy dashboard endpoint accessed - generating default dashboard for {} days", days);

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateDefaultDashboard(days);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("LEGACY_DASHBOARD")
                .reportName("Legacy Dashboard (Default)")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(Map.of("days", days))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Legacy executive dashboard endpoint
     * Handles requests to /api/reports/dashboard/executive
     */
    @GetMapping("/dashboard/executive")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getLegacyExecutiveDashboard(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {

        log.info("Legacy executive dashboard endpoint accessed for {} days", days);

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateExecutiveDashboard(days);
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("LEGACY_EXECUTIVE_DASHBOARD")
                .reportName("Legacy Executive Dashboard")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .appliedFilters(Map.of("days", days))
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Legacy operational dashboard endpoint
     * Handles requests to /api/reports/dashboard/operational
     */
    @GetMapping("/dashboard/operational")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('OPERATIONS')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getLegacyOperationalDashboard() {

        log.info("Legacy operational dashboard endpoint accessed");

        long startTime = System.currentTimeMillis();
        Map<String, Object> dashboard = reportService.generateOperationalDashboard();
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("LEGACY_OPERATIONAL_DASHBOARD")
                .reportName("Legacy Operational Dashboard")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(dashboard, metadata));
    }

    /**
     * Legacy KPI endpoint
     * Handles requests to /api/reports/kpi/real-time
     */
    @GetMapping(ApplicationConstants.KPI_REAL_TIME_ENDPOINT_LEGACY)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> getLegacyRealTimeKPIs() {

        log.info("Legacy real-time KPIs endpoint accessed");

        long startTime = System.currentTimeMillis();
        Map<String, Object> kpis = reportService.generateRealTimeKPIs();
        long executionTime = System.currentTimeMillis() - startTime;

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("LEGACY_REAL_TIME_KPI")
                .reportName("Legacy Real-time KPIs")
                .generatedAt(LocalDateTime.now())
                .executionTimeMs(executionTime)
                .build();

        return ResponseEntity.ok(StandardReportResponse.success(kpis, metadata));
    }

    /**
     * Redirect endpoint to inform clients about the new API version
     */
    @GetMapping("/**")
    public ResponseEntity<StandardReportResponse<Map<String, Object>>> handleLegacyRequests() {
        log.warn("Legacy report endpoint accessed - redirecting to v1 API");

        Map<String, Object> response = Map.of(
                "message", "This endpoint has been moved to " + ApplicationConstants.API_V1_REPORTS + "/",
                "newApiBase", ApplicationConstants.API_V1_REPORTS,
                "availableEndpoints", Map.of(
                        "dashboard", ApplicationConstants.API_V1_DASHBOARD,
                        "executiveDashboard", ApplicationConstants.API_V1_EXECUTIVE_DASHBOARD,
                        "operationalDashboard", ApplicationConstants.API_V1_OPERATIONAL_DASHBOARD,
                        "realTimeKPIs", ApplicationConstants.API_V1_REAL_TIME_KPI
                )
        );

        ReportMetadata metadata = ReportMetadata.builder()
                .reportType("LEGACY_REDIRECT")
                .reportName("Legacy API Redirect")
                .generatedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.status(301) // Moved Permanently
                .body(StandardReportResponse.success(response, metadata,
                        "Please use the new API endpoints under " + ApplicationConstants.API_V1_REPORTS + "/"));
    }
}
