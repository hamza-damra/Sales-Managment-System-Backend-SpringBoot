package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString // Remove exclude parameter
@EqualsAndHashCode // Remove exclude parameter
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // Enhanced attributes for better sales item management
    @Column(name = "original_unit_price", precision = 10, scale = 2)
    private BigDecimal originalUnitPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers; // For serialized products

    @Column(name = "warranty_info")
    private String warrantyInfo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_returned")
    @Builder.Default
    private Boolean isReturned = false;

    @Column(name = "returned_quantity")
    @Builder.Default
    private Integer returnedQuantity = 0;

    @Column(name = "unit_of_measure")
    @Builder.Default
    private String unitOfMeasure = "PCS";

    // Custom constructors for specific use cases
    public SaleItem(Sale sale, Product product, Integer quantity, BigDecimal unitPrice) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.originalUnitPrice = unitPrice;
        this.costPrice = product.getCostPrice();
        this.unitOfMeasure = product.getUnitOfMeasure();
        this.discountPercentage = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxPercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.isReturned = false;
        this.returnedQuantity = 0;
        calculateTotals();
    }

    public SaleItem(Sale sale, Product product, Integer quantity, BigDecimal unitPrice,
                   BigDecimal discountPercentage, BigDecimal taxPercentage) {
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.originalUnitPrice = unitPrice;
        this.discountPercentage = discountPercentage != null ? discountPercentage : BigDecimal.ZERO;
        this.taxPercentage = taxPercentage != null ? taxPercentage : BigDecimal.ZERO;
        this.costPrice = product.getCostPrice();
        this.unitOfMeasure = product.getUnitOfMeasure();
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.isReturned = false;
        this.returnedQuantity = 0;
        calculateTotals();
    }

    // Business logic methods
    public void calculateTotals() {
        // Calculate subtotal
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        // Apply discount
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercentage)
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // Calculate tax
        if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(taxPercentage)
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        }

        // Calculate total
        this.totalPrice = afterDiscount.add(taxAmount);
    }

    public BigDecimal getLineTotal() {
        return totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }

    public BigDecimal getProfit() {
        if (costPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCost = costPrice.multiply(BigDecimal.valueOf(quantity));
        return getLineTotal().subtract(totalCost);
    }

    public BigDecimal getProfitMargin() {
        if (costPrice == null || costPrice.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        return getProfit().divide(getLineTotal(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public boolean canReturn(int requestedQuantity) {
        return !isReturned && (returnedQuantity + requestedQuantity) <= quantity;
    }

    public void processReturn(int returnQuantity) {
        if (canReturn(returnQuantity)) {
            this.returnedQuantity += returnQuantity;
            if (this.returnedQuantity.equals(this.quantity)) {
                this.isReturned = true;
            }
        }
    }

    public int getRemainingQuantity() {
        return (quantity != null ? quantity : 0) - (returnedQuantity != null ? returnedQuantity : 0);
    }

    public int getAvailableQuantityForReturn() {
        return getRemainingQuantity();
    }
}
