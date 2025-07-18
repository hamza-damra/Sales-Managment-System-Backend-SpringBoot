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

    // Enhanced financial reporting queries
    @Query("SELECT s FROM Sale s " +
           "LEFT JOIN FETCH s.items si " +
           "LEFT JOIN FETCH si.product p " +
           "LEFT JOIN FETCH p.category c " +
           "LEFT JOIN FETCH s.customer cu " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED'")
    List<Sale> findCompletedSalesWithDetailsForPeriod(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s.paymentMethod, " +
           "COUNT(s) as transactionCount, " +
           "SUM(s.totalAmount) as totalRevenue, " +
           "AVG(s.totalAmount) as avgTransactionValue " +
           "FROM Sale s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY s.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.name as categoryName, " +
           "COUNT(DISTINCT s.id) as salesCount, " +
           "SUM(si.quantity) as totalQuantitySold, " +
           "SUM(si.totalPrice) as totalRevenue, " +
           "SUM(si.costPrice * si.quantity) as totalCost, " +
           "AVG(si.unitPrice) as avgUnitPrice " +
           "FROM Sale s " +
           "JOIN s.items si " +
           "JOIN si.product p " +
           "JOIN p.category c " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY c.id, c.name " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getRevenueByCategoryForPeriod(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.name as productName, " +
           "p.sku as productSku, " +
           "c.name as categoryName, " +
           "SUM(si.quantity) as totalQuantitySold, " +
           "SUM(si.totalPrice) as totalRevenue, " +
           "SUM(si.costPrice * si.quantity) as totalCost, " +
           "AVG(si.unitPrice) as avgUnitPrice, " +
           "COUNT(DISTINCT s.id) as salesCount " +
           "FROM Sale s " +
           "JOIN s.items si " +
           "JOIN si.product p " +
           "JOIN p.category c " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY p.id, p.name, p.sku, c.name " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getRevenueByProductForPeriod(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    @Query("SELECT cu.id as customerId, " +
           "cu.name as customerName, " +
           "cu.customerType as customerType, " +
           "COUNT(s.id) as totalOrders, " +
           "SUM(s.totalAmount) as totalRevenue, " +
           "AVG(s.totalAmount) as avgOrderValue, " +
           "MAX(s.saleDate) as lastPurchaseDate " +
           "FROM Sale s " +
           "JOIN s.customer cu " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY cu.id, cu.name, cu.customerType " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> getCustomerRevenueAnalysisForPeriod(@Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DATE(s.saleDate) as saleDate, " +
           "COUNT(s.id) as dailySalesCount, " +
           "SUM(s.totalAmount) as dailyRevenue, " +
           "SUM(s.costOfGoodsSold) as dailyCost, " +
           "AVG(s.totalAmount) as avgOrderValue " +
           "FROM Sale s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED' " +
           "GROUP BY DATE(s.saleDate) " +
           "ORDER BY saleDate")
    List<Object[]> getDailyRevenueAnalysis(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
           "SUM(s.totalAmount) as totalRevenue, " +
           "SUM(s.costOfGoodsSold) as totalCost, " +
           "SUM(s.taxAmount) as totalTax, " +
           "SUM(s.discountAmount) as totalDiscounts, " +
           "SUM(s.shippingCost) as totalShipping, " +
           "COUNT(s.id) as totalTransactions, " +
           "COUNT(DISTINCT s.customer.id) as uniqueCustomers " +
           "FROM Sale s " +
           "WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "AND s.status = 'COMPLETED'")
    Object[] getFinancialSummaryForPeriod(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
