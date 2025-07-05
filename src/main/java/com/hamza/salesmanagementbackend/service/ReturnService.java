package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.ReturnDTO;
import com.hamza.salesmanagementbackend.dto.ReturnItemDTO;
import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReturnService {

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private ReturnItemRepository returnItemRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    private static final int DEFAULT_RETURN_POLICY_DAYS = 30;

    /**
     * Creates a new return request
     */
    public ReturnDTO createReturn(ReturnDTO returnDTO) {
        // Validate original sale exists
        Sale originalSale = saleRepository.findById(returnDTO.getOriginalSaleId())
                .orElseThrow(() -> new ResourceNotFoundException("Original sale not found with id: " + returnDTO.getOriginalSaleId()));

        // Validate customer exists
        Customer customer = customerRepository.findById(returnDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + returnDTO.getCustomerId()));

        // Validate return is within policy period
        if (!isWithinReturnPeriod(originalSale.getSaleDate())) {
            throw new BusinessLogicException("Return request is outside the allowed return period of " + DEFAULT_RETURN_POLICY_DAYS + " days");
        }

        // Create return entity
        Return returnEntity = mapToEntity(returnDTO);
        returnEntity.setOriginalSale(originalSale);
        returnEntity.setCustomer(customer);
        returnEntity.setReturnNumber(generateReturnNumber());

        // Validate and create return items
        if (returnDTO.getItems() != null && !returnDTO.getItems().isEmpty()) {
            final Return finalReturnEntity = returnEntity; // Make effectively final for lambda
            List<ReturnItem> returnItems = returnDTO.getItems().stream()
                    .map(itemDTO -> createReturnItem(finalReturnEntity, itemDTO))
                    .collect(Collectors.toList());
            returnEntity.setItems(returnItems);
        }

        // Calculate total refund amount
        returnEntity.calculateTotalRefundAmount();

        returnEntity = returnRepository.save(returnEntity);
        return mapToDTO(returnEntity);
    }

    /**
     * Updates an existing return
     */
    public ReturnDTO updateReturn(Long id, ReturnDTO returnDTO) {
        Return existingReturn = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));

        if (!existingReturn.canBeModified()) {
            throw new BusinessLogicException("Return cannot be modified in current status: " + existingReturn.getStatus());
        }

        // Update fields
        updateReturnFields(existingReturn, returnDTO);
        existingReturn = returnRepository.save(existingReturn);
        return mapToDTO(existingReturn);
    }

    /**
     * Retrieves all returns with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReturnDTO> getAllReturns(Pageable pageable) {
        return returnRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a return by ID
     */
    @Transactional(readOnly = true)
    public ReturnDTO getReturnById(Long id) {
        return returnRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));
    }

    /**
     * Deletes a return (only if pending)
     */
    public void deleteReturn(Long id) {
        Return returnEntity = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));

        if (!returnEntity.canBeModified()) {
            throw new BusinessLogicException("Cannot delete return in current status: " + returnEntity.getStatus());
        }

        returnRepository.delete(returnEntity);
    }

    /**
     * Searches returns with pagination
     */
    @Transactional(readOnly = true)
    public Page<ReturnDTO> searchReturns(String searchTerm, Pageable pageable) {
        return returnRepository.searchReturns(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets returns by status
     */
    @Transactional(readOnly = true)
    public Page<ReturnDTO> getReturnsByStatus(Return.ReturnStatus status, Pageable pageable) {
        return returnRepository.findByStatus(status, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Approves a return
     */
    public ReturnDTO approveReturn(Long id, String approvedBy) {
        Return returnEntity = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));

        if (!returnEntity.canBeProcessed()) {
            throw new BusinessLogicException("Return cannot be approved in current status: " + returnEntity.getStatus());
        }

        returnEntity.approve(approvedBy);
        returnEntity = returnRepository.save(returnEntity);
        return mapToDTO(returnEntity);
    }

    /**
     * Rejects a return
     */
    public ReturnDTO rejectReturn(Long id, String rejectedBy, String rejectionReason) {
        Return returnEntity = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));

        if (!returnEntity.canBeProcessed()) {
            throw new BusinessLogicException("Return cannot be rejected in current status: " + returnEntity.getStatus());
        }

        returnEntity.reject(rejectedBy, rejectionReason);
        returnEntity = returnRepository.save(returnEntity);
        return mapToDTO(returnEntity);
    }

    /**
     * Processes a refund
     */
    public ReturnDTO processRefund(Long id, Return.RefundMethod refundMethod, String refundReference) {
        Return returnEntity = returnRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));

        if (returnEntity.getStatus() != Return.ReturnStatus.APPROVED) {
            throw new BusinessLogicException("Return must be approved before processing refund");
        }

        returnEntity.processRefund(refundMethod, refundReference);

        // Process return items and update inventory if restockable
        if (returnEntity.getItems() != null) {
            for (ReturnItem item : returnEntity.getItems()) {
                item.markAsProcessed();
                
                // Restock if item is restockable
                if (item.canBeRestocked()) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getReturnQuantity());
                    productRepository.save(product);
                }
            }
        }

        returnEntity = returnRepository.save(returnEntity);
        return mapToDTO(returnEntity);
    }

    /**
     * Gets return with items
     */
    @Transactional(readOnly = true)
    public ReturnDTO getReturnWithItems(Long id) {
        return returnRepository.findByIdWithItems(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Return not found with id: " + id));
    }

    /**
     * Gets returns by customer
     */
    @Transactional(readOnly = true)
    public List<ReturnDTO> getReturnsByCustomer(Long customerId) {
        return returnRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private boolean isWithinReturnPeriod(LocalDateTime saleDate) {
        if (saleDate == null) {
            return false;
        }
        LocalDateTime cutoffDate = saleDate.plusDays(DEFAULT_RETURN_POLICY_DAYS);
        return LocalDateTime.now().isBefore(cutoffDate) || LocalDateTime.now().isEqual(cutoffDate);
    }

    private String generateReturnNumber() {
        String prefix = "RET";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + "-" + timestamp.substring(timestamp.length() - 6) + "-" + uuid;
    }

    private ReturnItem createReturnItem(Return returnEntity, ReturnItemDTO itemDTO) {
        // Validate original sale item
        SaleItem originalSaleItem = saleItemRepository.findById(itemDTO.getOriginalSaleItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Original sale item not found with id: " + itemDTO.getOriginalSaleItemId()));

        // Validate product
        Product product = productRepository.findById(itemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

        ReturnItem returnItem = ReturnItem.builder()
                .returnEntity(returnEntity)
                .originalSaleItem(originalSaleItem)
                .product(product)
                .returnQuantity(itemDTO.getReturnQuantity())
                .originalUnitPrice(itemDTO.getOriginalUnitPrice())
                .restockingFee(itemDTO.getRestockingFee() != null ? itemDTO.getRestockingFee() : BigDecimal.ZERO)
                .conditionNotes(itemDTO.getConditionNotes())
                .itemCondition(itemDTO.getItemCondition())
                .serialNumbers(itemDTO.getSerialNumbers())
                .isRestockable(itemDTO.getIsRestockable() != null ? itemDTO.getIsRestockable() : true)
                .disposalReason(itemDTO.getDisposalReason())
                .build();

        // Validate return quantity
        if (!returnItem.isValidReturnQuantity()) {
            throw new BusinessLogicException("Invalid return quantity for product: " + product.getName());
        }

        returnItem.calculateRefundAmount();
        return returnItem;
    }

    private void updateReturnFields(Return returnEntity, ReturnDTO returnDTO) {
        if (returnDTO.getReason() != null) {
            returnEntity.setReason(returnDTO.getReason());
        }
        if (returnDTO.getNotes() != null) {
            returnEntity.setNotes(returnDTO.getNotes());
        }
        if (returnDTO.getTotalRefundAmount() != null) {
            returnEntity.setTotalRefundAmount(returnDTO.getTotalRefundAmount());
        }
    }

    private Return mapToEntity(ReturnDTO dto) {
        return Return.builder()
                .reason(dto.getReason())
                .totalRefundAmount(dto.getTotalRefundAmount() != null ? dto.getTotalRefundAmount() : BigDecimal.ZERO)
                .notes(dto.getNotes())
                .build();
    }

    private ReturnDTO mapToDTO(Return returnEntity) {
        ReturnDTO dto = ReturnDTO.builder()
                .id(returnEntity.getId())
                .returnNumber(returnEntity.getReturnNumber())
                .originalSaleId(returnEntity.getOriginalSale() != null ? returnEntity.getOriginalSale().getId() : null)
                .originalSaleNumber(returnEntity.getOriginalSale() != null ? returnEntity.getOriginalSale().getSaleNumber() : null)
                .customerId(returnEntity.getCustomer() != null ? returnEntity.getCustomer().getId() : null)
                .customerName(returnEntity.getCustomer() != null ? returnEntity.getCustomer().getName() : null)
                .returnDate(returnEntity.getReturnDate())
                .reason(returnEntity.getReason())
                .status(returnEntity.getStatus())
                .totalRefundAmount(returnEntity.getTotalRefundAmount())
                .notes(returnEntity.getNotes())
                .processedBy(returnEntity.getProcessedBy())
                .processedDate(returnEntity.getProcessedDate())
                .refundMethod(returnEntity.getRefundMethod())
                .refundReference(returnEntity.getRefundReference())
                .refundDate(returnEntity.getRefundDate())
                .createdAt(returnEntity.getCreatedAt())
                .updatedAt(returnEntity.getUpdatedAt())
                .build();

        // Map return items
        if (returnEntity.getItems() != null) {
            List<ReturnItemDTO> itemDTOs = returnEntity.getItems().stream()
                    .map(this::mapReturnItemToDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        // Computed fields
        dto.setStatusDisplay(returnEntity.getStatusDisplay());
        dto.setReasonDisplay(returnEntity.getReasonDisplay());
        dto.setCanBeModified(returnEntity.canBeModified());
        dto.setCanBeProcessed(returnEntity.canBeProcessed());
        dto.setIsWithinReturnPeriod(returnEntity.isWithinReturnPeriod(DEFAULT_RETURN_POLICY_DAYS));
        dto.setTotalItems(returnEntity.getItems() != null ? returnEntity.getItems().size() : 0);
        dto.setRefundMethodDisplay(returnEntity.getRefundMethod() != null ? 
                returnEntity.getRefundMethod().name().replace("_", " ") : null);
        
        if (returnEntity.getReturnDate() != null) {
            dto.setDaysSinceReturn((int) ChronoUnit.DAYS.between(returnEntity.getReturnDate(), LocalDateTime.now()));
        }

        return dto;
    }

    private ReturnItemDTO mapReturnItemToDTO(ReturnItem item) {
        ReturnItemDTO dto = ReturnItemDTO.builder()
                .id(item.getId())
                .returnId(item.getReturnEntity() != null ? item.getReturnEntity().getId() : null)
                .originalSaleItemId(item.getOriginalSaleItem() != null ? item.getOriginalSaleItem().getId() : null)
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .productSku(item.getProduct() != null ? item.getProduct().getSku() : null)
                .returnQuantity(item.getReturnQuantity())
                .originalUnitPrice(item.getOriginalUnitPrice())
                .refundAmount(item.getRefundAmount())
                .restockingFee(item.getRestockingFee())
                .conditionNotes(item.getConditionNotes())
                .itemCondition(item.getItemCondition())
                .serialNumbers(item.getSerialNumbers())
                .isRestockable(item.getIsRestockable())
                .disposalReason(item.getDisposalReason())
                .build();

        // Computed fields
        dto.setConditionDisplay(item.getConditionDisplay());
        dto.setCanBeRestocked(item.canBeRestocked());
        dto.setNetRefundAmount(item.getNetRefundAmount());
        dto.setTotalOriginalValue(item.getTotalOriginalValue());
        dto.setIsValidReturnQuantity(item.isValidReturnQuantity());
        
        if (item.getOriginalSaleItem() != null) {
            dto.setOriginalSaleQuantity(item.getOriginalSaleItem().getQuantity());
            dto.setAlreadyReturnedQuantity(item.getOriginalSaleItem().getReturnedQuantity());
            dto.setMaxReturnableQuantity(item.getOriginalSaleItem().getQuantity() - 
                    (item.getOriginalSaleItem().getReturnedQuantity() != null ? 
                     item.getOriginalSaleItem().getReturnedQuantity() : 0));
        }

        return dto;
    }
}
