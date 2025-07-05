package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {

    List<ReturnItem> findByReturnEntityId(Long returnId);

    List<ReturnItem> findByProductId(Long productId);

    List<ReturnItem> findByOriginalSaleItemId(Long originalSaleItemId);

    @Query("SELECT ri FROM ReturnItem ri JOIN ri.returnEntity r WHERE r.returnDate BETWEEN :startDate AND :endDate")
    List<ReturnItem> findByReturnDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ri.product.id, ri.product.name, SUM(ri.returnQuantity) as totalReturned, SUM(ri.refundAmount) as totalRefunded " +
           "FROM ReturnItem ri JOIN ri.returnEntity r WHERE r.status = 'REFUNDED' " +
           "GROUP BY ri.product.id, ri.product.name ORDER BY totalReturned DESC")
    List<Object[]> findMostReturnedProducts();

    @Query("SELECT ri FROM ReturnItem ri WHERE ri.isRestockable = true AND ri.returnEntity.status = 'APPROVED'")
    List<ReturnItem> findRestockableItems();

    @Query("SELECT ri FROM ReturnItem ri WHERE ri.isRestockable = false")
    List<ReturnItem> findNonRestockableItems();

    @Query("SELECT ri FROM ReturnItem ri WHERE ri.itemCondition = :condition")
    List<ReturnItem> findByItemCondition(@Param("condition") ReturnItem.ItemCondition condition);

    @Query("SELECT SUM(ri.returnQuantity) FROM ReturnItem ri WHERE ri.product.id = :productId AND ri.returnEntity.status IN ('APPROVED', 'REFUNDED', 'EXCHANGED')")
    Integer getTotalReturnedQuantityForProduct(@Param("productId") Long productId);

    @Query("SELECT ri FROM ReturnItem ri WHERE ri.restockingFee > 0")
    List<ReturnItem> findItemsWithRestockingFee();

    @Query("SELECT ri.itemCondition, COUNT(ri) FROM ReturnItem ri WHERE ri.returnEntity.returnDate BETWEEN :startDate AND :endDate " +
           "GROUP BY ri.itemCondition ORDER BY COUNT(ri) DESC")
    List<Object[]> getItemConditionStatistics(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
}
