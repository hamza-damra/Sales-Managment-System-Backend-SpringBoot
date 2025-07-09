package com.hamza.salesmanagementbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer Report DTO with comprehensive customer analytics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReportDTO {
    
    private CustomerSummary summary;
    private List<CustomerSegment> segments;
    private List<CustomerLifetimeValue> topCustomers;
    private CustomerRetentionMetrics retention;
    private CustomerAcquisitionMetrics acquisition;
    private List<CustomerBehaviorInsight> behaviorInsights;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSummary {
        private Long totalCustomers;
        private Long activeCustomers;
        private Long newCustomersThisPeriod;
        private BigDecimal averageLifetimeValue;
        private Double retentionRate;
        private Double churnRate;
        private BigDecimal averageOrderValue;
        private Integer averageOrdersPerCustomer;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerSegment {
        private String segmentName;
        private Long customerCount;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private Double retentionRate;
        private String characteristics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerLifetimeValue {
        private Long customerId;
        private String customerName;
        private String email;
        private BigDecimal lifetimeValue;
        private Integer totalOrders;
        private LocalDateTime firstPurchase;
        private LocalDateTime lastPurchase;
        private String segment;
        private BigDecimal predictedValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerRetentionMetrics {
        private Double overallRetentionRate;
        private Double monthlyRetentionRate;
        private Double yearlyRetentionRate;
        private List<CohortData> cohortAnalysis;
        private Double repeatPurchaseRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerAcquisitionMetrics {
        private Long newCustomersThisMonth;
        private Long newCustomersLastMonth;
        private Double acquisitionGrowthRate;
        private BigDecimal acquisitionCost;
        private String topAcquisitionChannel;
        private Double conversionRate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerBehaviorInsight {
        private String insight;
        private String category;
        private String impact;
        private String recommendation;
        private BigDecimal potentialValue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CohortData {
        private String cohortPeriod;
        private Long initialCustomers;
        private Long retainedCustomers;
        private Double retentionRate;
        private BigDecimal revenuePerCustomer;
    }
}
