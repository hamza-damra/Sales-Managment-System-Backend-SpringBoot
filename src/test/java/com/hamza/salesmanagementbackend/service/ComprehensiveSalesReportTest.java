package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test class for comprehensive sales report functionality
 * Tests all the fixed null value issues in the sales report
 */
@ExtendWith(MockitoExtension.class)
class ComprehensiveSalesReportTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private PromotionRepository promotionRepository;
    
    @Mock
    private AppliedPromotionRepository appliedPromotionRepository;
    
    @Mock
    private ReportHelperService reportHelperService;
    
    @InjectMocks
    private ReportService reportService;

    private Customer testCustomer;
    private Product testProduct;
    private Sale testSale;
    private SaleItem testSaleItem;
    private ReportRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        // Create test customer with address for regional analysis
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .address("123 North Street, California, USA")
                .billingAddress("123 North Street, California, USA")
                .build();

        // Create test product with category
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
        
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .category(category)
                .build();

        // Create test sale item
        testSaleItem = SaleItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100))
                .totalPrice(BigDecimal.valueOf(200))
                .costPrice(BigDecimal.valueOf(60))
                .build();

        // Create test sale with all required fields
        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(220))
                .subtotal(BigDecimal.valueOf(200))
                .taxAmount(BigDecimal.valueOf(20))
                .promotionDiscountAmount(BigDecimal.valueOf(10))
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .status(SaleStatus.COMPLETED)
                .items(Arrays.asList(testSaleItem))
                .build();

        testSaleItem.setSale(testSale);

        // Create test request
        testRequest = ReportRequestDTO.builder()
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testGenerateComprehensiveSalesReport_ShouldNotReturnNullValues() {
        // Arrange
        List<Sale> testSales = Arrays.asList(testSale);
        
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(testSales);
        
        // Mock helper service methods to return proper data
        SalesReportDTO.SalesSummary mockSummary = SalesReportDTO.SalesSummary.builder()
                .totalSales(1L)
                .totalRevenue(BigDecimal.valueOf(220))
                .averageOrderValue(BigDecimal.valueOf(220))
                .totalDiscounts(BigDecimal.valueOf(10))
                .totalTax(BigDecimal.valueOf(20))
                .netRevenue(BigDecimal.valueOf(210))
                .uniqueCustomers(1)
                .conversionRate(85.5)
                .revenueGrowth(BigDecimal.valueOf(12.5))
                .salesGrowth(8.3)
                .build();
        
        when(reportHelperService.generateAdvancedSalesSummary(any(), any())).thenReturn(mockSummary);
        when(reportHelperService.generateDailyBreakdown(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generateTopCustomersAnalysis(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generateTopProductsAnalysis(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generatePaymentMethodAnalysis(any())).thenReturn(
                SalesReportDTO.PaymentMethodAnalysis.builder().build());
        when(reportHelperService.generateRegionalAnalysis(any())).thenReturn(
                SalesReportDTO.RegionalAnalysis.builder().build());

        // Act
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(testRequest);

        // Assert
        assertNotNull(result, "Report should not be null");
        assertNotNull(result.getSummary(), "Summary should not be null");
        assertNotNull(result.getDailyBreakdown(), "Daily breakdown should not be null");
        assertNotNull(result.getTopCustomers(), "Top customers should not be null");
        assertNotNull(result.getTopProducts(), "Top products should not be null");
        assertNotNull(result.getSalesByStatus(), "Sales by status should not be null");
        assertNotNull(result.getTrends(), "Trends should not be null");
        assertNotNull(result.getPaymentAnalysis(), "Payment analysis should not be null");
        assertNotNull(result.getRegionalAnalysis(), "Regional analysis should not be null");

        // Verify summary fields are not null
        SalesReportDTO.SalesSummary summary = result.getSummary();
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
    }

    @Test
    void testGenerateComprehensiveSalesReport_WithEmptyData_ShouldHandleGracefully() {
        // Arrange
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(Arrays.asList());
        
        // Mock helper service methods for empty data
        when(reportHelperService.generateAdvancedSalesSummary(any(), any())).thenReturn(
                SalesReportDTO.SalesSummary.builder()
                        .totalSales(0L)
                        .totalRevenue(BigDecimal.ZERO)
                        .averageOrderValue(BigDecimal.ZERO)
                        .totalDiscounts(BigDecimal.ZERO)
                        .totalTax(BigDecimal.ZERO)
                        .netRevenue(BigDecimal.ZERO)
                        .uniqueCustomers(0)
                        .conversionRate(0.0)
                        .revenueGrowth(BigDecimal.ZERO)
                        .salesGrowth(0.0)
                        .build());
        when(reportHelperService.generateDailyBreakdown(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generateTopCustomersAnalysis(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generateTopProductsAnalysis(any())).thenReturn(Arrays.asList());
        when(reportHelperService.generatePaymentMethodAnalysis(any())).thenReturn(
                SalesReportDTO.PaymentMethodAnalysis.builder().build());
        when(reportHelperService.generateRegionalAnalysis(any())).thenReturn(
                SalesReportDTO.RegionalAnalysis.builder().build());

        // Act
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(testRequest);

        // Assert
        assertNotNull(result, "Report should not be null even with empty data");
        assertNotNull(result.getSummary(), "Summary should not be null");
        assertEquals(0L, result.getSummary().getTotalSales(), "Total sales should be 0 for empty data");
        assertEquals(BigDecimal.ZERO, result.getSummary().getTotalRevenue(), "Total revenue should be 0 for empty data");
    }
}
