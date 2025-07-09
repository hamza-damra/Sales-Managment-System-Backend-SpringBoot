package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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

        // Create test category
        testCategory = Category.builder()
                .name("ELECTRONICS")
                .description("Electronic products")
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

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(50)
                .category(testCategory)
                .sku("TEST-001")
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
                .andExpect(jsonPath("$.appliedPromotions[0].promotionName").value("Test Promotion"))
                .andExpect(jsonPath("$.appliedPromotions[0].couponCode").value("TEST10"))
                .andExpect(jsonPath("$.appliedPromotions[0].discountAmount").value(20.00));
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
                .andExpected(status().isCreated())
                .andExpect(jsonPath("$.hasPromotions").value(true))
                .andExpect(jsonPath("$.appliedPromotions[0].isAutoApplied").value(true));
    }

    @Test
    void testApplyPromotionToExistingSale() throws Exception {
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

        // When & Then - Apply promotion to existing sale
        mockMvc.perform(post("/api/sales/" + createdSale.getId() + "/apply-promotion")
                        .param("couponCode", "TEST10"))
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
                .andExpect(jsonPath("$[0].couponCode").value("TEST10"));
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
}
