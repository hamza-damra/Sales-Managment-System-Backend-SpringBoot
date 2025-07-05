package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Coupon;
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
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    List<Coupon> findByIsActive(Boolean isActive);

    Page<Coupon> findByIsActive(Boolean isActive, Pageable pageable);

    List<Coupon> findByType(Coupon.CouponType type);

    List<Coupon> findByCustomerEligibility(Coupon.CustomerEligibility customerEligibility);

    List<Coupon> findByCreatedBy(String createdBy);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND :currentDate BETWEEN c.startDate AND c.endDate")
    List<Coupon> findActiveCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND :currentDate BETWEEN c.startDate AND c.endDate AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    List<Coupon> findAvailableCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c FROM Coupon c WHERE c.endDate < :currentDate")
    List<Coupon> findExpiredCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c FROM Coupon c WHERE c.startDate > :currentDate")
    List<Coupon> findScheduledCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c FROM Coupon c WHERE c.usageLimit IS NOT NULL AND c.usageCount >= c.usageLimit")
    List<Coupon> findUsageLimitReachedCoupons();

    @Query("SELECT c FROM Coupon c WHERE c.isSingleUse = true")
    List<Coupon> findSingleUseCoupons();

    @Query("SELECT c FROM Coupon c WHERE " +
           "LOWER(c.couponCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Coupon> searchCoupons(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true")
    Long countActiveCoupons();

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.endDate < :currentDate")
    Long countExpiredCoupons(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT c.type, COUNT(c) FROM Coupon c WHERE c.isActive = true GROUP BY c.type")
    List<Object[]> getCouponTypeStatistics();

    @Query("SELECT c FROM Coupon c WHERE c.endDate BETWEEN :startDate AND :endDate")
    List<Coupon> findCouponsExpiringBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c FROM Coupon c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    List<Coupon> findCouponsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(c.usageCount) FROM Coupon c WHERE c.createdAt BETWEEN :startDate AND :endDate")
    Long getTotalUsageCountBetween(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    boolean existsByCouponCode(String couponCode);
}
