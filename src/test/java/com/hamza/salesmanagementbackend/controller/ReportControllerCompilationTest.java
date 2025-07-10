package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.dto.report.StandardReportResponse;
import com.hamza.salesmanagementbackend.service.ReportService;
import com.hamza.salesmanagementbackend.service.ReportExportService;
import com.hamza.salesmanagementbackend.service.ReportCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Simple compilation test to verify the ReportController fixes compile correctly
 */
@ExtendWith(MockitoExtension.class)
class ReportControllerCompilationTest {

    @Mock
    private ReportService reportService;
    
    @Mock
    private ReportExportService reportExportService;
    
    @Mock
    private ReportCacheService reportCacheService;
    
    @InjectMocks
    private ReportController reportController;

    @Test
    void testGetComprehensiveSalesReport_CompilationTest() {
        // Arrange
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .customerIds(Arrays.asList(1L, 2L))
                .productIds(Arrays.asList(1L, 2L))
                .categoryIds(Arrays.asList(1L))
                .regions(Arrays.asList("North Region"))
                .paymentMethods(Arrays.asList("CREDIT_CARD"))
                .statuses(Arrays.asList("COMPLETED"))
                .amountRange(ReportRequestDTO.BigDecimalRange.builder()
                        .min(BigDecimal.valueOf(100))
                        .max(BigDecimal.valueOf(1000))
                        .build())
                .includeReturns(false)
                .includePromotions(true)
                .build();

        SalesReportDTO mockReport = SalesReportDTO.builder()
                .summary(SalesReportDTO.SalesSummary.builder()
                        .totalSales(10L)
                        .totalRevenue(BigDecimal.valueOf(1000))
                        .build())
                .build();

        when(reportService.generateComprehensiveSalesReport(any())).thenReturn(mockReport);

        // Act
        ResponseEntity<StandardReportResponse<SalesReportDTO>> response = 
                reportController.getComprehensiveSalesReport(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertNotNull(response.getBody().getData());
        assertNotNull(response.getBody().getMetadata());
        
        // Verify metadata is properly populated
        assertNotNull(response.getBody().getMetadata().getReportType());
        assertNotNull(response.getBody().getMetadata().getReportName());
        assertNotNull(response.getBody().getMetadata().getGeneratedAt());
        assertNotNull(response.getBody().getMetadata().getPeriod());
        assertNotNull(response.getBody().getMetadata().getExecutionTimeMs());
        assertNotNull(response.getBody().getMetadata().getVersion());
        assertNotNull(response.getBody().getMetadata().getFromCache());
        
        // Verify period information
        assertNotNull(response.getBody().getMetadata().getPeriod().getStartDate());
        assertNotNull(response.getBody().getMetadata().getPeriod().getEndDate());
        assertNotNull(response.getBody().getMetadata().getPeriod().getDaysIncluded());
        
        // Verify applied filters are captured
        if (response.getBody().getMetadata().getAppliedFilters() != null) {
            assertTrue(response.getBody().getMetadata().getAppliedFilters().containsKey("customerIds") ||
                      response.getBody().getMetadata().getAppliedFilters().isEmpty());
        }
    }

    @Test
    void testReportRequestDTO_AllFieldsAccessible() {
        // This test verifies that all the fields we're trying to access in the controller exist
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .customerIds(Arrays.asList(1L))
                .productIds(Arrays.asList(1L))
                .categoryIds(Arrays.asList(1L))
                .regions(Arrays.asList("North"))
                .paymentMethods(Arrays.asList("CASH"))
                .statuses(Arrays.asList("COMPLETED"))
                .amountRange(ReportRequestDTO.BigDecimalRange.builder()
                        .min(BigDecimal.valueOf(0))
                        .max(BigDecimal.valueOf(1000))
                        .build())
                .discountRange(ReportRequestDTO.BigDecimalRange.builder()
                        .min(BigDecimal.valueOf(0))
                        .max(BigDecimal.valueOf(100))
                        .build())
                .includeReturns(true)
                .includePromotions(false)
                .build();

        // Verify all getters work (compilation test)
        assertNotNull(request.getStartDate());
        assertNotNull(request.getEndDate());
        assertNotNull(request.getCustomerIds());
        assertNotNull(request.getProductIds());
        assertNotNull(request.getCategoryIds());
        assertNotNull(request.getRegions());
        assertNotNull(request.getPaymentMethods());
        assertNotNull(request.getStatuses());
        assertNotNull(request.getAmountRange());
        assertNotNull(request.getDiscountRange());
        assertNotNull(request.getIncludeReturns());
        assertNotNull(request.getIncludePromotions());
        
        // Verify BigDecimalRange works
        assertNotNull(request.getAmountRange().getMin());
        assertNotNull(request.getAmountRange().getMax());
        assertNotNull(request.getDiscountRange().getMin());
        assertNotNull(request.getDiscountRange().getMax());
    }
}
