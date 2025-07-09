package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.CustomerDTO;
import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerDeletionTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .address("123 Main St")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .creditLimit(BigDecimal.valueOf(1000))
                .currentBalance(BigDecimal.ZERO)
                .loyaltyPoints(100)
                .totalPurchases(BigDecimal.valueOf(500))
                .isEmailVerified(true)
                .isPhoneVerified(false)
                .isDeleted(false)
                .build();
    }

    @Test
    void testSoftDeleteCustomer_Success() {
        // Given
        when(customerRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        customerService.softDeleteCustomer(1L, "admin", "Customer requested account closure");

        // Then
        verify(customerRepository).save(any(Customer.class));
        assertTrue(testCustomer.isDeleted());
        assertNotNull(testCustomer.getDeletedAt());
        assertEquals("admin", testCustomer.getDeletedBy());
        assertEquals("Customer requested account closure", testCustomer.getDeletionReason());
        assertEquals(Customer.CustomerStatus.INACTIVE, testCustomer.getCustomerStatus());
    }

    @Test
    void testSoftDeleteCustomer_CustomerNotFound() {
        // Given
        when(customerRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            customerService.softDeleteCustomer(1L, "admin", "Test deletion"));
    }

    @Test
    void testSoftDeleteCustomer_BlacklistedCustomer() {
        // Given
        testCustomer.setCustomerStatus(Customer.CustomerStatus.BLACKLISTED);
        when(customerRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testCustomer));

        // When & Then
        assertThrows(BusinessLogicException.class, () -> 
            customerService.softDeleteCustomer(1L, "admin", "Test deletion"));
    }

    @Test
    void testRestoreCustomer_Success() {
        // Given
        testCustomer.softDelete("admin", "Test deletion");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDTO result = customerService.restoreCustomer(1L);

        // Then
        verify(customerRepository).save(any(Customer.class));
        assertFalse(testCustomer.isDeleted());
        assertNull(testCustomer.getDeletedAt());
        assertNull(testCustomer.getDeletedBy());
        assertNull(testCustomer.getDeletionReason());
        assertEquals(Customer.CustomerStatus.ACTIVE, testCustomer.getCustomerStatus());
        assertNotNull(result);
    }

    @Test
    void testRestoreCustomer_CustomerNotDeleted() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When & Then
        assertThrows(BusinessLogicException.class, () -> 
            customerService.restoreCustomer(1L));
    }

    @Test
    void testHardDeleteCustomer_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.countSalesByCustomerId(1L)).thenReturn(0L);
        when(customerRepository.countReturnsByCustomerId(1L)).thenReturn(0L);

        // When
        customerService.hardDeleteCustomer(1L);

        // Then
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void testHardDeleteCustomer_WithAssociatedRecords() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.countSalesByCustomerId(1L)).thenReturn(3L);
        when(customerRepository.countReturnsByCustomerId(1L)).thenReturn(1L);

        // When
        customerService.hardDeleteCustomer(1L);

        // Then
        verify(customerRepository).deleteById(1L);
        // Should log warning about cascade deletion
    }

    @Test
    void testDeleteCustomer_DefaultsToSoftDelete() {
        // Given
        when(customerRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository).save(any(Customer.class));
        assertTrue(testCustomer.isDeleted());
    }

    @Test
    void testDeleteCustomer_WithForceFlag() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.countSalesByCustomerId(1L)).thenReturn(0L);
        when(customerRepository.countReturnsByCustomerId(1L)).thenReturn(0L);

        // When
        customerService.deleteCustomer(1L, true);

        // Then
        verify(customerRepository).deleteById(1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
