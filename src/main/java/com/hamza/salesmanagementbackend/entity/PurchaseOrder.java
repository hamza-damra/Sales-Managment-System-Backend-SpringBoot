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

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

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
        PENDING, APPROVED, ORDERED, PARTIALLY_RECEIVED, RECEIVED, CANCELLED
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
            
            this.totalAmount = this.subtotal.add(this.taxAmount).subtract(this.discountAmount);
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

    public void markAsOrdered() {
        if (this.status == PurchaseOrderStatus.APPROVED) {
            this.status = PurchaseOrderStatus.ORDERED;
        }
    }

    public void markAsReceived() {
        if (this.status == PurchaseOrderStatus.ORDERED || 
            this.status == PurchaseOrderStatus.PARTIALLY_RECEIVED) {
            this.status = PurchaseOrderStatus.RECEIVED;
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
               (this.status == PurchaseOrderStatus.ORDERED || 
                this.status == PurchaseOrderStatus.PARTIALLY_RECEIVED);
    }
}
