package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.SupplierDTO;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SupplierController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@Import(SupplierControllerTest.TestSecurityConfig.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupplierService supplierService;

    @Autowired
    private ObjectMapper objectMapper;

    private SupplierDTO testSupplierDTO;

    @BeforeEach
    void setUp() {
        testSupplierDTO = new SupplierDTO();
        testSupplierDTO.setId(1L);
        testSupplierDTO.setName("Test Supplier");
        testSupplierDTO.setContactPerson("John Smith");
        testSupplierDTO.setPhone("+1234567890");
        testSupplierDTO.setEmail("supplier@test.com");
        testSupplierDTO.setAddress("123 Supplier St");
        testSupplierDTO.setCity("Test City");
        testSupplierDTO.setCountry("Test Country");
        testSupplierDTO.setTaxNumber("TAX123456");
        testSupplierDTO.setPaymentTerms("NET30");
        testSupplierDTO.setDeliveryTerms("FOB");
        testSupplierDTO.setRating(4.5);
        testSupplierDTO.setStatus(Supplier.SupplierStatus.ACTIVE);
        testSupplierDTO.setTotalOrders(10);
        testSupplierDTO.setTotalAmount(BigDecimal.valueOf(50000));
        testSupplierDTO.setLastOrderDate(LocalDateTime.now());
        testSupplierDTO.setNotes("Test notes");
        testSupplierDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createSupplier_Success() throws Exception {
        // Given
        when(supplierService.createSupplier(any(SupplierDTO.class))).thenReturn(testSupplierDTO);

        // When & Then
        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSupplierDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Supplier"))
                .andExpect(jsonPath("$.email").value("supplier@test.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(supplierService).createSupplier(any(SupplierDTO.class));
    }

    @Test
    void getAllSuppliers_Success() throws Exception {
        // Given
        Page<SupplierDTO> supplierPage = new PageImpl<>(Arrays.asList(testSupplierDTO),
                PageRequest.of(0, 10), 1);
        when(supplierService.getAllSuppliers(any())).thenReturn(supplierPage);

        // When & Then
        mockMvc.perform(get("/api/suppliers")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(supplierService).getAllSuppliers(any());
    }

    @Test
    void getAllSuppliers_WithStatusFilter_Success() throws Exception {
        // Given
        Page<SupplierDTO> supplierPage = new PageImpl<>(Arrays.asList(testSupplierDTO),
                PageRequest.of(0, 10), 1);
        when(supplierService.getSuppliersByStatus(eq(Supplier.SupplierStatus.ACTIVE), any())).thenReturn(supplierPage);

        // When & Then
        mockMvc.perform(get("/api/suppliers")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(supplierService).getSuppliersByStatus(eq(Supplier.SupplierStatus.ACTIVE), any());
    }

    @Test
    void getAllSuppliers_WithInvalidStatus_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/suppliers")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).getSuppliersByStatus(any(), any());
        verify(supplierService, never()).getAllSuppliers(any());
    }

    @Test
    void getSupplierById_Success() throws Exception {
        // Given
        when(supplierService.getSupplierById(1L)).thenReturn(testSupplierDTO);

        // When & Then
        mockMvc.perform(get("/api/suppliers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier"))
                .andExpect(jsonPath("$.email").value("supplier@test.com"));

        verify(supplierService).getSupplierById(1L);
    }

    @Test
    void getSupplierById_NotFound() throws Exception {
        // Given
        when(supplierService.getSupplierById(1L)).thenThrow(new ResourceNotFoundException("Supplier not found"));

        // When & Then
        mockMvc.perform(get("/api/suppliers/1"))
                .andExpect(status().isNotFound());

        verify(supplierService).getSupplierById(1L);
    }

    @Test
    void getSupplierById_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/suppliers/0"))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).getSupplierById(any());
    }

    @Test
    void updateSupplier_Success() throws Exception {
        // Given
        when(supplierService.updateSupplier(eq(1L), any(SupplierDTO.class))).thenReturn(testSupplierDTO);

        // When & Then
        mockMvc.perform(put("/api/suppliers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSupplierDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier"));

        verify(supplierService).updateSupplier(eq(1L), any(SupplierDTO.class));
    }

    @Test
    void updateSupplier_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/suppliers/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSupplierDTO)))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).updateSupplier(any(), any());
    }

    @Test
    void deleteSupplier_Success() throws Exception {
        // Given
        doNothing().when(supplierService).deleteSupplier(1L);

        // When & Then
        mockMvc.perform(delete("/api/suppliers/1"))
                .andExpect(status().isNoContent());

        verify(supplierService).deleteSupplier(1L);
    }

    @Test
    void deleteSupplier_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/suppliers/0"))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).deleteSupplier(any());
    }

    @Test
    void searchSuppliers_Success() throws Exception {
        // Given
        Page<SupplierDTO> supplierPage = new PageImpl<>(Arrays.asList(testSupplierDTO),
                PageRequest.of(0, 10), 1);
        when(supplierService.searchSuppliers(eq("Test"), any())).thenReturn(supplierPage);

        // When & Then
        mockMvc.perform(get("/api/suppliers/search")
                .param("query", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Test Supplier"));

        verify(supplierService).searchSuppliers(eq("Test"), any());
    }

    @Test
    void getSupplierWithOrders_Success() throws Exception {
        // Given
        when(supplierService.getSupplierWithPurchaseOrders(1L)).thenReturn(testSupplierDTO);

        // When & Then
        mockMvc.perform(get("/api/suppliers/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Supplier"));

        verify(supplierService).getSupplierWithPurchaseOrders(1L);
    }

    @Test
    void getTopRatedSuppliers_Success() throws Exception {
        // Given
        List<SupplierDTO> suppliers = Arrays.asList(testSupplierDTO);
        when(supplierService.getTopRatedSuppliers(4.0)).thenReturn(suppliers);

        // When & Then
        mockMvc.perform(get("/api/suppliers/top-rated")
                .param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Supplier"));

        verify(supplierService).getTopRatedSuppliers(4.0);
    }

    @Test
    void getTopRatedSuppliers_InvalidRating_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/suppliers/top-rated")
                .param("minRating", "6.0"))
                .andExpect(status().isBadRequest());

        verify(supplierService, never()).getTopRatedSuppliers(any());
    }

    @Test
    void testSortingParameterValidation_ValidSortField() throws Exception {
        // Given
        Page<SupplierDTO> supplierPage = new PageImpl<>(Arrays.asList(testSupplierDTO),
                PageRequest.of(0, 10), 1);
        when(supplierService.getAllSuppliers(any())).thenReturn(supplierPage);

        // When & Then - Test valid sort fields
        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "rating")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        verify(supplierService, times(2)).getAllSuppliers(any());
    }

    @Test
    void testSortingParameterValidation_InvalidSortField_UsesDefault() throws Exception {
        // Given
        Page<SupplierDTO> supplierPage = new PageImpl<>(Arrays.asList(testSupplierDTO),
                PageRequest.of(0, 10), 1);
        when(supplierService.getAllSuppliers(any())).thenReturn(supplierPage);

        // When & Then - Invalid sort field should default to 'id'
        mockMvc.perform(get("/api/suppliers")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        verify(supplierService).getAllSuppliers(any());
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityConfig {

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public com.hamza.salesmanagementbackend.security.JwtTokenProvider jwtTokenProvider() {
            return org.mockito.Mockito.mock(com.hamza.salesmanagementbackend.security.JwtTokenProvider.class);
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public com.hamza.salesmanagementbackend.security.CustomUserDetailsService customUserDetailsService() {
            return org.mockito.Mockito.mock(com.hamza.salesmanagementbackend.security.CustomUserDetailsService.class);
        }
    }
}
