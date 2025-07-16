package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.AppliedPromotionDTO;
import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.dto.SaleDTO;
import com.hamza.salesmanagementbackend.dto.SaleItemDTO;
import com.hamza.salesmanagementbackend.entity.AppliedPromotion;
import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleItem;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.DataIntegrityException;
import com.hamza.salesmanagementbackend.exception.InsufficientStockException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.CustomerRepository;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.SaleRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PromotionApplicationService promotionApplicationService;
    private final PromotionService promotionService;

    public SaleService(SaleRepository saleRepository,
                      CustomerRepository customerRepository,
                      ProductRepository productRepository,
                      ProductService productService,
                      PromotionApplicationService promotionApplicationService,
                      PromotionService promotionService) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.promotionApplicationService = promotionApplicationService;
        this.promotionService = promotionService;
    }

    /**
     * Creates a new sale with items and calculates total using streams
     */
    public SaleDTO createSale(SaleDTO saleDTO) {
        validateSaleData(saleDTO);

        Customer customer = customerRepository.findById(saleDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + saleDTO.getCustomerId()));

        Sale sale = new Sale(customer);

        // Process sale items using streams
        List<SaleItem> saleItems = saleDTO.getItems().stream()
                .map(itemDTO -> createSaleItem(sale, itemDTO))
                .collect(Collectors.toList());

        sale.setItems(saleItems);

        // Calculate totals properly (this sets both subtotal and totalAmount)
        sale.calculateTotals();

        // Apply auto-applicable promotions
        applyAutoPromotions(sale);

        // Reduce stock for each product
        saleItems.forEach(item ->
                productService.reduceStock(item.getProduct().getId(), item.getQuantity()));

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Retrieves all sales using streams for DTO conversion
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getAllSales() {
        return saleRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all sales with pagination
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getAllSales(Pageable pageable) {
        return saleRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a sale by ID with items
     */
    @Transactional(readOnly = true)
    public SaleDTO getSaleById(Long id) {
        return saleRepository.findByIdWithItems(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    /**
     * Updates sale status
     */
    public SaleDTO updateSaleStatus(Long id, SaleStatus status) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        validateStatusTransition(sale.getStatus(), status);
        sale.setStatus(status);

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Gets sales by customer using streams for processing
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByCustomer(Long customerId) {
        return saleRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets sales by customer with pagination
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getSalesByCustomer(Long customerId, Pageable pageable) {
        return saleRepository.findByCustomerIdOrderBySaleDateDesc(customerId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets sales by status using streams
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByStatus(SaleStatus status) {
        return saleRepository.findByStatus(status)
                .stream()
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets sales by status with pagination
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getSalesByStatus(SaleStatus status, Pageable pageable) {
        return saleRepository.findByStatus(status, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets sales within date range using streams for processing
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets sales within date range with pagination
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return saleRepository.findBySaleDateBetween(startDate, endDate, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets high-value sales using streams for additional filtering
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getHighValueSales(BigDecimal minAmount) {
        return saleRepository.findHighValueSales(minAmount)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets sales analytics using streams for data processing
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate);

        return sales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        completedSales -> {
                            BigDecimal totalRevenue = completedSales.stream()
                                    .map(Sale::getTotalAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                            BigDecimal averageSaleAmount = completedSales.isEmpty() ?
                                    BigDecimal.ZERO :
                                    totalRevenue.divide(BigDecimal.valueOf(completedSales.size()), RoundingMode.HALF_UP);

                            Map<SaleStatus, Long> salesByStatus = sales.stream()
                                    .collect(Collectors.groupingBy(Sale::getStatus, Collectors.counting()));

                            Map<String, Long> topCustomers = completedSales.stream()
                                    .collect(Collectors.groupingBy(
                                            sale -> sale.getCustomer().getName(),
                                            Collectors.counting()
                                    ));

                            return Map.of(
                                    "totalSales", completedSales.size(),
                                    "totalRevenue", totalRevenue,
                                    "averageSaleAmount", averageSaleAmount,
                                    "salesByStatus", salesByStatus,
                                    "topCustomers", topCustomers
                            );
                        }
                ));
    }

    /**
     * Gets daily sales summary using streams
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getDailySalesSummary(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        sale -> sale.getSaleDate().toLocalDate().toString(),
                        Collectors.mapping(Sale::getTotalAmount,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    /**
     * Gets product performance from sales using streams
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProductPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .filter(sale -> sale.getStatus() == SaleStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<String, Integer> productQuantities = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.summingInt(SaleItem::getQuantity)
                ));

        Map<String, BigDecimal> productRevenue = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getName(),
                        Collectors.mapping(
                                item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return Map.of(
                "productQuantities", productQuantities,
                "productRevenue", productRevenue
        );
    }

    /**
     * Cancels a sale and restores inventory
     */
    public SaleDTO cancelSale(Long id) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessLogicException("Sale is already cancelled");
        }

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessLogicException("Cannot cancel completed sale");
        }

        // Restore inventory using streams
        sale.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        sale.setStatus(SaleStatus.CANCELLED);
        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    // Private helper methods

    private void validateSaleData(SaleDTO saleDTO) {
        if (saleDTO.getItems() == null || saleDTO.getItems().isEmpty()) {
            throw new BusinessLogicException("Sale must contain at least one item");
        }

        // Validate each item using streams
        saleDTO.getItems().stream()
                .filter(item -> item.getQuantity() <= 0)
                .findFirst()
                .ifPresent(item -> {
                    throw new BusinessLogicException("Item quantity must be greater than zero");
                });

        // Validate discount and tax percentages
        if (saleDTO.getDiscountPercentage() != null &&
            (saleDTO.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0 ||
             saleDTO.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new BusinessLogicException("Discount percentage must be between 0 and 100");
        }

        if (saleDTO.getTaxPercentage() != null && saleDTO.getTaxPercentage().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessLogicException("Tax percentage cannot be negative");
        }
    }

    private void validateStatusTransition(SaleStatus currentStatus, SaleStatus newStatus) {
        if (currentStatus == SaleStatus.COMPLETED && newStatus != SaleStatus.COMPLETED) {
            throw new BusinessLogicException("Cannot change status of completed sale");
        }

        if (currentStatus == SaleStatus.CANCELLED && newStatus != SaleStatus.CANCELLED) {
            throw new BusinessLogicException("Cannot change status of cancelled sale");
        }
    }

    private SaleItem createSaleItem(Sale sale, SaleItemDTO itemDTO) {
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

        if (product.getStockQuantity() < itemDTO.getQuantity()) {
            throw new InsufficientStockException(
                    product.getName(), product.getStockQuantity(), itemDTO.getQuantity()
            );
        }

        SaleItem saleItem = new SaleItem(sale, product, itemDTO.getQuantity(), product.getPrice());

        // Set enhanced attributes if provided
        if (itemDTO.getDiscountPercentage() != null) {
            saleItem.setDiscountPercentage(itemDTO.getDiscountPercentage());
        }
        if (itemDTO.getTaxPercentage() != null) {
            saleItem.setTaxPercentage(itemDTO.getTaxPercentage());
        }
        if (itemDTO.getSerialNumbers() != null) {
            saleItem.setSerialNumbers(itemDTO.getSerialNumbers());
        }
        if (itemDTO.getNotes() != null) {
            saleItem.setNotes(itemDTO.getNotes());
        }

        // Recalculate totals
        saleItem.calculateTotals();

        return saleItem;
    }

    private SaleDTO mapToDTO(Sale sale) {
        List<SaleItemDTO> itemDTOs = Optional.ofNullable(sale.getItems())
                .map(items -> items.stream()
                        .map(this::mapItemToDTO)
                        .collect(Collectors.toList()))
                .orElse(List.of());

        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setCustomerId(sale.getCustomer().getId());
        dto.setCustomerName(sale.getCustomer().getName());
        dto.setSaleNumber(sale.getSaleNumber());
        dto.setReferenceNumber(sale.getReferenceNumber());
        dto.setSaleDate(sale.getSaleDate());
        dto.setSubtotal(sale.getSubtotal());
        dto.setDiscountAmount(sale.getDiscountAmount());
        dto.setDiscountPercentage(sale.getDiscountPercentage());
        dto.setTaxAmount(sale.getTaxAmount());
        dto.setTaxPercentage(sale.getTaxPercentage());
        dto.setShippingCost(sale.getShippingCost());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setStatus(sale.getStatus());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setPaymentStatus(sale.getPaymentStatus());
        dto.setPaymentDate(sale.getPaymentDate());
        dto.setDueDate(sale.getDueDate());
        dto.setBillingAddress(sale.getBillingAddress());
        dto.setShippingAddress(sale.getShippingAddress());
        dto.setSalesPerson(sale.getSalesPerson());
        dto.setSalesChannel(sale.getSalesChannel());
        dto.setSaleType(sale.getSaleType());
        dto.setCurrency(sale.getCurrency());
        dto.setExchangeRate(sale.getExchangeRate());
        dto.setNotes(sale.getNotes());
        dto.setInternalNotes(sale.getInternalNotes());
        dto.setTermsAndConditions(sale.getTermsAndConditions());
        dto.setWarrantyInfo(sale.getWarrantyInfo());
        dto.setDeliveryDate(sale.getDeliveryDate());
        dto.setExpectedDeliveryDate(sale.getExpectedDeliveryDate());
        dto.setDeliveryStatus(sale.getDeliveryStatus());
        dto.setTrackingNumber(sale.getTrackingNumber());
        dto.setIsGift(sale.getIsGift());
        dto.setGiftMessage(sale.getGiftMessage());
        dto.setLoyaltyPointsEarned(sale.getLoyaltyPointsEarned());
        dto.setLoyaltyPointsUsed(sale.getLoyaltyPointsUsed());
        dto.setIsReturn(sale.getIsReturn());
        dto.setOriginalSaleId(sale.getOriginalSaleId());
        dto.setReturnReason(sale.getReturnReason());
        dto.setProfitMargin(sale.getProfitMargin());
        dto.setCostOfGoodsSold(sale.getCostOfGoodsSold());
        dto.setItems(itemDTOs);
        dto.setCreatedAt(sale.getCreatedAt());
        dto.setUpdatedAt(sale.getUpdatedAt());

        // Map promotion-related fields
        dto.setPromotionId(sale.getPromotionId());
        dto.setCouponCode(sale.getCouponCode());
        dto.setOriginalTotal(sale.getOriginalTotal());
        dto.setFinalTotal(sale.getFinalTotal());
        dto.setPromotionDiscountAmount(sale.getPromotionDiscountAmount());

        // Map applied promotions
        if (sale.getAppliedPromotions() != null && !sale.getAppliedPromotions().isEmpty()) {
            List<AppliedPromotionDTO> appliedPromotionDTOs = sale.getAppliedPromotions().stream()
                    .map(this::mapAppliedPromotionToDTO)
                    .collect(Collectors.toList());
            dto.setAppliedPromotions(appliedPromotionDTOs);

            // Calculate computed fields
            dto.setTotalSavings(sale.getAppliedPromotions().stream()
                    .map(AppliedPromotion::getDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.setHasPromotions(true);
            dto.setPromotionCount(appliedPromotionDTOs.size());
        } else {
            dto.setAppliedPromotions(List.of());
            dto.setTotalSavings(BigDecimal.ZERO);
            dto.setHasPromotions(false);
            dto.setPromotionCount(0);
        }

        return dto;
    }

    private AppliedPromotionDTO mapAppliedPromotionToDTO(AppliedPromotion appliedPromotion) {
        AppliedPromotionDTO dto = new AppliedPromotionDTO();
        dto.setId(appliedPromotion.getId());
        dto.setSaleId(appliedPromotion.getSale().getId());
        dto.setPromotionId(appliedPromotion.getPromotion().getId());
        dto.setPromotionName(appliedPromotion.getPromotionName());
        dto.setPromotionType(appliedPromotion.getPromotionType());
        dto.setCouponCode(appliedPromotion.getCouponCode());
        dto.setDiscountAmount(appliedPromotion.getDiscountAmount());
        dto.setDiscountPercentage(appliedPromotion.getDiscountPercentage());
        dto.setOriginalAmount(appliedPromotion.getOriginalAmount());
        dto.setFinalAmount(appliedPromotion.getFinalAmount());
        dto.setIsAutoApplied(appliedPromotion.getIsAutoApplied());
        dto.setAppliedAt(appliedPromotion.getAppliedAt());

        // Note: Computed fields (displayText, typeDisplay, savingsAmount, isPercentageDiscount, isFixedAmountDiscount)
        // are automatically calculated by the DTO's getter methods and don't need to be set explicitly

        return dto;
    }

    private SaleItemDTO mapItemToDTO(SaleItem item) {
        SaleItemDTO dto = new SaleItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setOriginalUnitPrice(item.getOriginalUnitPrice());
        dto.setCostPrice(item.getCostPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setSubtotal(item.getSubtotal());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setSerialNumbers(item.getSerialNumbers());
        dto.setWarrantyInfo(item.getWarrantyInfo());
        dto.setNotes(item.getNotes());
        dto.setIsReturned(item.getIsReturned());
        dto.setReturnedQuantity(item.getReturnedQuantity());
        dto.setUnitOfMeasure(item.getUnitOfMeasure());
        return dto;
    }

    // New methods for enhanced sales management

    /**
     * Creates a comprehensive sale with enhanced features
     */
    public SaleDTO createComprehensiveSale(SaleDTO saleDTO) {
        validateSaleData(saleDTO);

        Customer customer = customerRepository.findById(saleDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + saleDTO.getCustomerId()));

        Sale sale = new Sale(customer);

        // Set enhanced sale attributes
        sale.setReferenceNumber(saleDTO.getReferenceNumber());
        sale.setDiscountPercentage(saleDTO.getDiscountPercentage() != null ? saleDTO.getDiscountPercentage() : BigDecimal.ZERO);
        sale.setTaxPercentage(saleDTO.getTaxPercentage() != null ? saleDTO.getTaxPercentage() : BigDecimal.ZERO);
        sale.setShippingCost(saleDTO.getShippingCost() != null ? saleDTO.getShippingCost() : BigDecimal.ZERO);
        sale.setPaymentMethod(saleDTO.getPaymentMethod());
        sale.setSaleType(saleDTO.getSaleType() != null ? saleDTO.getSaleType() : Sale.SaleType.RETAIL);
        sale.setBillingAddress(saleDTO.getBillingAddress() != null ? saleDTO.getBillingAddress() : customer.getBillingAddress());
        sale.setShippingAddress(saleDTO.getShippingAddress() != null ? saleDTO.getShippingAddress() : customer.getShippingAddress());
        sale.setSalesPerson(saleDTO.getSalesPerson());
        sale.setSalesChannel(saleDTO.getSalesChannel());
        sale.setDueDate(saleDTO.getDueDate());
        sale.setNotes(saleDTO.getNotes());
        sale.setInternalNotes(saleDTO.getInternalNotes());
        sale.setIsGift(saleDTO.getIsGift() != null ? saleDTO.getIsGift() : false);
        sale.setGiftMessage(saleDTO.getGiftMessage());
        sale.setLoyaltyPointsUsed(saleDTO.getLoyaltyPointsUsed() != null ? saleDTO.getLoyaltyPointsUsed() : 0);

        // Process sale items using streams
        List<SaleItem> saleItems = saleDTO.getItems().stream()
                .map(itemDTO -> createSaleItem(sale, itemDTO))
                .collect(Collectors.toList());

        sale.setItems(saleItems);

        // Calculate totals using the enhanced calculation method
        sale.calculateTotals();

        // Process loyalty points
        sale.processLoyaltyPoints();

        // Reduce stock for each product
        saleItems.forEach(item -> {
            Product product = item.getProduct();
            product.updateSalesStats(item.getQuantity(), item.getSubtotal());
            productRepository.save(product);
        });

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Updates payment information
     */
    public SaleDTO updatePaymentInfo(Long id, Sale.PaymentMethod paymentMethod, Sale.PaymentStatus paymentStatus) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        sale.setPaymentMethod(paymentMethod);
        sale.setPaymentStatus(paymentStatus);

        if (paymentStatus == Sale.PaymentStatus.PAID) {
            sale.markAsPaid();
        }

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Updates delivery information
     */
    public SaleDTO updateDeliveryInfo(Long id, Sale.DeliveryStatus deliveryStatus, String trackingNumber) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        sale.setDeliveryStatus(deliveryStatus);
        sale.setTrackingNumber(trackingNumber);

        if (deliveryStatus == Sale.DeliveryStatus.DELIVERED) {
            sale.setDeliveryDate(LocalDateTime.now());
        }

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Gets sales by payment method using streams
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getSalesByPaymentMethod(Sale.PaymentMethod paymentMethod) {
        return saleRepository.findAll()
                .stream()
                .filter(sale -> sale.getPaymentMethod() != null && sale.getPaymentMethod() == paymentMethod)
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets overdue sales using streams
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getOverdueSales() {
        return saleRepository.findAll()
                .stream()
                .filter(Sale::isOverdue)
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s1.getDueDate().compareTo(s2.getDueDate()))
                .collect(Collectors.toList());
    }

    /**
     * Gets gift sales using streams
     */
    @Transactional(readOnly = true)
    public List<SaleDTO> getGiftSales() {
        return saleRepository.findAll()
                .stream()
                .filter(sale -> Boolean.TRUE.equals(sale.getIsGift()))
                .map(this::mapToDTO)
                .sorted((s1, s2) -> s2.getSaleDate().compareTo(s1.getSaleDate()))
                .collect(Collectors.toList());
    }

    /**
     * Processes a return for a sale item
     */
    @Transactional
    public SaleDTO processItemReturn(Long saleId, Long itemId, Integer returnQuantity, String returnReason) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        SaleItem item = sale.getItems().stream()
                .filter(saleItem -> saleItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sale item not found with id: " + itemId));

        if (returnQuantity > item.getAvailableQuantityForReturn()) {
            throw new BusinessLogicException("Return quantity exceeds available quantity for return");
        }

        // Process the return
        item.processReturn(returnQuantity);

        // Restore inventory
        Product product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + returnQuantity);
        productRepository.save(product);

        // Update sale if this is a return
        sale.setReturnReason(returnReason);

        // Recalculate totals
        sale.calculateTotals();

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Updates sale information
     */
    public SaleDTO updateSale(Long id, SaleDTO saleDTO) {
        Sale existingSale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        // Only allow updates if sale is not completed or cancelled
        if (existingSale.getStatus() == SaleStatus.COMPLETED || existingSale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessLogicException("Cannot update completed or cancelled sales");
        }

        // Update basic fields
        Optional.ofNullable(saleDTO.getReferenceNumber()).ifPresent(existingSale::setReferenceNumber);
        Optional.ofNullable(saleDTO.getDiscountPercentage()).ifPresent(existingSale::setDiscountPercentage);
        Optional.ofNullable(saleDTO.getTaxPercentage()).ifPresent(existingSale::setTaxPercentage);
        Optional.ofNullable(saleDTO.getShippingCost()).ifPresent(existingSale::setShippingCost);
        Optional.ofNullable(saleDTO.getPaymentMethod()).ifPresent(existingSale::setPaymentMethod);
        Optional.ofNullable(saleDTO.getSaleType()).ifPresent(existingSale::setSaleType);
        Optional.ofNullable(saleDTO.getBillingAddress()).ifPresent(existingSale::setBillingAddress);
        Optional.ofNullable(saleDTO.getShippingAddress()).ifPresent(existingSale::setShippingAddress);
        Optional.ofNullable(saleDTO.getSalesPerson()).ifPresent(existingSale::setSalesPerson);
        Optional.ofNullable(saleDTO.getSalesChannel()).ifPresent(existingSale::setSalesChannel);
        Optional.ofNullable(saleDTO.getNotes()).ifPresent(existingSale::setNotes);
        Optional.ofNullable(saleDTO.getInternalNotes()).ifPresent(existingSale::setInternalNotes);

        // Recalculate totals
        existingSale.calculateTotals();

        Sale savedSale = saleRepository.save(existingSale);
        return mapToDTO(savedSale);
    }

    /**
     * Deletes a sale (soft delete by setting status to CANCELLED)
     */
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessLogicException("Cannot delete completed sales");
        }

        // Check for associated returns
        Long returnCount = saleRepository.countReturnsBySaleId(id);
        if (returnCount > 0) {
            throw DataIntegrityException.saleHasReturns(id, returnCount.intValue());
        }

        // Restore inventory if sale was pending
        if (sale.getStatus() == SaleStatus.PENDING && sale.getItems() != null) {
            sale.getItems().forEach(item -> {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            });
        }

        sale.setStatus(SaleStatus.CANCELLED);
        saleRepository.save(sale);
    }

    /**
     * Completes a sale
     */
    public SaleDTO completeSale(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessLogicException("Sale is already completed");
        }

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessLogicException("Cannot complete cancelled sale");
        }

        sale.setStatus(SaleStatus.COMPLETED);
        sale.processLoyaltyPoints();

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Creates a sale with promotion application
     */
    public SaleDTO createSaleWithPromotion(SaleDTO saleDTO, String couponCode) {
        validateSaleData(saleDTO);

        Customer customer = customerRepository.findById(saleDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + saleDTO.getCustomerId()));

        Sale sale = new Sale(customer);

        // Process sale items
        List<SaleItem> saleItems = saleDTO.getItems().stream()
                .map(itemDTO -> createSaleItem(sale, itemDTO))
                .collect(Collectors.toList());

        sale.setItems(saleItems);

        // Calculate initial totals
        sale.calculateTotals();

        // Apply promotion if coupon code provided
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            applyPromotionToSale(sale, couponCode, false);
        } else {
            // Apply auto-applicable promotions
            applyAutoPromotions(sale);
        }

        // Reduce stock for each product
        saleItems.forEach(item ->
                productService.reduceStock(item.getProduct().getId(), item.getQuantity()));

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Applies a promotion to an existing sale
     */
    public SaleDTO applyPromotionToExistingSale(Long saleId, String couponCode) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new BusinessLogicException("Can only apply promotions to pending sales");
        }

        applyPromotionToSale(sale, couponCode, false);

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Removes a promotion from an existing sale
     */
    public SaleDTO removePromotionFromSale(Long saleId, Long promotionId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new BusinessLogicException("Can only remove promotions from pending sales");
        }

        promotionApplicationService.removePromotionFromSale(sale, promotionId);

        Sale savedSale = saleRepository.save(sale);
        return mapToDTO(savedSale);
    }

    /**
     * Gets eligible promotions for a sale
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getEligiblePromotionsForSale(Long saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + saleId));

        BigDecimal orderAmount = sale.getSubtotal() != null ? sale.getSubtotal() : sale.getTotalAmount();

        return promotionApplicationService.findEligiblePromotions(
                sale.getCustomer(), sale.getItems(), orderAmount)
                .stream()
                .map(promotion -> promotionService.mapToDTO(promotion))
                .collect(Collectors.toList());
    }

    // Private helper methods for promotion integration

    private void applyPromotionToSale(Sale sale, String couponCode, boolean isAutoApplied) {
        BigDecimal orderAmount = sale.getSubtotal() != null ? sale.getSubtotal() : sale.getTotalAmount();

        Promotion promotion = promotionApplicationService.validateCouponCode(
                couponCode, sale.getCustomer(), sale.getItems(), orderAmount);

        promotionApplicationService.applyPromotionToSale(sale, promotion, isAutoApplied);
    }

    private void applyAutoPromotions(Sale sale) {
        BigDecimal orderAmount = sale.getSubtotal() != null ? sale.getSubtotal() : sale.getTotalAmount();

        log.debug("Applying auto promotions for sale with order amount: {}", orderAmount);

        List<Promotion> autoPromotions = promotionApplicationService.findAutoApplicablePromotions(
                sale.getCustomer(), sale.getItems(), orderAmount);

        log.debug("Found {} auto-applicable promotions", autoPromotions.size());

        for (Promotion promotion : autoPromotions) {
            try {
                log.debug("Attempting to apply auto-promotion: {} (ID: {})", promotion.getName(), promotion.getId());
                promotionApplicationService.applyPromotionToSale(sale, promotion, true);
                log.info("Auto-applied promotion {} to sale", promotion.getId());
            } catch (Exception e) {
                log.warn("Failed to auto-apply promotion {} to sale: {}", promotion.getId(), e.getMessage());
            }
        }
    }
}
