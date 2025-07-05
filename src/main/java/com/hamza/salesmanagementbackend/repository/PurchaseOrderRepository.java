package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.PurchaseOrder;
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
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findBySupplierId(Long supplierId);

    Page<PurchaseOrder> findBySupplierId(Long supplierId, Pageable pageable);

    List<PurchaseOrder> findByStatus(PurchaseOrder.PurchaseOrderStatus status);

    Page<PurchaseOrder> findByStatus(PurchaseOrder.PurchaseOrderStatus status, Pageable pageable);

    List<PurchaseOrder> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<PurchaseOrder> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.items poi LEFT JOIN FETCH poi.product WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId AND po.status = :status")
    List<PurchaseOrder> findBySupplierIdAndStatus(@Param("supplierId") Long supplierId,
                                                 @Param("status") PurchaseOrder.PurchaseOrderStatus status);

    @Query("SELECT SUM(po.totalAmount) FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate AND po.status IN ('RECEIVED', 'PARTIALLY_RECEIVED')")
    BigDecimal calculateTotalPurchaseValueBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.orderDate >= :date AND po.status != 'CANCELLED'")
    Long countActivePurchaseOrdersSince(@Param("date") LocalDateTime date);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.totalAmount >= :minAmount ORDER BY po.totalAmount DESC")
    List<PurchaseOrder> findHighValuePurchaseOrders(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId ORDER BY po.orderDate DESC")
    Page<PurchaseOrder> findBySupplierIdOrderByOrderDateDesc(@Param("supplierId") Long supplierId, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDeliveryDate < :currentDate AND po.status IN ('ORDERED', 'PARTIALLY_RECEIVED')")
    List<PurchaseOrder> findOverduePurchaseOrders(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.expectedDeliveryDate BETWEEN :startDate AND :endDate AND po.status IN ('ORDERED', 'PARTIALLY_RECEIVED')")
    List<PurchaseOrder> findPurchaseOrdersDueInPeriod(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(po) FROM PurchaseOrder po WHERE po.status = :status")
    Long countByStatus(@Param("status") PurchaseOrder.PurchaseOrderStatus status);

    @Query("SELECT po.supplier.name, COUNT(po), SUM(po.totalAmount) FROM PurchaseOrder po " +
           "WHERE po.orderDate BETWEEN :startDate AND :endDate AND po.status != 'CANCELLED' " +
           "GROUP BY po.supplier.id, po.supplier.name ORDER BY SUM(po.totalAmount) DESC")
    List<Object[]> getSupplierPurchaseStatistics(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.createdBy = :userId ORDER BY po.orderDate DESC")
    Page<PurchaseOrder> findByCreatedByOrderByOrderDateDesc(@Param("userId") String userId, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
