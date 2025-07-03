package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private Map<String, Object> salesReport;
    private Map<String, Object> customerReport;
    private Map<String, Object> inventoryReport;
    private Map<String, Object> revenueTrends;

    @BeforeEach
    void setUp() {
        // Setup sales report mock data
        salesReport = new HashMap<>();
        salesReport.put("period", Map.of("startDate", "2024-01-01", "endDate", "2024-12-31"));
        salesReport.put("summary", Map.of("totalRevenue", BigDecimal.valueOf(50000.00), "totalSales", 150L));

        // Setup customer report mock data
        customerReport = new HashMap<>();
        customerReport.put("totalCustomers", 100L);
        customerReport.put("activeCustomers", 80L);
        customerReport.put("customerRetentionRate", 80.0);

        // Setup inventory report mock data
        inventoryReport = new HashMap<>();
        inventoryReport.put("totalProducts", 200);
        inventoryReport.put("totalInventoryValue", BigDecimal.valueOf(100000.00));
        inventoryReport.put("stockLevels", Map.of("Low Stock", 5L, "High Stock", 195L));

        // Setup revenue trends mock data
        revenueTrends = new HashMap<>();
        revenueTrends.put("period", "6 months");
        revenueTrends.put("totalRevenue", BigDecimal.valueOf(75000.00));
        revenueTrends.put("totalSales", 200L);
    }

    @Test
    void generateSalesReport_Success() throws Exception {
        // Given
        when(reportService.generateSalesReport(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(salesReport);

        // When & Then
        mockMvc.perform(get("/api/reports/sales")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period.startDate").value("2024-01-01"))
                .andExpect(jsonPath("$.period.endDate").value("2024-12-31"));

        verify(reportService).generateSalesReport(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void generateCustomerReport_Success() throws Exception {
        // Given
        when(reportService.generateCustomerReport()).thenReturn(customerReport);

        // When & Then
        mockMvc.perform(get("/api/reports/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(100))
                .andExpect(jsonPath("$.activeCustomers").value(80))
                .andExpect(jsonPath("$.customerRetentionRate").value(80.0));

        verify(reportService).generateCustomerReport();
    }

    @Test
    void generateInventoryReport_Success() throws Exception {
        // Given
        when(reportService.generateInventoryReport()).thenReturn(inventoryReport);

        // When & Then
        mockMvc.perform(get("/api/reports/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(200))
                .andExpect(jsonPath("$.totalInventoryValue").value(100000.00));

        verify(reportService).generateInventoryReport();
    }

    @Test
    void generateRevenueTrends_Success() throws Exception {
        // Given
        when(reportService.generateRevenueTrends(anyInt())).thenReturn(revenueTrends);

        // When & Then
        mockMvc.perform(get("/api/reports/revenue-trends")
                .param("months", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("6 months"))
                .andExpect(jsonPath("$.totalRevenue").value(75000.00))
                .andExpect(jsonPath("$.totalSales").value(200));

        verify(reportService).generateRevenueTrends(6);
    }

    @Test
    void generateRevenueTrends_DefaultMonths_Success() throws Exception {
        // Given
        when(reportService.generateRevenueTrends(12)).thenReturn(revenueTrends);

        // When & Then
        mockMvc.perform(get("/api/reports/revenue-trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("6 months"));

        verify(reportService).generateRevenueTrends(12);
    }
}
