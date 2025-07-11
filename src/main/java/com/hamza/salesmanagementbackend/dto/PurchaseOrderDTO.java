package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderDTO {

    private Long id;

    private String orderNumber;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    private String supplierName;

    private LocalDateTime orderDate;

    private LocalDateTime expectedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private PurchaseOrder.PurchaseOrderStatus status;

    private PurchaseOrder.OrderPriority priority;

    private String paymentTerms;

    private String deliveryTerms;

    private String shippingAddress;

    private BigDecimal shippingCost;

    private Double taxRate;

    private String notes;

    private String createdBy;

    private String approvedBy;

    private LocalDateTime approvedDate;

    private LocalDateTime sentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PurchaseOrderItemDTO> items;

    // Computed fields for frontend
    private Integer itemsCount;
    private Boolean isFullyReceived;
    private Double receivingProgress;

    // Computed fields for frontend
    private String statusDisplay;
    private Boolean canBeModified;
    private Boolean canBeCancelled;
    private Boolean isOverdue;
    private Integer totalItems;
    private String deliveryStatus;
}
