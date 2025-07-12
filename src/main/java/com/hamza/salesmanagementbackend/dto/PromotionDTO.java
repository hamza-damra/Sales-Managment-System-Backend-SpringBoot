package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Promotion;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDTO {

    private Long id;

    @NotBlank(message = "Promotion name is required")
    private String name;

    private String description;

    @NotNull(message = "Promotion type is required")
    private Promotion.PromotionType type;

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

    private List<Long> applicableProducts;

    private List<String> applicableCategories;

    private Integer usageLimit;

    private Integer usageCount;

    private Promotion.CustomerEligibility customerEligibility;

    private String couponCode;

    private Boolean autoApply;

    private Boolean stackable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Computed fields for frontend
    private String statusDisplay;
    private String typeDisplay;
    private String eligibilityDisplay;
    private Boolean isCurrentlyActive;
    private Boolean isExpired;
    private Boolean isNotYetStarted;
    private Boolean isUsageLimitReached;
    private Long daysUntilExpiry;
    private Integer remainingUsage;
    private Double usagePercentage;
}
