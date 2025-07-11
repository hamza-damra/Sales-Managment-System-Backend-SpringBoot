package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.TestConfig;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.org.springframework.security=OFF",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.flyway.enabled=false",
        "spring.main.lazy-initialization=true"
    },
    classes = {
        com.hamza.salesmanagementbackend.SalesManagementBackendApplication.class,
        TestConfig.class
    }
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SalesPromotionIntegrationTest {

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
                .applicableCategories(new ArrayList<>(Arrays.asList("ELECTRONICS")))
                .couponCode("TEST10")
                .build();
        testPromotion = promotionRepository.save(testPromotion);

        // Debug: Verify promotion was saved correctly
        System.out.println("Test Setup - Promotion saved with ID: " + testPromotion.getId());
        System.out.println("Test Setup - Promotion coupon code: " + testPromotion.getCouponCode());
        System.out.println("Test Setup - Promotion active: " + testPromotion.isCurrentlyActive());
        System.out.println("Test Setup - Promotion categories: " + testPromotion.getApplicableCategories());
        System.out.println("Test Setup - Promotion min order: " + testPromotion.getMinimumOrderAmount());

        // Verify we can find the promotion by coupon code
        var foundPromotion = promotionRepository.findByCouponCode("TEST10");
        System.out.println("Test Setup - Can find promotion by coupon: " + foundPromotion.isPresent());
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
    void testCreateSaleWithAutoPromotion() throws Exception {
        // Given - Set promotion to auto-apply
        testPromotion.setAutoApply(true);
        testPromotion.setCouponCode(null); // Auto-apply promotions don't need coupon codes
        promotionRepository.save(testPromotion);

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.promotionCount").value(1))
                .andExpect(jsonPath("$.appliedPromotions").isArray())
                .andExpect(jsonPath("$.appliedPromotions[0].isAutoApplied").value(true))
                .andExpect(jsonPath("$.appliedPromotions[0].promotionName").value("Test Promotion"));
    }

    @Test
    void testApplyPromotionToExistingSale() throws Exception {
        // Given - Create a sale first WITHOUT promotion
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

        String response = mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SaleDTO createdSale = objectMapper.readValue(response, SaleDTO.class);

        // Debug: Print the created sale details
        System.out.println("Created Sale ID: " + createdSale.getId());
        System.out.println("Created Sale Status: " + createdSale.getStatus());
        System.out.println("Created Sale Total: " + createdSale.getTotalAmount());
        System.out.println("Created Sale Subtotal: " + createdSale.getSubtotal());
        System.out.println("Promotion ID: " + testPromotion.getId());
        System.out.println("Coupon Code: " + testPromotion.getCouponCode());
        System.out.println("Promotion Active: " + testPromotion.isCurrentlyActive());
        System.out.println("Promotion Min Order: " + testPromotion.getMinimumOrderAmount());
        System.out.println("Product Category Name: " + testProduct.getCategory().getName());
        System.out.println("Promotion Applicable Categories: " + testPromotion.getApplicableCategories());
        System.out.println("Customer Type: " + testCustomer.getCustomerType());
        System.out.println("Customer Total Purchases: " + testCustomer.getTotalPurchases());
        System.out.println("Promotion Customer Eligibility: " + testPromotion.getCustomerEligibility());

        // When & Then - Apply promotion to existing sale
        mockMvc.perform(post("/api/sales/" + createdSale.getId() + "/apply-promotion")
                        .param("couponCode", "TEST10"))
                .andDo(result -> {
                    System.out.println("Response Status: " + result.getResponse().getStatus());
                    System.out.println("Response Body: " + result.getResponse().getContentAsString());
                    if (result.getResponse().getStatus() != 200) {
                        System.out.println("ERROR: Expected 200 but got " + result.getResponse().getStatus());
                    }
                })
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.totalSavings").value(20.00));
    }

    @Test
    void testGetEligiblePromotions() throws Exception {
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

        String response = mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SaleDTO createdSale = objectMapper.readValue(response, SaleDTO.class);

        // When & Then - Get eligible promotions
        mockMvc.perform(get("/api/sales/" + createdSale.getId() + "/eligible-promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Promotion"))
                .andExpect(jsonPath("$[0].couponCode").value("TEST10"))
                .andExpect(jsonPath("$[0].isCurrentlyActive").value(true));
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
    void testCreateSaleWithInsufficientOrderAmount() throws Exception {
        // Given - Create promotion with higher minimum order amount
        Promotion highMinPromotion = Promotion.builder()
                .name("High Min Promotion")
                .description("Requires $300 minimum")
                .type(Promotion.PromotionType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15.00))
                .minimumOrderAmount(BigDecimal.valueOf(300.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .usageCount(0)
                .customerEligibility(Promotion.CustomerEligibility.ALL)
                .autoApply(false)
                .applicableCategories(new ArrayList<>(Arrays.asList("ELECTRONICS")))
                .couponCode("HIGH300")
                .build();
        promotionRepository.save(highMinPromotion);

        SaleItemDTO saleItemDTO = SaleItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(1) // Only $100, below $300 minimum
                .unitPrice(BigDecimal.valueOf(100.00))
                .build();

        SaleDTO saleDTO = SaleDTO.builder()
                .customerId(testCustomer.getId())
                .items(Arrays.asList(saleItemDTO))
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        // When & Then
        mockMvc.perform(post("/api/sales")
                        .param("couponCode", "HIGH300")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateSaleWithoutPromotionHasCorrectFields() throws Exception {
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
    void testRemovePromotionFromSale() throws Exception {
        // Given - Create a sale with promotion first
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

        String response = mockMvc.perform(post("/api/sales")
                        .param("couponCode", "TEST10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saleDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SaleDTO createdSale = objectMapper.readValue(response, SaleDTO.class);

        // When & Then - Remove promotion from sale
        mockMvc.perform(delete("/api/sales/" + createdSale.getId() + "/remove-promotion")
                        .param("promotionId", testPromotion.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasPromotions").value(false))
                .andExpect(jsonPath("$.promotionCount").value(0))
                .andExpect(jsonPath("$.totalSavings").value(0.00));
    }

    @Test
    void testPromotionValidationDirectly() {
        // Test the promotion validation logic directly
        System.out.println("=== Direct Promotion Validation Test ===");
        System.out.println("Promotion ID: " + testPromotion.getId());
        System.out.println("Promotion Active: " + testPromotion.isCurrentlyActive());
        System.out.println("Promotion Min Order: " + testPromotion.getMinimumOrderAmount());
        System.out.println("Promotion Categories: " + testPromotion.getApplicableCategories());
        System.out.println("Promotion Customer Eligibility: " + testPromotion.getCustomerEligibility());

        System.out.println("Customer Type: " + testCustomer.getCustomerType());
        System.out.println("Customer Total Purchases: " + testCustomer.getTotalPurchases());
        System.out.println("Customer is applicable: " + testPromotion.isApplicableToCustomer(testCustomer));

        System.out.println("Product Category: " + testProduct.getCategory().getName());
        System.out.println("Category is applicable: " + testPromotion.isApplicableToCategory(testProduct.getCategory().getName()));

        // Test order amount validation
        BigDecimal orderAmount = BigDecimal.valueOf(200.00);
        System.out.println("Order Amount: " + orderAmount);
        System.out.println("Meets minimum: " + (orderAmount.compareTo(testPromotion.getMinimumOrderAmount()) >= 0));

        // Test discount calculation
        BigDecimal discount = testPromotion.calculateDiscount(orderAmount);
        System.out.println("Calculated Discount: " + discount);

        // All checks should pass
        assertTrue(testPromotion.isCurrentlyActive());
        assertTrue(testPromotion.isApplicableToCustomer(testCustomer));
        assertTrue(testPromotion.isApplicableToCategory("ELECTRONICS"));
        assertTrue(orderAmount.compareTo(testPromotion.getMinimumOrderAmount()) >= 0);
        assertTrue(discount.compareTo(BigDecimal.ZERO) > 0);
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }
}
