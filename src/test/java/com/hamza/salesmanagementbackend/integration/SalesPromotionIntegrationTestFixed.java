package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import com.hamza.salesmanagementbackend.service.SaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.flyway.enabled=false",
        "logging.level.org.springframework.security=OFF",
        "logging.level.com.hamza.salesmanagementbackend.config.DataInitializer=OFF"
    }
)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SalesPromotionIntegrationTestFixed {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public com.hamza.salesmanagementbackend.config.DataInitializer testDataInitializer() {
            return new com.hamza.salesmanagementbackend.config.DataInitializer() {
                @Override
                public void run(String... args) throws Exception {
                    // Do nothing - prevent data initialization during tests
                }
            };
        }
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

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

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;
    private Promotion testPromotion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Create and save test category first
        testCategory = Category.builder()
                .name("ELECTRONICS")
                .description("Electronic products")
                .status(Category.CategoryStatus.ACTIVE)
                .displayOrder(1)
                .build();
        testCategory = categoryRepository.save(testCategory);

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
    }

    @Test
    void testCreateSaleWithPromotion() throws Exception {
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
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "TEST10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.promotionCount").value(1))
                .andExpect(jsonPath("$.totalSavings").value(20.00))
                .andExpect(jsonPath("$.appliedPromotions").isArray())
                .andExpect(jsonPath("$.appliedPromotions[0].promotionName").value("Test Promotion"))
                .andExpect(jsonPath("$.appliedPromotions[0].couponCode").value("TEST10"))
                .andExpect(jsonPath("$.appliedPromotions[0].discountAmount").value(20.00))
                .andExpect(jsonPath("$.appliedPromotions[0].isAutoApplied").value(false));
    }

    @Test
    void testCreateSaleWithoutPromotion() throws Exception {
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

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(testCustomer.getId()))
                .andExpect(jsonPath("$.hasPromotions").value(false))
                .andExpect(jsonPath("$.promotionCount").value(0))
                .andExpect(jsonPath("$.totalSavings").value(0.00))
                .andExpect(jsonPath("$.appliedPromotions").isArray())
                .andExpect(jsonPath("$.appliedPromotions").isEmpty());
    }

    @Test
    void testInvalidCouponCode() throws Exception {
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
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testServiceLayerDirectly() {
        // Test the service layer directly without HTTP
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

        // Create sale with promotion
        SaleDTO result = saleService.createSaleWithPromotion(saleDTO, "TEST10");

        // Verify results
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testCustomer.getId(), result.getCustomerId());
        assertTrue(result.getHasPromotions());
        assertEquals(1, result.getPromotionCount());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTotalSavings()));
    }

    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected non-null value");
        }
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + ", but was: " + actual);
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }
}
