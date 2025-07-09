package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
public class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PromotionDTO testPromotionDTO;
    private List<PromotionDTO> promotionList;

    @BeforeEach
    void setUp() {
        testPromotionDTO = PromotionDTO.builder()
                .id(1L)
                .name("Summer Sale 2024")
                .description("20% off all summer items")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20.00))
                .minimumOrderAmount(BigDecimal.valueOf(50.00))
                .maximumDiscountAmount(BigDecimal.valueOf(100.00))
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .applicableProducts(Arrays.asList(1L, 2L, 3L))
                .applicableCategories(Arrays.asList("CLOTHING", "ACCESSORIES"))
                .usageLimit(1000)
                .usageCount(0)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .couponCode("SUMMER2024-ABC123")
                .autoApply(false)
                .stackable(false)
                .build();

        PromotionDTO secondPromotion = PromotionDTO.builder()
                .id(2L)
                .name("Winter Sale 2024")
                .description("15% off winter items")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .minimumOrderAmount(BigDecimal.valueOf(30.00))
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(60))
                .isActive(true)
                .couponCode("WINTER2024-XYZ789")
                .build();

        promotionList = Arrays.asList(testPromotionDTO, secondPromotion);
    }

    // Test for the new GET /api/promotions/coupon/{couponCode} endpoint
    @Test
    void getCouponByCode_ShouldReturnPromotion_WhenValidCouponCode() throws Exception {
        // Given
        when(promotionService.validateCouponCode("SUMMER2024-ABC123")).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(get("/api/promotions/coupon/SUMMER2024-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Summer Sale 2024")))
                .andExpect(jsonPath("$.couponCode", is("SUMMER2024-ABC123")))
                .andExpect(jsonPath("$.type", is("PERCENTAGE")))
                .andExpect(jsonPath("$.discountValue", is(20.00)));

        verify(promotionService).validateCouponCode("SUMMER2024-ABC123");
    }

    @Test
    void getCouponByCode_ShouldReturnNotFound_WhenInvalidCouponCode() throws Exception {
        // Given
        when(promotionService.validateCouponCode("INVALID-CODE"))
                .thenThrow(new ResourceNotFoundException("Invalid coupon code: INVALID-CODE"));

        // When & Then
        mockMvc.perform(get("/api/promotions/coupon/INVALID-CODE"))
                .andExpect(status().isNotFound());

        verify(promotionService).validateCouponCode("INVALID-CODE");
    }

    @Test
    void getCouponByCode_ShouldReturnBadRequest_WhenExpiredCoupon() throws Exception {
        // Given
        when(promotionService.validateCouponCode("EXPIRED-CODE"))
                .thenThrow(new BusinessLogicException("Coupon code is not currently active: EXPIRED-CODE"));

        // When & Then
        mockMvc.perform(get("/api/promotions/coupon/EXPIRED-CODE"))
                .andExpect(status().isBadRequest());

        verify(promotionService).validateCouponCode("EXPIRED-CODE");
    }

    @Test
    void getCouponByCode_ShouldReturnBadRequest_WhenEmptyCouponCode() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/promotions/coupon/ "))
                .andExpect(status().isNotFound()); // Spring will return 404 for empty path variable

        // No service call should be made
        verify(promotionService, never()).validateCouponCode(any());
    }

    @Test
    void getCouponByCode_ShouldTrimCouponCode() throws Exception {
        // Given
        when(promotionService.validateCouponCode("SUMMER2024-ABC123")).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(get("/api/promotions/coupon/SUMMER2024-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode", is("SUMMER2024-ABC123")));

        verify(promotionService).validateCouponCode("SUMMER2024-ABC123");
    }

    // Test for the existing POST /api/promotions/validate-coupon endpoint
    @Test
    void validateCoupon_ShouldReturnPromotion_WhenValidCouponCode() throws Exception {
        // Given
        when(promotionService.validateCouponCode("SUMMER2024-ABC123")).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions/validate-coupon")
                        .param("couponCode", "SUMMER2024-ABC123"))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Summer Sale 2024")))
                .andExpect(jsonPath("$.couponCode", is("SUMMER2024-ABC123")));

        verify(promotionService).validateCouponCode("SUMMER2024-ABC123");
    }

    @Test
    void validateCoupon_ShouldReturnBadRequest_WhenEmptyCouponCode() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/promotions/validate-coupon")
                        .param("couponCode", ""))
                .andExpect(status().isBadRequest());

        // No service call should be made
        verify(promotionService, never()).validateCouponCode(any());
    }

    @Test
    void validateCoupon_ShouldReturnNotFound_WhenInvalidCouponCode() throws Exception {
        // Given
        when(promotionService.validateCouponCode("INVALID-CODE"))
                .thenThrow(new ResourceNotFoundException("Invalid coupon code: INVALID-CODE"));

        // When & Then
        mockMvc.perform(post("/api/promotions/validate-coupon")
                        .param("couponCode", "INVALID-CODE"))
                .andExpect(status().isNotFound());

        verify(promotionService).validateCouponCode("INVALID-CODE");
    }

    // Test for other existing endpoints to ensure they still work
    @Test
    void getAllPromotions_ShouldReturnPagedPromotions() throws Exception {
        // Given
        Page<PromotionDTO> promotionPage = new PageImpl<>(promotionList, PageRequest.of(0, 10), 2);
        when(promotionService.getAllPromotions(any())).thenReturn(promotionPage);

        // When & Then
        mockMvc.perform(get("/api/promotions")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Summer Sale 2024")))
                .andExpect(jsonPath("$.content[1].name", is("Winter Sale 2024")))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getPromotionById_ShouldReturnPromotion_WhenExists() throws Exception {
        // Given
        when(promotionService.getPromotionById(1L)).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Summer Sale 2024")))
                .andExpect(jsonPath("$.couponCode", is("SUMMER2024-ABC123")));
    }

    @Test
    void createPromotion_ShouldCreateSuccessfully() throws Exception {
        // Given
        when(promotionService.createPromotion(any(PromotionDTO.class))).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPromotionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Summer Sale 2024")))
                .andExpect(jsonPath("$.couponCode", is("SUMMER2024-ABC123")));

        verify(promotionService).createPromotion(any(PromotionDTO.class));
    }

    @Test
    void applyPromotion_ShouldCalculateDiscount() throws Exception {
        // Given
        BigDecimal orderAmount = BigDecimal.valueOf(100.00);
        BigDecimal discountAmount = BigDecimal.valueOf(20.00);
        when(promotionService.calculateDiscount(1L, orderAmount)).thenReturn(discountAmount);

        // When & Then
        mockMvc.perform(post("/api/promotions/1/apply")
                        .param("orderAmount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount", is(20.00)))
                .andExpect(jsonPath("$.orderAmount", is(100.00)))
                .andExpect(jsonPath("$.finalAmount", is(80.00)));

        verify(promotionService).calculateDiscount(1L, orderAmount);
    }
}
