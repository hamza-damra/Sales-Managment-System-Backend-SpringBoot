package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.flyway.enabled=false",
    "logging.level.org.hibernate.SQL=DEBUG"
})
@ComponentScan(basePackages = "com.hamza.salesmanagementbackend")
@Transactional
class SalesPromotionIntegrationTestSimple {

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
    private ObjectMapper objectMapper;

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
    void testRepositoriesAreWorking() {
        // Test that all entities are properly saved and can be retrieved
        assertNotNull(testCustomer.getId());
        assertNotNull(testProduct.getId());
        assertNotNull(testCategory.getId());
        assertNotNull(testPromotion.getId());

        // Test relationships
        assertEquals(testCategory.getId(), testProduct.getCategory().getId());
        assertEquals("ELECTRONICS", testCategory.getName());
        assertEquals("TEST10", testPromotion.getCouponCode());
    }

    @Test
    void testPromotionRepositoryQueries() {
        // Test findByCouponCode
        var foundPromotion = promotionRepository.findByCouponCode("TEST10");
        assertTrue(foundPromotion.isPresent());
        assertEquals(testPromotion.getId(), foundPromotion.get().getId());

        // Test findAvailablePromotions
        var availablePromotions = promotionRepository.findAvailablePromotions(LocalDateTime.now());
        assertFalse(availablePromotions.isEmpty());
        assertTrue(availablePromotions.stream().anyMatch(p -> p.getId().equals(testPromotion.getId())));
    }

    @Test
    void testPromotionBusinessLogic() {
        // Test promotion is currently active
        assertTrue(testPromotion.isCurrentlyActive());

        // Test promotion applies to category
        assertTrue(testPromotion.isApplicableToCategory("ELECTRONICS"));
        assertFalse(testPromotion.isApplicableToCategory("CLOTHING"));

        // Test promotion applies to customer
        assertTrue(testPromotion.isApplicableToCustomer(testCustomer));

        // Test discount calculation
        BigDecimal orderAmount = BigDecimal.valueOf(200.00);
        BigDecimal expectedDiscount = BigDecimal.valueOf(20.00); // 10% of 200
        BigDecimal actualDiscount = testPromotion.calculateDiscount(orderAmount);
        assertEquals(0, expectedDiscount.compareTo(actualDiscount));
    }

    @Test
    void testSaleCreation() {
        // Create a basic sale
        Sale sale = Sale.builder()
                .customer(testCustomer)
                .totalAmount(BigDecimal.valueOf(200.00))
                .subtotal(BigDecimal.valueOf(200.00))
                .build();

        // Create sale item
        SaleItem saleItem = SaleItem.builder()
                .sale(sale)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        sale.setItems(Arrays.asList(saleItem));
        sale.calculateTotals();

        Sale savedSale = saleRepository.save(sale);
        entityManager.flush();

        assertNotNull(savedSale.getId());
        assertEquals(BigDecimal.valueOf(200.00), savedSale.getTotalAmount());
        assertEquals(1, savedSale.getItems().size());
    }

    @Test
    void testPromotionEligibilityLogic() {
        // Test minimum order amount
        BigDecimal lowAmount = BigDecimal.valueOf(30.00); // Below minimum of 50
        BigDecimal highAmount = BigDecimal.valueOf(100.00); // Above minimum

        assertEquals(BigDecimal.ZERO, testPromotion.calculateDiscount(lowAmount));
        assertTrue(testPromotion.calculateDiscount(highAmount).compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testInvalidCouponCode() {
        var invalidPromotion = promotionRepository.findByCouponCode("INVALID");
        assertTrue(invalidPromotion.isEmpty());
    }

    @Test
    void testAutoApplyPromotion() {
        // Create an auto-apply promotion
        Promotion autoPromotion = Promotion.builder()
                .name("Auto Promotion")
                .description("Auto-applied 5% off")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(5.00))
                .minimumOrderAmount(BigDecimal.valueOf(50.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .usageCount(0)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(true)
                .applicableCategories(Arrays.asList("ELECTRONICS"))
                .couponCode(null) // Auto-apply promotions don't need coupon codes
                .build();

        Promotion savedAutoPromotion = promotionRepository.save(autoPromotion);
        entityManager.flush();

        assertNotNull(savedAutoPromotion.getId());
        assertTrue(savedAutoPromotion.getAutoApply());
        assertNull(savedAutoPromotion.getCouponCode());

        // Test finding auto-apply promotions
        var autoPromotions = promotionRepository.findAutoApplyPromotions(LocalDateTime.now());
        assertTrue(autoPromotions.stream().anyMatch(p -> p.getId().equals(savedAutoPromotion.getId())));
    }
}
