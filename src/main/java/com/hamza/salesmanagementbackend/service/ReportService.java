package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public ReportService(SaleRepository saleRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

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
                                customerSales -> Map.of(
                                        "totalSales", customerSales.size(),
                                        "totalSpent", customerSales.stream()
                                                .map(Sale::getTotalAmount)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                                        "averageOrderValue", calculateAverageOrderValue(customerSales),
                                        "lastPurchase", customerSales.stream()
                                                .map(Sale::getSaleDate)
                                                .max(LocalDateTime::compareTo)
                                                .orElse(null)
                                )
                        )
                ));

        long totalCustomers = customerRepository.count();
        long activeCustomers = customerStats.size();

        return Map.of(
                "totalCustomers", totalCustomers,
                "activeCustomers", activeCustomers,
                "customerRetentionRate", calculateRetentionRate(activeCustomers, totalCustomers),
                "customerDetails", customerStats
        );
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
                                                    categoryProducts -> Map.of(
                                                            "productCount", categoryProducts.size(),
                                                            "totalValue", categoryProducts.stream()
                                                                    .map(p -> p.getPrice().multiply(
                                                                            BigDecimal.valueOf(p.getStockQuantity())))
                                                                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                                                            "averagePrice", categoryProducts.stream()
                                                                    .map(p -> p.getPrice())
                                                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                                    .divide(BigDecimal.valueOf(categoryProducts.size()),
                                                                            RoundingMode.HALF_UP)
                                                    )
                                            )
                                    ));

                            return Map.of(
                                    "totalProducts", products.size(),
                                    "totalInventoryValue", totalInventoryValue,
                                    "stockLevels", stockLevels,
                                    "categoryAnalysis", categoryAnalysis,
                                    "lowStockProducts", products.stream()
                                            .filter(p -> p.getStockQuantity() < 10)
                                            .map(p -> Map.of(
                                                    "name", p.getName(),
                                                    "currentStock", p.getStockQuantity(),
                                                    "category", p.getCategory() != null ? p.getCategory().getName() : "Uncategorized"
                                            ))
                                            .collect(Collectors.toList())
                            );
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

        // Top products by revenue
        Map<String, BigDecimal> topProductsByRevenue = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.mapping(
                                item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
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
        return sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                items -> Map.of(
                                        "quantitySold", items.stream()
                                                .mapToInt(item -> item.getQuantity())
                                                .sum(),
                                        "revenue", items.stream()
                                                .map(item -> item.getUnitPrice()
                                                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                                        "averagePrice", items.stream()
                                                .map(item -> item.getUnitPrice())
                                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                                .divide(BigDecimal.valueOf(items.size()), RoundingMode.HALF_UP)
                                )
                        )
                ));
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
}
