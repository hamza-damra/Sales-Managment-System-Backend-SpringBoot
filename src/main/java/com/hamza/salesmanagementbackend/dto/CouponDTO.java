package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Coupon;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CouponDTO {

    private Long id;

    @NotBlank(message = "Coupon code is required")
    private String couponCode;

    private String description;

    @NotNull(message = "Coupon type is required")
    private Coupon.CouponType type;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;

    private BigDecimal maximumDiscountAmount;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean isActive;

    private Integer usageLimit;

    private Integer usageCount;

    private Integer usageLimitPerCustomer;

    private Coupon.CustomerEligibility customerEligibility;

    private Boolean isSingleUse;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Computed fields for frontend
    private String statusDisplay;
    private String typeDisplay;
    private String eligibilityDisplay;
    private Boolean isValid;
    private Boolean isExpired;
    private Boolean isNotYetStarted;
    private Boolean isUsageLimitReached;
    private Long daysUntilExpiry;
    private Integer remainingUsage;
    private Double usagePercentage;
}
