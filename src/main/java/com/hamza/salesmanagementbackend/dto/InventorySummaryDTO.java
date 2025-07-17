package com.hamza.salesmanagementbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * DTO for inventory summary statistics
 * Contains aggregated metrics about the current inventory state
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySummaryDTO {

    /**
     * Count of all products that have quantity > 0
     */
    private Long totalProductsInStock;

    /**
     * Count of products where stockQuantity <= reorderPoint (low stock alerts)
     */
    private Long lowStockAlerts;

    /**
     * Count of products where stockQuantity = 0
     */
    private Long outOfStockProducts;

    /**
     * Sum of (price × stockQuantity) for all products with stock > 0
     */
    private BigDecimal totalStockValue;

    /**
     * Timestamp when this summary was calculated
     */
    private LocalDateTime lastUpdated;

    /**
     * Additional metrics for enhanced reporting
     */
    
    /**
     * Total number of unique products in the system
     */
    private Long totalProducts;

    /**
     * Count of products that need reordering (at or below reorder point)
     */
    private Long productsNeedingReorder;

    /**
     * Average stock value per product (for products with stock > 0)
     */
    private BigDecimal averageStockValuePerProduct;

    /**
     * Percentage of products that are out of stock
     */
    private Double outOfStockPercentage;

    /**
     * Percentage of products that are low on stock
     */
    private Double lowStockPercentage;

    /**
     * Constructor for basic inventory summary
     */
    public InventorySummaryDTO(Long totalProductsInStock, Long lowStockAlerts, 
                              Long outOfStockProducts, BigDecimal totalStockValue) {
        this.totalProductsInStock = totalProductsInStock;
        this.lowStockAlerts = lowStockAlerts;
        this.outOfStockProducts = outOfStockProducts;
        this.totalStockValue = totalStockValue != null ? totalStockValue : BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Calculate derived metrics after setting basic values
     */
    public void calculateDerivedMetrics() {
        if (totalProducts != null && totalProducts > 0) {
            // Calculate percentages
            if (outOfStockProducts != null) {
                this.outOfStockPercentage = (outOfStockProducts.doubleValue() / totalProducts.doubleValue()) * 100.0;
            }
            
            if (lowStockAlerts != null) {
                this.lowStockPercentage = (lowStockAlerts.doubleValue() / totalProducts.doubleValue()) * 100.0;
            }
        }

        // Calculate average stock value per product
        if (totalProductsInStock != null && totalProductsInStock > 0 && totalStockValue != null) {
            this.averageStockValuePerProduct = totalStockValue.divide(
                BigDecimal.valueOf(totalProductsInStock), 2, RoundingMode.HALF_UP);
        }

        // Set last updated timestamp
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Check if inventory is in good health (low percentage of out-of-stock and low-stock items)
     */
    public boolean isInventoryHealthy() {
        return (outOfStockPercentage != null && outOfStockPercentage < 5.0) &&
               (lowStockPercentage != null && lowStockPercentage < 15.0);
    }

    /**
     * Get inventory health status as string
     */
    public String getInventoryHealthStatus() {
        if (isInventoryHealthy()) {
            return "HEALTHY";
        } else if (outOfStockPercentage != null && outOfStockPercentage > 10.0) {
            return "CRITICAL";
        } else {
            return "WARNING";
        }
    }
}
