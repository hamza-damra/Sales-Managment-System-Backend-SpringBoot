package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.CustomerDTO;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.CustomerService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createCustomerSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            CustomerDTO customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id,
                                                     @Valid @RequestBody CustomerDTO customerDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id,
                                              @RequestParam(defaultValue = "soft") String deleteType,
                                              @RequestParam(required = false) String deletedBy,
                                              @RequestParam(required = false) String reason) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        switch (deleteType.toLowerCase()) {
            case "soft":
                customerService.softDeleteCustomer(id,
                    deletedBy != null ? deletedBy : "API_USER",
                    reason != null ? reason : "Customer deletion requested via API");
                break;
            case "hard":
                customerService.hardDeleteCustomer(id);
                break;
            case "force":
                customerService.deleteCustomer(id, true);
                break;
            default:
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<CustomerDTO> restoreCustomer(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        CustomerDTO restoredCustomer = customerService.restoreCustomer(id);
        return ResponseEntity.ok(restoredCustomer);
    }

    @GetMapping("/deleted")
    public ResponseEntity<Page<CustomerDTO>> getDeletedCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deletedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createCustomerSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<CustomerDTO> deletedCustomers = customerService.getDeletedCustomers(pageable);
        return ResponseEntity.ok(deletedCustomers);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Validate parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size);
        Page<CustomerDTO> customers = customerService.searchCustomers(query, pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Debug endpoint: Get all customers including deleted ones
     * This endpoint should only be used for debugging purposes
     */
    @GetMapping("/debug/all")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomersIncludingDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createCustomerSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<CustomerDTO> customers = customerService.getAllCustomersIncludingDeleted(pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Debug endpoint: Fix customers with NULL isDeleted values
     * This endpoint should only be used for maintenance purposes
     */
    @PostMapping("/debug/fix-null-deleted")
    public ResponseEntity<Map<String, Object>> fixCustomersWithNullIsDeleted() {
        int fixed = customerService.fixCustomersWithNullIsDeleted();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Fixed customers with NULL isDeleted values");
        response.put("customersFixed", fixed);
        return ResponseEntity.ok(response);
    }
}
