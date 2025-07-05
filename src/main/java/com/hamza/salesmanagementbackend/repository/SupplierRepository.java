package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Supplier;
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
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByEmail(String email);

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByStatus(Supplier.SupplierStatus status);

    Page<Supplier> findByStatus(Supplier.SupplierStatus status, Pageable pageable);

    List<Supplier> findByCity(String city);

    List<Supplier> findByCountry(String country);

    @Query("SELECT s FROM Supplier s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<Supplier> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.purchaseOrders WHERE s.id = :id")
    Optional<Supplier> findByIdWithPurchaseOrders(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.createdAt >= :date")
    Long countNewSuppliersSince(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT s FROM Supplier s WHERE s.rating >= :minRating ORDER BY s.rating DESC")
    List<Supplier> findTopRatedSuppliers(@Param("minRating") Double minRating);

    @Query("SELECT s FROM Supplier s WHERE s.totalAmount >= :minAmount ORDER BY s.totalAmount DESC")
    List<Supplier> findHighValueSuppliers(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT s FROM Supplier s WHERE s.lastOrderDate < :cutoffDate AND s.status = 'ACTIVE'")
    List<Supplier> findInactiveSuppliers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.status = :status")
    Long countByStatus(@Param("status") Supplier.SupplierStatus status);

    @Query("SELECT AVG(s.rating) FROM Supplier s WHERE s.status = 'ACTIVE'")
    Double getAverageRating();

    @Query("SELECT SUM(s.totalAmount) FROM Supplier s WHERE s.status = 'ACTIVE'")
    BigDecimal getTotalSupplierValue();

    boolean existsByEmail(String email);

    boolean existsByTaxNumber(String taxNumber);

    Optional<Supplier> findByTaxNumber(String taxNumber);
}
