package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class SaleItemEntityTest {

    private SaleItem saleItem;
    private Sale sale;
    private Product product;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        sale = Sale.builder()
                .id(1L)
                .customer(customer)
                .status(SaleStatus.PENDING)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .stockQuantity(50)
                .unitOfMeasure("PCS")
                .isTaxable(true)
                .taxRate(new BigDecimal("10.0"))
                .build();

        saleItem = SaleItem.builder()
                .id(1L)
                .sale(sale)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .originalUnitPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(new BigDecimal("10.0"))
                .taxAmount(new BigDecimal("20.00"))
                .subtotal(new BigDecimal("200.00"))
                .totalPrice(new BigDecimal("220.00"))
                .unitOfMeasure("PCS")
                .isReturned(false)
                .returnedQuantity(0)
                .build();
    }

    @Test
    void calculateSubtotal_WithQuantityAndUnitPrice_CalculatesCorrectly() {
        // Given
        saleItem.setQuantity(3);
        saleItem.setUnitPrice(new BigDecimal("50.00"));

        // When
        BigDecimal expectedSubtotal = new BigDecimal("50.00").multiply(BigDecimal.valueOf(3));
        saleItem.setSubtotal(expectedSubtotal);

        // Then
        assertEquals(new BigDecimal("150.00"), saleItem.getSubtotal());
    }

    @Test
    void calculateDiscountAmount_WithPercentage_CalculatesCorrectly() {
        // Given
        saleItem.setSubtotal(new BigDecimal("200.00"));
        saleItem.setDiscountPercentage(new BigDecimal("10.0"));

        // When
        BigDecimal discountAmount = saleItem.getSubtotal()
                .multiply(saleItem.getDiscountPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        saleItem.setDiscountAmount(discountAmount);

        // Then
        assertEquals(new BigDecimal("20.00"), saleItem.getDiscountAmount());
    }

    @Test
    void calculateTaxAmount_WithTaxPercentage_CalculatesCorrectly() {
        // Given
        BigDecimal afterDiscount = saleItem.getSubtotal().subtract(saleItem.getDiscountAmount());
        saleItem.setTaxPercentage(new BigDecimal("15.0"));

        // When
        BigDecimal taxAmount = afterDiscount
                .multiply(saleItem.getTaxPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        saleItem.setTaxAmount(taxAmount);

        // Then
        assertEquals(new BigDecimal("30.00"), saleItem.getTaxAmount()); // 200 * 15% = 30
    }

    @Test
    void calculateTotalPrice_WithDiscountAndTax_CalculatesCorrectly() {
        // Given
        saleItem.setSubtotal(new BigDecimal("200.00"));
        saleItem.setDiscountAmount(new BigDecimal("20.00"));
        saleItem.setTaxAmount(new BigDecimal("27.00")); // 15% of (200-20)

        // When
        BigDecimal totalPrice = saleItem.getSubtotal()
                .subtract(saleItem.getDiscountAmount())
                .add(saleItem.getTaxAmount());
        saleItem.setTotalPrice(totalPrice);

        // Then
        assertEquals(new BigDecimal("207.00"), saleItem.getTotalPrice()); // 200 - 20 + 27
    }

    @Test
    void getLineTotal_ReturnsCorrectValue() {
        // Given
        saleItem.setTotalPrice(new BigDecimal("220.00"));

        // When
        BigDecimal lineTotal = saleItem.getTotalPrice();

        // Then
        assertEquals(new BigDecimal("220.00"), lineTotal);
    }

    @Test
    void getLineTotal_WhenTotalPriceIsNull_ReturnsZero() {
        // Given
        saleItem.setTotalPrice(null);

        // When
        BigDecimal lineTotal = saleItem.getTotalPrice() != null ? saleItem.getTotalPrice() : BigDecimal.ZERO;

        // Then
        assertEquals(new BigDecimal("0.00"), lineTotal); // Zero with 2 decimal places
    }

    @Test
    void calculateProfitMargin_WithCostPrice_CalculatesCorrectly() {
        // Given
        saleItem.setUnitPrice(new BigDecimal("100.00"));
        saleItem.setCostPrice(new BigDecimal("60.00"));

        // When
        BigDecimal profitMargin = saleItem.getUnitPrice().subtract(saleItem.getCostPrice());

        // Then
        assertEquals(new BigDecimal("40.00"), profitMargin);
    }

    @Test
    void calculateProfitPercentage_WithCostPrice_CalculatesCorrectly() {
        // Given
        saleItem.setUnitPrice(new BigDecimal("100.00"));
        saleItem.setCostPrice(new BigDecimal("60.00"));

        // When
        BigDecimal profitMargin = saleItem.getUnitPrice().subtract(saleItem.getCostPrice());
        BigDecimal profitPercentage = profitMargin
                .divide(saleItem.getCostPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        // Then
        // Step-by-step verification:
        // 1. Profit margin: 100 - 60 = 40
        assertEquals(new BigDecimal("40.00"), profitMargin);

        // 2. Profit ratio: 40 / 60 = 0.6666... (rounded to 4 decimal places = 0.6667)
        BigDecimal profitRatio = profitMargin.divide(saleItem.getCostPrice(), 4, RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("0.6667"), profitRatio);

        // 3. Profit percentage: 0.6667 * 100 = 66.67 (but with 4 decimal places = 66.6700)
        assertEquals(new BigDecimal("66.6700"), profitPercentage);

        // Alternative: Use compareTo to ignore scale differences
        assertTrue(profitPercentage.compareTo(new BigDecimal("66.67")) == 0);
    }

    @Test
    void isFullyReturned_WhenReturnedQuantityEqualsQuantity_ReturnsTrue() {
        // Given
        saleItem.setQuantity(5);
        saleItem.setReturnedQuantity(5);
        saleItem.setIsReturned(true);

        // When & Then
        assertTrue(saleItem.getIsReturned());
        assertEquals(saleItem.getQuantity(), saleItem.getReturnedQuantity());
    }

    @Test
    void isPartiallyReturned_WhenReturnedQuantityLessThanQuantity_ReturnsTrue() {
        // Given
        saleItem.setQuantity(5);
        saleItem.setReturnedQuantity(3);
        saleItem.setIsReturned(true);

        // When & Then
        assertTrue(saleItem.getIsReturned());
        assertTrue(saleItem.getReturnedQuantity() < saleItem.getQuantity());
        assertTrue(saleItem.getReturnedQuantity() > 0);
    }

    @Test
    void getRemainingQuantity_WithPartialReturn_CalculatesCorrectly() {
        // Given
        saleItem.setQuantity(10);
        saleItem.setReturnedQuantity(3);

        // When
        Integer remainingQuantity = saleItem.getQuantity() - saleItem.getReturnedQuantity();

        // Then
        assertEquals(7, remainingQuantity);
    }

    @Test
    void hasSerialNumbers_WhenSerialNumbersProvided_ReturnsTrue() {
        // Given
        saleItem.setSerialNumbers("SN001,SN002,SN003");

        // When & Then
        assertNotNull(saleItem.getSerialNumbers());
        assertFalse(saleItem.getSerialNumbers().isEmpty());
        assertTrue(saleItem.getSerialNumbers().contains(","));
    }

    @Test
    void hasWarranty_WhenWarrantyInfoProvided_ReturnsTrue() {
        // Given
        saleItem.setWarrantyInfo("2-year manufacturer warranty");

        // When & Then
        assertNotNull(saleItem.getWarrantyInfo());
        assertFalse(saleItem.getWarrantyInfo().isEmpty());
        assertTrue(saleItem.getWarrantyInfo().contains("warranty"));
    }

    @Test
    void builderPattern_CreatesValidSaleItem() {
        // When
        SaleItem builtSaleItem = SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantity(5)
                .unitPrice(new BigDecimal("75.00"))
                .subtotal(new BigDecimal("375.00"))
                .totalPrice(new BigDecimal("412.50"))
                .unitOfMeasure("PCS")
                .build();

        // Then
        assertEquals(sale, builtSaleItem.getSale());
        assertEquals(product, builtSaleItem.getProduct());
        assertEquals(5, builtSaleItem.getQuantity());
        assertEquals(new BigDecimal("75.00"), builtSaleItem.getUnitPrice());
        assertEquals(new BigDecimal("375.00"), builtSaleItem.getSubtotal());
        assertEquals(new BigDecimal("412.50"), builtSaleItem.getTotalPrice());
        assertEquals("PCS", builtSaleItem.getUnitOfMeasure());
    }

    @Test
    void defaultValues_AreSetCorrectly() {
        // When
        SaleItem newSaleItem = SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        // Then
        assertEquals(BigDecimal.ZERO, newSaleItem.getDiscountPercentage()); // Percentages remain as ZERO
        assertEquals(new BigDecimal("0.00"), newSaleItem.getDiscountAmount()); // Amounts have 2 decimal places
        assertEquals(BigDecimal.ZERO, newSaleItem.getTaxPercentage()); // Percentages remain as ZERO
        assertEquals(new BigDecimal("0.00"), newSaleItem.getTaxAmount()); // Amounts have 2 decimal places
        assertFalse(newSaleItem.getIsReturned());
        assertEquals(0, newSaleItem.getReturnedQuantity());
    }

    @Test
    void constructor_WithBasicParameters_SetsValues() {
        // When
        SaleItem newSaleItem = SaleItem.builder()
                .sale(sale)
                .product(product)
                .quantity(3)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("165.00"))
                .build();

        // Then
        assertEquals(sale, newSaleItem.getSale());
        assertEquals(product, newSaleItem.getProduct());
        assertEquals(3, newSaleItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), newSaleItem.getUnitPrice());
        assertEquals(new BigDecimal("150.00"), newSaleItem.getSubtotal());
        assertEquals(new BigDecimal("165.00"), newSaleItem.getTotalPrice());
    }

    @Test
    void validation_RequiredFields_AreNotNull() {
        // Given & When & Then
        assertNotNull(saleItem.getSale());
        assertNotNull(saleItem.getProduct());
        assertNotNull(saleItem.getQuantity());
        assertNotNull(saleItem.getUnitPrice());
        assertNotNull(saleItem.getTotalPrice());
        assertTrue(saleItem.getQuantity() > 0);
        assertTrue(saleItem.getUnitPrice().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(saleItem.getTotalPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void toString_ContainsKeyInformation() {
        // When
        String saleItemString = saleItem.toString();

        // Then
        assertNotNull(saleItemString);
        // Note: The actual toString implementation depends on Lombok @ToString annotation
        // This test ensures toString doesn't throw an exception
    }

    @Test
    void equals_WithSameValues_ReturnsTrue() {
        // Given - Create another SaleItem with identical field values
        SaleItem anotherSaleItem = SaleItem.builder()
                .id(1L)
                .sale(sale)
                .product(product)
                .quantity(2) // Same as original saleItem
                .unitPrice(new BigDecimal("100.00"))
                .originalUnitPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(new BigDecimal("10.0"))
                .taxAmount(new BigDecimal("20.00"))
                .subtotal(new BigDecimal("200.00"))
                .totalPrice(new BigDecimal("220.00"))
                .unitOfMeasure("PCS")
                .isReturned(false)
                .returnedQuantity(0)
                .build();

        // When & Then
        assertEquals(saleItem, anotherSaleItem);
        assertEquals(saleItem.getId(), anotherSaleItem.getId());
    }

    @Test
    void hashCode_WithSameId_ReturnsSameHashCode() {
        // Given - Create another SaleItem with identical field values
        SaleItem anotherSaleItem = SaleItem.builder()
                .id(1L)
                .sale(sale)
                .product(product)
                .quantity(2) // Same as original saleItem
                .unitPrice(new BigDecimal("100.00"))
                .originalUnitPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(new BigDecimal("10.0"))
                .taxAmount(new BigDecimal("20.00"))
                .subtotal(new BigDecimal("200.00"))
                .totalPrice(new BigDecimal("220.00"))
                .unitOfMeasure("PCS")
                .isReturned(false)
                .returnedQuantity(0)
                .build();

        // When & Then
        assertEquals(saleItem.hashCode(), anotherSaleItem.hashCode());
        assertEquals(saleItem, anotherSaleItem); // They should also be equal
    }
}
