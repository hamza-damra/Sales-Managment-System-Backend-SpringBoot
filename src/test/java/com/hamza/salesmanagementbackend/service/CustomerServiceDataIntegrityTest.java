package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceDataIntegrityTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void deleteCustomer_WithAssociatedSales_ThrowsDataIntegrityException() {
        // Given
        Long customerId = 1L;
        Long salesCount = 5L;
        Long returnCount = 0L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Sales", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 5 associated sales", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_SALES", exception.getErrorCode());

        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository, never()).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_WithAssociatedReturns_ThrowsDataIntegrityException() {
        // Given
        Long customerId = 2L;
        Long salesCount = 0L;
        Long returnCount = 3L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Returns", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 3 associated returns", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_RETURNS", exception.getErrorCode());

        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_WithoutDependencies_Success() {
        // Given
        Long customerId = 3L;
        Long salesCount = 0L;
        Long returnCount = 0L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        customerService.deleteCustomer(customerId);

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_CustomerNotFound_ThrowsResourceNotFoundException() {
        // Given
        Long customerId = 999L;
        
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Customer not found with id: 999", exception.getMessage());
        verify(customerRepository).existsById(customerId);
        verify(customerRepository, never()).countSalesByCustomerId(customerId);
        verify(customerRepository, never()).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_WithSingleSale_CorrectMessage() {
        // Given
        Long customerId = 4L;
        Long salesCount = 1L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Cannot delete customer because they have 1 associated sale", exception.getUserMessage());
        assertTrue(exception.getSuggestion().contains("complete, cancel, or reassign all customer sales"));
    }

    @Test
    void deleteCustomer_WithSingleReturn_CorrectMessage() {
        // Given
        Long customerId = 5L;
        Long salesCount = 0L;
        Long returnCount = 1L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("Cannot delete customer because they have 1 associated return", exception.getUserMessage());
        assertTrue(exception.getSuggestion().contains("process all customer returns"));
    }
}
