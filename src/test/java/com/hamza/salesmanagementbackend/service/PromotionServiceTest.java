package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private Promotion testPromotion;
    private PromotionDTO testPromotionDTO;

    @BeforeEach
    void setUp() {
        testPromotion = Promotion.builder()
                .id(1L)
                .name("Test Promotion")
                .description("Test promotion description")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.0))
                .minimumOrderAmount(BigDecimal.valueOf(100.0))
                .maximumDiscountAmount(BigDecimal.valueOf(50.0))
                .startDate(LocalDateTime.now().minusDays(1)) // Started yesterday
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .applicableProducts(Arrays.asList(1L, 2L))
                .applicableCategories(Arrays.asList("Electronics", "Books"))
                .usageLimit(100)
                .usageCount(10)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .couponCode("SAVE10")
                .createdAt(LocalDateTime.now())
                .build();

        testPromotionDTO = new PromotionDTO();
        testPromotionDTO.setId(1L);
        testPromotionDTO.setName("Test Promotion");
        testPromotionDTO.setDescription("Test promotion description");
        testPromotionDTO.setType(Promotion.PromotionType.PERCENTAGE);
        testPromotionDTO.setDiscountValue(BigDecimal.valueOf(10.0));
        testPromotionDTO.setMinimumOrderAmount(BigDecimal.valueOf(100.0));
        testPromotionDTO.setMaximumDiscountAmount(BigDecimal.valueOf(50.0));
        testPromotionDTO.setStartDate(LocalDateTime.now().plusDays(1)); // Future start date for creation
        testPromotionDTO.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotionDTO.setIsActive(true);
        testPromotionDTO.setApplicableProducts(Arrays.asList(1L, 2L));
        testPromotionDTO.setApplicableCategories(Arrays.asList("Electronics", "Books"));
        testPromotionDTO.setUsageLimit(100);
        testPromotionDTO.setUsageCount(10);
        testPromotionDTO.setCustomerEligibility(Promotion.CustomerEligibility.ALL);
        testPromotionDTO.setCouponCode("SAVE10");
        testPromotionDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPromotion_Success() {
        // Given
        when(promotionRepository.existsByCouponCode("SAVE10")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // When
        PromotionDTO result = promotionService.createPromotion(testPromotionDTO);

        // Then
        assertNotNull(result);
        assertEquals(testPromotionDTO.getName(), result.getName());
        assertEquals(testPromotionDTO.getCouponCode(), result.getCouponCode());
        verify(promotionRepository).existsByCouponCode("SAVE10");
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void createPromotion_CouponCodeAlreadyExists_ThrowsException() {
        // Given
        when(promotionRepository.existsByCouponCode("SAVE10")).thenReturn(true);

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> promotionService.createPromotion(testPromotionDTO));
        assertTrue(exception.getMessage().contains("Coupon code already exists"));
        verify(promotionRepository).existsByCouponCode("SAVE10");
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void createPromotion_InvalidDates_ThrowsException() {
        // Given
        testPromotionDTO.setStartDate(LocalDateTime.now().plusDays(10));
        testPromotionDTO.setEndDate(LocalDateTime.now().plusDays(5)); // End before start

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> promotionService.createPromotion(testPromotionDTO));
        assertTrue(exception.getMessage().contains("Start date must be before end date"));
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void createPromotion_WithoutCouponCode_GeneratesCode() {
        // Given
        testPromotionDTO.setCouponCode(null);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // When
        PromotionDTO result = promotionService.createPromotion(testPromotionDTO);

        // Then
        assertNotNull(result);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void getAllPromotions_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Promotion> promotions = Arrays.asList(testPromotion);
        Page<Promotion> promotionPage = new PageImpl<>(promotions, pageable, 1);
        when(promotionRepository.findAll(pageable)).thenReturn(promotionPage);

        // When
        Page<PromotionDTO> result = promotionService.getAllPromotions(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPromotion.getName(), result.getContent().get(0).getName());
        verify(promotionRepository).findAll(pageable);
    }

    @Test
    void getPromotionById_Success() {
        // Given
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        PromotionDTO result = promotionService.getPromotionById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testPromotion.getName(), result.getName());
        assertEquals(testPromotion.getCouponCode(), result.getCouponCode());
        verify(promotionRepository).findById(1L);
    }

    @Test
    void getPromotionById_NotFound_ThrowsException() {
        // Given
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> promotionService.getPromotionById(1L));
        assertEquals("Promotion not found with id: 1", exception.getMessage());
        verify(promotionRepository).findById(1L);
    }

    @Test
    void updatePromotion_Success() {
        // Given
        PromotionDTO updateDTO = new PromotionDTO();
        updateDTO.setName("Updated Promotion");
        updateDTO.setCouponCode("UPDATED10");
        updateDTO.setStartDate(LocalDateTime.now().plusDays(1));
        updateDTO.setEndDate(LocalDateTime.now().plusDays(30));

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.existsByCouponCode("UPDATED10")).thenReturn(false);
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // When
        PromotionDTO result = promotionService.updatePromotion(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(promotionRepository).findById(1L);
        verify(promotionRepository).existsByCouponCode("UPDATED10");
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void updatePromotion_NotFound_ThrowsException() {
        // Given
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> promotionService.updatePromotion(1L, testPromotionDTO));
        assertEquals("Promotion not found with id: 1", exception.getMessage());
        verify(promotionRepository).findById(1L);
        verify(promotionRepository, never()).save(any(Promotion.class));
    }

    @Test
    void deletePromotion_Success() {
        // Given
        testPromotion.setIsActive(false); // Make it inactive so it can be deleted
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        promotionService.deletePromotion(1L);

        // Then
        verify(promotionRepository).findById(1L);
        verify(promotionRepository).delete(testPromotion);
    }

    @Test
    void deletePromotion_NotFound_ThrowsException() {
        // Given
        when(promotionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> promotionService.deletePromotion(1L));
        assertEquals("Promotion not found with id: 1", exception.getMessage());
        verify(promotionRepository).findById(1L);
        verify(promotionRepository, never()).delete(any(Promotion.class));
    }

    @Test
    void activatePromotion_Success() {
        // Given
        testPromotion.setIsActive(false);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // When
        PromotionDTO result = promotionService.activatePromotion(1L);

        // Then
        assertNotNull(result);
        verify(promotionRepository).findById(1L);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void deactivatePromotion_Success() {
        // Given
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));
        when(promotionRepository.save(any(Promotion.class))).thenReturn(testPromotion);

        // When
        PromotionDTO result = promotionService.deactivatePromotion(1L);

        // Then
        assertNotNull(result);
        verify(promotionRepository).findById(1L);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void getActivePromotions_Success() {
        // Given
        List<Promotion> promotions = Arrays.asList(testPromotion);
        when(promotionRepository.findActivePromotions(any(LocalDateTime.class))).thenReturn(promotions);

        // When
        List<PromotionDTO> result = promotionService.getActivePromotions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPromotion.getName(), result.get(0).getName());
        verify(promotionRepository).findActivePromotions(any(LocalDateTime.class));
    }

    @Test
    void getAvailablePromotions_Success() {
        // Given
        List<Promotion> promotions = Arrays.asList(testPromotion);
        when(promotionRepository.findAvailablePromotions(any(LocalDateTime.class))).thenReturn(promotions);

        // When
        List<PromotionDTO> result = promotionService.getAvailablePromotions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPromotion.getName(), result.get(0).getName());
        verify(promotionRepository).findAvailablePromotions(any(LocalDateTime.class));
    }

    @Test
    void validateCouponCode_Success() {
        // Given
        // Set up promotion to be currently active
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setIsActive(true);
        testPromotion.setUsageCount(5); // Below usage limit
        when(promotionRepository.findByCouponCode("SAVE10")).thenReturn(Optional.of(testPromotion));

        // When
        PromotionDTO result = promotionService.validateCouponCode("SAVE10");

        // Then
        assertNotNull(result);
        assertEquals(testPromotion.getCouponCode(), result.getCouponCode());
        verify(promotionRepository).findByCouponCode("SAVE10");
    }

    @Test
    void validateCouponCode_NotFound_ThrowsException() {
        // Given
        when(promotionRepository.findByCouponCode("INVALID")).thenReturn(Optional.empty());

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> promotionService.validateCouponCode("INVALID"));
        assertTrue(exception.getMessage().contains("Invalid coupon code"));
        verify(promotionRepository).findByCouponCode("INVALID");
    }

    @Test
    void calculateDiscount_PercentageType_Success() {
        // Given
        // Set up promotion to be currently active
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setIsActive(true);
        testPromotion.setUsageCount(5); // Below usage limit
        BigDecimal orderAmount = BigDecimal.valueOf(200.0);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        BigDecimal discount = promotionService.calculateDiscount(1L, orderAmount);

        // Then
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(discount)); // 10% of 200 = 20.00
        verify(promotionRepository).findById(1L);
    }

    @Test
    void calculateDiscount_FixedAmountType_Success() {
        // Given
        testPromotion.setType(Promotion.PromotionType.FIXED_AMOUNT);
        testPromotion.setDiscountValue(BigDecimal.valueOf(15.0));
        // Set up promotion to be currently active
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setIsActive(true);
        testPromotion.setUsageCount(5); // Below usage limit
        BigDecimal orderAmount = BigDecimal.valueOf(200.0);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        BigDecimal discount = promotionService.calculateDiscount(1L, orderAmount);

        // Then
        assertEquals(0, BigDecimal.valueOf(15.0).compareTo(discount));
        verify(promotionRepository).findById(1L);
    }

    @Test
    void calculateDiscount_BelowMinimumOrder_ReturnsZero() {
        // Given
        // Set up promotion to be currently active
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setIsActive(true);
        testPromotion.setUsageCount(5); // Below usage limit
        BigDecimal orderAmount = BigDecimal.valueOf(50.0); // Below minimum of 100
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        BigDecimal discount = promotionService.calculateDiscount(1L, orderAmount);

        // Then
        assertEquals(BigDecimal.ZERO, discount);
        verify(promotionRepository).findById(1L);
    }

    @Test
    void calculateDiscount_ExceedsMaximumDiscount_ReturnsMaximum() {
        // Given
        testPromotion.setDiscountValue(BigDecimal.valueOf(50.0)); // 50% discount
        // Set up promotion to be currently active
        testPromotion.setStartDate(LocalDateTime.now().minusDays(1));
        testPromotion.setEndDate(LocalDateTime.now().plusDays(30));
        testPromotion.setIsActive(true);
        testPromotion.setUsageCount(5); // Below usage limit
        BigDecimal orderAmount = BigDecimal.valueOf(200.0);
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(testPromotion));

        // When
        BigDecimal discount = promotionService.calculateDiscount(1L, orderAmount);

        // Then
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(discount)); // Capped at maximum
        verify(promotionRepository).findById(1L);
    }

    @Test
    void getPromotionsByActiveStatus_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Promotion> promotions = Arrays.asList(testPromotion);
        Page<Promotion> promotionPage = new PageImpl<>(promotions, pageable, 1);
        when(promotionRepository.findByIsActive(true, pageable)).thenReturn(promotionPage);

        // When
        Page<PromotionDTO> result = promotionService.getPromotionsByStatus(true, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsActive());
        verify(promotionRepository).findByIsActive(true, pageable);
    }
}
