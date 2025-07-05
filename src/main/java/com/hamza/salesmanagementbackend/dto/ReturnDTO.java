package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Return;
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
public class ReturnDTO {

    private Long id;

    private String returnNumber;

    @NotNull(message = "Original sale ID is required")
    private Long originalSaleId;

    private String originalSaleNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerName;

    private LocalDateTime returnDate;

    @NotNull(message = "Return reason is required")
    private Return.ReturnReason reason;

    private Return.ReturnStatus status;

    @NotNull(message = "Total refund amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total refund amount cannot be negative")
    private BigDecimal totalRefundAmount;

    private String notes;

    private String processedBy;

    private LocalDateTime processedDate;

    private Return.RefundMethod refundMethod;

    private String refundReference;

    private LocalDateTime refundDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<ReturnItemDTO> items;

    // Computed fields for frontend
    private String statusDisplay;
    private String reasonDisplay;
    private Boolean canBeModified;
    private Boolean canBeProcessed;
    private Boolean isWithinReturnPeriod;
    private Integer totalItems;
    private String refundMethodDisplay;
    private Integer daysSinceReturn;
}
