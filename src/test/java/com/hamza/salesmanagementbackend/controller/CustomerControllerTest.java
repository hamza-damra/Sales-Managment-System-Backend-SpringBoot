package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.CustomerDTO;
import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setName("John Doe");
        testCustomerDTO.setFirstName("John");
        testCustomerDTO.setLastName("Doe");
        testCustomerDTO.setEmail("john.doe@example.com");
        testCustomerDTO.setPhone("123456789");
        testCustomerDTO.setAddress("123 Main St");
        testCustomerDTO.setCustomerType(Customer.CustomerType.REGULAR);
        testCustomerDTO.setCustomerStatus(Customer.CustomerStatus.ACTIVE);
        testCustomerDTO.setCreditLimit(BigDecimal.valueOf(1000));
        testCustomerDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createCustomer_Success() throws Exception {
        // Given
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(testCustomerDTO);

        // When & Then
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService).createCustomer(any(CustomerDTO.class));
    }

    @Test
    void getAllCustomers_Success() throws Exception {
        // Given
        List<CustomerDTO> customers = Arrays.asList(testCustomerDTO);
        when(customerService.getAllCustomers()).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(customerService).getAllCustomers();
    }

    @Test
    void getAllCustomersWithPagination_Success() throws Exception {
        // Given
        Page<CustomerDTO> customerPage = new PageImpl<>(Arrays.asList(testCustomerDTO),
                PageRequest.of(0, 10), 1);
        when(customerService.getAllCustomers(any())).thenReturn(customerPage);

        // When & Then
        mockMvc.perform(get("/api/customers/page")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(customerService).getAllCustomers(any());
    }

    @Test
    void getCustomerById_Success() throws Exception {
        // Given
        when(customerService.getCustomerById(1L)).thenReturn(testCustomerDTO);

        // When & Then
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    void updateCustomer_Success() throws Exception {
        // Given
        when(customerService.updateCustomer(eq(1L), any(CustomerDTO.class))).thenReturn(testCustomerDTO);

        // When & Then
        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService).updateCustomer(eq(1L), any(CustomerDTO.class));
    }

    @Test
    void deleteCustomer_Success() throws Exception {
        // Given
        doNothing().when(customerService).deleteCustomer(1L);

        // When & Then
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void searchCustomers_Success() throws Exception {
        // Given
        List<CustomerDTO> customers = Arrays.asList(testCustomerDTO);
        when(customerService.searchCustomersByName("John")).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers/search")
                .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(customerService).searchCustomersByName("John");
    }
}
