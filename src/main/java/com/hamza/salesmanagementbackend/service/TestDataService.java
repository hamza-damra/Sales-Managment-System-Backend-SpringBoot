package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.dto.ReturnItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to provide test data information for API testing
 */
@Service
public class TestDataService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    /**
     * Get all available test data for returns testing
     */
    public TestDataInfo getTestDataInfo() {
        TestDataInfo info = new TestDataInfo();
        
        // Get customers
        info.customers = customerRepository.findAll().stream()
                .map(c -> new CustomerInfo(c.getId(), c.getName(), c.getEmail()))
                .collect(Collectors.toList());
        
        // Get products
        info.products = productRepository.findAll().stream()
                .map(p -> new ProductInfo(p.getId(), p.getName(), p.getSku(), p.getPrice()))
                .collect(Collectors.toList());
        
        // Get sales with items
        info.sales = saleRepository.findAll().stream()
                .map(s -> {
                    SaleInfo saleInfo = new SaleInfo(s.getId(), s.getSaleNumber(), 
                            s.getCustomer().getId(), s.getCustomer().getName(), 
                            s.getStatus().toString(), s.getTotalAmount());
                    
                    // Get sale items
                    saleInfo.items = saleItemRepository.findBySaleId(s.getId()).stream()
                            .map(si -> new SaleItemInfo(si.getId(), si.getProduct().getId(), 
                                    si.getProduct().getName(), si.getQuantity(), si.getUnitPrice()))
                            .collect(Collectors.toList());
                    
                    return saleInfo;
                })
                .collect(Collectors.toList());
        
        return info;
    }

    /**
     * Generate a valid return request payload for testing
     */
    public ReturnDTO generateValidReturnRequest() {
        // Get the first completed sale
        Sale sale = saleRepository.findByStatus(SaleStatus.COMPLETED).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No completed sales found for testing"));
        
        // Get sale items
        List<SaleItem> saleItems = saleItemRepository.findBySaleId(sale.getId());
        if (saleItems.isEmpty()) {
            throw new RuntimeException("No sale items found for sale ID: " + sale.getId());
        }

        // Create return items for the first two sale items
        List<ReturnItemDTO> returnItems = saleItems.stream()
                .limit(2) // Only return first 2 items
                .map(saleItem -> ReturnItemDTO.builder()
                        .originalSaleItemId(saleItem.getId())
                        .productId(saleItem.getProduct().getId())
                        .returnQuantity(1) // Return 1 item
                        .originalUnitPrice(saleItem.getUnitPrice())
                        .refundAmount(saleItem.getUnitPrice()) // Full refund
                        .itemCondition(ReturnItem.ItemCondition.GOOD)
                        .conditionNotes("Item in good condition")
                        .isRestockable(true)
                        .build())
                .collect(Collectors.toList());

        // Calculate total refund amount
        BigDecimal totalRefund = returnItems.stream()
                .map(ReturnItemDTO::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ReturnDTO.builder()
                .returnNumber("RET-" + System.currentTimeMillis())
                .originalSaleId(sale.getId())
                .originalSaleNumber(sale.getSaleNumber())
                .customerId(sale.getCustomer().getId())
                .customerName(sale.getCustomer().getName())
                .returnDate(LocalDateTime.now())
                .reason(Return.ReturnReason.DAMAGED_IN_SHIPPING)
                .status(Return.ReturnStatus.PENDING)
                .totalRefundAmount(totalRefund)
                .notes("Test return request")
                .refundMethod(Return.RefundMethod.CASH)
                .items(returnItems)
                .build();
    }

    // Data classes for test information
    public static class TestDataInfo {
        public List<CustomerInfo> customers;
        public List<ProductInfo> products;
        public List<SaleInfo> sales;
    }

    public static class CustomerInfo {
        public Long id;
        public String name;
        public String email;

        public CustomerInfo(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
    }

    public static class ProductInfo {
        public Long id;
        public String name;
        public String sku;
        public BigDecimal price;

        public ProductInfo(Long id, String name, String sku, BigDecimal price) {
            this.id = id;
            this.name = name;
            this.sku = sku;
            this.price = price;
        }
    }

    public static class SaleInfo {
        public Long id;
        public String saleNumber;
        public Long customerId;
        public String customerName;
        public String status;
        public BigDecimal totalAmount;
        public List<SaleItemInfo> items;

        public SaleInfo(Long id, String saleNumber, Long customerId, String customerName, 
                       String status, BigDecimal totalAmount) {
            this.id = id;
            this.saleNumber = saleNumber;
            this.customerId = customerId;
            this.customerName = customerName;
            this.status = status;
            this.totalAmount = totalAmount;
        }
    }

    public static class SaleItemInfo {
        public Long id;
        public Long productId;
        public String productName;
        public Integer quantity;
        public BigDecimal unitPrice;

        public SaleItemInfo(Long id, Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
            this.id = id;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
