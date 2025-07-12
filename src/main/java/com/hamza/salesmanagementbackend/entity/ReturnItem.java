package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "return_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Return returnEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_sale_item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private SaleItem originalSaleItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull(message = "Return quantity is required")
    @Min(value = 1, message = "Return quantity must be at least 1")
    @Column(name = "return_quantity")
    private Integer returnQuantity;

    @NotNull(message = "Original unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Original unit price must be greater than 0")
    @Column(name = "original_unit_price", precision = 10, scale = 2)
    private BigDecimal originalUnitPrice;

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Refund amount cannot be negative")
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "restocking_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal restockingFee = BigDecimal.ZERO;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_condition")
    private ItemCondition itemCondition;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "is_restockable")
    @Builder.Default
    private Boolean isRestockable = true;

    @Column(name = "disposal_reason")
    private String disposalReason;

    // Enums
    public enum ItemCondition {
        NEW, LIKE_NEW, GOOD, FAIR, POOR, DAMAGED, DEFECTIVE
    }

    // Custom constructors
    public ReturnItem(Return returnEntity, SaleItem originalSaleItem, Integer returnQuantity) {
        this.returnEntity = returnEntity;
        this.originalSaleItem = originalSaleItem;
        this.product = originalSaleItem.getProduct();
        this.returnQuantity = returnQuantity;
        this.originalUnitPrice = originalSaleItem.getUnitPrice();
        this.restockingFee = BigDecimal.ZERO;
        this.isRestockable = true;
        calculateRefundAmount();
    }

    public ReturnItem(Return returnEntity, SaleItem originalSaleItem, Integer returnQuantity, 
                     BigDecimal restockingFee, ItemCondition itemCondition) {
        this.returnEntity = returnEntity;
        this.originalSaleItem = originalSaleItem;
        this.product = originalSaleItem.getProduct();
        this.returnQuantity = returnQuantity;
        this.originalUnitPrice = originalSaleItem.getUnitPrice();
        this.restockingFee = restockingFee != null ? restockingFee : BigDecimal.ZERO;
        this.itemCondition = itemCondition;
        this.isRestockable = determineRestockability(itemCondition);
        calculateRefundAmount();
    }

    // Business logic methods
    public void calculateRefundAmount() {
        if (originalUnitPrice != null && returnQuantity != null) {
            BigDecimal totalValue = originalUnitPrice.multiply(BigDecimal.valueOf(returnQuantity));
            this.refundAmount = totalValue.subtract(restockingFee != null ? restockingFee : BigDecimal.ZERO);
            
            // Ensure refund amount is not negative
            if (this.refundAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.refundAmount = BigDecimal.ZERO;
            }
        }
    }

    public boolean isValidReturnQuantity() {
        if (originalSaleItem == null || returnQuantity == null) {
            return false;
        }
        
        // Check if return quantity doesn't exceed original sale quantity
        Integer originalQuantity = originalSaleItem.getQuantity();
        Integer alreadyReturned = originalSaleItem.getReturnedQuantity() != null ? 
                                 originalSaleItem.getReturnedQuantity() : 0;
        
        return returnQuantity <= (originalQuantity - alreadyReturned);
    }

    public void setItemCondition(ItemCondition condition) {
        this.itemCondition = condition;
        this.isRestockable = determineRestockability(condition);
        
        // If not restockable, might need disposal reason
        if (!this.isRestockable && this.disposalReason == null) {
            this.disposalReason = "Item condition: " + condition.name();
        }
    }

    public void applyRestockingFee(BigDecimal fee) {
        this.restockingFee = fee != null ? fee : BigDecimal.ZERO;
        calculateRefundAmount();
    }

    public BigDecimal getNetRefundAmount() {
        return this.refundAmount;
    }

    public BigDecimal getTotalOriginalValue() {
        return originalUnitPrice.multiply(BigDecimal.valueOf(returnQuantity));
    }

    public boolean canBeRestocked() {
        return this.isRestockable && 
               (this.itemCondition == ItemCondition.NEW || 
                this.itemCondition == ItemCondition.LIKE_NEW || 
                this.itemCondition == ItemCondition.GOOD);
    }

    private boolean determineRestockability(ItemCondition condition) {
        if (condition == null) {
            return true; // Default to restockable
        }
        
        switch (condition) {
            case NEW:
            case LIKE_NEW:
            case GOOD:
                return true;
            case FAIR:
                return true; // Might be restockable with discount
            case POOR:
            case DAMAGED:
            case DEFECTIVE:
                return false;
            default:
                return false;
        }
    }

    public String getConditionDisplay() {
        return this.itemCondition != null ? this.itemCondition.name().replace("_", " ") : "NOT SPECIFIED";
    }

    public void markAsProcessed() {
        // Update original sale item's returned quantity
        if (originalSaleItem != null) {
            Integer currentReturned = originalSaleItem.getReturnedQuantity() != null ? 
                                    originalSaleItem.getReturnedQuantity() : 0;
            originalSaleItem.setReturnedQuantity(currentReturned + this.returnQuantity);
            
            // Check if fully returned
            if (originalSaleItem.getReturnedQuantity().equals(originalSaleItem.getQuantity())) {
                originalSaleItem.setIsReturned(true);
            }
        }
    }
}
