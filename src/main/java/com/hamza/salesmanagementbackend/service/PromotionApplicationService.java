package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.entity.*;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PromotionApplicationService {

    private final PromotionRepository promotionRepository;

    /**
     * Finds all eligible promotions for a sale
     */
    @Transactional(readOnly = true)
    public List<Promotion> findEligiblePromotions(Customer customer, List<SaleItem> saleItems, BigDecimal orderAmount) {
        log.debug("Finding eligible promotions for customer {} with order amount {}", 
                customer.getId(), orderAmount);

        List<Promotion> availablePromotions = promotionRepository.findAvailablePromotions(LocalDateTime.now());
        
        return availablePromotions.stream()
                .filter(promotion -> isPromotionEligible(promotion, customer, saleItems, orderAmount))
                .collect(Collectors.toList());
    }

    /**
     * Finds auto-applicable promotions for a sale
     */
    @Transactional(readOnly = true)
    public List<Promotion> findAutoApplicablePromotions(Customer customer, List<SaleItem> saleItems, BigDecimal orderAmount) {
        log.debug("Finding auto-applicable promotions for customer {} with order amount {}",
                customer.getId(), orderAmount);

        List<Promotion> eligiblePromotions = findEligiblePromotions(customer, saleItems, orderAmount);
        log.debug("Found {} eligible promotions", eligiblePromotions.size());

        List<Promotion> autoPromotions = eligiblePromotions.stream()
                .filter(promotion -> {
                    boolean isAutoApply = Boolean.TRUE.equals(promotion.getAutoApply());
                    log.debug("Promotion {} (ID: {}) autoApply: {}", promotion.getName(), promotion.getId(), isAutoApply);
                    return isAutoApply;
                })
                .collect(Collectors.toList());

        log.debug("Found {} auto-applicable promotions", autoPromotions.size());
        return autoPromotions;
    }

    /**
     * Validates if a promotion can be applied to a sale
     */
    public boolean validatePromotionForSale(Promotion promotion, Customer customer, List<SaleItem> saleItems, BigDecimal orderAmount) {
        try {
            return isPromotionEligible(promotion, customer, saleItems, orderAmount);
        } catch (Exception e) {
            log.warn("Promotion validation failed for promotion {} and customer {}: {}", 
                    promotion.getId(), customer.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Validates a coupon code and returns the promotion
     */
    @Transactional(readOnly = true)
    public Promotion validateCouponCode(String couponCode, Customer customer, List<SaleItem> saleItems, BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new BusinessLogicException("Invalid coupon code: " + couponCode));

        if (!promotion.isCurrentlyActive()) {
            throw new BusinessLogicException("Coupon code is not currently active: " + couponCode);
        }

        if (!isPromotionEligible(promotion, customer, saleItems, orderAmount)) {
            throw new BusinessLogicException("Coupon code is not applicable to this order: " + couponCode);
        }

        return promotion;
    }

    /**
     * Calculates the discount amount for a promotion applied to specific sale items
     */
    public BigDecimal calculatePromotionDiscount(Promotion promotion, List<SaleItem> saleItems, BigDecimal orderAmount) {
        if (!promotion.isCurrentlyActive()) {
            return BigDecimal.ZERO;
        }

        // Check minimum order amount (handle null case)
        BigDecimal minimumOrderAmount = promotion.getMinimumOrderAmount();
        if (minimumOrderAmount != null && orderAmount.compareTo(minimumOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        // Calculate applicable amount based on promotion scope
        BigDecimal applicableAmount = calculateApplicableAmount(promotion, saleItems);
        
        if (applicableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        switch (promotion.getType()) {
            case PERCENTAGE:
                discount = applicableAmount.multiply(promotion.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue();
                break;
            case FREE_SHIPPING:
                discount = BigDecimal.ZERO; // Handled separately in shipping calculation
                break;
            case BUY_X_GET_Y:
                discount = calculateBuyXGetYDiscount(promotion, saleItems);
                break;
            default:
                discount = BigDecimal.ZERO;
                break;
        }

        // Apply maximum discount limit if set
        BigDecimal maximumDiscountAmount = promotion.getMaximumDiscountAmount();
        if (maximumDiscountAmount != null && discount.compareTo(maximumDiscountAmount) > 0) {
            discount = maximumDiscountAmount;
        }

        // Ensure discount doesn't exceed applicable amount
        if (discount.compareTo(applicableAmount) > 0) {
            discount = applicableAmount;
        }

        return discount.max(BigDecimal.ZERO);
    }

    /**
     * Applies a promotion to a sale and creates an AppliedPromotion record
     */
    public AppliedPromotion applyPromotionToSale(Sale sale, Promotion promotion, boolean isAutoApplied) {
        log.info("Applying promotion {} to sale {}", promotion.getId(), sale.getId());

        // Calculate discount
        BigDecimal orderAmount = sale.getSubtotal() != null ? sale.getSubtotal() : sale.getTotalAmount();
        BigDecimal discountAmount = calculatePromotionDiscount(promotion, sale.getItems(), orderAmount);

        if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("Promotion does not provide any discount for this order");
        }

        // Create applied promotion record
        AppliedPromotion appliedPromotion = new AppliedPromotion(
                sale, promotion, discountAmount, orderAmount, isAutoApplied);

        // Add to sale's applied promotions
        if (sale.getAppliedPromotions() == null) {
            sale.setAppliedPromotions(new ArrayList<>());
        }
        sale.getAppliedPromotions().add(appliedPromotion);

        // Update sale totals
        updateSaleTotalsWithPromotions(sale);

        // Increment promotion usage count
        promotion.incrementUsageCount();

        log.info("Successfully applied promotion {} to sale {} with discount amount {}", 
                promotion.getId(), sale.getId(), discountAmount);

        return appliedPromotion;
    }

    /**
     * Removes a promotion from a sale
     */
    public void removePromotionFromSale(Sale sale, Long promotionId) {
        log.info("Removing promotion {} from sale {}", promotionId, sale.getId());

        if (sale.getAppliedPromotions() == null) {
            throw new BusinessLogicException("No promotions applied to this sale");
        }

        AppliedPromotion toRemove = sale.getAppliedPromotions().stream()
                .filter(ap -> ap.getPromotion().getId().equals(promotionId))
                .findFirst()
                .orElseThrow(() -> new BusinessLogicException("Promotion not found in this sale"));

        sale.getAppliedPromotions().remove(toRemove);

        // Update sale totals
        updateSaleTotalsWithPromotions(sale);

        // Decrement promotion usage count
        Promotion promotion = toRemove.getPromotion();
        promotion.decrementUsageCount();

        log.info("Successfully removed promotion {} from sale {}", promotionId, sale.getId());
    }

    /**
     * Updates sale totals based on applied promotions
     */
    public void updateSaleTotalsWithPromotions(Sale sale) {
        // Calculate original total (subtotal before promotions)
        BigDecimal originalTotal = sale.getSubtotal() != null ? sale.getSubtotal() : 
                calculateSubtotalFromItems(sale.getItems());

        // Calculate total promotion discount
        BigDecimal totalPromotionDiscount = BigDecimal.ZERO;
        if (sale.getAppliedPromotions() != null) {
            totalPromotionDiscount = sale.getAppliedPromotions().stream()
                    .map(AppliedPromotion::getDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Update sale fields
        sale.setOriginalTotal(originalTotal);
        sale.setPromotionDiscountAmount(totalPromotionDiscount);
        
        // Calculate final total: original - promotion discount + tax + shipping
        BigDecimal finalTotal = originalTotal.subtract(totalPromotionDiscount);
        if (sale.getTaxAmount() != null) {
            finalTotal = finalTotal.add(sale.getTaxAmount());
        }
        if (sale.getShippingCost() != null) {
            finalTotal = finalTotal.add(sale.getShippingCost());
        }
        
        sale.setFinalTotal(finalTotal);
        sale.setTotalAmount(finalTotal); // Keep totalAmount in sync
        
        // Update discount amount to include promotion discount
        BigDecimal totalDiscount = totalPromotionDiscount;
        if (sale.getDiscountAmount() != null) {
            totalDiscount = totalDiscount.add(sale.getDiscountAmount());
        }
        sale.setDiscountAmount(totalDiscount);
    }

    // Private helper methods

    private boolean isPromotionEligible(Promotion promotion, Customer customer, List<SaleItem> saleItems, BigDecimal orderAmount) {
        // Check if promotion is currently active
        if (!promotion.isCurrentlyActive()) {
            return false;
        }

        // Check customer eligibility
        if (!promotion.isApplicableToCustomer(customer)) {
            return false;
        }

        // Check minimum order amount (handle null case)
        BigDecimal minimumOrderAmount = promotion.getMinimumOrderAmount();
        if (minimumOrderAmount != null && orderAmount.compareTo(minimumOrderAmount) < 0) {
            return false;
        }

        // Check if promotion applies to any products in the sale
        if (!isPromotionApplicableToProducts(promotion, saleItems)) {
            return false;
        }

        return true;
    }

    private boolean isPromotionApplicableToProducts(Promotion promotion, List<SaleItem> saleItems) {
        // If no specific products or categories are defined, promotion applies to all
        if ((promotion.getApplicableProducts() == null || promotion.getApplicableProducts().isEmpty()) &&
            (promotion.getApplicableCategories() == null || promotion.getApplicableCategories().isEmpty())) {
            return true;
        }

        // Check if any sale item matches the promotion criteria
        return saleItems.stream().anyMatch(item -> {
            Product product = item.getProduct();
            
            // Check specific products
            if (promotion.getApplicableProducts() != null && 
                promotion.getApplicableProducts().contains(product.getId())) {
                return true;
            }
            
            // Check categories
            if (promotion.getApplicableCategories() != null && product.getCategory() != null &&
                promotion.getApplicableCategories().contains(product.getCategory().getName())) {
                return true;
            }
            
            return false;
        });
    }

    private BigDecimal calculateApplicableAmount(Promotion promotion, List<SaleItem> saleItems) {
        // If no specific products or categories, apply to all items
        if ((promotion.getApplicableProducts() == null || promotion.getApplicableProducts().isEmpty()) &&
            (promotion.getApplicableCategories() == null || promotion.getApplicableCategories().isEmpty())) {
            return saleItems.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // Calculate amount for applicable items only
        return saleItems.stream()
                .filter(item -> {
                    Product product = item.getProduct();
                    return (promotion.getApplicableProducts() != null && 
                            promotion.getApplicableProducts().contains(product.getId())) ||
                           (promotion.getApplicableCategories() != null && product.getCategory() != null &&
                            promotion.getApplicableCategories().contains(product.getCategory().getName()));
                })
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateBuyXGetYDiscount(Promotion promotion, List<SaleItem> saleItems) {
        // Simplified Buy X Get Y logic - can be enhanced based on specific requirements
        // For now, return zero as this requires more complex business rules
        log.warn("Buy X Get Y promotion calculation not yet implemented for promotion {}", promotion.getId());
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateSubtotalFromItems(List<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
