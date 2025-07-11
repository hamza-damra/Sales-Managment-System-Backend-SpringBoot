package com.hamza.salesmanagementbackend.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppliedPromotion Entity Tests")
class AppliedPromotionTest {

    private Sale testSale;
    private Promotion testPromotion;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .build();

        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        testPromotion = Promotion.builder()
                .id(1L)
                .name("Test Promotion")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .couponCode("TEST10")
                .build();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create AppliedPromotion with all required fields")
        void testConstructor_AllFields() {
            // Given
            BigDecimal discountAmount = BigDecimal.valueOf(20.00);
            BigDecimal originalAmount = BigDecimal.valueOf(200.00);
            Boolean isAutoApplied = false;

            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, discountAmount, originalAmount, isAutoApplied);

            // Then
            assertNotNull(appliedPromotion);
            assertEquals(testSale, appliedPromotion.getSale());
            assertEquals(testPromotion, appliedPromotion.getPromotion());
            assertEquals("Test Promotion", appliedPromotion.getPromotionName());
            assertEquals(Promotion.PromotionType.PERCENTAGE, appliedPromotion.getPromotionType());
            assertEquals("TEST10", appliedPromotion.getCouponCode());
            assertEquals(discountAmount, appliedPromotion.getDiscountAmount());
            assertEquals(originalAmount, appliedPromotion.getOriginalAmount());
            assertEquals(BigDecimal.valueOf(180.00), appliedPromotion.getFinalAmount());
            assertFalse(appliedPromotion.getIsAutoApplied());
        }

        @Test
        @DisplayName("Should handle null isAutoApplied parameter")
        void testConstructor_NullAutoApplied() {
            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), null);

            // Then
            assertFalse(appliedPromotion.getIsAutoApplied());
        }

        @Test
        @DisplayName("Should calculate discount percentage for percentage promotions")
        void testConstructor_PercentagePromotionDiscountCalculation() {
            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);

            // Then
            assertEquals(BigDecimal.valueOf(10.00), appliedPromotion.getDiscountPercentage());
        }

        @Test
        @DisplayName("Should calculate discount percentage for fixed amount promotions")
        void testConstructor_FixedAmountPromotionDiscountCalculation() {
            // Given
            testPromotion.setType(Promotion.PromotionType.FIXED_AMOUNT);
            testPromotion.setDiscountValue(BigDecimal.valueOf(25.00));

            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(25.00), BigDecimal.valueOf(200.00), false);

            // Then
            assertEquals(BigDecimal.valueOf(12.50).setScale(2), appliedPromotion.getDiscountPercentage().setScale(2));
        }

        @Test
        @DisplayName("Should handle zero original amount gracefully")
        void testConstructor_ZeroOriginalAmount() {
            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(0.00), BigDecimal.valueOf(0.00), false);

            // Then
            assertNotNull(appliedPromotion);
            assertEquals(BigDecimal.valueOf(0.00), appliedPromotion.getOriginalAmount());
            assertEquals(BigDecimal.valueOf(0.00), appliedPromotion.getFinalAmount());
        }
    }

    @Nested
    @DisplayName("Business Logic Methods")
    class BusinessLogicMethods {

        private AppliedPromotion appliedPromotion;

        @BeforeEach
        void setUp() {
            appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);
        }

        @Test
        @DisplayName("Should return correct savings amount")
        void testGetSavingsAmount() {
            // When
            BigDecimal savings = appliedPromotion.getSavingsAmount();

            // Then
            assertEquals(BigDecimal.valueOf(20.00), savings);
        }

        @Test
        @DisplayName("Should return zero savings when discount amount is null")
        void testGetSavingsAmount_NullDiscount() {
            // Given
            appliedPromotion.setDiscountAmount(null);

            // When
            BigDecimal savings = appliedPromotion.getSavingsAmount();

            // Then
            assertEquals(BigDecimal.ZERO, savings);
        }

        @Test
        @DisplayName("Should correctly identify percentage discount")
        void testIsPercentageDiscount() {
            // When & Then
            assertTrue(appliedPromotion.isPercentageDiscount());
        }

        @Test
        @DisplayName("Should correctly identify fixed amount discount")
        void testIsFixedAmountDiscount() {
            // Given
            appliedPromotion.setPromotionType(Promotion.PromotionType.FIXED_AMOUNT);

            // When & Then
            assertTrue(appliedPromotion.isFixedAmountDiscount());
            assertFalse(appliedPromotion.isPercentageDiscount());
        }

        @Test
        @DisplayName("Should generate correct display text for percentage discount")
        void testGetDisplayText_PercentageDiscount() {
            // When
            String displayText = appliedPromotion.getDisplayText();

            // Then
            assertEquals("Test Promotion (10.0% off)", displayText);
        }

        @Test
        @DisplayName("Should generate correct display text for fixed amount discount")
        void testGetDisplayText_FixedAmountDiscount() {
            // Given
            appliedPromotion.setPromotionType(Promotion.PromotionType.FIXED_AMOUNT);
            appliedPromotion.setDiscountPercentage(null);

            // When
            String displayText = appliedPromotion.getDisplayText();

            // Then
            assertEquals("Test Promotion ($20.00 off)", displayText);
        }

        @Test
        @DisplayName("Should generate correct display text when discount percentage is null")
        void testGetDisplayText_NullDiscountPercentage() {
            // Given
            appliedPromotion.setDiscountPercentage(null);

            // When
            String displayText = appliedPromotion.getDisplayText();

            // Then
            assertEquals("Test Promotion ($20.00 off)", displayText);
        }

        @Test
        @DisplayName("Should return correct type display for percentage promotion")
        void testGetTypeDisplay_Percentage() {
            // When
            String typeDisplay = appliedPromotion.getTypeDisplay();

            // Then
            assertEquals("Percentage Discount", typeDisplay);
        }

        @Test
        @DisplayName("Should return correct type display for fixed amount promotion")
        void testGetTypeDisplay_FixedAmount() {
            // Given
            appliedPromotion.setPromotionType(Promotion.PromotionType.FIXED_AMOUNT);

            // When
            String typeDisplay = appliedPromotion.getTypeDisplay();

            // Then
            assertEquals("Fixed Amount Discount", typeDisplay);
        }

        @Test
        @DisplayName("Should return correct type display for Buy X Get Y promotion")
        void testGetTypeDisplay_BuyXGetY() {
            // Given
            appliedPromotion.setPromotionType(Promotion.PromotionType.BUY_X_GET_Y);

            // When
            String typeDisplay = appliedPromotion.getTypeDisplay();

            // Then
            assertEquals("Buy X Get Y", typeDisplay);
        }

        @Test
        @DisplayName("Should return correct type display for Free Shipping promotion")
        void testGetTypeDisplay_FreeShipping() {
            // Given
            appliedPromotion.setPromotionType(Promotion.PromotionType.FREE_SHIPPING);

            // When
            String typeDisplay = appliedPromotion.getTypeDisplay();

            // Then
            assertEquals("Free Shipping", typeDisplay);
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create AppliedPromotion using builder")
        void testBuilder() {
            // Given
            LocalDateTime appliedAt = LocalDateTime.now();

            // When
            AppliedPromotion appliedPromotion = AppliedPromotion.builder()
                    .sale(testSale)
                    .promotion(testPromotion)
                    .promotionName("Test Promotion")
                    .promotionType(Promotion.PromotionType.PERCENTAGE)
                    .couponCode("TEST10")
                    .discountAmount(BigDecimal.valueOf(20.00))
                    .discountPercentage(BigDecimal.valueOf(10.00))
                    .originalAmount(BigDecimal.valueOf(200.00))
                    .finalAmount(BigDecimal.valueOf(180.00))
                    .isAutoApplied(false)
                    .appliedAt(appliedAt)
                    .build();

            // Then
            assertNotNull(appliedPromotion);
            assertEquals(testSale, appliedPromotion.getSale());
            assertEquals(testPromotion, appliedPromotion.getPromotion());
            assertEquals("Test Promotion", appliedPromotion.getPromotionName());
            assertEquals(Promotion.PromotionType.PERCENTAGE, appliedPromotion.getPromotionType());
            assertEquals("TEST10", appliedPromotion.getCouponCode());
            assertEquals(BigDecimal.valueOf(20.00), appliedPromotion.getDiscountAmount());
            assertEquals(BigDecimal.valueOf(10.00), appliedPromotion.getDiscountPercentage());
            assertEquals(BigDecimal.valueOf(200.00), appliedPromotion.getOriginalAmount());
            assertEquals(BigDecimal.valueOf(180.00), appliedPromotion.getFinalAmount());
            assertFalse(appliedPromotion.getIsAutoApplied());
            assertEquals(appliedAt, appliedPromotion.getAppliedAt());
        }

        @Test
        @DisplayName("Should create AppliedPromotion with default values")
        void testBuilder_DefaultValues() {
            // When
            AppliedPromotion appliedPromotion = AppliedPromotion.builder()
                    .sale(testSale)
                    .promotion(testPromotion)
                    .promotionName("Test Promotion")
                    .promotionType(Promotion.PromotionType.PERCENTAGE)
                    .discountAmount(BigDecimal.valueOf(20.00))
                    .build();

            // Then
            assertNotNull(appliedPromotion);
            assertEquals(testSale, appliedPromotion.getSale());
            assertEquals(testPromotion, appliedPromotion.getPromotion());
            assertEquals("Test Promotion", appliedPromotion.getPromotionName());
            assertEquals(BigDecimal.valueOf(20.00), appliedPromotion.getDiscountAmount());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle very large discount amounts")
        void testLargeDiscountAmount() {
            // Given
            BigDecimal largeDiscount = new BigDecimal("999999.99");
            BigDecimal largeOriginal = new BigDecimal("1000000.00");

            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, largeDiscount, largeOriginal, false);

            // Then
            assertEquals(largeDiscount, appliedPromotion.getDiscountAmount());
            assertEquals(largeOriginal, appliedPromotion.getOriginalAmount());
            assertEquals(new BigDecimal("0.01"), appliedPromotion.getFinalAmount());
        }

        @Test
        @DisplayName("Should handle precision in percentage calculations")
        void testPrecisionInPercentageCalculation() {
            // Given
            testPromotion.setType(Promotion.PromotionType.FIXED_AMOUNT);
            BigDecimal discountAmount = new BigDecimal("33.33");
            BigDecimal originalAmount = new BigDecimal("100.00");

            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, discountAmount, originalAmount, false);

            // Then
            assertEquals(new BigDecimal("33.33"), appliedPromotion.getDiscountPercentage());
        }

        @Test
        @DisplayName("Should handle auto-applied promotion correctly")
        void testAutoAppliedPromotion() {
            // When
            AppliedPromotion appliedPromotion = new AppliedPromotion(
                    testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), true);

            // Then
            assertTrue(appliedPromotion.getIsAutoApplied());
        }
    }
}
