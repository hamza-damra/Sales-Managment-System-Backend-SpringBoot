package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.SupplierDTO;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    /**
     * Creates a new supplier with validation
     */
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        // Validate email uniqueness
        validateEmailUniqueness(supplierDTO.getEmail(), null);
        
        // Validate tax number uniqueness if provided
        if (supplierDTO.getTaxNumber() != null && !supplierDTO.getTaxNumber().trim().isEmpty()) {
            validateTaxNumberUniqueness(supplierDTO.getTaxNumber(), null);
        }

        Supplier supplier = mapToEntity(supplierDTO);
        supplier = supplierRepository.save(supplier);
        return mapToDTO(supplier);
    }

    /**
     * Updates an existing supplier
     */
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // Validate email uniqueness (excluding current supplier)
        validateEmailUniqueness(supplierDTO.getEmail(), id);
        
        // Validate tax number uniqueness if provided
        if (supplierDTO.getTaxNumber() != null && !supplierDTO.getTaxNumber().trim().isEmpty()) {
            validateTaxNumberUniqueness(supplierDTO.getTaxNumber(), id);
        }

        // Update fields
        updateSupplierFields(existingSupplier, supplierDTO);
        existingSupplier = supplierRepository.save(existingSupplier);
        return mapToDTO(existingSupplier);
    }

    /**
     * Retrieves all suppliers with pagination
     */
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a supplier by ID
     */
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    /**
     * Deletes a supplier
     */
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // Check if supplier has active purchase orders
        if (supplier.getPurchaseOrders() != null && !supplier.getPurchaseOrders().isEmpty()) {
            boolean hasActivePurchaseOrders = supplier.getPurchaseOrders().stream()
                    .anyMatch(po -> po.getStatus() != null && 
                             (po.getStatus().name().equals("PENDING") || 
                              po.getStatus().name().equals("APPROVED") || 
                              po.getStatus().name().equals("ORDERED")));
            
            if (hasActivePurchaseOrders) {
                int activeOrderCount = (int) supplier.getPurchaseOrders().stream()
                        .filter(po -> po.getStatus() != null &&
                                     (po.getStatus().name().equals("PENDING") ||
                                      po.getStatus().name().equals("APPROVED") ||
                                      po.getStatus().name().equals("ORDERED")))
                        .count();
                throw DataIntegrityException.supplierHasPurchaseOrders(id, activeOrderCount);
            }
        }

        supplierRepository.delete(supplier);
    }

    /**
     * Searches suppliers with pagination
     */
    @Transactional(readOnly = true)
    public Page<SupplierDTO> searchSuppliers(String searchTerm, Pageable pageable) {
        return supplierRepository.searchSuppliers(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets suppliers by status
     */
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getSuppliersByStatus(Supplier.SupplierStatus status, Pageable pageable) {
        return supplierRepository.findByStatus(status, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets top-rated suppliers
     */
    @Transactional(readOnly = true)
    public List<SupplierDTO> getTopRatedSuppliers(Double minRating) {
        return supplierRepository.findTopRatedSuppliers(minRating)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets high-value suppliers
     */
    @Transactional(readOnly = true)
    public List<SupplierDTO> getHighValueSuppliers(BigDecimal minAmount) {
        return supplierRepository.findHighValueSuppliers(minAmount)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets supplier with purchase orders
     */
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierWithPurchaseOrders(Long id) {
        return supplierRepository.findByIdWithPurchaseOrders(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    /**
     * Updates supplier rating
     */
    public SupplierDTO updateSupplierRating(Long id, Double rating) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        if (rating < 0.0 || rating > 5.0) {
            throw new BusinessLogicException("Rating must be between 0.0 and 5.0");
        }

        supplier.updateRating(rating);
        supplier = supplierRepository.save(supplier);
        return mapToDTO(supplier);
    }

    /**
     * Finds supplier by email
     */
    @Transactional(readOnly = true)
    public Optional<SupplierDTO> findByEmail(String email) {
        return supplierRepository.findByEmail(email)
                .map(this::mapToDTO);
    }

    /**
     * Gets supplier analytics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSupplierAnalytics() {
        long totalSuppliers = supplierRepository.count();
        long activeSuppliers = supplierRepository.countByStatus(Supplier.SupplierStatus.ACTIVE);

        Double averageRating = supplierRepository.findAverageRating();
        if (averageRating == null) {
            averageRating = 0.0;
        }

        BigDecimal totalValue = supplierRepository.findTotalValue();
        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }

        long topPerformingSuppliers = supplierRepository.countByRatingGreaterThanEqual(4.0);

        // Get new suppliers this month
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newSuppliersThisMonth = supplierRepository.countByCreatedAtGreaterThanEqual(startOfMonth);

        return Map.of(
            "totalSuppliers", totalSuppliers,
            "activeSuppliers", activeSuppliers,
            "averageRating", Math.round(averageRating * 10.0) / 10.0,
            "totalValue", totalValue,
            "topPerformingSuppliers", topPerformingSuppliers,
            "newSuppliersThisMonth", newSuppliersThisMonth
        );
    }

    // Private helper methods

    private void validateEmailUniqueness(String email, Long excludeId) {
        if (email != null && !email.trim().isEmpty()) {
            supplierRepository.findByEmail(email)
                    .filter(supplier -> excludeId == null || !supplier.getId().equals(excludeId))
                    .ifPresent(supplier -> {
                        throw new BusinessLogicException("Email already exists: " + email);
                    });
        }
    }

    private void validateTaxNumberUniqueness(String taxNumber, Long excludeId) {
        if (supplierRepository.existsByTaxNumber(taxNumber)) {
            // Additional check to exclude current supplier if updating
            if (excludeId != null) {
                Optional<Supplier> existing = supplierRepository.findById(excludeId);
                if (existing.isPresent() && taxNumber.equals(existing.get().getTaxNumber())) {
                    return; // Same supplier, same tax number - OK
                }
            }
            throw new BusinessLogicException("Tax number already exists: " + taxNumber);
        }
    }

    private void updateSupplierFields(Supplier supplier, SupplierDTO supplierDTO) {
        supplier.setName(supplierDTO.getName());
        supplier.setContactPerson(supplierDTO.getContactPerson());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setCity(supplierDTO.getCity());
        supplier.setCountry(supplierDTO.getCountry());
        supplier.setTaxNumber(supplierDTO.getTaxNumber());
        supplier.setPaymentTerms(supplierDTO.getPaymentTerms());
        supplier.setDeliveryTerms(supplierDTO.getDeliveryTerms());
        supplier.setNotes(supplierDTO.getNotes());
        
        if (supplierDTO.getStatus() != null) {
            supplier.setStatus(supplierDTO.getStatus());
        }
        
        if (supplierDTO.getRating() != null) {
            supplier.updateRating(supplierDTO.getRating());
        }
    }

    private Supplier mapToEntity(SupplierDTO dto) {
        return Supplier.builder()
                .name(dto.getName())
                .contactPerson(dto.getContactPerson())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .city(dto.getCity())
                .country(dto.getCountry())
                .taxNumber(dto.getTaxNumber())
                .paymentTerms(dto.getPaymentTerms())
                .deliveryTerms(dto.getDeliveryTerms())
                .rating(dto.getRating() != null ? dto.getRating() : 0.0)
                .status(dto.getStatus() != null ? dto.getStatus() : Supplier.SupplierStatus.ACTIVE)
                .notes(dto.getNotes())
                .build();
    }

    private SupplierDTO mapToDTO(Supplier supplier) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setAddress(supplier.getAddress());
        dto.setCity(supplier.getCity());
        dto.setCountry(supplier.getCountry());
        dto.setTaxNumber(supplier.getTaxNumber());
        dto.setPaymentTerms(supplier.getPaymentTerms());
        dto.setDeliveryTerms(supplier.getDeliveryTerms());
        dto.setRating(supplier.getRating());
        dto.setStatus(supplier.getStatus());
        dto.setTotalOrders(supplier.getTotalOrders());
        dto.setTotalAmount(supplier.getTotalAmount());
        dto.setLastOrderDate(supplier.getLastOrderDate());
        dto.setNotes(supplier.getNotes());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());

        // Computed fields
        dto.setFullAddress(supplier.getFullAddress());
        dto.setIsActive(supplier.isActive());
        dto.setStatusDisplay(supplier.getStatus() != null ? supplier.getStatus().name() : "UNKNOWN");
        dto.setRatingDisplay(supplier.getRating() != null ? 
                String.format("%.1f/5.0", supplier.getRating()) : "Not Rated");

        return dto;
    }
}
