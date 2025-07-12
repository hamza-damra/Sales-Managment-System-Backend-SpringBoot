package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "purchase_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit cost must be greater than 0")
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "received_quantity")
    @Builder.Default
    private Integer receivedQuantity = 0;

    @Column(name = "pending_quantity")
    private Integer pendingQuantity;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Custom constructors
    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Product product, Integer quantity, BigDecimal unitCost) {
        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.receivedQuantity = 0;
        this.taxPercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        calculateTotals();
    }

    public PurchaseOrderItem(PurchaseOrder purchaseOrder, Product product, Integer quantity, 
                           BigDecimal unitCost, BigDecimal discountPercentage, BigDecimal taxPercentage) {
        this.purchaseOrder = purchaseOrder;
        this.product = product;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        this.taxPercentage = taxPercentage != null ? taxPercentage : BigDecimal.ZERO;
        this.receivedQuantity = 0;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        calculateTotals();
    }

    // Business logic methods
    public void calculateTotals() {
        // Calculate subtotal
        this.subtotal = unitCost.multiply(BigDecimal.valueOf(quantity));

        // Apply discount
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // Calculate tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(taxPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Calculate total
        this.totalPrice = afterDiscount.add(taxAmount);
        
        // Update pending quantity
        this.pendingQuantity = quantity - (receivedQuantity != null ? receivedQuantity : 0);
    }

    public void receiveQuantity(Integer receivedQty) {
        if (receivedQty != null && receivedQty > 0) {
            int currentReceived = this.receivedQuantity != null ? this.receivedQuantity : 0;
            int newReceived = Math.min(currentReceived + receivedQty, this.quantity);
            this.receivedQuantity = newReceived;
            this.pendingQuantity = this.quantity - this.receivedQuantity;
        }
    }

    public boolean isFullyReceived() {
        return this.receivedQuantity != null && this.receivedQuantity.equals(this.quantity);
    }

    public boolean isPartiallyReceived() {
        return this.receivedQuantity != null && this.receivedQuantity > 0 && 
               this.receivedQuantity < this.quantity;
    }

    public Integer getRemainingQuantity() {
        return this.quantity - (this.receivedQuantity != null ? this.receivedQuantity : 0);
    }

    public BigDecimal getReceivedValue() {
        if (this.receivedQuantity == null || this.receivedQuantity == 0) {
            return BigDecimal.ZERO;
        }
        return this.unitCost.multiply(BigDecimal.valueOf(this.receivedQuantity));
    }
}
