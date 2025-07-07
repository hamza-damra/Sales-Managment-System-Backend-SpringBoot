package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceEnhancedTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private SaleService saleService;

    private Customer testCustomer;
    private Product testProduct;
    private Sale testSale;
    private SaleDTO testSaleDTO;
    private SaleItemDTO testSaleItemDTO;

    @BeforeEach
    void setUp() {
        // Setup test customer with enhanced attributes
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("+1234567890")
                .billingAddress("123 Main St, City, State")
                .shippingAddress("456 Oak Ave, City, State")
                .loyaltyPoints(500)
                .totalPurchases(new BigDecimal("2500.00"))
                .customerType(Customer.CustomerType.VIP)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .build();

        // Setup test product with enhanced attributes
        testProduct = Product.builder()
                .id(1L)
                .name("Premium Smartphone")
                .sku("PHONE-001")
                .price(new BigDecimal("999.99"))
                .costPrice(new BigDecimal("600.00"))
                .stockQuantity(50)
                .minStockLevel(10)
                .reorderPoint(15)
                .isTaxable(true)
                .taxRate(new BigDecimal("15.0"))
                .unitOfMeasure("PCS")
                .build();

        // Setup test sale item DTO with enhanced attributes
        testSaleItemDTO = SaleItemDTO.builder()
                .productId(1L)
                .productName("Premium Smartphone")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .originalUnitPrice(new BigDecimal("999.99"))
                .costPrice(new BigDecimal("600.00"))
                .discountPercentage(new BigDecimal("5.0"))
                .discountAmount(new BigDecimal("99.999"))
                .taxPercentage(new BigDecimal("15.0"))
                .taxAmount(new BigDecimal("285.00"))
                .subtotal(new BigDecimal("1999.98"))
                .totalPrice(new BigDecimal("2184.98"))
                .unitOfMeasure("PCS")
                .warrantyInfo("2-year manufacturer warranty")
                .build();

        // Setup test sale DTO with enhanced attributes
        testSaleDTO = SaleDTO.builder()
                .customerId(1L)
                .customerName("John Doe")
                .saleDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("2184.98"))
                .status(SaleStatus.PENDING)
                .items(Arrays.asList(testSaleItemDTO))
                .subtotal(new BigDecimal("1999.98"))
                .discountAmount(new BigDecimal("99.999"))
                .discountPercentage(new BigDecimal("5.0"))
                .taxAmount(new BigDecimal("285.00"))
                .taxPercentage(new BigDecimal("15.0"))
                .shippingCost(BigDecimal.ZERO)
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .billingAddress("123 Main St, City, State")
                .shippingAddress("456 Oak Ave, City, State")
                .salesPerson("Sales Rep 1")
                .salesChannel("IN_STORE")
                .saleType(Sale.SaleType.RETAIL)
                .currency("USD")
                .exchangeRate(new BigDecimal("1.0"))
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .isGift(false)
                .loyaltyPointsEarned(218)
                .loyaltyPointsUsed(0)
                .isReturn(false)
                .dueDate(LocalDate.now().plusDays(30))
                .notes("VIP customer sale")
                .build();

        // Setup test sale entity
        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("2184.98"))
                .status(SaleStatus.PENDING)
                .saleNumber("SALE-2025-000001")
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .build();
    }

    @Test
    void createComprehensiveSale_WithAllFeatures_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        assertEquals(testSaleDTO.getCustomerId(), result.getCustomerId());
        assertEquals(testSaleDTO.getTotalAmount(), result.getTotalAmount());
        verify(customerRepository).findById(1L);
        verify(productRepository).save(any(Product.class)); // Verify stock update
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void createSale_WithInsufficientStock_ThrowsException() {
        // Given
        testProduct.setStockQuantity(1); // Less than requested quantity
        testSaleItemDTO.setQuantity(5); // Request more than available

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        // Note: No need to mock productService.reduceStock() as exception is thrown earlier

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            saleService.createSale(testSaleDTO);
        });

        // Verify the exception details
        assertEquals("Premium Smartphone", exception.getProductName());
        assertEquals(Integer.valueOf(1), exception.getAvailableStock());
        assertEquals(Integer.valueOf(5), exception.getRequestedQuantity());

        // Verify repository interactions
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        // productService.reduceStock() should NOT be called since exception is thrown earlier
        verify(productService, never()).reduceStock(anyLong(), anyInt());
    }

    @Test
    void createSale_WithSufficientStock_CallsReduceStock() {
        // Given
        testProduct.setStockQuantity(10); // More than requested quantity
        testSaleItemDTO.setQuantity(5); // Request less than available

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createSale(testSaleDTO);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        // productService.reduceStock() SHOULD be called when there's sufficient stock
        verify(productService).reduceStock(1L, 5);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void createSale_WithLoyaltyPointsCalculation_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        // Verify loyalty points calculation (1 point per $10 spent)
        int expectedPoints = testSaleDTO.getTotalAmount().divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();
        assertEquals(expectedPoints, testSaleDTO.getLoyaltyPointsEarned());
    }

    @Test
    void createSale_WithDiscountAndTax_CalculatesCorrectly() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        // Verify calculations
        assertEquals(new BigDecimal("1999.98"), testSaleItemDTO.getSubtotal());
        assertEquals(new BigDecimal("99.999"), testSaleItemDTO.getDiscountAmount());
        assertEquals(new BigDecimal("285.00"), testSaleItemDTO.getTaxAmount());
    }

    @Test
    void updatePaymentInfo_Success() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.updatePaymentInfo(1L, Sale.PaymentMethod.CASH, Sale.PaymentStatus.PAID);

        // Then
        assertNotNull(result);
        assertEquals(Sale.PaymentMethod.CASH, testSale.getPaymentMethod());
        assertEquals(Sale.PaymentStatus.PAID, testSale.getPaymentStatus());
        verify(saleRepository).findById(1L);
        verify(saleRepository).save(testSale);
    }

    @Test
    void updateDeliveryInfo_Success() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.updateDeliveryInfo(1L, Sale.DeliveryStatus.SHIPPED, "TRACK123456");

        // Then
        assertNotNull(result);
        assertEquals(Sale.DeliveryStatus.SHIPPED, testSale.getDeliveryStatus());
        assertEquals("TRACK123456", testSale.getTrackingNumber());
        verify(saleRepository).findById(1L);
        verify(saleRepository).save(testSale);
    }

    @Test
    void updateDeliveryInfo_WhenDelivered_SetsDeliveryDate() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.updateDeliveryInfo(1L, Sale.DeliveryStatus.DELIVERED, "TRACK123456");

        // Then
        assertNotNull(result);
        assertEquals(Sale.DeliveryStatus.DELIVERED, testSale.getDeliveryStatus());
        assertNotNull(testSale.getDeliveryDate());
        verify(saleRepository).save(testSale);
    }

    @Test
    void createSale_WithGiftOptions_Success() {
        // Given
        testSaleDTO.setIsGift(true);
        testSaleDTO.setGiftMessage("Happy Birthday!");
        
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        assertTrue(testSaleDTO.getIsGift());
        assertEquals("Happy Birthday!", testSaleDTO.getGiftMessage());
    }

    @Test
    void createSale_WithMultipleCurrencies_Success() {
        // Given
        testSaleDTO.setCurrency("EUR");
        testSaleDTO.setExchangeRate(new BigDecimal("0.85"));
        
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        assertEquals("EUR", testSaleDTO.getCurrency());
        assertEquals(new BigDecimal("0.85"), testSaleDTO.getExchangeRate());
    }

    @Test
    void updateSale_CompletedSale_ThrowsException() {
        // Given
        testSale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> saleService.updateSale(1L, testSaleDTO));
        assertTrue(exception.getMessage().contains("Cannot update completed or cancelled sales"));
        verify(saleRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void getHighValueSales_Success() {
        // Given
        List<Sale> highValueSales = Arrays.asList(testSale);
        when(saleRepository.findHighValueSales(new BigDecimal("1000.00"))).thenReturn(highValueSales);

        // When
        List<SaleDTO> result = saleService.getHighValueSales(new BigDecimal("1000.00"));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(saleRepository).findHighValueSales(new BigDecimal("1000.00"));
    }

    @Test
    void createSale_WithSerializedProduct_Success() {
        // Given
        testProduct.setIsSerialized(true);
        testSaleItemDTO.setSerialNumbers("SN001,SN002");
        
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createComprehensiveSale(testSaleDTO);

        // Then
        assertNotNull(result);
        assertEquals("SN001,SN002", testSaleItemDTO.getSerialNumbers());
    }
}
