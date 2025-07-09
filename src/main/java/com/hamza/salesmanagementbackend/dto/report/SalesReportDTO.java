package com.hamza.salesmanagementbackend.dto.report;

import com.hamza.salesmanagementbackend.entity.SaleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Sales Report DTO with detailed analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDTO {
    
    private SalesSummary summary;
    private List<DailySalesData> dailyBreakdown;
    private List<TopCustomer> topCustomers;
    private List<TopProduct> topProducts;
    private Map<SaleStatus, Long> salesByStatus;
    private List<SalesTrend> trends;
    private PaymentMethodAnalysis paymentAnalysis;
    private RegionalAnalysis regionalAnalysis;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesSummary {
        private Long totalSales;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private BigDecimal totalDiscounts;
        private BigDecimal totalTax;
        private BigDecimal netRevenue;
        private Double conversionRate;
        private Integer uniqueCustomers;
        private BigDecimal revenueGrowth;
        private Double salesGrowth;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySalesData {
        private String date;
        private Long salesCount;
        private BigDecimal revenue;
        private BigDecimal averageOrderValue;
        private Integer uniqueCustomers;
        private BigDecimal discountAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopCustomer {
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private Long totalOrders;
        private BigDecimal totalSpent;
        private BigDecimal averageOrderValue;
        private LocalDateTime lastPurchase;
        private String customerSegment;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProduct {
        private Long productId;
        private String productName;
        private String category;
        private Integer quantitySold;
        private BigDecimal revenue;
        private BigDecimal averagePrice;
        private BigDecimal profitMargin;
        private Integer uniqueCustomers;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesTrend {
        private String period;
        private BigDecimal revenue;
        private Long salesCount;
        private BigDecimal growthRate;
        private String trendDirection;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentMethodAnalysis {
        private Map<String, Long> countByMethod;
        private Map<String, BigDecimal> revenueByMethod;
        private String mostPopularMethod;
        private String highestRevenueMethod;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionalAnalysis {
        private Map<String, BigDecimal> revenueByRegion;
        private Map<String, Long> salesByRegion;
        private String topPerformingRegion;
        private BigDecimal regionalGrowth;
    }
}
