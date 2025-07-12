package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to handle data migrations and fix data integrity issues
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Order(10) // Run after other initializers
public class DataMigrationService implements CommandLineRunner {

    private final SaleRepository saleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Running data migration checks...");
        fixNullPaymentMethods();
        log.info("Data migration completed successfully");
    }

    /**
     * Fix sales with null payment methods
     */
    @Transactional
    public void fixNullPaymentMethods() {
        log.info("Checking for sales with null payment methods...");
        
        List<Sale> salesWithNullPaymentMethod = saleRepository.findAll()
                .stream()
                .filter(sale -> sale.getPaymentMethod() == null)
                .collect(Collectors.toList());

        if (!salesWithNullPaymentMethod.isEmpty()) {
            log.warn("Found {} sales with null payment methods. Fixing...", salesWithNullPaymentMethod.size());
            
            salesWithNullPaymentMethod.forEach(sale -> {
                // Set default payment method based on sale type or other criteria
                Sale.PaymentMethod defaultMethod = determineDefaultPaymentMethod(sale);
                sale.setPaymentMethod(defaultMethod);
                log.debug("Updated sale {} with payment method: {}", sale.getSaleNumber(), defaultMethod);
            });
            
            saleRepository.saveAll(salesWithNullPaymentMethod);
            log.info("Successfully updated {} sales with default payment methods", salesWithNullPaymentMethod.size());
        } else {
            log.info("No sales with null payment methods found");
        }
    }

    /**
     * Determine appropriate default payment method based on sale characteristics
     */
    private Sale.PaymentMethod determineDefaultPaymentMethod(Sale sale) {
        // Logic to determine appropriate payment method
        if (sale.getSaleType() == Sale.SaleType.B2B || sale.getSaleType() == Sale.SaleType.WHOLESALE) {
            return Sale.PaymentMethod.NET_30;
        } else if (sale.getSaleType() == Sale.SaleType.ONLINE) {
            return Sale.PaymentMethod.CREDIT_CARD;
        } else {
            return Sale.PaymentMethod.CASH;
        }
    }
}
