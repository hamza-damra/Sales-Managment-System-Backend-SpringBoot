package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite specifically for testing null handling and edge cases in ReportController
 * Focuses on preventing NullPointerException when using Map.of() with null values
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReportController.class)
class ReportControllerNullHandlingTest {

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
    private Map<String, Object> mockReportData;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();
        
        // Setup mock report data
        mockReportData = new HashMap<>();
        mockReportData.put("totalSales", 100);
        mockReportData.put("totalRevenue", BigDecimal.valueOf(50000));
        mockReportData.put("period", "30 days");
    }

    @Test
    @DisplayName("Should handle null warehouseIds in inventory status report")
    void shouldHandleNullWarehouseIdsInInventoryStatus() throws Exception {
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
    @DisplayName("Should handle null categoryIds in inventory turnover report")
    void shouldHandleNullCategoryIdsInInventoryTurnover() throws Exception {
        // Given
        when(reportService.generateInventoryTurnoverReport(anyInt(), isNull()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/products/inventory-turnover")
                        .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_TURNOVER"))
                .andExpect(jsonPath("$.metadata.appliedFilters.months").value(6))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").doesNotExist());
    }

    @Test
    @DisplayName("Should handle null categoryIds in inventory valuation report")
    void shouldHandleNullCategoryIdsInInventoryValuation() throws Exception {
        // Given
        when(reportService.generateInventoryValuationReport(anyString(), isNull()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/inventory/valuation")
                        .param("valuationMethod", "LIFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_VALUATION"))
                .andExpect(jsonPath("$.metadata.appliedFilters.valuationMethod").value("LIFO"))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").doesNotExist());
    }

    @Test
    @DisplayName("Should handle null promotionIds in promotion usage report")
    void shouldHandleNullPromotionIdsInPromotionUsage() throws Exception {
        // Given
        when(reportService.generatePromotionUsageReport(isNull(), anyInt()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/promotions/usage")
                        .param("days", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("PROMOTION_USAGE"))
                .andExpect(jsonPath("$.metadata.appliedFilters.days").value(14))
                .andExpect(jsonPath("$.metadata.appliedFilters.promotionIds").doesNotExist());
    }

    @Test
    @DisplayName("Should handle null values in product performance report")
    void shouldHandleNullValuesInProductPerformance() throws Exception {
        // Given
        when(reportService.generateProductPerformanceReport(any(ReportRequestDTO.class)))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/products/performance")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("PRODUCT_PERFORMANCE"))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").doesNotExist())
                .andExpect(jsonPath("$.metadata.appliedFilters.productIds").doesNotExist());
    }

    @Test
    @DisplayName("Should handle mixed null and non-null values in filters")
    void shouldHandleMixedNullAndNonNullValues() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(anyBoolean(), anyList()))
                .thenReturn(mockReportData);

        // When & Then - Test with includeInactive=false and warehouseIds provided
        mockMvc.perform(get("/api/v1/reports/inventory/status")
                        .param("includeInactive", "false")
                        .param("warehouseIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.appliedFilters.includeInactive").value(false))
                .andExpect(jsonPath("$.metadata.appliedFilters.warehouseIds").isArray());
    }

    @Test
    @DisplayName("Should handle edge case with empty string parameters")
    void shouldHandleEmptyStringParameters() throws Exception {
        // Given
        when(reportService.generateInventoryValuationReport(anyString(), isNull()))
                .thenReturn(mockReportData);

        // When & Then
        mockMvc.perform(get("/api/v1/reports/inventory/valuation")
                        .param("valuationMethod", "AVERAGE")
                        .param("categoryIds", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.appliedFilters.valuationMethod").value("AVERAGE"));
    }

    @Test
    @DisplayName("Should handle all null optional parameters")
    void shouldHandleAllNullOptionalParameters() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(anyBoolean(), isNull()))
                .thenReturn(mockReportData);

        // When & Then - Only required parameters provided
        mockMvc.perform(get("/api/v1/reports/inventory/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_STATUS"))
                .andExpect(jsonPath("$.metadata.appliedFilters.includeInactive").value(false))
                .andExpect(jsonPath("$.metadata.appliedFilters.warehouseIds").doesNotExist());
    }

    @Test
    @DisplayName("Should validate that appliedFilters map is created correctly")
    void shouldValidateAppliedFiltersMapCreation() throws Exception {
        // Given
        when(reportService.generateInventoryTurnoverReport(anyInt(), anyList()))
                .thenReturn(mockReportData);

        // When & Then - Test with both parameters provided
        mockMvc.perform(get("/api/v1/reports/products/inventory-turnover")
                        .param("months", "18")
                        .param("categoryIds", "1,2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.appliedFilters.months").value(18))
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds").isArray())
                .andExpect(jsonPath("$.metadata.appliedFilters.categoryIds.length()").value(3));
    }

    /**
     * This test demonstrates the fix for the original NullPointerException issue.
     * Before the fix, calling Map.of() with null values would throw NPE.
     * After the fix, using createSafeFilterMap() handles null values gracefully.
     */
    @Test
    @DisplayName("Should demonstrate the fix for the original NullPointerException")
    void shouldDemonstrateNullPointerExceptionFix() throws Exception {
        // Given
        when(reportService.generateInventoryStatusReport(eq(false), isNull()))
                .thenReturn(mockReportData);

        // When & Then - This exact scenario was causing the original NPE
        // GET /api/v1/reports/inventory/status?includeInactive=false
        // warehouseIds parameter is null (not provided)
        mockMvc.perform(get(ApplicationConstants.API_V1_REPORTS + "/inventory/status")
                        .param("includeInactive", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("INVENTORY_STATUS"))
                .andExpect(jsonPath("$.metadata.appliedFilters.includeInactive").value(false))
                // The key difference: warehouseIds should not be present in the map
                // instead of causing a NullPointerException
                .andExpect(jsonPath("$.metadata.appliedFilters.warehouseIds").doesNotExist());
    }
}
