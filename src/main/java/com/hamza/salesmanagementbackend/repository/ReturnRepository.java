package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Return;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {

    Optional<Return> findByReturnNumber(String returnNumber);

    List<Return> findByCustomerId(Long customerId);

    Page<Return> findByCustomerId(Long customerId, Pageable pageable);

    List<Return> findByOriginalSaleId(Long originalSaleId);

    List<Return> findByStatus(Return.ReturnStatus status);

    Page<Return> findByStatus(Return.ReturnStatus status, Pageable pageable);

    List<Return> findByReason(Return.ReturnReason reason);

    List<Return> findByReturnDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Return> findByReturnDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT r FROM Return r LEFT JOIN FETCH r.items ri LEFT JOIN FETCH ri.product WHERE r.id = :id")
    Optional<Return> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT r FROM Return r WHERE r.customer.id = :customerId AND r.status = :status")
    List<Return> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                         @Param("status") Return.ReturnStatus status);

    @Query("SELECT SUM(r.totalRefundAmount) FROM Return r WHERE r.returnDate BETWEEN :startDate AND :endDate AND r.status = 'REFUNDED'")
    BigDecimal calculateTotalRefundAmountBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.returnDate >= :date AND r.status != 'CANCELLED'")
    Long countActiveReturnsSince(@Param("date") LocalDateTime date);

    @Query("SELECT r FROM Return r WHERE r.totalRefundAmount >= :minAmount ORDER BY r.totalRefundAmount DESC")
    List<Return> findHighValueReturns(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT r FROM Return r WHERE r.customer.id = :customerId ORDER BY r.returnDate DESC")
    Page<Return> findByCustomerIdOrderByReturnDateDesc(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT r FROM Return r WHERE r.status = 'PENDING' AND r.returnDate < :cutoffDate")
    List<Return> findPendingReturnsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT r FROM Return r WHERE " +
           "LOWER(r.returnNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.customer.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Return> searchReturns(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.status = :status")
    Long countByStatus(@Param("status") Return.ReturnStatus status);

    @Query("SELECT r.reason, COUNT(r) FROM Return r WHERE r.returnDate BETWEEN :startDate AND :endDate " +
           "GROUP BY r.reason ORDER BY COUNT(r) DESC")
    List<Object[]> getReturnReasonStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Return r WHERE r.processedDate IS NOT NULL")
    List<Return> findProcessedReturns();

    @Query("SELECT r FROM Return r WHERE r.processedBy = :userId ORDER BY r.processedDate DESC")
    Page<Return> findByProcessedByOrderByProcessedDateDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT r FROM Return r WHERE r.refundMethod = :refundMethod AND r.status = 'REFUNDED'")
    List<Return> findByRefundMethod(@Param("refundMethod") Return.RefundMethod refundMethod);

    boolean existsByReturnNumber(String returnNumber);
}
