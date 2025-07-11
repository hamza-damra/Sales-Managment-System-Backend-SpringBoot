package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService Unit Tests")
class SaleServiceTest {

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
    private Category testCategory;
    private Sale testSale;
    private SaleDTO testSaleDTO;
    private SaleItem testSaleItem;
    private Promotion testPromotion;
    private AppliedPromotion testAppliedPromotion;

    @BeforeEach
    void setUp() {
        // Setup test category
        testCategory = Category.builder()
                .id(1L)
                .name("ELECTRONICS")
                .description("Electronic products")
                .build();

        // Setup test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .totalPurchases(BigDecimal.valueOf(500.00))
                .loyaltyPoints(100)
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test product description")
                .price(BigDecimal.valueOf(100.00))
                .costPrice(BigDecimal.valueOf(60.00))
                .stockQuantity(50)
                .category(testCategory)
                .sku("TEST-001")
                .build();

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
                .couponCode("TEST10")
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .build();

        // Setup test sale item
        testSaleItem = SaleItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .totalPrice(BigDecimal.valueOf(200.00))
                .build();

        // Setup test sale
        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .subtotal(BigDecimal.valueOf(200.00))
                .totalAmount(BigDecimal.valueOf(200.00))
                .status(SaleStatus.PENDING)
                .items(Arrays.asList(testSaleItem))
                .appliedPromotions(new ArrayList<>())
                .build();

        testSaleItem.setSale(testSale);

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

    @Nested
    @DisplayName("Basic Sale Operations")
    class BasicSaleOperations {

        @Test
        @DisplayName("Should create sale successfully")
        void testCreateSale_Success() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSale(testSaleDTO);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getCustomerId());
            assertEquals("John Doe", result.getCustomerName());
            verify(customerRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(productService).reduceStock(1L, 2);
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void testCreateSale_CustomerNotFound() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> saleService.createSale(testSaleDTO));
            verify(customerRepository).findById(1L);
            verifyNoInteractions(productRepository, saleRepository, productService);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testCreateSale_ProductNotFound() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> saleService.createSale(testSaleDTO));
            verify(customerRepository).findById(1L);
            verify(productRepository).findById(1L);
            verifyNoInteractions(saleRepository, productService);
        }

        @Test
        @DisplayName("Should throw exception when insufficient stock")
        void testCreateSale_InsufficientStock() {
            // Given
            testProduct.setStockQuantity(1); // Less than required quantity of 2
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // When & Then
            assertThrows(InsufficientStockException.class, () -> saleService.createSale(testSaleDTO));
            verify(customerRepository).findById(1L);
            verify(productRepository).findById(1L);
            verifyNoInteractions(saleRepository, productService);
        }

        @Test
        @DisplayName("Should get sale by ID successfully")
        void testGetSaleById_Success() {
            // Given
            when(saleRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testSale));

            // When
            SaleDTO result = saleService.getSaleById(1L);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getCustomerId());
            verify(saleRepository).findByIdWithItems(1L);
        }

        @Test
        @DisplayName("Should throw exception when sale not found by ID")
        void testGetSaleById_NotFound() {
            // Given
            when(saleRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, () -> saleService.getSaleById(999L));
            verify(saleRepository).findByIdWithItems(999L);
        }

        @Test
        @DisplayName("Should get all sales with pagination")
        void testGetAllSales_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Sale> salesPage = new PageImpl<>(Arrays.asList(testSale));
            when(saleRepository.findAll(pageable)).thenReturn(salesPage);

            // When
            Page<SaleDTO> result = saleService.getAllSales(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1L, result.getContent().get(0).getId());
            verify(saleRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should update sale successfully")
        void testUpdateSale_Success() {
            // Given
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.updateSale(1L, testSaleDTO);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(saleRepository).findById(1L);
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should delete sale successfully")
        void testDeleteSale_Success() {
            // Given
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
            when(saleRepository.countReturnsBySaleId(1L)).thenReturn(0L);

            // When
            assertDoesNotThrow(() -> saleService.deleteSale(1L));

            // Then
            verify(saleRepository).findById(1L);
            verify(saleRepository).countReturnsBySaleId(1L);
            verify(saleRepository).save(testSale);
        }

        @Test
        @DisplayName("Should complete sale successfully")
        void testCompleteSale_Success() {
            // Given
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.completeSale(1L);

            // Then
            assertNotNull(result);
            verify(saleRepository).findById(1L);
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should throw exception when completing already completed sale")
        void testCompleteSale_AlreadyCompleted() {
            // Given
            testSale.setStatus(SaleStatus.COMPLETED);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

            // When & Then
            assertThrows(BusinessLogicException.class, () -> saleService.completeSale(1L));
            verify(saleRepository).findById(1L);
            verify(saleRepository, never()).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should cancel sale successfully")
        void testCancelSale_Success() {
            // Given
            when(saleRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testSale));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.cancelSale(1L);

            // Then
            assertNotNull(result);
            verify(saleRepository).findByIdWithItems(1L);
            verify(saleRepository).save(any(Sale.class));
        }
    }

    @Nested
    @DisplayName("Promotion Integration Tests")
    class PromotionIntegrationTests {

        @Test
        @DisplayName("Should create sale with promotion successfully")
        void testCreateSaleWithPromotion_Success() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.validateCouponCode(eq("TEST10"), any(), any(), any()))
                    .thenReturn(testPromotion);
            when(promotionApplicationService.applyPromotionToSale(any(), eq(testPromotion), eq(false)))
                    .thenReturn(testAppliedPromotion);
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, "TEST10");

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(customerRepository).findById(1L);
            verify(productRepository).findById(1L);
            verify(promotionApplicationService).validateCouponCode(eq("TEST10"), any(), any(), any());
            verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(false));
            verify(productService).reduceStock(1L, 2);
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should create sale with auto-applied promotions")
        void testCreateSaleWithPromotion_AutoApply() {
            // Given
            testPromotion.setAutoApply(true);
            List<Promotion> autoPromotions = Arrays.asList(testPromotion);
            
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.findAutoApplicablePromotions(any(), any(), any()))
                    .thenReturn(autoPromotions);
            when(promotionApplicationService.applyPromotionToSale(any(), eq(testPromotion), eq(true)))
                    .thenReturn(testAppliedPromotion);
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, null);

            // Then
            assertNotNull(result);
            verify(promotionApplicationService).findAutoApplicablePromotions(any(), any(), any());
            verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(true));
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should throw exception for invalid coupon code")
        void testCreateSaleWithPromotion_InvalidCoupon() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.validateCouponCode(eq("INVALID"), any(), any(), any()))
                    .thenThrow(new BusinessLogicException("Invalid coupon code"));

            // When & Then
            assertThrows(BusinessLogicException.class, 
                    () -> saleService.createSaleWithPromotion(testSaleDTO, "INVALID"));
            verify(promotionApplicationService).validateCouponCode(eq("INVALID"), any(), any(), any());
            verifyNoInteractions(saleRepository);
        }

        @Test
        @DisplayName("Should apply promotion to existing sale successfully")
        void testApplyPromotionToExistingSale_Success() {
            // Given
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
            when(promotionApplicationService.validateCouponCode(eq("TEST10"), any(), any(), any()))
                    .thenReturn(testPromotion);
            when(promotionApplicationService.applyPromotionToSale(any(), eq(testPromotion), eq(false)))
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
        @DisplayName("Should throw exception when applying promotion to non-pending sale")
        void testApplyPromotionToExistingSale_NotPending() {
            // Given
            testSale.setStatus(SaleStatus.COMPLETED);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

            // When & Then
            assertThrows(BusinessLogicException.class, 
                    () -> saleService.applyPromotionToExistingSale(1L, "TEST10"));
            verify(saleRepository).findById(1L);
            verifyNoInteractions(promotionApplicationService);
        }

        @Test
        @DisplayName("Should remove promotion from sale successfully")
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
        @DisplayName("Should throw exception when removing promotion from non-pending sale")
        void testRemovePromotionFromSale_NotPending() {
            // Given
            testSale.setStatus(SaleStatus.COMPLETED);
            when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

            // When & Then
            assertThrows(BusinessLogicException.class, 
                    () -> saleService.removePromotionFromSale(1L, 1L));
            verify(saleRepository).findById(1L);
            verifyNoInteractions(promotionApplicationService);
        }

        @Test
        @DisplayName("Should get eligible promotions for sale successfully")
        void testGetEligiblePromotionsForSale_Success() {
            // Given
            List<Promotion> eligiblePromotions = Arrays.asList(testPromotion);
            PromotionDTO promotionDTO = PromotionDTO.builder()
                    .id(1L)
                    .name("Test Promotion")
                    .couponCode("TEST10")
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
            assertEquals("TEST10", result.get(0).getCouponCode());
            verify(saleRepository).findById(1L);
            verify(promotionApplicationService).findEligiblePromotions(any(), any(), any());
            verify(promotionService).mapToDTO(testPromotion);
        }

        @Test
        @DisplayName("Should throw exception when getting eligible promotions for non-existent sale")
        void testGetEligiblePromotionsForSale_SaleNotFound() {
            // Given
            when(saleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(ResourceNotFoundException.class, 
                    () -> saleService.getEligiblePromotionsForSale(999L));
            verify(saleRepository).findById(999L);
            verifyNoInteractions(promotionApplicationService, promotionService);
        }
    }

    @Nested
    @DisplayName("DTO Mapping Tests")
    class DTOMappingTests {

        @Test
        @DisplayName("Should map sale to DTO correctly without promotions")
        void testMapToDTO_WithoutPromotions() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSale(testSaleDTO);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(1L, result.getCustomerId());
            assertEquals("John Doe", result.getCustomerName());
            assertEquals(BigDecimal.valueOf(200.00), result.getTotalAmount());
            assertFalse(result.getHasPromotions());
            assertEquals(0, result.getPromotionCount());
            assertEquals(BigDecimal.ZERO, result.getTotalSavings());
            assertNotNull(result.getAppliedPromotions());
            assertTrue(result.getAppliedPromotions().isEmpty());
        }

        @Test
        @DisplayName("Should map sale to DTO correctly with promotions")
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
            assertEquals(1L, result.getId());
            assertTrue(result.getHasPromotions());
            assertEquals(1, result.getPromotionCount());
            assertEquals(BigDecimal.valueOf(20.00), result.getTotalSavings());
            assertEquals(BigDecimal.valueOf(20.00), result.getPromotionDiscountAmount());
            assertEquals(BigDecimal.valueOf(200.00), result.getOriginalTotal());
            assertEquals(BigDecimal.valueOf(180.00), result.getFinalTotal());
            assertNotNull(result.getAppliedPromotions());
            assertEquals(1, result.getAppliedPromotions().size());
        }

        @Test
        @DisplayName("Should map applied promotion to DTO correctly")
        void testMapAppliedPromotionToDTO() {
            // Given
            testSale.getAppliedPromotions().add(testAppliedPromotion);
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSale(testSaleDTO);

            // Then
            assertNotNull(result.getAppliedPromotions());
            assertEquals(1, result.getAppliedPromotions().size());
            
            var appliedPromotionDTO = result.getAppliedPromotions().get(0);
            assertEquals(1L, appliedPromotionDTO.getPromotionId());
            assertEquals("Test Promotion", appliedPromotionDTO.getPromotionName());
            assertEquals(Promotion.PromotionType.PERCENTAGE, appliedPromotionDTO.getPromotionType());
            assertEquals("TEST10", appliedPromotionDTO.getCouponCode());
            assertEquals(BigDecimal.valueOf(20.00), appliedPromotionDTO.getDiscountAmount());
            assertFalse(appliedPromotionDTO.getIsAutoApplied());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle null coupon code gracefully")
        void testCreateSaleWithPromotion_NullCouponCode() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.findAutoApplicablePromotions(any(), any(), any()))
                    .thenReturn(Arrays.asList());
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, null);

            // Then
            assertNotNull(result);
            verify(promotionApplicationService).findAutoApplicablePromotions(any(), any(), any());
            verify(promotionApplicationService, never()).validateCouponCode(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should handle empty coupon code gracefully")
        void testCreateSaleWithPromotion_EmptyCouponCode() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.findAutoApplicablePromotions(any(), any(), any()))
                    .thenReturn(Arrays.asList());
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, "   ");

            // Then
            assertNotNull(result);
            verify(promotionApplicationService).findAutoApplicablePromotions(any(), any(), any());
            verify(promotionApplicationService, never()).validateCouponCode(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should handle auto-promotion application failure gracefully")
        void testCreateSaleWithPromotion_AutoPromotionFailure() {
            // Given
            when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(promotionApplicationService.findAutoApplicablePromotions(any(), any(), any()))
                    .thenReturn(Arrays.asList(testPromotion));
            when(promotionApplicationService.applyPromotionToSale(any(), eq(testPromotion), eq(true)))
                    .thenThrow(new BusinessLogicException("Promotion application failed"));
            when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

            // When
            SaleDTO result = saleService.createSaleWithPromotion(testSaleDTO, null);

            // Then
            assertNotNull(result);
            verify(promotionApplicationService).findAutoApplicablePromotions(any(), any(), any());
            verify(promotionApplicationService).applyPromotionToSale(any(), eq(testPromotion), eq(true));
            verify(saleRepository).save(any(Sale.class));
        }

        @Test
        @DisplayName("Should validate sale data before processing")
        void testCreateSale_InvalidSaleData() {
            // Given
            SaleDTO invalidSaleDTO = SaleDTO.builder()
                    .customerId(null) // Invalid - null customer ID
                    .items(Arrays.asList()) // Invalid - empty items
                    .build();

            // When & Then
            assertThrows(Exception.class, () -> saleService.createSale(invalidSaleDTO));
            verifyNoInteractions(customerRepository, productRepository, saleRepository, productService);
        }
    }
}
