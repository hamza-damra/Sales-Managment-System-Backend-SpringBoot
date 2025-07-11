package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaleController.class)
class SaleControllerPromotionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaleService saleService;

    @Autowired
    private ObjectMapper objectMapper;

    private SaleDTO testSaleDTO;
    private PromotionDTO testPromotionDTO;

    @BeforeEach
    void setUp() {
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        testSaleDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .customerName("Test Customer")
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .originalTotal(BigDecimal.valueOf(200.00))
                .finalTotal(BigDecimal.valueOf(180.00))
                .promotionDiscountAmount(BigDecimal.valueOf(20.00))
                .totalSavings(BigDecimal.valueOf(20.00))
                .hasPromotions(true)
                .promotionCount(1)
                .build();

        testPromotionDTO = PromotionDTO.builder()
                .id(1L)
                .name("Test Promotion")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .couponCode("TEST10")
                .build();
    }

    @Test
    void testCreateSaleWithCoupon_Success() throws Exception {
        // Given
        SaleDTO requestDTO = SaleDTO.builder()
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        when(saleService.createSaleWithPromotion(any(SaleDTO.class), eq("TEST10")))
                .thenReturn(testSaleDTO);

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "TEST10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.finalTotal").value(180.00))
                .andExpect(jsonPath("$.promotionDiscountAmount").value(20.00))
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.promotionCount").value(1));
    }

    @Test
    void testCreateSaleWithoutCoupon_Success() throws Exception {
        // Given
        SaleDTO requestDTO = SaleDTO.builder()
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        SaleDTO responseDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .hasPromotions(false)
                .promotionCount(0)
                .build();

        when(saleService.createSale(any(SaleDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.totalAmount").value(200.00))
                .andExpect(jsonPath("$.hasPromotions").value(false))
                .andExpect(jsonPath("$.promotionCount").value(0));
    }

    @Test
    void testApplyPromotionToSale_Success() throws Exception {
        // Given
        when(saleService.applyPromotionToExistingSale(1L, "TEST10"))
                .thenReturn(testSaleDTO);

        // When & Then
        mockMvc.perform(post("/api/sales/1/apply-promotion")
                        .param("couponCode", "TEST10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.finalTotal").value(180.00))
                .andExpect(jsonPath("$.promotionDiscountAmount").value(20.00))
                .andExpect(jsonPath("$.hasPromotions").value(true));
    }

    @Test
    void testApplyPromotionToSale_InvalidSaleId() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sales/0/apply-promotion")
                        .param("couponCode", "TEST10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApplyPromotionToSale_EmptyCouponCode() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sales/1/apply-promotion")
                        .param("couponCode", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testApplyPromotionToSale_SaleNotFound() throws Exception {
        // Given
        when(saleService.applyPromotionToExistingSale(999L, "TEST10"))
                .thenThrow(new ResourceNotFoundException("Sale not found"));

        // When & Then
        mockMvc.perform(post("/api/sales/999/apply-promotion")
                        .param("couponCode", "TEST10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testApplyPromotionToSale_BusinessLogicException() throws Exception {
        // Given
        when(saleService.applyPromotionToExistingSale(1L, "INVALID"))
                .thenThrow(new BusinessLogicException("Invalid coupon code"));

        // When & Then
        mockMvc.perform(post("/api/sales/1/apply-promotion")
                        .param("couponCode", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemovePromotionFromSale_Success() throws Exception {
        // Given
        SaleDTO responseDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .totalAmount(BigDecimal.valueOf(200.00))
                .hasPromotions(false)
                .promotionCount(0)
                .build();

        when(saleService.removePromotionFromSale(1L, 1L)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(delete("/api/sales/1/remove-promotion")
                        .param("promotionId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hasPromotions").value(false))
                .andExpect(jsonPath("$.promotionCount").value(0));
    }

    @Test
    void testRemovePromotionFromSale_InvalidIds() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sales/0/remove-promotion")
                        .param("promotionId", "1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/sales/1/remove-promotion")
                        .param("promotionId", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemovePromotionFromSale_SaleNotFound() throws Exception {
        // Given
        when(saleService.removePromotionFromSale(999L, 1L))
                .thenThrow(new ResourceNotFoundException("Sale not found"));

        // When & Then
        mockMvc.perform(delete("/api/sales/999/remove-promotion")
                        .param("promotionId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEligiblePromotionsForSale_Success() throws Exception {
        // Given
        List<PromotionDTO> eligiblePromotions = Arrays.asList(testPromotionDTO);
        when(saleService.getEligiblePromotionsForSale(1L)).thenReturn(eligiblePromotions);

        // When & Then
        mockMvc.perform(get("/api/sales/1/eligible-promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Promotion"))
                .andExpect(jsonPath("$[0].couponCode").value("TEST10"));
    }

    @Test
    void testGetEligiblePromotionsForSale_InvalidSaleId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/sales/0/eligible-promotions"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEligiblePromotionsForSale_SaleNotFound() throws Exception {
        // Given
        when(saleService.getEligiblePromotionsForSale(999L))
                .thenThrow(new ResourceNotFoundException("Sale not found"));

        // When & Then
        mockMvc.perform(get("/api/sales/999/eligible-promotions"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSaleWithInvalidData_ValidationError() throws Exception {
        // Given
        SaleDTO invalidSaleDTO = SaleDTO.builder()
                .customerId(null) // Invalid - null customer ID
                .items(Arrays.asList()) // Invalid - empty items
                .build();

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSaleDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateSaleWithCoupon_BusinessLogicException() throws Exception {
        // Given
        SaleDTO requestDTO = SaleDTO.builder()
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        when(saleService.createSaleWithPromotion(any(SaleDTO.class), eq("EXPIRED")))
                .thenThrow(new BusinessLogicException("Promotion has expired"));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "EXPIRED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateSaleWithCoupon_ResourceNotFoundException() throws Exception {
        // Given
        SaleDTO requestDTO = SaleDTO.builder()
                .customerId(999L) // Non-existent customer
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        when(saleService.createSaleWithPromotion(any(SaleDTO.class), eq("TEST10")))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "TEST10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testApplyPromotionToSale_MissingCouponCode() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/sales/1/apply-promotion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemovePromotionFromSale_MissingPromotionId() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/sales/1/remove-promotion"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemovePromotionFromSale_BusinessLogicException() throws Exception {
        // Given
        when(saleService.removePromotionFromSale(1L, 1L))
                .thenThrow(new BusinessLogicException("Sale is not in pending status"));

        // When & Then
        mockMvc.perform(delete("/api/sales/1/remove-promotion")
                        .param("promotionId", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEligiblePromotionsForSale_EmptyList() throws Exception {
        // Given
        when(saleService.getEligiblePromotionsForSale(1L)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/sales/1/eligible-promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetEligiblePromotionsForSale_MultiplePromotions() throws Exception {
        // Given
        PromotionDTO promotion1 = PromotionDTO.builder()
                .id(1L)
                .name("Summer Sale")
                .couponCode("SUMMER20")
                .build();

        PromotionDTO promotion2 = PromotionDTO.builder()
                .id(2L)
                .name("Flash Sale")
                .couponCode("FLASH10")
                .build();

        List<PromotionDTO> eligiblePromotions = Arrays.asList(promotion1, promotion2);
        when(saleService.getEligiblePromotionsForSale(1L)).thenReturn(eligiblePromotions);

        // When & Then
        mockMvc.perform(get("/api/sales/1/eligible-promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Summer Sale"))
                .andExpect(jsonPath("$[0].couponCode").value("SUMMER20"))
                .andExpect(jsonPath("$[1].name").value("Flash Sale"))
                .andExpect(jsonPath("$[1].couponCode").value("FLASH10"));
    }

    @Test
    void testApplyPromotionToSale_WithComplexPromotionData() throws Exception {
        // Given
        SaleDTO complexSaleDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .customerName("Test Customer")
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .originalTotal(BigDecimal.valueOf(200.00))
                .finalTotal(BigDecimal.valueOf(170.00))
                .promotionDiscountAmount(BigDecimal.valueOf(30.00))
                .totalSavings(BigDecimal.valueOf(30.00))
                .hasPromotions(true)
                .promotionCount(2)
                .build();

        when(saleService.applyPromotionToExistingSale(1L, "COMBO20"))
                .thenReturn(complexSaleDTO);

        // When & Then
        mockMvc.perform(post("/api/sales/1/apply-promotion")
                        .param("couponCode", "COMBO20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalTotal").value(200.00))
                .andExpect(jsonPath("$.finalTotal").value(170.00))
                .andExpect(jsonPath("$.promotionDiscountAmount").value(30.00))
                .andExpect(jsonPath("$.totalSavings").value(30.00))
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.promotionCount").value(2));
    }

    @Test
    void testCreateSaleWithAutoPromotions() throws Exception {
        // Given
        SaleDTO requestDTO = SaleDTO.builder()
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        SaleDTO responseDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .items(testSaleDTO.getItems())
                .totalAmount(BigDecimal.valueOf(180.00))
                .originalTotal(BigDecimal.valueOf(200.00))
                .finalTotal(BigDecimal.valueOf(180.00))
                .promotionDiscountAmount(BigDecimal.valueOf(20.00))
                .hasPromotions(true)
                .promotionCount(1)
                .build();

        when(saleService.createSale(any(SaleDTO.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.originalTotal").value(200.00))
                .andExpect(jsonPath("$.finalTotal").value(180.00))
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.promotionCount").value(1));
    }
}
