package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Sale;
import com.hamza.salesmanagementbackend.entity.SaleStatus;
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
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findByStatus(SaleStatus status);

    Page<Sale> findByStatus(SaleStatus status, Pageable pageable);

    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.items si LEFT JOIN FETCH si.product WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId AND s.status = :status")
    List<Sale> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                       @Param("status") SaleStatus status);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.status = 'COMPLETED'")
    BigDecimal calculateTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :date AND s.status = 'COMPLETED'")
    Long countCompletedSalesSince(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Sale s WHERE s.totalAmount >= :minAmount ORDER BY s.totalAmount DESC")
    List<Sale> findHighValueSales(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId ORDER BY s.saleDate DESC")
    Page<Sale> findByCustomerIdOrderBySaleDateDesc(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT AVG(s.totalAmount) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateAverageSaleAmount(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Return r WHERE r.originalSale.id = :saleId")
    Long countReturnsBySaleId(@Param("saleId") Long saleId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.customer.id = :customerId")
    Long countSalesByCustomerId(@Param("customerId") Long customerId);
}
