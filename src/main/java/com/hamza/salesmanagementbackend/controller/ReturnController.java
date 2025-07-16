package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.entity.Return;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.ReturnService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/returns")
@CrossOrigin(origins = "*")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @GetMapping
    public ResponseEntity<Page<ReturnDTO>> getAllReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createReturnSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<ReturnDTO> returns;
        if (status != null && !status.trim().isEmpty()) {
            try {
                Return.ReturnStatus returnStatus = Return.ReturnStatus.valueOf(status.toUpperCase());
                returns = returnService.getReturnsByStatus(returnStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            returns = returnService.getAllReturns(pageable);
        }

        return ResponseEntity.ok(returns);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnDTO> getReturnById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReturnDTO returnDTO = returnService.getReturnById(id);
            return ResponseEntity.ok(returnDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ReturnDTO> createReturn(@Valid @RequestBody ReturnDTO returnDTO) {
        ReturnDTO createdReturn = returnService.createReturn(returnDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReturn);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReturnDTO> updateReturn(@PathVariable Long id,
                                                 @Valid @RequestBody ReturnDTO returnDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReturnDTO updatedReturn = returnService.updateReturn(id, returnDTO);
            return ResponseEntity.ok(updatedReturn);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReturn(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            returnService.deleteReturn(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ReturnDTO>> searchReturns(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "returnDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createReturnSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);
        
        Page<ReturnDTO> returns = returnService.searchReturns(query, pageable);
        return ResponseEntity.ok(returns);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ReturnDTO> approveReturn(@PathVariable Long id,
                                                  @RequestBody Map<String, String> approvalRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String approvedBy = approvalRequest.get("approvedBy");
        if (approvedBy == null || approvedBy.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReturnDTO approvedReturn = returnService.approveReturn(id, approvedBy);
            return ResponseEntity.ok(approvedReturn);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ReturnDTO> rejectReturn(@PathVariable Long id,
                                                 @RequestBody Map<String, String> rejectionRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String rejectedBy = rejectionRequest.get("rejectedBy");
        String rejectionReason = rejectionRequest.get("rejectionReason");
        
        if (rejectedBy == null || rejectedBy.trim().isEmpty() ||
            rejectionReason == null || rejectionReason.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReturnDTO rejectedReturn = returnService.rejectReturn(id, rejectedBy, rejectionReason);
            return ResponseEntity.ok(rejectedReturn);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ReturnDTO> processRefund(@PathVariable Long id,
                                                  @RequestBody Map<String, String> refundRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String refundMethodStr = refundRequest.get("refundMethod");
        String refundReference = refundRequest.get("refundReference");
        
        if (refundMethodStr == null || refundMethodStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Return.RefundMethod refundMethod = Return.RefundMethod.valueOf(refundMethodStr.toUpperCase());
            ReturnDTO processedReturn = returnService.processRefund(id, refundMethod, refundReference);
            return ResponseEntity.ok(processedReturn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ReturnDTO> getReturnWithItems(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ReturnDTO returnWithItems = returnService.getReturnWithItems(id);
            return ResponseEntity.ok(returnWithItems);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getReturnsByCustomer(@PathVariable Long customerId) {
        if (customerId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            return ResponseEntity.ok(returnService.getReturnsByCustomer(customerId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getReturnAnalytics() {
        // This would typically call a service method that aggregates return data
        // For now, returning a placeholder response
        Map<String, Object> analytics = Map.of(
            "message", "Return analytics endpoint - implementation pending",
            "totalReturns", 0,
            "pendingReturns", 0,
            "approvedReturns", 0,
            "totalRefundAmount", 0.0,
            "averageProcessingTime", 0.0
        );
        return ResponseEntity.ok(analytics);
    }
}
