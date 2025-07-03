package com.hamza.salesmanagementbackend.controller;

import com.hamza.salesmanagementbackend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> report = reportService.generateSalesReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueReport(
            @RequestParam(defaultValue = "6") int months) {

        Map<String, Object> report = reportService.generateRevenueTrends(months);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/top-products")
    public ResponseEntity<Map<String, Object>> getTopSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> topProducts = reportService.generateTopPerformersReport(startDate, endDate);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/customer-analytics")
    public ResponseEntity<Map<String, Object>> getCustomerAnalytics() {

        Map<String, Object> analytics = reportService.generateCustomerReport();
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventoryReport() {

        Map<String, Object> inventory = reportService.generateInventoryReport();
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {

        // Create a comprehensive dashboard using multiple report methods
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30); // Last 30 days

        Map<String, Object> salesReport = reportService.generateSalesReport(startDate, endDate);
        Map<String, Object> customerReport = reportService.generateCustomerReport();
        Map<String, Object> inventoryReport = reportService.generateInventoryReport();
        Map<String, Object> revenueReport = reportService.generateRevenueTrends(6);

        Map<String, Object> dashboard = Map.of(
                "period", "Last 30 days",
                "sales", salesReport,
                "customers", customerReport,
                "inventory", inventoryReport,
                "revenue", revenueReport,
                "generatedAt", LocalDateTime.now()
        );

        return ResponseEntity.ok(dashboard);
    }
}
