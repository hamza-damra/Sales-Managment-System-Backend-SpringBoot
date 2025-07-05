package com.hamza.salesmanagementbackend.service;

import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.entity.Promotion;
import com.hamza.salesmanagementbackend.exception.BusinessLogicException;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    /**
     * Creates a new promotion
     */
    public PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        // Validate dates
        validatePromotionDates(promotionDTO.getStartDate(), promotionDTO.getEndDate());
        
        // Validate coupon code uniqueness if provided
        if (promotionDTO.getCouponCode() != null && !promotionDTO.getCouponCode().trim().isEmpty()) {
            validateCouponCodeUniqueness(promotionDTO.getCouponCode(), null);
        }

        Promotion promotion = mapToEntity(promotionDTO);
        
        // Generate coupon code if not provided but needed
        if (promotion.getCouponCode() == null && needsCouponCode(promotion)) {
            promotion.setCouponCode(generateCouponCode());
        }

        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    /**
     * Updates an existing promotion
     */
    public PromotionDTO updatePromotion(Long id, PromotionDTO promotionDTO) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        // Validate dates
        validatePromotionDates(promotionDTO.getStartDate(), promotionDTO.getEndDate());
        
        // Validate coupon code uniqueness if provided
        if (promotionDTO.getCouponCode() != null && !promotionDTO.getCouponCode().trim().isEmpty()) {
            validateCouponCodeUniqueness(promotionDTO.getCouponCode(), id);
        }

        // Update fields
        updatePromotionFields(existingPromotion, promotionDTO);
        existingPromotion = promotionRepository.save(existingPromotion);
        return mapToDTO(existingPromotion);
    }

    /**
     * Retrieves all promotions with pagination
     */
    @Transactional(readOnly = true)
    public Page<PromotionDTO> getAllPromotions(Pageable pageable) {
        return promotionRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Retrieves a promotion by ID
     */
    @Transactional(readOnly = true)
    public PromotionDTO getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));
    }

    /**
     * Deletes a promotion
     */
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        // Check if promotion is currently active
        if (promotion.isCurrentlyActive()) {
            throw new BusinessLogicException("Cannot delete an active promotion");
        }

        promotionRepository.delete(promotion);
    }

    /**
     * Searches promotions with pagination
     */
    @Transactional(readOnly = true)
    public Page<PromotionDTO> searchPromotions(String searchTerm, Pageable pageable) {
        return promotionRepository.searchPromotions(searchTerm, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets active promotions
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets available promotions (active and not usage limit reached)
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getAvailablePromotions() {
        return promotionRepository.findAvailablePromotions(LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Activates a promotion
     */
    public PromotionDTO activatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        promotion.activate();
        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    /**
     * Deactivates a promotion
     */
    public PromotionDTO deactivatePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + id));

        promotion.deactivate();
        promotion = promotionRepository.save(promotion);
        return mapToDTO(promotion);
    }

    /**
     * Gets promotions by status
     */
    @Transactional(readOnly = true)
    public Page<PromotionDTO> getPromotionsByStatus(Boolean isActive, Pageable pageable) {
        return promotionRepository.findByIsActive(isActive, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Gets promotions for a specific product
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getPromotionsForProduct(Long productId) {
        return promotionRepository.findPromotionsForProduct(productId)
                .stream()
                .filter(Promotion::isCurrentlyActive)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets promotions for a specific category
     */
    @Transactional(readOnly = true)
    public List<PromotionDTO> getPromotionsForCategory(String category) {
        return promotionRepository.findPromotionsForCategory(category)
                .stream()
                .filter(Promotion::isCurrentlyActive)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Validates a coupon code and returns the promotion if valid
     */
    @Transactional(readOnly = true)
    public PromotionDTO validateCouponCode(String couponCode) {
        Promotion promotion = promotionRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new BusinessLogicException("Invalid coupon code: " + couponCode));

        if (!promotion.isCurrentlyActive()) {
            throw new BusinessLogicException("Coupon code is not currently active: " + couponCode);
        }

        if (promotion.isUsageLimitReached()) {
            throw new BusinessLogicException("Coupon code usage limit has been reached: " + couponCode);
        }

        return mapToDTO(promotion);
    }

    /**
     * Calculates discount for a promotion and order amount
     */
    @Transactional(readOnly = true)
    public java.math.BigDecimal calculateDiscount(Long promotionId, java.math.BigDecimal orderAmount) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + promotionId));

        if (!promotion.isCurrentlyActive()) {
            throw new BusinessLogicException("Promotion is not currently active");
        }

        return promotion.calculateDiscount(orderAmount);
    }

    // Private helper methods

    private void validatePromotionDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessLogicException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessLogicException("Start date must be before end date");
        }
        
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("End date cannot be in the past");
        }
    }

    private void validateCouponCodeUniqueness(String couponCode, Long excludeId) {
        if (promotionRepository.existsByCouponCode(couponCode)) {
            // Additional check to exclude current promotion if updating
            if (excludeId != null) {
                Promotion existing = promotionRepository.findByCouponCode(couponCode).orElse(null);
                if (existing != null && existing.getId().equals(excludeId)) {
                    return; // Same promotion, same coupon code - OK
                }
            }
            throw new BusinessLogicException("Coupon code already exists: " + couponCode);
        }
    }

    private boolean needsCouponCode(Promotion promotion) {
        // Generate coupon code for non-auto-apply promotions
        return !promotion.getAutoApply();
    }

    private String generateCouponCode() {
        String prefix = "PROMO";
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return prefix + uuid;
    }

    private void updatePromotionFields(Promotion promotion, PromotionDTO promotionDTO) {
        promotion.setName(promotionDTO.getName());
        promotion.setDescription(promotionDTO.getDescription());
        promotion.setType(promotionDTO.getType());
        promotion.setDiscountValue(promotionDTO.getDiscountValue());
        promotion.setMinimumOrderAmount(promotionDTO.getMinimumOrderAmount());
        promotion.setMaximumDiscountAmount(promotionDTO.getMaximumDiscountAmount());
        promotion.setStartDate(promotionDTO.getStartDate());
        promotion.setEndDate(promotionDTO.getEndDate());
        promotion.setApplicableProducts(promotionDTO.getApplicableProducts());
        promotion.setApplicableCategories(promotionDTO.getApplicableCategories());
        promotion.setUsageLimit(promotionDTO.getUsageLimit());
        promotion.setCustomerEligibility(promotionDTO.getCustomerEligibility());
        promotion.setCouponCode(promotionDTO.getCouponCode());
        promotion.setAutoApply(promotionDTO.getAutoApply());
        promotion.setStackable(promotionDTO.getStackable());
        
        if (promotionDTO.getIsActive() != null) {
            promotion.setIsActive(promotionDTO.getIsActive());
        }
    }

    private Promotion mapToEntity(PromotionDTO dto) {
        return Promotion.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .type(dto.getType())
                .discountValue(dto.getDiscountValue())
                .minimumOrderAmount(dto.getMinimumOrderAmount())
                .maximumDiscountAmount(dto.getMaximumDiscountAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .applicableProducts(dto.getApplicableProducts())
                .applicableCategories(dto.getApplicableCategories())
                .usageLimit(dto.getUsageLimit())
                .usageCount(dto.getUsageCount() != null ? dto.getUsageCount() : 0)
                .customerEligibility(dto.getCustomerEligibility() != null ? 
                        dto.getCustomerEligibility() : Promotion.CustomerEligibility.ALL)
                .couponCode(dto.getCouponCode())
                .autoApply(dto.getAutoApply() != null ? dto.getAutoApply() : false)
                .stackable(dto.getStackable() != null ? dto.getStackable() : false)
                .build();
    }

    private PromotionDTO mapToDTO(Promotion promotion) {
        PromotionDTO dto = PromotionDTO.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .type(promotion.getType())
                .discountValue(promotion.getDiscountValue())
                .minimumOrderAmount(promotion.getMinimumOrderAmount())
                .maximumDiscountAmount(promotion.getMaximumDiscountAmount())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .applicableProducts(promotion.getApplicableProducts())
                .applicableCategories(promotion.getApplicableCategories())
                .usageLimit(promotion.getUsageLimit())
                .usageCount(promotion.getUsageCount())
                .customerEligibility(promotion.getCustomerEligibility())
                .couponCode(promotion.getCouponCode())
                .autoApply(promotion.getAutoApply())
                .stackable(promotion.getStackable())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();

        // Computed fields
        dto.setStatusDisplay(promotion.getStatusDisplay());
        dto.setTypeDisplay(promotion.getTypeDisplay());
        dto.setEligibilityDisplay(promotion.getEligibilityDisplay());
        dto.setIsCurrentlyActive(promotion.isCurrentlyActive());
        dto.setIsExpired(promotion.isExpired());
        dto.setIsNotYetStarted(promotion.isNotYetStarted());
        dto.setIsUsageLimitReached(promotion.isUsageLimitReached());
        dto.setDaysUntilExpiry(promotion.getDaysUntilExpiry());
        
        if (promotion.getUsageLimit() != null) {
            dto.setRemainingUsage(Math.max(0, promotion.getUsageLimit() - 
                    (promotion.getUsageCount() != null ? promotion.getUsageCount() : 0)));
            dto.setUsagePercentage(((double) (promotion.getUsageCount() != null ? promotion.getUsageCount() : 0) 
                    / promotion.getUsageLimit()) * 100.0);
        }

        return dto;
    }
}
