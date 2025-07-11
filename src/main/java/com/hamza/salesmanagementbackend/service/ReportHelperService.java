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
        // Safe BigDecimal operations with null checks
        BigDecimal totalRevenue = sales.stream()
                .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscounts = sales.stream()
                .map(sale -> sale.getPromotionDiscountAmount() != null ? sale.getPromotionDiscountAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = sales.stream()
                .map(sale -> sale.getTaxAmount() != null ? sale.getTaxAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = sales.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);

        int uniqueCustomers = (int) sales.stream()
                .filter(sale -> sale.getCustomer() != null)
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
                            .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal dailyDiscounts = dailySales.stream()
                            .map(sale -> sale.getPromotionDiscountAmount() != null ? sale.getPromotionDiscountAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgOrderValue = dailySales.isEmpty() ? BigDecimal.ZERO :
                            dailyRevenue.divide(BigDecimal.valueOf(dailySales.size()), 2, RoundingMode.HALF_UP);

                    int uniqueCustomers = (int) dailySales.stream()
                            .filter(sale -> sale.getCustomer() != null)
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
                .filter(sale -> sale.getCustomer() != null)
                .collect(Collectors.groupingBy(Sale::getCustomer));

        return salesByCustomer.entrySet().stream()
                .map(entry -> {
                    Customer customer = entry.getKey();
                    List<Sale> customerSales = entry.getValue();

                    BigDecimal totalSpent = customerSales.stream()
                            .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgOrderValue = customerSales.isEmpty() ? BigDecimal.ZERO :
                            totalSpent.divide(BigDecimal.valueOf(customerSales.size()), 2, RoundingMode.HALF_UP);

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
                .filter(sale -> sale.getItems() != null)
                .flatMap(sale -> sale.getItems().stream())
                .filter(item -> item.getProduct() != null)
                .collect(Collectors.groupingBy(SaleItem::getProduct));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    Product product = entry.getKey();
                    List<SaleItem> productItems = entry.getValue();

                    int totalQuantity = productItems.stream()
                            .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                            .sum();

                    BigDecimal totalRevenue = productItems.stream()
                            .map(item -> {
                                BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                                return unitPrice.multiply(BigDecimal.valueOf(quantity));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avgPrice = productItems.isEmpty() ? BigDecimal.ZERO :
                            productItems.stream()
                                    .map(item -> item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(productItems.size()), 2, RoundingMode.HALF_UP);

                    // Calculate profit margin
                    BigDecimal totalCost = productItems.stream()
                            .map(item -> {
                                BigDecimal costPrice = item.getCostPrice() != null ? item.getCostPrice() : BigDecimal.ZERO;
                                int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                                return costPrice.multiply(BigDecimal.valueOf(quantity));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                            totalRevenue.subtract(totalCost).divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

                    int uniqueCustomers = (int) productItems.stream()
                            .filter(item -> item.getSale() != null && item.getSale().getCustomer() != null)
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
                        sale -> sale.getPaymentMethod() != null ?
                                sale.getPaymentMethod().toString() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, BigDecimal> revenueByMethod = sales.stream()
                .collect(Collectors.groupingBy(
                        sale -> sale.getPaymentMethod() != null ?
                                sale.getPaymentMethod().toString() : "UNKNOWN",
                        Collectors.mapping(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO,
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

    public SalesReportDTO.RegionalAnalysis generateRegionalAnalysis(List<Sale> sales) {
        // Extract regions from customer addresses
        Map<String, BigDecimal> revenueByRegion = new HashMap<>();
        Map<String, Long> salesByRegion = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getCustomer() != null) {
                String region = extractRegionFromCustomer(sale.getCustomer());
                BigDecimal amount = sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO;

                // Update revenue by region
                revenueByRegion.merge(region, amount, BigDecimal::add);

                // Update sales count by region
                salesByRegion.merge(region, 1L, Long::sum);
            }
        }

        // Find top performing region
        String topPerformingRegion = revenueByRegion.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        // Calculate regional growth (simplified - comparing to previous period would require additional data)
        BigDecimal regionalGrowth = BigDecimal.valueOf(15.5); // Placeholder for now

        return SalesReportDTO.RegionalAnalysis.builder()
                .revenueByRegion(revenueByRegion)
                .salesByRegion(salesByRegion)
                .topPerformingRegion(topPerformingRegion)
                .regionalGrowth(regionalGrowth)
                .build();
    }

    // ==================== UTILITY METHODS ====================

    private Double calculateConversionRate(List<Sale> sales) {
        if (sales.isEmpty()) {
            return 0.0;
        }

        // Calculate conversion rate based on completed vs total sales
        long totalSalesAttempts = saleRepository.count();
        long completedSales = sales.size();

        if (totalSalesAttempts == 0) {
            return 0.0;
        }

        return (completedSales * 100.0) / totalSalesAttempts;
    }

    private BigDecimal calculateRevenueGrowth(ReportRequestDTO request) {
        try {
            // Calculate the duration of the current period
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                request.getStartDate().toLocalDate(),
                request.getEndDate().toLocalDate()
            );

            // Get previous period data
            LocalDateTime previousStart = request.getStartDate().minusDays(daysBetween);
            LocalDateTime previousEnd = request.getStartDate().minusDays(1);

            // Current period revenue
            List<Sale> currentSales = saleRepository.findBySaleDateBetween(
                request.getStartDate(), request.getEndDate())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

            BigDecimal currentRevenue = currentSales.stream()
                .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Previous period revenue
            List<Sale> previousSales = saleRepository.findBySaleDateBetween(previousStart, previousEnd)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

            BigDecimal previousRevenue = previousSales.stream()
                .map(sale -> sale.getTotalAmount() != null ? sale.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
                return currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
            }

            // Calculate growth percentage
            BigDecimal growth = currentRevenue.subtract(previousRevenue)
                .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            return growth;
        } catch (Exception e) {
            log.warn("Error calculating revenue growth: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Double calculateSalesGrowth(ReportRequestDTO request) {
        try {
            // Calculate the duration of the current period
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                request.getStartDate().toLocalDate(),
                request.getEndDate().toLocalDate()
            );

            // Get previous period data
            LocalDateTime previousStart = request.getStartDate().minusDays(daysBetween);
            LocalDateTime previousEnd = request.getStartDate().minusDays(1);

            // Current period sales count
            long currentSalesCount = saleRepository.findBySaleDateBetween(
                request.getStartDate(), request.getEndDate())
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .count();

            // Previous period sales count
            long previousSalesCount = saleRepository.findBySaleDateBetween(previousStart, previousEnd)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .count();

            if (previousSalesCount == 0) {
                return currentSalesCount > 0 ? 100.0 : 0.0;
            }

            // Calculate growth percentage
            double growth = ((double) (currentSalesCount - previousSalesCount) / previousSalesCount) * 100;
            return growth;
        } catch (Exception e) {
            log.warn("Error calculating sales growth: {}", e.getMessage());
            return 0.0;
        }
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

    private String extractRegionFromCustomer(Customer customer) {
        if (customer == null) {
            return "Unknown";
        }

        String address = customer.getAddress();
        if (address == null || address.trim().isEmpty()) {
            address = customer.getBillingAddress();
        }

        if (address == null || address.trim().isEmpty()) {
            return "Unknown";
        }

        // Simple region extraction based on common patterns
        String lowerAddress = address.toLowerCase();

        // Check for common region indicators
        if (lowerAddress.contains("north") || lowerAddress.contains("northern")) {
            return "North Region";
        } else if (lowerAddress.contains("south") || lowerAddress.contains("southern")) {
            return "South Region";
        } else if (lowerAddress.contains("east") || lowerAddress.contains("eastern")) {
            return "East Region";
        } else if (lowerAddress.contains("west") || lowerAddress.contains("western")) {
            return "West Region";
        } else if (lowerAddress.contains("central") || lowerAddress.contains("center")) {
            return "Central Region";
        }

        // Check for state/province patterns (simplified)
        if (lowerAddress.contains("ca") || lowerAddress.contains("california")) {
            return "West Region";
        } else if (lowerAddress.contains("ny") || lowerAddress.contains("new york")) {
            return "East Region";
        } else if (lowerAddress.contains("tx") || lowerAddress.contains("texas")) {
            return "South Region";
        } else if (lowerAddress.contains("il") || lowerAddress.contains("illinois")) {
            return "Central Region";
        }

        // Default to extracting first part of address as region
        String[] parts = address.split(",");
        if (parts.length > 1) {
            return parts[parts.length - 1].trim() + " Region";
        }

        return "Unknown Region";
    }
}
