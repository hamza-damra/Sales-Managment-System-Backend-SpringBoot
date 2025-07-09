package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @WithMockUser(roles = {"USER"})
    void shouldGenerateLegacyDashboard() throws Exception {
        // Given
        when(reportService.generateDefaultDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/dashboard")
                        .param("days", "30")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_DASHBOARD"))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.quickStats").exists());
    }

    @Test
    @DisplayName("Should generate legacy executive dashboard")
    @WithMockUser(roles = {"EXECUTIVE"})
    void shouldGenerateLegacyExecutiveDashboard() throws Exception {
        // Given
        when(reportService.generateExecutiveDashboard(anyInt()))
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/dashboard/executive")
                        .param("days", "30")
                        .with(csrf()))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_EXECUTIVE_DASHBOARD"));
    }

    @Test
    @DisplayName("Should generate legacy operational dashboard")
    @WithMockUser(roles = {"MANAGER"})
    void shouldGenerateLegacyOperationalDashboard() throws Exception {
        // Given
        when(reportService.generateOperationalDashboard())
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/dashboard/operational")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_OPERATIONAL_DASHBOARD"));
    }

    @Test
    @DisplayName("Should generate legacy real-time KPIs")
    @WithMockUser(roles = {"ADMIN"})
    void shouldGenerateLegacyRealTimeKPIs() throws Exception {
        // Given
        when(reportService.generateRealTimeKPIs())
                .thenReturn(mockDashboardData);

        // When & Then
        mockMvc.perform(get("/api/reports/kpi/real-time")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_REAL_TIME_KPI"));
    }

    @Test
    @DisplayName("Should handle legacy redirect for unknown endpoints")
    @WithMockUser(roles = {"USER"})
    void shouldHandleLegacyRedirect() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reports/unknown-endpoint")
                        .with(csrf()))
                .andExpect(status().isMovedPermanently())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.metadata.reportType").value("LEGACY_REDIRECT"))
                .andExpect(jsonPath("$.data.message").value("This endpoint has been moved to /api/v1/reports/"))
                .andExpect(jsonPath("$.data.newApiBase").value("/api/v1/reports"));
    }

    @Test
    @DisplayName("Should require authentication for dashboard")
    void shouldRequireAuthenticationForDashboard() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require proper role for executive dashboard")
    @WithMockUser(roles = {"USER"})
    void shouldRequireProperRoleForExecutiveDashboard() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reports/dashboard/executive")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
