package com.hamza.salesmanagementbackend.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItemDTO {

    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    // Enhanced attributes matching the entity
    private BigDecimal originalUnitPrice;
    private BigDecimal costPrice;
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal subtotal;
    private BigDecimal totalPrice;
    private String serialNumbers;
    private String warrantyInfo;
    private String notes;
    @Builder.Default
    private Boolean isReturned = false;
    @Builder.Default
    private Integer returnedQuantity = 0;
    @Builder.Default
    private String unitOfMeasure = "PCS";

    // Custom constructors for specific use cases
    public SaleItemDTO(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.originalUnitPrice = unitPrice;
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxPercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.isReturned = false;
        this.returnedQuantity = 0;
        this.unitOfMeasure = "PCS";
        calculateTotals();
    }

    // Utility methods
    public void calculateTotals() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Apply discount
            if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate discount from percentage
                this.discountAmount = subtotal.multiply(discountPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if (discountAmount == null) {
                // No discount amount set, default to zero
                this.discountAmount = BigDecimal.ZERO.setScale(2);
            } else if (discountAmount.scale() != 2) {
                // Ensure existing discount amount has proper scale
                this.discountAmount = discountAmount.setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal afterDiscount = subtotal.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);

            // Calculate tax
            if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
                // Calculate tax from percentage
                this.taxAmount = afterDiscount.multiply(taxPercentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if (taxAmount == null) {
                // No tax amount set, default to zero
                this.taxAmount = BigDecimal.ZERO.setScale(2);
            } else if (taxAmount.scale() != 2) {
                // Ensure existing tax amount has proper scale
                this.taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);
            }

            // Calculate total - ensure 2 decimal places for monetary values
            this.totalPrice = afterDiscount.add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    public BigDecimal getLineTotal() {
        return totalPrice != null ? totalPrice : BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal getProfit() {
        if (costPrice == null || costPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal totalCost = costPrice.multiply(BigDecimal.valueOf(quantity != null ? quantity : 0));
        return getLineTotal().subtract(totalCost).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.equals(BigDecimal.ZERO) || getLineTotal().equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return getProfit().divide(getLineTotal(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean canReturn(int requestedQuantity) {
        return !Boolean.TRUE.equals(isReturned) &&
                (returnedQuantity != null ? returnedQuantity : 0) + requestedQuantity <= (quantity != null ? quantity : 0);
    }

    public BigDecimal getEffectiveUnitPrice() {
        if (unitPrice == null) return BigDecimal.ZERO;

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = unitPrice.multiply(discountPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return unitPrice.subtract(discount);
        }
        return unitPrice;
    }

    public int getRemainingQuantity() {
        return (quantity != null ? quantity : 0) - (returnedQuantity != null ? returnedQuantity : 0);
    }
}
