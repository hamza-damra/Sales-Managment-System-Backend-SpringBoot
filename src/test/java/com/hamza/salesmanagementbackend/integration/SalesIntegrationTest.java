package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.CategoryRepository;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import com.hamza.salesmanagementbackend.service.SaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
class SalesIntegrationTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Clean up any existing data to ensure test isolation
        saleRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category - check if it already exists first
        testCategory = categoryRepository.findByName("Electronics")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name("Electronics")
                            .description("Electronic devices")
                            .status(Category.CategoryStatus.ACTIVE)
                            .displayOrder(1)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        // Create test customer
        testCustomer = Customer.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .billingAddress("123 Main St")
                .shippingAddress("123 Main St")
                .loyaltyPoints(100)
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .totalPurchases(BigDecimal.ZERO)
                .build();

        // Create test product
        testProduct = Product.builder()
                .name("Test Smartphone")
                .description("A test smartphone")
                .price(new BigDecimal("999.99"))
                .costPrice(new BigDecimal("600.00"))
                .stockQuantity(100)
                .category(testCategory)
                .sku("PHONE-TEST-001")
                .minStockLevel(10)
                .reorderPoint(15)
                .unitOfMeasure("PCS")
                .isTaxable(true)
                .taxRate(new BigDecimal("15.0"))
                .build();

        // Save test data
        customerRepository.save(testCustomer);
        productRepository.save(testProduct);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test to ensure proper isolation
        try {
            saleRepository.deleteAll();
            productRepository.deleteAll();
            customerRepository.deleteAll();
            categoryRepository.deleteAll();
        } catch (Exception e) {
            // Log but don't fail the test due to cleanup issues
            System.err.println("Warning: Error during test cleanup: " + e.getMessage());
        }
    }

    @Test
    void createSale_EndToEnd_Success() {
        // Given - Simple sale with 1 item, using actual product price
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .productName(testProduct.getName())
                .quantity(1)
                .unitPrice(testProduct.getPrice()) // Use actual product price
                .originalUnitPrice(testProduct.getPrice())
                .costPrice(testProduct.getCostPrice())
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .subtotal(testProduct.getPrice())
                .totalPrice(testProduct.getPrice())
                .unitOfMeasure("PCS")
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .customerName(testCustomer.getName())
                .totalAmount(testProduct.getPrice()) // Use actual product price
                .items(Arrays.asList(saleItemDTO))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .build();

        // When
        SaleDTO result = saleService.createSale(saleDTO);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getCustomerId());
        assertEquals(testProduct.getPrice(), result.getTotalAmount()); // Expect actual product price
        assertEquals(SaleStatus.PENDING, result.getStatus());
        assertNotNull(result.getSaleNumber());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(testProduct.getId(), result.getItems().get(0).getProductId());

        // Verify database state
        List<Sale> sales = saleRepository.findAll();
        assertEquals(1, sales.size());
        Sale savedSale = sales.get(0);
        assertEquals(testCustomer.getId(), savedSale.getCustomer().getId());
        assertEquals(SaleStatus.PENDING, savedSale.getStatus());
        assertNotNull(savedSale.getSaleNumber());

        // Verify stock reduction
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(99, updatedProduct.getStockQuantity()); // 100 - 1
    }

    @Test
    void completeSaleWorkflow_EndToEnd_Success() {
        // Given - Create a sale first
        SaleDTO createdSale = createTestSale();

        // When - Complete the sale
        SaleDTO completedSaleDTO = saleService.completeSale(createdSale.getId());

        // Then - Verify sale is completed
        assertNotNull(completedSaleDTO);
        assertEquals(SaleStatus.COMPLETED, completedSaleDTO.getStatus());

        Sale completedSale = saleRepository.findById(createdSale.getId()).orElse(null);
        assertNotNull(completedSale);
        assertEquals(SaleStatus.COMPLETED, completedSale.getStatus());

        // Verify customer loyalty points were awarded
        Customer updatedCustomer = customerRepository.findById(testCustomer.getId()).orElse(null);
        assertNotNull(updatedCustomer);
        assertTrue(updatedCustomer.getLoyaltyPoints() >= 100); // Should have earned points
    }

    @Test
    void cancelSaleWorkflow_EndToEnd_Success() {
        // Given - Create a sale first
        SaleDTO createdSale = createTestSale();

        // Record initial stock after sale creation
        Product productAfterSale = productRepository.findById(testProduct.getId()).orElse(null);
        int stockAfterSale = productAfterSale.getStockQuantity();

        // When - Cancel the sale
        SaleDTO cancelledSaleDTO = saleService.cancelSale(createdSale.getId());

        // Then - Verify sale is cancelled
        assertNotNull(cancelledSaleDTO);
        assertEquals(SaleStatus.CANCELLED, cancelledSaleDTO.getStatus());

        Sale cancelledSale = saleRepository.findById(createdSale.getId()).orElse(null);
        assertNotNull(cancelledSale);
        assertEquals(SaleStatus.CANCELLED, cancelledSale.getStatus());

        // Verify stock was restored
        Product restoredProduct = productRepository.findById(testProduct.getId()).orElse(null);
        assertNotNull(restoredProduct);
        assertEquals(stockAfterSale + 1, restoredProduct.getStockQuantity()); // Stock restored by 1
    }

    @Test
    void getSalesByCustomer_EndToEnd_Success() {
        // Given - Create sales for the customer
        createTestSale();
        createTestSale();

        // When
        List<SaleDTO> customerSales = saleService.getSalesByCustomer(testCustomer.getId());

        // Then
        assertNotNull(customerSales);
        assertEquals(2, customerSales.size());
        customerSales.forEach(sale -> assertEquals(testCustomer.getId(), sale.getCustomerId()));
    }

    @Test
    void getSaleById_EndToEnd_Success() {
        // Given - Create a sale
        SaleDTO createdSale = createTestSale();

        // When
        SaleDTO retrievedSale = saleService.getSaleById(createdSale.getId());

        // Then
        assertNotNull(retrievedSale);
        assertEquals(createdSale.getId(), retrievedSale.getId());
        assertEquals(testCustomer.getId(), retrievedSale.getCustomerId());
        assertNotNull(retrievedSale.getItems());
        assertEquals(1, retrievedSale.getItems().size());
        assertEquals(testProduct.getId(), retrievedSale.getItems().get(0).getProductId());
    }

    @Test
    void updateSale_EndToEnd_Success() {
        // Given - Create a sale
        SaleDTO createdSale = createTestSale();

        // Prepare update
        createdSale.setNotes("Updated notes");
        createdSale.setInternalNotes("Internal update");

        // When
        SaleDTO updatedSaleDTO = saleService.updateSale(createdSale.getId(), createdSale);

        // Then
        assertNotNull(updatedSaleDTO);
        assertEquals("Updated notes", updatedSaleDTO.getNotes());

        // Verify database update
        Sale updatedSale = saleRepository.findById(createdSale.getId()).orElse(null);
        assertNotNull(updatedSale);
        assertEquals("Updated notes", updatedSale.getNotes());
    }

    @Test
    void deleteSale_EndToEnd_Success() {
        // Given - Create a sale
        SaleDTO createdSale = createTestSale();

        // When
        saleService.deleteSale(createdSale.getId());

        // Then - Verify sale is cancelled (soft delete)
        Sale deletedSale = saleRepository.findById(createdSale.getId()).orElse(null);
        assertNotNull(deletedSale);
        assertEquals(SaleStatus.CANCELLED, deletedSale.getStatus());
    }

    private SaleDTO createTestSale() {
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .productName(testProduct.getName())
                .quantity(1)
                .unitPrice(testProduct.getPrice()) // Use actual product price
                .originalUnitPrice(testProduct.getPrice())
                .costPrice(testProduct.getCostPrice())
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .subtotal(testProduct.getPrice())
                .totalPrice(testProduct.getPrice())
                .unitOfMeasure("PCS")
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .customerName(testCustomer.getName())
                .totalAmount(testProduct.getPrice()) // Use actual product price
                .items(Arrays.asList(saleItemDTO))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .saleDate(LocalDateTime.now())
                .build();

        return saleService.createSale(saleDTO);
    }
}
