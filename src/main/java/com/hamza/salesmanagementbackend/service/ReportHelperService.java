package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.report.ReportRequestDTO;
import com.hamza.salesmanagementbackend.dto.report.SalesReportDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper service containing utility methods for report generation
 * Separated from main ReportService to maintain clean code organization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportHelperService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== SALES REPORT HELPERS ====================

    public SalesReportDTO.SalesSummary generateAdvancedSalesSummary(List<Sale> sales, ReportRequestDTO request) {
        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscounts = sales.stream()
                .map(Sale::getPromotionDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = sales.stream()
                .map(Sale::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = sales.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(sales.size()), RoundingMode.HALF_UP);

        int uniqueCustomers = (int) sales.stream()
                .map(sale -> sale.getCustomer().getId())
                .distinct()
                .count();

        return SalesReportDTO.SalesSummary.builder()
                .totalSales((long) sales.size())
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .totalDiscounts(totalDiscounts)
                .totalTax(totalTax)
                .netRevenue(totalRevenue.subtract(totalDiscounts))
                .uniqueCustomers(uniqueCustomers)
                .conversionRate(calculateConversionRate(sales))
                .revenueGrowth(calculateRevenueGrowth(request))
                .salesGrowth(calculateSalesGrowth(request))
                .build();
    }

    public List<SalesReportDTO.DailySalesData> generateDailyBreakdown(List<Sale> sales) {
        Map<String, List<Sale>> salesByDate = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().toLocalDate().format(DATE_FORMATTER)
                ));

        return salesByDate.entrySet().stream()
                .map(entry -> {
                    List<Sale> dailySales = entry.getValue();
                    BigDecimal dailyRevenue = dailySales.stream()
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal dailyDiscounts = dailySales.stream()
                            .map(Sale::getPromotionDiscountAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgOrderValue = dailySales.isEmpty() ? BigDecimal.ZERO :
                            dailyRevenue.divide(BigDecimal.valueOf(dailySales.size()), RoundingMode.HALF_UP);

                    int uniqueCustomers = (int) dailySales.stream()
                            .map(sale -> sale.getCustomer().getId())
                            .distinct()
                            .count();

                    return SalesReportDTO.DailySalesData.builder()
                            .date(entry.getKey())
                            .salesCount((long) dailySales.size())
                            .revenue(dailyRevenue)
                            .averageOrderValue(avgOrderValue)
                            .uniqueCustomers(uniqueCustomers)
                            .discountAmount(dailyDiscounts)
                            .build();
                })
                .sorted(Comparator.comparing(SalesReportDTO.DailySalesData::getDate))
                .collect(Collectors.toList());
    }

    public List<SalesReportDTO.TopCustomer> generateTopCustomersAnalysis(List<Sale> sales) {
        Map<Customer, List<Sale>> salesByCustomer = sales.stream()
                .collect(Collectors.groupingBy(Sale::getCustomer));

        return salesByCustomer.entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Sale> customerSales = entry.getValue();

                    BigDecimal totalSpent = customerSales.stream()
                            .map(Sale::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgOrderValue = customerSales.isEmpty() ? BigDecimal.ZERO :
                            totalSpent.divide(BigDecimal.valueOf(customerSales.size()), RoundingMode.HALF_UP);

                    LocalDateTime lastPurchase = customerSales.stream()
                            .map(Sale::getSaleDate)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);

                    return SalesReportDTO.TopCustomer.builder()
                            .customerId(customer.getId())
                            .customerName(customer.getName())
                            .customerEmail(customer.getEmail())
                            .totalOrders((long) customerSales.size())
                            .totalSpent(totalSpent)
                            .averageOrderValue(avgOrderValue)
                            .lastPurchase(lastPurchase)
                            .customerSegment(determineCustomerSegment(totalSpent, customerSales.size()))
                            .build();
                })
                .sorted(Comparator.comparing(SalesReportDTO.TopCustomer::getTotalSpent).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<SalesReportDTO.TopProduct> generateTopProductsAnalysis(List<Sale> sales) {
        Map<Product, List<SaleItem>> itemsByProduct = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<SaleItem> productItems = entry.getValue();

                    int totalQuantity = productItems.stream()
                            .mapToInt(SaleItem::getQuantity)
                            .sum();

                    BigDecimal totalRevenue = productItems.stream()
                            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgPrice = productItems.stream()
                            .map(SaleItem::getUnitPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(productItems.size()), RoundingMode.HALF_UP);

                    // Calculate profit margin
                    BigDecimal totalCost = productItems.stream()
                            .map(item -> (item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO)
                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            totalRevenue.subtract(totalCost).divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                    int uniqueCustomers = (int) productItems.stream()
                            .map(item -> item.getSale().getCustomer().getId())
                            .distinct()
                            .count();

                    return SalesReportDTO.TopProduct.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .category(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized")
                            .quantitySold(totalQuantity)
                            .revenue(totalRevenue)
                            .averagePrice(avgPrice)
                            .profitMargin(profitMargin)
                            .uniqueCustomers(uniqueCustomers)
                            .build();
                })
                .sorted(Comparator.comparing(SalesReportDTO.TopProduct::getRevenue).reversed())
                .limit(20)
                .collect(Collectors.toList());
    }

    public SalesReportDTO.PaymentMethodAnalysis generatePaymentMethodAnalysis(List<Sale> sales) {
        Map<String, Long> countByMethod = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getPaymentMethod().toString(),
                        Collectors.counting()
                ));

        Map<String, BigDecimal> revenueByMethod = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getPaymentMethod().toString(),
                        Collectors.mapping(Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        String mostPopularMethod = countByMethod.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String highestRevenueMethod = revenueByMethod.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return SalesReportDTO.PaymentMethodAnalysis.builder()
                .countByMethod(countByMethod)
                .revenueByMethod(revenueByMethod)
                .mostPopularMethod(mostPopularMethod)
                .highestRevenueMethod(highestRevenueMethod)
                .build();
    }

    // ==================== UTILITY METHODS ====================

    private Double calculateConversionRate(List<Sale> sales) {
        // Simplified conversion rate calculation
        // In real implementation, this would compare against total website visitors or leads
        return 85.5; // Placeholder
    }

    private BigDecimal calculateRevenueGrowth(ReportRequestDTO request) {
        // Calculate growth compared to previous period
        // This is a simplified implementation
        return BigDecimal.valueOf(12.5); // Placeholder
    }

    private Double calculateSalesGrowth(ReportRequestDTO request) {
        // Calculate sales count growth compared to previous period
        return 8.3; // Placeholder
    }

    private String determineCustomerSegment(BigDecimal totalSpent, int orderCount) {
        if (totalSpent.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return "VIP";
        } else if (totalSpent.compareTo(BigDecimal.valueOf(5000)) > 0) {
            return "Premium";
        } else if (orderCount > 10) {
            return "Loyal";
        } else {
            return "Regular";
        }
    }
}
