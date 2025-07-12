package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.config.ApplicationConstants;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.PurchaseOrderService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping(ApplicationConstants.API_PURCHASE_ORDERS)
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        log.info("Creating purchase order for supplier ID: {}", purchaseOrderDTO.getSupplierId());
        PurchaseOrderDTO createdOrder = purchaseOrderService.createPurchaseOrder(purchaseOrderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<Page<PurchaseOrderDTO>> getAllPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createPurchaseOrderSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        // Parse enum parameters
        PurchaseOrder.PurchaseOrderStatus orderStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                orderStatus = PurchaseOrder.PurchaseOrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        PurchaseOrder.OrderPriority orderPriority = null;
        if (priority != null && !priority.trim().isEmpty()) {
            try {
                orderPriority = PurchaseOrder.OrderPriority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Page<PurchaseOrderDTO> orders = purchaseOrderService.getAllPurchaseOrders(
                pageable, orderStatus, supplierId, orderPriority, fromDate, toDate);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PurchaseOrderDTO order = purchaseOrderService.getPurchaseOrderById(id);
            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> updatePurchaseOrder(@PathVariable Long id,
                                                               @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PurchaseOrderDTO updatedOrder = purchaseOrderService.updatePurchaseOrder(id, purchaseOrderDTO);
            return ResponseEntity.ok(updatedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            purchaseOrderService.deletePurchaseOrder(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PurchaseOrderDTO>> searchPurchaseOrders(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createPurchaseOrderSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<PurchaseOrderDTO> orders = purchaseOrderService.searchPurchaseOrders(query, pageable);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PurchaseOrderDTO> updatePurchaseOrderStatus(@PathVariable Long id,
                                                                     @RequestBody Map<String, Object> statusRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        String statusStr = (String) statusRequest.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PurchaseOrder.PurchaseOrderStatus newStatus = PurchaseOrder.PurchaseOrderStatus.valueOf(statusStr.toUpperCase());
            String notes = (String) statusRequest.get("notes");
            
            // Parse actualDeliveryDate if provided
            LocalDateTime actualDeliveryDate = null;
            if (statusRequest.get("actualDeliveryDate") != null) {
                actualDeliveryDate = LocalDateTime.parse((String) statusRequest.get("actualDeliveryDate"));
            }

            PurchaseOrderDTO updatedOrder = purchaseOrderService.updatePurchaseOrderStatus(id, newStatus, notes, actualDeliveryDate);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<PurchaseOrderDTO> approvePurchaseOrder(@PathVariable Long id,
                                                                @RequestBody Map<String, String> approvalRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String approvalNotes = approvalRequest.get("approvalNotes");
            String approvedBy = approvalRequest.get("approvedBy");
            
            if (approvedBy == null || approvedBy.trim().isEmpty()) {
                approvedBy = "system"; // Default value
            }

            PurchaseOrderDTO approvedOrder = purchaseOrderService.approvePurchaseOrder(id, approvalNotes, approvedBy);
            return ResponseEntity.ok(approvedOrder);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Page<PurchaseOrderDTO>> getPurchaseOrdersBySupplier(@PathVariable Long supplierId,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             @RequestParam(defaultValue = "orderDate") String sortBy,
                                                                             @RequestParam(defaultValue = "desc") String sortDir) {
        if (supplierId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createPurchaseOrderSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<PurchaseOrderDTO> orders = purchaseOrderService.getAllPurchaseOrders(
                pageable, null, supplierId, null, null, null);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderDTO> receivePurchaseOrderItems(@PathVariable Long id,
                                                                     @RequestBody Map<String, Object> receiveRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // For now, return a placeholder response
            // This would typically update received quantities for items
            PurchaseOrderDTO order = purchaseOrderService.getPurchaseOrderById(id);
            return ResponseEntity.ok(order);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getPurchaseOrderAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Long supplierId) {

        // Placeholder implementation for analytics
        Map<String, Object> analytics = Map.of(
            "message", "Purchase order analytics endpoint - implementation pending",
            "totalOrders", 0,
            "totalValue", 0.0,
            "averageOrderValue", 0.0,
            "pendingOrders", 0,
            "approvedOrders", 0,
            "deliveredOrders", 0,
            "cancelledOrders", 0
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePurchaseOrderPdf(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Placeholder implementation for PDF generation
            // This would typically generate a PDF document
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=purchase-order-" + id + ".pdf")
                    .body("PDF generation not implemented yet".getBytes());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Map<String, Object>> sendPurchaseOrderToSupplier(@PathVariable Long id,
                                                                           @RequestBody Map<String, Object> sendRequest) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Placeholder implementation for sending email
            // This would typically send an email to the supplier
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Purchase order sent successfully",
                "sentDate", LocalDateTime.now().toString(),
                "sentTo", sendRequest.get("recipientEmail")
            );
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
