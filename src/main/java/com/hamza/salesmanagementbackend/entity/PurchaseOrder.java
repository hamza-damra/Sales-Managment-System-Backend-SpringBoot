package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Supplier supplier;

    @Column(name = "order_date")
    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    @Column(name = "total_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderPriority priority = OrderPriority.NORMAL;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_terms")
    private String deliveryTerms;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "tax_rate")
    @Builder.Default
    private Double taxRate = 15.0;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<PurchaseOrderItem> items;

    // Enums
    public enum PurchaseOrderStatus {
        PENDING, APPROVED, SENT, DELIVERED, CANCELLED
    }

    public enum OrderPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    // Custom constructors
    public PurchaseOrder(Supplier supplier, String orderNumber, BigDecimal totalAmount) {
        this.supplier = supplier;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.subtotal = totalAmount;
        this.status = PurchaseOrderStatus.PENDING;
        this.orderDate = LocalDateTime.now();
        this.taxAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }

    // Business logic methods
    public void calculateTotals() {
        if (items != null && !items.isEmpty()) {
            this.subtotal = items.stream()
                    .map(PurchaseOrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate tax amount if tax rate is provided
            if (this.taxRate != null && this.taxRate > 0) {
                this.taxAmount = this.subtotal.multiply(BigDecimal.valueOf(this.taxRate))
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }

            this.totalAmount = this.subtotal.add(this.taxAmount)
                    .add(this.shippingCost != null ? this.shippingCost : BigDecimal.ZERO)
                    .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
        }
    }

    public boolean canBeModified() {
        return this.status == PurchaseOrderStatus.PENDING;
    }

    public boolean canBeCancelled() {
        return this.status == PurchaseOrderStatus.PENDING || 
               this.status == PurchaseOrderStatus.APPROVED;
    }

    public void approve(String approvedBy) {
        if (this.status == PurchaseOrderStatus.PENDING) {
            this.status = PurchaseOrderStatus.APPROVED;
            this.approvedBy = approvedBy;
            this.approvedDate = LocalDateTime.now();
        }
    }

    public void markAsSent() {
        if (this.status == PurchaseOrderStatus.APPROVED) {
            this.status = PurchaseOrderStatus.SENT;
            this.sentDate = LocalDateTime.now();
        }
    }

    public void markAsDelivered() {
        if (this.status == PurchaseOrderStatus.SENT) {
            this.status = PurchaseOrderStatus.DELIVERED;
            this.actualDeliveryDate = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (canBeCancelled()) {
            this.status = PurchaseOrderStatus.CANCELLED;
        }
    }

    public boolean isOverdue() {
        return this.expectedDeliveryDate != null &&
               this.expectedDeliveryDate.isBefore(LocalDateTime.now()) &&
               (this.status == PurchaseOrderStatus.SENT);
    }

    public boolean isFullyReceived() {
        if (items == null || items.isEmpty()) {
            return false;
        }
        return items.stream().allMatch(PurchaseOrderItem::isFullyReceived);
    }

    public double getReceivingProgress() {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        int totalQuantity = items.stream().mapToInt(PurchaseOrderItem::getQuantity).sum();
        int receivedQuantity = items.stream()
                .mapToInt(item -> item.getReceivedQuantity() != null ? item.getReceivedQuantity() : 0)
                .sum();

        return totalQuantity > 0 ? (double) receivedQuantity / totalQuantity * 100.0 : 0.0;
    }

    public int getItemsCount() {
        return items != null ? items.size() : 0;
    }
}
