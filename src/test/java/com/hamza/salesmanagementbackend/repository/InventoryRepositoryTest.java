package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class InventoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Inventory activeInventory;
    private Inventory inactiveInventory;
    private Inventory mainWarehouse;
    private Inventory nearCapacityInventory;
    private Inventory fullInventory;

    @BeforeEach
    void setUp() {
        // Create test inventories
        activeInventory = Inventory.builder()
                .name("Main Warehouse")
                .description("Primary storage facility")
                .location("Downtown")
                .address("123 Main St")
                .managerName("John Manager")
                .managerEmail("john@example.com")
                .capacity(100)
                .currentStockCount(50)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH001")
                .isMainWarehouse(false)
                .build();

        inactiveInventory = Inventory.builder()
                .name("Secondary Warehouse")
                .description("Secondary storage")
                .location("Uptown")
                .capacity(200)
                .currentStockCount(30)
                .status(Inventory.InventoryStatus.INACTIVE)
                .warehouseCode("WH002")
                .isMainWarehouse(false)
                .build();

        mainWarehouse = Inventory.builder()
                .name("Central Hub")
                .description("Main distribution center")
                .location("Central")
                .capacity(500)
                .currentStockCount(250)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH003")
                .isMainWarehouse(true)
                .build();

        nearCapacityInventory = Inventory.builder()
                .name("Small Storage")
                .description("Small capacity storage")
                .location("East Side")
                .capacity(50)
                .currentStockCount(45) // 90% capacity
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH004")
                .isMainWarehouse(false)
                .build();

        fullInventory = Inventory.builder()
                .name("Full Storage")
                .description("At capacity storage")
                .location("West Side")
                .capacity(100)
                .currentStockCount(100) // 100% capacity
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH005")
                .isMainWarehouse(false)
                .build();

        // Persist test data
        entityManager.persistAndFlush(activeInventory);
        entityManager.persistAndFlush(inactiveInventory);
        entityManager.persistAndFlush(mainWarehouse);
        entityManager.persistAndFlush(nearCapacityInventory);
        entityManager.persistAndFlush(fullInventory);
    }

    @Test
    void findByName_ShouldReturnInventory_WhenNameExists() {
        // When
        Optional<Inventory> result = inventoryRepository.findByName("Main Warehouse");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
        assertThat(result.get().getLocation()).isEqualTo("Downtown");
    }

    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // When
        Optional<Inventory> result = inventoryRepository.findByName("Non-existent Warehouse");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByNameIgnoreCase_ShouldReturnInventory_WhenNameExistsWithDifferentCase() {
        // When
        Optional<Inventory> result = inventoryRepository.findByNameIgnoreCase("MAIN WAREHOUSE");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void findByWarehouseCode_ShouldReturnInventory_WhenCodeExists() {
        // When
        Optional<Inventory> result = inventoryRepository.findByWarehouseCode("WH001");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getWarehouseCode()).isEqualTo("WH001");
        assertThat(result.get().getName()).isEqualTo("Main Warehouse");
    }

    @Test
    void findByStatus_ShouldReturnActiveInventories() {
        // When
        List<Inventory> result = inventoryRepository.findByStatus(Inventory.InventoryStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(4); // activeInventory, mainWarehouse, nearCapacityInventory, fullInventory
        assertThat(result).allMatch(inventory -> inventory.getStatus() == Inventory.InventoryStatus.ACTIVE);
    }

    @Test
    void findByStatus_WithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Inventory> result = inventoryRepository.findByStatus(Inventory.InventoryStatus.ACTIVE, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    void findMainWarehouses_ShouldReturnOnlyMainWarehouses() {
        // When
        List<Inventory> result = inventoryRepository.findMainWarehouses();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Central Hub");
        assertThat(result.get(0).getIsMainWarehouse()).isTrue();
    }

    @Test
    void findMainWarehousesByStatus_ShouldReturnActiveMainWarehouses() {
        // When
        List<Inventory> result = inventoryRepository.findMainWarehousesByStatus(Inventory.InventoryStatus.ACTIVE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Central Hub");
        assertThat(result.get(0).getStatus()).isEqualTo(Inventory.InventoryStatus.ACTIVE);
    }

    @Test
    void searchInventories_ShouldReturnMatchingInventories() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Inventory> result = inventoryRepository.searchInventories("warehouse", pageable);

        // Then
        assertThat(result.getContent()).hasSize(2); // Main Warehouse and Secondary Warehouse
        assertThat(result.getContent()).allMatch(inventory -> 
            inventory.getName().toLowerCase().contains("warehouse") ||
            inventory.getDescription().toLowerCase().contains("warehouse"));
    }

    @Test
    void findInventoriesNearCapacity_ShouldReturnInventoriesAboveThreshold() {
        // When - Find inventories at 80% or more capacity
        List<Inventory> result = inventoryRepository.findInventoriesNearCapacity(80.0);

        // Then
        assertThat(result).hasSize(2); // nearCapacityInventory (90%) and fullInventory (100%)
        assertThat(result).allMatch(inventory -> {
            double utilization = (double) inventory.getCurrentStockCount() / inventory.getCapacity() * 100;
            return utilization >= 80.0;
        });
    }

    @Test
    void findFullInventories_ShouldReturnInventoriesAtCapacity() {
        // When
        List<Inventory> result = inventoryRepository.findFullInventories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Full Storage");
        assertThat(result.get(0).getCurrentStockCount()).isEqualTo(result.get(0).getCapacity());
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // When
        Long activeCount = inventoryRepository.countByStatus(Inventory.InventoryStatus.ACTIVE);
        Long inactiveCount = inventoryRepository.countByStatus(Inventory.InventoryStatus.INACTIVE);

        // Then
        assertThat(activeCount).isEqualTo(4);
        assertThat(inactiveCount).isEqualTo(1);
    }

    @Test
    void getAverageCapacityUtilization_ShouldCalculateCorrectAverage() {
        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Expected calculation:
        // activeInventory: 50/100 * 100 = 50%
        // mainWarehouse: 250/500 * 100 = 50%
        // nearCapacityInventory: 45/50 * 100 = 90%
        // fullInventory: 100/100 * 100 = 100%
        // Average: (50 + 50 + 90 + 100) / 4 = 72.5%
        assertThat(averageUtilization).isNotNull();
        assertThat(averageUtilization).isEqualTo(72.5);
    }

    @Test
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        // When
        boolean exists = inventoryRepository.existsByName("Main Warehouse");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_ShouldReturnFalse_WhenNameDoesNotExist() {
        // When
        boolean exists = inventoryRepository.existsByName("Non-existent Warehouse");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnTrue_WhenNameExistsWithDifferentCase() {
        // When
        boolean exists = inventoryRepository.existsByNameIgnoreCase("MAIN WAREHOUSE");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByWarehouseCode_ShouldReturnTrue_WhenCodeExists() {
        // When
        boolean exists = inventoryRepository.existsByWarehouseCode("WH001");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsMainWarehouse_ShouldReturnTrue_WhenMainWarehouseExists() {
        // When
        boolean exists = inventoryRepository.existsMainWarehouse();

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsOtherMainWarehouse_ShouldReturnFalse_WhenExcludingOnlyMainWarehouse() {
        // When
        boolean exists = inventoryRepository.existsOtherMainWarehouse(mainWarehouse.getId());

        // Then
        assertThat(exists).isFalse();
    }
}
