package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.AppliedPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppliedPromotionRepository extends JpaRepository<AppliedPromotion, Long> {

    /**
     * Find all applied promotions for a specific sale
     */
    List<AppliedPromotion> findBySale_Id(Long saleId);

    /**
     * Find all applied promotions for a specific promotion
     */
    List<AppliedPromotion> findByPromotion_Id(Long promotionId);

    /**
     * Find applied promotions by sale and promotion
     */
    List<AppliedPromotion> findBySale_IdAndPromotion_Id(Long saleId, Long promotionId);

    /**
     * Find applied promotions within a date range
     */
    @Query("SELECT ap FROM AppliedPromotion ap WHERE ap.appliedAt BETWEEN :startDate AND :endDate")
    List<AppliedPromotion> findByAppliedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Find auto-applied promotions
     */
    List<AppliedPromotion> findByIsAutoApplied(Boolean isAutoApplied);

    /**
     * Count applied promotions for a specific promotion
     */
    long countByPromotion_Id(Long promotionId);

    /**
     * Find applied promotions by coupon code
     */
    List<AppliedPromotion> findByCouponCode(String couponCode);

    /**
     * Get promotion usage statistics
     */
    @Query("SELECT ap.promotion.id, COUNT(ap), SUM(ap.discountAmount) " +
           "FROM AppliedPromotion ap " +
           "WHERE ap.appliedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY ap.promotion.id")
    List<Object[]> getPromotionUsageStats(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
