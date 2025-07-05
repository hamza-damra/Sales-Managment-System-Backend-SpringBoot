package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.dto.ReturnItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.ReturnRepository;
import com.hamza.salesmanagementbackend.repository.ReturnItemRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import com.hamza.salesmanagementbackend.repository.SaleItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnItemRepository returnItemRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @InjectMocks
    private ReturnService returnService;

    private Return testReturn;
    private ReturnDTO testReturnDTO;
    private Sale testSale;
    private Customer testCustomer;
    private Product testProduct;
    private SaleItem testSaleItem;

    @BeforeEach
    void setUp() {
        // Setup test customer
        testCustomer = Customer.builder()
                .id(1L)
                .name("Test Customer")
                .email("customer@test.com")
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(BigDecimal.valueOf(50.00))
                .stockQuantity(100)
                .build();

        // Setup test sale item
        testSaleItem = SaleItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();

        // Setup test sale
        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now().minusDays(5))
                .totalAmount(BigDecimal.valueOf(100.00))
                .status(SaleStatus.COMPLETED)
                .items(Arrays.asList(testSaleItem))
                .build();

        // Setup test return
        testReturn = Return.builder()
                .id(1L)
                .returnNumber("RET-001")
                .originalSale(testSale)
                .customer(testCustomer)
                .returnDate(LocalDateTime.now())
                .reason(Return.ReturnReason.DEFECTIVE)
                .status(Return.ReturnStatus.PENDING)
                .totalRefundAmount(BigDecimal.valueOf(100.00))
                .notes("Test return notes")
                .refundMethod(Return.RefundMethod.ORIGINAL_PAYMENT)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup test return DTO
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

        // Setup return item DTO
        ReturnItemDTO returnItemDTO = new ReturnItemDTO();
        returnItemDTO.setProductId(1L);
        returnItemDTO.setReturnQuantity(1);
        returnItemDTO.setOriginalSaleItemId(1L);
        returnItemDTO.setOriginalUnitPrice(BigDecimal.valueOf(50.00));
        returnItemDTO.setRefundAmount(BigDecimal.valueOf(50.00));
        returnItemDTO.setItemCondition(ReturnItem.ItemCondition.DAMAGED);
        testReturnDTO.setItems(Arrays.asList(returnItemDTO));
    }

    @Test
    void createReturn_Success() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(saleItemRepository.findById(1L)).thenReturn(Optional.of(testSaleItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnDTO result = returnService.createReturn(testReturnDTO);

        // Then
        assertNotNull(result);
        assertEquals(testReturnDTO.getReturnNumber(), result.getReturnNumber());
        assertEquals(testReturnDTO.getReason(), result.getReason());
        verify(saleRepository).findById(1L);
        verify(customerRepository).findById(1L);
        verify(saleItemRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void createReturn_SaleNotFound_ThrowsException() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> returnService.createReturn(testReturnDTO));
        assertTrue(exception.getMessage().contains("Original sale not found"));
        verify(saleRepository).findById(1L);
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    void createReturn_CustomerNotFound_ThrowsException() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> returnService.createReturn(testReturnDTO));
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(saleRepository).findById(1L);
        verify(customerRepository).findById(1L);
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    void createReturn_OutsideReturnPeriod_ThrowsException() {
        // Given
        testSale.setSaleDate(LocalDateTime.now().minusDays(35)); // Outside 30-day return period
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> returnService.createReturn(testReturnDTO));
        assertTrue(exception.getMessage().contains("Return request is outside the allowed return period"));
        verify(saleRepository).findById(1L);
        verify(customerRepository).findById(1L);
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    void getAllReturns_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Return> returns = Arrays.asList(testReturn);
        Page<Return> returnPage = new PageImpl<>(returns, pageable, 1);
        when(returnRepository.findAll(pageable)).thenReturn(returnPage);

        // When
        Page<ReturnDTO> result = returnService.getAllReturns(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReturn.getReturnNumber(), result.getContent().get(0).getReturnNumber());
        verify(returnRepository).findAll(pageable);
    }

    @Test
    void getReturnById_Success() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));

        // When
        ReturnDTO result = returnService.getReturnById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testReturn.getReturnNumber(), result.getReturnNumber());
        assertEquals(testReturn.getStatus(), result.getStatus());
        verify(returnRepository).findById(1L);
    }

    @Test
    void getReturnById_NotFound_ThrowsException() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> returnService.getReturnById(1L));
        assertEquals("Return not found with id: 1", exception.getMessage());
        verify(returnRepository).findById(1L);
    }

    @Test
    void updateReturn_Success() {
        // Given
        ReturnDTO updateDTO = new ReturnDTO();
        updateDTO.setNotes("Updated notes");
        updateDTO.setReason(Return.ReturnReason.CUSTOMER_CHANGE_MIND);

        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnDTO result = returnService.updateReturn(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(returnRepository).findById(1L);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void updateReturn_NotFound_ThrowsException() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> returnService.updateReturn(1L, testReturnDTO));
        assertEquals("Return not found with id: 1", exception.getMessage());
        verify(returnRepository).findById(1L);
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    void updateReturn_CannotBeModified_ThrowsException() {
        // Given
        testReturn.setStatus(Return.ReturnStatus.REFUNDED);
        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> returnService.updateReturn(1L, testReturnDTO));
        assertTrue(exception.getMessage().contains("cannot be modified"));
        verify(returnRepository).findById(1L);
        verify(returnRepository, never()).save(any(Return.class));
    }

    @Test
    void deleteReturn_Success() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));

        // When
        returnService.deleteReturn(1L);

        // Then
        verify(returnRepository).findById(1L);
        verify(returnRepository).delete(testReturn);
    }

    @Test
    void deleteReturn_NotFound_ThrowsException() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> returnService.deleteReturn(1L));
        assertEquals("Return not found with id: 1", exception.getMessage());
        verify(returnRepository).findById(1L);
        verify(returnRepository, never()).delete(any(Return.class));
    }

    @Test
    void approveReturn_Success() {
        // Given
        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnDTO result = returnService.approveReturn(1L, "admin");

        // Then
        assertNotNull(result);
        verify(returnRepository).findById(1L);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void rejectReturn_Success() {
        // Given
        String rejectionReason = "Invalid return request";
        when(returnRepository.findById(1L)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnDTO result = returnService.rejectReturn(1L, "admin", rejectionReason);

        // Then
        assertNotNull(result);
        verify(returnRepository).findById(1L);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void processRefund_Success() {
        // Given
        testReturn.setStatus(Return.ReturnStatus.APPROVED);
        String refundReference = "REF123";
        when(returnRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testReturn));
        when(returnRepository.save(any(Return.class))).thenReturn(testReturn);

        // When
        ReturnDTO result = returnService.processRefund(1L, Return.RefundMethod.ORIGINAL_PAYMENT, refundReference);

        // Then
        assertNotNull(result);
        verify(returnRepository).findByIdWithItems(1L);
        verify(returnRepository).save(any(Return.class));
    }

    @Test
    void searchReturns_Success() {
        // Given
        String searchTerm = "RET-001";
        Pageable pageable = PageRequest.of(0, 10);
        List<Return> returns = Arrays.asList(testReturn);
        Page<Return> returnPage = new PageImpl<>(returns, pageable, 1);
        when(returnRepository.searchReturns(searchTerm, pageable)).thenReturn(returnPage);

        // When
        Page<ReturnDTO> result = returnService.searchReturns(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testReturn.getReturnNumber(), result.getContent().get(0).getReturnNumber());
        verify(returnRepository).searchReturns(searchTerm, pageable);
    }

    @Test
    void getReturnsByStatus_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Return> returns = Arrays.asList(testReturn);
        Page<Return> returnPage = new PageImpl<>(returns, pageable, 1);
        when(returnRepository.findByStatus(Return.ReturnStatus.PENDING, pageable)).thenReturn(returnPage);

        // When
        Page<ReturnDTO> result = returnService.getReturnsByStatus(Return.ReturnStatus.PENDING, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Return.ReturnStatus.PENDING, result.getContent().get(0).getStatus());
        verify(returnRepository).findByStatus(Return.ReturnStatus.PENDING, pageable);
    }

    @Test
    void getReturnsByCustomer_Success() {
        // Given
        List<Return> returns = Arrays.asList(testReturn);
        when(returnRepository.findByCustomerId(1L)).thenReturn(returns);

        // When
        List<ReturnDTO> result = returnService.getReturnsByCustomer(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReturn.getReturnNumber(), result.get(0).getReturnNumber());
        verify(returnRepository).findByCustomerId(1L);
    }
}
