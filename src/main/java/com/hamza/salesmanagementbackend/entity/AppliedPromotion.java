package com.hamza.salesmanagementbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applied_promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class AppliedPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Promotion promotion;

    @NotNull(message = "Promotion name is required")
    @Column(name = "promotion_name", nullable = false)
    private String promotionName;

    @NotNull(message = "Promotion type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false)
    private Promotion.PromotionType promotionType;

    @Column(name = "coupon_code")
    private String couponCode;

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount cannot be negative")
    @Column(name = "discount_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "is_auto_applied")
    @Builder.Default
    private Boolean isAutoApplied = false;

    @CreationTimestamp
    @Column(name = "applied_at", updatable = false)
    private LocalDateTime appliedAt;

    // Custom constructors
    public AppliedPromotion(Sale sale, Promotion promotion, BigDecimal discountAmount, 
                           BigDecimal originalAmount, Boolean isAutoApplied) {
        this.sale = sale;
        this.promotion = promotion;
        this.promotionName = promotion.getName();
        this.promotionType = promotion.getType();
        this.couponCode = promotion.getCouponCode();
        this.discountAmount = discountAmount;
        this.originalAmount = originalAmount;
        this.finalAmount = originalAmount.subtract(discountAmount);
        this.isAutoApplied = isAutoApplied != null ? isAutoApplied : false;
        
        // Calculate discount percentage if applicable
        if (promotion.getType() == Promotion.PromotionType.PERCENTAGE) {
            this.discountPercentage = promotion.getDiscountValue();
        } else if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.discountPercentage = discountAmount.multiply(BigDecimal.valueOf(100))
                    .divide(originalAmount, 2, java.math.RoundingMode.HALF_UP);
        }
    }

    // Business logic methods
    public BigDecimal getSavingsAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }

    public boolean isPercentageDiscount() {
        return promotionType == Promotion.PromotionType.PERCENTAGE;
    }

    public boolean isFixedAmountDiscount() {
        return promotionType == Promotion.PromotionType.FIXED_AMOUNT;
    }

    public String getDisplayText() {
        if (isPercentageDiscount() && discountPercentage != null) {
            return String.format("%s (%.1f%% off)", promotionName, discountPercentage);
        } else {
            return String.format("%s ($%.2f off)", promotionName, discountAmount);
        }
    }
}
