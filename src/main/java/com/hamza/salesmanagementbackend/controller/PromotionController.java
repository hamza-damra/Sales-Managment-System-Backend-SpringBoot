package com.hamza.salesmanagementbackend.controller;


import com.hamza.salesmanagementbackend.dto.PromotionDTO;
import com.hamza.salesmanagementbackend.exception.ResourceNotFoundException;
import com.hamza.salesmanagementbackend.service.PromotionService;
import com.hamza.salesmanagementbackend.util.SortingUtils;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/promotions")
@CrossOrigin(origins = "*")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<Page<PromotionDTO>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive) {

        // Validate pagination and sorting parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createPromotionSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);

        Page<PromotionDTO> promotions;
        if (isActive != null) {
            promotions = promotionService.getPromotionsByStatus(isActive, pageable);
        } else {
            promotions = promotionService.getAllPromotions(pageable);
        }

        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDTO> getPromotionById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO promotion = promotionService.getPromotionById(id);
            return ResponseEntity.ok(promotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PromotionDTO> createPromotion(@Valid @RequestBody PromotionDTO promotionDTO) {
        PromotionDTO createdPromotion = promotionService.createPromotion(promotionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromotion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(@PathVariable Long id,
                                                       @Valid @RequestBody PromotionDTO promotionDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO updatedPromotion = promotionService.updatePromotion(id, promotionDTO);
            return ResponseEntity.ok(updatedPromotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePromotion(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PromotionDTO>> searchPromotions(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Validate parameters
        SortingUtils.PaginationParams paginationParams = SortingUtils.validatePaginationParams(page, size);
        Sort sort = SortingUtils.createPromotionSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(paginationParams.page, paginationParams.size, sort);
        
        Page<PromotionDTO> promotions = promotionService.searchPromotions(query, pageable);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        List<PromotionDTO> activePromotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(activePromotions);
    }

    @GetMapping("/available")
    public ResponseEntity<List<PromotionDTO>> getAvailablePromotions() {
        List<PromotionDTO> availablePromotions = promotionService.getAvailablePromotions();
        return ResponseEntity.ok(availablePromotions);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<PromotionDTO> activatePromotion(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO activatedPromotion = promotionService.activatePromotion(id);
            return ResponseEntity.ok(activatedPromotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<PromotionDTO> deactivatePromotion(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO deactivatedPromotion = promotionService.deactivatePromotion(id);
            return ResponseEntity.ok(deactivatedPromotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PromotionDTO>> getPromotionsForProduct(@PathVariable Long productId) {
        if (productId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<PromotionDTO> promotions = promotionService.getPromotionsForProduct(productId);
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<PromotionDTO>> getPromotionsForCategory(@PathVariable String category) {
        if (category == null || category.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<PromotionDTO> promotions = promotionService.getPromotionsForCategory(category.trim());
        return ResponseEntity.ok(promotions);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getPromotionAnalytics() {
        // This would typically call a service method that aggregates promotion data
        // For now, returning a placeholder response
        Map<String, Object> analytics = Map.of(
            "message", "Promotion analytics endpoint - implementation pending",
            "totalPromotions", 0,
            "activePromotions", 0,
            "expiredPromotions", 0,
            "totalUsage", 0,
            "averageDiscountValue", 0.0
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/coupon/{couponCode}")
    public ResponseEntity<PromotionDTO> getCouponByCode(@PathVariable String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO promotion = promotionService.validateCouponCode(couponCode.trim());
            return ResponseEntity.ok(promotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-coupon")
    public ResponseEntity<PromotionDTO> validateCoupon(@RequestParam String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PromotionDTO promotion = promotionService.validateCouponCode(couponCode.trim());
            return ResponseEntity.ok(promotion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyPromotion(@PathVariable Long id,
                                                             @RequestParam BigDecimal orderAmount) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        try {
            BigDecimal discountAmount = promotionService.calculateDiscount(id, orderAmount);
            Map<String, Object> response = new HashMap<>();
            response.put("discountAmount", discountAmount);
            response.put("orderAmount", orderAmount);
            response.put("finalAmount", orderAmount.subtract(discountAmount));
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
