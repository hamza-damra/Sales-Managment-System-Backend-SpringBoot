package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Promotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppliedPromotionDTO {

    private Long id;
    private Long saleId;
    private Long promotionId;
    private String promotionName;
    private Promotion.PromotionType promotionType;
    private String couponCode;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private Boolean isAutoApplied;
    private LocalDateTime appliedAt;

    // Computed fields for display
    private String displayText;
    private String typeDisplay;
    private BigDecimal savingsAmount;
    private Boolean isPercentageDiscount;
    private Boolean isFixedAmountDiscount;

    // Utility methods
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

    public String getTypeDisplay() {
        switch (promotionType) {
            case PERCENTAGE:
                return "Percentage Discount";
            case FIXED_AMOUNT:
                return "Fixed Amount Discount";
            case BUY_X_GET_Y:
                return "Buy X Get Y";
            case FREE_SHIPPING:
                return "Free Shipping";
            default:
                return "Unknown";
        }
    }
}
