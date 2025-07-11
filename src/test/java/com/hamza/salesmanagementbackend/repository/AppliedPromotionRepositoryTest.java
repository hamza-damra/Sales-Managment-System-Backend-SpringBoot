package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=false"
})
@DisplayName("AppliedPromotionRepository Tests")
class AppliedPromotionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppliedPromotionRepository appliedPromotionRepository;

    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;
    private Sale testSale1;
    private Sale testSale2;
    private Promotion testPromotion1;
    private Promotion testPromotion2;
    private AppliedPromotion testAppliedPromotion1;
    private AppliedPromotion testAppliedPromotion2;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityManager.clear();

        // Create test category
        testCategory = Category.builder()
                .name("ELECTRONICS")
                .description("Electronic products")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(0)
                .build();
        entityManager.persistAndFlush(testCategory);

        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .totalPurchases(BigDecimal.ZERO)
                .loyaltyPoints(0)
                .build();
        entityManager.persistAndFlush(testCustomer);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(50)
                .category(testCategory)
                .sku("TEST-001")
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();
        entityManager.persistAndFlush(testProduct);

        // Create test promotions
        testPromotion1 = Promotion.builder()
                .name("Summer Sale")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .couponCode("SUMMER10")
                .usageCount(0)
                .minimumOrderAmount(BigDecimal.ZERO)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .stackable(false)
                .build();
        entityManager.persistAndFlush(testPromotion1);

        testPromotion2 = Promotion.builder()
                .name("Flash Sale")
                .type(Promotion.PromotionType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(15.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .couponCode("FLASH15")
                .usageCount(0)
                .minimumOrderAmount(BigDecimal.ZERO)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .stackable(false)
                .build();
        entityManager.persistAndFlush(testPromotion2);

        // Create test sales
        testSale1 = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .subtotal(BigDecimal.valueOf(200.00))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(SaleStatus.PENDING)
                .discountAmount(BigDecimal.ZERO)
                .discountPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .saleType(Sale.SaleType.RETAIL)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .isGift(false)
                .loyaltyPointsEarned(0)
                .loyaltyPointsUsed(0)
                .isReturn(false)
                .profitMargin(BigDecimal.ZERO)
                .costOfGoodsSold(BigDecimal.ZERO)
                .promotionDiscountAmount(BigDecimal.ZERO)
                .build();
        entityManager.persistAndFlush(testSale1);

        testSale2 = Sale.builder()
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .subtotal(BigDecimal.valueOf(300.00))
                .totalAmount(BigDecimal.valueOf(300.00))
                .status(SaleStatus.COMPLETED)
                .discountAmount(BigDecimal.ZERO)
                .discountPercentage(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .taxPercentage(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .saleType(Sale.SaleType.RETAIL)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .deliveryStatus(Sale.DeliveryStatus.DELIVERED)
                .isGift(false)
                .loyaltyPointsEarned(0)
                .loyaltyPointsUsed(0)
                .isReturn(false)
                .profitMargin(BigDecimal.ZERO)
                .costOfGoodsSold(BigDecimal.ZERO)
                .promotionDiscountAmount(BigDecimal.ZERO)
                .build();
        entityManager.persistAndFlush(testSale2);

        // Create test applied promotions
        try {
            testAppliedPromotion1 = AppliedPromotion.builder()
                    .sale(testSale1)
                    .promotion(testPromotion1)
                    .promotionName("Summer Sale")
                    .promotionType(Promotion.PromotionType.PERCENTAGE)
                    .couponCode("SUMMER10")
                    .discountAmount(BigDecimal.valueOf(20.00))
                    .discountPercentage(BigDecimal.valueOf(10.00))
                    .originalAmount(BigDecimal.valueOf(200.00))
                    .finalAmount(BigDecimal.valueOf(180.00))
                    .isAutoApplied(false)
                    .appliedAt(LocalDateTime.now())
                    .build();
            entityManager.persistAndFlush(testAppliedPromotion1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create testAppliedPromotion1: " + e.getMessage(), e);
        }

        try {
            testAppliedPromotion2 = AppliedPromotion.builder()
                    .sale(testSale2)
                    .promotion(testPromotion2)
                    .promotionName("Flash Sale")
                    .promotionType(Promotion.PromotionType.FIXED_AMOUNT)
                    .couponCode("FLASH15")
                    .discountAmount(BigDecimal.valueOf(15.00))
                    .discountPercentage(BigDecimal.valueOf(5.00)) // 15/300 * 100 = 5%
                    .originalAmount(BigDecimal.valueOf(300.00))
                    .finalAmount(BigDecimal.valueOf(285.00))
                    .isAutoApplied(true)
                    .appliedAt(LocalDateTime.now().minusHours(1))
                    .build();
            entityManager.persistAndFlush(testAppliedPromotion2);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create testAppliedPromotion2: " + e.getMessage(), e);
        }

        entityManager.clear();
    }

    @Test
    @DisplayName("Should verify basic repository functionality")
    void testBasicRepositoryFunctionality() {
        // Debug: Check if entities were created
        assertNotNull(testCustomer, "Test customer should not be null");
        assertNotNull(testCustomer.getId(), "Test customer ID should not be null");
        assertNotNull(testProduct, "Test product should not be null");
        assertNotNull(testProduct.getId(), "Test product ID should not be null");
        assertNotNull(testPromotion1, "Test promotion 1 should not be null");
        assertNotNull(testPromotion1.getId(), "Test promotion 1 ID should not be null");
        assertNotNull(testSale1, "Test sale 1 should not be null");
        assertNotNull(testSale1.getId(), "Test sale 1 ID should not be null");
        assertNotNull(testAppliedPromotion1, "Test applied promotion 1 should not be null");
        assertNotNull(testAppliedPromotion1.getId(), "Test applied promotion 1 ID should not be null");

        // When
        long count = appliedPromotionRepository.count();

        // Then
        assertEquals(2, count, "Should have 2 applied promotions in test data");
    }

    @Test
    @DisplayName("Should find applied promotions by sale ID")
    void testFindBySaleId() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findBySale_Id(testSale1.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppliedPromotion1.getId(), result.get(0).getId());
        assertEquals("Summer Sale", result.get(0).getPromotionName());
        assertEquals("SUMMER10", result.get(0).getCouponCode());
    }

    @Test
    @DisplayName("Should find applied promotions by promotion ID")
    void testFindByPromotionId() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findByPromotion_Id(testPromotion1.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppliedPromotion1.getId(), result.get(0).getId());
        assertEquals(testPromotion1.getId(), result.get(0).getPromotion().getId());
    }

    @Test
    @DisplayName("Should find applied promotions by sale ID and promotion ID")
    void testFindBySaleIdAndPromotionId() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findBySale_IdAndPromotion_Id(
                testSale1.getId(), testPromotion1.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppliedPromotion1.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should find applied promotions within date range")
    void testFindByAppliedAtBetween() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(2);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findByAppliedAtBetween(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should find auto-applied promotions")
    void testFindByIsAutoApplied() {
        // When
        List<AppliedPromotion> autoApplied = appliedPromotionRepository.findByIsAutoApplied(true);
        List<AppliedPromotion> manuallyApplied = appliedPromotionRepository.findByIsAutoApplied(false);

        // Then
        assertNotNull(autoApplied);
        assertEquals(1, autoApplied.size());
        assertEquals(testAppliedPromotion2.getId(), autoApplied.get(0).getId());
        assertTrue(autoApplied.get(0).getIsAutoApplied());

        assertNotNull(manuallyApplied);
        assertEquals(1, manuallyApplied.size());
        assertEquals(testAppliedPromotion1.getId(), manuallyApplied.get(0).getId());
        assertFalse(manuallyApplied.get(0).getIsAutoApplied());
    }

    @Test
    @DisplayName("Should count applied promotions by promotion ID")
    void testCountByPromotionId() {
        // When
        long count1 = appliedPromotionRepository.countByPromotion_Id(testPromotion1.getId());
        long count2 = appliedPromotionRepository.countByPromotion_Id(testPromotion2.getId());

        // Then
        assertEquals(1, count1);
        assertEquals(1, count2);
    }

    @Test
    @DisplayName("Should find applied promotions by coupon code")
    void testFindByCouponCode() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findByCouponCode("SUMMER10");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testAppliedPromotion1.getId(), result.get(0).getId());
        assertEquals("SUMMER10", result.get(0).getCouponCode());
    }

    @Test
    @DisplayName("Should get promotion usage statistics")
    void testGetPromotionUsageStats() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(2);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        List<Object[]> stats = appliedPromotionRepository.getPromotionUsageStats(startDate, endDate);

        // Then
        assertNotNull(stats);
        assertEquals(2, stats.size());

        // Verify statistics for each promotion
        for (Object[] stat : stats) {
            Long promotionId = (Long) stat[0];
            Long usageCount = (Long) stat[1];
            BigDecimal totalDiscount = (BigDecimal) stat[2];

            if (promotionId.equals(testPromotion1.getId())) {
                assertEquals(1L, usageCount);
                assertEquals(BigDecimal.valueOf(20.00).setScale(2), totalDiscount.setScale(2));
            } else if (promotionId.equals(testPromotion2.getId())) {
                assertEquals(1L, usageCount);
                assertEquals(BigDecimal.valueOf(15.00).setScale(2), totalDiscount.setScale(2));
            }
        }
    }

    @Test
    @DisplayName("Should return empty list when no applied promotions found")
    void testFindBySaleId_NotFound() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findBySale_Id(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when no promotions found for coupon code")
    void testFindByCouponCode_NotFound() {
        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findByCouponCode("NONEXISTENT");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return zero count for non-existent promotion")
    void testCountByPromotionId_NotFound() {
        // When
        long count = appliedPromotionRepository.countByPromotion_Id(999L);

        // Then
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should handle date range with no results")
    void testFindByAppliedAtBetween_NoResults() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(10);
        LocalDateTime endDate = LocalDateTime.now().minusDays(5);

        // When
        List<AppliedPromotion> result = appliedPromotionRepository.findByAppliedAtBetween(startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save and retrieve applied promotion correctly")
    void testSaveAndRetrieve() {
        // Given
        AppliedPromotion newAppliedPromotion = AppliedPromotion.builder()
                .sale(testSale1)
                .promotion(testPromotion2)
                .promotionName("New Promotion")
                .promotionType(Promotion.PromotionType.PERCENTAGE)
                .couponCode("NEW10")
                .discountAmount(BigDecimal.valueOf(25.00))
                .discountPercentage(BigDecimal.valueOf(12.50))
                .originalAmount(BigDecimal.valueOf(200.00))
                .finalAmount(BigDecimal.valueOf(175.00))
                .isAutoApplied(false)
                .appliedAt(LocalDateTime.now())
                .build();

        // When
        AppliedPromotion saved = appliedPromotionRepository.save(newAppliedPromotion);
        AppliedPromotion retrieved = appliedPromotionRepository.findById(saved.getId()).orElse(null);

        // Then
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(retrieved);
        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("New Promotion", retrieved.getPromotionName());
        assertEquals("NEW10", retrieved.getCouponCode());
        assertEquals(BigDecimal.valueOf(25.00), retrieved.getDiscountAmount());
    }

    @Test
    @DisplayName("Should delete applied promotion correctly")
    void testDelete() {
        // Given
        Long appliedPromotionId = testAppliedPromotion1.getId();

        // When
        appliedPromotionRepository.deleteById(appliedPromotionId);
        boolean exists = appliedPromotionRepository.existsById(appliedPromotionId);

        // Then
        assertFalse(exists);
    }
}
