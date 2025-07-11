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

    @Test
    void testCalculatePromotionDiscount_WithMaximumLimit() {
        // Given
        testPromotion.setMaximumDiscountAmount(BigDecimal.valueOf(15.00));

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.valueOf(15.00), discount);
    }

    @Test
    void testCalculatePromotionDiscount_InactivePromotion() {
        // Given
        testPromotion.setIsActive(false);

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void testCalculatePromotionDiscount_ExpiredPromotion() {
        // Given
        testPromotion.setEndDate(LocalDateTime.now().minusDays(1));

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void testCalculatePromotionDiscount_FreeShippingPromotion() {
        // Given
        testPromotion.setType(Promotion.PromotionType.FREE_SHIPPING);

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    @Test
    void testCalculatePromotionDiscount_BuyXGetYPromotion() {
        // Given
        testPromotion.setType(Promotion.PromotionType.BUY_X_GET_Y);

        // When
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                testPromotion, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertEquals(BigDecimal.ZERO, discount); // Not implemented yet
    }

    @Test
    void testApplyPromotionToSale_ZeroDiscount() {
        // Given
        testPromotion.setMinimumOrderAmount(BigDecimal.valueOf(300.00)); // Higher than order amount

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.applyPromotionToSale(testSale, testPromotion, false));
    }

    @Test
    void testUpdateSaleTotalsWithPromotions_WithTaxAndShipping() {
        // Given
        AppliedPromotion appliedPromotion = new AppliedPromotion(
                testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);
        testSale.getAppliedPromotions().add(appliedPromotion);
        testSale.setTaxAmount(BigDecimal.valueOf(18.00)); // 10% tax on discounted amount
        testSale.setShippingCost(BigDecimal.valueOf(10.00));

        // When
        promotionApplicationService.updateSaleTotalsWithPromotions(testSale);

        // Then
        assertEquals(BigDecimal.valueOf(200.00), testSale.getOriginalTotal());
        assertEquals(BigDecimal.valueOf(20.00), testSale.getPromotionDiscountAmount());
        assertEquals(BigDecimal.valueOf(208.00), testSale.getFinalTotal()); // 180 + 18 + 10
    }

    @Test
    void testValidatePromotionForSale_VIPCustomerEligibility() {
        // Given
        testCustomer.setCustomerType(Customer.CustomerType.VIP);
        testPromotion.setCustomerEligibility(Promotion.CustomerEligibility.VIP_ONLY);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePromotionForSale_NewCustomerEligibility() {
        // Given
        testCustomer.setTotalPurchases(BigDecimal.ZERO);
        testPromotion.setCustomerEligibility(Promotion.CustomerEligibility.NEW_CUSTOMERS);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePromotionForSale_ReturningCustomerEligibility() {
        // Given
        testCustomer.setTotalPurchases(BigDecimal.valueOf(100.00));
        testPromotion.setCustomerEligibility(Promotion.CustomerEligibility.RETURNING_CUSTOMERS);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePromotionForSale_SpecificProductApplicability() {
        // Given
        testPromotion.setApplicableProducts(Arrays.asList(1L));
        testPromotion.setApplicableCategories(null);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertTrue(result);
    }

    @Test
    void testValidatePromotionForSale_NoProductRestrictions() {
        // Given
        testPromotion.setApplicableProducts(null);
        testPromotion.setApplicableCategories(null);

        // When
        boolean result = promotionApplicationService.validatePromotionForSale(
                testPromotion, testCustomer, testSaleItems, BigDecimal.valueOf(200.00));

        // Then
        assertTrue(result);
    }

    @Test
    void testFindEligiblePromotions_WithNullMinimumOrderAmount() {
        // Given - Create a promotion with null minimum order amount
        Promotion promotionWithNullMinimum = Promotion.builder()
                .id(2L)
                .name("Test Promotion with Null Minimum")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .minimumOrderAmount(null) // Explicitly set to null
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .stackable(false)
                .usageCount(0)
                .build();

        List<Promotion> availablePromotions = Arrays.asList(promotionWithNullMinimum);
        when(promotionRepository.findAvailablePromotions(any(LocalDateTime.class)))
                .thenReturn(availablePromotions);

        // When - Should not throw NullPointerException
        List<Promotion> result = promotionApplicationService.findEligiblePromotions(
                testCustomer, testSaleItems, BigDecimal.valueOf(50.00));

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Should be eligible since null minimum means no minimum
        assertEquals(promotionWithNullMinimum.getId(), result.get(0).getId());
        verify(promotionRepository).findAvailablePromotions(any(LocalDateTime.class));
    }

    @Test
    void testCalculatePromotionDiscount_WithNullMinimumOrderAmount() {
        // Given - Create a promotion with null minimum order amount
        Promotion promotionWithNullMinimum = Promotion.builder()
                .id(2L)
                .name("Test Promotion with Null Minimum")
                .type(Promotion.PromotionType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(15.00))
                .minimumOrderAmount(null) // Explicitly set to null
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .stackable(false)
                .usageCount(0)
                .build();

        // When - Should not throw NullPointerException
        BigDecimal discount = promotionApplicationService.calculatePromotionDiscount(
                promotionWithNullMinimum, testSaleItems, BigDecimal.valueOf(50.00));

        // Then
        assertEquals(BigDecimal.valueOf(15.00), discount); // Should apply discount since no minimum
    }

    @Test
    void testRemovePromotionFromSale_NoPromotionsApplied() {
        // Given
        testSale.setAppliedPromotions(null);

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.removePromotionFromSale(testSale, 1L));
    }

    @Test
    void testValidateCouponCode_InactivePromotion() {
        // Given
        testPromotion.setIsActive(false);
        when(promotionRepository.findByCouponCode("TEST10"))
                .thenReturn(Optional.of(testPromotion));

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.validateCouponCode(
                        "TEST10", testCustomer, testSaleItems, BigDecimal.valueOf(200.00)));
    }

    @Test
    void testValidateCouponCode_NotApplicableToOrder() {
        // Given
        testPromotion.setMinimumOrderAmount(BigDecimal.valueOf(300.00));
        when(promotionRepository.findByCouponCode("TEST10"))
                .thenReturn(Optional.of(testPromotion));

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                promotionApplicationService.validateCouponCode(
                        "TEST10", testCustomer, testSaleItems, BigDecimal.valueOf(200.00)));
    }
}
