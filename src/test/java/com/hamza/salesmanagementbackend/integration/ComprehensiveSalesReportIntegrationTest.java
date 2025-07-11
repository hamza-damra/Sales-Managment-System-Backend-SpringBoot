package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the comprehensive sales report service
 * Tests the complete flow to ensure no null values and proper functionality
 */
@SpringBootTest
@ActiveProfiles("test")
class ComprehensiveSalesReportIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Test
    void testComprehensiveSalesReportService_ShouldReturnValidResponse() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);

        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            SalesReportDTO report = reportService.generateComprehensiveSalesReport(request);

            // Verify main data structure exists
            assertNotNull(report, "Report should not be null");
            assertNotNull(report.getSummary(), "Summary should not be null");
            assertNotNull(report.getDailyBreakdown(), "Daily breakdown should not be null");
            assertNotNull(report.getTopCustomers(), "Top customers should not be null");
            assertNotNull(report.getTopProducts(), "Top products should not be null");
            assertNotNull(report.getSalesByStatus(), "Sales by status should not be null");
            assertNotNull(report.getTrends(), "Trends should not be null");
            assertNotNull(report.getPaymentAnalysis(), "Payment analysis should not be null");
            assertNotNull(report.getRegionalAnalysis(), "Regional analysis should not be null");

            // Verify summary fields are not null (they should have default values even with no data)
            SalesReportDTO.SalesSummary summary = report.getSummary();
            assertNotNull(summary.getTotalSales(), "Total sales should not be null");
            assertNotNull(summary.getTotalRevenue(), "Total revenue should not be null");
            assertNotNull(summary.getAverageOrderValue(), "Average order value should not be null");
            assertNotNull(summary.getTotalDiscounts(), "Total discounts should not be null");
            assertNotNull(summary.getTotalTax(), "Total tax should not be null");
            assertNotNull(summary.getNetRevenue(), "Net revenue should not be null");
            assertNotNull(summary.getUniqueCustomers(), "Unique customers should not be null");
            assertNotNull(summary.getConversionRate(), "Conversion rate should not be null");
            assertNotNull(summary.getRevenueGrowth(), "Revenue growth should not be null");
            assertNotNull(summary.getSalesGrowth(), "Sales growth should not be null");

            // Verify payment analysis structure
            SalesReportDTO.PaymentMethodAnalysis paymentAnalysis = report.getPaymentAnalysis();
            assertNotNull(paymentAnalysis.getCountByMethod(), "Count by method should not be null");
            assertNotNull(paymentAnalysis.getRevenueByMethod(), "Revenue by method should not be null");
            assertNotNull(paymentAnalysis.getMostPopularMethod(), "Most popular method should not be null");
            assertNotNull(paymentAnalysis.getHighestRevenueMethod(), "Highest revenue method should not be null");

            // Verify regional analysis structure
            SalesReportDTO.RegionalAnalysis regionalAnalysis = report.getRegionalAnalysis();
            assertNotNull(regionalAnalysis.getRevenueByRegion(), "Revenue by region should not be null");
            assertNotNull(regionalAnalysis.getSalesByRegion(), "Sales by region should not be null");
            assertNotNull(regionalAnalysis.getTopPerformingRegion(), "Top performing region should not be null");
            assertNotNull(regionalAnalysis.getRegionalGrowth(), "Regional growth should not be null");
        });
    }

    @Test
    void testComprehensiveSalesReportWithFilters_ShouldReturnValidResponse() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerIds(java.util.Arrays.asList(1L, 2L, 3L))
                .productIds(java.util.Arrays.asList(1L, 2L))
                .categoryIds(java.util.Arrays.asList(1L))
                .build();

        // This should not throw any exceptions even with filters
        assertDoesNotThrow(() -> {
            SalesReportDTO report = reportService.generateComprehensiveSalesReport(request);
            assertNotNull(report, "Report with filters should not be null");
            assertNotNull(report.getSummary(), "Summary with filters should not be null");
        });
    }

    @Test
    void testComprehensiveSalesReportWithEmptyDateRange_ShouldHandleGracefully() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(1); // Very short range

        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Should handle empty results gracefully
        assertDoesNotThrow(() -> {
            SalesReportDTO report = reportService.generateComprehensiveSalesReport(request);
            assertNotNull(report, "Report should not be null even with empty date range");

            // Should have zero values but not null fields
            SalesReportDTO.SalesSummary summary = report.getSummary();
            assertNotNull(summary.getTotalSales(), "Total sales should not be null");
            assertNotNull(summary.getTotalRevenue(), "Total revenue should not be null");

            // Collections should be empty but not null
            assertNotNull(report.getDailyBreakdown(), "Daily breakdown should not be null");
            assertNotNull(report.getTopCustomers(), "Top customers should not be null");
            assertNotNull(report.getTopProducts(), "Top products should not be null");
        });
    }

    @Test
    void testComprehensiveSalesReportPerformance_ShouldCompleteInReasonableTime() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(365); // Full year

        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        long startTime = System.currentTimeMillis();

        assertDoesNotThrow(() -> {
            SalesReportDTO report = reportService.generateComprehensiveSalesReport(request);
            assertNotNull(report, "Report should be generated successfully");
        });

        long executionTime = System.currentTimeMillis() - startTime;

        // Should complete within reasonable time (10 seconds for large dataset)
        assertTrue(executionTime < 10000,
            "Report generation should complete within 10 seconds, took: " + executionTime + "ms");
    }
}
