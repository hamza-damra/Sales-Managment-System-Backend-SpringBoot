package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceDataIntegrityTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SaleService saleService;

    private Sale testSale;

    @BeforeEach
    void setUp() {
        testSale = Sale.builder()
                .id(1L)
                .status(SaleStatus.PENDING)
                .build();
    }

    @Test
    void deleteSale_WithAssociatedReturns_ThrowsDataIntegrityException() {
        // Given
        Long saleId = 1L;
        Long returnCount = 3L;
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
        when(saleRepository.countReturnsBySaleId(saleId)).thenReturn(returnCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> saleService.deleteSale(saleId));

        assertEquals("Sale", exception.getResourceType());
        assertEquals(saleId, exception.getResourceId());
        assertEquals("Returns", exception.getDependentResource());
        assertEquals("Cannot delete sale because it has 3 associated returns", exception.getUserMessage());
        assertEquals("SALE_HAS_RETURNS", exception.getErrorCode());

        verify(saleRepository).findById(saleId);
        verify(saleRepository).countReturnsBySaleId(saleId);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void deleteSale_WithoutReturns_Success() {
        // Given
        Long saleId = 1L;
        Long returnCount = 0L;
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
        when(saleRepository.countReturnsBySaleId(saleId)).thenReturn(returnCount);

        // When
        saleService.deleteSale(saleId);

        // Then
        verify(saleRepository).findById(saleId);
        verify(saleRepository).countReturnsBySaleId(saleId);
        verify(saleRepository).save(testSale);
        assertEquals(SaleStatus.CANCELLED, testSale.getStatus());
    }

    @Test
    void deleteSale_CompletedSale_ThrowsBusinessLogicException() {
        // Given
        Long saleId = 1L;
        testSale.setStatus(SaleStatus.COMPLETED);
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));

        // When & Then
        assertThrows(com.hamza.salesmanagementbackend.exception.BusinessLogicException.class,
                () -> saleService.deleteSale(saleId));

        verify(saleRepository).findById(saleId);
        verify(saleRepository, never()).countReturnsBySaleId(saleId);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void deleteSale_SaleNotFound_ThrowsResourceNotFoundException() {
        // Given
        Long saleId = 999L;
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> saleService.deleteSale(saleId));

        assertEquals("Sale not found with id: 999", exception.getMessage());
        verify(saleRepository).findById(saleId);
        verify(saleRepository, never()).countReturnsBySaleId(saleId);
        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    void deleteSale_WithSingleReturn_CorrectMessage() {
        // Given
        Long saleId = 1L;
        Long returnCount = 1L;
        
        when(saleRepository.findById(saleId)).thenReturn(Optional.of(testSale));
        when(saleRepository.countReturnsBySaleId(saleId)).thenReturn(returnCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> saleService.deleteSale(saleId));

        assertEquals("Cannot delete sale because it has 1 associated return", exception.getUserMessage());
        assertTrue(exception.getSuggestion().contains("process or cancel all associated returns"));
    }
}
