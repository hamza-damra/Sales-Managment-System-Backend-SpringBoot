package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceProductPerformanceTest {

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

    private ReportRequestDTO reportRequest;
    private List<Sale> mockSales;
    private Category category1, category2;
    private Product product1, product2, product3;
    private Customer customer1, customer2;

    @BeforeEach
    void setUp() {
        // Setup test data
        LocalDateTime startDate = LocalDateTime.of(2025, 6, 10, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 7, 10, 23, 59);
        
        reportRequest = ReportRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Create test categories
        category1 = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .build();
                
        category2 = Category.builder()
                .id(2L)
                .name("Books")
                .description("Book products")
                .build();

        // Create test products
        product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .sku("LAP001")
                .price(new BigDecimal("1000.00"))
                .costPrice(new BigDecimal("700.00"))
                .category(category1)
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .sku("MOU001")
                .price(new BigDecimal("25.00"))
                .costPrice(new BigDecimal("15.00"))
                .category(category1)
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();

        product3 = Product.builder()
                .id(3L)
                .name("Programming Book")
                .sku("BOO001")
                .price(new BigDecimal("50.00"))
                .costPrice(new BigDecimal("30.00"))
                .category(category2)
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();

        // Create test customers
        customer1 = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
                
        customer2 = Customer.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane@example.com")
                .build();

        // Create test sales with items
        mockSales = createMockSales();
    }

    private List<Sale> createMockSales() {
        // Sale 1: Laptop + Mouse (multi-item sale)
        Sale sale1 = Sale.builder()
                .id(1L)
                .customer(customer1)
                .saleDate(LocalDateTime.of(2025, 6, 15, 10, 0))
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("1025.00"))
                .build();
                
        SaleItem item1_1 = SaleItem.builder()
                .id(1L)
                .sale(sale1)
                .product(product1)
                .quantity(1)
                .unitPrice(new BigDecimal("1000.00"))
                .totalPrice(new BigDecimal("1000.00"))
                .costPrice(new BigDecimal("700.00"))
                .build();
                
        SaleItem item1_2 = SaleItem.builder()
                .id(2L)
                .sale(sale1)
                .product(product2)
                .quantity(1)
                .unitPrice(new BigDecimal("25.00"))
                .totalPrice(new BigDecimal("25.00"))
                .costPrice(new BigDecimal("15.00"))
                .build();
                
        sale1.setItems(Arrays.asList(item1_1, item1_2));

        // Sale 2: Programming Book (single item sale)
        Sale sale2 = Sale.builder()
                .id(2L)
                .customer(customer2)
                .saleDate(LocalDateTime.of(2025, 6, 20, 14, 30))
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("50.00"))
                .build();
                
        SaleItem item2_1 = SaleItem.builder()
                .id(3L)
                .sale(sale2)
                .product(product3)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .costPrice(new BigDecimal("30.00"))
                .build();
                
        sale2.setItems(Arrays.asList(item2_1));

        // Sale 3: Multiple mice (quantity sale)
        Sale sale3 = Sale.builder()
                .id(3L)
                .customer(customer1)
                .saleDate(LocalDateTime.of(2025, 7, 5, 16, 45))
                .status(SaleStatus.COMPLETED)
                .totalAmount(new BigDecimal("75.00"))
                .build();
                
        SaleItem item3_1 = SaleItem.builder()
                .id(4L)
                .sale(sale3)
                .product(product2)
                .quantity(3)
                .unitPrice(new BigDecimal("25.00"))
                .totalPrice(new BigDecimal("75.00"))
                .costPrice(new BigDecimal("15.00"))
                .build();
                
        sale3.setItems(Arrays.asList(item3_1));

        return Arrays.asList(sale1, sale2, sale3);
    }

    @Test
    void testGenerateProductPerformanceReport_WithValidData() {
        // Arrange
        when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockSales);

        // Act
        Map<String, Object> result = reportService.generateProductPerformanceReport(reportRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("productRankings"));
        assertTrue(result.containsKey("profitabilityAnalysis"));
        assertTrue(result.containsKey("categoryPerformance"));
        assertTrue(result.containsKey("productTrends"));
        assertTrue(result.containsKey("crossSellAnalysis"));

        // Verify productRankings structure
        @SuppressWarnings("unchecked")
        Map<String, Object> productRankings = (Map<String, Object>) result.get("productRankings");
        assertNotNull(productRankings);
        assertTrue(productRankings.containsKey("topProductsByRevenue"));
        assertTrue(productRankings.containsKey("topProductsByQuantity"));
        assertTrue(productRankings.containsKey("topProductsByProfit"));
        assertTrue(productRankings.containsKey("summary"));

        // Verify profitabilityAnalysis structure
        @SuppressWarnings("unchecked")
        Map<String, Object> profitabilityAnalysis = (Map<String, Object>) result.get("profitabilityAnalysis");
        assertNotNull(profitabilityAnalysis);
        assertTrue(profitabilityAnalysis.containsKey("profitabilityMetrics"));
        assertTrue(profitabilityAnalysis.containsKey("profitMarginDistribution"));

        // Verify categoryPerformance structure
        @SuppressWarnings("unchecked")
        Map<String, Object> categoryPerformance = (Map<String, Object>) result.get("categoryPerformance");
        assertNotNull(categoryPerformance);
        assertTrue(categoryPerformance.containsKey("categoryMetrics"));
        assertTrue(categoryPerformance.containsKey("topCategoriesByRevenue"));

        // Verify productTrends structure
        @SuppressWarnings("unchecked")
        Map<String, Object> productTrends = (Map<String, Object>) result.get("productTrends");
        assertNotNull(productTrends);
        assertTrue(productTrends.containsKey("dailyTrends"));
        assertTrue(productTrends.containsKey("weeklyTrends"));
        assertTrue(productTrends.containsKey("trendingProducts"));

        // Verify crossSellAnalysis structure
        @SuppressWarnings("unchecked")
        Map<String, Object> crossSellAnalysis = (Map<String, Object>) result.get("crossSellAnalysis");
        assertNotNull(crossSellAnalysis);
        assertTrue(crossSellAnalysis.containsKey("productPairs"));
        assertTrue(crossSellAnalysis.containsKey("crossSellOpportunities"));
        assertTrue(crossSellAnalysis.containsKey("basketAnalysis"));
    }

    @Test
    void testGenerateProductPerformanceReport_WithEmptyData() {
        // Arrange
        when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        // Act
        Map<String, Object> result = reportService.generateProductPerformanceReport(reportRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("productRankings"));
        assertTrue(result.containsKey("profitabilityAnalysis"));
        assertTrue(result.containsKey("categoryPerformance"));
        assertTrue(result.containsKey("productTrends"));
        assertTrue(result.containsKey("crossSellAnalysis"));

        // Verify that empty data returns appropriate messages
        @SuppressWarnings("unchecked")
        Map<String, Object> productRankings = (Map<String, Object>) result.get("productRankings");
        assertTrue(productRankings.containsKey("message"));
        assertEquals("No sales data available for product rankings", productRankings.get("message"));
    }

    @Test
    void testProductCountAccuracy() {
        // Arrange
        when(productRepository.count()).thenReturn(10L); // Total products in database
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3)); // Active products

        // Act
        Map<String, Object> result = reportService.generateProductPerformanceReport(reportRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("reportSummary"));

        @SuppressWarnings("unchecked")
        Map<String, Object> reportSummary = (Map<String, Object>) result.get("reportSummary");

        @SuppressWarnings("unchecked")
        Map<String, Object> productCounts = (Map<String, Object>) reportSummary.get("productCounts");

        // Verify different product count metrics
        assertEquals(10L, productCounts.get("totalProducts"),
                    "Should show total products in database");
        assertEquals(3L, productCounts.get("activeProducts"),
                    "Should show active products count");

        // Verify product coverage percentage exists
        assertTrue(productCounts.containsKey("productCoveragePercentage"));
        BigDecimal coveragePercentage = (BigDecimal) productCounts.get("productCoveragePercentage");
        assertNotNull(coveragePercentage);
    }

    @Test
    void testDataValidationWithProductCoverage() {
        // Arrange
        when(productRepository.count()).thenReturn(10L);
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

        // Act
        Map<String, Object> result = reportService.generateProductPerformanceReport(reportRequest);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) result.get("dataValidation");

        assertNotNull(validation);
        assertEquals("PASSED", validation.get("validationStatus"),
                    "Validation should pass for well-formed test data");

        // Verify product coverage information
        @SuppressWarnings("unchecked")
        Map<String, Object> productCoverage = (Map<String, Object>) validation.get("productCoverage");
        assertNotNull(productCoverage);
        assertTrue(productCoverage.containsKey("coveragePercentage"));
        assertTrue(productCoverage.containsKey("productsWithSales"));
        assertTrue(productCoverage.containsKey("activeProducts"));
        assertTrue(productCoverage.containsKey("totalProducts"));

        assertEquals(3L, productCoverage.get("activeProducts"));
        assertEquals(10L, productCoverage.get("totalProducts"));
    }
}
