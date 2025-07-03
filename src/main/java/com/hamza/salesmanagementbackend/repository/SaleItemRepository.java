package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    List<SaleItem> findBySaleId(Long saleId);

    List<SaleItem> findByProductId(Long productId);

    @Query("SELECT si FROM SaleItem si JOIN si.sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    List<SaleItem> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT si.product.id, si.product.name, SUM(si.quantity) as totalQuantity " +
           "FROM SaleItem si JOIN si.sale s WHERE s.status = 'COMPLETED' " +
           "GROUP BY si.product.id, si.product.name ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();
}
