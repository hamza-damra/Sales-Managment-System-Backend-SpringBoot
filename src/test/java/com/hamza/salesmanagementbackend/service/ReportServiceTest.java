package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.Category;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleItem;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReportService reportService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Sale testSale;
    private Customer testCustomer;
    private Product testProduct;
    private SaleItem testSaleItem;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();

        // Create test data
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic devices and accessories")
                .status(Category.CategoryStatus.ACTIVE)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .category(testCategory)
                .build();

        testSaleItem = SaleItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(99.99))
                .subtotal(BigDecimal.valueOf(199.98))
                .build();

        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now().minusDays(5))
                .totalAmount(BigDecimal.valueOf(199.98))
                .status(SaleStatus.COMPLETED)
                .items(List.of(testSaleItem))
                .build();
    }

    @Test
    void generateSalesReport_Success() {
        // Given
        List<Sale> sales = List.of(testSale);
        when(saleRepository.findBySaleDateBetween(startDate, endDate)).thenReturn(sales);

        // When
        Map<String, Object> result = reportService.generateSalesReport(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("period"));
        assertTrue(result.containsKey("summary"));
        verify(saleRepository).findBySaleDateBetween(startDate, endDate);
    }

    @Test
    void generateCustomerReport_Success() {
        // Given
        List<Sale> sales = List.of(testSale);
        when(saleRepository.findAll()).thenReturn(sales);
        when(customerRepository.count()).thenReturn(100L);

        // When
        Map<String, Object> result = reportService.generateCustomerReport();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("totalCustomers"));
        assertTrue(result.containsKey("activeCustomers"));
        verify(saleRepository).findAll();
        verify(customerRepository).count();
    }

    @Test
    void generateInventoryReport_Success() {
        // Given
        List<Product> products = List.of(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        Map<String, Object> result = reportService.generateInventoryReport();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("totalProducts"));
        assertTrue(result.containsKey("totalInventoryValue"));
        verify(productRepository).findAll();
    }

    @Test
    void generateRevenueTrends_Success() {
        // Given
        int months = 6;
        List<Sale> sales = List.of(testSale);
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(sales);

        // When
        Map<String, Object> result = reportService.generateRevenueTrends(months);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("period"));
        assertTrue(result.containsKey("totalRevenue"));
        assertTrue(result.containsKey("averageMonthlyRevenue"));
        verify(saleRepository).findBySaleDateBetween(any(), any());
    }

    @Test
    void generateRevenueTrends_EmptyData_Success() {
        // Given
        int months = 6;
        when(saleRepository.findBySaleDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, Object> result = reportService.generateRevenueTrends(months);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("period"));
        assertTrue(result.containsKey("totalRevenue"));
        assertEquals(BigDecimal.ZERO, result.get("totalRevenue"));
        assertEquals(BigDecimal.ZERO, result.get("averageMonthlyRevenue"));
        verify(saleRepository).findBySaleDateBetween(any(), any());
    }

    @Test
    void generateTopPerformersReport_Success() {
        // Given
        List<Sale> sales = List.of(testSale);
        when(saleRepository.findBySaleDateBetween(startDate, endDate)).thenReturn(sales);

        // When
        Map<String, Object> result = reportService.generateTopPerformersReport(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("topCustomersByRevenue"));
        assertTrue(result.containsKey("topProductsByQuantity"));
        assertTrue(result.containsKey("topProductsByRevenue"));
        verify(saleRepository).findBySaleDateBetween(startDate, endDate);
    }
}
