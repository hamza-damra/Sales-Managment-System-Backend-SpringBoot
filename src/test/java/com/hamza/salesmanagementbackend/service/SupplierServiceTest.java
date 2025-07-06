package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.SupplierDTO;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.SupplierRepository;
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
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;
    private SupplierDTO testSupplierDTO;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Test Supplier")
                .contactPerson("John Smith")
                .phone("123456789")
                .email("supplier@test.com")
                .address("123 Supplier St")
                .city("Test City")
                .country("Test Country")
                .taxNumber("TAX123456")
                .paymentTerms("NET30")
                .deliveryTerms("FOB")
                .rating(4.5)
                .status(Supplier.SupplierStatus.ACTIVE)
                .totalOrders(10)
                .totalAmount(BigDecimal.valueOf(50000))
                .lastOrderDate(LocalDateTime.now())
                .notes("Test notes")
                .createdAt(LocalDateTime.now())
                .build();

        testSupplierDTO = new SupplierDTO();
        testSupplierDTO.setId(1L);
        testSupplierDTO.setName("Test Supplier");
        testSupplierDTO.setContactPerson("John Smith");
        testSupplierDTO.setPhone("123456789");
        testSupplierDTO.setEmail("supplier@test.com");
        testSupplierDTO.setAddress("123 Supplier St");
        testSupplierDTO.setCity("Test City");
        testSupplierDTO.setCountry("Test Country");
        testSupplierDTO.setTaxNumber("TAX123456");
        testSupplierDTO.setPaymentTerms("NET30");
        testSupplierDTO.setDeliveryTerms("FOB");
        testSupplierDTO.setRating(4.5);
        testSupplierDTO.setStatus(Supplier.SupplierStatus.ACTIVE);
        testSupplierDTO.setTotalOrders(10);
        testSupplierDTO.setTotalAmount(BigDecimal.valueOf(50000));
        testSupplierDTO.setLastOrderDate(LocalDateTime.now());
        testSupplierDTO.setNotes("Test notes");
        testSupplierDTO.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createSupplier_Success() {
        // Given
        when(supplierRepository.findByEmail(testSupplierDTO.getEmail())).thenReturn(Optional.empty());
        when(supplierRepository.existsByTaxNumber(testSupplierDTO.getTaxNumber())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        SupplierDTO result = supplierService.createSupplier(testSupplierDTO);

        // Then
        assertNotNull(result);
        assertEquals(testSupplierDTO.getEmail(), result.getEmail());
        assertEquals(testSupplierDTO.getName(), result.getName());
        assertEquals(testSupplierDTO.getTaxNumber(), result.getTaxNumber());
        verify(supplierRepository).findByEmail(testSupplierDTO.getEmail());
        verify(supplierRepository).existsByTaxNumber(testSupplierDTO.getTaxNumber());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void createSupplier_EmailAlreadyExists_ThrowsException() {
        // Given
        when(supplierRepository.findByEmail(testSupplierDTO.getEmail())).thenReturn(Optional.of(testSupplier));

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> supplierService.createSupplier(testSupplierDTO));
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(supplierRepository).findByEmail(testSupplierDTO.getEmail());
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void createSupplier_TaxNumberAlreadyExists_ThrowsException() {
        // Given
        when(supplierRepository.findByEmail(testSupplierDTO.getEmail())).thenReturn(Optional.empty());
        when(supplierRepository.existsByTaxNumber(testSupplierDTO.getTaxNumber())).thenReturn(true);

        // When & Then
        BusinessLogicException exception = assertThrows(BusinessLogicException.class,
                () -> supplierService.createSupplier(testSupplierDTO));
        assertTrue(exception.getMessage().contains("Tax number already exists"));
        verify(supplierRepository).findByEmail(testSupplierDTO.getEmail());
        verify(supplierRepository).existsByTaxNumber(testSupplierDTO.getTaxNumber());
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void createSupplier_WithoutTaxNumber_Success() {
        // Given
        testSupplierDTO.setTaxNumber(null);
        when(supplierRepository.findByEmail(testSupplierDTO.getEmail())).thenReturn(Optional.empty());
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        SupplierDTO result = supplierService.createSupplier(testSupplierDTO);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findByEmail(testSupplierDTO.getEmail());
        verify(supplierRepository, never()).findByTaxNumber(any());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void getAllSuppliers_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        when(supplierRepository.findAll(pageable)).thenReturn(supplierPage);

        // When
        Page<SupplierDTO> result = supplierService.getAllSuppliers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testSupplier.getEmail(), result.getContent().get(0).getEmail());
        verify(supplierRepository).findAll(pageable);
    }

    @Test
    void getSupplierById_Success() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When
        SupplierDTO result = supplierService.getSupplierById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testSupplier.getEmail(), result.getEmail());
        assertEquals(testSupplier.getName(), result.getName());
        verify(supplierRepository).findById(1L);
    }

    @Test
    void getSupplierById_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> supplierService.getSupplierById(1L));
        assertEquals("Supplier not found with id: 1", exception.getMessage());
        verify(supplierRepository).findById(1L);
    }

    @Test
    void updateSupplier_Success() {
        // Given
        SupplierDTO updateDTO = new SupplierDTO();
        updateDTO.setName("Updated Supplier");
        updateDTO.setEmail("updated@test.com");
        updateDTO.setTaxNumber("UPDATED123");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.findByEmail(updateDTO.getEmail())).thenReturn(Optional.empty());
        when(supplierRepository.existsByTaxNumber(updateDTO.getTaxNumber())).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        SupplierDTO result = supplierService.updateSupplier(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).findByEmail(updateDTO.getEmail());
        verify(supplierRepository).existsByTaxNumber(updateDTO.getTaxNumber());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> supplierService.updateSupplier(1L, testSupplierDTO));
        assertEquals("Supplier not found with id: 1", exception.getMessage());
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void deleteSupplier_Success() {
        // Given
        // Ensure supplier has no purchase orders (or empty list)
        testSupplier.setPurchaseOrders(null);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // When
        supplierService.deleteSupplier(1L);

        // Then
        verify(supplierRepository).findById(1L);
        verify(supplierRepository).delete(testSupplier);
    }

    @Test
    void deleteSupplier_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> supplierService.deleteSupplier(1L));
        assertEquals("Supplier not found with id: 1", exception.getMessage());
        verify(supplierRepository).findById(1L);
        verify(supplierRepository, never()).delete(any(Supplier.class));
    }

    @Test
    void searchSuppliers_Success() {
        // Given
        String searchTerm = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        when(supplierRepository.searchSuppliers(searchTerm, pageable)).thenReturn(supplierPage);

        // When
        Page<SupplierDTO> result = supplierService.searchSuppliers(searchTerm, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testSupplier.getName(), result.getContent().get(0).getName());
        verify(supplierRepository).searchSuppliers(searchTerm, pageable);
    }

    @Test
    void getSuppliersByStatus_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        Page<Supplier> supplierPage = new PageImpl<>(suppliers, pageable, 1);
        when(supplierRepository.findByStatus(Supplier.SupplierStatus.ACTIVE, pageable)).thenReturn(supplierPage);

        // When
        Page<SupplierDTO> result = supplierService.getSuppliersByStatus(Supplier.SupplierStatus.ACTIVE, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(Supplier.SupplierStatus.ACTIVE, result.getContent().get(0).getStatus());
        verify(supplierRepository).findByStatus(Supplier.SupplierStatus.ACTIVE, pageable);
    }

    @Test
    void getTopRatedSuppliers_Success() {
        // Given
        Double minRating = 4.0;
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findTopRatedSuppliers(minRating)).thenReturn(suppliers);

        // When
        List<SupplierDTO> result = supplierService.getTopRatedSuppliers(minRating);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRating() >= minRating);
        verify(supplierRepository).findTopRatedSuppliers(minRating);
    }

    @Test
    void getHighValueSuppliers_Success() {
        // Given
        BigDecimal minAmount = BigDecimal.valueOf(10000);
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findHighValueSuppliers(minAmount)).thenReturn(suppliers);

        // When
        List<SupplierDTO> result = supplierService.getHighValueSuppliers(minAmount);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTotalAmount().compareTo(minAmount) >= 0);
        verify(supplierRepository).findHighValueSuppliers(minAmount);
    }

    @Test
    void getSupplierWithPurchaseOrders_Success() {
        // Given
        when(supplierRepository.findByIdWithPurchaseOrders(1L)).thenReturn(Optional.of(testSupplier));

        // When
        SupplierDTO result = supplierService.getSupplierWithPurchaseOrders(1L);

        // Then
        assertNotNull(result);
        assertEquals(testSupplier.getName(), result.getName());
        verify(supplierRepository).findByIdWithPurchaseOrders(1L);
    }

    @Test
    void getSupplierWithPurchaseOrders_NotFound_ThrowsException() {
        // Given
        when(supplierRepository.findByIdWithPurchaseOrders(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> supplierService.getSupplierWithPurchaseOrders(1L));
        assertEquals("Supplier not found with id: 1", exception.getMessage());
        verify(supplierRepository).findByIdWithPurchaseOrders(1L);
    }
}
