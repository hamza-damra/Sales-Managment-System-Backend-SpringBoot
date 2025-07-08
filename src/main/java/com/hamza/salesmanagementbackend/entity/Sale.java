package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Sale {

    // Static counter for ensuring unique sale numbers
    private static final AtomicLong saleCounter = new AtomicLong(0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    @Column(name = "sale_date")
    @Builder.Default
    private LocalDateTime saleDate = LocalDateTime.now();

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SaleStatus status = SaleStatus.PENDING;

    // New comprehensive attributes for better sales management
    @Column(name = "sale_number", unique = true)
    private String saleNumber;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "subtotal", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "sales_person")
    private String salesPerson;

    @Column(name = "sales_channel")
    private String salesChannel; // Online, In-store, Phone, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type")
    @Builder.Default
    private SaleType saleType = SaleType.RETAIL;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";

    @Column(name = "exchange_rate", precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "internal_notes", columnDefinition = "TEXT")
    private String internalNotes;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "warranty_info", columnDefinition = "TEXT")
    private String warrantyInfo;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.NOT_SHIPPED;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "is_gift")
    @Builder.Default
    private Boolean isGift = false;

    @Column(name = "gift_message", columnDefinition = "TEXT")
    private String giftMessage;

    @Column(name = "loyalty_points_earned")
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Column(name = "loyalty_points_used")
    @Builder.Default
    private Integer loyaltyPointsUsed = 0;

    @Column(name = "is_return")
    @Builder.Default
    private Boolean isReturn = false;

    @Column(name = "original_sale_id")
    private Long originalSaleId;

    @Column(name = "return_reason")
    private String returnReason;

    @Column(name = "profit_margin", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal profitMargin = BigDecimal.ZERO;

    @Column(name = "cost_of_goods_sold", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costOfGoodsSold = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<SaleItem> items;

    @OneToMany(mappedBy = "originalSale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Return> returns;

    // Enums
    public enum PaymentMethod {
        CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CHECK, PAYPAL, STRIPE, SQUARE, OTHER, NET_30
    }

    public enum PaymentStatus {
        PENDING, PAID, PARTIALLY_PAID, OVERDUE, REFUNDED, CANCELLED
    }

    public enum SaleType {
        RETAIL, WHOLESALE, B2B, ONLINE, SUBSCRIPTION, RETURN
    }

    public enum DeliveryStatus {
        NOT_SHIPPED, PROCESSING, SHIPPED, IN_TRANSIT, DELIVERED, RETURNED, CANCELLED, PICKED_UP
    }

    // Custom constructors for specific use cases
    public Sale(Customer customer) {
        this.customer = customer;
        this.saleDate = LocalDateTime.now();
        this.saleNumber = generateSaleNumber();
        this.status = SaleStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.discountPercentage = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.taxPercentage = BigDecimal.ZERO;
        this.shippingCost = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.PENDING;
        this.saleType = SaleType.RETAIL;
        this.currency = "USD";
        this.exchangeRate = BigDecimal.ONE;
        this.deliveryStatus = DeliveryStatus.NOT_SHIPPED;
        this.isGift = false;
        this.loyaltyPointsEarned = 0;
        this.loyaltyPointsUsed = 0;
        this.isReturn = false;
        this.profitMargin = BigDecimal.ZERO;
        this.costOfGoodsSold = BigDecimal.ZERO;
    }

    public Sale(Customer customer, PaymentMethod paymentMethod, SaleType saleType) {
        this(customer);
        this.paymentMethod = paymentMethod;
        this.saleType = saleType;
    }

    // Business logic methods
    private String generateSaleNumber() {
        // Combine timestamp with counter and random component for uniqueness
        long timestamp = System.currentTimeMillis();
        long counter = saleCounter.incrementAndGet();
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("SALE-%d-%d-%s", timestamp, counter, randomPart);
    }

    public void calculateTotals() {
        if (items != null && !items.isEmpty()) {
            this.subtotal = items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            this.totalAmount = subtotal
                    .subtract(discountAmount)
                    .add(taxAmount)
                    .add(shippingCost);
        }
    }

    public void processLoyaltyPoints() {
        if (customer != null && totalAmount != null) {
            // Calculate loyalty points earned (1 point per $10 spent)
            int pointsEarned = totalAmount.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).intValue();
            this.loyaltyPointsEarned = pointsEarned;

            // Add points to customer
            if (pointsEarned > 0) {
                customer.addLoyaltyPoints(pointsEarned);
            }
        }
    }

    public void markAsPaid() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentDate = LocalDateTime.now();
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) &&
               paymentStatus != PaymentStatus.PAID;
    }

    public BigDecimal getOutstandingAmount() {
        if (paymentStatus == PaymentStatus.PAID) {
            return BigDecimal.ZERO;
        }
        return totalAmount;
    }
}
