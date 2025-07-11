package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findByNameIgnoreCase(String name);

    List<Category> findByStatus(Category.CategoryStatus status);

    Page<Category> findByStatus(Category.CategoryStatus status, Pageable pageable);

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findAllOrderedByDisplayOrder();

    @Query("SELECT c FROM Category c WHERE c.status = :status ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findByStatusOrderedByDisplayOrder(@Param("status") Category.CategoryStatus status);

    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT p.category.id FROM Product p WHERE p.category IS NOT NULL)")
    List<Category> findEmptyCategories();

    List<Category> findByInventoryId(Long inventoryId);

    List<Category> findByInventoryIsNull();

    @Query("SELECT c FROM Category c WHERE c.inventory.id = :inventoryId AND c.status = :status")
    List<Category> findByInventoryIdAndStatus(@Param("inventoryId") Long inventoryId, @Param("status") Category.CategoryStatus status);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
