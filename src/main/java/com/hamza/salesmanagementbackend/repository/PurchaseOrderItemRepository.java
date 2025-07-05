package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {

    List<PurchaseOrderItem> findByPurchaseOrderId(Long purchaseOrderId);

    List<PurchaseOrderItem> findByProductId(Long productId);

    @Query("SELECT poi FROM PurchaseOrderItem poi JOIN poi.purchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrderItem> findByOrderDateBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT poi.product.id, poi.product.name, SUM(poi.quantity) as totalQuantity, SUM(poi.totalPrice) as totalValue " +
           "FROM PurchaseOrderItem poi JOIN poi.purchaseOrder po WHERE po.status IN ('RECEIVED', 'PARTIALLY_RECEIVED') " +
           "GROUP BY poi.product.id, poi.product.name ORDER BY totalQuantity DESC")
    List<Object[]> findTopPurchasedProducts();

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.receivedQuantity < poi.quantity")
    List<PurchaseOrderItem> findPendingItems();

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.receivedQuantity = poi.quantity")
    List<PurchaseOrderItem> findFullyReceivedItems();

    @Query("SELECT poi FROM PurchaseOrderItem poi WHERE poi.receivedQuantity > 0 AND poi.receivedQuantity < poi.quantity")
    List<PurchaseOrderItem> findPartiallyReceivedItems();

    @Query("SELECT SUM(poi.quantity - poi.receivedQuantity) FROM PurchaseOrderItem poi WHERE poi.product.id = :productId")
    Integer getTotalPendingQuantityForProduct(@Param("productId") Long productId);
}
