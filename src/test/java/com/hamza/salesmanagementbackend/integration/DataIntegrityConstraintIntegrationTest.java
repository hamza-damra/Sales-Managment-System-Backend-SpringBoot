package com.hamza.salesmanagementbackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.config.TestSecurityConfig;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class DataIntegrityConstraintIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Customer testCustomer;
    private Product testProduct;
    private Category testCategory;
    private Sale testSale;
    private Return testReturn;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        setupTestData();
    }

    private void setupTestData() {
        // Create test category
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category for integration tests")
                .status(Category.CategoryStatus.ACTIVE)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product for integration tests")
                .price(BigDecimal.valueOf(100.00))
                .stockQuantity(50)
                .category(testCategory)
                .sku("TEST-001")
                .productStatus(Product.ProductStatus.ACTIVE)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test customer
        testCustomer = Customer.builder()
                .name("Test Customer")
                .email("test@example.com")
                .phone("1234567890")
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .build();
        testCustomer = customerRepository.save(testCustomer);

        // Create test sale with PENDING status (so it can be deleted)
        testSale = Sale.builder()
                .customer(testCustomer)
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(SaleStatus.PENDING)
                .saleDate(LocalDateTime.now())
                .items(new ArrayList<>()) // Initialize empty items list
                .build();
        testSale = saleRepository.save(testSale);

        // Create test return
        testReturn = Return.builder()
                .originalSale(testSale)
                .customer(testCustomer)
                .reason(Return.ReturnReason.DEFECTIVE)
                .status(Return.ReturnStatus.PENDING)
                .totalRefundAmount(BigDecimal.valueOf(50.00))
                .returnNumber("RET-001")
                .build();
        testReturn = returnRepository.save(testReturn);
    }

    @Test
    void deleteSale_WithAssociatedReturns_ReturnsConflictError() throws Exception {
        mockMvc.perform(delete("/api/sales/{id}", testSale.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"))
                .andExpect(jsonPath("$.message").value("Cannot delete sale because it has 1 associated return"))
                .andExpect(jsonPath("$.errorCode").value("SALE_HAS_RETURNS"))
                .andExpect(jsonPath("$.suggestions").value("Please process or cancel all associated returns before deleting this sale."))
                .andExpect(jsonPath("$.details.resourceType").value("Sale"))
                .andExpect(jsonPath("$.details.resourceId").value(testSale.getId().intValue()))
                .andExpect(jsonPath("$.details.dependentResource").value("Returns"));
    }

    @Test
    void deleteCustomer_WithAssociatedSales_ReturnsConflictError() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", testCustomer.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"))
                .andExpect(jsonPath("$.message").value("Cannot delete customer because they have 1 associated sale"))
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_HAS_SALES"))
                .andExpect(jsonPath("$.suggestions").value("Please complete, cancel, or reassign all customer sales before deleting this customer."))
                .andExpect(jsonPath("$.details.resourceType").value("Customer"))
                .andExpect(jsonPath("$.details.resourceId").value(testCustomer.getId().intValue()))
                .andExpect(jsonPath("$.details.dependentResource").value("Sales"));
    }

    @Test
    void deleteCategory_WithAssociatedProducts_ReturnsConflictError() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Data Integrity Violation"))
                .andExpect(jsonPath("$.message").value("Cannot delete category because it contains 1 product"))
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_HAS_PRODUCTS"))
                .andExpect(jsonPath("$.suggestions").value("Please move all products to another category or delete them before removing this category."))
                .andExpect(jsonPath("$.details.resourceType").value("Category"))
                .andExpect(jsonPath("$.details.resourceId").value(testCategory.getId().intValue()))
                .andExpect(jsonPath("$.details.dependentResource").value("Products"));
    }

    @Test
    void deleteSale_WithoutReturns_SuccessfulDeletion() throws Exception {
        // Remove the return first
        returnRepository.delete(testReturn);

        mockMvc.perform(delete("/api/sales/{id}", testSale.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCustomer_WithoutDependencies_SuccessfulDeletion() throws Exception {
        // Remove dependencies first
        returnRepository.delete(testReturn);
        saleRepository.delete(testSale);

        mockMvc.perform(delete("/api/customers/{id}", testCustomer.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_WithoutProducts_SuccessfulDeletion() throws Exception {
        // Remove dependencies first
        returnRepository.delete(testReturn);
        saleRepository.delete(testSale);
        productRepository.delete(testProduct);

        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteNonExistentResource_ReturnsNotFound() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(delete("/api/sales/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
