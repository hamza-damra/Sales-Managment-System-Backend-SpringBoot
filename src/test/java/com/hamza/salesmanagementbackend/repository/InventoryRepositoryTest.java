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

import java.math.BigDecimal;
import java.time.LocalTime;
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
    private Inventory inventoryWithDimensions;
    private Inventory inventoryWithoutDimensions;

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
                .length(new BigDecimal("20.0"))
                .width(new BigDecimal("15.0"))
                .height(new BigDecimal("5.0"))
                .currentStockCount(50)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH001")
                .isMainWarehouse(false)
                .startWorkTime(LocalTime.of(9, 0))
                .endWorkTime(LocalTime.of(17, 0))
                .build();

        inactiveInventory = Inventory.builder()
                .name("Secondary Warehouse")
                .description("Secondary storage")
                .location("Uptown")
                .length(new BigDecimal("25.0"))
                .width(new BigDecimal("10.0"))
                .height(new BigDecimal("4.0"))
                .currentStockCount(30)
                .status(Inventory.InventoryStatus.INACTIVE)
                .warehouseCode("WH002")
                .isMainWarehouse(false)
                .startWorkTime(LocalTime.of(8, 0))
                .endWorkTime(LocalTime.of(16, 0))
                .build();

        mainWarehouse = Inventory.builder()
                .name("Central Hub")
                .description("Main distribution center")
                .location("Central")
                .length(new BigDecimal("50.0"))
                .width(new BigDecimal("30.0"))
                .height(new BigDecimal("8.0"))
                .currentStockCount(250)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH003")
                .isMainWarehouse(true)
                .startWorkTime(LocalTime.of(7, 0))
                .endWorkTime(LocalTime.of(19, 0))
                .build();

        inventoryWithDimensions = Inventory.builder()
                .name("Small Storage")
                .description("Storage with complete dimensions")
                .location("East Side")
                .length(new BigDecimal("10.0"))
                .width(new BigDecimal("8.0"))
                .height(new BigDecimal("3.0"))
                .currentStockCount(45)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH004")
                .isMainWarehouse(false)
                .startWorkTime(LocalTime.of(10, 0))
                .endWorkTime(LocalTime.of(18, 0))
                .build();

        inventoryWithoutDimensions = Inventory.builder()
                .name("Incomplete Storage")
                .description("Storage missing some dimensions")
                .location("West Side")
                .length(new BigDecimal("15.0"))
                // width and height are null
                .currentStockCount(100)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH005")
                .isMainWarehouse(false)
                .startWorkTime(LocalTime.of(6, 0))
                .endWorkTime(LocalTime.of(14, 0))
                .build();

        // Persist test data
        entityManager.persistAndFlush(activeInventory);
        entityManager.persistAndFlush(inactiveInventory);
        entityManager.persistAndFlush(mainWarehouse);
        entityManager.persistAndFlush(inventoryWithDimensions);
        entityManager.persistAndFlush(inventoryWithoutDimensions);
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
        assertThat(result).hasSize(4); // activeInventory, mainWarehouse, inventoryWithDimensions, inventoryWithoutDimensions
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
    void findInventoriesWithDimensions_ShouldReturnInventoriesWithAllDimensions() {
        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithDimensions();

        // Then
        assertThat(result).hasSize(3); // activeInventory, inactiveInventory, mainWarehouse, inventoryWithDimensions
        assertThat(result).allMatch(inventory ->
            inventory.getLength() != null &&
            inventory.getWidth() != null &&
            inventory.getHeight() != null);
    }

    @Test
    void findInventoriesWithoutDimensions_ShouldReturnInventoriesMissingDimensions() {
        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithoutDimensions();

        // Then
        assertThat(result).hasSize(1); // inventoryWithoutDimensions
        assertThat(result.get(0).getName()).isEqualTo("Incomplete Storage");
        assertThat(result.get(0).getWidth()).isNull();
        assertThat(result.get(0).getHeight()).isNull();
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
