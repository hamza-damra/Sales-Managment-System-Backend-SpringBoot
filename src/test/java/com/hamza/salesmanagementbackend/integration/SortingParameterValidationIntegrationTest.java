package com.hamza.salesmanagementbackend.integration;

import com.hamza.salesmanagementbackend.config.TestSecurityConfig;
import com.hamza.salesmanagementbackend.dto.*;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test to verify that sorting parameter validation works correctly
 * across all controllers and prevents PropertyReferenceException errors.
 */
@SpringBootTest(classes = com.hamza.salesmanagementbackend.SalesManagementBackendApplication.class,
               webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TestSecurityConfig.class)
class SortingParameterValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ProductService productService;

    @MockBean
    private SaleService saleService;

    @MockBean
    private SupplierService supplierService;

    @MockBean
    private ReturnService returnService;

    @MockBean
    private PromotionService promotionService;

    @Test
    void customerController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<CustomerDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(customerService.getAllCustomers(any())).thenReturn(emptyPage);

        // When & Then - Invalid sort fields should not cause PropertyReferenceException
        mockMvc.perform(get("/api/customers")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk()); // Should not return 500 error

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "nonExistentProperty")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "randomString123")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void productController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<ProductDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(productService.getAllProducts(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/products")
                .param("sortBy", "invalidProductField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                .param("sortBy", "badProperty")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void saleController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<SaleDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(saleService.getAllSales(any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/sales")
                .param("sortBy", "invalidSaleField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sales")
                .param("sortBy", "wrongField")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void supplierController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<SupplierDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(supplierService.getAllSuppliers(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "invalidSupplierField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "nonExistentField")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Test search endpoint as well
        when(supplierService.searchSuppliers(any(), any())).thenReturn(emptyPage);
        mockMvc.perform(get("/api/suppliers/search")
                .param("query", "test")
                .param("sortBy", "badField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void returnController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<ReturnDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(returnService.getAllReturns(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/returns")
                .param("sortBy", "invalidReturnField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/returns")
                .param("sortBy", "wrongProperty")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Test search endpoint as well
        when(returnService.searchReturns(any(), any())).thenReturn(emptyPage);
        mockMvc.perform(get("/api/returns/search")
                .param("query", "test")
                .param("sortBy", "badField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());
    }

    @Test
    void promotionController_InvalidSortField_DoesNotThrowPropertyReferenceException() throws Exception {
        // Given
        Page<PromotionDTO> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(promotionService.getAllPromotions(any())).thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "invalidPromotionField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "nonExistentProperty")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void allControllers_ValidSortFields_WorkCorrectly() throws Exception {
        // Given
        Page<CustomerDTO> customerPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        Page<ProductDTO> productPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        Page<SaleDTO> salePage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        Page<SupplierDTO> supplierPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        Page<ReturnDTO> returnPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        Page<PromotionDTO> promotionPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);

        when(customerService.getAllCustomers(any())).thenReturn(customerPage);
        when(productService.getAllProducts(any())).thenReturn(productPage);
        when(saleService.getAllSales(any(Pageable.class))).thenReturn(salePage);
        when(supplierService.getAllSuppliers(any())).thenReturn(supplierPage);
        when(returnService.getAllReturns(any())).thenReturn(returnPage);
        when(promotionService.getAllPromotions(any())).thenReturn(promotionPage);

        // When & Then - Valid sort fields should work correctly
        
        // Customer valid fields
        mockMvc.perform(get("/api/customers")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "email")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Product valid fields
        mockMvc.perform(get("/api/products")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                .param("sortBy", "price")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Sale valid fields
        mockMvc.perform(get("/api/sales")
                .param("sortBy", "saleDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Supplier valid fields
        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "rating")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Return valid fields
        mockMvc.perform(get("/api/returns")
                .param("sortBy", "returnDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        // Promotion valid fields
        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/promotions")
                .param("sortBy", "startDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void allControllers_InvalidSortDirection_UsesDefault() throws Exception {
        // Given
        Page<CustomerDTO> customerPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(customerService.getAllCustomers(any())).thenReturn(customerPage);

        // When & Then - Invalid sort directions should default to 'asc'
        mockMvc.perform(get("/api/customers")
                .param("sortBy", "name")
                .param("sortDir", "invalid"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "name")
                .param("sortDir", "up"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "name")
                .param("sortDir", "down"))
                .andExpect(status().isOk());
    }

    @Test
    void allControllers_CaseInsensitiveSortFields_WorkCorrectly() throws Exception {
        // Given
        Page<CustomerDTO> customerPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(customerService.getAllCustomers(any())).thenReturn(customerPage);

        // When & Then - Case insensitive sort fields should work
        mockMvc.perform(get("/api/customers")
                .param("sortBy", "NAME")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "Email")
                .param("sortDir", "DESC"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/customers")
                .param("sortBy", "createdAt")
                .param("sortDir", "ASC"))
                .andExpect(status().isOk());
    }

    @Test
    void paginationParameters_InvalidValues_AreValidated() throws Exception {
        // Given
        Page<CustomerDTO> customerPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10), 0);
        when(customerService.getAllCustomers(any())).thenReturn(customerPage);

        // When & Then - Invalid pagination parameters should be handled gracefully
        
        // Negative page should be handled
        mockMvc.perform(get("/api/customers")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk());

        // Zero size should be handled
        mockMvc.perform(get("/api/customers")
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isOk());

        // Large size should be capped
        mockMvc.perform(get("/api/customers")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }


}
