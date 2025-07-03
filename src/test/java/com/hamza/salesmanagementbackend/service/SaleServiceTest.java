package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
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
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private SaleService saleService;

    private Sale testSale;
    private SaleDTO testSaleDTO;
    private Customer testCustomer;
    private Product testProduct;
    private SaleItem testSaleItem;
    private SaleItemDTO testSaleItemDTO;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(100)
                .build();

        testSaleItem = SaleItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(99.99))
                .subtotal(BigDecimal.valueOf(199.98))
                .build();

        testSale = Sale.builder()
                .id(1L)
                .customer(testCustomer)
                .saleDate(LocalDateTime.now())
                .totalAmount(BigDecimal.valueOf(199.98))
                .paymentMethod(Sale.PaymentMethod.CASH)
                .paymentStatus(Sale.PaymentStatus.PAID)
                .saleType(Sale.SaleType.RETAIL)
                .deliveryStatus(Sale.DeliveryStatus.NOT_SHIPPED)
                .items(Arrays.asList(testSaleItem))
                .build();

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
    void createSale_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.createSale(testSaleDTO);

        // Then
        assertNotNull(result);
        assertEquals(testSaleDTO.getTotalAmount(), result.getTotalAmount());
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void createSale_CustomerNotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.createSale(testSaleDTO));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void createSale_ProductNotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.createSale(testSaleDTO));
        assertEquals("Product not found with id: 1", exception.getMessage());
        verify(customerRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void getAllSales_Success() {
        // Given
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findAll()).thenReturn(sales);

        // When
        List<SaleDTO> result = saleService.getAllSales();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSale.getTotalAmount(), result.get(0).getTotalAmount());
        verify(saleRepository).findAll();
    }

    @Test
    void getAllSalesWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Sale> sales = Arrays.asList(testSale);
        Page<Sale> salePage = new PageImpl<>(sales, pageable, 1);
        when(saleRepository.findAll(pageable)).thenReturn(salePage);

        // When
        Page<SaleDTO> result = saleService.getAllSales(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testSale.getTotalAmount(), result.getContent().get(0).getTotalAmount());
        verify(saleRepository).findAll(pageable);
    }

    @Test
    void getSaleById_Success() {
        // Given
        when(saleRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testSale));

        // When
        SaleDTO result = saleService.getSaleById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testSale.getTotalAmount(), result.getTotalAmount());
        assertEquals(testSale.getId(), result.getId());
        verify(saleRepository).findByIdWithItems(1L);
    }

    @Test
    void getSaleById_NotFound_ThrowsException() {
        // Given
        when(saleRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.getSaleById(1L));
        assertEquals("Sale not found with id: 1", exception.getMessage());
        verify(saleRepository).findByIdWithItems(1L);
    }

    @Test
    void updateSale_Success() {
        // Given
        SaleDTO updateDTO = SaleDTO.builder()
                .paymentStatus(Sale.PaymentStatus.PAID)
                .deliveryStatus(Sale.DeliveryStatus.DELIVERED)
                .build();

        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);

        // When
        SaleDTO result = saleService.updateSale(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(saleRepository).findById(1L);
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void updateSale_NotFound_ThrowsException() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.updateSale(1L, testSaleDTO));
        assertEquals("Sale not found with id: 1", exception.getMessage());
        verify(saleRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void deleteSale_Success() {
        // Given
        testSale.setStatus(SaleStatus.PENDING); // Set to PENDING so it can be deleted
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

        // When
        saleService.deleteSale(1L);

        // Then
        verify(saleRepository).findById(1L);
        verify(saleRepository).save(any(Sale.class)); // Saves with CANCELLED status
        verify(productRepository).save(any(Product.class)); // Restores inventory
    }

    @Test
    void deleteSale_NotFound_ThrowsException() {
        // Given
        when(saleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.deleteSale(1L));
        assertEquals("Sale not found with id: 1", exception.getMessage());
        verify(saleRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void deleteSale_CompletedSale_ThrowsException() {
        // Given
        testSale.setStatus(SaleStatus.COMPLETED);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(testSale));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> saleService.deleteSale(1L));
        assertEquals("Cannot delete completed sales", exception.getMessage());
        verify(saleRepository).findById(1L);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void getSalesByCustomer_Success() {
        // Given
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findByCustomerId(1L)).thenReturn(sales);

        // When
        List<SaleDTO> result = saleService.getSalesByCustomer(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSale.getTotalAmount(), result.get(0).getTotalAmount());
        verify(saleRepository).findByCustomerId(1L);
    }

    @Test
    void getSalesByDateRange_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findBySaleDateBetween(startDate, endDate)).thenReturn(sales);

        // When
        List<SaleDTO> result = saleService.getSalesByDateRange(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(saleRepository).findBySaleDateBetween(startDate, endDate);
    }
}
