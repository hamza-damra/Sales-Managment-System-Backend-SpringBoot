package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.PurchaseOrderService;
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
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    private PurchaseOrderDTO testPurchaseOrderDTO;
    private PurchaseOrderItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        testItemDTO = PurchaseOrderItemDTO.builder()
                .productId(1L)
                .productName("Test Product")
                .productSku("TEST-001")
                .quantity(100)
                .unitCost(BigDecimal.valueOf(25.50))
                .totalPrice(BigDecimal.valueOf(2550.00))
                .build();

        testPurchaseOrderDTO = PurchaseOrderDTO.builder()
                .id(1L)
                .orderNumber("PO-2024-001")
                .supplierId(1L)
                .supplierName("Test Supplier")
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(LocalDateTime.now().plusDays(10))
                .status(PurchaseOrder.PurchaseOrderStatus.PENDING)
                .priority(PurchaseOrder.OrderPriority.NORMAL)
                .shippingAddress("123 Test Street")
                .taxRate(15.0)
                .items(Arrays.asList(testItemDTO))
                .subtotal(BigDecimal.valueOf(2550.00))
                .taxAmount(BigDecimal.valueOf(382.50))
                .totalAmount(BigDecimal.valueOf(2932.50))
                .notes("Test order")
                .itemsCount(1)
                .isFullyReceived(false)
                .receivingProgress(0.0)
                .build();
    }

    @Test
    void createPurchaseOrder_ShouldReturnCreated_WhenValidData() throws Exception {
        // Given
        when(purchaseOrderService.createPurchaseOrder(any(PurchaseOrderDTO.class)))
                .thenReturn(testPurchaseOrderDTO);

        // When & Then
        mockMvc.perform(post("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPurchaseOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO-2024-001")))
                .andExpect(jsonPath("$.supplierId", is(1)))
                .andExpect(jsonPath("$.supplierName", is("Test Supplier")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(2932.50)))
                .andExpect(jsonPath("$.itemsCount", is(1)));

        verify(purchaseOrderService).createPurchaseOrder(any(PurchaseOrderDTO.class));
    }

    @Test
    void getAllPurchaseOrders_ShouldReturnPagedResults() throws Exception {
        // Given
        Page<PurchaseOrderDTO> mockPage = new PageImpl<>(Arrays.asList(testPurchaseOrderDTO));
        when(purchaseOrderService.getAllPurchaseOrders(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "orderDate")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].orderNumber", is("PO-2024-001")));

        verify(purchaseOrderService).getAllPurchaseOrders(any(), any(), any(), any(), any(), any());
    }

    @Test
    void getPurchaseOrderById_ShouldReturnOrder_WhenExists() throws Exception {
        // Given
        when(purchaseOrderService.getPurchaseOrderById(1L)).thenReturn(testPurchaseOrderDTO);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO-2024-001")))
                .andExpect(jsonPath("$.supplierId", is(1)));

        verify(purchaseOrderService).getPurchaseOrderById(1L);
    }

    @Test
    void getPurchaseOrderById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Given
        when(purchaseOrderService.getPurchaseOrderById(1L))
                .thenThrow(new ResourceNotFoundException("Purchase order not found"));

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/1"))
                .andExpect(status().isNotFound());

        verify(purchaseOrderService).getPurchaseOrderById(1L);
    }

    @Test
    void updatePurchaseOrder_ShouldReturnUpdated_WhenValidData() throws Exception {
        // Given
        when(purchaseOrderService.updatePurchaseOrder(eq(1L), any(PurchaseOrderDTO.class)))
                .thenReturn(testPurchaseOrderDTO);

        // When & Then
        mockMvc.perform(put("/api/purchase-orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPurchaseOrderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO-2024-001")));

        verify(purchaseOrderService).updatePurchaseOrder(eq(1L), any(PurchaseOrderDTO.class));
    }

    @Test
    void deletePurchaseOrder_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // Given
        doNothing().when(purchaseOrderService).deletePurchaseOrder(1L);

        // When & Then
        mockMvc.perform(delete("/api/purchase-orders/1"))
                .andExpect(status().isNoContent());

        verify(purchaseOrderService).deletePurchaseOrder(1L);
    }

    @Test
    void searchPurchaseOrders_ShouldReturnResults_WhenValidQuery() throws Exception {
        // Given
        Page<PurchaseOrderDTO> mockPage = new PageImpl<>(Arrays.asList(testPurchaseOrderDTO));
        when(purchaseOrderService.searchPurchaseOrders(eq("test"), any()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/search")
                        .param("query", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderNumber", is("PO-2024-001")));

        verify(purchaseOrderService).searchPurchaseOrders(eq("test"), any());
    }

    @Test
    void updatePurchaseOrderStatus_ShouldReturnUpdated_WhenValidStatus() throws Exception {
        // Given
        testPurchaseOrderDTO.setStatus(PurchaseOrder.PurchaseOrderStatus.APPROVED);
        when(purchaseOrderService.updatePurchaseOrderStatus(eq(1L), any(), any(), any()))
                .thenReturn(testPurchaseOrderDTO);

        Map<String, Object> statusRequest = new HashMap<>();
        statusRequest.put("status", "APPROVED");
        statusRequest.put("notes", "Approved by manager");

        // When & Then
        mockMvc.perform(put("/api/purchase-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(purchaseOrderService).updatePurchaseOrderStatus(eq(1L), 
                eq(PurchaseOrder.PurchaseOrderStatus.APPROVED), eq("Approved by manager"), any());
    }

    @Test
    void approvePurchaseOrder_ShouldReturnApproved_WhenValidRequest() throws Exception {
        // Given
        testPurchaseOrderDTO.setStatus(PurchaseOrder.PurchaseOrderStatus.APPROVED);
        when(purchaseOrderService.approvePurchaseOrder(eq(1L), any(), any()))
                .thenReturn(testPurchaseOrderDTO);

        Map<String, String> approvalRequest = new HashMap<>();
        approvalRequest.put("approvalNotes", "Approved for Q1 procurement");
        approvalRequest.put("approvedBy", "manager@test.com");

        // When & Then
        mockMvc.perform(put("/api/purchase-orders/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        verify(purchaseOrderService).approvePurchaseOrder(eq(1L), 
                eq("Approved for Q1 procurement"), eq("manager@test.com"));
    }

    @Test
    void getPurchaseOrdersBySupplier_ShouldReturnResults_WhenValidSupplierId() throws Exception {
        // Given
        Page<PurchaseOrderDTO> mockPage = new PageImpl<>(Arrays.asList(testPurchaseOrderDTO));
        when(purchaseOrderService.getAllPurchaseOrders(any(), any(), eq(1L), any(), any(), any()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/supplier/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].supplierId", is(1)));

        verify(purchaseOrderService).getAllPurchaseOrders(any(), any(), eq(1L), any(), any(), any());
    }

    @Test
    void getAnalytics_ShouldReturnPlaceholderData() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/purchase-orders/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("analytics endpoint")));
    }

    @Test
    void generatePdf_ShouldReturnPlaceholderResponse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/purchase-orders/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void sendToSupplier_ShouldReturnSuccessResponse() throws Exception {
        // Given
        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("sendMethod", "EMAIL");
        sendRequest.put("recipientEmail", "supplier@test.com");
        sendRequest.put("subject", "Purchase Order PO-2024-001");
        sendRequest.put("includePdf", true);

        // When & Then
        mockMvc.perform(post("/api/purchase-orders/1/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("sent successfully")));
    }
}
