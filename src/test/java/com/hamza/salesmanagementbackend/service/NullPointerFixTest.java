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
 * Test to verify that null pointer exceptions are fixed in the comprehensive sales report
 */
@ExtendWith(MockitoExtension.class)
class NullPointerFixTest {

    @Mock
    private SaleRepository saleRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReportHelperService reportHelperService;

    private ReportRequestDTO testRequest;

    @BeforeEach
    void setUp() {
        testRequest = ReportRequestDTO.builder()
                .startDate(LocalDateTime.now().minusDays(30))
                .endDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testGenerateAdvancedSalesSummary_WithNullValues_ShouldNotThrowNullPointer() {
        // Create sales with null values that previously caused NPE
        Customer customer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .build();

        Sale saleWithNulls = Sale.builder()
                .id(1L)
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .totalAmount(null) // This was causing NPE
                .promotionDiscountAmount(null) // This was causing NPE
                .taxAmount(null) // This was causing NPE
                .status(SaleStatus.COMPLETED)
                .build();

        Sale saleWithValues = Sale.builder()
                .id(2L)
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(100))
                .promotionDiscountAmount(BigDecimal.valueOf(10))
                .taxAmount(BigDecimal.valueOf(8))
                .status(SaleStatus.COMPLETED)
                .build();

        List<Sale> sales = Arrays.asList(saleWithNulls, saleWithValues);

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            SalesReportDTO.SalesSummary summary = reportHelperService.generateAdvancedSalesSummary(sales, testRequest);
            
            // Verify the summary is created successfully
            assertNotNull(summary);
            assertEquals(2L, summary.getTotalSales());
            assertEquals(BigDecimal.valueOf(100), summary.getTotalRevenue()); // Only non-null value counted
            assertEquals(BigDecimal.valueOf(10), summary.getTotalDiscounts()); // Only non-null value counted
            assertEquals(BigDecimal.valueOf(8), summary.getTotalTax()); // Only non-null value counted
            assertEquals(BigDecimal.valueOf(90), summary.getNetRevenue()); // 100 - 10
        });
    }

    @Test
    void testGenerateTopCustomersAnalysis_WithNullCustomer_ShouldNotThrowNullPointer() {
        Sale saleWithNullCustomer = Sale.builder()
                .id(1L)
                .customer(null) // This could cause NPE
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        Customer validCustomer = Customer.builder()
                .id(1L)
                .name("Valid Customer")
                .email("valid@example.com")
                .build();

        Sale saleWithValidCustomer = Sale.builder()
                .id(2L)
                .customer(validCustomer)
                .totalAmount(BigDecimal.valueOf(200))
                .build();

        List<Sale> sales = Arrays.asList(saleWithNullCustomer, saleWithValidCustomer);

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            List<SalesReportDTO.TopCustomer> topCustomers = reportHelperService.generateTopCustomersAnalysis(sales);
            
            // Should only include the valid customer
            assertNotNull(topCustomers);
            assertEquals(1, topCustomers.size());
            assertEquals("Valid Customer", topCustomers.get(0).getCustomerName());
        });
    }

    @Test
    void testGenerateTopProductsAnalysis_WithNullValues_ShouldNotThrowNullPointer() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .build();

        SaleItem itemWithNulls = SaleItem.builder()
                .id(1L)
                .product(product)
                .quantity(null) // This could cause NPE
                .unitPrice(null) // This could cause NPE
                .costPrice(null) // This could cause NPE
                .build();

        Sale saleWithNullItems = Sale.builder()
                .id(1L)
                .items(null) // This could cause NPE
                .build();

        Sale saleWithValidItems = Sale.builder()
                .id(2L)
                .items(Arrays.asList(itemWithNulls))
                .build();

        itemWithNulls.setSale(saleWithValidItems);

        List<Sale> sales = Arrays.asList(saleWithNullItems, saleWithValidItems);

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            List<SalesReportDTO.TopProduct> topProducts = reportHelperService.generateTopProductsAnalysis(sales);
            
            // Should handle null values gracefully
            assertNotNull(topProducts);
        });
    }

    @Test
    void testGeneratePaymentMethodAnalysis_WithNullTotalAmount_ShouldNotThrowNullPointer() {
        Sale saleWithNullAmount = Sale.builder()
                .id(1L)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .totalAmount(null) // This was causing NPE
                .build();

        Sale saleWithValidAmount = Sale.builder()
                .id(2L)
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        List<Sale> sales = Arrays.asList(saleWithNullAmount, saleWithValidAmount);

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            SalesReportDTO.PaymentMethodAnalysis analysis = reportHelperService.generatePaymentMethodAnalysis(sales);
            
            // Should handle null values gracefully
            assertNotNull(analysis);
            assertNotNull(analysis.getRevenueByMethod());
            assertEquals(BigDecimal.valueOf(100), analysis.getRevenueByMethod().get("CREDIT_CARD"));
            assertEquals(BigDecimal.ZERO, analysis.getRevenueByMethod().get("CASH")); // Null treated as zero
        });
    }

    @Test
    void testGenerateRegionalAnalysis_WithNullCustomer_ShouldNotThrowNullPointer() {
        Sale saleWithNullCustomer = Sale.builder()
                .id(1L)
                .customer(null) // This could cause NPE
                .totalAmount(BigDecimal.valueOf(100))
                .build();

        Customer customerWithAddress = Customer.builder()
                .id(1L)
                .name("Customer")
                .address("123 North Street, California")
                .build();

        Sale saleWithValidCustomer = Sale.builder()
                .id(2L)
                .customer(customerWithAddress)
                .totalAmount(BigDecimal.valueOf(200))
                .build();

        List<Sale> sales = Arrays.asList(saleWithNullCustomer, saleWithValidCustomer);

        // This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            SalesReportDTO.RegionalAnalysis analysis = reportHelperService.generateRegionalAnalysis(sales);
            
            // Should handle null values gracefully
            assertNotNull(analysis);
            assertNotNull(analysis.getRevenueByRegion());
            assertNotNull(analysis.getSalesByRegion());
        });
    }
}
