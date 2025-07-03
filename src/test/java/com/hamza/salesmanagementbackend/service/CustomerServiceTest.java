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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerDTO testCustomerDTO;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("123456789")
                .address("123 Main St")
                .customerType(Customer.CustomerType.REGULAR)
                .customerStatus(Customer.CustomerStatus.ACTIVE)
                .creditLimit(BigDecimal.valueOf(1000))
                .createdAt(LocalDateTime.now())
                .build();

        testCustomerDTO = new CustomerDTO();
        testCustomerDTO.setId(1L);
        testCustomerDTO.setName("John Doe");
        testCustomerDTO.setFirstName("John");
        testCustomerDTO.setLastName("Doe");
        testCustomerDTO.setEmail("john.doe@example.com");
        testCustomerDTO.setPhone("123456789");
        testCustomerDTO.setAddress("123 Main St");
        testCustomerDTO.setCustomerType(Customer.CustomerType.REGULAR);
        testCustomerDTO.setCustomerStatus(Customer.CustomerStatus.ACTIVE);
        testCustomerDTO.setCreditLimit(BigDecimal.valueOf(1000));
        testCustomerDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createCustomer_Success() {
        // Given
        when(customerRepository.findByEmail(testCustomerDTO.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDTO result = customerService.createCustomer(testCustomerDTO);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerDTO.getEmail(), result.getEmail());
        assertEquals(testCustomerDTO.getName(), result.getName());
        verify(customerRepository).findByEmail(testCustomerDTO.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_EmailAlreadyExists_ThrowsException() {
        // Given
        when(customerRepository.findByEmail(testCustomerDTO.getEmail())).thenReturn(Optional.of(testCustomer));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> customerService.createCustomer(testCustomerDTO));
        assertEquals("Email already exists: " + testCustomerDTO.getEmail(), exception.getMessage());
        verify(customerRepository).findByEmail(testCustomerDTO.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getAllCustomers_Success() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);

        // When
        List<CustomerDTO> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomer.getEmail(), result.get(0).getEmail());
        verify(customerRepository).findAll();
    }

    @Test
    void getAllCustomersWithPagination_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(testCustomer);
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(customerPage);

        // When
        Page<CustomerDTO> result = customerService.getAllCustomers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testCustomer.getEmail(), result.getContent().get(0).getEmail());
        verify(customerRepository).findAll(pageable);
    }

    @Test
    void getCustomerById_Success() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // When
        CustomerDTO result = customerService.getCustomerById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testCustomer.getEmail(), result.getEmail());
        assertEquals(testCustomer.getName(), result.getName());
        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(1L));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository).findById(1L);
    }

    @Test
    void updateCustomer_Success() {
        // Given
        CustomerDTO updateDTO = new CustomerDTO();
        updateDTO.setName("Jane Doe");
        updateDTO.setEmail("jane.doe@example.com");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.findByEmail(updateDTO.getEmail())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDTO result = customerService.updateCustomer(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).findByEmail(updateDTO.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.updateCustomer(1L, testCustomerDTO));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository).findById(1L);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void deleteCustomer_Success() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(true);

        // When
        customerService.deleteCustomer(1L);

        // Then
        verify(customerRepository).existsById(1L);
        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Given
        when(customerRepository.existsById(1L)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> customerService.deleteCustomer(1L));
        assertEquals("Customer not found with id: 1", exception.getMessage());
        verify(customerRepository).existsById(1L);
        verify(customerRepository, never()).deleteById(1L);
    }

    @Test
    void searchCustomersByName_Success() {
        // Given
        String searchName = "John";
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(customers);

        // When
        List<CustomerDTO> result = customerService.searchCustomersByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomer.getName(), result.get(0).getName());
        verify(customerRepository).findByNameContainingIgnoreCase(searchName);
    }

    @Test
    void findByEmail_Success() {
        // Given
        when(customerRepository.findByEmail(testCustomer.getEmail())).thenReturn(Optional.of(testCustomer));

        // When
        Optional<CustomerDTO> result = customerService.findByEmail(testCustomer.getEmail());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCustomer.getEmail(), result.get().getEmail());
        verify(customerRepository).findByEmail(testCustomer.getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        // Given
        when(customerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<CustomerDTO> result = customerService.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
        verify(customerRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void getCustomersCreatedBetween_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findByCreatedAtBetween(startDate, endDate)).thenReturn(customers);

        // When
        List<CustomerDTO> result = customerService.getCustomersCreatedBetween(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(customerRepository).findByCreatedAtBetween(startDate, endDate);
    }

    @Test
    void getNewCustomersCount_Success() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(customerRepository.countNewCustomersSince(since)).thenReturn(5L);

        // When
        Long result = customerService.getNewCustomersCount(since);

        // Then
        assertEquals(5L, result);
        verify(customerRepository).countNewCustomersSince(since);
    }

    @Test
    void updateCustomerStatus_Success() {
        // Given
        Customer.CustomerStatus newStatus = Customer.CustomerStatus.INACTIVE;
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDTO result = customerService.updateCustomerStatus(1L, newStatus);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomerType_Success() {
        // Given
        Customer.CustomerType newType = Customer.CustomerType.VIP;
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        CustomerDTO result = customerService.updateCustomerType(1L, newType);

        // Then
        assertNotNull(result);
        verify(customerRepository).findById(1L);
        verify(customerRepository).save(any(Customer.class));
    }
}
