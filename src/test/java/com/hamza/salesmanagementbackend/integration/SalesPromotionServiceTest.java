package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import com.hamza.salesmanagementbackend.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=false",
    "logging.level.org.hibernate.SQL=WARN"
})
@ComponentScan(basePackages = "com.hamza.salesmanagementbackend")
@Transactional
class SalesPromotionServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleService saleService;

    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;
    private Promotion testPromotion;

    @BeforeEach
    void setUp() {
        // Create and save test category first
        testCategory = Category.builder()
                .name("ELECTRONICS")
                .description("Electronic products")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(1)
                .build();
        testCategory = categoryRepository.save(testCategory);
        entityManager.flush();

        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .totalPurchases(BigDecimal.valueOf(500.00))
                .build();
        testCustomer = customerRepository.save(testCustomer);
        entityManager.flush();

        // Create test product with saved category
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(50)
                .category(testCategory)
                .sku("TEST-001")
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);
        entityManager.flush();

        // Create test promotion
        testPromotion = Promotion.builder()
                .name("Test Promotion")
                .description("10% off electronics")
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
        testPromotion = promotionRepository.save(testPromotion);
        entityManager.flush();
    }

    @Test
    void testCreateSaleWithPromotion() {
        // Given
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        // When
        SaleDTO result = saleService.createSaleWithPromotion(saleDTO, "TEST10");

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testCustomer.getId(), result.getCustomerId());
        assertTrue(result.getHasPromotions());
        assertEquals(1, result.getPromotionCount());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTotalSavings()));
        assertNotNull(result.getAppliedPromotions());
        assertEquals(1, result.getAppliedPromotions().size());
        assertEquals("Test Promotion", result.getAppliedPromotions().get(0).getPromotionName());
        assertEquals("TEST10", result.getAppliedPromotions().get(0).getCouponCode());
        assertFalse(result.getAppliedPromotions().get(0).getIsAutoApplied());
    }

    @Test
    void testCreateSaleWithAutoPromotion() {
        // Given - Set promotion to auto-apply
        testPromotion.setAutoApply(true);
        testPromotion.setCouponCode(null);
        promotionRepository.save(testPromotion);
        entityManager.flush();

        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        // When
        SaleDTO result = saleService.createSale(saleDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.getHasPromotions());
        assertEquals(1, result.getPromotionCount());
        assertNotNull(result.getAppliedPromotions());
        assertEquals(1, result.getAppliedPromotions().size());
        assertTrue(result.getAppliedPromotions().get(0).getIsAutoApplied());
    }

    @Test
    void testApplyPromotionToExistingSale() {
        // Given - Create a sale first
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        SaleDTO createdSale = saleService.createSale(saleDTO);

        // When - Apply promotion to existing sale
        SaleDTO result = saleService.applyPromotionToExistingSale(createdSale.getId(), "TEST10");

        // Then
        assertNotNull(result);
        assertTrue(result.getHasPromotions());
        assertEquals(1, result.getPromotionCount());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTotalSavings()));
    }

    @Test
    void testGetEligiblePromotionsForSale() {
        // Given - Create a sale first
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        SaleDTO createdSale = saleService.createSale(saleDTO);

        // When
        List<PromotionDTO> eligiblePromotions = saleService.getEligiblePromotionsForSale(createdSale.getId());

        // Then
        assertNotNull(eligiblePromotions);
        assertFalse(eligiblePromotions.isEmpty());
        assertTrue(eligiblePromotions.stream().anyMatch(p -> "Test Promotion".equals(p.getName())));
        assertTrue(eligiblePromotions.stream().anyMatch(p -> "TEST10".equals(p.getCouponCode())));
    }

    @Test
    void testInvalidCouponCode() {
        // Given
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(200.00))
                .build();

        // When & Then
        assertThrows(Exception.class, () -> {
            saleService.createSaleWithPromotion(saleDTO, "INVALID");
        });
    }

    @Test
    void testCreateSaleWithoutPromotion() {
        // Given
        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        // When
        SaleDTO result = saleService.createSale(saleDTO);

        // Then
        assertNotNull(result);
        assertFalse(result.getHasPromotions());
        assertEquals(0, result.getPromotionCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalSavings()));
        assertTrue(result.getAppliedPromotions().isEmpty());
    }
}
