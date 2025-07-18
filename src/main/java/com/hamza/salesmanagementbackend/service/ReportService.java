package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final ReportHelperService reportHelperService;
    private final ReturnRepository returnRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Generates comprehensive sales report using streams for data aggregation
     */
    public Map<String, Object> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);

        // Filter completed sales for revenue calculations
        List<Sale> completedSales = sales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        return Map.of(
                "period", Map.of(
                        "startDate", startDate.format(DATE_FORMATTER),
                        "endDate", endDate.format(DATE_FORMATTER)
                ),
                "summary", generateSalesSummary(completedSales),
                "salesByStatus", generateSalesByStatus(sales),
                "dailyRevenue", generateDailyRevenue(completedSales),
                "topCustomers", generateTopCustomersReport(completedSales),
                "productPerformance", generateProductPerformanceReport(completedSales)
        );
    }

    /**
     * Generates customer analysis report using streams
     */
    public Map<String, Object> generateCustomerReport() {
        List<Sale> allSales = saleRepository.findAll()
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Object> customerStats = allSales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getCustomer().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                customerSales -> {
                                    Map<String, Object> customerMap = new HashMap<>();
                                    customerMap.put("totalSales", customerSales.size());
                                    customerMap.put("totalSpent", customerSales.stream()
                                            .map(Sale::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    customerMap.put("averageOrderValue", calculateAverageOrderValue(customerSales));
                                    customerMap.put("lastPurchase", customerSales.stream()
                                            .map(Sale::getSaleDate)
                                            .max(LocalDateTime::compareTo)
                                            .orElse(null));
                                    return customerMap;
                                }
                        )
                ));

        long totalCustomers = customerRepository.count();
        long activeCustomers = customerStats.size();

        Map<String, Object> customerReport = new HashMap<>();
        customerReport.put("totalCustomers", totalCustomers);
        customerReport.put("activeCustomers", activeCustomers);
        customerReport.put("customerRetentionRate", calculateRetentionRate(activeCustomers, totalCustomers));
        customerReport.put("customerDetails", customerStats);

        return customerReport;
    }

    /**
     * Generates inventory report using streams for analysis
     */
    public Map<String, Object> generateInventoryReport() {
        return productRepository.findAll()
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        products -> {
                            Map<String, Long> stockLevels = products.stream()
                                    .collect(Collectors.groupingBy(
                                            product -> {
                                                if (product.getStockQuantity() == 0) return "Out of Stock";
                                                if (product.getStockQuantity() < 10) return "Low Stock";
                                                if (product.getStockQuantity() < 50) return "Medium Stock";
                                                return "High Stock";
                                            },
                                            Collectors.counting()
                                    ));

                            BigDecimal totalInventoryValue = products.stream()
                                    .map(product -> product.getPrice()
                                            .multiply(BigDecimal.valueOf(product.getStockQuantity())))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            Map<String, Object> categoryAnalysis = products.stream()
                                    .filter(product -> product.getCategory() != null)
                                    .collect(Collectors.groupingBy(
                                            product -> product.getCategory().getName(),
                                            Collectors.collectingAndThen(
                                                    Collectors.toList(),
                                                    categoryProducts -> {
                                                        Map<String, Object> categoryMap = new HashMap<>();
                                                        categoryMap.put("productCount", categoryProducts.size());
                                                        categoryMap.put("totalValue", categoryProducts.stream()
                                                                .map(p -> p.getPrice().multiply(
                                                                        BigDecimal.valueOf(p.getStockQuantity())))
                                                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                                                        categoryMap.put("averagePrice", categoryProducts.stream()
                                                                .map(p -> p.getPrice())
                                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                                .divide(BigDecimal.valueOf(categoryProducts.size()),
                                                                        RoundingMode.HALF_UP));
                                                        return categoryMap;
                                                    }
                                            )
                                    ));

                            Map<String, Object> inventoryReport = new HashMap<>();
                            inventoryReport.put("totalProducts", products.size());
                            inventoryReport.put("totalInventoryValue", totalInventoryValue);
                            inventoryReport.put("stockLevels", stockLevels);
                            inventoryReport.put("categoryAnalysis", categoryAnalysis);
                            inventoryReport.put("lowStockProducts", products.stream()
                                    .filter(p -> p.getStockQuantity() < 10)
                                    .map(p -> {
                                        Map<String, Object> productMap = new HashMap<>();
                                        productMap.put("name", p.getName());
                                        productMap.put("currentStock", p.getStockQuantity());
                                        productMap.put("category", p.getCategory() != null ? p.getCategory().getName() : "Uncategorized");
                                        return productMap;
                                    })
                                    .collect(Collectors.toList()));
                            return inventoryReport;
                        }
                ));
    }

    /**
     * Generates revenue trends report using streams
     */
    public Map<String, Object> generateRevenueTrends(int months) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, BigDecimal> monthlyRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getYear() + "-" +
                               String.format("%02d", sale.getSaleDate().getMonthValue()),
                        Collectors.mapping(Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        Map<String, Long> monthlySalesCount = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getYear() + "-" +
                               String.format("%02d", sale.getSaleDate().getMonthValue()),
                        Collectors.counting()
                ));

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "period", months + " months",
                "totalRevenue", totalRevenue,
                "totalSales", sales.size(),
                "monthlyRevenue", monthlyRevenue,
                "monthlySalesCount", monthlySalesCount,
                "averageMonthlyRevenue", monthlyRevenue.isEmpty() ? BigDecimal.ZERO :
                        monthlyRevenue.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(monthlyRevenue.size()), RoundingMode.HALF_UP),
                "growthTrend", calculateGrowthTrend(monthlyRevenue)
        );
    }

    /**
     * Generates top performers report using streams
     */
    public Map<String, Object> generateTopPerformersReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        // Top customers by revenue
        Map<String, BigDecimal> topCustomersByRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getCustomer().getName(),
                        Collectors.mapping(Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        // Top products by quantity sold
        Map<String, Integer> topProductsByQuantity = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.summingInt(item -> item.getQuantity())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        // Top products by revenue (using accurate totalPrice calculation)
        Map<String, BigDecimal> topProductsByRevenue = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.mapping(
                                item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));

        return Map.of(
                "topCustomersByRevenue", topCustomersByRevenue,
                "topProductsByQuantity", topProductsByQuantity,
                "topProductsByRevenue", topProductsByRevenue
        );
    }

    // Private helper methods

    private Map<String, Object> generateSalesSummary(List<Sale> sales) {
        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = sales.isEmpty() ?
                BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(sales.size()), RoundingMode.HALF_UP);

        return Map.of(
                "totalSales", sales.size(),
                "totalRevenue", totalRevenue,
                "averageOrderValue", averageOrderValue
        );
    }

    private Map<SaleStatus, Long> generateSalesByStatus(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(Sale::getStatus, Collectors.counting()));
    }

    private Map<String, BigDecimal> generateDailyRevenue(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().toLocalDate().format(DATE_FORMATTER),
                        Collectors.mapping(Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    private Map<String, Object> generateTopCustomersReport(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getCustomer().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                customerSales -> {
                                    Map<String, Object> customerData = new java.util.HashMap<>();
                                    customerData.put("totalOrders", customerSales.size());
                                    customerData.put("totalSpent", customerSales.stream()
                                            .map(Sale::getTotalAmount)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    customerData.put("averageOrderValue", calculateAverageOrderValue(customerSales));
                                    return customerData;
                                }
                        )
                ))
                .entrySet().stream()
                .sorted((e1, e2) -> {
                    BigDecimal total1 = (BigDecimal) e1.getValue().get("totalSpent");
                    BigDecimal total2 = (BigDecimal) e2.getValue().get("totalSpent");
                    return total2.compareTo(total1);
                })
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }

    private Map<String, Object> generateProductPerformanceReport(List<Sale> sales) {
        log.debug("Generating product performance report for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "productMetrics", Map.of(),
                "summary", Map.of(),
                "message", "No sales data available for product performance analysis"
            );
        }

        // Extract all sale items from sales
        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "productMetrics", Map.of(),
                "summary", Map.of(),
                "message", "No sale items found in the provided sales data"
            );
        }

        // Group by product and calculate accurate metrics
        Map<String, Map<String, Object>> productMetrics = allItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    Map<String, Object> metrics = new HashMap<>();

                                    // Calculate total quantity sold
                                    int totalQuantity = items.stream()
                                            .mapToInt(SaleItem::getQuantity)
                                            .sum();

                                    // Calculate accurate revenue using totalPrice (includes discounts, taxes)
                                    BigDecimal totalRevenue = items.stream()
                                            .map(item -> item.getTotalPrice() != null ?
                                                item.getTotalPrice() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    // Calculate total cost and profit
                                    BigDecimal totalCost = items.stream()
                                            .map(item -> {
                                                BigDecimal cost = item.getCostPrice() != null ?
                                                    item.getCostPrice() : BigDecimal.ZERO;
                                                return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    BigDecimal totalProfit = totalRevenue.subtract(totalCost);

                                    // Calculate weighted average unit price (revenue / quantity)
                                    BigDecimal avgUnitPrice = totalQuantity > 0 ?
                                            totalRevenue.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP) :
                                            BigDecimal.ZERO;

                                    // Calculate profit margin
                                    BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                                            totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                                    .multiply(BigDecimal.valueOf(100)) :
                                            BigDecimal.ZERO;

                                    // Get product details
                                    Product product = items.get(0).getProduct();

                                    metrics.put("productId", product.getId());
                                    metrics.put("productName", product.getName());
                                    metrics.put("sku", product.getSku());
                                    metrics.put("category", product.getCategory() != null ?
                                        product.getCategory().getName() : "Uncategorized");
                                    metrics.put("quantitySold", totalQuantity);
                                    metrics.put("revenue", totalRevenue);
                                    metrics.put("totalCost", totalCost);
                                    metrics.put("profit", totalProfit);
                                    metrics.put("profitMargin", profitMargin);
                                    metrics.put("avgUnitPrice", avgUnitPrice);
                                    metrics.put("salesCount", items.size());
                                    metrics.put("currentStock", product.getStockQuantity());

                                    return metrics;
                                }
                        )
                ));

        // Calculate summary statistics
        BigDecimal totalRevenueAll = productMetrics.values().stream()
                .map(m -> (BigDecimal) m.get("revenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantityAll = productMetrics.values().stream()
                .mapToInt(m -> (Integer) m.get("quantitySold"))
                .sum();

        BigDecimal totalProfitAll = productMetrics.values().stream()
                .map(m -> (BigDecimal) m.get("profit"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenuePerProduct = productMetrics.isEmpty() ? BigDecimal.ZERO :
                totalRevenueAll.divide(BigDecimal.valueOf(productMetrics.size()), 2, RoundingMode.HALF_UP);

        BigDecimal overallProfitMargin = totalRevenueAll.compareTo(BigDecimal.ZERO) > 0 ?
                totalProfitAll.divide(totalRevenueAll, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        Map<String, Object> summary = Map.of(
                "totalProducts", productMetrics.size(),
                "totalRevenue", totalRevenueAll,
                "totalQuantitySold", totalQuantityAll,
                "totalProfit", totalProfitAll,
                "avgRevenuePerProduct", avgRevenuePerProduct,
                "overallProfitMargin", overallProfitMargin
        );

        return Map.of(
                "productMetrics", productMetrics,
                "summary", summary
        );
    }

    private BigDecimal calculateAverageOrderValue(List<Sale> sales) {
        if (sales.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(sales.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRetentionRate(long activeCustomers, long totalCustomers) {
        if (totalCustomers == 0) return BigDecimal.ZERO;

        return BigDecimal.valueOf(activeCustomers)
                .divide(BigDecimal.valueOf(totalCustomers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private String calculateGrowthTrend(Map<String, BigDecimal> monthlyRevenue) {
        if (monthlyRevenue.size() < 2) return "Insufficient data";

        List<BigDecimal> revenues = monthlyRevenue.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        BigDecimal firstMonth = revenues.get(0);
        BigDecimal lastMonth = revenues.get(revenues.size() - 1);

        if (firstMonth.equals(BigDecimal.ZERO)) return "No baseline data";

        BigDecimal growthRate = lastMonth.subtract(firstMonth)
                .divide(firstMonth, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (growthRate.compareTo(BigDecimal.ZERO) > 0) {
            return "Growing (" + growthRate + "%)";
        } else if (growthRate.compareTo(BigDecimal.ZERO) < 0) {
            return "Declining (" + growthRate + "%)";
        } else {
            return "Stable";
        }
    }

    // ==================== NEW ENHANCED REPORT METHODS ====================

    /**
     * Generate comprehensive sales report with detailed analytics
     */
    public SalesReportDTO generateComprehensiveSalesReport(ReportRequestDTO request) {
        log.info("Generating comprehensive sales report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        List<Sale> sales = saleRepository.findBySaleDateBetween(request.getStartDate(), request.getEndDate());
        List<Sale> completedSales = sales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        return SalesReportDTO.builder()
                .summary(reportHelperService.generateAdvancedSalesSummary(completedSales, request))
                .dailyBreakdown(reportHelperService.generateDailyBreakdown(completedSales))
                .topCustomers(reportHelperService.generateTopCustomersAnalysis(completedSales))
                .topProducts(reportHelperService.generateTopProductsAnalysis(completedSales))
                .salesByStatus(generateSalesByStatus(sales))
                .trends(generateSalesTrendsData(completedSales, request))
                .paymentAnalysis(reportHelperService.generatePaymentMethodAnalysis(completedSales))
                .regionalAnalysis(reportHelperService.generateRegionalAnalysis(completedSales))
                .build();
    }

    /**
     * Generate sales trends analysis with forecasting
     */
    public Map<String, Object> generateSalesTrendsAnalysis(int months, String groupBy) {
        log.info("Generating sales trends analysis for {} months grouped by {}", months, groupBy);

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Object> trends = new HashMap<>();

        switch (groupBy.toUpperCase()) {
            case "DAY":
                trends.putAll(generateDailyTrends(sales));
                break;
            case "WEEK":
                trends.putAll(generateWeeklyTrends(sales));
                break;
            case "MONTH":
                trends.putAll(generateMonthlyTrends(sales));
                break;
            default:
                trends.putAll(generateMonthlyTrends(sales));
                break;
        }

        // Add forecasting data
        trends.put("forecast", generateSalesForecast(sales, groupBy));
        trends.put("seasonality", analyzeSeasonality(sales));
        trends.put("growthMetrics", calculateGrowthMetrics(sales));

        return trends;
    }

    /**
     * Generate customer analytics report with segmentation
     */
    public Map<String, Object> generateCustomerAnalyticsReport(Boolean includeInactive, int months) {
        log.info("Generating customer analytics report for {} months, includeInactive: {}", months, includeInactive);

        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(months);
        List<Sale> recentSales = saleRepository.findBySaleDateBetween(cutoffDate, LocalDateTime.now())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("customerSegmentation", generateCustomerSegmentation(recentSales));
        analytics.put("lifetimeValueAnalysis", generateLifetimeValueAnalysis(recentSales));
        analytics.put("behaviorAnalysis", generateCustomerBehaviorAnalysis(recentSales));
        analytics.put("acquisitionMetrics", generateCustomerAcquisitionMetrics(months));
        analytics.put("churnAnalysis", generateChurnAnalysis(months));

        return analytics;
    }

    /**
     * Generate customer lifetime value report with pagination
     */
    public Page<Map<String, Object>> generateCustomerLifetimeValueReport(Pageable pageable) {
        log.info("Generating customer lifetime value report with pagination");

        // This would typically use a custom repository method with pagination
        // For now, implementing basic version
        List<Customer> customers = customerRepository.findAll();

        List<Map<String, Object>> customerLTV = customers.stream()
                .map(this::calculateCustomerLifetimeValue)
                .sorted((a, b) -> ((BigDecimal) b.get("totalValue")).compareTo((BigDecimal) a.get("totalValue")))
                .collect(Collectors.toList());

        // Manual pagination implementation - in production, use database pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), customerLTV.size());
        List<Map<String, Object>> pageContent = customerLTV.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, customerLTV.size());
    }

    /**
     * Generate customer retention analysis
     */
    public Map<String, Object> generateCustomerRetentionReport(int months) {
        log.info("Generating customer retention analysis for {} months", months);

        Map<String, Object> retention = new HashMap<>();
        retention.put("cohortAnalysis", generateCohortAnalysis(months));
        retention.put("retentionRates", calculateRetentionRates(months));
        retention.put("repeatPurchaseAnalysis", analyzeRepeatPurchases(months));
        retention.put("customerLifecycle", analyzeCustomerLifecycle(months));

        return retention;
    }

    /**
     * Generate comprehensive product performance report with accurate calculations
     */
    public Map<String, Object> generateProductPerformanceReport(ReportRequestDTO request) {
        log.info("Generating comprehensive product performance report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        // Fetch and filter sales data
        List<Sale> sales = saleRepository.findBySaleDateBetween(request.getStartDate(), request.getEndDate())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        // Apply additional filters if provided
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            sales = sales.stream()
                    .filter(sale -> sale.getItems().stream()
                            .anyMatch(item -> request.getProductIds().contains(item.getProduct().getId())))
                    .collect(Collectors.toList());
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            sales = sales.stream()
                    .filter(sale -> sale.getItems().stream()
                            .anyMatch(item -> item.getProduct().getCategory() != null &&
                                    request.getCategoryIds().contains(item.getProduct().getCategory().getId())))
                    .collect(Collectors.toList());
        }

        log.debug("Processing {} sales for product performance analysis", sales.size());

        // Generate comprehensive performance analysis
        Map<String, Object> performance = new HashMap<>();

        // Core product metrics with accurate calculations
        performance.put("productRankings", generateProductRankings(sales));
        performance.put("profitabilityAnalysis", generateProfitabilityAnalysis(sales));
        performance.put("categoryPerformance", generateCategoryPerformance(sales));

        // Additional analysis sections
        performance.put("productTrends", generateProductTrends(sales));
        performance.put("crossSellAnalysis", generateCrossSellAnalysis(sales));

        // Summary metrics for the entire report
        performance.put("reportSummary", generateProductReportSummary(sales, request));

        // Data validation and consistency checks
        performance.put("dataValidation", performDataValidationChecks(sales));

        // Safe access to nested map data for logging
        Object productRankings = performance.get("productRankings");
        if (productRankings instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rankingsMap = (Map<String, Object>) productRankings;
            Object summary = rankingsMap.get("summary");
            log.info("Product performance report generated successfully with {} products analyzed", summary);
        } else {
            log.info("Product performance report generated successfully");
        }

        return performance;
    }

    /**
     * Generate inventory turnover report
     */
    public Map<String, Object> generateInventoryTurnoverReport(int months, List<Long> categoryIds) {
        log.info("Generating inventory turnover report for {} months", months);

        LocalDateTime startDate = LocalDateTime.now().minusMonths(months);
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, LocalDateTime.now());

        Map<String, Object> turnover = new HashMap<>();
        turnover.put("turnoverRates", calculateTurnoverRates(sales, categoryIds));
        turnover.put("slowMovingItems", identifySlowMovingItems(sales, categoryIds));
        turnover.put("fastMovingItems", identifyFastMovingItems(sales, categoryIds));
        turnover.put("stockOptimization", generateStockOptimizationRecommendations(sales, categoryIds));

        return turnover;
    }

    /**
     * Generate inventory status report
     */
    public Map<String, Object> generateInventoryStatusReport(Boolean includeInactive, List<Long> warehouseIds) {
        log.info("Generating inventory status report");

        List<Product> products = productRepository.findAll();
        if (!includeInactive) {
            products = products.stream()
                    .filter(product -> product.getStockQuantity() > 0)
                    .collect(Collectors.toList());
        }

        Map<String, Object> status = new HashMap<>();
        status.put("stockLevels", categorizeStockLevels(products));
        status.put("lowStockAlerts", generateLowStockAlerts(products));
        status.put("outOfStockItems", getOutOfStockItems(products));
        status.put("inventoryValuation", calculateInventoryValuation(products));
        status.put("warehouseDistribution", analyzeWarehouseDistribution(products, warehouseIds));

        return status;
    }

    /**
     * Generate inventory valuation report
     */
    public Map<String, Object> generateInventoryValuationReport(String valuationMethod, List<Long> categoryIds) {
        log.info("Generating inventory valuation report using {} method", valuationMethod);

        List<Product> products = productRepository.findAll();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            products = products.stream()
                    .filter(product -> product.getCategory() != null &&
                            categoryIds.contains(product.getCategory().getId()))
                    .collect(Collectors.toList());
        }

        Map<String, Object> valuation = new HashMap<>();
        valuation.put("totalValuation", calculateTotalValuation(products, valuationMethod));
        valuation.put("categoryBreakdown", calculateCategoryValuation(products, valuationMethod));
        valuation.put("valuationMethod", valuationMethod);
        valuation.put("marketValueComparison", compareMarketValues(products));

        return valuation;
    }

    /**
     * Generate promotion effectiveness report
     */
    public Map<String, Object> generatePromotionEffectivenessReport(ReportRequestDTO request) {
        log.info("Generating promotion effectiveness report");

        List<AppliedPromotion> appliedPromotions = appliedPromotionRepository
                .findByAppliedAtBetween(request.getStartDate(), request.getEndDate());

        Map<String, Object> effectiveness = new HashMap<>();
        effectiveness.put("promotionROI", calculatePromotionROI(appliedPromotions));
        effectiveness.put("usageStatistics", generatePromotionUsageStats(appliedPromotions));
        effectiveness.put("customerResponse", analyzeCustomerResponse(appliedPromotions));
        effectiveness.put("revenueImpact", calculateRevenueImpact(appliedPromotions));

        return effectiveness;
    }

    /**
     * Generate promotion usage report
     */
    public Map<String, Object> generatePromotionUsageReport(List<Long> promotionIds, int days) {
        log.info("Generating promotion usage report for {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<AppliedPromotion> appliedPromotions = appliedPromotionRepository
                .findByAppliedAtBetween(startDate, LocalDateTime.now());

        if (promotionIds != null && !promotionIds.isEmpty()) {
            appliedPromotions = appliedPromotions.stream()
                    .filter(ap -> promotionIds.contains(ap.getPromotion().getId()))
                    .collect(Collectors.toList());
        }

        Map<String, Object> usage = new HashMap<>();
        usage.put("dailyUsage", generateDailyPromotionUsage(appliedPromotions));
        usage.put("topPromotions", getTopPerformingPromotions(appliedPromotions));
        usage.put("customerSegments", analyzePromotionByCustomerSegment(appliedPromotions));
        usage.put("conversionRates", calculatePromotionConversionRates(appliedPromotions));

        return usage;
    }

    /**
     * Generate comprehensive financial revenue report with detailed business intelligence
     */
    public Map<String, Object> generateFinancialRevenueReport(ReportRequestDTO request) {
        log.info("Generating comprehensive financial revenue report for period: {} to {}",
                request.getStartDate(), request.getEndDate());

        // Get comprehensive sales data with all related entities
        List<Sale> sales = saleRepository.findCompletedSalesWithDetailsForPeriod(
                request.getStartDate(), request.getEndDate());

        // Get financial summary data
        Object[] financialSummary = saleRepository.getFinancialSummaryForPeriod(
                request.getStartDate(), request.getEndDate());

        // Handle case where no financial data is found
        if (financialSummary == null) {
            financialSummary = new Object[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                          BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L};
        }

        Map<String, Object> financial = new HashMap<>();

        // 1. Revenue Analysis
        financial.put("revenueAnalysis", generateComprehensiveRevenueAnalysis(sales, financialSummary, request));

        // 2. Profit Margin Analysis
        financial.put("profitMarginAnalysis", generateDetailedProfitMarginAnalysis(sales, request));

        // 3. Payment Method Revenue Breakdown
        financial.put("paymentMethodAnalysis", generatePaymentMethodRevenueAnalysis(request));

        // 4. Tax Analysis
        financial.put("taxAnalysis", generateComprehensiveTaxAnalysis(sales, request));

        // 5. Cost Analysis
        financial.put("costAnalysis", generateDetailedCostAnalysis(sales, request));

        // 6. Advanced Metrics
        financial.put("advancedMetrics", generateAdvancedFinancialMetrics(sales, request));

        // 7. Executive Summary
        financial.put("executiveSummary", generateExecutiveFinancialSummary(sales, financialSummary, request));

        log.info("Financial revenue report generated successfully with {} sales records", sales.size());
        return financial;
    }

    /**
     * Generate default dashboard for general users
     */
    public Map<String, Object> generateDefaultDashboard(int days) {
        log.info("Generating default dashboard for {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        Map<String, Object> dashboard = new HashMap<>();

        // Basic KPIs suitable for all users
        dashboard.put("summary", generateBasicSummary(startDate, endDate));
        dashboard.put("recentSales", getRecentSalesMetrics(days));
        dashboard.put("topProducts", getTopProductsMetrics(days));
        dashboard.put("salesOverview", generateSalesOverview(startDate, endDate));
        dashboard.put("quickStats", generateQuickStats());

        return dashboard;
    }

    /**
     * Generate executive dashboard
     */
    public Map<String, Object> generateExecutiveDashboard(int days) {
        log.info("Generating executive dashboard for {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("kpis", generateExecutiveKPIs(startDate, endDate));
        dashboard.put("salesOverview", generateSalesOverview(startDate, endDate));
        dashboard.put("customerMetrics", generateCustomerMetrics(startDate, endDate));
        dashboard.put("financialSummary", generateFinancialSummary(startDate, endDate));
        dashboard.put("alerts", generateExecutiveAlerts());

        return dashboard;
    }

    /**
     * Generate operational dashboard
     */
    public Map<String, Object> generateOperationalDashboard() {
        log.info("Generating operational dashboard");

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("todaysSales", getTodaysSalesMetrics());
        dashboard.put("inventoryAlerts", getInventoryAlerts());
        dashboard.put("pendingOrders", getPendingOrdersMetrics());
        dashboard.put("customerService", getCustomerServiceMetrics());
        dashboard.put("systemHealth", getSystemHealthMetrics());

        return dashboard;
    }

    /**
     * Generate real-time KPIs
     */
    public Map<String, Object> generateRealTimeKPIs() {
        log.info("Generating real-time KPIs");

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("todaysSales", getTodaysSalesCount(today, now));
        kpis.put("todaysRevenue", getTodaysRevenue(today, now));
        kpis.put("activeCustomers", getActiveCustomersToday(today, now));
        kpis.put("inventoryValue", getCurrentInventoryValue());
        kpis.put("lowStockItems", getLowStockItemsCount());
        kpis.put("pendingReturns", getPendingReturnsCount());

        return kpis;
    }

    // ==================== HELPER METHODS (STUB IMPLEMENTATIONS) ====================
    // These methods provide basic implementations and should be enhanced based on specific business requirements



    private List<SalesReportDTO.SalesTrend> generateSalesTrendsData(List<Sale> sales, ReportRequestDTO request) {
        Map<String, List<Sale>> salesByMonth = sales.stream()
                .collect(Collectors.groupingBy(sale -> sale.getSaleDate().format(MONTH_FORMATTER)));

        List<SalesReportDTO.SalesTrend> trends = new ArrayList<>();
        List<String> sortedMonths = salesByMonth.keySet().stream().sorted().collect(Collectors.toList());

        for (int i = 0; i < sortedMonths.size(); i++) {
            String month = sortedMonths.get(i);
            List<Sale> monthlySales = salesByMonth.get(month);

            BigDecimal monthlyRevenue = monthlySales.stream()
                    .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate growth rate compared to previous month
            BigDecimal growthRate = BigDecimal.ZERO;
            String trendDirection = "Stable";

            if (i > 0) {
                String previousMonth = sortedMonths.get(i - 1);
                List<Sale> previousMonthlySales = salesByMonth.get(previousMonth);
                BigDecimal previousRevenue = previousMonthlySales.stream()
                        .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    growthRate = monthlyRevenue.subtract(previousRevenue)
                            .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    if (growthRate.compareTo(BigDecimal.valueOf(5)) > 0) {
                        trendDirection = "Increasing";
                    } else if (growthRate.compareTo(BigDecimal.valueOf(-5)) < 0) {
                        trendDirection = "Decreasing";
                    } else {
                        trendDirection = "Stable";
                    }
                }
            }

            trends.add(SalesReportDTO.SalesTrend.builder()
                    .period(month)
                    .salesCount((long) monthlySales.size())
                    .revenue(monthlyRevenue)
                    .growthRate(growthRate)
                    .trendDirection(trendDirection)
                    .build());
        }

        return trends;
    }





    // Customer Analytics Implementation Methods

    /**
     * Generate customer segmentation based on purchase behavior and value
     */
    private Map<String, Object> generateCustomerSegmentation(List<Sale> sales) {
        log.debug("Generating customer segmentation for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "segments", List.of(),
                "totalCustomers", 0,
                "message", "No sales data available for segmentation"
            );
        }

        // Group sales by customer and calculate metrics
        Map<Customer, List<Sale>> salesByCustomer = sales.stream()
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(Sale::getCustomer));

        List<Map<String, Object>> customerMetrics = salesByCustomer.entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Sale> customerSales = entry.getValue();

                    BigDecimal totalSpent = customerSales.stream()
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int totalOrders = customerSales.size();
                    BigDecimal avgOrderValue = totalOrders > 0 ?
                            totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    LocalDateTime firstPurchase = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);

                    LocalDateTime lastPurchase = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    // Calculate days between first and last purchase
                    long daysBetween = firstPurchase != null && lastPurchase != null ?
                            ChronoUnit.DAYS.between(firstPurchase, lastPurchase) : 0;

                    // Use HashMap to allow null values
                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("customer", customer);
                    customerData.put("totalSpent", totalSpent);
                    customerData.put("totalOrders", totalOrders);
                    customerData.put("avgOrderValue", avgOrderValue);
                    customerData.put("firstPurchase", firstPurchase);
                    customerData.put("lastPurchase", lastPurchase);
                    customerData.put("daysBetween", daysBetween);
                    return customerData;
                })
                .collect(Collectors.toList());

        // Calculate segmentation thresholds
        List<BigDecimal> spentAmounts = customerMetrics.stream()
                .map(m -> (BigDecimal) m.get("totalSpent"))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        BigDecimal highValueThreshold = calculatePercentile(spentAmounts, 80);
        BigDecimal mediumValueThreshold = calculatePercentile(spentAmounts, 50);

        // Segment customers
        Map<String, List<Map<String, Object>>> segments = new HashMap<>();
        segments.put("highValue", new ArrayList<>());
        segments.put("mediumValue", new ArrayList<>());
        segments.put("lowValue", new ArrayList<>());
        segments.put("newCustomers", new ArrayList<>());
        segments.put("atRiskCustomers", new ArrayList<>());

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

        for (Map<String, Object> metrics : customerMetrics) {
            BigDecimal totalSpent = (BigDecimal) metrics.get("totalSpent");
            LocalDateTime firstPurchase = (LocalDateTime) metrics.get("firstPurchase");
            LocalDateTime lastPurchase = (LocalDateTime) metrics.get("lastPurchase");
            Customer customer = (Customer) metrics.get("customer");

            // Use HashMap to allow null values for firstPurchase and lastPurchase
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("customerId", customer.getId());
            customerData.put("customerName", customer.getName());
            customerData.put("email", customer.getEmail());
            customerData.put("totalSpent", totalSpent);
            customerData.put("totalOrders", metrics.get("totalOrders"));
            customerData.put("avgOrderValue", metrics.get("avgOrderValue"));
            customerData.put("firstPurchase", firstPurchase);
            customerData.put("lastPurchase", lastPurchase);

            // Segment logic
            if (firstPurchase != null && firstPurchase.isAfter(thirtyDaysAgo)) {
                segments.get("newCustomers").add(customerData);
            } else if (lastPurchase != null && lastPurchase.isBefore(ninetyDaysAgo)) {
                segments.get("atRiskCustomers").add(customerData);
            } else if (totalSpent.compareTo(highValueThreshold) >= 0) {
                segments.get("highValue").add(customerData);
            } else if (totalSpent.compareTo(mediumValueThreshold) >= 0) {
                segments.get("mediumValue").add(customerData);
            } else {
                segments.get("lowValue").add(customerData);
            }
        }

        // Calculate segment summaries
        Map<String, Object> segmentSummaries = segments.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Map.of(
                                "count", entry.getValue().size(),
                                "percentage", customerMetrics.isEmpty() ? 0.0 :
                                        (double) entry.getValue().size() / customerMetrics.size() * 100,
                                "totalRevenue", entry.getValue().stream()
                                        .map(c -> (BigDecimal) c.get("totalSpent"))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                                "avgOrderValue", entry.getValue().isEmpty() ? BigDecimal.ZERO :
                                        entry.getValue().stream()
                                                .map(c -> (BigDecimal) c.get("avgOrderValue"))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                .divide(BigDecimal.valueOf(entry.getValue().size()), 2, RoundingMode.HALF_UP)
                        )
                ));

        return Map.of(
                "segments", segmentSummaries,
                "customerDetails", segments,
                "totalCustomers", customerMetrics.size(),
                "thresholds", Map.of(
                        "highValue", highValueThreshold,
                        "mediumValue", mediumValueThreshold
                ),
                "summary", Map.of(
                        "totalRevenue", spentAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add),
                        "avgCustomerValue", spentAmounts.isEmpty() ? BigDecimal.ZERO :
                                spentAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(spentAmounts.size()), 2, RoundingMode.HALF_UP)
                )
        );
    }
    /**
     * Generate lifetime value analysis for customers
     */
    private Map<String, Object> generateLifetimeValueAnalysis(List<Sale> sales) {
        log.debug("Generating lifetime value analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "analysis", Map.of(),
                "topCustomers", List.of(),
                "metrics", Map.of(),
                "message", "No sales data available for lifetime value analysis"
            );
        }

        // Group sales by customer
        Map<Customer, List<Sale>> salesByCustomer = sales.stream()
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(Sale::getCustomer));

        List<Map<String, Object>> customerLTVData = salesByCustomer.entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Sale> customerSales = entry.getValue();

                    BigDecimal totalRevenue = customerSales.stream()
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    int totalOrders = customerSales.size();
                    BigDecimal avgOrderValue = totalOrders > 0 ?
                            totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    LocalDateTime firstPurchase = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);

                    LocalDateTime lastPurchase = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    // Calculate customer lifespan in days
                    long lifespanDays = firstPurchase != null && lastPurchase != null ?
                            ChronoUnit.DAYS.between(firstPurchase, lastPurchase) + 1 : 1;

                    // Calculate purchase frequency (orders per month)
                    double purchaseFrequency = lifespanDays > 0 ?
                            (double) totalOrders / (lifespanDays / 30.0) : 0;

                    // Calculate predicted LTV (simple model: AOV * frequency * estimated lifespan)
                    double estimatedLifespanMonths = 24; // Assume 24 months average lifespan
                    BigDecimal predictedLTV = avgOrderValue
                            .multiply(BigDecimal.valueOf(purchaseFrequency))
                            .multiply(BigDecimal.valueOf(estimatedLifespanMonths));

                    // Calculate profit margin (assuming 30% margin)
                    BigDecimal profitMargin = totalRevenue.multiply(BigDecimal.valueOf(0.30));

                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("customerId", customer.getId());
                    customerData.put("customerName", customer.getName());
                    customerData.put("email", customer.getEmail());
                    customerData.put("totalRevenue", totalRevenue);
                    customerData.put("totalOrders", totalOrders);
                    customerData.put("avgOrderValue", avgOrderValue);
                    customerData.put("firstPurchase", firstPurchase);
                    customerData.put("lastPurchase", lastPurchase);
                    customerData.put("lifespanDays", lifespanDays);
                    customerData.put("purchaseFrequency", BigDecimal.valueOf(purchaseFrequency).setScale(2, RoundingMode.HALF_UP));
                    customerData.put("predictedLTV", predictedLTV.setScale(2, RoundingMode.HALF_UP));
                    customerData.put("profitMargin", profitMargin.setScale(2, RoundingMode.HALF_UP));
                    customerData.put("customerSegment", categorizeCustomerByLTV(totalRevenue));
                    return customerData;
                })
                .sorted((a, b) -> {
                    BigDecimal aRevenue = (BigDecimal) a.get("totalRevenue");
                    BigDecimal bRevenue = (BigDecimal) b.get("totalRevenue");
                    return bRevenue.compareTo(aRevenue);
                })
                .collect(Collectors.toList());

        // Calculate overall metrics
        BigDecimal totalRevenue = customerLTVData.stream()
                .map(c -> (BigDecimal) c.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgLTV = customerLTVData.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(customerLTVData.size()), 2, RoundingMode.HALF_UP);

        BigDecimal avgOrderValue = customerLTVData.stream()
                .map(c -> (BigDecimal) c.get("avgOrderValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(customerLTVData.size(), 1)), 2, RoundingMode.HALF_UP);

        double avgPurchaseFrequency = customerLTVData.stream()
                .mapToDouble(c -> ((BigDecimal) c.get("purchaseFrequency")).doubleValue())
                .average()
                .orElse(0.0);

        // Get top 10 customers by LTV
        List<Map<String, Object>> topCustomers = customerLTVData.stream()
                .limit(10)
                .collect(Collectors.toList());

        // LTV distribution analysis
        Map<String, Long> ltvDistribution = customerLTVData.stream()
                .collect(Collectors.groupingBy(
                        c -> (String) c.get("customerSegment"),
                        Collectors.counting()
                ));

        return Map.of(
                "analysis", Map.of(
                        "totalCustomers", customerLTVData.size(),
                        "totalRevenue", totalRevenue,
                        "avgLifetimeValue", avgLTV,
                        "avgOrderValue", avgOrderValue,
                        "avgPurchaseFrequency", BigDecimal.valueOf(avgPurchaseFrequency).setScale(2, RoundingMode.HALF_UP),
                        "ltvDistribution", ltvDistribution
                ),
                "topCustomers", topCustomers,
                "metrics", Map.of(
                        "highValueCustomers", ltvDistribution.getOrDefault("High Value", 0L),
                        "mediumValueCustomers", ltvDistribution.getOrDefault("Medium Value", 0L),
                        "lowValueCustomers", ltvDistribution.getOrDefault("Low Value", 0L),
                        "revenueConcentration", calculateRevenueConcentration(customerLTVData)
                ),
                "recommendations", generateLTVRecommendations(customerLTVData, avgLTV)
        );
    }

    private String categorizeCustomerByLTV(BigDecimal totalRevenue) {
        if (totalRevenue.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            return "High Value";
        } else if (totalRevenue.compareTo(BigDecimal.valueOf(300)) >= 0) {
            return "Medium Value";
        } else {
            return "Low Value";
        }
    }

    private Map<String, Object> calculateRevenueConcentration(List<Map<String, Object>> customerData) {
        if (customerData.isEmpty()) {
            return Map.of("top10Percent", 0.0, "top20Percent", 0.0);
        }

        BigDecimal totalRevenue = customerData.stream()
                .map(c -> (BigDecimal) c.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int top10Count = Math.max(1, customerData.size() / 10);
        int top20Count = Math.max(1, customerData.size() / 5);

        BigDecimal top10Revenue = customerData.stream()
                .limit(top10Count)
                .map(c -> (BigDecimal) c.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal top20Revenue = customerData.stream()
                .limit(top20Count)
                .map(c -> (BigDecimal) c.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double top10Percent = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                top10Revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;

        double top20Percent = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                top20Revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue() : 0.0;

        return Map.of(
                "top10Percent", top10Percent,
                "top20Percent", top20Percent
        );
    }

    private List<String> generateLTVRecommendations(List<Map<String, Object>> customerData, BigDecimal avgLTV) {
        List<String> recommendations = new ArrayList<>();

        long highValueCount = customerData.stream()
                .mapToLong(c -> "High Value".equals(c.get("customerSegment")) ? 1 : 0)
                .sum();

        double highValuePercentage = customerData.isEmpty() ? 0.0 :
                (double) highValueCount / customerData.size() * 100;

        if (highValuePercentage < 20) {
            recommendations.add("Focus on converting medium-value customers to high-value through targeted promotions");
        }

        if (avgLTV.compareTo(BigDecimal.valueOf(500)) < 0) {
            recommendations.add("Implement customer retention programs to increase average lifetime value");
        }

        recommendations.add("Develop personalized marketing campaigns for top 20% of customers");
        recommendations.add("Create loyalty programs to increase purchase frequency");

        return recommendations;
    }

    private Map<String, Object> calculateCustomerLifetimeValue(Customer customer) {
        return Map.of("customerId", customer.getId(), "totalValue", BigDecimal.valueOf(1000));
    }
    /**
     * Generate customer behavior analysis
     */
    private Map<String, Object> generateCustomerBehaviorAnalysis(List<Sale> sales) {
        log.debug("Generating customer behavior analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "purchasePatterns", Map.of(),
                "seasonality", Map.of(),
                "preferences", Map.of(),
                "message", "No sales data available for behavior analysis"
            );
        }

        // Group sales by customer
        Map<Customer, List<Sale>> salesByCustomer = sales.stream()
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(Sale::getCustomer));

        // Analyze purchase patterns
        Map<String, Object> purchasePatterns = analyzePurchasePatterns(sales, salesByCustomer);

        // Analyze seasonality and timing
        Map<String, Object> seasonality = analyzeSeasonalityPatterns(sales);

        // Analyze product preferences
        Map<String, Object> preferences = analyzeProductPreferences(sales);

        // Analyze customer journey
        Map<String, Object> customerJourney = analyzeCustomerJourney(salesByCustomer);

        return Map.of(
                "purchasePatterns", purchasePatterns,
                "seasonality", seasonality,
                "preferences", preferences,
                "customerJourney", customerJourney,
                "summary", Map.of(
                        "totalCustomers", salesByCustomer.size(),
                        "totalSales", sales.size(),
                        "analysisDate", LocalDateTime.now().format(DATE_FORMATTER)
                )
        );
    }

    private Map<String, Object> analyzePurchasePatterns(List<Sale> sales, Map<Customer, List<Sale>> salesByCustomer) {
        // Calculate purchase frequency distribution
        Map<String, Long> frequencyDistribution = salesByCustomer.values().stream()
                .collect(Collectors.groupingBy(
                        customerSales -> {
                            int count = customerSales.size();
                            if (count == 1) return "One-time buyers";
                            else if (count <= 3) return "Occasional buyers";
                            else if (count <= 10) return "Regular buyers";
                            else return "Frequent buyers";
                        },
                        Collectors.counting()
                ));

        // Calculate average days between purchases
        List<Double> daysBetweenPurchases = salesByCustomer.values().stream()
                .filter(customerSales -> customerSales.size() > 1)
                .map(customerSales -> {
                    List<LocalDateTime> sortedDates = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .sorted()
                            .collect(Collectors.toList());

                    double totalDays = 0;
                    for (int i = 1; i < sortedDates.size(); i++) {
                        totalDays += ChronoUnit.DAYS.between(sortedDates.get(i-1), sortedDates.get(i));
                    }
                    return totalDays / (sortedDates.size() - 1);
                })
                .collect(Collectors.toList());

        double avgDaysBetweenPurchases = daysBetweenPurchases.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Analyze order value patterns
        Map<String, Long> orderValueDistribution = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> {
                            BigDecimal amount = sale.getTotalAmount();
                            if (amount.compareTo(BigDecimal.valueOf(50)) < 0) return "Low value (<$50)";
                            else if (amount.compareTo(BigDecimal.valueOf(200)) < 0) return "Medium value ($50-$200)";
                            else if (amount.compareTo(BigDecimal.valueOf(500)) < 0) return "High value ($200-$500)";
                            else return "Premium value (>$500)";
                        },
                        Collectors.counting()
                ));

        return Map.of(
                "frequencyDistribution", frequencyDistribution,
                "avgDaysBetweenPurchases", BigDecimal.valueOf(avgDaysBetweenPurchases).setScale(1, RoundingMode.HALF_UP),
                "orderValueDistribution", orderValueDistribution,
                "repeatCustomerRate", calculateRepeatCustomerRate(salesByCustomer)
        );
    }

    private Map<String, Object> analyzeSeasonalityPatterns(List<Sale> sales) {
        // Group by month
        Map<String, Long> salesByMonth = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        // Group by day of week
        Map<String, Long> salesByDayOfWeek = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().getDayOfWeek().toString(),
                        Collectors.counting()
                ));

        // Group by hour of day
        Map<String, Long> salesByHour = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> String.valueOf(sale.getSaleDate().getHour()),
                        Collectors.counting()
                ));

        return Map.of(
                "monthlyTrends", salesByMonth,
                "dayOfWeekTrends", salesByDayOfWeek,
                "hourlyTrends", salesByHour,
                "peakSalesDay", findPeakDay(salesByDayOfWeek),
                "peakSalesHour", findPeakHour(salesByHour)
        );
    }

    private Map<String, Object> analyzeProductPreferences(List<Sale> sales) {
        // This would require SaleItem data, for now return basic analysis
        Map<String, Object> preferences = new HashMap<>();

        // Analyze by payment method
        Map<String, Long> paymentMethodPreferences = sales.stream()
                .filter(sale -> sale.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        sale -> sale.getPaymentMethod().toString(),
                        Collectors.counting()
                ));

        // Analyze by sale type
        Map<String, Long> saleTypePreferences = sales.stream()
                .filter(sale -> sale.getSaleType() != null)
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleType().toString(),
                        Collectors.counting()
                ));

        preferences.put("paymentMethodPreferences", paymentMethodPreferences);
        preferences.put("saleTypePreferences", saleTypePreferences);
        preferences.put("avgDiscountUsage", calculateAverageDiscountUsage(sales));

        return preferences;
    }

    private Map<String, Object> analyzeCustomerJourney(Map<Customer, List<Sale>> salesByCustomer) {
        // Analyze customer lifecycle stages
        Map<String, Long> lifecycleStages = salesByCustomer.entrySet().stream()
                .collect(Collectors.groupingBy(
                        entry -> {
                            List<Sale> customerSales = entry.getValue();
                            LocalDateTime firstPurchase = customerSales.stream()
                                    .map(Sale::getSaleDate)
                                    .min(LocalDateTime::compareTo)
                                    .orElse(null);

                            if (firstPurchase == null) return "Unknown";

                            long daysAgo = ChronoUnit.DAYS.between(firstPurchase, LocalDateTime.now());

                            if (daysAgo <= 30) return "New Customer";
                            else if (daysAgo <= 90) return "Growing Customer";
                            else if (daysAgo <= 365) return "Established Customer";
                            else return "Loyal Customer";
                        },
                        Collectors.counting()
                ));

        return Map.of(
                "lifecycleStages", lifecycleStages,
                "avgCustomerAge", calculateAverageCustomerAge(salesByCustomer),
                "customerRetentionInsights", generateRetentionInsights(salesByCustomer)
        );
    }

    // Helper methods for behavior analysis
    private double calculateRepeatCustomerRate(Map<Customer, List<Sale>> salesByCustomer) {
        long repeatCustomers = salesByCustomer.values().stream()
                .mapToLong(customerSales -> customerSales.size() > 1 ? 1 : 0)
                .sum();

        return salesByCustomer.isEmpty() ? 0.0 :
                (double) repeatCustomers / salesByCustomer.size() * 100;
    }

    private String findPeakDay(Map<String, Long> salesByDayOfWeek) {
        return salesByDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private String findPeakHour(Map<String, Long> salesByHour) {
        return salesByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + ":00")
                .orElse("Unknown");
    }

    private double calculateAverageDiscountUsage(List<Sale> sales) {
        return sales.stream()
                .filter(sale -> sale.getDiscountAmount() != null)
                .mapToDouble(sale -> sale.getDiscountAmount().doubleValue())
                .average()
                .orElse(0.0);
    }

    private double calculateAverageCustomerAge(Map<Customer, List<Sale>> salesByCustomer) {
        return salesByCustomer.entrySet().stream()
                .mapToDouble(entry -> {
                    LocalDateTime firstPurchase = entry.getValue().stream()
                            .map(Sale::getSaleDate)
                            .min(LocalDateTime::compareTo)
                            .orElse(LocalDateTime.now());
                    return ChronoUnit.DAYS.between(firstPurchase, LocalDateTime.now());
                })
                .average()
                .orElse(0.0);
    }

    private List<String> generateRetentionInsights(Map<Customer, List<Sale>> salesByCustomer) {
        List<String> insights = new ArrayList<>();

        double repeatRate = calculateRepeatCustomerRate(salesByCustomer);
        if (repeatRate < 30) {
            insights.add("Low repeat customer rate - consider implementing loyalty programs");
        }

        long oneTimeBuyers = salesByCustomer.values().stream()
                .mapToLong(customerSales -> customerSales.size() == 1 ? 1 : 0)
                .sum();

        if (oneTimeBuyers > salesByCustomer.size() * 0.6) {
            insights.add("High percentage of one-time buyers - focus on customer retention strategies");
        }

        insights.add("Implement personalized follow-up campaigns for new customers");

        return insights;
    }

    /**
     * Generate customer acquisition metrics
     */
    private Map<String, Object> generateCustomerAcquisitionMetrics(int months) {
        log.debug("Generating customer acquisition metrics for {} months", months);

        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(months);
        LocalDateTime now = LocalDateTime.now();

        // Get all customers created in the period
        List<Customer> newCustomers = customerRepository.findByCreatedAtBetween(cutoffDate, now);

        if (newCustomers.isEmpty()) {
            return Map.of(
                "acquisitionTrends", Map.of(),
                "metrics", Map.of(),
                "channels", Map.of(),
                "message", "No new customers acquired in the specified period"
            );
        }

        // Monthly acquisition trends
        Map<String, Long> monthlyAcquisition = newCustomers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()
                ));

        // Calculate growth rate
        List<String> sortedMonths = monthlyAcquisition.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        Double growthRate = null;
        if (sortedMonths.size() >= 2) {
            String firstMonth = sortedMonths.get(0);
            String lastMonth = sortedMonths.get(sortedMonths.size() - 1);
            Long firstCount = monthlyAcquisition.get(firstMonth);
            Long lastCount = monthlyAcquisition.get(lastMonth);

            if (firstCount > 0) {
                growthRate = ((double) (lastCount - firstCount) / firstCount) * 100;
            }
        }

        // Analyze acquisition channels (simplified - based on customer type or other available data)
        Map<String, Long> acquisitionChannels = newCustomers.stream()
                .collect(Collectors.groupingBy(
                        customer -> customer.getCustomerType() != null ?
                                customer.getCustomerType().toString() : "UNKNOWN",
                        Collectors.counting()
                ));

        // Calculate conversion metrics (customers who made purchases)
        List<Customer> customersWithPurchases = newCustomers.stream()
                .filter(customer -> {
                    // Check if customer has any completed sales
                    return saleRepository.findAll().stream()
                            .anyMatch(sale -> sale.getCustomer() != null &&
                                    sale.getCustomer().getId().equals(customer.getId()) &&
                                    sale.getStatus() == SaleStatus.COMPLETED);
                })
                .collect(Collectors.toList());

        double conversionRate = newCustomers.isEmpty() ? 0.0 :
                (double) customersWithPurchases.size() / newCustomers.size() * 100;

        // Calculate average time to first purchase
        List<Long> daysToFirstPurchase = customersWithPurchases.stream()
                .map(customer -> {
                    Optional<Sale> firstSale = saleRepository.findAll().stream()
                            .filter(sale -> sale.getCustomer() != null &&
                                    sale.getCustomer().getId().equals(customer.getId()) &&
                                    sale.getStatus() == SaleStatus.COMPLETED)
                            .min(Comparator.comparing(Sale::getSaleDate));

                    if (firstSale.isPresent()) {
                        return ChronoUnit.DAYS.between(customer.getCreatedAt(), firstSale.get().getSaleDate());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        double avgDaysToFirstPurchase = daysToFirstPurchase.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        // Calculate customer acquisition cost (simplified)
        BigDecimal estimatedAcquisitionCost = BigDecimal.valueOf(50.0); // Placeholder

        // Create acquisition trends map with null-safe handling
        Map<String, Object> acquisitionTrends = new HashMap<>();
        acquisitionTrends.put("monthlyAcquisition", monthlyAcquisition);
        acquisitionTrends.put("growthRate", growthRate != null ?
                BigDecimal.valueOf(growthRate).setScale(2, RoundingMode.HALF_UP) : null);
        acquisitionTrends.put("totalNewCustomers", newCustomers.size());

        // Create metrics map
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("conversionRate", BigDecimal.valueOf(conversionRate).setScale(2, RoundingMode.HALF_UP));
        metrics.put("avgDaysToFirstPurchase", BigDecimal.valueOf(avgDaysToFirstPurchase).setScale(1, RoundingMode.HALF_UP));
        metrics.put("customersWithPurchases", customersWithPurchases.size());
        metrics.put("estimatedAcquisitionCost", estimatedAcquisitionCost);

        // Create channels map
        Map<String, Object> channels = new HashMap<>();
        channels.put("acquisitionChannels", acquisitionChannels);
        channels.put("topChannel", findTopAcquisitionChannel(acquisitionChannels));

        // Create final result map
        Map<String, Object> result = new HashMap<>();
        result.put("acquisitionTrends", acquisitionTrends);
        result.put("metrics", metrics);
        result.put("channels", channels);
        result.put("recommendations", generateAcquisitionRecommendations(conversionRate, avgDaysToFirstPurchase));

        return result;
    }

    /**
     * Generate churn analysis
     */
    private Map<String, Object> generateChurnAnalysis(int months) {
        log.debug("Generating churn analysis for {} months", months);

        LocalDateTime churnThreshold = LocalDateTime.now().minusDays(90); // Consider churned if no purchase in 90 days

        // Get all customers who had purchases before the churn threshold
        List<Customer> allCustomers = customerRepository.findAllActive();

        if (allCustomers.isEmpty()) {
            return Map.of(
                "churnMetrics", Map.of(),
                "churnedCustomers", List.of(),
                "riskAnalysis", Map.of(),
                "message", "No customer data available for churn analysis"
            );
        }

        List<Map<String, Object>> customerChurnData = allCustomers.stream()
                .map(customer -> {
                    // Get customer's last purchase
                    Optional<Sale> lastSale = saleRepository.findAll().stream()
                            .filter(sale -> sale.getCustomer() != null &&
                                    sale.getCustomer().getId().equals(customer.getId()) &&
                                    sale.getStatus() == SaleStatus.COMPLETED)
                            .max(Comparator.comparing(Sale::getSaleDate));

                    LocalDateTime lastPurchase = lastSale.map(Sale::getSaleDate).orElse(null);

                    // Calculate total customer value
                    BigDecimal totalValue = saleRepository.findAll().stream()
                            .filter(sale -> sale.getCustomer() != null &&
                                    sale.getCustomer().getId().equals(customer.getId()) &&
                                    sale.getStatus() == SaleStatus.COMPLETED)
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Determine churn status
                    boolean isChurned = lastPurchase == null || lastPurchase.isBefore(churnThreshold);

                    // Calculate days since last purchase
                    long daysSinceLastPurchase = lastPurchase != null ?
                            ChronoUnit.DAYS.between(lastPurchase, LocalDateTime.now()) : -1;

                    // Risk assessment
                    String riskLevel = assessChurnRisk(lastPurchase, totalValue);

                    Map<String, Object> churnData = new HashMap<>();
                    churnData.put("customerId", customer.getId());
                    churnData.put("customerName", customer.getName());
                    churnData.put("email", customer.getEmail());
                    churnData.put("lastPurchase", lastPurchase);
                    churnData.put("daysSinceLastPurchase", daysSinceLastPurchase);
                    churnData.put("totalValue", totalValue);
                    churnData.put("isChurned", isChurned);
                    churnData.put("riskLevel", riskLevel);
                    churnData.put("customerSince", customer.getCreatedAt());
                    return churnData;
                })
                .collect(Collectors.toList());

        // Calculate churn metrics
        long churnedCustomers = customerChurnData.stream()
                .mapToLong(c -> (Boolean) c.get("isChurned") ? 1 : 0)
                .sum();

        double churnRate = allCustomers.isEmpty() ? 0.0 :
                (double) churnedCustomers / allCustomers.size() * 100;

        // Risk analysis
        Map<String, Long> riskDistribution = customerChurnData.stream()
                .collect(Collectors.groupingBy(
                        c -> (String) c.get("riskLevel"),
                        Collectors.counting()
                ));

        // Get high-risk customers
        List<Map<String, Object>> highRiskCustomers = customerChurnData.stream()
                .filter(c -> "High Risk".equals(c.get("riskLevel")))
                .sorted((a, b) -> {
                    BigDecimal aValue = (BigDecimal) a.get("totalValue");
                    BigDecimal bValue = (BigDecimal) b.get("totalValue");
                    return bValue.compareTo(aValue);
                })
                .limit(10)
                .collect(Collectors.toList());

        // Calculate revenue at risk
        BigDecimal revenueAtRisk = customerChurnData.stream()
                .filter(c -> "High Risk".equals(c.get("riskLevel")) || (Boolean) c.get("isChurned"))
                .map(c -> (BigDecimal) c.get("totalValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "churnMetrics", Map.of(
                        "totalCustomers", allCustomers.size(),
                        "churnedCustomers", churnedCustomers,
                        "churnRate", BigDecimal.valueOf(churnRate).setScale(2, RoundingMode.HALF_UP),
                        "revenueAtRisk", revenueAtRisk,
                        "avgDaysSinceLastPurchase", calculateAvgDaysSinceLastPurchase(customerChurnData)
                ),
                "churnedCustomers", customerChurnData.stream()
                        .filter(c -> (Boolean) c.get("isChurned"))
                        .limit(20)
                        .collect(Collectors.toList()),
                "riskAnalysis", Map.of(
                        "riskDistribution", riskDistribution,
                        "highRiskCustomers", highRiskCustomers,
                        "retentionOpportunity", calculateRetentionOpportunity(customerChurnData)
                ),
                "recommendations", generateChurnRecommendations(churnRate, riskDistribution)
        );
    }

    // Helper methods for acquisition and churn analysis
    private String findTopAcquisitionChannel(Map<String, Long> channels) {
        return channels.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private List<String> generateAcquisitionRecommendations(double conversionRate, double avgDaysToFirstPurchase) {
        List<String> recommendations = new ArrayList<>();

        if (conversionRate < 50) {
            recommendations.add("Improve onboarding process to increase conversion rate");
        }

        if (avgDaysToFirstPurchase > 14) {
            recommendations.add("Implement welcome campaigns to accelerate first purchase");
        }

        recommendations.add("Focus marketing spend on highest-converting channels");
        recommendations.add("Develop targeted campaigns for new customer segments");

        return recommendations;
    }

    private String assessChurnRisk(LocalDateTime lastPurchase, BigDecimal totalValue) {
        if (lastPurchase == null) {
            return "High Risk";
        }

        long daysSinceLastPurchase = ChronoUnit.DAYS.between(lastPurchase, LocalDateTime.now());

        if (daysSinceLastPurchase > 90) {
            return "Churned";
        } else if (daysSinceLastPurchase > 60) {
            return "High Risk";
        } else if (daysSinceLastPurchase > 30) {
            return "Medium Risk";
        } else {
            return "Low Risk";
        }
    }

    private double calculateAvgDaysSinceLastPurchase(List<Map<String, Object>> customerData) {
        return customerData.stream()
                .filter(c -> (Long) c.get("daysSinceLastPurchase") >= 0)
                .mapToLong(c -> (Long) c.get("daysSinceLastPurchase"))
                .average()
                .orElse(0.0);
    }

    private BigDecimal calculateRetentionOpportunity(List<Map<String, Object>> customerData) {
        return customerData.stream()
                .filter(c -> "High Risk".equals(c.get("riskLevel")))
                .map(c -> (BigDecimal) c.get("totalValue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<String> generateChurnRecommendations(double churnRate, Map<String, Long> riskDistribution) {
        List<String> recommendations = new ArrayList<>();

        if (churnRate > 20) {
            recommendations.add("Implement aggressive retention campaigns for high-risk customers");
        }

        long highRiskCount = riskDistribution.getOrDefault("High Risk", 0L);
        if (highRiskCount > 0) {
            recommendations.add("Create personalized win-back campaigns for high-risk customers");
        }

        recommendations.add("Develop loyalty programs to improve customer retention");
        recommendations.add("Implement predictive analytics to identify at-risk customers earlier");

        return recommendations;
    }

    /**
     * Generate comprehensive report summary for product performance with accurate product counts
     */
    private Map<String, Object> generateProductReportSummary(List<Sale> sales, ReportRequestDTO request) {
        log.debug("Generating product report summary for {} sales", sales.size());

        // Get total products in database for comparison
        long totalProductsInDatabase = productRepository.count();

        // Get active products count (excluding discontinued/inactive products)
        long activeProductsInDatabase = productRepository.findAll().stream()
                .filter(product -> product.getProductStatus() == Product.ProductStatus.ACTIVE)
                .count();

        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "totalSales", 0,
                "totalProducts", totalProductsInDatabase,
                "activeProducts", activeProductsInDatabase,
                "productsWithSales", 0,
                "totalRevenue", BigDecimal.ZERO,
                "totalProfit", BigDecimal.ZERO,
                "productCoveragePercentage", BigDecimal.ZERO,
                "message", "No sales data available for the specified period"
            );
        }

        // Calculate summary metrics
        int totalSales = sales.size();

        // Count unique products that had sales in this period
        long productsWithSales = allItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .count();

        // Calculate product coverage percentage
        BigDecimal productCoveragePercentage = activeProductsInDatabase > 0 ?
                BigDecimal.valueOf(productsWithSales)
                        .divide(BigDecimal.valueOf(activeProductsInDatabase), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        BigDecimal totalRevenue = allItems.stream()
                .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = allItems.stream()
                .map(item -> {
                    BigDecimal cost = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                    return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        BigDecimal overallMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        int totalQuantity = allItems.stream()
                .mapToInt(SaleItem::getQuantity)
                .sum();

        // Generate insights about product performance coverage
        List<String> insights = generateProductCoverageInsights(
                totalProductsInDatabase, activeProductsInDatabase, productsWithSales, productCoveragePercentage);

        return Map.of(
                "reportPeriod", Map.of(
                        "startDate", request.getStartDate().format(DATE_FORMATTER),
                        "endDate", request.getEndDate().format(DATE_FORMATTER),
                        "durationDays", ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate())
                ),
                "productCounts", Map.of(
                        "totalProducts", totalProductsInDatabase,
                        "activeProducts", activeProductsInDatabase,
                        "productsWithSales", productsWithSales,
                        "productsWithoutSales", activeProductsInDatabase - productsWithSales,
                        "productCoveragePercentage", productCoveragePercentage
                ),
                "salesMetrics", Map.of(
                        "totalSales", totalSales,
                        "totalQuantitySold", totalQuantity,
                        "avgQuantityPerSale", totalSales > 0 ? totalQuantity / totalSales : 0,
                        "avgProductsPerSale", totalSales > 0 ?
                                allItems.size() / totalSales : 0
                ),
                "financialMetrics", Map.of(
                        "totalRevenue", totalRevenue,
                        "totalCost", totalCost,
                        "totalProfit", totalProfit,
                        "overallProfitMargin", overallMargin,
                        "avgRevenuePerSale", totalSales > 0 ?
                                totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                        "avgRevenuePerProduct", productsWithSales > 0 ?
                                totalRevenue.divide(BigDecimal.valueOf(productsWithSales), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO
                ),
                "insights", insights
        );
    }

    /**
     * Generate insights about product performance coverage
     */
    private List<String> generateProductCoverageInsights(long totalProducts, long activeProducts,
                                                        long productsWithSales, BigDecimal coveragePercentage) {
        List<String> insights = new ArrayList<>();

        if (coveragePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            insights.add("Excellent product coverage - most active products are generating sales");
        } else if (coveragePercentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
            insights.add("Good product coverage - majority of active products are performing");
        } else if (coveragePercentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            insights.add("Moderate product coverage - consider reviewing underperforming products");
        } else if (coveragePercentage.compareTo(BigDecimal.valueOf(20)) >= 0) {
            insights.add("Low product coverage - significant opportunity to improve product performance");
        } else {
            insights.add("Very low product coverage - urgent review of product portfolio needed");
        }

        long inactiveProducts = totalProducts - activeProducts;
        if (inactiveProducts > 0) {
            insights.add(String.format("%d products are inactive and may need attention", inactiveProducts));
        }

        long productsWithoutSales = activeProducts - productsWithSales;
        if (productsWithoutSales > 0) {
            insights.add(String.format("%d active products had no sales in this period", productsWithoutSales));
        }

        return insights;
    }

    /**
     * Perform data validation and consistency checks including product coverage analysis
     */
    private Map<String, Object> performDataValidationChecks(List<Sale> sales) {
        log.debug("Performing data validation checks for {} sales", sales.size());

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Integer> validationCounts = new HashMap<>();

        // Get product coverage metrics
        long totalProductsInDatabase = productRepository.count();
        long activeProductsInDatabase = productRepository.findAll().stream()
                .filter(product -> product.getProductStatus() == Product.ProductStatus.ACTIVE)
                .count();

        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .collect(Collectors.toList());

        long productsWithSales = allItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .count();

        BigDecimal productCoveragePercentage = activeProductsInDatabase > 0 ?
                BigDecimal.valueOf(productsWithSales)
                        .divide(BigDecimal.valueOf(activeProductsInDatabase), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        int totalSales = sales.size();
        int salesWithItems = 0;
        int itemsWithoutCost = 0;
        int itemsWithZeroPrice = 0;
        int itemsWithNegativeQuantity = 0;

        for (Sale sale : sales) {
            if (sale.getItems() != null && !sale.getItems().isEmpty()) {
                salesWithItems++;

                for (SaleItem item : sale.getItems()) {
                    if (item.getCostPrice() == null || item.getCostPrice().equals(BigDecimal.ZERO)) {
                        itemsWithoutCost++;
                    }

                    if (item.getUnitPrice() == null || item.getUnitPrice().equals(BigDecimal.ZERO)) {
                        itemsWithZeroPrice++;
                    }

                    if (item.getQuantity() <= 0) {
                        itemsWithNegativeQuantity++;
                    }
                }
            }
        }

        // Generate warnings based on validation results
        if (itemsWithoutCost > 0) {
            warnings.add(String.format("%d sale items missing cost price data - profit calculations may be inaccurate", itemsWithoutCost));
        }

        if (itemsWithZeroPrice > 0) {
            warnings.add(String.format("%d sale items have zero unit price", itemsWithZeroPrice));
        }

        if (itemsWithNegativeQuantity > 0) {
            errors.add(String.format("%d sale items have negative or zero quantity", itemsWithNegativeQuantity));
        }

        if (salesWithItems < totalSales) {
            warnings.add(String.format("%d sales have no items", totalSales - salesWithItems));
        }

        // Product coverage warnings
        if (productCoveragePercentage.compareTo(BigDecimal.valueOf(20)) < 0 && activeProductsInDatabase > 5) {
            warnings.add(String.format("Low product coverage: only %d out of %d active products had sales (%.1f%%)",
                    productsWithSales, activeProductsInDatabase, productCoveragePercentage.doubleValue()));
        }

        if (productsWithSales < 5 && activeProductsInDatabase > 10) {
            warnings.add(String.format("Very few products with sales activity: only %d products generated revenue", productsWithSales));
        }

        long inactiveProducts = totalProductsInDatabase - activeProductsInDatabase;
        if (inactiveProducts > 0) {
            warnings.add(String.format("%d products are inactive and excluded from performance analysis", inactiveProducts));
        }

        validationCounts.put("totalSales", totalSales);
        validationCounts.put("salesWithItems", salesWithItems);
        validationCounts.put("itemsWithoutCost", itemsWithoutCost);
        validationCounts.put("itemsWithZeroPrice", itemsWithZeroPrice);
        validationCounts.put("itemsWithNegativeQuantity", itemsWithNegativeQuantity);
        validationCounts.put("totalProductsInDatabase", (int) totalProductsInDatabase);
        validationCounts.put("activeProducts", (int) activeProductsInDatabase);
        validationCounts.put("productsWithSales", (int) productsWithSales);

        return Map.of(
                "validationStatus", errors.isEmpty() ? "PASSED" : "FAILED",
                "warnings", warnings,
                "errors", errors,
                "validationCounts", validationCounts,
                "productCoverage", Map.of(
                        "coveragePercentage", productCoveragePercentage,
                        "productsWithSales", productsWithSales,
                        "activeProducts", activeProductsInDatabase,
                        "totalProducts", totalProductsInDatabase
                ),
                "dataQualityScore", calculateDataQualityScore(validationCounts)
        );
    }

    /**
     * Calculate data quality score based on validation results
     */
    private BigDecimal calculateDataQualityScore(Map<String, Integer> counts) {
        int totalSales = counts.get("totalSales");
        if (totalSales == 0) return BigDecimal.ZERO;

        int issues = counts.get("itemsWithoutCost") +
                    counts.get("itemsWithZeroPrice") +
                    counts.get("itemsWithNegativeQuantity");

        int salesWithoutItems = totalSales - counts.get("salesWithItems");
        issues += salesWithoutItems;

        // Calculate score as percentage (100% - issue percentage)
        double issuePercentage = (double) issues / totalSales * 100;
        double score = Math.max(0, 100 - issuePercentage);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    // Utility methods
    private BigDecimal calculatePercentile(List<BigDecimal> values, int percentile) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> sortedValues = values.stream()
                .sorted()
                .collect(Collectors.toList());

        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index);
    }

    // Stub implementations for trend analysis methods
    private Map<String, Object> generateDailyTrends(List<Sale> sales) { return new HashMap<>(); }
    private Map<String, Object> generateWeeklyTrends(List<Sale> sales) { return new HashMap<>(); }
    private Map<String, Object> generateMonthlyTrends(List<Sale> sales) { return new HashMap<>(); }
    private Map<String, Object> generateSalesForecast(List<Sale> sales, String groupBy) { return new HashMap<>(); }
    private Map<String, Object> analyzeSeasonality(List<Sale> sales) { return new HashMap<>(); }
    private Map<String, Object> calculateGrowthMetrics(List<Sale> sales) { return new HashMap<>(); }

    private Map<String, Object> generateCohortAnalysis(int months) { return new HashMap<>(); }
    private Map<String, Object> calculateRetentionRates(int months) { return new HashMap<>(); }
    private Map<String, Object> analyzeRepeatPurchases(int months) { return new HashMap<>(); }
    private Map<String, Object> analyzeCustomerLifecycle(int months) { return new HashMap<>(); }

    // Product and inventory helper methods

    /**
     * Generate accurate product rankings based on sales performance metrics
     */
    private Map<String, Object> generateProductRankings(List<Sale> sales) {
        log.debug("Generating product rankings for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "topProductsByRevenue", List.of(),
                "topProductsByQuantity", List.of(),
                "topProductsByProfit", List.of(),
                "topProductsByMargin", List.of(),
                "summary", Map.of(),
                "message", "No sales data available for product rankings"
            );
        }

        // Extract all sale items from sales
        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "topProductsByRevenue", List.of(),
                "topProductsByQuantity", List.of(),
                "topProductsByProfit", List.of(),
                "topProductsByMargin", List.of(),
                "summary", Map.of(),
                "message", "No sale items found"
            );
        }

        // Group by product and calculate accurate metrics
        Map<Product, List<SaleItem>> itemsByProduct = allItems.stream()
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        List<Map<String, Object>> productMetrics = itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<SaleItem> items = entry.getValue();

                    int totalQuantitySold = items.stream()
                            .mapToInt(SaleItem::getQuantity)
                            .sum();

                    // Use totalPrice for accurate revenue (includes discounts, taxes)
                    BigDecimal totalRevenue = items.stream()
                            .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Calculate total cost accurately
                    BigDecimal totalCost = items.stream()
                            .map(item -> {
                                BigDecimal cost = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                                return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalProfit = totalRevenue.subtract(totalCost);

                    // Calculate weighted average unit price (total revenue / total quantity)
                    BigDecimal avgUnitPrice = totalQuantitySold > 0 ?
                            totalRevenue.divide(BigDecimal.valueOf(totalQuantitySold), 2, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO;

                    // Calculate accurate profit margin
                    BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;

                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("productId", product.getId());
                    metrics.put("productName", product.getName());
                    metrics.put("sku", product.getSku());
                    metrics.put("category", product.getCategory() != null ? product.getCategory().getName() : "Uncategorized");
                    metrics.put("brand", product.getBrand());
                    metrics.put("totalQuantitySold", totalQuantitySold);
                    metrics.put("totalRevenue", totalRevenue);
                    metrics.put("totalCost", totalCost);
                    metrics.put("totalProfit", totalProfit);
                    metrics.put("avgUnitPrice", avgUnitPrice);
                    metrics.put("profitMargin", profitMargin);
                    metrics.put("salesCount", items.size());
                    metrics.put("currentStock", product.getStockQuantity());
                    metrics.put("stockTurnover", product.getStockQuantity() > 0 ?
                        BigDecimal.valueOf(totalQuantitySold).divide(BigDecimal.valueOf(product.getStockQuantity()), 2, RoundingMode.HALF_UP) :
                        BigDecimal.ZERO);
                    return metrics;
                })
                .collect(Collectors.toList());

        // Calculate total revenue for percentage calculations
        BigDecimal totalRevenueAll = productMetrics.stream()
                .map(m -> (BigDecimal) m.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add revenue percentage to each product
        productMetrics.forEach(metrics -> {
            BigDecimal revenue = (BigDecimal) metrics.get("totalRevenue");
            BigDecimal percentage = totalRevenueAll.compareTo(BigDecimal.ZERO) > 0 ?
                    revenue.divide(totalRevenueAll, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;
            metrics.put("revenuePercentage", percentage);
        });

        // Sort by different metrics and get top 10
        List<Map<String, Object>> topByRevenue = productMetrics.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> topByQuantity = productMetrics.stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalQuantitySold"), (Integer) a.get("totalQuantitySold")))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> topByProfit = productMetrics.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalProfit")).compareTo((BigDecimal) a.get("totalProfit")))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> topByMargin = productMetrics.stream()
                .filter(m -> ((BigDecimal) m.get("totalRevenue")).compareTo(BigDecimal.ZERO) > 0) // Only products with revenue
                .sorted((a, b) -> ((BigDecimal) b.get("profitMargin")).compareTo((BigDecimal) a.get("profitMargin")))
                .limit(10)
                .collect(Collectors.toList());

        // Calculate comprehensive summary statistics
        int totalQuantityAll = productMetrics.stream()
                .mapToInt(m -> (Integer) m.get("totalQuantitySold"))
                .sum();

        BigDecimal totalProfitAll = productMetrics.stream()
                .map(m -> (BigDecimal) m.get("totalProfit"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostAll = productMetrics.stream()
                .map(m -> (BigDecimal) m.get("totalCost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenuePerProduct = productMetrics.isEmpty() ? BigDecimal.ZERO :
                totalRevenueAll.divide(BigDecimal.valueOf(productMetrics.size()), 2, RoundingMode.HALF_UP);

        BigDecimal overallProfitMargin = totalRevenueAll.compareTo(BigDecimal.ZERO) > 0 ?
                totalProfitAll.divide(totalRevenueAll, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        // Get database product counts for context
        long totalProductsInDatabase = productRepository.count();
        long activeProductsInDatabase = productRepository.findAll().stream()
                .filter(product -> product.getProductStatus() == Product.ProductStatus.ACTIVE)
                .count();

        // Create summary map separately to avoid Map.of() parameter limit
        Map<String, Object> summary = new HashMap<>();
        summary.put("productsWithSales", productMetrics.size());
        summary.put("totalProductsInDatabase", totalProductsInDatabase);
        summary.put("activeProductsInDatabase", activeProductsInDatabase);
        summary.put("totalRevenue", totalRevenueAll);
        summary.put("totalQuantitySold", totalQuantityAll);
        summary.put("totalProfit", totalProfitAll);
        summary.put("totalCost", totalCostAll);
        summary.put("avgRevenuePerProduct", avgRevenuePerProduct);
        summary.put("overallProfitMargin", overallProfitMargin);
        summary.put("avgQuantityPerProduct", productMetrics.isEmpty() ? 0 :
                totalQuantityAll / productMetrics.size());
        summary.put("productCoveragePercentage", activeProductsInDatabase > 0 ?
                BigDecimal.valueOf(productMetrics.size())
                        .divide(BigDecimal.valueOf(activeProductsInDatabase), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO);

        return Map.of(
                "topProductsByRevenue", topByRevenue,
                "topProductsByQuantity", topByQuantity,
                "topProductsByProfit", topByProfit,
                "topProductsByMargin", topByMargin,
                "summary", summary,
                "allProductMetrics", productMetrics // Include all products for detailed analysis
        );
    }

    /**
     * Generate profitability analysis for products
     */
    private Map<String, Object> generateProfitabilityAnalysis(List<Sale> sales) {
        log.debug("Generating profitability analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "profitabilityMetrics", Map.of(),
                "profitMarginDistribution", Map.of(),
                "costAnalysis", Map.of(),
                "message", "No sales data available for profitability analysis"
            );
        }

        // Extract all sale items
        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null && item.getCostPrice() != null)
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "profitabilityMetrics", Map.of(),
                "profitMarginDistribution", Map.of(),
                "costAnalysis", Map.of(),
                "message", "No sale items with cost data found"
            );
        }

        // Calculate overall profitability metrics using accurate calculations
        BigDecimal totalRevenue = allItems.stream()
                .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = allItems.stream()
                .map(item -> {
                    BigDecimal cost = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                    return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        BigDecimal overallProfitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        // Count items with missing cost data for validation
        long itemsWithoutCost = allItems.stream()
                .filter(item -> item.getCostPrice() == null || item.getCostPrice().equals(BigDecimal.ZERO))
                .count();

        // Analyze profit margin distribution with accurate calculations
        Map<String, Long> marginDistribution = allItems.stream()
                .map(item -> {
                    BigDecimal itemRevenue = item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO;
                    if (itemRevenue.compareTo(BigDecimal.ZERO) <= 0) return "No Revenue";

                    // Calculate profit accurately
                    BigDecimal itemCost = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal totalItemCost = itemCost.multiply(BigDecimal.valueOf(item.getQuantity()));
                    BigDecimal itemProfit = itemRevenue.subtract(totalItemCost);

                    if (itemCost.equals(BigDecimal.ZERO)) return "No Cost Data";

                    BigDecimal margin = itemProfit.divide(itemRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    if (margin.compareTo(BigDecimal.valueOf(50)) >= 0) return "High Margin (50%+)";
                    if (margin.compareTo(BigDecimal.valueOf(30)) >= 0) return "Good Margin (30-49%)";
                    if (margin.compareTo(BigDecimal.valueOf(15)) >= 0) return "Average Margin (15-29%)";
                    if (margin.compareTo(BigDecimal.valueOf(5)) >= 0) return "Low Margin (5-14%)";
                    if (margin.compareTo(BigDecimal.ZERO) >= 0) return "Minimal Margin (0-4%)";
                    return "Loss Making";
                })
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()));

        // Analyze by product categories
        Map<String, Map<String, Object>> categoryProfitability = allItems.stream()
                .filter(item -> item.getProduct().getCategory() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getCategory().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> {
                                    BigDecimal catRevenue = items.stream()
                                            .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    BigDecimal catCost = items.stream()
                                            .map(item -> {
                                                BigDecimal cost = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                                                return cost.multiply(BigDecimal.valueOf(item.getQuantity()));
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    BigDecimal catProfit = catRevenue.subtract(catCost);
                                    BigDecimal catMargin = catRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                                            catProfit.divide(catRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                                            BigDecimal.ZERO;

                                    return Map.of(
                                            "revenue", catRevenue,
                                            "cost", catCost,
                                            "profit", catProfit,
                                            "profitMargin", catMargin,
                                            "itemCount", items.size()
                                    );
                                }
                        )
                ));

        // Find most and least profitable products
        List<Map<String, Object>> productProfitability = allItems.stream()
                .collect(Collectors.groupingBy(SaleItem::getProduct))
                .entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<SaleItem> items = entry.getValue();

                    BigDecimal prodRevenue = items.stream()
                            .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal prodProfit = items.stream()
                            .map(item -> item.getProfit() != null ? item.getProfit() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal prodMargin = prodRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            prodProfit.divide(prodRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;

                    Map<String, Object> prodData = new HashMap<>();
                    prodData.put("productId", product.getId());
                    prodData.put("productName", product.getName());
                    prodData.put("sku", product.getSku());
                    prodData.put("revenue", prodRevenue);
                    prodData.put("profit", prodProfit);
                    prodData.put("profitMargin", prodMargin);
                    return prodData;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> mostProfitable = productProfitability.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("profitMargin")).compareTo((BigDecimal) a.get("profitMargin")))
                .limit(10)
                .collect(Collectors.toList());

        List<Map<String, Object>> leastProfitable = productProfitability.stream()
                .sorted((a, b) -> ((BigDecimal) a.get("profitMargin")).compareTo((BigDecimal) b.get("profitMargin")))
                .limit(10)
                .collect(Collectors.toList());

        return Map.of(
                "profitabilityMetrics", Map.of(
                        "totalRevenue", totalRevenue,
                        "totalCost", totalCost,
                        "totalProfit", totalProfit,
                        "overallProfitMargin", overallProfitMargin,
                        "avgProfitPerItem", allItems.isEmpty() ? BigDecimal.ZERO :
                                totalProfit.divide(BigDecimal.valueOf(allItems.size()), 2, RoundingMode.HALF_UP),
                        "totalItems", allItems.size(),
                        "itemsWithoutCostData", itemsWithoutCost
                ),
                "profitMarginDistribution", marginDistribution,
                "categoryProfitability", categoryProfitability,
                "mostProfitableProducts", mostProfitable,
                "leastProfitableProducts", leastProfitable,
                "costAnalysis", Map.of(
                        "avgCostPerItem", allItems.isEmpty() ? BigDecimal.ZERO :
                                totalCost.divide(BigDecimal.valueOf(allItems.size()), 2, RoundingMode.HALF_UP),
                        "costToRevenueRatio", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                                totalCost.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                                BigDecimal.ZERO,
                        "dataQuality", Map.of(
                                "itemsWithCostData", allItems.size() - itemsWithoutCost,
                                "itemsWithoutCostData", itemsWithoutCost,
                                "costDataCompleteness", allItems.isEmpty() ? BigDecimal.ZERO :
                                        BigDecimal.valueOf(allItems.size() - itemsWithoutCost)
                                                .divide(BigDecimal.valueOf(allItems.size()), 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                        )
                )
        );
    }

    /**
     * Generate category performance analysis
     */
    private Map<String, Object> generateCategoryPerformance(List<Sale> sales) {
        log.debug("Generating category performance analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "categoryMetrics", List.of(),
                "categoryComparison", Map.of(),
                "topCategories", List.of(),
                "message", "No sales data available for category analysis"
            );
        }

        // Extract all sale items
        List<SaleItem> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null && item.getProduct().getCategory() != null)
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "categoryMetrics", List.of(),
                "categoryComparison", Map.of(),
                "topCategories", List.of(),
                "message", "No sale items with category data found"
            );
        }

        // Group by category and calculate metrics
        Map<Category, List<SaleItem>> itemsByCategory = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getCategory()));

        List<Map<String, Object>> categoryMetrics = itemsByCategory.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();
                    List<SaleItem> items = entry.getValue();

                    // Calculate basic metrics
                    int totalQuantitySold = items.stream()
                            .mapToInt(SaleItem::getQuantity)
                            .sum();

                    BigDecimal totalRevenue = items.stream()
                            .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalProfit = items.stream()
                            .map(item -> item.getProfit() != null ? item.getProfit() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgUnitPrice = items.stream()
                            .map(item -> item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(items.size()), 2, RoundingMode.HALF_UP);

                    BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;

                    // Count unique products in this category
                    long uniqueProducts = items.stream()
                            .map(item -> item.getProduct().getId())
                            .distinct()
                            .count();

                    // Count unique customers who bought from this category
                    long uniqueCustomers = items.stream()
                            .map(item -> item.getSale().getCustomer().getId())
                            .distinct()
                            .count();

                    // Calculate average order value for this category
                    BigDecimal avgOrderValue = items.stream()
                            .collect(Collectors.groupingBy(item -> item.getSale().getId()))
                            .values().stream()
                            .map(saleItems -> saleItems.stream()
                                    .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(items.stream()
                                    .map(item -> item.getSale().getId())
                                    .distinct()
                                    .count()), 2, RoundingMode.HALF_UP);

                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("categoryId", category.getId());
                    metrics.put("categoryName", category.getName());
                    metrics.put("totalQuantitySold", totalQuantitySold);
                    metrics.put("totalRevenue", totalRevenue);
                    metrics.put("totalProfit", totalProfit);
                    metrics.put("profitMargin", profitMargin);
                    metrics.put("avgUnitPrice", avgUnitPrice);
                    metrics.put("avgOrderValue", avgOrderValue);
                    metrics.put("uniqueProducts", uniqueProducts);
                    metrics.put("uniqueCustomers", uniqueCustomers);
                    metrics.put("salesCount", items.size());
                    return metrics;
                })
                .collect(Collectors.toList());

        // Sort categories by different metrics
        List<Map<String, Object>> topByRevenue = categoryMetrics.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
                .collect(Collectors.toList());

        List<Map<String, Object>> topByQuantity = categoryMetrics.stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalQuantitySold"), (Integer) a.get("totalQuantitySold")))
                .collect(Collectors.toList());

        List<Map<String, Object>> topByProfitMargin = categoryMetrics.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("profitMargin")).compareTo((BigDecimal) a.get("profitMargin")))
                .collect(Collectors.toList());

        // Calculate overall totals for comparison
        BigDecimal totalRevenueAll = categoryMetrics.stream()
                .map(m -> (BigDecimal) m.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantityAll = categoryMetrics.stream()
                .mapToInt(m -> (Integer) m.get("totalQuantitySold"))
                .sum();

        // Add percentage contribution to each category
        categoryMetrics.forEach(metrics -> {
            BigDecimal categoryRevenue = (BigDecimal) metrics.get("totalRevenue");
            Integer categoryQuantity = (Integer) metrics.get("totalQuantitySold");

            BigDecimal revenuePercentage = totalRevenueAll.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryRevenue.divide(totalRevenueAll, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;

            BigDecimal quantityPercentage = totalQuantityAll > 0 ?
                    BigDecimal.valueOf(categoryQuantity).divide(BigDecimal.valueOf(totalQuantityAll), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;

            metrics.put("revenuePercentage", revenuePercentage);
            metrics.put("quantityPercentage", quantityPercentage);
        });

        return Map.of(
                "categoryMetrics", categoryMetrics,
                "topCategoriesByRevenue", topByRevenue,
                "topCategoriesByQuantity", topByQuantity,
                "topCategoriesByProfitMargin", topByProfitMargin,
                "categoryComparison", Map.of(
                        "totalCategories", categoryMetrics.size(),
                        "totalRevenue", totalRevenueAll,
                        "totalQuantitySold", totalQuantityAll,
                        "avgRevenuePerCategory", categoryMetrics.isEmpty() ? BigDecimal.ZERO :
                                totalRevenueAll.divide(BigDecimal.valueOf(categoryMetrics.size()), 2, RoundingMode.HALF_UP)
                )
        );
    }

    /**
     * Generate product trends analysis over the specified time period
     */
    private Map<String, Object> generateProductTrends(List<Sale> sales) {
        log.debug("Generating product trends analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "dailyTrends", Map.of(),
                "weeklyTrends", Map.of(),
                "trendingProducts", List.of(),
                "message", "No sales data available for trend analysis"
            );
        }

        // Extract all sale items with dates
        List<Map<String, Object>> allItems = sales.stream()
                .filter(sale -> sale.getItems() != null && sale.getSaleDate() != null)
                .flatMap(sale -> sale.getItems().stream()
                        .filter(item -> item.getProduct() != null)
                        .map(item -> {
                            // Create a wrapper to include sale date with item
                            Map<String, Object> itemWithDate = new HashMap<>();
                            itemWithDate.put("item", item);
                            itemWithDate.put("saleDate", sale.getSaleDate());
                            return itemWithDate;
                        }))
                .collect(Collectors.toList());

        if (allItems.isEmpty()) {
            return Map.of(
                "dailyTrends", Map.of(),
                "weeklyTrends", Map.of(),
                "trendingProducts", List.of(),
                "message", "No sale items found for trend analysis"
            );
        }

        // Daily trends analysis
        Map<String, Map<String, Object>> dailyTrends = allItems.stream()
                .collect(Collectors.groupingBy(
                        itemData -> ((LocalDateTime) itemData.get("saleDate")).toLocalDate().format(DATE_FORMATTER),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                dayItems -> {
                                    int totalQuantity = dayItems.stream()
                                            .mapToInt(itemData -> ((SaleItem) itemData.get("item")).getQuantity())
                                            .sum();

                                    BigDecimal totalRevenue = dayItems.stream()
                                            .map(itemData -> {
                                                SaleItem item = (SaleItem) itemData.get("item");
                                                return item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO;
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    long uniqueProducts = dayItems.stream()
                                            .map(itemData -> ((SaleItem) itemData.get("item")).getProduct().getId())
                                            .distinct()
                                            .count();

                                    return Map.of(
                                            "totalQuantity", totalQuantity,
                                            "totalRevenue", totalRevenue,
                                            "uniqueProducts", uniqueProducts,
                                            "salesCount", dayItems.size()
                                    );
                                }
                        )
                ));

        // Weekly trends analysis
        Map<String, Map<String, Object>> weeklyTrends = allItems.stream()
                .collect(Collectors.groupingBy(
                        itemData -> {
                            LocalDateTime saleDate = (LocalDateTime) itemData.get("saleDate");
                            // Get week of year
                            return saleDate.getYear() + "-W" + String.format("%02d", saleDate.get(java.time.temporal.WeekFields.ISO.weekOfYear()));
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                weekItems -> {
                                    int totalQuantity = weekItems.stream()
                                            .mapToInt(itemData -> ((SaleItem) itemData.get("item")).getQuantity())
                                            .sum();

                                    BigDecimal totalRevenue = weekItems.stream()
                                            .map(itemData -> {
                                                SaleItem item = (SaleItem) itemData.get("item");
                                                return item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO;
                                            })
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    long uniqueProducts = weekItems.stream()
                                            .map(itemData -> ((SaleItem) itemData.get("item")).getProduct().getId())
                                            .distinct()
                                            .count();

                                    return Map.of(
                                            "totalQuantity", totalQuantity,
                                            "totalRevenue", totalRevenue,
                                            "uniqueProducts", uniqueProducts,
                                            "salesCount", weekItems.size()
                                    );
                                }
                        )
                ));

        // Identify trending products (products with increasing sales over time)
        Map<Product, List<Map<String, Object>>> productsByDate = allItems.stream()
                .collect(Collectors.groupingBy(
                        itemData -> ((SaleItem) itemData.get("item")).getProduct(),
                        Collectors.mapping(
                                itemData -> Map.of(
                                        "date", ((LocalDateTime) itemData.get("saleDate")).toLocalDate(),
                                        "quantity", ((SaleItem) itemData.get("item")).getQuantity(),
                                        "revenue", ((SaleItem) itemData.get("item")).getTotalPrice() != null ?
                                                ((SaleItem) itemData.get("item")).getTotalPrice() : BigDecimal.ZERO
                                ),
                                Collectors.toList()
                        )
                ));

        List<Map<String, Object>> trendingProducts = productsByDate.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 2) // Need at least 2 data points for trend
                .map(entry -> {
                    Product product = entry.getKey();
                    List<Map<String, Object>> productData = entry.getValue();

                    // Sort by date
                    productData.sort((a, b) -> ((LocalDate) a.get("date")).compareTo((LocalDate) b.get("date")));

                    // Calculate trend (simple: compare first half vs second half)
                    int midPoint = productData.size() / 2;

                    int firstHalfQuantity = productData.subList(0, midPoint).stream()
                            .mapToInt(data -> (Integer) data.get("quantity"))
                            .sum();

                    int secondHalfQuantity = productData.subList(midPoint, productData.size()).stream()
                            .mapToInt(data -> (Integer) data.get("quantity"))
                            .sum();

                    BigDecimal firstHalfRevenue = productData.subList(0, midPoint).stream()
                            .map(data -> (BigDecimal) data.get("revenue"))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal secondHalfRevenue = productData.subList(midPoint, productData.size()).stream()
                            .map(data -> (BigDecimal) data.get("revenue"))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Calculate growth rates
                    double quantityGrowth = firstHalfQuantity > 0 ?
                            ((double) (secondHalfQuantity - firstHalfQuantity) / firstHalfQuantity) * 100 : 0;

                    BigDecimal revenueGrowth = firstHalfRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            secondHalfRevenue.subtract(firstHalfRevenue)
                                    .divide(firstHalfRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;

                    String trendDirection = quantityGrowth > 10 ? "Increasing" :
                                          quantityGrowth < -10 ? "Decreasing" : "Stable";

                    Map<String, Object> trendData = new HashMap<>();
                    trendData.put("productId", product.getId());
                    trendData.put("productName", product.getName());
                    trendData.put("sku", product.getSku());
                    trendData.put("category", product.getCategory() != null ? product.getCategory().getName() : "Uncategorized");
                    trendData.put("quantityGrowth", BigDecimal.valueOf(quantityGrowth).setScale(2, RoundingMode.HALF_UP));
                    trendData.put("revenueGrowth", revenueGrowth);
                    trendData.put("trendDirection", trendDirection);
                    trendData.put("totalDataPoints", productData.size());
                    return trendData;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("quantityGrowth")).compareTo((BigDecimal) a.get("quantityGrowth")))
                .limit(20) // Top 20 trending products
                .collect(Collectors.toList());

        return Map.of(
                "dailyTrends", dailyTrends,
                "weeklyTrends", weeklyTrends,
                "trendingProducts", trendingProducts,
                "trendSummary", Map.of(
                        "totalDaysAnalyzed", dailyTrends.size(),
                        "totalWeeksAnalyzed", weeklyTrends.size(),
                        "productsWithTrends", trendingProducts.size(),
                        "avgDailyRevenue", dailyTrends.isEmpty() ? BigDecimal.ZERO :
                                dailyTrends.values().stream()
                                        .map(day -> (BigDecimal) day.get("totalRevenue"))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(dailyTrends.size()), 2, RoundingMode.HALF_UP)
                )
        );
    }

    /**
     * Generate cross-sell analysis to identify products frequently bought together
     */
    private Map<String, Object> generateCrossSellAnalysis(List<Sale> sales) {
        log.debug("Generating cross-sell analysis for {} sales", sales.size());

        if (sales.isEmpty()) {
            return Map.of(
                "productPairs", List.of(),
                "crossSellOpportunities", List.of(),
                "basketAnalysis", Map.of(),
                "message", "No sales data available for cross-sell analysis"
            );
        }

        // Filter sales with multiple items (potential for cross-selling)
        List<Sale> multiItemSales = sales.stream()
                .filter(sale -> sale.getItems() != null && sale.getItems().size() > 1)
                .collect(Collectors.toList());

        if (multiItemSales.isEmpty()) {
            return Map.of(
                "productPairs", List.of(),
                "crossSellOpportunities", List.of(),
                "basketAnalysis", Map.of(
                        "totalSales", sales.size(),
                        "multiItemSales", 0,
                        "crossSellRate", BigDecimal.ZERO
                ),
                "message", "No multi-item sales found for cross-sell analysis"
            );
        }

        // Analyze product pairs (products bought together)
        Map<String, Integer> productPairCounts = new HashMap<>();
        Map<String, BigDecimal> productPairRevenue = new HashMap<>();

        for (Sale sale : multiItemSales) {
            List<Product> products = sale.getItems().stream()
                    .map(SaleItem::getProduct)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // Generate all pairs of products in this sale
            for (int i = 0; i < products.size(); i++) {
                for (int j = i + 1; j < products.size(); j++) {
                    Product product1 = products.get(i);
                    Product product2 = products.get(j);

                    // Create a consistent key for the pair (alphabetical order)
                    String pairKey = product1.getName().compareTo(product2.getName()) <= 0 ?
                            product1.getName() + " + " + product2.getName() :
                            product2.getName() + " + " + product1.getName();

                    productPairCounts.merge(pairKey, 1, Integer::sum);

                    // Calculate revenue for this pair in this sale
                    BigDecimal pairRevenue = sale.getItems().stream()
                            .filter(item -> item.getProduct().equals(product1) || item.getProduct().equals(product2))
                            .map(item -> item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    productPairRevenue.merge(pairKey, pairRevenue, BigDecimal::add);
                }
            }
        }

        // Convert to list and sort by frequency
        List<Map<String, Object>> productPairs = productPairCounts.entrySet().stream()
                .map(entry -> {
                    String pairKey = entry.getKey();
                    Integer count = entry.getValue();
                    BigDecimal revenue = productPairRevenue.getOrDefault(pairKey, BigDecimal.ZERO);

                    // Calculate support (percentage of multi-item sales containing this pair)
                    BigDecimal support = BigDecimal.valueOf(count)
                            .divide(BigDecimal.valueOf(multiItemSales.size()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    Map<String, Object> pairData = new HashMap<>();
                    pairData.put("productPair", pairKey);
                    pairData.put("frequency", count);
                    pairData.put("totalRevenue", revenue);
                    pairData.put("support", support);
                    pairData.put("avgRevenuePerOccurrence", count > 0 ?
                            revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                    return pairData;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("frequency"), (Integer) a.get("frequency")))
                .limit(20) // Top 20 product pairs
                .collect(Collectors.toList());

        // Analyze individual product cross-sell potential
        Map<Product, Map<String, Object>> productCrossSellData = new HashMap<>();

        for (Sale sale : multiItemSales) {
            List<SaleItem> items = sale.getItems().stream()
                    .filter(item -> item.getProduct() != null)
                    .collect(Collectors.toList());

            for (SaleItem item : items) {
                Product product = item.getProduct();

                productCrossSellData.computeIfAbsent(product, p -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("productId", p.getId());
                    data.put("productName", p.getName());
                    data.put("sku", p.getSku());
                    data.put("category", p.getCategory() != null ? p.getCategory().getName() : "Uncategorized");
                    data.put("multiItemSalesCount", 0);
                    data.put("totalSalesCount", 0);
                    data.put("companionProducts", new HashSet<String>());
                    data.put("totalRevenue", BigDecimal.ZERO);
                    return data;
                });

                Map<String, Object> data = productCrossSellData.get(product);
                data.put("multiItemSalesCount", (Integer) data.get("multiItemSalesCount") + 1);
                data.put("totalRevenue", ((BigDecimal) data.get("totalRevenue")).add(
                        item.getTotalPrice() != null ? item.getTotalPrice() : BigDecimal.ZERO));

                // Add companion products
                @SuppressWarnings("unchecked")
                Set<String> companions = (Set<String>) data.get("companionProducts");
                items.stream()
                        .filter(otherItem -> !otherItem.getProduct().equals(product))
                        .forEach(otherItem -> companions.add(otherItem.getProduct().getName()));
            }
        }

        // Count total sales for each product
        for (Sale sale : sales) {
            if (sale.getItems() != null) {
                for (SaleItem item : sale.getItems()) {
                    if (item.getProduct() != null && productCrossSellData.containsKey(item.getProduct())) {
                        Map<String, Object> data = productCrossSellData.get(item.getProduct());
                        data.put("totalSalesCount", (Integer) data.get("totalSalesCount") + 1);
                    }
                }
            }
        }

        // Calculate cross-sell rates and create opportunities list
        List<Map<String, Object>> crossSellOpportunities = productCrossSellData.values().stream()
                .map(data -> {
                    Integer multiItemSalesCount = (Integer) data.get("multiItemSalesCount");
                    Integer totalSalesCount = (Integer) data.get("totalSalesCount");
                    @SuppressWarnings("unchecked")
                    Set<String> companions = (Set<String>) data.get("companionProducts");

                    BigDecimal crossSellRate = totalSalesCount > 0 ?
                            BigDecimal.valueOf(multiItemSalesCount)
                                    .divide(BigDecimal.valueOf(totalSalesCount), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)) :
                            BigDecimal.ZERO;

                    Map<String, Object> opportunity = new HashMap<>();
                    opportunity.put("productId", data.get("productId"));
                    opportunity.put("productName", data.get("productName"));
                    opportunity.put("sku", data.get("sku"));
                    opportunity.put("category", data.get("category"));
                    opportunity.put("crossSellRate", crossSellRate);
                    opportunity.put("multiItemSalesCount", multiItemSalesCount);
                    opportunity.put("totalSalesCount", totalSalesCount);
                    opportunity.put("companionProductsCount", companions.size());
                    opportunity.put("topCompanions", companions.stream().limit(5).collect(Collectors.toList()));
                    opportunity.put("totalRevenue", data.get("totalRevenue"));
                    return opportunity;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("crossSellRate")).compareTo((BigDecimal) a.get("crossSellRate")))
                .collect(Collectors.toList());

        // Calculate basket analysis metrics
        double avgItemsPerBasket = multiItemSales.stream()
                .mapToInt(sale -> sale.getItems().size())
                .average()
                .orElse(0.0);

        BigDecimal avgBasketValue = multiItemSales.stream()
                .map(Sale::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(multiItemSales.size()), 2, RoundingMode.HALF_UP);

        BigDecimal crossSellRate = sales.size() > 0 ?
                BigDecimal.valueOf(multiItemSales.size())
                        .divide(BigDecimal.valueOf(sales.size()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        return Map.of(
                "productPairs", productPairs,
                "crossSellOpportunities", crossSellOpportunities,
                "basketAnalysis", Map.of(
                        "totalSales", sales.size(),
                        "multiItemSales", multiItemSales.size(),
                        "crossSellRate", crossSellRate,
                        "avgItemsPerBasket", BigDecimal.valueOf(avgItemsPerBasket).setScale(2, RoundingMode.HALF_UP),
                        "avgBasketValue", avgBasketValue,
                        "topProductPairsCount", Math.min(productPairs.size(), 10)
                ),
                "insights", Map.of(
                        "mostFrequentPair", productPairs.isEmpty() ? "None" :
                                ((Map<String, Object>) productPairs.get(0)).get("productPair"),
                        "highestRevenuePair", productPairs.stream()
                                .max((a, b) -> ((BigDecimal) a.get("totalRevenue")).compareTo((BigDecimal) b.get("totalRevenue")))
                                .map(pair -> pair.get("productPair"))
                                .orElse("None"),
                        "bestCrossSellProduct", crossSellOpportunities.isEmpty() ? "None" :
                                ((Map<String, Object>) crossSellOpportunities.get(0)).get("productName")
                )
        );
    }
    private Map<String, Object> calculateTurnoverRates(List<Sale> sales, List<Long> categoryIds) { return new HashMap<>(); }
    private Map<String, Object> identifySlowMovingItems(List<Sale> sales, List<Long> categoryIds) { return new HashMap<>(); }
    private Map<String, Object> identifyFastMovingItems(List<Sale> sales, List<Long> categoryIds) { return new HashMap<>(); }
    private Map<String, Object> generateStockOptimizationRecommendations(List<Sale> sales, List<Long> categoryIds) { return new HashMap<>(); }

    // Inventory helper methods
    private Map<String, Object> categorizeStockLevels(List<Product> products) { return new HashMap<>(); }
    private Map<String, Object> generateLowStockAlerts(List<Product> products) { return new HashMap<>(); }
    private List<Map<String, Object>> getOutOfStockItems(List<Product> products) { return new ArrayList<>(); }
    private Map<String, Object> calculateInventoryValuation(List<Product> products) { return new HashMap<>(); }
    private Map<String, Object> analyzeWarehouseDistribution(List<Product> products, List<Long> warehouseIds) { return new HashMap<>(); }
    private Map<String, Object> calculateTotalValuation(List<Product> products, String method) { return new HashMap<>(); }
    private Map<String, Object> calculateCategoryValuation(List<Product> products, String method) { return new HashMap<>(); }
    private Map<String, Object> compareMarketValues(List<Product> products) { return new HashMap<>(); }

    // Promotion helper methods
    private Map<String, Object> calculatePromotionROI(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> generatePromotionUsageStats(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> analyzeCustomerResponse(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> calculateRevenueImpact(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> generateDailyPromotionUsage(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> getTopPerformingPromotions(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> analyzePromotionByCustomerSegment(List<AppliedPromotion> promotions) { return new HashMap<>(); }
    private Map<String, Object> calculatePromotionConversionRates(List<AppliedPromotion> promotions) { return new HashMap<>(); }

    // ==================== COMPREHENSIVE FINANCIAL ANALYSIS METHODS ====================

    /**
     * Generate comprehensive revenue analysis with trends and growth metrics
     */
    private Map<String, Object> generateComprehensiveRevenueAnalysis(List<Sale> sales, Object[] financialSummary, ReportRequestDTO request) {
        Map<String, Object> analysis = new HashMap<>();

        // Basic revenue metrics from summary - with safe casting
        BigDecimal totalRevenue = safeCastToBigDecimal(financialSummary, 0);
        BigDecimal totalCost = safeCastToBigDecimal(financialSummary, 1);
        BigDecimal totalTax = safeCastToBigDecimal(financialSummary, 2);
        BigDecimal totalDiscounts = safeCastToBigDecimal(financialSummary, 3);
        BigDecimal totalShipping = safeCastToBigDecimal(financialSummary, 4);
        Long totalTransactions = safeCastToLong(financialSummary, 5);
        Long uniqueCustomers = safeCastToLong(financialSummary, 6);

        BigDecimal grossProfit = totalRevenue.subtract(totalCost);
        BigDecimal netRevenue = totalRevenue.subtract(totalDiscounts);
        BigDecimal netProfit = grossProfit.subtract(totalTax).subtract(totalShipping);

        // Revenue summary
        Map<String, Object> revenueSummary = new HashMap<>();
        revenueSummary.put("totalRevenue", totalRevenue);
        revenueSummary.put("grossRevenue", totalRevenue.add(totalDiscounts)); // Revenue before discounts
        revenueSummary.put("netRevenue", netRevenue);
        revenueSummary.put("grossProfit", grossProfit);
        revenueSummary.put("netProfit", netProfit);
        revenueSummary.put("totalCost", totalCost);
        revenueSummary.put("totalTax", totalTax);
        revenueSummary.put("totalDiscounts", totalDiscounts);
        revenueSummary.put("totalShipping", totalShipping);
        revenueSummary.put("totalTransactions", totalTransactions);
        revenueSummary.put("uniqueCustomers", uniqueCustomers);
        revenueSummary.put("averageOrderValue", totalTransactions > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        revenueSummary.put("revenuePerCustomer", uniqueCustomers > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        analysis.put("summary", revenueSummary);

        // Revenue by category
        List<Object[]> categoryRevenue = saleRepository.getRevenueByCategoryForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> categoryAnalysis = categoryRevenue.stream()
            .map(row -> {
                Map<String, Object> category = new HashMap<>();
                category.put("categoryName", row[0]);
                category.put("salesCount", row[1]);
                category.put("totalQuantitySold", row[2]);
                category.put("totalRevenue", row[3]);
                category.put("totalCost", row[4]);
                category.put("avgUnitPrice", row[5]);
                BigDecimal revenue = (BigDecimal) row[3];
                BigDecimal cost = (BigDecimal) row[4];
                category.put("grossProfit", revenue.subtract(cost));
                category.put("profitMargin", revenue.compareTo(BigDecimal.ZERO) > 0 ?
                    revenue.subtract(cost).divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                category.put("revenuePercentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                return category;
            })
            .collect(Collectors.toList());
        analysis.put("revenueByCategory", categoryAnalysis);

        // Revenue trends (daily breakdown)
        List<Object[]> dailyRevenue = saleRepository.getDailyRevenueAnalysis(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> trendAnalysis = dailyRevenue.stream()
            .map(row -> {
                Map<String, Object> day = new HashMap<>();
                day.put("date", row[0].toString());
                day.put("salesCount", row[1]);
                day.put("revenue", row[2]);
                day.put("cost", row[3]);
                day.put("avgOrderValue", row[4]);
                BigDecimal dayRevenue = (BigDecimal) row[2];
                BigDecimal dayCost = (BigDecimal) row[3];
                day.put("profit", dayRevenue.subtract(dayCost));
                return day;
            })
            .collect(Collectors.toList());
        analysis.put("dailyTrends", trendAnalysis);

        // Growth calculations (compare with previous period)
        LocalDateTime previousStart = request.getStartDate().minusDays(
            ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()));
        LocalDateTime previousEnd = request.getStartDate().minusDays(1);

        Object[] previousSummary = saleRepository.getFinancialSummaryForPeriod(previousStart, previousEnd);
        BigDecimal previousRevenue = safeCastToBigDecimal(previousSummary, 0);

        Map<String, Object> growthMetrics = new HashMap<>();
        if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal revenueGrowth = totalRevenue.subtract(previousRevenue)
                .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            growthMetrics.put("revenueGrowthPercentage", revenueGrowth);
        } else {
            growthMetrics.put("revenueGrowthPercentage", BigDecimal.ZERO);
        }
        growthMetrics.put("previousPeriodRevenue", previousRevenue);
        growthMetrics.put("currentPeriodRevenue", totalRevenue);
        analysis.put("growthMetrics", growthMetrics);

        return analysis;
    }

    /**
     * Generate detailed profit margin analysis by product and category
     */
    private Map<String, Object> generateDetailedProfitMarginAnalysis(List<Sale> sales, ReportRequestDTO request) {
        Map<String, Object> analysis = new HashMap<>();

        // Overall profit margin metrics
        BigDecimal totalRevenue = sales.stream()
            .map(Sale::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCost = sales.stream()
            .map(Sale::getCostOfGoodsSold)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = totalRevenue.subtract(totalCost);
        BigDecimal grossMarginPercentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        Map<String, Object> overallMargins = new HashMap<>();
        overallMargins.put("totalRevenue", totalRevenue);
        overallMargins.put("totalCost", totalCost);
        overallMargins.put("grossProfit", grossProfit);
        overallMargins.put("grossMarginPercentage", grossMarginPercentage);

        // Calculate net margin (after taxes and discounts)
        BigDecimal totalTax = sales.stream()
            .map(Sale::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscounts = sales.stream()
            .map(Sale::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalShipping = sales.stream()
            .map(Sale::getShippingCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = grossProfit.subtract(totalTax).subtract(totalShipping);
        BigDecimal netMarginPercentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            netProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        overallMargins.put("totalTax", totalTax);
        overallMargins.put("totalDiscounts", totalDiscounts);
        overallMargins.put("totalShipping", totalShipping);
        overallMargins.put("netProfit", netProfit);
        overallMargins.put("netMarginPercentage", netMarginPercentage);

        analysis.put("overallMargins", overallMargins);

        // Product-level profit margin analysis
        List<Object[]> productMargins = saleRepository.getRevenueByProductForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> productAnalysis = productMargins.stream()
            .map(row -> {
                Map<String, Object> product = new HashMap<>();
                product.put("productName", row[0]);
                product.put("productSku", row[1]);
                product.put("categoryName", row[2]);
                product.put("totalQuantitySold", row[3]);
                BigDecimal productRevenue = (BigDecimal) row[4];
                BigDecimal productCost = (BigDecimal) row[5];
                product.put("totalRevenue", productRevenue);
                product.put("totalCost", productCost);
                product.put("avgUnitPrice", row[6]);
                product.put("salesCount", row[7]);

                BigDecimal productProfit = productRevenue.subtract(productCost);
                BigDecimal productMargin = productRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    productProfit.divide(productRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                product.put("grossProfit", productProfit);
                product.put("profitMarginPercentage", productMargin);
                product.put("profitPerUnit", row[3] != null && ((Number) row[3]).longValue() > 0 ?
                    productProfit.divide(BigDecimal.valueOf(((Number) row[3]).longValue()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                return product;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("profitMarginPercentage")).compareTo((BigDecimal) a.get("profitMarginPercentage")))
            .collect(Collectors.toList());

        analysis.put("productMargins", productAnalysis);

        // Top and bottom performers
        List<Map<String, Object>> topPerformers = productAnalysis.stream()
            .limit(10)
            .collect(Collectors.toList());

        List<Map<String, Object>> bottomPerformers = productAnalysis.stream()
            .skip(Math.max(0, productAnalysis.size() - 10))
            .collect(Collectors.toList());

        analysis.put("topPerformingProducts", topPerformers);
        analysis.put("bottomPerformingProducts", bottomPerformers);

        // Category-level margin analysis
        List<Object[]> categoryMargins = saleRepository.getRevenueByCategoryForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> categoryAnalysis = categoryMargins.stream()
            .map(row -> {
                Map<String, Object> category = new HashMap<>();
                category.put("categoryName", row[0]);
                BigDecimal categoryRevenue = (BigDecimal) row[3];
                BigDecimal categoryCost = (BigDecimal) row[4];
                BigDecimal categoryProfit = categoryRevenue.subtract(categoryCost);
                BigDecimal categoryMargin = categoryRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryProfit.divide(categoryRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                category.put("totalRevenue", categoryRevenue);
                category.put("totalCost", categoryCost);
                category.put("grossProfit", categoryProfit);
                category.put("profitMarginPercentage", categoryMargin);
                category.put("salesCount", row[1]);
                category.put("totalQuantitySold", row[2]);

                return category;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("profitMarginPercentage")).compareTo((BigDecimal) a.get("profitMarginPercentage")))
            .collect(Collectors.toList());

        analysis.put("categoryMargins", categoryAnalysis);

        // Margin variance analysis
        Map<String, Object> varianceAnalysis = new HashMap<>();
        if (!productAnalysis.isEmpty()) {
            BigDecimal avgMargin = productAnalysis.stream()
                .map(p -> (BigDecimal) p.get("profitMarginPercentage"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(productAnalysis.size()), 2, RoundingMode.HALF_UP);

            BigDecimal maxMargin = productAnalysis.stream()
                .map(p -> (BigDecimal) p.get("profitMarginPercentage"))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            BigDecimal minMargin = productAnalysis.stream()
                .map(p -> (BigDecimal) p.get("profitMarginPercentage"))
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

            varianceAnalysis.put("averageMargin", avgMargin);
            varianceAnalysis.put("maxMargin", maxMargin);
            varianceAnalysis.put("minMargin", minMargin);
            varianceAnalysis.put("marginRange", maxMargin.subtract(minMargin));
        }

        analysis.put("marginVariance", varianceAnalysis);

        return analysis;
    }

    /**
     * Generate payment method revenue analysis
     */
    private Map<String, Object> generatePaymentMethodRevenueAnalysis(ReportRequestDTO request) {
        Map<String, Object> analysis = new HashMap<>();

        // Get payment method data from repository
        List<Object[]> paymentMethodData = saleRepository.getRevenueByPaymentMethod(request.getStartDate(), request.getEndDate());

        BigDecimal totalRevenue = paymentMethodData.stream()
            .map(row -> safeCastToBigDecimal(row, 2))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalTransactions = paymentMethodData.stream()
            .map(row -> safeCastToLong(row, 1))
            .reduce(0L, Long::sum);

        // Payment method breakdown
        List<Map<String, Object>> paymentMethodBreakdown = paymentMethodData.stream()
            .map(row -> {
                Map<String, Object> method = new HashMap<>();
                method.put("paymentMethod", row[0] != null ? row[0].toString() : "Unknown");
                Long transactionCount = safeCastToLong(row, 1);
                BigDecimal methodRevenue = safeCastToBigDecimal(row, 2);
                BigDecimal avgTransactionValue = safeCastToBigDecimal(row, 3);

                method.put("transactionCount", transactionCount);
                method.put("totalRevenue", methodRevenue);
                method.put("avgTransactionValue", avgTransactionValue);

                // Calculate percentages
                method.put("revenuePercentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    methodRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                method.put("transactionPercentage", totalTransactions > 0 ?
                    BigDecimal.valueOf(transactionCount).divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

                return method;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
            .collect(Collectors.toList());

        analysis.put("paymentMethodBreakdown", paymentMethodBreakdown);

        // Summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalTransactions", totalTransactions);
        summary.put("overallAvgTransactionValue", totalTransactions > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        summary.put("uniquePaymentMethods", paymentMethodBreakdown.size());

        analysis.put("summary", summary);

        // Payment method preferences analysis
        Map<String, Object> preferences = new HashMap<>();
        if (!paymentMethodBreakdown.isEmpty()) {
            Map<String, Object> mostPopular = paymentMethodBreakdown.stream()
                .max((a, b) -> ((Long) a.get("transactionCount")).compareTo((Long) b.get("transactionCount")))
                .orElse(new HashMap<>());

            Map<String, Object> highestRevenue = paymentMethodBreakdown.get(0); // Already sorted by revenue

            Map<String, Object> highestAvgValue = paymentMethodBreakdown.stream()
                .max((a, b) -> ((BigDecimal) a.get("avgTransactionValue")).compareTo((BigDecimal) b.get("avgTransactionValue")))
                .orElse(new HashMap<>());

            preferences.put("mostPopularByTransactions", mostPopular);
            preferences.put("highestRevenueMethod", highestRevenue);
            preferences.put("highestAvgTransactionValue", highestAvgValue);
        }

        analysis.put("preferences", preferences);

        // Trends analysis (if we have daily data)
        Map<String, Object> trends = new HashMap<>();
        // Calculate growth trends for each payment method compared to previous period
        LocalDateTime previousStart = request.getStartDate().minusDays(
            ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()));
        LocalDateTime previousEnd = request.getStartDate().minusDays(1);

        List<Object[]> previousPaymentData = saleRepository.getRevenueByPaymentMethod(previousStart, previousEnd);
        Map<String, BigDecimal> previousRevenueMap = previousPaymentData.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> safeCastToBigDecimal(row, 2),
                (existing, replacement) -> existing
            ));

        List<Map<String, Object>> growthAnalysis = paymentMethodBreakdown.stream()
            .map(method -> {
                Map<String, Object> growth = new HashMap<>();
                String paymentMethod = (String) method.get("paymentMethod");
                BigDecimal currentRevenue = (BigDecimal) method.get("totalRevenue");
                BigDecimal previousRevenue = previousRevenueMap.getOrDefault(paymentMethod, BigDecimal.ZERO);

                growth.put("paymentMethod", paymentMethod);
                growth.put("currentRevenue", currentRevenue);
                growth.put("previousRevenue", previousRevenue);

                if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal growthPercentage = currentRevenue.subtract(previousRevenue)
                        .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    growth.put("growthPercentage", growthPercentage);
                } else {
                    growth.put("growthPercentage", currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO);
                }

                return growth;
            })
            .collect(Collectors.toList());

        trends.put("paymentMethodGrowth", growthAnalysis);
        analysis.put("trends", trends);

        return analysis;
    }

    /**
     * Generate comprehensive tax analysis
     */
    private Map<String, Object> generateComprehensiveTaxAnalysis(List<Sale> sales, ReportRequestDTO request) {
        Map<String, Object> analysis = new HashMap<>();

        // Overall tax summary
        BigDecimal totalTaxCollected = sales.stream()
            .map(Sale::getTaxAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTaxableRevenue = sales.stream()
            .map(sale -> sale.getTotalAmount().subtract(sale.getTaxAmount()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = sales.stream()
            .map(Sale::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> taxSummary = new HashMap<>();
        taxSummary.put("totalTaxCollected", totalTaxCollected);
        taxSummary.put("totalTaxableRevenue", totalTaxableRevenue);
        taxSummary.put("totalRevenue", totalRevenue);
        taxSummary.put("effectiveTaxRate", totalTaxableRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalTaxCollected.divide(totalTaxableRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        taxSummary.put("taxAsPercentageOfRevenue", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalTaxCollected.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

        analysis.put("taxSummary", taxSummary);

        // Tax rate analysis
        Map<BigDecimal, List<Sale>> salesByTaxRate = sales.stream()
            .filter(sale -> sale.getTaxPercentage() != null)
            .collect(Collectors.groupingBy(Sale::getTaxPercentage));

        List<Map<String, Object>> taxRateBreakdown = salesByTaxRate.entrySet().stream()
            .map(entry -> {
                BigDecimal taxRate = entry.getKey();
                List<Sale> rateSales = entry.getValue();

                BigDecimal rateRevenue = rateSales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal rateTaxCollected = rateSales.stream()
                    .map(Sale::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> rateAnalysis = new HashMap<>();
                rateAnalysis.put("taxRate", taxRate);
                rateAnalysis.put("transactionCount", rateSales.size());
                rateAnalysis.put("totalRevenue", rateRevenue);
                rateAnalysis.put("taxCollected", rateTaxCollected);
                rateAnalysis.put("revenuePercentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    rateRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                rateAnalysis.put("taxPercentage", totalTaxCollected.compareTo(BigDecimal.ZERO) > 0 ?
                    rateTaxCollected.divide(totalTaxCollected, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

                return rateAnalysis;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
            .collect(Collectors.toList());

        analysis.put("taxRateBreakdown", taxRateBreakdown);

        // Tax by product category
        Map<String, List<Sale>> salesByCategory = sales.stream()
            .filter(sale -> sale.getItems() != null && !sale.getItems().isEmpty())
            .collect(Collectors.groupingBy(sale ->
                sale.getItems().get(0).getProduct().getCategory().getName()));

        List<Map<String, Object>> categoryTaxAnalysis = salesByCategory.entrySet().stream()
            .map(entry -> {
                String categoryName = entry.getKey();
                List<Sale> categorySales = entry.getValue();

                BigDecimal categoryRevenue = categorySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal categoryTax = categorySales.stream()
                    .map(Sale::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> categoryAnalysis = new HashMap<>();
                categoryAnalysis.put("categoryName", categoryName);
                categoryAnalysis.put("totalRevenue", categoryRevenue);
                categoryAnalysis.put("taxCollected", categoryTax);
                categoryAnalysis.put("effectiveTaxRate", categoryRevenue.subtract(categoryTax).compareTo(BigDecimal.ZERO) > 0 ?
                    categoryTax.divide(categoryRevenue.subtract(categoryTax), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                categoryAnalysis.put("transactionCount", categorySales.size());

                return categoryAnalysis;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("taxCollected")).compareTo((BigDecimal) a.get("taxCollected")))
            .collect(Collectors.toList());

        analysis.put("taxByCategory", categoryTaxAnalysis);

        // Daily tax collection trends
        Map<String, List<Sale>> salesByDate = sales.stream()
            .collect(Collectors.groupingBy(sale ->
                sale.getSaleDate().toLocalDate().toString()));

        List<Map<String, Object>> dailyTaxTrends = salesByDate.entrySet().stream()
            .map(entry -> {
                String date = entry.getKey();
                List<Sale> dailySales = entry.getValue();

                BigDecimal dailyRevenue = dailySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal dailyTax = dailySales.stream()
                    .map(Sale::getTaxAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> dayAnalysis = new HashMap<>();
                dayAnalysis.put("date", date);
                dayAnalysis.put("revenue", dailyRevenue);
                dayAnalysis.put("taxCollected", dailyTax);
                dayAnalysis.put("transactionCount", dailySales.size());
                dayAnalysis.put("avgTaxPerTransaction", dailySales.size() > 0 ?
                    dailyTax.divide(BigDecimal.valueOf(dailySales.size()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                return dayAnalysis;
            })
            .sorted((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")))
            .collect(Collectors.toList());

        analysis.put("dailyTaxTrends", dailyTaxTrends);

        // Tax compliance metrics
        Map<String, Object> complianceMetrics = new HashMap<>();
        long taxableTransactions = sales.stream()
            .filter(sale -> sale.getTaxAmount() != null && sale.getTaxAmount().compareTo(BigDecimal.ZERO) > 0)
            .count();

        long totalTransactions = sales.size();

        complianceMetrics.put("taxableTransactions", taxableTransactions);
        complianceMetrics.put("totalTransactions", totalTransactions);
        complianceMetrics.put("taxComplianceRate", totalTransactions > 0 ?
            BigDecimal.valueOf(taxableTransactions).divide(BigDecimal.valueOf(totalTransactions), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

        // Calculate average tax rates
        BigDecimal avgTaxRate = sales.stream()
            .filter(sale -> sale.getTaxPercentage() != null)
            .map(Sale::getTaxPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, sales.size())), 2, RoundingMode.HALF_UP);

        complianceMetrics.put("averageTaxRate", avgTaxRate);
        analysis.put("complianceMetrics", complianceMetrics);

        return analysis;
    }

    /**
     * Generate detailed cost analysis including COGS and operational costs
     */
    private Map<String, Object> generateDetailedCostAnalysis(List<Sale> sales, ReportRequestDTO request) {
        Map<String, Object> analysis = new HashMap<>();

        // Overall cost summary
        BigDecimal totalRevenue = sales.stream()
            .map(Sale::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCOGS = sales.stream()
            .map(Sale::getCostOfGoodsSold)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalShipping = sales.stream()
            .map(Sale::getShippingCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscounts = sales.stream()
            .map(Sale::getDiscountAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOperationalCosts = totalShipping; // Can be expanded to include other operational costs
        BigDecimal totalCosts = totalCOGS.add(totalOperationalCosts);

        Map<String, Object> costSummary = new HashMap<>();
        costSummary.put("totalRevenue", totalRevenue);
        costSummary.put("totalCOGS", totalCOGS);
        costSummary.put("totalShippingCosts", totalShipping);
        costSummary.put("totalDiscounts", totalDiscounts);
        costSummary.put("totalOperationalCosts", totalOperationalCosts);
        costSummary.put("totalCosts", totalCosts);
        costSummary.put("grossProfit", totalRevenue.subtract(totalCosts));
        costSummary.put("cogsPercentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalCOGS.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        costSummary.put("operationalCostPercentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalOperationalCosts.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

        analysis.put("costSummary", costSummary);

        // Cost per sale analysis
        Map<String, Object> costPerSale = new HashMap<>();
        int totalSales = sales.size();
        if (totalSales > 0) {
            costPerSale.put("avgCOGSPerSale", totalCOGS.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
            costPerSale.put("avgShippingPerSale", totalShipping.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
            costPerSale.put("avgTotalCostPerSale", totalCosts.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
            costPerSale.put("avgRevenuePerSale", totalRevenue.divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
            costPerSale.put("avgProfitPerSale", totalRevenue.subtract(totalCosts).divide(BigDecimal.valueOf(totalSales), 2, RoundingMode.HALF_UP));
        }
        costPerSale.put("totalSales", totalSales);
        analysis.put("costPerSale", costPerSale);

        // Cost by product category
        List<Object[]> categoryData = saleRepository.getRevenueByCategoryForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> categoryCostAnalysis = categoryData.stream()
            .map(row -> {
                Map<String, Object> category = new HashMap<>();
                category.put("categoryName", row[0]);
                category.put("salesCount", row[1]);
                category.put("totalQuantitySold", row[2]);
                BigDecimal categoryRevenue = (BigDecimal) row[3];
                BigDecimal categoryCost = (BigDecimal) row[4];

                category.put("totalRevenue", categoryRevenue);
                category.put("totalCost", categoryCost);
                category.put("grossProfit", categoryRevenue.subtract(categoryCost));
                category.put("costRatio", categoryRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryCost.divide(categoryRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                category.put("profitMargin", categoryRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryRevenue.subtract(categoryCost).divide(categoryRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

                return category;
            })
            .sorted((a, b) -> ((BigDecimal) a.get("costRatio")).compareTo((BigDecimal) b.get("costRatio")))
            .collect(Collectors.toList());

        analysis.put("costByCategory", categoryCostAnalysis);

        // Cost efficiency metrics
        Map<String, Object> efficiencyMetrics = new HashMap<>();

        // Calculate cost efficiency trends
        List<Object[]> dailyData = saleRepository.getDailyRevenueAnalysis(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> dailyCostEfficiency = dailyData.stream()
            .map(row -> {
                Map<String, Object> day = new HashMap<>();
                day.put("date", row[0].toString());
                BigDecimal dayRevenue = (BigDecimal) row[2];
                BigDecimal dayCost = (BigDecimal) row[3];

                day.put("revenue", dayRevenue);
                day.put("cost", dayCost);
                day.put("profit", dayRevenue.subtract(dayCost));
                day.put("costRatio", dayRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    dayCost.divide(dayRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                day.put("profitMargin", dayRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    dayRevenue.subtract(dayCost).divide(dayRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

                return day;
            })
            .collect(Collectors.toList());

        efficiencyMetrics.put("dailyCostEfficiency", dailyCostEfficiency);

        // Calculate average cost ratios
        if (!dailyCostEfficiency.isEmpty()) {
            BigDecimal avgCostRatio = dailyCostEfficiency.stream()
                .map(day -> (BigDecimal) day.get("costRatio"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyCostEfficiency.size()), 2, RoundingMode.HALF_UP);

            BigDecimal avgProfitMargin = dailyCostEfficiency.stream()
                .map(day -> (BigDecimal) day.get("profitMargin"))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyCostEfficiency.size()), 2, RoundingMode.HALF_UP);

            efficiencyMetrics.put("averageCostRatio", avgCostRatio);
            efficiencyMetrics.put("averageProfitMargin", avgProfitMargin);
        }

        analysis.put("efficiencyMetrics", efficiencyMetrics);

        // Top cost-efficient and cost-inefficient products
        List<Object[]> productData = saleRepository.getRevenueByProductForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> productCostAnalysis = productData.stream()
            .map(row -> {
                Map<String, Object> product = new HashMap<>();
                product.put("productName", row[0]);
                product.put("productSku", row[1]);
                BigDecimal productRevenue = (BigDecimal) row[4];
                BigDecimal productCost = (BigDecimal) row[5];

                product.put("totalRevenue", productRevenue);
                product.put("totalCost", productCost);
                product.put("costRatio", productRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    productCost.divide(productRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
                product.put("profitMargin", productRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                    productRevenue.subtract(productCost).divide(productRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

                return product;
            })
            .collect(Collectors.toList());

        // Most cost-efficient products (lowest cost ratio)
        List<Map<String, Object>> mostEfficient = productCostAnalysis.stream()
            .sorted((a, b) -> ((BigDecimal) a.get("costRatio")).compareTo((BigDecimal) b.get("costRatio")))
            .limit(10)
            .collect(Collectors.toList());

        // Least cost-efficient products (highest cost ratio)
        List<Map<String, Object>> leastEfficient = productCostAnalysis.stream()
            .sorted((a, b) -> ((BigDecimal) b.get("costRatio")).compareTo((BigDecimal) a.get("costRatio")))
            .limit(10)
            .collect(Collectors.toList());

        analysis.put("mostCostEfficientProducts", mostEfficient);
        analysis.put("leastCostEfficientProducts", leastEfficient);

        return analysis;
    }

    /**
     * Generate advanced financial metrics including CLV, conversion rates, and seasonal patterns
     */
    private Map<String, Object> generateAdvancedFinancialMetrics(List<Sale> sales, ReportRequestDTO request) {
        Map<String, Object> metrics = new HashMap<>();

        // Customer Lifetime Value Impact Analysis
        List<Object[]> customerData = saleRepository.getCustomerRevenueAnalysisForPeriod(request.getStartDate(), request.getEndDate());
        List<Map<String, Object>> customerAnalysis = customerData.stream()
            .map(row -> {
                Map<String, Object> customer = new HashMap<>();
                customer.put("customerId", row[0]);
                customer.put("customerName", row[1]);
                customer.put("customerType", row[2]);
                customer.put("totalOrders", row[3]);
                customer.put("totalRevenue", row[4]);
                customer.put("avgOrderValue", row[5]);
                customer.put("lastPurchaseDate", row[6]);

                // Calculate customer value metrics
                Long totalOrders = (Long) row[3];
                BigDecimal totalRevenue = (BigDecimal) row[4];

                if (totalOrders > 0) {
                    customer.put("revenuePerOrder", totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP));
                }

                return customer;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("totalRevenue")).compareTo((BigDecimal) a.get("totalRevenue")))
            .collect(Collectors.toList());

        // Top customers by revenue contribution
        List<Map<String, Object>> topCustomers = customerAnalysis.stream()
            .limit(20)
            .collect(Collectors.toList());

        metrics.put("topCustomersByRevenue", topCustomers);

        // Customer segmentation analysis
        BigDecimal totalCustomerRevenue = customerAnalysis.stream()
            .map(c -> (BigDecimal) c.get("totalRevenue"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> customerSegmentation = new HashMap<>();
        customerSegmentation.put("totalCustomers", customerAnalysis.size());
        customerSegmentation.put("totalRevenue", totalCustomerRevenue);

        if (!customerAnalysis.isEmpty()) {
            BigDecimal avgRevenuePerCustomer = totalCustomerRevenue.divide(BigDecimal.valueOf(customerAnalysis.size()), 2, RoundingMode.HALF_UP);
            customerSegmentation.put("avgRevenuePerCustomer", avgRevenuePerCustomer);

            // Pareto analysis (80/20 rule)
            BigDecimal top20PercentRevenue = customerAnalysis.stream()
                .limit((int) Math.ceil(customerAnalysis.size() * 0.2))
                .map(c -> (BigDecimal) c.get("totalRevenue"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            customerSegmentation.put("top20PercentCustomers", (int) Math.ceil(customerAnalysis.size() * 0.2));
            customerSegmentation.put("top20PercentRevenue", top20PercentRevenue);
            customerSegmentation.put("paretoRatio", totalCustomerRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                top20PercentRevenue.divide(totalCustomerRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        }

        metrics.put("customerSegmentation", customerSegmentation);

        // Sales conversion analysis
        Map<String, Object> conversionMetrics = new HashMap<>();
        long totalSales = sales.size();
        long uniqueCustomers = sales.stream()
            .filter(sale -> sale.getCustomer() != null)
            .map(sale -> sale.getCustomer().getId())
            .distinct()
            .count();

        conversionMetrics.put("totalSales", totalSales);
        conversionMetrics.put("uniqueCustomers", uniqueCustomers);
        conversionMetrics.put("salesPerCustomer", uniqueCustomers > 0 ?
            BigDecimal.valueOf(totalSales).divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        // Repeat customer analysis
        Map<Long, Long> customerOrderCounts = sales.stream()
            .filter(sale -> sale.getCustomer() != null)
            .collect(Collectors.groupingBy(
                sale -> sale.getCustomer().getId(),
                Collectors.counting()
            ));

        long repeatCustomers = customerOrderCounts.values().stream()
            .filter(count -> count > 1)
            .count();

        conversionMetrics.put("repeatCustomers", repeatCustomers);
        conversionMetrics.put("repeatCustomerRate", uniqueCustomers > 0 ?
            BigDecimal.valueOf(repeatCustomers).divide(BigDecimal.valueOf(uniqueCustomers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);

        metrics.put("conversionMetrics", conversionMetrics);

        // Seasonal patterns analysis
        Map<String, Object> seasonalAnalysis = new HashMap<>();

        // Group sales by month
        Map<String, List<Sale>> salesByMonth = sales.stream()
            .collect(Collectors.groupingBy(sale ->
                sale.getSaleDate().getYear() + "-" + String.format("%02d", sale.getSaleDate().getMonthValue())));

        List<Map<String, Object>> monthlyPatterns = salesByMonth.entrySet().stream()
            .map(entry -> {
                String month = entry.getKey();
                List<Sale> monthlySales = entry.getValue();

                BigDecimal monthlyRevenue = monthlySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", month);
                monthData.put("salesCount", monthlySales.size());
                monthData.put("revenue", monthlyRevenue);
                monthData.put("avgOrderValue", monthlySales.size() > 0 ?
                    monthlyRevenue.divide(BigDecimal.valueOf(monthlySales.size()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                return monthData;
            })
            .sorted((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")))
            .collect(Collectors.toList());

        seasonalAnalysis.put("monthlyPatterns", monthlyPatterns);

        // Day of week analysis
        Map<String, List<Sale>> salesByDayOfWeek = sales.stream()
            .collect(Collectors.groupingBy(sale ->
                sale.getSaleDate().getDayOfWeek().toString()));

        List<Map<String, Object>> dayOfWeekPatterns = salesByDayOfWeek.entrySet().stream()
            .map(entry -> {
                String dayOfWeek = entry.getKey();
                List<Sale> daySales = entry.getValue();

                BigDecimal dayRevenue = daySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("dayOfWeek", dayOfWeek);
                dayData.put("salesCount", daySales.size());
                dayData.put("revenue", dayRevenue);
                dayData.put("avgOrderValue", daySales.size() > 0 ?
                    dayRevenue.divide(BigDecimal.valueOf(daySales.size()), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

                return dayData;
            })
            .collect(Collectors.toList());

        seasonalAnalysis.put("dayOfWeekPatterns", dayOfWeekPatterns);
        metrics.put("seasonalAnalysis", seasonalAnalysis);

        return metrics;
    }

    /**
     * Generate executive financial summary with key insights and recommendations
     */
    private Map<String, Object> generateExecutiveFinancialSummary(List<Sale> sales, Object[] financialSummary, ReportRequestDTO request) {
        Map<String, Object> summary = new HashMap<>();

        // Key financial indicators - with safe casting
        BigDecimal totalRevenue = safeCastToBigDecimal(financialSummary, 0);
        BigDecimal totalCost = safeCastToBigDecimal(financialSummary, 1);
        BigDecimal totalTax = safeCastToBigDecimal(financialSummary, 2);
        Long totalTransactions = safeCastToLong(financialSummary, 5);
        Long uniqueCustomers = safeCastToLong(financialSummary, 6);

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalRevenue", totalRevenue);
        kpis.put("grossProfit", totalRevenue.subtract(totalCost));
        kpis.put("grossMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalRevenue.subtract(totalCost).divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO);
        kpis.put("totalTransactions", totalTransactions);
        kpis.put("uniqueCustomers", uniqueCustomers);
        kpis.put("avgOrderValue", totalTransactions > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        kpis.put("revenuePerCustomer", uniqueCustomers > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        summary.put("keyPerformanceIndicators", kpis);

        // Performance insights
        List<String> insights = new ArrayList<>();

        BigDecimal grossMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
            totalRevenue.subtract(totalCost).divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        if (grossMargin.compareTo(BigDecimal.valueOf(30)) >= 0) {
            insights.add("Excellent gross margin of " + grossMargin + "% indicates strong pricing strategy and cost control.");
        } else if (grossMargin.compareTo(BigDecimal.valueOf(20)) >= 0) {
            insights.add("Good gross margin of " + grossMargin + "% with room for improvement in cost optimization.");
        } else {
            insights.add("Low gross margin of " + grossMargin + "% requires immediate attention to pricing and cost structure.");
        }

        BigDecimal avgOrderValue = totalTransactions > 0 ?
            totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        if (avgOrderValue.compareTo(BigDecimal.valueOf(100)) >= 0) {
            insights.add("Strong average order value of $" + avgOrderValue + " indicates effective upselling strategies.");
        } else {
            insights.add("Average order value of $" + avgOrderValue + " suggests opportunities for cross-selling and upselling.");
        }

        if (uniqueCustomers > 0 && totalTransactions > 0) {
            BigDecimal salesPerCustomer = BigDecimal.valueOf(totalTransactions).divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP);
            if (salesPerCustomer.compareTo(BigDecimal.valueOf(2)) >= 0) {
                insights.add("Good customer retention with " + salesPerCustomer + " average transactions per customer.");
            } else {
                insights.add("Focus needed on customer retention - only " + salesPerCustomer + " transactions per customer on average.");
            }
        }

        summary.put("insights", insights);

        // Recommendations
        List<String> recommendations = new ArrayList<>();

        if (grossMargin.compareTo(BigDecimal.valueOf(25)) < 0) {
            recommendations.add("Review pricing strategy and negotiate better supplier terms to improve gross margins.");
        }

        if (avgOrderValue.compareTo(BigDecimal.valueOf(75)) < 0) {
            recommendations.add("Implement cross-selling and upselling strategies to increase average order value.");
        }

        if (uniqueCustomers > 0 && totalTransactions > 0) {
            BigDecimal salesPerCustomer = BigDecimal.valueOf(totalTransactions).divide(BigDecimal.valueOf(uniqueCustomers), 2, RoundingMode.HALF_UP);
            if (salesPerCustomer.compareTo(BigDecimal.valueOf(1.5)) < 0) {
                recommendations.add("Develop customer loyalty programs to increase repeat purchase rates.");
            }
        }

        recommendations.add("Monitor daily revenue trends to identify peak performance periods and optimize operations.");
        recommendations.add("Analyze top-performing products and categories to focus marketing and inventory investments.");

        summary.put("recommendations", recommendations);

        // Period comparison
        Map<String, Object> periodInfo = new HashMap<>();
        periodInfo.put("startDate", request.getStartDate().format(DATE_FORMATTER));
        periodInfo.put("endDate", request.getEndDate().format(DATE_FORMATTER));
        periodInfo.put("periodDays", ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1);
        periodInfo.put("avgDailyRevenue", totalRevenue.divide(
            BigDecimal.valueOf(ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1),
            2, RoundingMode.HALF_UP));

        summary.put("periodInformation", periodInfo);

        return summary;
    }

    // Dashboard helper methods
    private Map<String, Object> generateExecutiveKPIs(LocalDateTime start, LocalDateTime end) { return new HashMap<>(); }
    private Map<String, Object> generateSalesOverview(LocalDateTime start, LocalDateTime end) { return new HashMap<>(); }
    private Map<String, Object> generateCustomerMetrics(LocalDateTime start, LocalDateTime end) { return new HashMap<>(); }
    private Map<String, Object> generateFinancialSummary(LocalDateTime start, LocalDateTime end) { return new HashMap<>(); }
    private List<Map<String, Object>> generateExecutiveAlerts() { return new ArrayList<>(); }
    private Map<String, Object> getTodaysSalesMetrics() { return new HashMap<>(); }
    private List<Map<String, Object>> getInventoryAlerts() { return new ArrayList<>(); }
    private Map<String, Object> getPendingOrdersMetrics() { return new HashMap<>(); }
    private Map<String, Object> getCustomerServiceMetrics() { return new HashMap<>(); }
    private Map<String, Object> getSystemHealthMetrics() { return new HashMap<>(); }

    // Real-time KPI helper methods
    private Long getTodaysSalesCount(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end).stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .count();
    }

    private BigDecimal getTodaysRevenue(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end).stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long getActiveCustomersToday(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end).stream()
                .map(sale -> sale.getCustomer().getId())
                .distinct()
                .count();
    }

    private BigDecimal getCurrentInventoryValue() {
        return productRepository.findAll().stream()
                .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long getLowStockItemsCount() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() < 10)
                .count();
    }

    private Long getPendingReturnsCount() {
        return (long) returnRepository.findByStatus(Return.ReturnStatus.PENDING).size();
    }

    // Helper methods for default dashboard

    private Map<String, Object> generateBasicSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalSales", sales.size(),
                "totalRevenue", totalRevenue,
                "averageOrderValue", sales.isEmpty() ? BigDecimal.ZERO :
                    totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP),
                "period", Map.of(
                    "startDate", startDate.format(DATE_FORMATTER),
                    "endDate", endDate.format(DATE_FORMATTER)
                )
        );
    }

    private Map<String, Object> getRecentSalesMetrics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Sale> recentSales = saleRepository.findBySaleDateBetween(startDate, LocalDateTime.now())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .limit(10)
                .collect(Collectors.toList());

        Map<String, Object> recentSalesReport = new HashMap<>();
        recentSalesReport.put("count", recentSales.size());
        recentSalesReport.put("sales", recentSales.stream()
                .map(sale -> {
                    Map<String, Object> saleMap = new HashMap<>();
                    saleMap.put("id", sale.getId());
                    saleMap.put("customerName", sale.getCustomer().getName());
                    saleMap.put("totalAmount", sale.getTotalAmount());
                    saleMap.put("saleDate", sale.getSaleDate().format(DATE_FORMATTER));
                    return saleMap;
                })
                .collect(Collectors.toList()));

        return recentSalesReport;
    }

    private Map<String, Object> getTopProductsMetrics(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, LocalDateTime.now())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Integer> productSales = new HashMap<>();
        Map<String, BigDecimal> productRevenue = new HashMap<>();

        sales.forEach(sale ->
            sale.getItems().forEach(item -> {
                String productName = item.getProduct().getName();
                productSales.merge(productName, item.getQuantity(), Integer::sum);
                productRevenue.merge(productName,
                    item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                    BigDecimal::add);
            })
        );

        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("productName", entry.getKey());
                    productMap.put("quantitySold", entry.getValue());
                    productMap.put("revenue", productRevenue.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                    return productMap;
                })
                .collect(Collectors.toList());

        return Map.of("topProducts", topProducts);
    }

    private Map<String, Object> generateQuickStats() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> quickStats = new HashMap<>();
        quickStats.put("todaysSales", getTodaysSalesCount(today, now));
        quickStats.put("todaysRevenue", getTodaysRevenue(today, now));
        quickStats.put("totalCustomers", customerRepository.count());
        quickStats.put("totalProducts", productRepository.count());
        quickStats.put("lowStockItems", getLowStockItemsCount());

        return quickStats;
    }

    // ==================== UTILITY METHODS FOR SAFE CASTING ====================

    /**
     * Safely cast array element to BigDecimal with null and bounds checking
     */
    private BigDecimal safeCastToBigDecimal(Object[] array, int index) {
        if (array == null || index >= array.length || array[index] == null) {
            return BigDecimal.ZERO;
        }
        try {
            return (BigDecimal) array[index];
        } catch (ClassCastException e) {
            log.warn("Failed to cast array[{}] to BigDecimal: {}", index, array[index]);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Safely cast array element to Long with null and bounds checking
     */
    private Long safeCastToLong(Object[] array, int index) {
        if (array == null || index >= array.length || array[index] == null) {
            return 0L;
        }
        try {
            return (Long) array[index];
        } catch (ClassCastException e) {
            log.warn("Failed to cast array[{}] to Long: {}", index, array[index]);
            return 0L;
        }
    }
}
