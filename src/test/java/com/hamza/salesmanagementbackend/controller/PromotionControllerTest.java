package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.PromotionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PromotionController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@Import(PromotionControllerTest.TestSecurityConfig.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    @Autowired
    private ObjectMapper objectMapper;

    private PromotionDTO testPromotionDTO;

    @BeforeEach
    void setUp() {
        testPromotionDTO = new PromotionDTO();
        testPromotionDTO.setId(1L);
        testPromotionDTO.setName("Test Promotion");
        testPromotionDTO.setDescription("Test promotion description");
        testPromotionDTO.setType(Promotion.PromotionType.PERCENTAGE);
        testPromotionDTO.setDiscountValue(BigDecimal.valueOf(10.0));
        testPromotionDTO.setMinimumOrderAmount(BigDecimal.valueOf(100.0));
        testPromotionDTO.setMaximumDiscountAmount(BigDecimal.valueOf(50.0));
        testPromotionDTO.setStartDate(LocalDateTime.now());
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
    void createPromotion_Success() throws Exception {
        // Given
        when(promotionService.createPromotion(any(PromotionDTO.class))).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPromotionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Promotion"))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.couponCode").value("SAVE10"));

        verify(promotionService).createPromotion(any(PromotionDTO.class));
    }

    @Test
    void getAllPromotions_Success() throws Exception {
        // Given
        Page<PromotionDTO> promotionPage = new PageImpl<>(Arrays.asList(testPromotionDTO),
                PageRequest.of(0, 10), 1);
        when(promotionService.getAllPromotions(any())).thenReturn(promotionPage);

        // When & Then
        mockMvc.perform(get("/api/promotions")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Promotion"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(promotionService).getAllPromotions(any());
    }

    @Test
    void getAllPromotions_WithActiveFilter_Success() throws Exception {
        // Given
        Page<PromotionDTO> promotionPage = new PageImpl<>(Arrays.asList(testPromotionDTO),
                PageRequest.of(0, 10), 1);
        when(promotionService.getPromotionsByStatus(eq(true), any())).thenReturn(promotionPage);

        // When & Then
        mockMvc.perform(get("/api/promotions")
                .param("isActive", "true")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(promotionService).getPromotionsByStatus(eq(true), any());
    }

    @Test
    void getPromotionById_Success() throws Exception {
        // Given
        when(promotionService.getPromotionById(1L)).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Promotion"))
                .andExpect(jsonPath("$.couponCode").value("SAVE10"));

        verify(promotionService).getPromotionById(1L);
    }

    @Test
    void getPromotionById_NotFound() throws Exception {
        // Given
        when(promotionService.getPromotionById(1L)).thenThrow(new ResourceNotFoundException("Promotion not found"));

        // When & Then
        mockMvc.perform(get("/api/promotions/1"))
                .andExpect(status().isNotFound());

        verify(promotionService).getPromotionById(1L);
    }

    @Test
    void getPromotionById_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/promotions/0"))
                .andExpect(status().isBadRequest());

        verify(promotionService, never()).getPromotionById(any());
    }

    @Test
    void updatePromotion_Success() throws Exception {
        // Given
        when(promotionService.updatePromotion(eq(1L), any(PromotionDTO.class))).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(put("/api/promotions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPromotionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Promotion"));

        verify(promotionService).updatePromotion(eq(1L), any(PromotionDTO.class));
    }

    @Test
    void updatePromotion_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/promotions/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testPromotionDTO)))
                .andExpect(status().isBadRequest());

        verify(promotionService, never()).updatePromotion(any(), any());
    }

    @Test
    void deletePromotion_Success() throws Exception {
        // Given
        doNothing().when(promotionService).deletePromotion(1L);

        // When & Then
        mockMvc.perform(delete("/api/promotions/1"))
                .andExpect(status().isNoContent());

        verify(promotionService).deletePromotion(1L);
    }

    @Test
    void deletePromotion_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/promotions/0"))
                .andExpect(status().isBadRequest());

        verify(promotionService, never()).deletePromotion(any());
    }

    @Test
    void activatePromotion_Success() throws Exception {
        // Given
        testPromotionDTO.setIsActive(true);
        when(promotionService.activatePromotion(1L)).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));

        verify(promotionService).activatePromotion(1L);
    }

    @Test
    void activatePromotion_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/promotions/0/activate"))
                .andExpect(status().isBadRequest());

        verify(promotionService, never()).activatePromotion(any());
    }

    @Test
    void deactivatePromotion_Success() throws Exception {
        // Given
        testPromotionDTO.setIsActive(false);
        when(promotionService.deactivatePromotion(1L)).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        verify(promotionService).deactivatePromotion(1L);
    }

    @Test
    void getActivePromotions_Success() throws Exception {
        // Given
        List<PromotionDTO> promotions = Arrays.asList(testPromotionDTO);
        when(promotionService.getActivePromotions()).thenReturn(promotions);

        // When & Then
        mockMvc.perform(get("/api/promotions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Promotion"));

        verify(promotionService).getActivePromotions();
    }

    @Test
    void getAvailablePromotions_Success() throws Exception {
        // Given
        List<PromotionDTO> promotions = Arrays.asList(testPromotionDTO);
        when(promotionService.getAvailablePromotions()).thenReturn(promotions);

        // When & Then
        mockMvc.perform(get("/api/promotions/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Promotion"));

        verify(promotionService).getAvailablePromotions();
    }

    @Test
    void validateCoupon_Success() throws Exception {
        // Given
        when(promotionService.validateCouponCode("SAVE10")).thenReturn(testPromotionDTO);

        // When & Then
        mockMvc.perform(post("/api/promotions/validate-coupon")
                .param("couponCode", "SAVE10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponCode").value("SAVE10"));

        verify(promotionService).validateCouponCode("SAVE10");
    }

    @Test
    void validateCoupon_InvalidCode_ReturnsNotFound() throws Exception {
        // Given
        when(promotionService.validateCouponCode("INVALID")).thenThrow(new ResourceNotFoundException("Invalid coupon"));

        // When & Then
        mockMvc.perform(post("/api/promotions/validate-coupon")
                .param("couponCode", "INVALID"))
                .andExpect(status().isNotFound());

        verify(promotionService).validateCouponCode("INVALID");
    }

    @Test
    void applyPromotion_Success() throws Exception {
        // Given
        BigDecimal orderAmount = BigDecimal.valueOf(200.0);
        BigDecimal discountAmount = BigDecimal.valueOf(20.0);
        when(promotionService.calculateDiscount(1L, orderAmount)).thenReturn(discountAmount);

        // When & Then
        mockMvc.perform(post("/api/promotions/1/apply")
                .param("orderAmount", "200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountAmount").value(20.0));

        verify(promotionService).calculateDiscount(1L, orderAmount);
    }

    @Test
    void applyPromotion_InvalidOrderAmount_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/promotions/1/apply")
                .param("orderAmount", "-10.0"))
                .andExpect(status().isBadRequest());

        verify(promotionService, never()).calculateDiscount(any(), any());
    }

    @Test
    void testSortingParameterValidation_ValidSortField() throws Exception {
        // Given
        Page<PromotionDTO> promotionPage = new PageImpl<>(Arrays.asList(testPromotionDTO),
                PageRequest.of(0, 10), 1);
        when(promotionService.getAllPromotions(any())).thenReturn(promotionPage);

        // When & Then - Test valid sort fields
        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "startDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        verify(promotionService, times(2)).getAllPromotions(any());
    }

    @Test
    void testSortingParameterValidation_InvalidSortField_UsesDefault() throws Exception {
        // Given
        Page<PromotionDTO> promotionPage = new PageImpl<>(Arrays.asList(testPromotionDTO),
                PageRequest.of(0, 10), 1);
        when(promotionService.getAllPromotions(any())).thenReturn(promotionPage);

        // When & Then - Invalid sort field should default to 'id'
        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        verify(promotionService).getAllPromotions(any());
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityConfig {

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public com.hamza.salesmanagementbackend.security.JwtTokenProvider jwtTokenProvider() {
            return org.mockito.Mockito.mock(com.hamza.salesmanagementbackend.security.JwtTokenProvider.class);
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public com.hamza.salesmanagementbackend.security.CustomUserDetailsService customUserDetailsService() {
            return org.mockito.Mockito.mock(com.hamza.salesmanagementbackend.security.CustomUserDetailsService.class);
        }
    }
}
