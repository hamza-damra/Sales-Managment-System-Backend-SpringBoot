package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceNullPointerFixTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ReportService reportService;

    private List<Customer> testCustomers;
    private List<Sale> testSales;

    @BeforeEach
    void setUp() {
        testCustomers = new ArrayList<>();
        testSales = new ArrayList<>();

        // Create test customers with no sales (this should trigger the null scenario)
        Customer customer1 = Customer.builder()
                .id(1L)
                .name("Test Customer 1")
                .email("test1@example.com")
                .createdAt(LocalDateTime.now().minusMonths(2))
                .build();

        Customer customer2 = Customer.builder()
                .id(2L)
                .name("Test Customer 2")
                .email("test2@example.com")
                .createdAt(LocalDateTime.now().minusMonths(1))
                .build();

        testCustomers.add(customer1);
        testCustomers.add(customer2);
    }

    @Test
    void testGenerateCustomerAnalyticsReport_WithNoSales_ShouldNotThrowNullPointer() {
        // Mock repositories to return empty lists (scenario that caused the original NPE)
        when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testSales); // Empty list
        when(customerRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testCustomers); // Customers with no sales
        when(customerRepository.findAllActive()).thenReturn(testCustomers);
        when(saleRepository.findAll()).thenReturn(testSales); // Empty sales list

        // This should not throw NullPointerException anymore
        assertDoesNotThrow(() -> {
            Map<String, Object> result = reportService.generateCustomerAnalyticsReport(true, 6);
            
            // Verify the result is not null and contains expected keys
            assertNotNull(result);
            assertTrue(result.containsKey("acquisitionMetrics"));
            assertTrue(result.containsKey("customerSegmentation"));
            assertTrue(result.containsKey("lifetimeValueAnalysis"));
            assertTrue(result.containsKey("behaviorAnalysis"));
            assertTrue(result.containsKey("churnAnalysis"));
            
            // Verify acquisition metrics handles null growth rate properly
            Map<String, Object> acquisitionMetrics = (Map<String, Object>) result.get("acquisitionMetrics");
            assertNotNull(acquisitionMetrics);
            assertTrue(acquisitionMetrics.containsKey("acquisitionTrends"));
            
            Map<String, Object> acquisitionTrends = (Map<String, Object>) acquisitionMetrics.get("acquisitionTrends");
            assertNotNull(acquisitionTrends);
            
            // The growthRate should be null when there's insufficient data, and this should not cause NPE
            Object growthRate = acquisitionTrends.get("growthRate");
            // growthRate can be null, and that's okay now
        });
    }

    @Test
    void testGenerateCustomerAnalyticsReport_WithMinimalData_ShouldHandleNullValues() {
        // Create a scenario with minimal data that might produce null values
        when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testSales);
        when(customerRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testCustomers.get(0))); // Only one customer
        when(customerRepository.findAllActive()).thenReturn(testCustomers);
        when(saleRepository.findAll()).thenReturn(testSales);

        assertDoesNotThrow(() -> {
            Map<String, Object> result = reportService.generateCustomerAnalyticsReport(false, 3);
            assertNotNull(result);
            
            // Verify that the result contains the expected structure even with minimal data
            Map<String, Object> acquisitionMetrics = (Map<String, Object>) result.get("acquisitionMetrics");
            assertNotNull(acquisitionMetrics);
            
            Map<String, Object> acquisitionTrends = (Map<String, Object>) acquisitionMetrics.get("acquisitionTrends");
            assertNotNull(acquisitionTrends);
            
            // Verify that null values are handled properly
            assertTrue(acquisitionTrends.containsKey("growthRate"));
            assertTrue(acquisitionTrends.containsKey("monthlyAcquisition"));
            assertTrue(acquisitionTrends.containsKey("totalNewCustomers"));
        });
    }
}
