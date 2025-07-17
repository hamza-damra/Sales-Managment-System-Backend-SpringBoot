package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Product;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    // Category information
    private Long categoryId;
    private String categoryName;
    private String sku;

    // Enhanced attributes matching the entity
    private BigDecimal costPrice;
    private String brand;
    private String modelNumber;
    private String barcode;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Product.ProductStatus productStatus;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String supplierName;
    private String supplierCode;
    private Integer warrantyPeriod;
    private LocalDate expiryDate;
    private LocalDate manufacturingDate;
    private Set<String> tags;
    private String imageUrl;
    private Set<String> additionalImages;
    private Boolean isSerialized;
    private Boolean isDigital;
    private Boolean isTaxable;
    private BigDecimal taxRate;
    private String unitOfMeasure;
    private BigDecimal discountPercentage;
    private String locationInWarehouse;
    private Integer totalSold;
    private BigDecimal totalRevenue;
    private LocalDateTime lastSoldDate;
    private LocalDateTime lastRestockedDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional inventory fields for enhanced responses
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private Boolean needsReorder;
    private String stockStatus;
    private Integer daysUntilReorder;
    private BigDecimal profitMargin;
    private BigDecimal effectivePrice;
    private BigDecimal stockValue;

    // Utility methods
    public BigDecimal getProfitMargin() {
        if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(costPrice).divide(costPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getDiscountedPrice() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.multiply(discountPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return price.subtract(discount);
        }
        return price;
    }

    public boolean isLowStock() {
        return reorderPoint != null && stockQuantity <= reorderPoint;
    }

    public boolean isOutOfStock() {
        return stockQuantity == 0;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public BigDecimal calculateVolume() {
        if (length != null && width != null && height != null) {
            return length.multiply(width).multiply(height);
        }
        return BigDecimal.ZERO;
    }
}
