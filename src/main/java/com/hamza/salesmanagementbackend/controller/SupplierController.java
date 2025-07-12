package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.SupplierDTO;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.SupplierService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApplicationConstants.API_SUPPLIERS)
@CrossOrigin(origins = "*")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Page<SupplierDTO>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createSupplierSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<SupplierDTO> suppliers;
        if (status != null && !status.trim().isEmpty()) {
            try {
                Supplier.SupplierStatus supplierStatus = Supplier.SupplierStatus.valueOf(status.toUpperCase());
                suppliers = supplierService.getSuppliersByStatus(supplierStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            suppliers = supplierService.getAllSuppliers(pageable);
        }

        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SupplierDTO supplier = supplierService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSupplier);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id,
                                                     @Valid @RequestBody SupplierDTO supplierDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        SupplierDTO updatedSupplier = supplierService.updateSupplier(id, supplierDTO);
        return ResponseEntity.ok(updatedSupplier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<SupplierDTO>> searchSuppliers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Validate parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createSupplierSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);
        
        Page<SupplierDTO> suppliers = supplierService.searchSuppliers(query, pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<SupplierDTO> getSupplierWithOrders(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SupplierDTO supplier = supplierService.getSupplierWithPurchaseOrders(id);
            return ResponseEntity.ok(supplier);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<SupplierDTO>> getTopRatedSuppliers(
            @RequestParam(defaultValue = "4.0") Double minRating) {
        
        if (minRating < 0.0 || minRating > 5.0) {
            return ResponseEntity.badRequest().build();
        }

        List<SupplierDTO> suppliers = supplierService.getTopRatedSuppliers(minRating);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<SupplierDTO>> getHighValueSuppliers(
            @RequestParam(defaultValue = "10000") BigDecimal minAmount) {
        
        if (minAmount.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }

        List<SupplierDTO> suppliers = supplierService.getHighValueSuppliers(minAmount);
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<SupplierDTO> updateSupplierRating(@PathVariable Long id,
                                                           @RequestBody Map<String, Double> ratingRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Double rating = ratingRequest.get("rating");
        if (rating == null || rating < 0.0 || rating > 5.0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SupplierDTO updatedSupplier = supplierService.updateSupplierRating(id, rating);
            return ResponseEntity.ok(updatedSupplier);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getSupplierAnalytics() {
        Map<String, Object> analytics = supplierService.getSupplierAnalytics();
        return ResponseEntity.ok(analytics);
    }
}
