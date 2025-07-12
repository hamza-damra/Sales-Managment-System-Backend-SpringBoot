package com.hamza.salesmanagementbackend.entity;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Column(name = "coupon_code", nullable = false, unique = true)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Coupon type is required")
    private CouponType type;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "usage_limit_per_customer")
    private Integer usageLimitPerCustomer;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_eligibility")
    @Builder.Default
    private CustomerEligibility customerEligibility = CustomerEligibility.ALL;

    @Column(name = "is_single_use")
    @Builder.Default
    private Boolean isSingleUse = false;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum CouponType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING, BUY_ONE_GET_ONE
    }

    public enum CustomerEligibility {
        ALL, VIP_ONLY, NEW_CUSTOMERS, RETURNING_CUSTOMERS, PREMIUM_ONLY
    }

    // Custom constructors
    public Coupon(String couponCode, CouponType type, BigDecimal discountValue, 
                 LocalDateTime startDate, LocalDateTime endDate) {
        this.couponCode = couponCode;
        this.type = type;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = true;
        this.usageCount = 0;
        this.customerEligibility = CustomerEligibility.ALL;
        this.isSingleUse = false;
        this.minimumOrderAmount = BigDecimal.ZERO;
    }

    // Business logic methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return this.isActive && 
               now.isAfter(this.startDate) && 
               now.isBefore(this.endDate) &&
               !isUsageLimitReached();
    }

    public boolean isUsageLimitReached() {
        return this.usageLimit != null && 
               this.usageCount != null && 
               this.usageCount >= this.usageLimit;
    }

    public boolean canBeUsedByCustomer(Customer customer, int customerUsageCount) {
        if (!isApplicableToCustomer(customer)) {
            return false;
        }

        if (this.usageLimitPerCustomer != null && 
            customerUsageCount >= this.usageLimitPerCustomer) {
            return false;
        }

        if (this.isSingleUse && customerUsageCount > 0) {
            return false;
        }

        return true;
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount != null ? this.usageCount : 0) + 1;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isApplicableToCustomer(Customer customer) {
        if (customer == null) {
            return this.customerEligibility == CustomerEligibility.ALL;
        }

        switch (this.customerEligibility) {
            case ALL:
                return true;
            case VIP_ONLY:
                return customer.getCustomerType() == Customer.CustomerType.VIP;
            case NEW_CUSTOMERS:
                return customer.getTotalPurchases().compareTo(BigDecimal.ZERO) == 0;
            case RETURNING_CUSTOMERS:
                return customer.getTotalPurchases().compareTo(BigDecimal.ZERO) > 0;
            case PREMIUM_ONLY:
                return customer.getCustomerType() == Customer.CustomerType.PREMIUM;
            default:
                return false;
        }
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount.compareTo(this.minimumOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (this.type) {
            case PERCENTAGE:
                discount = orderAmount.multiply(this.discountValue)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discount = this.discountValue;
                break;
            case FREE_SHIPPING:
                discount = BigDecimal.ZERO; // Handled separately
                break;
            case BUY_ONE_GET_ONE:
                discount = BigDecimal.ZERO; // Complex logic, handled separately
                break;
            default:
                discount = BigDecimal.ZERO;
                break;
        }

        // Apply maximum discount limit if set
        if (this.maximumDiscountAmount != null && 
            discount.compareTo(this.maximumDiscountAmount) > 0) {
            discount = this.maximumDiscountAmount;
        }

        return discount;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.endDate);
    }

    public boolean isNotYetStarted() {
        return LocalDateTime.now().isBefore(this.startDate);
    }

    public long getDaysUntilExpiry() {
        if (isExpired()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), this.endDate);
    }

    public String getStatusDisplay() {
        if (!this.isActive) {
            return "INACTIVE";
        }
        if (isNotYetStarted()) {
            return "SCHEDULED";
        }
        if (isExpired()) {
            return "EXPIRED";
        }
        if (isUsageLimitReached()) {
            return "USAGE_LIMIT_REACHED";
        }
        return "ACTIVE";
    }

    public String getTypeDisplay() {
        return this.type != null ? this.type.name().replace("_", " ") : "UNKNOWN";
    }

    public String getEligibilityDisplay() {
        return this.customerEligibility != null ? 
               this.customerEligibility.name().replace("_", " ") : "ALL";
    }

    public int getRemainingUsage() {
        if (this.usageLimit == null) {
            return Integer.MAX_VALUE; // Unlimited
        }
        return Math.max(0, this.usageLimit - (this.usageCount != null ? this.usageCount : 0));
    }

    public double getUsagePercentage() {
        if (this.usageLimit == null || this.usageLimit == 0) {
            return 0.0;
        }
        return ((double) (this.usageCount != null ? this.usageCount : 0) / this.usageLimit) * 100.0;
    }
}
