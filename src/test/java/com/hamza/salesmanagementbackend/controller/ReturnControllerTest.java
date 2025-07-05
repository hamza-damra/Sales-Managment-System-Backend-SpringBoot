package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.entity.Return;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.ReturnService;
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

@WebMvcTest(controllers = ReturnController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@Import(ReturnControllerTest.TestSecurityConfig.class)
class ReturnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReturnService returnService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReturnDTO testReturnDTO;

    @BeforeEach
    void setUp() {
        testReturnDTO = new ReturnDTO();
        testReturnDTO.setId(1L);
        testReturnDTO.setReturnNumber("RET-001");
        testReturnDTO.setOriginalSaleId(1L);
        testReturnDTO.setCustomerId(1L);
        testReturnDTO.setReturnDate(LocalDateTime.now());
        testReturnDTO.setReason(Return.ReturnReason.DEFECTIVE);
        testReturnDTO.setStatus(Return.ReturnStatus.PENDING);
        testReturnDTO.setTotalRefundAmount(BigDecimal.valueOf(100.00));
        testReturnDTO.setNotes("Test return notes");
        testReturnDTO.setRefundMethod(Return.RefundMethod.ORIGINAL_PAYMENT);
        testReturnDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createReturn_Success() throws Exception {
        // Given
        when(returnService.createReturn(any(ReturnDTO.class))).thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReturnDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnNumber").value("RET-001"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("DEFECTIVE"));

        verify(returnService).createReturn(any(ReturnDTO.class));
    }

    @Test
    void getAllReturns_Success() throws Exception {
        // Given
        Page<ReturnDTO> returnPage = new PageImpl<>(Arrays.asList(testReturnDTO),
                PageRequest.of(0, 10), 1);
        when(returnService.getAllReturns(any())).thenReturn(returnPage);

        // When & Then
        mockMvc.perform(get("/api/returns")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "returnDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].returnNumber").value("RET-001"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(returnService).getAllReturns(any());
    }

    @Test
    void getAllReturns_WithStatusFilter_Success() throws Exception {
        // Given
        Page<ReturnDTO> returnPage = new PageImpl<>(Arrays.asList(testReturnDTO),
                PageRequest.of(0, 10), 1);
        when(returnService.getReturnsByStatus(eq(Return.ReturnStatus.PENDING), any())).thenReturn(returnPage);

        // When & Then
        mockMvc.perform(get("/api/returns")
                .param("status", "PENDING")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(returnService).getReturnsByStatus(eq(Return.ReturnStatus.PENDING), any());
    }

    @Test
    void getAllReturns_WithInvalidStatus_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/returns")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).getReturnsByStatus(any(), any());
        verify(returnService, never()).getAllReturns(any());
    }

    @Test
    void getReturnById_Success() throws Exception {
        // Given
        when(returnService.getReturnById(1L)).thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(get("/api/returns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnNumber").value("RET-001"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(returnService).getReturnById(1L);
    }

    @Test
    void getReturnById_NotFound() throws Exception {
        // Given
        when(returnService.getReturnById(1L)).thenThrow(new ResourceNotFoundException("Return not found"));

        // When & Then
        mockMvc.perform(get("/api/returns/1"))
                .andExpect(status().isNotFound());

        verify(returnService).getReturnById(1L);
    }

    @Test
    void getReturnById_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/returns/0"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).getReturnById(any());
    }

    @Test
    void updateReturn_Success() throws Exception {
        // Given
        when(returnService.updateReturn(eq(1L), any(ReturnDTO.class))).thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(put("/api/returns/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReturnDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnNumber").value("RET-001"));

        verify(returnService).updateReturn(eq(1L), any(ReturnDTO.class));
    }

    @Test
    void updateReturn_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/returns/0")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReturnDTO)))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).updateReturn(any(), any());
    }

    @Test
    void deleteReturn_Success() throws Exception {
        // Given
        doNothing().when(returnService).deleteReturn(1L);

        // When & Then
        mockMvc.perform(delete("/api/returns/1"))
                .andExpect(status().isNoContent());

        verify(returnService).deleteReturn(1L);
    }

    @Test
    void deleteReturn_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/returns/0"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).deleteReturn(any());
    }

    @Test
    void searchReturns_Success() throws Exception {
        // Given
        Page<ReturnDTO> returnPage = new PageImpl<>(Arrays.asList(testReturnDTO),
                PageRequest.of(0, 10), 1);
        when(returnService.searchReturns(eq("RET-001"), any())).thenReturn(returnPage);

        // When & Then
        mockMvc.perform(get("/api/returns/search")
                .param("query", "RET-001")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].returnNumber").value("RET-001"));

        verify(returnService).searchReturns(eq("RET-001"), any());
    }

    @Test
    void approveReturn_Success() throws Exception {
        // Given
        testReturnDTO.setStatus(Return.ReturnStatus.APPROVED);
        when(returnService.approveReturn(eq(1L), anyString())).thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(post("/api/returns/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approvedBy\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(returnService).approveReturn(eq(1L), eq("admin"));
    }

    @Test
    void approveReturn_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/returns/0/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approvedBy\":\"admin\"}"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).approveReturn(any(), any());
    }

    @Test
    void rejectReturn_Success() throws Exception {
        // Given
        testReturnDTO.setStatus(Return.ReturnStatus.REJECTED);
        when(returnService.rejectReturn(eq(1L), anyString(), anyString())).thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(post("/api/returns/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rejectedBy\":\"admin\",\"rejectionReason\":\"Invalid return request\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(returnService).rejectReturn(eq(1L), eq("admin"), eq("Invalid return request"));
    }

    @Test
    void processRefund_Success() throws Exception {
        // Given
        testReturnDTO.setStatus(Return.ReturnStatus.REFUNDED);
        when(returnService.processRefund(eq(1L), eq(Return.RefundMethod.ORIGINAL_PAYMENT), anyString()))
                .thenReturn(testReturnDTO);

        // When & Then
        mockMvc.perform(post("/api/returns/1/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refundMethod\":\"ORIGINAL_PAYMENT\",\"refundReference\":\"REF123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        verify(returnService).processRefund(eq(1L), eq(Return.RefundMethod.ORIGINAL_PAYMENT), eq("REF123"));
    }

    @Test
    void processRefund_InvalidRefundMethod_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/returns/1/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refundMethod\":\"INVALID_METHOD\",\"refundReference\":\"REF123\"}"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).processRefund(any(), any(), any());
    }

    @Test
    void getReturnsByCustomer_Success() throws Exception {
        // Given
        List<ReturnDTO> returns = Arrays.asList(testReturnDTO);
        when(returnService.getReturnsByCustomer(1L)).thenReturn(returns);

        // When & Then
        mockMvc.perform(get("/api/returns/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].returnNumber").value("RET-001"));

        verify(returnService).getReturnsByCustomer(1L);
    }

    @Test
    void getReturnsByCustomer_InvalidId_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/returns/customer/0"))
                .andExpect(status().isBadRequest());

        verify(returnService, never()).getReturnsByCustomer(any());
    }

    @Test
    void testSortingParameterValidation_ValidSortField() throws Exception {
        // Given
        Page<ReturnDTO> returnPage = new PageImpl<>(Arrays.asList(testReturnDTO),
                PageRequest.of(0, 10), 1);
        when(returnService.getAllReturns(any())).thenReturn(returnPage);

        // When & Then - Test valid sort fields
        mockMvc.perform(get("/api/returns")
                .param("sortBy", "returnDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/returns")
                .param("sortBy", "status")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        verify(returnService, times(2)).getAllReturns(any());
    }

    @Test
    void testSortingParameterValidation_InvalidSortField_UsesDefault() throws Exception {
        // Given
        Page<ReturnDTO> returnPage = new PageImpl<>(Arrays.asList(testReturnDTO),
                PageRequest.of(0, 10), 1);
        when(returnService.getAllReturns(any())).thenReturn(returnPage);

        // When & Then - Invalid sort field should default to 'id'
        mockMvc.perform(get("/api/returns")
                .param("sortBy", "invalidField")
                .param("sortDir", "asc"))
                .andExpect(status().isOk());

        verify(returnService).getAllReturns(any());
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
