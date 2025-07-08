package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CustomerService force deletion functionality.
 * Verifies that the force delete parameter properly bypasses business validation
 * and allows cascade deletion to occur.
 */
@ExtendWith(MockitoExtension.class)
public class CustomerServiceForceDeleteTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(customerRepository);
    }

    @Test
    void deleteCustomer_SafeMode_WithSales_ThrowsException() {
        // Given
        Long customerId = 1L;
        Long salesCount = 3L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId, false));

        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Sales", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 3 associated sales", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_SALES", exception.getErrorCode());

        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository, never()).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_SafeMode_WithReturns_ThrowsException() {
        // Given
        Long customerId = 2L;
        Long salesCount = 0L;
        Long returnCount = 2L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId, false));

        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Returns", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 2 associated returns", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_RETURNS", exception.getErrorCode());

        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_ForceMode_WithSales_SucceedsWithLogging() {
        // Given
        Long customerId = 3L;
        Long salesCount = 5L;
        Long returnCount = 2L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId, true));

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_ForceMode_WithReturnsOnly_SucceedsWithLogging() {
        // Given
        Long customerId = 4L;
        Long salesCount = 0L;
        Long returnCount = 3L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId, true));

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_ForceMode_NoRelatedRecords_Succeeds() {
        // Given
        Long customerId = 5L;
        Long salesCount = 0L;
        Long returnCount = 0L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId, true));

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_SafeMode_NoRelatedRecords_Succeeds() {
        // Given
        Long customerId = 6L;
        Long salesCount = 0L;
        Long returnCount = 0L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId, false));

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_CustomerNotFound_ThrowsException() {
        // Given
        Long customerId = 999L;
        
        when(customerRepository.existsById(customerId)).thenReturn(false);

        // When & Then - Safe mode
        ResourceNotFoundException exception1 = assertThrows(ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(customerId, false));
        assertEquals("Customer not found with id: " + customerId, exception1.getMessage());

        // When & Then - Force mode
        ResourceNotFoundException exception2 = assertThrows(ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(customerId, true));
        assertEquals("Customer not found with id: " + customerId, exception2.getMessage());

        verify(customerRepository, times(2)).existsById(customerId);
        verify(customerRepository, never()).countSalesByCustomerId(customerId);
        verify(customerRepository, never()).countReturnsByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_DefaultMethod_CallsSafeModeVersion() {
        // Given
        Long customerId = 7L;
        Long salesCount = 1L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);

        // When & Then
        DataIntegrityException exception = assertThrows(DataIntegrityException.class,
                () -> customerService.deleteCustomer(customerId));

        assertEquals("CUSTOMER_HAS_SALES", exception.getErrorCode());
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository, never()).deleteById(customerId);
    }

    @Test
    void deleteCustomer_ForceMode_VerifyMethodCallOrder() {
        // Given
        Long customerId = 8L;
        Long salesCount = 2L;
        Long returnCount = 1L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        customerService.deleteCustomer(customerId, true);

        // Then - Verify the order of method calls
        var inOrder = inOrder(customerRepository);
        inOrder.verify(customerRepository).existsById(customerId);
        inOrder.verify(customerRepository).countSalesByCustomerId(customerId);
        inOrder.verify(customerRepository).countReturnsByCustomerId(customerId);
        inOrder.verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_ForceMode_WithLargeNumberOfRelatedRecords() {
        // Given
        Long customerId = 9L;
        Long salesCount = 100L;
        Long returnCount = 50L;
        
        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(customerRepository.countSalesByCustomerId(customerId)).thenReturn(salesCount);
        when(customerRepository.countReturnsByCustomerId(customerId)).thenReturn(returnCount);

        // When
        assertDoesNotThrow(() -> customerService.deleteCustomer(customerId, true));

        // Then
        verify(customerRepository).existsById(customerId);
        verify(customerRepository).countSalesByCustomerId(customerId);
        verify(customerRepository).countReturnsByCustomerId(customerId);
        verify(customerRepository).deleteById(customerId);
    }
}
