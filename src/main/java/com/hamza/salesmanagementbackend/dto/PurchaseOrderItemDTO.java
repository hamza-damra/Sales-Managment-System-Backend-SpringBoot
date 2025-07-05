package com.hamza.salesmanagementbackend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderItemDTO {

    private Long id;

    private Long purchaseOrderId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    private String productSku;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit cost must be greater than 0")
    private BigDecimal unitCost;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    private BigDecimal totalPrice;

    private Integer receivedQuantity;

    private Integer pendingQuantity;

    private BigDecimal taxPercentage;

    private BigDecimal taxAmount;

    private BigDecimal discountPercentage;

    private BigDecimal discountAmount;

    private BigDecimal subtotal;

    private String notes;

    // Computed fields for frontend
    private Boolean isFullyReceived;
    private Boolean isPartiallyReceived;
    private Integer remainingQuantity;
    private BigDecimal receivedValue;
    private String receiptStatus;
}
