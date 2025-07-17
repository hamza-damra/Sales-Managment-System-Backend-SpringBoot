package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Product;
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
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByStockQuantityLessThan(Integer threshold);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT c.name FROM Product p JOIN p.category c WHERE c IS NOT NULL")
    List<String> findDistinctCategoryNames();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p LEFT JOIN p.category c WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity < :threshold")
    Long countLowStockProducts(@Param("threshold") Integer threshold);

    boolean existsBySku(String sku);

    @Query("SELECT COUNT(si) FROM SaleItem si WHERE si.product.id = :productId")
    Long countSaleItemsByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(ri) FROM ReturnItem ri WHERE ri.product.id = :productId")
    Long countReturnItemsByProductId(@Param("productId") Long productId);

    /**
     * Find products created after a specific date
     */
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :fromDate")
    Page<Product> findRecentProducts(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    /**
     * Find recent products by category ID
     */
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :fromDate AND p.category.id = :categoryId")
    Page<Product> findRecentProductsByCategoryId(@Param("fromDate") LocalDateTime fromDate,
                                                @Param("categoryId") Long categoryId,
                                                Pageable pageable);

    /**
     * Find recent products by category name
     */
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :fromDate AND p.category.name = :categoryName")
    Page<Product> findRecentProductsByCategoryName(@Param("fromDate") LocalDateTime fromDate,
                                                  @Param("categoryName") String categoryName,
                                                  Pageable pageable);

    /**
     * Count recent products
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdAt >= :fromDate")
    Long countRecentProducts(@Param("fromDate") LocalDateTime fromDate);

    // Inventory Summary Metrics Queries

    /**
     * Count products that have stock > 0
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0")
    Long countProductsInStock();

    /**
     * Count products that are out of stock (stockQuantity = 0)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0")
    Long countOutOfStockProducts();

    /**
     * Count products with low stock (stockQuantity <= reorderPoint)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.reorderPoint IS NOT NULL AND p.stockQuantity <= p.reorderPoint")
    Long countLowStockProducts();

    /**
     * Count products that need reordering (at or below reorder point and reorder point is set)
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.reorderPoint IS NOT NULL AND p.stockQuantity <= p.reorderPoint AND p.stockQuantity > 0")
    Long countProductsNeedingReorder();

    /**
     * Calculate total stock value (sum of price * stockQuantity for products with stock > 0)
     */
    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.stockQuantity > 0 AND p.price IS NOT NULL")
    BigDecimal calculateTotalStockValue();

    /**
     * Count total products in the system
     */
    @Query("SELECT COUNT(p) FROM Product p")
    Long countTotalProducts();

    /**
     * Get inventory summary with category filter
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0 AND p.category.id = :categoryId")
    Long countProductsInStockByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0 AND p.category.id = :categoryId")
    Long countOutOfStockProductsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.reorderPoint IS NOT NULL AND p.stockQuantity <= p.reorderPoint AND p.category.id = :categoryId")
    Long countLowStockProductsByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.stockQuantity > 0 AND p.price IS NOT NULL AND p.category.id = :categoryId")
    BigDecimal calculateTotalStockValueByCategory(@Param("categoryId") Long categoryId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countTotalProductsByCategory(@Param("categoryId") Long categoryId);

    /**
     * Get inventory summary with category name filter
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0 AND p.category.name = :categoryName")
    Long countProductsInStockByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity = 0 AND p.category.name = :categoryName")
    Long countOutOfStockProductsByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.reorderPoint IS NOT NULL AND p.stockQuantity <= p.reorderPoint AND p.category.name = :categoryName")
    Long countLowStockProductsByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT COALESCE(SUM(p.price * p.stockQuantity), 0) FROM Product p WHERE p.stockQuantity > 0 AND p.price IS NOT NULL AND p.category.name = :categoryName")
    BigDecimal calculateTotalStockValueByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.name = :categoryName")
    Long countTotalProductsByCategoryName(@Param("categoryName") String categoryName);
}
