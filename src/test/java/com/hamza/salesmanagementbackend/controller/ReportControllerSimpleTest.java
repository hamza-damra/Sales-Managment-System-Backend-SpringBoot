package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.service.ReportService;
import com.hamza.salesmanagementbackend.service.ReportExportService;
import com.hamza.salesmanagementbackend.service.ReportCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified test suite for ReportController without security dependencies
 * Tests core functionality and business logic without authentication/authorization
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReportController.class)
class ReportControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private ReportExportService reportExportService;

    @MockBean
    private ReportCacheService reportCacheService;

    @Autowired
    private ObjectMapper objectMapper;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private SalesReportDTO mockSalesReport;
    private Map<String, Object> mockReportData;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();
        
        // Setup mock sales report
        mockSalesReport = SalesReportDTO.builder()
                .summary(SalesReportDTO.SalesSummary.builder()
                        .totalSales(100L)
                        .totalRevenue(BigDecimal.valueOf(50000))
                        .averageOrderValue(BigDecimal.valueOf(500))
                        .uniqueCustomers(75)
                        .build())
                .build();

        // Setup mock report data
        mockReportData = new HashMap<>();
        mockReportData.put("totalSales", 100);
        mockReportData.put("totalRevenue", BigDecimal.valueOf(50000));
        mockReportData.put("period", "30 days");
    }

    @Test
    @DisplayName("Should generate comprehensive sales report successfully")
    void shouldGenerateComprehensiveSalesReport() throws Exception {
        // Given
        when(reportService.generateComprehensiveSalesReport(any(ReportRequestDTO.class)))
                .thenReturn(mockSalesReport);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.totalSales").value(100))
                .andExpect(jsonPath("$.data.summary.totalRevenue").value(50000))
                .andExpect(jsonPath("$.metadata.reportType").value("SALES_COMPREHENSIVE"));
    }

    @Test
    @DisplayName("Should generate sales summary report")
    void shouldGenerateSalesSummary() throws Exception {
        // Given
        when(reportCacheService.getCachedReport(anyString(), eq(Map.class)))
                .thenReturn(null);
        when(reportService.generateSalesReport(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/sales/summary")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("useCache", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSales").value(100))
                .andExpect(jsonPath("$.metadata.reportType").value("SALES_SUMMARY"));
    }

    @Test
    @DisplayName("Should generate sales trends analysis")
    void shouldGenerateSalesTrends() throws Exception {
        // Given
        when(reportService.generateSalesTrendsAnalysis(anyInt(), anyString()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/sales/trends")
                        .param("months", "12")
                        .param("groupBy", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("SALES_TRENDS"));
    }

    @Test
    @DisplayName("Should generate customer analytics report")
    void shouldGenerateCustomerAnalytics() throws Exception {
        // Given
        when(reportService.generateCustomerAnalyticsReport(anyBoolean(), anyInt()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/customers/analytics")
                        .param("includeInactive", "false")
                        .param("months", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("CUSTOMER_ANALYTICS"));
    }

    @Test
    @DisplayName("Should generate customer lifetime value report with pagination")
    void shouldGenerateCustomerLifetimeValue() throws Exception {
        // Given
        Page<Map<String, Object>> mockPage = new PageImpl<>(
                List.of(mockReportData), 
                PageRequest.of(0, 20), 
                1
        );
        when(reportService.generateCustomerLifetimeValueReport(any()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/customers/lifetime-value")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortBy", "totalValue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.pagination.page").value(0))
                .andExpect(jsonPath("$.metadata.pagination.size").value(20));
    }

    @Test
    @DisplayName("Should generate product performance report")
    void shouldGenerateProductPerformance() throws Exception {
        // Given
        when(reportService.generateProductPerformanceReport(any(ReportRequestDTO.class)))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/products/performance")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("PRODUCT_PERFORMANCE"));
    }

    @Test
    @DisplayName("Should generate inventory status report")
    void shouldGenerateInventoryStatus() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(anyBoolean(), anyList()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/inventory/status")
                        .param("includeInactive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_STATUS"));
    }

    @Test
    @DisplayName("Should generate inventory status report with null warehouseIds")
    void shouldGenerateInventoryStatusWithNullWarehouseIds() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(anyBoolean(), isNull()))
                .thenReturn(mockReportData);

        // When & Then - This should not throw NullPointerException
        mockMvc.perform(get("/api/v1/reports/inventory/status")
                        .param("includeInactive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_STATUS"))
                .andExpect(jsonPath("$.metadata.appliedFilters.includeInactive").value(true))
                .andExpect(jsonPath("$.metadata.appliedFilters.warehouseIds").doesNotExist());
    }

    @Test
    @DisplayName("Should generate inventory status report with specific warehouse IDs")
    void shouldGenerateInventoryStatusWithWarehouseIds() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(anyBoolean(), anyList()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/inventory/status")
                        .param("includeInactive", "false")
                        .param("warehouseIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_STATUS"))
                .andExpect(jsonPath("$.metadata.appliedFilters.includeInactive").value(false))
                .andExpect(jsonPath("$.metadata.appliedFilters.warehouseIds").isArray());
    }

    @Test
    @DisplayName("Should generate real-time KPIs")
    void shouldGetRealTimeKPIs() throws Exception {
        // Given
        when(reportService.generateRealTimeKPIs())
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_V1_REPORTS + "/kpi/real-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("REAL_TIME_KPI"));
    }

    @Test
    @DisplayName("Should validate date range parameters")
    void shouldValidateDateRangeParameters() throws Exception {
        mockMvc.perform(get(ApplicationConstants.API_V1_REPORTS + "/sales/comprehensive")
                        .param("startDate", "invalid-date")
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate pagination parameters")
    void shouldValidatePaginationParameters() throws Exception {
        mockMvc.perform(get("/api/v1/reports/customers/lifetime-value")
                        .param("page", "-1")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should generate default dashboard")
    void shouldGenerateDefaultDashboard() throws Exception {
        // Given
        Map<String, Object> mockDashboard = new HashMap<>();
        mockDashboard.put("summary", Map.of("totalSales", 100, "totalRevenue", 50000.00));
        mockDashboard.put("quickStats", Map.of("todaysSales", 5, "todaysRevenue", 2500.00));

        when(reportService.generateDefaultDashboard(anyInt()))
                .thenReturn(mockDashboard);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/dashboard")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("DEFAULT_DASHBOARD"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.quickStats").exists());
    }

    @Test
    @DisplayName("Should generate inventory turnover report with null categoryIds")
    void shouldGenerateInventoryTurnoverWithNullCategoryIds() throws Exception {
        // Given
        when(reportService.generateInventoryTurnoverReport(anyInt(), isNull()))
                .thenReturn(mockReportData);

        // When & Then - This should not throw NullPointerException
        mockMvc.perform(get("/api/v1/reports/products/inventory-turnover")
                        .param("months", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_TURNOVER"))
                .andExpect(jsonPath("$.metadata.appliedFilters.months").value(12))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").doesNotExist());
    }

    @Test
    @DisplayName("Should generate inventory valuation report with null categoryIds")
    void shouldGenerateInventoryValuationWithNullCategoryIds() throws Exception {
        // Given
        when(reportService.generateInventoryValuationReport(anyString(), isNull()))
                .thenReturn(mockReportData);

        // When & Then - This should not throw NullPointerException
        mockMvc.perform(get("/api/v1/reports/inventory/valuation")
                        .param("valuationMethod", "FIFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_VALUATION"))
                .andExpect(jsonPath("$.metadata.appliedFilters.valuationMethod").value("FIFO"))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").doesNotExist());
    }

    @Test
    @DisplayName("Should generate promotion usage report with null promotionIds")
    void shouldGeneratePromotionUsageWithNullPromotionIds() throws Exception {
        // Given
        when(reportService.generatePromotionUsageReport(isNull(), anyInt()))
                .thenReturn(mockReportData);

        // When & Then - This should not throw NullPointerException
        mockMvc.perform(get("/api/v1/reports/promotions/usage")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("PROMOTION_USAGE"))
                .andExpect(jsonPath("$.metadata.appliedFilters.days").value(30))
                .andExpect(jsonPath("$.metadata.appliedFilters.promotionIds").doesNotExist());
    }
}
