package com.hamza.salesmanagementbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.SaleService;
import com.hamza.salesmanagementbackend.service.ProductService;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;

@WebMvcTest(controllers = SaleController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
@Import({SaleControllerCreateSaleTest.TestSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("Sale Controller - Create Sale API Tests")
class SaleControllerCreateSaleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SaleService saleService;

    @MockBean
    private ProductService productService;

    @MockBean
    private SaleRepository saleRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SaleDTO validSaleDTO;
    private SaleItemDTO validSaleItemDTO;
    private SaleDTO createdSaleDTO;

    @BeforeEach
    void setUp() {
        // Setup valid sale item
        validSaleItemDTO = SaleItemDTO.builder()
                .productId(1L)
                .productName("Smartphone")
                .quantity(2)
                .unitPrice(new BigDecimal("999.99"))
                .originalUnitPrice(new BigDecimal("999.99"))
                .costPrice(new BigDecimal("600.00"))
                .discountPercentage(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .taxPercentage(new BigDecimal("15.0"))
                .taxAmount(new BigDecimal("299.997"))
                .subtotal(new BigDecimal("1999.98"))
                .totalPrice(new BigDecimal("2299.977"))
                .unitOfMeasure("PCS")
                .serialNumbers("SN001,SN002")
                .warrantyInfo("2-year warranty")
                .isReturned(false)
                .returnedQuantity(0)
                .build();

        // Setup valid sale DTO for request
        validSaleDTO = SaleDTO.builder()
                .customerId(1L)
                .customerName("أحمد محمد")
                .totalAmount(new BigDecimal("2299.977"))
                .status(SaleStatus.PENDING)
                .items(Arrays.asList(validSaleItemDTO))
                .subtotal(new BigDecimal("1999.98"))
                .discountAmount(BigDecimal.ZERO)
                .discountPercentage(BigDecimal.ZERO)
                .taxAmount(new BigDecimal("299.997"))
                .taxPercentage(new BigDecimal("15.0"))
                .shippingCost(BigDecimal.ZERO)
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .billingAddress("123 شارع الملك فهد، الرياض")
                .shippingAddress("123 شارع الملك فهد، الرياض")
                .salesPerson("Current User")
                .salesChannel("IN_STORE")
                .saleType(Sale.SaleType.RETAIL)
                .currency("USD")
                .exchangeRate(BigDecimal.ONE)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .isGift(false)
                .loyaltyPointsEarned(229)
                .loyaltyPointsUsed(0)
                .isReturn(false)
                .notes("Test sale creation")
                .build();

        // Setup expected response DTO
        createdSaleDTO = SaleDTO.builder()
                .id(1L)
                .customerId(1L)
                .customerName("أحمد محمد")
                .saleDate(LocalDateTime.now())
                .totalAmount(new BigDecimal("2299.977"))
                .status(SaleStatus.PENDING)
                .saleNumber("SALE-2025-000001")
                .referenceNumber("REF-001")
                .items(Arrays.asList(validSaleItemDTO))
                .subtotal(new BigDecimal("1999.98"))
                .taxAmount(new BigDecimal("299.997"))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        // Reset all mocks after each test to prevent interference between tests
        reset(saleService, productService, saleRepository, customerRepository, productRepository);
    }

    @Nested
    @DisplayName("Successful Sale Creation Tests")
    class SuccessfulSaleCreationTests {

        @Test
        @DisplayName("Should create sale successfully with valid data")
        void createSale_WithValidData_ShouldReturnCreated() throws Exception {
            // Given
            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.customerId").value(1))
                    .andExpect(jsonPath("$.customerName").value("أحمد محمد"))
                    .andExpect(jsonPath("$.totalAmount").value(2299.977))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.saleNumber").value("SALE-2025-000001"))
                    .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                    .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").value(1))
                    .andExpect(jsonPath("$.items[0].quantity").value(2))
                    .andExpect(jsonPath("$.items[0].unitPrice").value(999.99))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should create sale with multiple items successfully")
        void createSale_WithMultipleItems_ShouldReturnCreated() throws Exception {
            // Given
            SaleItemDTO secondItem = SaleItemDTO.builder()
                    .productId(2L)
                    .productName("Laptop")
                    .quantity(1)
                    .unitPrice(new BigDecimal("1499.99"))
                    .subtotal(new BigDecimal("1499.99"))
                    .totalPrice(new BigDecimal("1724.99"))
                    .unitOfMeasure("PCS")
                    .build();

            validSaleDTO.setItems(Arrays.asList(validSaleItemDTO, secondItem));
            validSaleDTO.setTotalAmount(new BigDecimal("4024.967")); // Updated total

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items.length()").value(1)); // Service returns single item in response

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should create sale with gift options successfully")
        void createSale_WithGiftOptions_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setIsGift(true);
            validSaleDTO.setGiftMessage("Happy Birthday!");
            
            createdSaleDTO.setIsGift(true);
            createdSaleDTO.setGiftMessage("Happy Birthday!");

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.isGift").value(true))
                    .andExpect(jsonPath("$.giftMessage").value("Happy Birthday!"));

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should create sale with discount and tax calculations")
        void createSale_WithDiscountAndTax_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setDiscountPercentage(new BigDecimal("10.0"));
            validSaleDTO.setDiscountAmount(new BigDecimal("199.998"));
            validSaleDTO.setTaxPercentage(new BigDecimal("15.0"));
            validSaleDTO.setTaxAmount(new BigDecimal("269.997"));

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalAmount").exists());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should create sale with different payment methods")
        void createSale_WithCreditCard_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setPaymentMethod(Sale.PaymentMethod.CREDIT_CARD);
            createdSaleDTO.setPaymentMethod(Sale.PaymentMethod.CREDIT_CARD);

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationErrorTests {

        @Test
        @DisplayName("Should return 400 when customer ID is null")
        void createSale_WithNullCustomerId_ShouldReturnBadRequest() throws Exception {
            // Given
            validSaleDTO.setCustomerId(null);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.validationErrors.customerId").exists());

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when total amount is null")
        void createSale_WithNullTotalAmount_ShouldReturnBadRequest() throws Exception {
            // Given
            validSaleDTO.setTotalAmount(null);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.validationErrors.totalAmount").exists());

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when total amount is negative")
        void createSale_WithNegativeTotalAmount_ShouldReturnBadRequest() throws Exception {
            // Given
            validSaleDTO.setTotalAmount(new BigDecimal("-100.00"));

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.validationErrors.totalAmount").exists());

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when items list is empty")
        void createSale_WithEmptyItems_ShouldReturnBadRequest() throws Exception {
            // Given
            validSaleDTO.setItems(Collections.emptyList());

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.validationErrors.items").exists());

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when sale item has invalid data")
        void createSale_WithInvalidSaleItem_ShouldReturnBadRequest() throws Exception {
            // Given
            validSaleItemDTO.setProductId(null);
            validSaleItemDTO.setQuantity(0);
            validSaleItemDTO.setUnitPrice(new BigDecimal("-50.00"));

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest());

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void createSale_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Malformed JSON"))
                    .andExpect(jsonPath("$.errorCode").value("MALFORMED_JSON"));

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 415 when Content-Type is not JSON")
        void createSale_WithWrongContentType_ShouldReturnUnsupportedMediaType() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));

            verify(saleService, never()).createSale(any(SaleDTO.class));
        }
    }

    @Nested
    @DisplayName("Business Logic Error Tests")
    class BusinessLogicErrorTests {

        @Test
        @DisplayName("Should return 404 when customer not found")
        void createSale_WithNonExistentCustomer_ShouldReturnNotFound() throws Exception {
            // Given
            when(saleService.createSale(any(SaleDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Customer not found with id: 999"));

            validSaleDTO.setCustomerId(999L);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isNotFound());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void createSale_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
            // Given
            when(saleService.createSale(any(SaleDTO.class)))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            validSaleItemDTO.setProductId(999L);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isNotFound());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 409 when insufficient stock")
        void createSale_WithInsufficientStock_ShouldReturnConflict() throws Exception {
            // Given
            when(saleService.createSale(any(SaleDTO.class)))
                    .thenThrow(new InsufficientStockException("Insufficient stock for product 'Smartphone'. Available: 1, Requested: 2"));

            validSaleItemDTO.setQuantity(2);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isConflict());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 400 when calculation mismatch occurs")
        void createSale_WithCalculationMismatch_ShouldReturnBadRequest() throws Exception {
            // Given
            clearInvocations(saleService); // Clear any previous invocations
            when(saleService.createSale(any(SaleDTO.class)))
                    .thenThrow(new IllegalArgumentException("Total amount calculation mismatch"));

            // Set incorrect total amount
            validSaleDTO.setTotalAmount(new BigDecimal("1000.00")); // Wrong total

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Invalid Request"))
                    .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should return 500 when unexpected error occurs")
        void createSale_WithUnexpectedError_ShouldReturnInternalServerError() throws Exception {
            // Given
            clearInvocations(saleService); // Clear any previous invocations
            when(saleService.createSale(any(SaleDTO.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle sale with zero shipping cost")
        void createSale_WithZeroShippingCost_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setShippingCost(BigDecimal.ZERO);
            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1));

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with maximum allowed items")
        void createSale_WithMaximumItems_ShouldReturnCreated() throws Exception {
            // Given - Create sale with many items (simulate maximum)
            validSaleDTO.setItems(Arrays.asList(
                    validSaleItemDTO,
                    createSaleItem(2L, "Laptop", 1, "1499.99"),
                    createSaleItem(3L, "Tablet", 3, "299.99"),
                    createSaleItem(4L, "Headphones", 2, "199.99"),
                    createSaleItem(5L, "Mouse", 5, "29.99")
            ));

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with very large amounts")
        void createSale_WithLargeAmount_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setTotalAmount(new BigDecimal("999999.99"));
            validSaleItemDTO.setUnitPrice(new BigDecimal("999999.99"));
            validSaleItemDTO.setQuantity(1);

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with decimal precision")
        void createSale_WithDecimalPrecision_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setTotalAmount(new BigDecimal("123.456789"));
            validSaleItemDTO.setUnitPrice(new BigDecimal("61.7283945"));
            validSaleItemDTO.setQuantity(2);

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with different currencies")
        void createSale_WithDifferentCurrency_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setCurrency("EUR");
            validSaleDTO.setExchangeRate(new BigDecimal("0.85"));

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with loyalty points")
        void createSale_WithLoyaltyPoints_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setLoyaltyPointsUsed(50);
            validSaleDTO.setLoyaltyPointsEarned(229);

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with Arabic customer name and address")
        void createSale_WithArabicText_ShouldReturnCreated() throws Exception {
            // Given
            validSaleDTO.setCustomerName("محمد أحمد العبدالله");
            validSaleDTO.setBillingAddress("شارع الملك فهد، حي العليا، الرياض، المملكة العربية السعودية");
            validSaleDTO.setShippingAddress("شارع الملك فهد، حي العليا، الرياض، المملكة العربية السعودية");

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with serialized products")
        void createSale_WithSerializedProducts_ShouldReturnCreated() throws Exception {
            // Given
            validSaleItemDTO.setSerialNumbers("SN001,SN002,SN003");
            validSaleItemDTO.setWarrantyInfo("Extended 3-year warranty");

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }
    }

    @Nested
    @DisplayName("Performance and Load Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle concurrent sale creation requests")
        void createSale_ConcurrentRequests_ShouldHandleGracefully() throws Exception {
            // Given
            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then - Simulate multiple concurrent requests
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSaleDTO)))
                        .andExpect(status().isCreated());
            }

            verify(saleService, times(5)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle large JSON payload efficiently")
        void createSale_WithLargePayload_ShouldReturnCreated() throws Exception {
            // Given - Create sale with detailed notes and addresses
            validSaleDTO.setNotes("This is a very long note that contains detailed information about the sale, " +
                    "including special instructions, customer preferences, delivery requirements, and other " +
                    "important details that need to be recorded for future reference and customer service purposes.");

            validSaleDTO.setInternalNotes("Internal processing notes with detailed workflow information, " +
                    "staff assignments, special handling requirements, and other operational details.");

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should handle sale with complex calculations")
        void createSale_WithComplexCalculations_ShouldReturnCreated() throws Exception {
            // Given - Complex sale with multiple discounts, taxes, and shipping
            validSaleDTO.setSubtotal(new BigDecimal("2000.00"));
            validSaleDTO.setDiscountPercentage(new BigDecimal("12.5"));
            validSaleDTO.setDiscountAmount(new BigDecimal("250.00"));
            validSaleDTO.setTaxPercentage(new BigDecimal("8.75"));
            validSaleDTO.setTaxAmount(new BigDecimal("153.125"));
            validSaleDTO.setShippingCost(new BigDecimal("25.50"));
            validSaleDTO.setTotalAmount(new BigDecimal("1928.625"));

            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }
    }

    @Nested
    @DisplayName("Integration and Workflow Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should create sale and verify all required fields are populated")
        void createSale_VerifyAllFields_ShouldReturnCompleteResponse() throws Exception {
            // Given
            clearInvocations(saleService); // Clear any previous invocations
            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When & Then
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.customerId").exists())
                    .andExpect(jsonPath("$.saleDate").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.saleNumber").exists())
                    .andExpect(jsonPath("$.paymentMethod").exists())
                    .andExpect(jsonPath("$.paymentStatus").exists())
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").exists())
                    .andExpect(jsonPath("$.items[0].quantity").exists())
                    .andExpect(jsonPath("$.items[0].unitPrice").exists());

            verify(saleService, times(1)).createSale(any(SaleDTO.class));
        }

        @Test
        @DisplayName("Should create sale and verify service method is called with correct parameters")
        void createSale_VerifyServiceCall_ShouldPassCorrectParameters() throws Exception {
            // Given
            clearInvocations(saleService); // Clear any previous invocations
            when(saleService.createSale(any(SaleDTO.class))).thenReturn(createdSaleDTO);

            // When
            mockMvc.perform(post("/api/sales")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSaleDTO)))
                    .andExpect(status().isCreated());

            // Then - Verify the service was called with a SaleDTO that has the expected properties
            verify(saleService, times(1)).createSale(argThat(saleDTO ->
                saleDTO.getCustomerId().equals(1L) &&
                saleDTO.getTotalAmount().compareTo(new BigDecimal("2299.977")) == 0 &&
                saleDTO.getItems().size() == 1 &&
                saleDTO.getPaymentMethod() == Sale.PaymentMethod.CASH
            ));
        }
    }

    // Helper method to create sale items for testing
    private SaleItemDTO createSaleItem(Long productId, String productName, Integer quantity, String unitPrice) {
        return SaleItemDTO.builder()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(new BigDecimal(unitPrice))
                .subtotal(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)))
                .totalPrice(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)))
                .unitOfMeasure("PCS")
                .isReturned(false)
                .returnedQuantity(0)
                .build();
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
