package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simplified test suite for LegacyReportController without security dependencies
 * Tests core functionality and business logic without authentication/authorization
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(LegacyReportController.class)
@DisplayName("Legacy Report Controller Tests")
class LegacyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private Map<String, Object> mockDashboardData;

    @BeforeEach
    void setUp() {
        mockDashboardData = new HashMap<>();
        mockDashboardData.put("summary", Map.of(
                "totalSales", 100,
                "totalRevenue", 50000.00
        ));
        mockDashboardData.put("quickStats", Map.of(
                "todaysSales", 5,
                "todaysRevenue", 2500.00
        ));
    }

    @Test
    @DisplayName("Should generate legacy dashboard")
    void shouldGenerateLegacyDashboard() throws Exception {
        // Given
        when(reportService.generateDefaultDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_REPORTS + "/dashboard")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_DASHBOARD"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.quickStats").exists());
    }

    @Test
    @DisplayName("Should generate legacy executive dashboard")
    void shouldGenerateLegacyExecutiveDashboard() throws Exception {
        // Given
        when(reportService.generateExecutiveDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/dashboard/executive")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_EXECUTIVE_DASHBOARD"));
    }

    @Test
    @DisplayName("Should generate legacy operational dashboard")
    void shouldGenerateLegacyOperationalDashboard() throws Exception {
        // Given
        when(reportService.generateOperationalDashboard())
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/dashboard/operational"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_OPERATIONAL_DASHBOARD"));
    }

    @Test
    @DisplayName("Should generate legacy real-time KPIs")
    void shouldGenerateLegacyRealTimeKPIs() throws Exception {
        // Given
        when(reportService.generateRealTimeKPIs())
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_REPORTS + "/kpi/real-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_REAL_TIME_KPI"));
    }

    @Test
    @DisplayName("Should handle legacy redirect for unknown endpoints")
    void shouldHandleLegacyRedirect() throws Exception {
        // When & Then
        mockMvc.perform(get(ApplicationConstants.API_REPORTS + "/unknown-endpoint"))
                .andExpect(status().isMovedPermanently())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_REDIRECT"))
                .andExpect(jsonPath("$.data.message").value("This endpoint has been moved to " + ApplicationConstants.API_V1_REPORTS + "/"))
                .andExpect(jsonPath("$.data.newApiBase").value(ApplicationConstants.API_V1_REPORTS));
    }

    @Test
    @DisplayName("Should handle dashboard request without authentication")
    void shouldHandleDashboardRequestWithoutAuthentication() throws Exception {
        // Given
        when(reportService.generateDefaultDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then - Without security, this should work
        mockMvc.perform(get(ApplicationConstants.API_REPORTS + "/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle executive dashboard request")
    void shouldHandleExecutiveDashboardRequest() throws Exception {
        // Given
        when(reportService.generateExecutiveDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then - Without security, this should work
        mockMvc.perform(get(ApplicationConstants.API_REPORTS + "/dashboard/executive"))
                .andExpect(status().isOk());
    }
}
