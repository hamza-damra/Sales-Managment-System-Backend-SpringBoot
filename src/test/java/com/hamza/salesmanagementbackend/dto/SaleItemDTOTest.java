package com.hamza.salesmanagementbackend.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class SaleItemDTOTest {

    private SaleItemDTO saleItemDTO;

    @BeforeEach
    void setUp() {
        saleItemDTO = SaleItemDTO.builder()
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .originalUnitPrice(new BigDecimal("100.00"))
                .costPrice(new BigDecimal("60.00"))
                .discountPercentage(null)
                .discountAmount(null)
                .taxPercentage(null)
                .taxAmount(null)
                .unitOfMeasure("PCS")
                .isReturned(false)
                .returnedQuantity(0)
                .build();
    }

    @Test
    void calculateTotals_WithBasicValues_CalculatesCorrectly() {
        // Given
        saleItemDTO.setQuantity(3);
        saleItemDTO.setUnitPrice(new BigDecimal("50.00"));
        saleItemDTO.setDiscountPercentage(BigDecimal.ZERO);
        saleItemDTO.setTaxPercentage(new BigDecimal("10.0"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("150.00"), saleItemDTO.getSubtotal()); // 3 * 50
        assertEquals(new BigDecimal("0.00"), saleItemDTO.getDiscountAmount()); // Zero with 2 decimal places
        assertEquals(new BigDecimal("15.00"), saleItemDTO.getTaxAmount()); // 150 * 10%
        assertEquals(new BigDecimal("165.00"), saleItemDTO.getTotalPrice()); // 150 + 15
    }

    @Test
    void calculateTotals_WithDiscountPercentage_CalculatesCorrectly() {
        // Given
        saleItemDTO.setQuantity(2);
        saleItemDTO.setUnitPrice(new BigDecimal("100.00"));
        saleItemDTO.setDiscountPercentage(new BigDecimal("20.0"));
        saleItemDTO.setTaxPercentage(new BigDecimal("15.0"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("200.00"), saleItemDTO.getSubtotal()); // 2 * 100
        assertEquals(new BigDecimal("40.00"), saleItemDTO.getDiscountAmount()); // 200 * 20%
        assertEquals(new BigDecimal("24.00"), saleItemDTO.getTaxAmount()); // (200-40) * 15%
        assertEquals(new BigDecimal("184.00"), saleItemDTO.getTotalPrice()); // 200 - 40 + 24
    }

    @Test
    void calculateTotals_WithFixedDiscountAmount_CalculatesCorrectly() {
        // Given
        saleItemDTO.setQuantity(1);
        saleItemDTO.setUnitPrice(new BigDecimal("100.00"));
        saleItemDTO.setDiscountPercentage(BigDecimal.ZERO);
        saleItemDTO.setDiscountAmount(new BigDecimal("25.00"));
        saleItemDTO.setTaxPercentage(new BigDecimal("10.0"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("100.00"), saleItemDTO.getSubtotal()); // 1 * 100
        assertEquals(new BigDecimal("25.00"), saleItemDTO.getDiscountAmount()); // Fixed amount
        assertEquals(new BigDecimal("7.50"), saleItemDTO.getTaxAmount()); // (100-25) * 10%
        assertEquals(new BigDecimal("82.50"), saleItemDTO.getTotalPrice()); // 100 - 25 + 7.5
    }

    @Test
    void calculateTotals_WithNullValues_HandlesGracefully() {
        // Given
        saleItemDTO.setQuantity(2);
        saleItemDTO.setUnitPrice(new BigDecimal("50.00"));
        saleItemDTO.setDiscountPercentage(null);
        saleItemDTO.setDiscountAmount(null);
        saleItemDTO.setTaxPercentage(null);
        saleItemDTO.setTaxAmount(null);

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("100.00"), saleItemDTO.getSubtotal()); // 2 * 50
        assertEquals(new BigDecimal("0.00"), saleItemDTO.getDiscountAmount()); // Default to zero with 2 decimal places
        assertEquals(new BigDecimal("0.00"), saleItemDTO.getTaxAmount()); // Default to zero with 2 decimal places
        assertEquals(new BigDecimal("100.00"), saleItemDTO.getTotalPrice()); // 100 - 0 + 0
    }

    @Test
    void calculateTotals_WithZeroQuantity_DoesNotCalculate() {
        // Given
        saleItemDTO.setQuantity(null);
        saleItemDTO.setUnitPrice(new BigDecimal("100.00"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertNull(saleItemDTO.getSubtotal());
    }

    @Test
    void calculateTotals_WithZeroUnitPrice_DoesNotCalculate() {
        // Given
        saleItemDTO.setQuantity(2);
        saleItemDTO.setUnitPrice(null);

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertNull(saleItemDTO.getSubtotal());
    }

    @Test
    void getLineTotal_WithValidTotalPrice_ReturnsCorrectValue() {
        // Given
        saleItemDTO.setTotalPrice(new BigDecimal("150.00"));

        // When
        BigDecimal lineTotal = saleItemDTO.getLineTotal();

        // Then
        assertEquals(new BigDecimal("150.00"), lineTotal);
    }

    @Test
    void getLineTotal_WithNullTotalPrice_ReturnsZero() {
        // Given
        saleItemDTO.setTotalPrice(null);

        // When
        BigDecimal lineTotal = saleItemDTO.getLineTotal();

        // Then
        assertEquals(new BigDecimal("0.00"), lineTotal); // Zero with 2 decimal places
    }

    @Test
    void constructor_WithBasicParameters_SetsValuesAndCalculates() {
        // When
        SaleItemDTO newSaleItem = new SaleItemDTO(1L, "Product", 3, new BigDecimal("75.00"));

        // Then
        assertEquals(1L, newSaleItem.getProductId());
        assertEquals("Product", newSaleItem.getProductName());
        assertEquals(3, newSaleItem.getQuantity());
        assertEquals(new BigDecimal("75.00"), newSaleItem.getUnitPrice());
        assertEquals(new BigDecimal("75.00"), newSaleItem.getOriginalUnitPrice());
        assertEquals(BigDecimal.ZERO, newSaleItem.getDiscountPercentage()); // Percentages remain as ZERO
        assertEquals(new BigDecimal("0.00"), newSaleItem.getDiscountAmount()); // Amounts have 2 decimal places
        assertEquals(BigDecimal.ZERO, newSaleItem.getTaxPercentage()); // Percentages remain as ZERO
        assertEquals(new BigDecimal("0.00"), newSaleItem.getTaxAmount()); // Amounts have 2 decimal places
        assertFalse(newSaleItem.getIsReturned());
        assertEquals(0, newSaleItem.getReturnedQuantity());
        assertEquals("PCS", newSaleItem.getUnitOfMeasure());
        // Verify calculations were performed
        assertEquals(new BigDecimal("225.00"), newSaleItem.getSubtotal()); // 3 * 75
        assertEquals(new BigDecimal("225.00"), newSaleItem.getTotalPrice()); // No tax or discount
    }

    @Test
    void builderPattern_CreatesValidSaleItemDTO() {
        // When
        SaleItemDTO builtSaleItem = SaleItemDTO.builder()
                .productId(2L)
                .productName("Built Product")
                .quantity(4)
                .unitPrice(new BigDecimal("25.00"))
                .discountPercentage(new BigDecimal("5.0"))
                .taxPercentage(new BigDecimal("8.0"))
                .unitOfMeasure("KG")
                .build();

        // When
        builtSaleItem.calculateTotals();

        // Then
        assertEquals(2L, builtSaleItem.getProductId());
        assertEquals("Built Product", builtSaleItem.getProductName());
        assertEquals(4, builtSaleItem.getQuantity());
        assertEquals(new BigDecimal("25.00"), builtSaleItem.getUnitPrice());
        assertEquals("KG", builtSaleItem.getUnitOfMeasure());
        assertEquals(new BigDecimal("100.00"), builtSaleItem.getSubtotal()); // 4 * 25
        assertEquals(new BigDecimal("5.00"), builtSaleItem.getDiscountAmount()); // 100 * 5%
        assertEquals(new BigDecimal("7.60"), builtSaleItem.getTaxAmount()); // (100-5) * 8%
        assertEquals(new BigDecimal("102.60"), builtSaleItem.getTotalPrice()); // 100 - 5 + 7.6
    }

    @Test
    void defaultValues_AreSetCorrectly() {
        // When
        SaleItemDTO newSaleItem = SaleItemDTO.builder()
                .productId(1L)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        // Then
        assertEquals(BigDecimal.ZERO, newSaleItem.getDiscountPercentage()); // Percentages remain as ZERO
        assertEquals(BigDecimal.ZERO, newSaleItem.getDiscountAmount()); // Builder defaults to ZERO (not calculated)
        assertEquals(BigDecimal.ZERO, newSaleItem.getTaxPercentage()); // Percentages remain as ZERO
        assertEquals(BigDecimal.ZERO, newSaleItem.getTaxAmount()); // Builder defaults to ZERO (not calculated)
        assertFalse(newSaleItem.getIsReturned());
        assertEquals(0, newSaleItem.getReturnedQuantity());
        assertEquals("PCS", newSaleItem.getUnitOfMeasure());
    }

    @Test
    void calculateTotals_WithComplexScenario_CalculatesCorrectly() {
        // Given - Complex scenario with multiple items, discount, and tax
        saleItemDTO.setQuantity(5);
        saleItemDTO.setUnitPrice(new BigDecimal("199.99"));
        saleItemDTO.setDiscountPercentage(new BigDecimal("12.5"));
        saleItemDTO.setTaxPercentage(new BigDecimal("13.5"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("999.95"), saleItemDTO.getSubtotal()); // 5 * 199.99
        assertEquals(new BigDecimal("124.99"), saleItemDTO.getDiscountAmount()); // 999.95 * 12.5% (rounded)
        assertEquals(new BigDecimal("118.12"), saleItemDTO.getTaxAmount()); // (999.95-124.99) * 13.5% (rounded)
        assertEquals(new BigDecimal("993.08"), saleItemDTO.getTotalPrice()); // 999.95 - 124.99 + 118.12
    }

    @Test
    void calculateTotals_WithHighPrecisionValues_RoundsCorrectly() {
        // Given
        saleItemDTO.setQuantity(3);
        saleItemDTO.setUnitPrice(new BigDecimal("33.333"));
        saleItemDTO.setDiscountPercentage(new BigDecimal("7.777"));
        saleItemDTO.setTaxPercentage(new BigDecimal("11.111"));

        // When
        saleItemDTO.calculateTotals();

        // Then
        assertEquals(new BigDecimal("99.999"), saleItemDTO.getSubtotal()); // 3 * 33.333
        // Verify rounding to 2 decimal places
        assertEquals(2, saleItemDTO.getDiscountAmount().scale());
        assertEquals(2, saleItemDTO.getTaxAmount().scale());
        assertEquals(2, saleItemDTO.getTotalPrice().scale());
    }

    @Test
    void returnedQuantityLogic_WorksCorrectly() {
        // Given
        saleItemDTO.setQuantity(10);
        saleItemDTO.setReturnedQuantity(3);
        saleItemDTO.setIsReturned(true);

        // When & Then
        assertTrue(saleItemDTO.getIsReturned());
        assertEquals(3, saleItemDTO.getReturnedQuantity());
        assertEquals(7, saleItemDTO.getQuantity() - saleItemDTO.getReturnedQuantity()); // Remaining quantity
    }

    @Test
    void serialNumbersAndWarranty_AreHandledCorrectly() {
        // Given
        saleItemDTO.setSerialNumbers("SN001,SN002,SN003");
        saleItemDTO.setWarrantyInfo("2-year manufacturer warranty");
        saleItemDTO.setNotes("Special handling required");

        // When & Then
        assertEquals("SN001,SN002,SN003", saleItemDTO.getSerialNumbers());
        assertEquals("2-year manufacturer warranty", saleItemDTO.getWarrantyInfo());
        assertEquals("Special handling required", saleItemDTO.getNotes());
    }

    @Test
    void validation_RequiredFields_AreNotNull() {
        // Given & When & Then
        assertNotNull(saleItemDTO.getProductId());
        assertNotNull(saleItemDTO.getQuantity());
        assertNotNull(saleItemDTO.getUnitPrice());
        assertTrue(saleItemDTO.getQuantity() > 0);
        assertTrue(saleItemDTO.getUnitPrice().compareTo(BigDecimal.ZERO) > 0);
    }
}
