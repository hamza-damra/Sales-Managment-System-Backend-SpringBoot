package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionApplicationServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionApplicationService promotionApplicationService;

    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;
    private Sale testSale;
    private List<SaleItem> testSaleItems;
    private Promotion testPromotion;

    @BeforeEach
    void setUp() {
        // Setup test category
        testCategory = Category.builder()
                .id(1L)
                .name("ELECTRONICS")
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100.00))
                .category(testCategory)
                .build();

        // Setup test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .customerType(Customer.CustomerType.REGULAR)
                .totalPurchases(BigDecimal.valueOf(500.00))
                .build();

        // Setup test sale
        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .subtotal(BigDecimal.valueOf(200.00))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(SaleStatus.PENDING)
                .appliedPromotions(new ArrayList<>())
                .build();

        // Setup test sale items
        SaleItem saleItem = SaleItem.builder()
                .id(1L)
                .sale(testSale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .totalPrice(BigDecimal.valueOf(200.00))
                .build();
        testSaleItems = Arrays.asList(saleItem);
        testSale.setItems(testSaleItems);

        // Setup test promotion
        testPromotion = Promotion.builder()
                .id(1L)
                .name("Test Promotion")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .minimumOrderAmount(BigDecimal.valueOf(50.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .usageCount(0)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .applicableCategories(Arrays.asList("ELECTRONICS"))
                .couponCode("TEST10")
                .build();
    }

    @Test
    void testFindEligiblePromotions_Success() {
        // Given
        List<Promotion> availablePromotions = Arrays.asList(testPromotion);
        when(promotionRepository.findAvailablePromotions(any(LocalDateTime.class)))
                .thenReturn(availablePromotions);

        // When
        List<Promotion> result = promotionApplicationService.findEligiblePromotions(
                testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPromotion.getId(), result.get(0).getId());
        verify(promotionRepository).findAvailablePromotions(any(LocalDateTime.class));
    }

    @Test
    void testFindAutoApplicablePromotions_Success() {
        // Given
        testPromotion.setAutoApply(true);
        List<Promotion> availablePromotions = Arrays.asList(testPromotion);
        when(promotionRepository.findAvailablePromotions(any(LocalDateTime.class)))
                .thenReturn(availablePromotions);

        // When
        List<Promotion> result = promotionApplicationService.findAutoApplicablePromotions(
                testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPromotion.getId(), result.get(0).getId());
    }

    @Test
    void testValidateCouponCode_Success() {
        // Given
        when(promotionRepository.findByCouponCode("TEST10"))
                .thenReturn(Optional.of(testPromotion));

        // When
        Promotion result = promotionApplicationService.validateCouponCode(
                "TEST10", testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertNotNull(result);
        assertEquals(testPromotion.getId(), result.getId());
        verify(promotionRepository).findByCouponCode("TEST10");
    }

    @Test
    void testValidateCouponCode_InvalidCode() {
        // Given
        when(promotionRepository.findByCouponCode("INVALID"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.validateCouponCode(
                        "INVALID", testCustomer, testSaleItems, BigDecimal.valueOf(200.00)));
    }

    @Test
    void testCalculatePromotionDiscount_PercentagePromotion() {
        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), discount.setScale(2));
    }

    @Test
    void testCalculatePromotionDiscount_FixedAmountPromotion() {
        // Given
        testPromotion.setType(Promotion.PromotionType.FIXED_AMOUNT);
        testPromotion.setDiscountValue(BigDecimal.valueOf(15.00));

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.valueOf(15.00), discount);
    }

    @Test
    void testCalculatePromotionDiscount_BelowMinimumOrder() {
        // Given
        testPromotion.setMinimumOrderAmount(BigDecimal.valueOf(300.00));

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void testApplyPromotionToSale_Success() {
        // When
        AppliedPromotion result = promotionApplicationService.applyPromotionToSale(
                testSale, testPromotion, false);

        // Then
        assertNotNull(result);
        assertEquals(testSale, result.getSale());
        assertEquals(testPromotion, result.getPromotion());
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), result.getDiscountAmount().setScale(2));
        assertFalse(result.getIsAutoApplied());
        assertEquals(1, testSale.getAppliedPromotions().size());
    }

    @Test
    void testRemovePromotionFromSale_Success() {
        // Given
        AppliedPromotion appliedPromotion = new AppliedPromotion(
                testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);
        testSale.getAppliedPromotions().add(appliedPromotion);

        // When
        promotionApplicationService.removePromotionFromSale(testSale, testPromotion.getId());

        // Then
        assertTrue(testSale.getAppliedPromotions().isEmpty());
    }

    @Test
    void testRemovePromotionFromSale_PromotionNotFound() {
        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.removePromotionFromSale(testSale, 999L));
    }

    @Test
    void testUpdateSaleTotalsWithPromotions() {
        // Given
        AppliedPromotion appliedPromotion = new AppliedPromotion(
                testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);
        testSale.getAppliedPromotions().add(appliedPromotion);

        // When
        promotionApplicationService.updateSaleTotalsWithPromotions(testSale);

        // Then
        assertEquals(BigDecimal.valueOf(200.00), testSale.getOriginalTotal());
        assertEquals(BigDecimal.valueOf(20.00), testSale.getPromotionDiscountAmount());
        assertEquals(BigDecimal.valueOf(180.00), testSale.getFinalTotal());
    }

    @Test
    void testValidatePromotionForSale_CustomerNotEligible() {
        // Given
        testPromotion.setCustomerEligibility(Promotion.CustomerEligibility.VIP_ONLY);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertFalse(result);
    }

    @Test
    void testValidatePromotionForSale_ProductNotApplicable() {
        // Given
        testPromotion.setApplicableCategories(Arrays.asList("CLOTHING"));

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertFalse(result);
    }
}
