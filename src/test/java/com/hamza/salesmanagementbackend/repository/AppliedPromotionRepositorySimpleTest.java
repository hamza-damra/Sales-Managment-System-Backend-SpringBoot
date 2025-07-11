package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppliedPromotionRepositorySimpleTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppliedPromotionRepository appliedPromotionRepository;

    @Test
    void testRepositoryExists() {
        assertNotNull(appliedPromotionRepository);
        assertNotNull(entityManager);
    }

    @Test
    void testBasicCRUD() {
        // Create minimal test data
        Category category = Category.builder()
                .name("TEST_CATEGORY")
                .description("Test category")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(0)
                .build();
        entityManager.persistAndFlush(category);

        Customer customer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .totalPurchases(BigDecimal.ZERO)
                .loyaltyPoints(0)
                .build();
        entityManager.persistAndFlush(customer);

        Product product = Product.builder()
                .name("Test Product")
                .description("Test product")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(50)
                .category(category)
                .sku("TEST-001")
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();
        entityManager.persistAndFlush(product);

        Promotion promotion = Promotion.builder()
                .name("Test Promotion")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .couponCode("TEST10")
                .usageCount(0)
                .minimumOrderAmount(BigDecimal.ZERO)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .stackable(false)
                .build();
        entityManager.persistAndFlush(promotion);

        Sale sale = Sale.builder()
                .customer(customer)
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
        entityManager.persistAndFlush(sale);

        // Create AppliedPromotion
        AppliedPromotion appliedPromotion = AppliedPromotion.builder()
                .sale(sale)
                .promotion(promotion)
                .promotionName("Test Promotion")
                .promotionType(Promotion.PromotionType.PERCENTAGE)
                .couponCode("TEST10")
                .discountAmount(BigDecimal.valueOf(20.00))
                .discountPercentage(BigDecimal.valueOf(10.00))
                .originalAmount(BigDecimal.valueOf(200.00))
                .finalAmount(BigDecimal.valueOf(180.00))
                .isAutoApplied(false)
                .appliedAt(LocalDateTime.now())
                .build();

        // Test save
        AppliedPromotion saved = appliedPromotionRepository.save(appliedPromotion);
        assertNotNull(saved);
        assertNotNull(saved.getId());

        // Test find
        AppliedPromotion found = appliedPromotionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Test Promotion", found.getPromotionName());
        assertEquals("TEST10", found.getCouponCode());

        // Test count
        long count = appliedPromotionRepository.count();
        assertEquals(1, count);
    }
}
