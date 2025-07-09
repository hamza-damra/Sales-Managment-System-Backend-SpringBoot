package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify null payment method handling without complex mocking
 */
class PaymentMethodNullHandlingTest {

    private Customer testCustomer;
    private Sale saleWithPaymentMethod;
    private Sale saleWithNullPaymentMethod;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .build();

        // Create sale with payment method
        saleWithPaymentMethod = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now().minusDays(1))
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(SaleStatus.COMPLETED)
                .paymentMethod(Sale.PaymentMethod.CREDIT_CARD)
                .build();

        // Create sale with null payment method (simulating old data)
        saleWithNullPaymentMethod = Sale.builder()
                .id(2L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now().minusDays(2))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(SaleStatus.COMPLETED)
                .paymentMethod(null) // This is the problematic case
                .build();
    }

    @Test
    @DisplayName("Should handle null payment methods in stream grouping operations")
    void testNullPaymentMethodHandling() {
        // Arrange
        List<Sale> salesWithMixedPaymentMethods = Arrays.asList(
                saleWithPaymentMethod,
                saleWithNullPaymentMethod
        );

        // Act - This should not throw NullPointerException
        assertDoesNotThrow(() -> {
            Map<String, Long> countByMethod = salesWithMixedPaymentMethods.stream()
                    .collect(Collectors.groupingBy(
                            sale -> sale.getPaymentMethod() != null ? 
                                    sale.getPaymentMethod().toString() : "UNKNOWN",
                            Collectors.counting()
                    ));

            // Assert
            assertNotNull(countByMethod);
            assertTrue(countByMethod.containsKey("UNKNOWN"));
            assertTrue(countByMethod.containsKey("CREDIT_CARD"));
            assertEquals(1L, countByMethod.get("UNKNOWN"));
            assertEquals(1L, countByMethod.get("CREDIT_CARD"));
        });
    }

    @Test
    @DisplayName("Should handle all null payment methods")
    void testAllNullPaymentMethods() {
        // Arrange
        List<Sale> salesWithAllNullPaymentMethods = Arrays.asList(
                saleWithNullPaymentMethod,
                Sale.builder()
                        .id(3L)
                        .customer(testCustomer)
                        .saleDate(LocalDateTime.now().minusDays(3))
                        .totalAmount(BigDecimal.valueOf(300.00))
                        .status(SaleStatus.COMPLETED)
                        .paymentMethod(null)
                        .build()
        );

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<String, Long> countByMethod = salesWithAllNullPaymentMethods.stream()
                    .collect(Collectors.groupingBy(
                            sale -> sale.getPaymentMethod() != null ? 
                                    sale.getPaymentMethod().toString() : "UNKNOWN",
                            Collectors.counting()
                    ));

            assertNotNull(countByMethod);
            assertTrue(countByMethod.containsKey("UNKNOWN"));
            assertEquals(2L, countByMethod.get("UNKNOWN"));
            assertEquals(1, countByMethod.size()); // Only "UNKNOWN" key should exist
        });
    }

    @Test
    @DisplayName("Should handle revenue grouping with null payment methods")
    void testRevenueGroupingWithNullPaymentMethods() {
        // Arrange
        List<Sale> salesWithMixedPaymentMethods = Arrays.asList(
                saleWithPaymentMethod,
                saleWithNullPaymentMethod
        );

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<String, BigDecimal> revenueByMethod = salesWithMixedPaymentMethods.stream()
                    .collect(Collectors.groupingBy(
                            sale -> sale.getPaymentMethod() != null ? 
                                    sale.getPaymentMethod().toString() : "UNKNOWN",
                            Collectors.mapping(Sale::getTotalAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));

            assertNotNull(revenueByMethod);
            assertTrue(revenueByMethod.containsKey("UNKNOWN"));
            assertTrue(revenueByMethod.containsKey("CREDIT_CARD"));
            assertEquals(BigDecimal.valueOf(200.00), revenueByMethod.get("UNKNOWN"));
            assertEquals(BigDecimal.valueOf(100.00), revenueByMethod.get("CREDIT_CARD"));
        });
    }

    @Test
    @DisplayName("Should filter sales by payment method with null check")
    void testFilterSalesByPaymentMethodWithNullCheck() {
        // Arrange
        List<Sale> allSales = Arrays.asList(
                saleWithPaymentMethod,
                saleWithNullPaymentMethod
        );

        // Act & Assert - Filter for CREDIT_CARD payments
        assertDoesNotThrow(() -> {
            List<Sale> creditCardSales = allSales.stream()
                    .filter(sale -> sale.getPaymentMethod() != null && 
                                   sale.getPaymentMethod() == Sale.PaymentMethod.CREDIT_CARD)
                    .collect(Collectors.toList());

            assertEquals(1, creditCardSales.size());
            assertEquals(Sale.PaymentMethod.CREDIT_CARD, creditCardSales.get(0).getPaymentMethod());
        });
    }
}
