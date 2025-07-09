package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServicePromotionTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private PromotionApplicationService promotionApplicationService;

    @Mock
    private PromotionService promotionService;

    @InjectMocks
    private SaleService saleService;

    private Customer testCustomer;
    private Product testProduct;
    private Sale testSale;
    private SaleDTO testSaleDTO;
    private Promotion testPromotion;
    private AppliedPromotion testAppliedPromotion;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("test@example.com")
                .customerType(Customer.CustomerType.REGULAR)
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(10)
                .build();

        // Setup test promotion
        testPromotion = Promotion.builder()
                .id(1L)
                .name("Test Promotion")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10.00))
                .couponCode("TEST10")
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

        // Setup test applied promotion
        testAppliedPromotion = new AppliedPromotion(
                testSale, testPromotion, BigDecimal.valueOf(20.00), BigDecimal.valueOf(200.00), false);

        // Setup test sale DTO
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        testSaleDTO = SaleDTO.builder()
                .customerId(1L)
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();
    }

    @Test
    void testCreateSaleWithPromotion_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(promotionApplicationService.validateCouponCode(eq("TEST10"), any(), any(), any()))
                .thenReturn(testPromotion);
        when(promotionApplicationService.applyPromotionToSale(any(), any(), eq(false)))
                .thenReturn(testAppliedPromotion);
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, "TEST10");

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(promotionApplicationService).validateCouponCode(eq("TEST10"), any(), any(), any());
        verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(false));
        verify(saleRepository).save(any(Sale.class));
        verify(productService).reduceStock(1L, 2);
    }

    @Test
    void testCreateSaleWithPromotion_InvalidCoupon() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(promotionApplicationService.validateCouponCode(eq("INVALID"), any(), any(), any()))
                .thenThrow(new BusinessLogicException("Invalid coupon code"));

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                saleService.createSaleWithPromotion(testSaleDTO, "INVALID"));
    }

    @Test
    void testApplyPromotionToExistingSale_Success() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(promotionApplicationService.validateCouponCode(eq("TEST10"), any(), any(), any()))
                .thenReturn(testPromotion);
        when(promotionApplicationService.applyPromotionToSale(any(), any(), eq(false)))
                .thenReturn(testAppliedPromotion);
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.applyPromotionToExistingSale(1L, "TEST10");

        // Then
        assertNotNull(result);
        verify(saleRepository).findById(1L);
        verify(promotionApplicationService).validateCouponCode(eq("TEST10"), any(), any(), any());
        verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(false));
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void testApplyPromotionToExistingSale_SaleNotFound() {
        // Given
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                saleService.applyPromotionToExistingSale(999L, "TEST10"));
    }

    @Test
    void testApplyPromotionToExistingSale_SaleNotPending() {
        // Given
        testSale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                saleService.applyPromotionToExistingSale(1L, "TEST10"));
    }

    @Test
    void testRemovePromotionFromSale_Success() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.removePromotionFromSale(1L, 1L);

        // Then
        assertNotNull(result);
        verify(saleRepository).findById(1L);
        verify(promotionApplicationService).removePromotionFromSale(testSale, 1L);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void testRemovePromotionFromSale_SaleNotPending() {
        // Given
        testSale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

        // When & Then
        assertThrows(BusinessLogicException.class, () ->
                saleService.removePromotionFromSale(1L, 1L));
    }

    @Test
    void testGetEligiblePromotionsForSale_Success() {
        // Given
        List<Promotion> eligiblePromotions = Arrays.asList(testPromotion);
        PromotionDTO promotionDTO = PromotionDTO.builder()
                .id(1L)
                .name("Test Promotion")
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(promotionApplicationService.findEligiblePromotions(any(), any(), any()))
                .thenReturn(eligiblePromotions);
        when(promotionService.mapToDTO(testPromotion)).thenReturn(promotionDTO);

        // When
        List<PromotionDTO> result = saleService.getEligiblePromotionsForSale(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Promotion", result.get(0).getName());
        verify(saleRepository).findById(1L);
        verify(promotionApplicationService).findEligiblePromotions(any(), any(), any());
        verify(promotionService).mapToDTO(testPromotion);
    }

    @Test
    void testGetEligiblePromotionsForSale_SaleNotFound() {
        // Given
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                saleService.getEligiblePromotionsForSale(999L));
    }

    @Test
    void testCreateSaleWithAutoPromotions() {
        // Given
        List<Promotion> autoPromotions = Arrays.asList(testPromotion);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(promotionApplicationService.findAutoApplicablePromotions(any(), any(), any()))
                .thenReturn(autoPromotions);
        when(promotionApplicationService.applyPromotionToSale(any(), any(), eq(true)))
                .thenReturn(testAppliedPromotion);
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, null);

        // Then
        assertNotNull(result);
        verify(promotionApplicationService).findAutoApplicablePromotions(any(), any(), any());
        verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(true));
    }

    @Test
    void testMapToDTO_WithPromotions() {
        // Given
        testSale.getAppliedPromotions().add(testAppliedPromotion);
        testSale.setPromotionDiscountAmount(BigDecimal.valueOf(20.00));
        testSale.setOriginalTotal(BigDecimal.valueOf(200.00));
        testSale.setFinalTotal(BigDecimal.valueOf(180.00));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createSale(testSaleDTO);

        // Then
        assertNotNull(result);
        // Additional assertions would be added here to verify promotion mapping
    }
}
