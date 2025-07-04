package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.service.SaleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@WebMvcTest(SaleController.class)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaleService saleService;

    @Autowired
    private ObjectMapper objectMapper;

    private SaleDTO testSaleDTO;
    private SaleItemDTO testSaleItemDTO;

    @BeforeEach
    void setUp() {
        testSaleItemDTO = SaleItemDTO.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(99.99))
                .subtotal(BigDecimal.valueOf(199.98))
                .build();

        testSaleDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .saleDate(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(199.98))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .items(Arrays.asList(testSaleItemDTO))
                .build();
    }

    @Test
    void createSale_Success() throws Exception {
        // Given
        when(saleService.createSale(any(SaleDTO.class))).thenReturn(testSaleDTO);

        // When & Then
        mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSaleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));

        verify(saleService).createSale(any(SaleDTO.class));
    }

    @Test
    void getAllSales_Success() throws Exception {
        // Given
        List<SaleDTO> sales = Arrays.asList(testSaleDTO);
        when(saleService.getAllSales()).thenReturn(sales);

        // When & Then
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].totalAmount").value(199.98));

        verify(saleService).getAllSales();
    }

    @Test
    void getSaleById_Success() throws Exception {
        // Given
        when(saleService.getSaleById(1L)).thenReturn(testSaleDTO);

        // When & Then
        mockMvc.perform(get("/api/sales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"));

        verify(saleService).getSaleById(1L);
    }

    @Test
    void updateSale_Success() throws Exception {
        // Given
        when(saleService.updateSale(eq(1L), any(SaleDTO.class))).thenReturn(testSaleDTO);

        // When & Then
        mockMvc.perform(put("/api/sales/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testSaleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(199.98));

        verify(saleService).updateSale(eq(1L), any(SaleDTO.class));
    }

    @Test
    void deleteSale_Success() throws Exception {
        // Given
        doNothing().when(saleService).deleteSale(1L);

        // When & Then
        mockMvc.perform(delete("/api/sales/1"))
                .andExpect(status().isNoContent());

        verify(saleService).deleteSale(1L);
    }

    @Test
    void getSalesByCustomer_Success() throws Exception {
        // Given
        List<SaleDTO> sales = Arrays.asList(testSaleDTO);
        when(saleService.getSalesByCustomer(1L)).thenReturn(sales);

        // When & Then
        mockMvc.perform(get("/api/sales/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].totalAmount").value(199.98));

        verify(saleService).getSalesByCustomer(1L);
    }
}
