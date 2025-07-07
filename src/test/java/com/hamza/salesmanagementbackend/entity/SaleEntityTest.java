package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SaleEntityTest {

    private Sale sale;
    private Customer customer;
    private Product product;
    private SaleItem saleItem1;
    private SaleItem saleItem2;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .loyaltyPoints(100)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .stockQuantity(50)
                .build();

        saleItem1 = SaleItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .totalPrice(new BigDecimal("200.00"))
                .build();

        saleItem2 = SaleItem.builder()
                .id(2L)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("50.00"))
                .build();

        sale = Sale.builder()
                .id(1L)
                .customer(customer)
                .saleDate(LocalDateTime.now())
                .status(SaleStatus.PENDING)
                .totalAmount(new BigDecimal("250.00"))
                .subtotal(new BigDecimal("250.00"))
                .discountAmount(BigDecimal.ZERO)
                .discountPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .isGift(false)
                .loyaltyPointsEarned(0)
                .loyaltyPointsUsed(0)
                .isReturn(false)
                .profitMargin(BigDecimal.ZERO)
                .costOfGoodsSold(BigDecimal.ZERO)
                .items(Arrays.asList(saleItem1, saleItem2))
                .build();
    }

    @Test
    void calculateTotals_WithItems_CalculatesCorrectly() {
        // Given
        sale.setItems(Arrays.asList(saleItem1, saleItem2));
        sale.setDiscountAmount(new BigDecimal("25.00"));
        sale.setTaxAmount(new BigDecimal("22.50"));
        sale.setShippingCost(new BigDecimal("10.00"));

        // When
        sale.calculateTotals();

        // Then
        assertEquals(new BigDecimal("250.00"), sale.getSubtotal()); // 200 + 50
        assertEquals(new BigDecimal("257.50"), sale.getTotalAmount()); // 250 - 25 + 22.50 + 10
    }

    @Test
    void calculateTotals_WithNoItems_DoesNotCalculate() {
        // Given
        sale.setItems(null);
        BigDecimal originalTotal = sale.getTotalAmount();

        // When
        sale.calculateTotals();

        // Then
        assertEquals(originalTotal, sale.getTotalAmount());
    }

    @Test
    void processLoyaltyPoints_WithValidCustomerAndAmount_CalculatesPoints() {
        // Given
        sale.setTotalAmount(new BigDecimal("150.00"));
        sale.setCustomer(customer);

        // When
        sale.processLoyaltyPoints();

        // Then
        assertEquals(15, sale.getLoyaltyPointsEarned()); // 150 / 10 = 15 points
        assertEquals(115, customer.getLoyaltyPoints()); // 100 + 15
    }

    @Test
    void processLoyaltyPoints_WithZeroAmount_NoPointsEarned() {
        // Given
        sale.setTotalAmount(new BigDecimal("5.00")); // Less than $10
        sale.setCustomer(customer);

        // When
        sale.processLoyaltyPoints();

        // Then
        assertEquals(0, sale.getLoyaltyPointsEarned());
        assertEquals(100, customer.getLoyaltyPoints()); // Unchanged
    }

    @Test
    void processLoyaltyPoints_WithNullCustomer_DoesNotProcess() {
        // Given
        sale.setCustomer(null);
        sale.setTotalAmount(new BigDecimal("100.00"));

        // When
        sale.processLoyaltyPoints();

        // Then
        assertEquals(0, sale.getLoyaltyPointsEarned());
    }

    @Test
    void markAsPaid_UpdatesPaymentStatusAndDate() {
        // Given
        assertNull(sale.getPaymentDate());
        assertEquals(Sale.PaymentStatus.PENDING, sale.getPaymentStatus());

        // When
        sale.markAsPaid();

        // Then
        assertEquals(Sale.PaymentStatus.PAID, sale.getPaymentStatus());
        assertNotNull(sale.getPaymentDate());
        assertTrue(sale.getPaymentDate().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void isPaid_WhenPaid_ReturnsTrue() {
        // Given
        sale.setPaymentStatus(Sale.PaymentStatus.PAID);

        // When & Then
        assertTrue(sale.isPaid());
    }

    @Test
    void isPaid_WhenNotPaid_ReturnsFalse() {
        // Given
        sale.setPaymentStatus(Sale.PaymentStatus.PENDING);

        // When & Then
        assertFalse(sale.isPaid());
    }

    @Test
    void isOverdue_WhenPastDueAndNotPaid_ReturnsTrue() {
        // Given
        sale.setDueDate(LocalDate.now().minusDays(1)); // Past due
        sale.setPaymentStatus(Sale.PaymentStatus.PENDING);

        // When & Then
        assertTrue(sale.isOverdue());
    }

    @Test
    void isOverdue_WhenPastDueButPaid_ReturnsFalse() {
        // Given
        sale.setDueDate(LocalDate.now().minusDays(1)); // Past due
        sale.setPaymentStatus(Sale.PaymentStatus.PAID);

        // When & Then
        assertFalse(sale.isOverdue());
    }

    @Test
    void isOverdue_WhenNotPastDue_ReturnsFalse() {
        // Given
        sale.setDueDate(LocalDate.now().plusDays(1)); // Future due date
        sale.setPaymentStatus(Sale.PaymentStatus.PENDING);

        // When & Then
        assertFalse(sale.isOverdue());
    }

    @Test
    void isOverdue_WhenNoDueDate_ReturnsFalse() {
        // Given
        sale.setDueDate(null);
        sale.setPaymentStatus(Sale.PaymentStatus.PENDING);

        // When & Then
        assertFalse(sale.isOverdue());
    }

    @Test
    void generateSaleNumber_CreatesUniqueNumber() {
        // Given
        Sale sale1 = new Sale(customer);
        Sale sale2 = new Sale(customer);

        // When & Then
        assertNotNull(sale1.getSaleNumber());
        assertNotNull(sale2.getSaleNumber());
        assertNotEquals(sale1.getSaleNumber(), sale2.getSaleNumber());
        assertTrue(sale1.getSaleNumber().startsWith("SALE-"));
        assertTrue(sale2.getSaleNumber().startsWith("SALE-"));

        // Verify the format: SALE-{timestamp}-{counter}-{random}
        assertTrue(sale1.getSaleNumber().matches("SALE-\\d+-\\d+-[A-Z0-9]{4}"));
        assertTrue(sale2.getSaleNumber().matches("SALE-\\d+-\\d+-[A-Z0-9]{4}"));

        // Verify that sale numbers are truly unique even when created quickly
        Sale sale3 = new Sale(customer);
        Sale sale4 = new Sale(customer);
        Sale sale5 = new Sale(customer);

        assertNotEquals(sale1.getSaleNumber(), sale3.getSaleNumber());
        assertNotEquals(sale2.getSaleNumber(), sale4.getSaleNumber());
        assertNotEquals(sale3.getSaleNumber(), sale5.getSaleNumber());
        assertNotEquals(sale4.getSaleNumber(), sale5.getSaleNumber());
    }

    @Test
    void constructor_WithCustomer_SetsDefaultValues() {
        // When
        Sale newSale = new Sale(customer);

        // Then
        assertEquals(customer, newSale.getCustomer());
        assertEquals(SaleStatus.PENDING, newSale.getStatus());
        assertEquals(BigDecimal.ZERO, newSale.getTotalAmount());
        assertEquals(BigDecimal.ZERO, newSale.getSubtotal());
        assertEquals(BigDecimal.ZERO, newSale.getDiscountAmount());
        assertEquals(BigDecimal.ZERO, newSale.getTaxAmount());
        assertEquals(Sale.PaymentStatus.PENDING, newSale.getPaymentStatus());
        assertEquals(Sale.SaleType.RETAIL, newSale.getSaleType());
        assertEquals("USD", newSale.getCurrency());
        assertEquals(BigDecimal.ONE, newSale.getExchangeRate());
        assertEquals(Sale.DeliveryStatus.NOT_SHIPPED, newSale.getDeliveryStatus());
        assertFalse(newSale.getIsGift());
        assertFalse(newSale.getIsReturn());
        assertNotNull(newSale.getSaleNumber());
        assertNotNull(newSale.getSaleDate());
    }

    @Test
    void constructor_WithCustomerAndPaymentDetails_SetsValues() {
        // When
        Sale newSale = new Sale(customer, Sale.PaymentMethod.CREDIT_CARD, Sale.SaleType.WHOLESALE);

        // Then
        assertEquals(customer, newSale.getCustomer());
        assertEquals(Sale.PaymentMethod.CREDIT_CARD, newSale.getPaymentMethod());
        assertEquals(Sale.SaleType.WHOLESALE, newSale.getSaleType());
        assertEquals(SaleStatus.PENDING, newSale.getStatus());
    }

    @Test
    void saleEnums_HaveCorrectValues() {
        // Test PaymentMethod enum
        assertNotNull(Sale.PaymentMethod.CASH);
        assertNotNull(Sale.PaymentMethod.CREDIT_CARD);
        assertNotNull(Sale.PaymentMethod.DEBIT_CARD);
        assertNotNull(Sale.PaymentMethod.BANK_TRANSFER);

        // Test PaymentStatus enum
        assertNotNull(Sale.PaymentStatus.PENDING);
        assertNotNull(Sale.PaymentStatus.PAID);
        assertNotNull(Sale.PaymentStatus.PARTIALLY_PAID);
        assertNotNull(Sale.PaymentStatus.OVERDUE);

        // Test SaleType enum
        assertNotNull(Sale.SaleType.RETAIL);
        assertNotNull(Sale.SaleType.WHOLESALE);
        assertNotNull(Sale.SaleType.B2B);
        assertNotNull(Sale.SaleType.ONLINE);

        // Test DeliveryStatus enum
        assertNotNull(Sale.DeliveryStatus.NOT_SHIPPED);
        assertNotNull(Sale.DeliveryStatus.PROCESSING);
        assertNotNull(Sale.DeliveryStatus.SHIPPED);
        assertNotNull(Sale.DeliveryStatus.DELIVERED);
    }

    @Test
    void builderPattern_CreatesValidSale() {
        // When
        Sale builtSale = Sale.builder()
                .customer(customer)
                .totalAmount(new BigDecimal("100.00"))
                .status(SaleStatus.COMPLETED)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .saleType(Sale.SaleType.RETAIL)
                .build();

        // Then
        assertEquals(customer, builtSale.getCustomer());
        assertEquals(new BigDecimal("100.00"), builtSale.getTotalAmount());
        assertEquals(SaleStatus.COMPLETED, builtSale.getStatus());
        assertEquals(Sale.PaymentMethod.CASH, builtSale.getPaymentMethod());
        assertEquals(Sale.SaleType.RETAIL, builtSale.getSaleType());
    }
}
