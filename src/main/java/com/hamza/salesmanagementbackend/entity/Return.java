package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_number", unique = true)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_sale_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale originalSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    @Column(name = "return_date")
    @Builder.Default
    private LocalDateTime returnDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Return reason is required")
    private ReturnReason reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReturnStatus status = ReturnStatus.PENDING;

    @NotNull(message = "Total refund amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total refund amount cannot be negative")
    @Column(name = "total_refund_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "processed_by")
    private String processedBy;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method")
    private RefundMethod refundMethod;

    @Column(name = "refund_reference")
    private String refundReference;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "returnEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<ReturnItem> items = new ArrayList<>();

    // Enums
    public enum ReturnReason {
        DEFECTIVE, WRONG_ITEM, CUSTOMER_CHANGE_MIND, DAMAGED_IN_SHIPPING, 
        NOT_AS_DESCRIBED, EXPIRED, DUPLICATE_ORDER, OTHER
    }

    public enum ReturnStatus {
        PENDING, APPROVED, REJECTED, REFUNDED, EXCHANGED, CANCELLED
    }

    public enum RefundMethod {
        ORIGINAL_PAYMENT, STORE_CREDIT, CASH, BANK_TRANSFER, CHECK
    }

    // Custom constructors
    public Return(Sale originalSale, Customer customer, ReturnReason reason, BigDecimal totalRefundAmount) {
        this.originalSale = originalSale;
        this.customer = customer;
        this.reason = reason;
        this.totalRefundAmount = totalRefundAmount;
        this.status = ReturnStatus.PENDING;
        this.returnDate = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    // Business logic methods
    public void approve(String processedBy) {
        if (this.status == ReturnStatus.PENDING) {
            this.status = ReturnStatus.APPROVED;
            this.processedBy = processedBy;
            this.processedDate = LocalDateTime.now();
        }
    }

    public void reject(String processedBy, String rejectionReason) {
        if (this.status == ReturnStatus.PENDING) {
            this.status = ReturnStatus.REJECTED;
            this.processedBy = processedBy;
            this.processedDate = LocalDateTime.now();
            this.notes = (this.notes != null ? this.notes + "\n" : "") + "Rejection reason: " + rejectionReason;
        }
    }

    public void processRefund(RefundMethod refundMethod, String refundReference) {
        if (this.status == ReturnStatus.APPROVED) {
            this.status = ReturnStatus.REFUNDED;
            this.refundMethod = refundMethod;
            this.refundReference = refundReference;
            this.refundDate = LocalDateTime.now();
        }
    }

    public void processExchange() {
        if (this.status == ReturnStatus.APPROVED) {
            this.status = ReturnStatus.EXCHANGED;
            this.processedDate = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (this.status == ReturnStatus.PENDING) {
            this.status = ReturnStatus.CANCELLED;
            this.processedDate = LocalDateTime.now();
        }
    }

    public boolean canBeModified() {
        return this.status == ReturnStatus.PENDING;
    }

    public boolean canBeProcessed() {
        return this.status == ReturnStatus.PENDING;
    }

    public boolean isWithinReturnPeriod(int returnPolicyDays) {
        if (this.originalSale == null || this.originalSale.getSaleDate() == null) {
            return false;
        }
        LocalDateTime cutoffDate = this.originalSale.getSaleDate().plusDays(returnPolicyDays);
        return this.returnDate.isBefore(cutoffDate) || this.returnDate.isEqual(cutoffDate);
    }

    public void calculateTotalRefundAmount() {
        if (items != null && !items.isEmpty()) {
            this.totalRefundAmount = items.stream()
                    .map(ReturnItem::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public String getStatusDisplay() {
        return this.status != null ? this.status.name().replace("_", " ") : "UNKNOWN";
    }

    public String getReasonDisplay() {
        return this.reason != null ? this.reason.name().replace("_", " ") : "UNKNOWN";
    }
}
