package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the comprehensive sales report endpoint
 * Tests the complete flow from controller to service to ensure no null values
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class ComprehensiveSalesReportIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testComprehensiveSalesReportEndpoint_ShouldReturnValidResponse() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                .param("startDate", startDateStr)
                .param("endDate", endDateStr)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.metadata").exists())
                
                // Verify main data structure exists
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.dailyBreakdown").exists())
                .andExpect(jsonPath("$.data.topCustomers").exists())
                .andExpect(jsonPath("$.data.topProducts").exists())
                .andExpect(jsonPath("$.data.salesByStatus").exists())
                .andExpect(jsonPath("$.data.trends").exists())
                .andExpect(jsonPath("$.data.paymentAnalysis").exists())
                .andExpect(jsonPath("$.data.regionalAnalysis").exists())
                
                // Verify summary fields are not null (they should have default values even with no data)
                .andExpect(jsonPath("$.data.summary.totalSales").exists())
                .andExpect(jsonPath("$.data.summary.totalRevenue").exists())
                .andExpect(jsonPath("$.data.summary.averageOrderValue").exists())
                .andExpect(jsonPath("$.data.summary.totalDiscounts").exists())
                .andExpect(jsonPath("$.data.summary.totalTax").exists())
                .andExpect(jsonPath("$.data.summary.netRevenue").exists())
                .andExpected(jsonPath("$.data.summary.uniqueCustomers").exists())
                .andExpect(jsonPath("$.data.summary.conversionRate").exists())
                .andExpect(jsonPath("$.data.summary.revenueGrowth").exists())
                .andExpect(jsonPath("$.data.summary.salesGrowth").exists())
                
                // Verify payment analysis structure
                .andExpect(jsonPath("$.data.paymentAnalysis.countByMethod").exists())
                .andExpect(jsonPath("$.data.paymentAnalysis.revenueByMethod").exists())
                .andExpect(jsonPath("$.data.paymentAnalysis.mostPopularMethod").exists())
                .andExpect(jsonPath("$.data.paymentAnalysis.highestRevenueMethod").exists())
                
                // Verify regional analysis structure
                .andExpect(jsonPath("$.data.regionalAnalysis.revenueByRegion").exists())
                .andExpect(jsonPath("$.data.regionalAnalysis.salesByRegion").exists())
                .andExpect(jsonPath("$.data.regionalAnalysis.topPerformingRegion").exists())
                .andExpect(jsonPath("$.data.regionalAnalysis.regionalGrowth").exists())
                
                // Verify metadata is properly populated
                .andExpect(jsonPath("$.metadata.reportType").value("SALES_COMPREHENSIVE"))
                .andExpect(jsonPath("$.metadata.reportName").value("Comprehensive Sales Analytics"))
                .andExpect(jsonPath("$.metadata.generatedAt").exists())
                .andExpect(jsonPath("$.metadata.period").exists())
                .andExpect(jsonPath("$.metadata.period.startDate").exists())
                .andExpect(jsonPath("$.metadata.period.endDate").exists())
                .andExpect(jsonPath("$.metadata.period.daysIncluded").exists())
                .andExpect(jsonPath("$.metadata.totalRecords").exists())
                .andExpect(jsonPath("$.metadata.executionTimeMs").exists())
                .andExpect(jsonPath("$.metadata.version").value("1.0"))
                .andExpect(jsonPath("$.metadata.fromCache").value(false));
    }

    @Test
    @WithMockUser(roles = {"MANAGER"})
    void testComprehensiveSalesReportWithFilters_ShouldReturnValidResponse() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                .param("startDate", startDateStr)
                .param("endDate", endDateStr)
                .param("customerIds", "1,2,3")
                .param("productIds", "1,2")
                .param("categoryIds", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.metadata").exists())
                .andExpect(jsonPath("$.metadata.appliedFilters").exists());
    }

    @Test
    @WithMockUser(roles = {"SALES_ANALYST"})
    void testComprehensiveSalesReportAccessControl_ShouldAllowAccess() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(1);
        
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                .param("startDate", startDateStr)
                .param("endDate", endDateStr)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testComprehensiveSalesReportWithoutAuth_ShouldDenyAccess() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(1);
        
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/v1/reports/sales/comprehensive")
                .param("startDate", startDateStr)
                .param("endDate", endDateStr)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
