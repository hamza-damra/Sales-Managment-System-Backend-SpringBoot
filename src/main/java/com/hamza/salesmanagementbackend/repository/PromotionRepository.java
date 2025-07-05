package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCouponCode(String couponCode);

    List<Promotion> findByIsActive(Boolean isActive);

    Page<Promotion> findByIsActive(Boolean isActive, Pageable pageable);

    List<Promotion> findByType(Promotion.PromotionType type);

    List<Promotion> findByCustomerEligibility(Promotion.CustomerEligibility customerEligibility);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND :currentDate BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND :currentDate BETWEEN p.startDate AND p.endDate AND (p.usageLimit IS NULL OR p.usageCount < p.usageLimit)")
    List<Promotion> findAvailablePromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE p.endDate < :currentDate")
    List<Promotion> findExpiredPromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE p.startDate > :currentDate")
    List<Promotion> findScheduledPromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE p.usageLimit IS NOT NULL AND p.usageCount >= p.usageLimit")
    List<Promotion> findUsageLimitReachedPromotions();

    @Query("SELECT p FROM Promotion p WHERE p.autoApply = true AND p.isActive = true AND :currentDate BETWEEN p.startDate AND p.endDate")
    List<Promotion> findAutoApplyPromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.couponCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Promotion> searchPromotions(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true")
    Long countActivePromotions();

    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.endDate < :currentDate")
    Long countExpiredPromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p.type, COUNT(p) FROM Promotion p WHERE p.isActive = true GROUP BY p.type")
    List<Object[]> getPromotionTypeStatistics();

    @Query("SELECT p FROM Promotion p WHERE p.endDate BETWEEN :startDate AND :endDate")
    List<Promotion> findPromotionsExpiringBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Promotion p WHERE :productId MEMBER OF p.applicableProducts")
    List<Promotion> findPromotionsForProduct(@Param("productId") Long productId);

    @Query("SELECT p FROM Promotion p WHERE :category MEMBER OF p.applicableCategories")
    List<Promotion> findPromotionsForCategory(@Param("category") String category);

    boolean existsByCouponCode(String couponCode);
}
