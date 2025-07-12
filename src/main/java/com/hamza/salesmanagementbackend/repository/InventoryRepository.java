package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByName(String name);

    Optional<Inventory> findByNameIgnoreCase(String name);

    Optional<Inventory> findByWarehouseCode(String warehouseCode);

    List<Inventory> findByStatus(Inventory.InventoryStatus status);

    Page<Inventory> findByStatus(Inventory.InventoryStatus status, Pageable pageable);

    List<Inventory> findByNameContainingIgnoreCase(String name);

    List<Inventory> findByLocation(String location);

    List<Inventory> findByLocationContainingIgnoreCase(String location);

    @Query("SELECT i FROM Inventory i WHERE i.isMainWarehouse = true")
    List<Inventory> findMainWarehouses();

    @Query("SELECT i FROM Inventory i WHERE i.isMainWarehouse = true AND i.status = :status")
    List<Inventory> findMainWarehousesByStatus(@Param("status") Inventory.InventoryStatus status);

    @Query("SELECT i FROM Inventory i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Inventory> searchInventories(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.inventory.id = :inventoryId")
    Long countCategoriesByInventoryId(@Param("inventoryId") Long inventoryId);

    @Query("SELECT i FROM Inventory i WHERE i.id NOT IN (SELECT DISTINCT c.inventory.id FROM Category c WHERE c.inventory IS NOT NULL)")
    List<Inventory> findEmptyInventories();

    @Query("SELECT i FROM Inventory i WHERE i.managerName = :managerName")
    List<Inventory> findByManagerName(@Param("managerName") String managerName);

    @Query("SELECT i FROM Inventory i WHERE i.managerEmail = :managerEmail")
    Optional<Inventory> findByManagerEmail(@Param("managerEmail") String managerEmail);

    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.status = :status")
    Long countByStatus(@Param("status") Inventory.InventoryStatus status);

    @Query("SELECT i FROM Inventory i WHERE i.length IS NOT NULL AND i.width IS NOT NULL AND i.height IS NOT NULL")
    List<Inventory> findInventoriesWithDimensions();

    @Query("SELECT i FROM Inventory i WHERE i.length IS NULL OR i.width IS NULL OR i.height IS NULL")
    List<Inventory> findInventoriesWithoutDimensions();

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByWarehouseCode(String warehouseCode);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i WHERE i.isMainWarehouse = true AND i.id != :excludeId")
    boolean existsOtherMainWarehouse(@Param("excludeId") Long excludeId);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inventory i WHERE i.isMainWarehouse = true")
    boolean existsMainWarehouse();
}
