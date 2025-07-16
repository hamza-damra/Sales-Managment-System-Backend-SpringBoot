package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PurchaseOrderDTO;
import com.hamza.salesmanagementbackend.dto.PurchaseOrderItemDTO;
import com.hamza.salesmanagementbackend.entity.Product;
import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
import com.hamza.salesmanagementbackend.entity.PurchaseOrderItem;
import com.hamza.salesmanagementbackend.entity.Supplier;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.ProductRepository;
import com.hamza.salesmanagementbackend.repository.PurchaseOrderRepository;
import com.hamza.salesmanagementbackend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    /**
     * Creates a new purchase order with validation
     */
    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO) {
        log.info("Creating purchase order for supplier ID: {}", purchaseOrderDTO.getSupplierId());

        // Validate supplier
        Supplier supplier = supplierRepository.findById(purchaseOrderDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + purchaseOrderDTO.getSupplierId()));

        if (!supplier.isActive()) {
            throw new BusinessLogicException("Cannot create order for inactive supplier");
        }

        // Validate items
        if (purchaseOrderDTO.getItems() == null || purchaseOrderDTO.getItems().isEmpty()) {
            throw new BusinessLogicException("At least one item is required");
        }

        // Create purchase order entity
        PurchaseOrder purchaseOrder = mapToEntity(purchaseOrderDTO, supplier);
        
        // Generate order number if not provided
        if (purchaseOrder.getOrderNumber() == null || purchaseOrder.getOrderNumber().trim().isEmpty()) {
            purchaseOrder.setOrderNumber(generateOrderNumber());
        }

        // Inherit terms from supplier if not provided
        if (purchaseOrder.getPaymentTerms() == null && supplier.getPaymentTerms() != null) {
            purchaseOrder.setPaymentTerms(supplier.getPaymentTerms());
        }
        if (purchaseOrder.getDeliveryTerms() == null && supplier.getDeliveryTerms() != null) {
            purchaseOrder.setDeliveryTerms(supplier.getDeliveryTerms());
        }

        // Create and validate items
        List<PurchaseOrderItem> items = createPurchaseOrderItems(purchaseOrder, purchaseOrderDTO.getItems());
        purchaseOrder.setItems(items);

        // Calculate totals
        purchaseOrder.calculateTotals();

        // Validate financial calculations
        validateFinancialCalculations(purchaseOrder, purchaseOrderDTO);

        // Save purchase order
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        // Update supplier statistics
        supplier.addOrder(savedOrder.getTotalAmount());
        supplierRepository.save(supplier);

        log.info("Purchase order created successfully with ID: {}", savedOrder.getId());
        return mapToDTO(savedOrder);
    }

    /**
     * Retrieves all purchase orders with pagination and filtering
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> getAllPurchaseOrders(Pageable pageable, 
                                                       PurchaseOrder.PurchaseOrderStatus status,
                                                       Long supplierId,
                                                       PurchaseOrder.OrderPriority priority,
                                                       LocalDateTime fromDate,
                                                       LocalDateTime toDate) {
        Page<PurchaseOrder> orders;

        if (status != null && supplierId != null) {
            orders = purchaseOrderRepository.findByStatusAndSupplierId(status, supplierId, pageable);
        } else if (status != null) {
            orders = purchaseOrderRepository.findByStatus(status, pageable);
        } else if (supplierId != null) {
            orders = purchaseOrderRepository.findBySupplierId(supplierId, pageable);
        } else if (fromDate != null && toDate != null) {
            orders = purchaseOrderRepository.findByOrderDateBetween(fromDate, toDate, pageable);
        } else {
            orders = purchaseOrderRepository.findAll(pageable);
        }

        return orders.map(this::mapToDTO);
    }

    /**
     * Retrieves a purchase order by ID with items
     */
    @Transactional(readOnly = true)
    public PurchaseOrderDTO getPurchaseOrderById(Long id) {
        if (id <= 0) {
            throw new BusinessLogicException("Invalid purchase order ID");
        }

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        return mapToDTO(purchaseOrder);
    }

    /**
     * Updates an existing purchase order
     */
    public PurchaseOrderDTO updatePurchaseOrder(Long id, PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrder existingOrder = purchaseOrderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        // Check if order can be modified
        if (!existingOrder.canBeModified()) {
            throw new BusinessLogicException("Cannot modify order with status: " + existingOrder.getStatus());
        }

        // Validate supplier if changed
        if (!existingOrder.getSupplier().getId().equals(purchaseOrderDTO.getSupplierId())) {
            Supplier newSupplier = supplierRepository.findById(purchaseOrderDTO.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + purchaseOrderDTO.getSupplierId()));
            
            if (!newSupplier.isActive()) {
                throw new BusinessLogicException("Cannot assign order to inactive supplier");
            }
            existingOrder.setSupplier(newSupplier);
        }

        // Update order fields
        updatePurchaseOrderFields(existingOrder, purchaseOrderDTO);

        // Update items if provided
        if (purchaseOrderDTO.getItems() != null) {
            // Remove existing items
            existingOrder.getItems().clear();
            
            // Add new items
            List<PurchaseOrderItem> newItems = createPurchaseOrderItems(existingOrder, purchaseOrderDTO.getItems());
            existingOrder.setItems(newItems);
        }

        // Recalculate totals
        existingOrder.calculateTotals();

        // Validate financial calculations
        validateFinancialCalculations(existingOrder, purchaseOrderDTO);

        PurchaseOrder savedOrder = purchaseOrderRepository.save(existingOrder);
        return mapToDTO(savedOrder);
    }

    /**
     * Deletes a purchase order
     */
    public void deletePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        // Only PENDING orders can be deleted
        if (purchaseOrder.getStatus() != PurchaseOrder.PurchaseOrderStatus.PENDING) {
            throw new BusinessLogicException("Cannot delete order with status: " + purchaseOrder.getStatus());
        }

        purchaseOrderRepository.delete(purchaseOrder);
        log.info("Purchase order deleted with ID: {}", id);
    }

    /**
     * Searches purchase orders
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrderDTO> searchPurchaseOrders(String query, Pageable pageable) {
        Page<PurchaseOrder> orders = purchaseOrderRepository.searchPurchaseOrders(query, pageable);
        return orders.map(this::mapToDTO);
    }

    /**
     * Updates purchase order status
     */
    public PurchaseOrderDTO updatePurchaseOrderStatus(Long id, PurchaseOrder.PurchaseOrderStatus newStatus, 
                                                     String notes, LocalDateTime actualDeliveryDate) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        // Validate status transition
        validateStatusTransition(purchaseOrder.getStatus(), newStatus);

        // Update status and related fields
        purchaseOrder.setStatus(newStatus);
        if (notes != null) {
            purchaseOrder.setNotes(notes);
        }
        if (actualDeliveryDate != null) {
            purchaseOrder.setActualDeliveryDate(actualDeliveryDate);
        }

        // Handle specific status changes
        switch (newStatus) {
            case PENDING:
                // No specific action needed for PENDING status
                // Status is already set above
                break;
            case APPROVED:
                // For APPROVED status, we need to set approval details
                // This should typically be done through the approvePurchaseOrder method
                // but we'll handle it here for direct status updates
                if (purchaseOrder.getStatus() == PurchaseOrder.PurchaseOrderStatus.PENDING) {
                    purchaseOrder.setApprovedDate(LocalDateTime.now());
                    // Note: approvedBy should be set by the caller if needed
                }
                break;
            case SENT:
                purchaseOrder.markAsSent();
                break;
            case DELIVERED:
                purchaseOrder.markAsDelivered();
                break;
            case CANCELLED:
                purchaseOrder.cancel();
                break;
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToDTO(savedOrder);
    }

    /**
     * Approves a purchase order
     */
    public PurchaseOrderDTO approvePurchaseOrder(Long id, String approvalNotes, String approvedBy) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with ID: " + id));

        if (purchaseOrder.getStatus() != PurchaseOrder.PurchaseOrderStatus.PENDING) {
            throw new BusinessLogicException("Only PENDING orders can be approved");
        }

        purchaseOrder.approve(approvedBy);
        if (approvalNotes != null) {
            purchaseOrder.setNotes(approvalNotes);
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        return mapToDTO(savedOrder);
    }

    // Private helper methods

    private String generateOrderNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = purchaseOrderRepository.countByOrderNumberStartingWith("PO-" + year);
        return String.format("PO-%s-%03d", year, count + 1);
    }

    private List<PurchaseOrderItem> createPurchaseOrderItems(PurchaseOrder purchaseOrder, List<PurchaseOrderItemDTO> itemDTOs) {
        return itemDTOs.stream().map(itemDTO -> {
            // Validate product
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));

            // Validate quantities and prices
            if (itemDTO.getQuantity() <= 0) {
                throw new BusinessLogicException("Quantity must be greater than 0");
            }
            if (itemDTO.getUnitCost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessLogicException("Unit cost must be greater than 0");
            }

            // Create item
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitCost(itemDTO.getUnitCost())
                    .taxPercentage(itemDTO.getTaxPercentage() != null ? itemDTO.getTaxPercentage() : BigDecimal.ZERO)
                    .discountPercentage(itemDTO.getDiscountPercentage() != null ? itemDTO.getDiscountPercentage() : BigDecimal.ZERO)
                    .notes(itemDTO.getNotes())
                    .build();

            // Calculate totals
            item.calculateTotals();

            // Validate total price if provided
            if (itemDTO.getTotalPrice() != null &&
                item.getTotalPrice().compareTo(itemDTO.getTotalPrice()) != 0) {
                throw new BusinessLogicException("Total price calculation mismatch for product: " + product.getName());
            }

            return item;
        }).collect(Collectors.toList());
    }

    private void validateFinancialCalculations(PurchaseOrder order, PurchaseOrderDTO dto) {
        // Validate subtotal
        if (dto.getSubtotal() != null && order.getSubtotal().compareTo(dto.getSubtotal()) != 0) {
            throw new BusinessLogicException("Subtotal calculation mismatch");
        }

        // Validate tax amount
        if (dto.getTaxAmount() != null && order.getTaxAmount().compareTo(dto.getTaxAmount()) != 0) {
            throw new BusinessLogicException("Tax amount calculation mismatch");
        }

        // Validate total amount
        if (dto.getTotalAmount() != null && order.getTotalAmount().compareTo(dto.getTotalAmount()) != 0) {
            throw new BusinessLogicException("Total amount calculation mismatch");
        }
    }

    private void validateStatusTransition(PurchaseOrder.PurchaseOrderStatus currentStatus,
                                        PurchaseOrder.PurchaseOrderStatus newStatus) {
        // Define valid transitions
        Map<PurchaseOrder.PurchaseOrderStatus, List<PurchaseOrder.PurchaseOrderStatus>> validTransitions = Map.of(
            PurchaseOrder.PurchaseOrderStatus.PENDING, List.of(PurchaseOrder.PurchaseOrderStatus.APPROVED, PurchaseOrder.PurchaseOrderStatus.CANCELLED),
            PurchaseOrder.PurchaseOrderStatus.APPROVED, List.of(PurchaseOrder.PurchaseOrderStatus.SENT, PurchaseOrder.PurchaseOrderStatus.CANCELLED),
            PurchaseOrder.PurchaseOrderStatus.SENT, List.of(PurchaseOrder.PurchaseOrderStatus.DELIVERED, PurchaseOrder.PurchaseOrderStatus.CANCELLED),
            PurchaseOrder.PurchaseOrderStatus.DELIVERED, List.of(PurchaseOrder.PurchaseOrderStatus.CANCELLED),
            PurchaseOrder.PurchaseOrderStatus.CANCELLED, List.of() // No transitions from cancelled
        );

        List<PurchaseOrder.PurchaseOrderStatus> allowedTransitions = validTransitions.get(currentStatus);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new BusinessLogicException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void updatePurchaseOrderFields(PurchaseOrder order, PurchaseOrderDTO dto) {
        if (dto.getExpectedDeliveryDate() != null) {
            order.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        }
        if (dto.getPriority() != null) {
            order.setPriority(dto.getPriority());
        }
        if (dto.getPaymentTerms() != null) {
            order.setPaymentTerms(dto.getPaymentTerms());
        }
        if (dto.getDeliveryTerms() != null) {
            order.setDeliveryTerms(dto.getDeliveryTerms());
        }
        if (dto.getShippingAddress() != null) {
            order.setShippingAddress(dto.getShippingAddress());
        }
        if (dto.getShippingCost() != null) {
            order.setShippingCost(dto.getShippingCost());
        }
        if (dto.getTaxRate() != null) {
            order.setTaxRate(dto.getTaxRate());
        }
        if (dto.getDiscountAmount() != null) {
            order.setDiscountAmount(dto.getDiscountAmount());
        }
        if (dto.getNotes() != null) {
            order.setNotes(dto.getNotes());
        }
    }

    private PurchaseOrder mapToEntity(PurchaseOrderDTO dto, Supplier supplier) {
        return PurchaseOrder.builder()
                .orderNumber(dto.getOrderNumber())
                .supplier(supplier)
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now())
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .status(dto.getStatus() != null ? dto.getStatus() : PurchaseOrder.PurchaseOrderStatus.PENDING)
                .priority(dto.getPriority() != null ? dto.getPriority() : PurchaseOrder.OrderPriority.NORMAL)
                .paymentTerms(dto.getPaymentTerms())
                .deliveryTerms(dto.getDeliveryTerms())
                .shippingAddress(dto.getShippingAddress())
                .shippingCost(dto.getShippingCost() != null ? dto.getShippingCost() : BigDecimal.ZERO)
                .taxRate(dto.getTaxRate() != null ? dto.getTaxRate() : 15.0)
                .discountAmount(dto.getDiscountAmount() != null ? dto.getDiscountAmount() : BigDecimal.ZERO)
                .notes(dto.getNotes())
                .createdBy(dto.getCreatedBy())
                .build();
    }

    private PurchaseOrderDTO mapToDTO(PurchaseOrder order) {
        PurchaseOrderDTO dto = PurchaseOrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .supplierId(order.getSupplier().getId())
                .supplierName(order.getSupplier().getName())
                .orderDate(order.getOrderDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .status(order.getStatus())
                .priority(order.getPriority())
                .paymentTerms(order.getPaymentTerms())
                .deliveryTerms(order.getDeliveryTerms())
                .shippingAddress(order.getShippingAddress())
                .shippingCost(order.getShippingCost())
                .taxRate(order.getTaxRate())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdBy(order.getCreatedBy())
                .approvedBy(order.getApprovedBy())
                .approvedDate(order.getApprovedDate())
                .sentDate(order.getSentDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();

        // Map items if present
        if (order.getItems() != null) {
            List<PurchaseOrderItemDTO> itemDTOs = order.getItems().stream()
                    .map(this::mapItemToDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        // Set computed fields
        dto.setItemsCount(order.getItemsCount());
        dto.setIsFullyReceived(order.isFullyReceived());
        dto.setReceivingProgress(order.getReceivingProgress());

        return dto;
    }

    private PurchaseOrderItemDTO mapItemToDTO(PurchaseOrderItem item) {
        return PurchaseOrderItemDTO.builder()
                .id(item.getId())
                .purchaseOrderId(item.getPurchaseOrder().getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productSku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .totalPrice(item.getTotalPrice())
                .receivedQuantity(item.getReceivedQuantity())
                .pendingQuantity(item.getPendingQuantity())
                .taxPercentage(item.getTaxPercentage())
                .taxAmount(item.getTaxAmount())
                .discountPercentage(item.getDiscountPercentage())
                .discountAmount(item.getDiscountAmount())
                .subtotal(item.getSubtotal())
                .notes(item.getNotes())
                .isFullyReceived(item.isFullyReceived())
                .isPartiallyReceived(item.getReceivedQuantity() != null && item.getReceivedQuantity() > 0 && !item.isFullyReceived())
                .remainingQuantity(item.getPendingQuantity())
                .build();
    }
}
