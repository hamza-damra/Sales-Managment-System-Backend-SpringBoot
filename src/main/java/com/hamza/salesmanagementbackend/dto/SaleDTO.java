package com.hamza.salesmanagementbackend.dto;

import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDTO {

    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private String customerName;
    private LocalDateTime saleDate;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount cannot be negative")
    private BigDecimal totalAmount;

    private SaleStatus status;

    @NotEmpty(message = "Sale must contain at least one item")
    @Valid
    private List<SaleItemDTO> items;

    // Enhanced attributes matching the entity
    private String saleNumber;
    private String referenceNumber;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private BigDecimal taxAmount;
    private BigDecimal taxPercentage;
    private BigDecimal shippingCost;
    private Sale.PaymentMethod paymentMethod;
    private Sale.PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private LocalDate dueDate;
    private String billingAddress;
    private String shippingAddress;
    private String salesPerson;
    private String salesChannel;
    private Sale.SaleType saleType;
    private String currency;
    private BigDecimal exchangeRate;
    private String notes;
    private String internalNotes;
    private String termsAndConditions;
    private String warrantyInfo;
    private LocalDateTime deliveryDate;
    private LocalDate expectedDeliveryDate;
    private Sale.DeliveryStatus deliveryStatus;
    private String trackingNumber;
    private Boolean isGift;
    private String giftMessage;
    private Integer loyaltyPointsEarned;
    private Integer loyaltyPointsUsed;
    private Boolean isReturn;
    private Long originalSaleId;
    private String returnReason;
    private BigDecimal profitMargin;
    private BigDecimal costOfGoodsSold;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Utility methods
    public BigDecimal getGrandTotal() {
        if (subtotal == null) return totalAmount != null ? totalAmount : BigDecimal.ZERO;

        BigDecimal total = subtotal;
        if (discountAmount != null) {
            total = total.subtract(discountAmount);
        }
        if (taxAmount != null) {
            total = total.add(taxAmount);
        }
        if (shippingCost != null) {
            total = total.add(shippingCost);
        }
        return total;
    }

    public boolean isPaid() {
        return paymentStatus == Sale.PaymentStatus.PAID;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) &&
                paymentStatus != Sale.PaymentStatus.PAID;
    }

    public BigDecimal getOutstandingAmount() {
        if (isPaid()) {
            return BigDecimal.ZERO;
        }
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    public int getTotalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(SaleItemDTO::getQuantity)
                .sum();
    }
}
