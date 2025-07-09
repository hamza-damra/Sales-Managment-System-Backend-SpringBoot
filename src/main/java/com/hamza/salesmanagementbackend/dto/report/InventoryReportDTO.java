package com.hamza.salesmanagementbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Inventory Report DTO with comprehensive inventory analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReportDTO {
    
    private InventorySummary summary;
    private List<StockAlert> stockAlerts;
    private List<ProductTurnover> turnoverAnalysis;
    private InventoryValuation valuation;
    private List<CategoryAnalysis> categoryBreakdown;
    private List<WarehouseAnalysis> warehouseDistribution;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventorySummary {
        private Long totalProducts;
        private BigDecimal totalInventoryValue;
        private Long outOfStockItems;
        private Long lowStockItems;
        private Long overstockItems;
        private BigDecimal averageTurnoverRate;
        private Integer daysOfInventory;
        private BigDecimal deadStockValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockAlert {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private Integer reorderLevel;
        private String alertType; // LOW_STOCK, OUT_OF_STOCK, OVERSTOCK
        private String priority; // HIGH, MEDIUM, LOW
        private Integer daysUntilStockout;
        private BigDecimal potentialLostSales;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductTurnover {
        private Long productId;
        private String productName;
        private String category;
        private BigDecimal turnoverRate;
        private Integer daysSinceLastSale;
        private Integer quantitySold;
        private BigDecimal revenue;
        private String movementClassification; // FAST, MEDIUM, SLOW, DEAD
        private String recommendation;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryValuation {
        private BigDecimal totalCostValue;
        private BigDecimal totalRetailValue;
        private BigDecimal totalMarketValue;
        private String valuationMethod; // FIFO, LIFO, AVERAGE
        private Map<String, BigDecimal> categoryValuation;
        private BigDecimal unrealizedGainLoss;
        private LocalDateTime valuationDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAnalysis {
        private String categoryName;
        private Long productCount;
        private BigDecimal totalValue;
        private BigDecimal averageTurnover;
        private Integer lowStockCount;
        private Integer outOfStockCount;
        private BigDecimal categoryRevenue;
        private Double profitMargin;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseAnalysis {
        private Long warehouseId;
        private String warehouseName;
        private String location;
        private Long productCount;
        private BigDecimal totalValue;
        private Double utilizationRate;
        private Integer capacity;
        private Integer currentStock;
        private List<StockAlert> alerts;
    }
}
