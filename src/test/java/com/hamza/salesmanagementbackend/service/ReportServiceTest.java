package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test suite for ReportService
 * Tests business logic, data aggregation, and report generation
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

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
    private ReturnRepository returnRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Sale> mockSales;
    private List<Customer> mockCustomers;
    private List<Product> mockProducts;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();
        
        setupMockData();
    }

    private void setupMockData() {
        // Setup mock customers
        Customer customer1 = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Customer customer2 = Customer.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        mockCustomers = Arrays.asList(customer1, customer2);

        // Setup mock products
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        Product product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000))
                .stockQuantity(50)
                .category(category)
                .costPrice(BigDecimal.valueOf(800))
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .price(BigDecimal.valueOf(25))
                .stockQuantity(100)
                .category(category)
                .costPrice(BigDecimal.valueOf(15))
                .build();

        mockProducts = Arrays.asList(product1, product2);

        // Setup mock sales
        Sale sale1 = Sale.builder()
                .id(1L)
                .customer(customer1)
                .saleDate(LocalDateTime.now().minusDays(5))
                .totalAmount(BigDecimal.valueOf(1025))
                .status(SaleStatus.COMPLETED)
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .promotionDiscountAmount(BigDecimal.valueOf(25))
                .taxAmount(BigDecimal.valueOf(100))
                .build();

        Sale sale2 = Sale.builder()
                .id(2L)
                .customer(customer2)
                .saleDate(LocalDateTime.now().minusDays(3))
                .totalAmount(BigDecimal.valueOf(50))
                .status(SaleStatus.COMPLETED)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .promotionDiscountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.valueOf(5))
                .build();

        // Setup sale items
        SaleItem item1 = SaleItem.builder()
                .id(1L)
                .sale(sale1)
                .product(product1)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(1000))
                .costPrice(BigDecimal.valueOf(800))
                .totalPrice(BigDecimal.valueOf(1000))
                .build();

        SaleItem item2 = SaleItem.builder()
                .id(2L)
                .sale(sale2)
                .product(product2)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(25))
                .costPrice(BigDecimal.valueOf(15))
                .totalPrice(BigDecimal.valueOf(50))
                .build();

        sale1.setItems(Arrays.asList(item1));
        sale2.setItems(Arrays.asList(item2));

        mockSales = Arrays.asList(sale1, sale2);
    }

    @Test
    @DisplayName("Should generate comprehensive sales report successfully")
    void shouldGenerateComprehensiveSalesReport() {
        // Given
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(saleRepository.findBySaleDateBetween(startDate, endDate))
                .thenReturn(mockSales);

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary()).isNotNull();
        assertThat(result.getSummary().getTotalSales()).isEqualTo(2L);
        assertThat(result.getSummary().getTotalRevenue()).isEqualTo(BigDecimal.valueOf(1075));
        assertThat(result.getSummary().getUniqueCustomers()).isEqualTo(2);
        
        assertThat(result.getDailyBreakdown()).isNotEmpty();
        assertThat(result.getTopCustomers()).isNotEmpty();
        assertThat(result.getTopProducts()).isNotEmpty();
    }

    @Test
    @DisplayName("Should generate sales trends analysis")
    void shouldGenerateSalesTrendsAnalysis() {
        // Given
        when(saleRepository.findBySaleDateBetween(any(), any()))
                .thenReturn(mockSales);

        // When
        Map<String, Object> result = reportService.generateSalesTrendsAnalysis(12, "MONTH");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("forecast", "seasonality", "growthMetrics");
    }

    @Test
    @DisplayName("Should generate customer analytics report")
    void shouldGenerateCustomerAnalyticsReport() {
        // Given
        when(saleRepository.findBySaleDateBetween(any(), any()))
                .thenReturn(mockSales);

        // When
        Map<String, Object> result = reportService.generateCustomerAnalyticsReport(false, 12);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys(
                "customerSegmentation", 
                "lifetimeValueAnalysis", 
                "behaviorAnalysis",
                "acquisitionMetrics",
                "churnAnalysis"
        );
    }

    @Test
    @DisplayName("Should generate customer lifetime value report with pagination")
    void shouldGenerateCustomerLifetimeValueReport() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        when(customerRepository.findAll()).thenReturn(mockCustomers);

        // When
        Page<Map<String, Object>> result = reportService.generateCustomerLifetimeValueReport(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should generate product performance report")
    void shouldGenerateProductPerformanceReport() {
        // Given
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(saleRepository.findBySaleDateBetween(startDate, endDate))
                .thenReturn(mockSales);

        // When
        Map<String, Object> result = reportService.generateProductPerformanceReport(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys(
                "productRankings",
                "profitabilityAnalysis",
                "categoryPerformance",
                "productTrends",
                "crossSellAnalysis"
        );
    }

    @Test
    @DisplayName("Should generate inventory status report")
    void shouldGenerateInventoryStatusReport() {
        // Given
        when(productRepository.findAll()).thenReturn(mockProducts);

        // When
        Map<String, Object> result = reportService.generateInventoryStatusReport(false, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys(
                "stockLevels",
                "lowStockAlerts",
                "outOfStockItems",
                "inventoryValuation",
                "warehouseDistribution"
        );
    }

    @Test
    @DisplayName("Should generate real-time KPIs")
    void shouldGenerateRealTimeKPIs() {
        // Given
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        when(saleRepository.findBySaleDateBetween(any(), any()))
                .thenReturn(mockSales);
        when(productRepository.findAll()).thenReturn(mockProducts);
        when(returnRepository.findByStatus(any())).thenReturn(Arrays.asList());

        // When
        Map<String, Object> result = reportService.generateRealTimeKPIs();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys(
                "todaysSales",
                "todaysRevenue",
                "activeCustomers",
                "inventoryValue",
                "lowStockItems",
                "pendingReturns"
        );
        
        assertThat(result.get("todaysSales")).isInstanceOf(Long.class);
        assertThat(result.get("todaysRevenue")).isInstanceOf(BigDecimal.class);
        assertThat(result.get("inventoryValue")).isInstanceOf(BigDecimal.class);
    }

    @Test
    @DisplayName("Should handle empty sales data gracefully")
    void shouldHandleEmptySalesDataGracefully() {
        // Given
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(saleRepository.findBySaleDateBetween(startDate, endDate))
                .thenReturn(Arrays.asList());

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSummary().getTotalSales()).isEqualTo(0L);
        assertThat(result.getSummary().getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getDailyBreakdown()).isEmpty();
        assertThat(result.getTopCustomers()).isEmpty();
        assertThat(result.getTopProducts()).isEmpty();
    }

    @Test
    @DisplayName("Should calculate correct revenue metrics")
    void shouldCalculateCorrectRevenueMetrics() {
        // Given
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(saleRepository.findBySaleDateBetween(startDate, endDate))
                .thenReturn(mockSales);

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        SalesReportDTO.SalesSummary summary = result.getSummary();
        
        // Total revenue should be sum of all completed sales
        BigDecimal expectedRevenue = BigDecimal.valueOf(1025).add(BigDecimal.valueOf(50));
        assertThat(summary.getTotalRevenue()).isEqualTo(expectedRevenue);
        
        // Average order value should be total revenue / number of sales
        BigDecimal expectedAOV = expectedRevenue.divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        assertThat(summary.getAverageOrderValue()).isEqualTo(expectedAOV);
        
        // Total discounts should be sum of all promotion discounts
        BigDecimal expectedDiscounts = BigDecimal.valueOf(25);
        assertThat(summary.getTotalDiscounts()).isEqualTo(expectedDiscounts);
    }

    @Test
    @DisplayName("Should filter sales by status correctly")
    void shouldFilterSalesByStatusCorrectly() {
        // Given
        Sale pendingSale = Sale.builder()
                .id(3L)
                .customer(mockCustomers.get(0))
                .saleDate(LocalDateTime.now().minusDays(1))
                .totalAmount(BigDecimal.valueOf(100))
                .status(SaleStatus.PENDING)
                .build();

        List<Sale> allSales = Arrays.asList(mockSales.get(0), mockSales.get(1), pendingSale);
        
        ReportRequestDTO request = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        when(saleRepository.findBySaleDateBetween(startDate, endDate))
                .thenReturn(allSales);

        // When
        SalesReportDTO result = reportService.generateComprehensiveSalesReport(request);

        // Then
        // Should only include completed sales in revenue calculations
        assertThat(result.getSummary().getTotalSales()).isEqualTo(2L); // Only completed sales
        assertThat(result.getSummary().getTotalRevenue()).isEqualTo(BigDecimal.valueOf(1075));
    }
}
