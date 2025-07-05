package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.ReturnItem;
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
public class ReturnItemDTO {

    private Long id;

    private Long returnId;

    @NotNull(message = "Original sale item ID is required")
    private Long originalSaleItemId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    private String productSku;

    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be at least 1")
    private Integer returnQuantity;

    @NotNull(message = "Original unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original unit price must be greater than 0")
    private BigDecimal originalUnitPrice;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Refund amount cannot be negative")
    private BigDecimal refundAmount;

    private BigDecimal restockingFee;

    private String conditionNotes;

    private ReturnItem.ItemCondition itemCondition;

    private String serialNumbers;

    private Boolean isRestockable;

    private String disposalReason;

    // Computed fields for frontend
    private String conditionDisplay;
    private Boolean canBeRestocked;
    private BigDecimal netRefundAmount;
    private BigDecimal totalOriginalValue;
    private Boolean isValidReturnQuantity;
    private Integer originalSaleQuantity;
    private Integer alreadyReturnedQuantity;
    private Integer maxReturnableQuantity;
}
