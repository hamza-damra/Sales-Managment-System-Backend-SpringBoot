package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for capacity-related queries in InventoryRepository,
 * especially testing the fixed getAverageCapacityUtilization() method.
 */
@DataJpaTest
@ActiveProfiles("test")
class InventoryRepositoryCapacityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        entityManager.clear();
    }

    @Test
    void getAverageCapacityUtilization_ShouldReturnNull_WhenNoActiveInventories() {
        // Given - Only inactive inventories
        Inventory inactiveInventory = createInventory("Inactive", 100, 50, Inventory.InventoryStatus.INACTIVE);
        entityManager.persistAndFlush(inactiveInventory);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        assertThat(averageUtilization).isNull();
    }

    @Test
    void getAverageCapacityUtilization_ShouldIgnoreInventoriesWithNullCapacity() {
        // Given
        Inventory withCapacity = createInventory("WithCapacity", 100, 50, Inventory.InventoryStatus.ACTIVE);
        Inventory withoutCapacity = createInventory("WithoutCapacity", null, 30, Inventory.InventoryStatus.ACTIVE);
        
        entityManager.persistAndFlush(withCapacity);
        entityManager.persistAndFlush(withoutCapacity);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Should only consider withCapacity: 50/100 * 100 = 50%
        assertThat(averageUtilization).isEqualTo(50.0);
    }

    @Test
    void getAverageCapacityUtilization_ShouldIgnoreInventoriesWithZeroCapacity() {
        // Given
        Inventory normalInventory = createInventory("Normal", 100, 75, Inventory.InventoryStatus.ACTIVE);
        Inventory zeroCapacityInventory = createInventory("ZeroCapacity", 0, 10, Inventory.InventoryStatus.ACTIVE);
        
        entityManager.persistAndFlush(normalInventory);
        entityManager.persistAndFlush(zeroCapacityInventory);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Should only consider normalInventory: 75/100 * 100 = 75%
        assertThat(averageUtilization).isEqualTo(75.0);
    }

    @Test
    void getAverageCapacityUtilization_ShouldCalculateCorrectAverage_WithMultipleInventories() {
        // Given
        Inventory inventory1 = createInventory("Inventory1", 100, 25, Inventory.InventoryStatus.ACTIVE); // 25%
        Inventory inventory2 = createInventory("Inventory2", 200, 100, Inventory.InventoryStatus.ACTIVE); // 50%
        Inventory inventory3 = createInventory("Inventory3", 50, 40, Inventory.InventoryStatus.ACTIVE); // 80%
        Inventory inventory4 = createInventory("Inventory4", 300, 300, Inventory.InventoryStatus.ACTIVE); // 100%
        
        entityManager.persistAndFlush(inventory1);
        entityManager.persistAndFlush(inventory2);
        entityManager.persistAndFlush(inventory3);
        entityManager.persistAndFlush(inventory4);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Average: (25 + 50 + 80 + 100) / 4 = 63.75%
        assertThat(averageUtilization).isEqualTo(63.75);
    }

    @Test
    void getAverageCapacityUtilization_ShouldHandleDecimalResults() {
        // Given
        Inventory inventory1 = createInventory("Inventory1", 3, 1, Inventory.InventoryStatus.ACTIVE); // 33.333...%
        Inventory inventory2 = createInventory("Inventory2", 3, 2, Inventory.InventoryStatus.ACTIVE); // 66.666...%
        
        entityManager.persistAndFlush(inventory1);
        entityManager.persistAndFlush(inventory2);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Average: (33.333... + 66.666...) / 2 = 50.0%
        assertThat(averageUtilization).isEqualTo(50.0);
    }

    @Test
    void getAverageCapacityUtilization_ShouldOnlyConsiderActiveInventories() {
        // Given
        Inventory activeInventory = createInventory("Active", 100, 80, Inventory.InventoryStatus.ACTIVE); // 80%
        Inventory inactiveInventory = createInventory("Inactive", 100, 20, Inventory.InventoryStatus.INACTIVE); // 20%
        Inventory archivedInventory = createInventory("Archived", 100, 10, Inventory.InventoryStatus.ARCHIVED); // 10%
        Inventory maintenanceInventory = createInventory("Maintenance", 100, 90, Inventory.InventoryStatus.MAINTENANCE); // 90%
        
        entityManager.persistAndFlush(activeInventory);
        entityManager.persistAndFlush(inactiveInventory);
        entityManager.persistAndFlush(archivedInventory);
        entityManager.persistAndFlush(maintenanceInventory);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Should only consider activeInventory: 80%
        assertThat(averageUtilization).isEqualTo(80.0);
    }

    @Test
    void findInventoriesNearCapacity_ShouldReturnCorrectInventories() {
        // Given
        Inventory lowUtilization = createInventory("Low", 100, 30, Inventory.InventoryStatus.ACTIVE); // 30%
        Inventory mediumUtilization = createInventory("Medium", 100, 70, Inventory.InventoryStatus.ACTIVE); // 70%
        Inventory highUtilization = createInventory("High", 100, 85, Inventory.InventoryStatus.ACTIVE); // 85%
        Inventory fullUtilization = createInventory("Full", 100, 100, Inventory.InventoryStatus.ACTIVE); // 100%
        
        entityManager.persistAndFlush(lowUtilization);
        entityManager.persistAndFlush(mediumUtilization);
        entityManager.persistAndFlush(highUtilization);
        entityManager.persistAndFlush(fullUtilization);

        // When
        List<Inventory> nearCapacity = inventoryRepository.findInventoriesNearCapacity(80.0);

        // Then
        assertThat(nearCapacity).hasSize(2);
        assertThat(nearCapacity).extracting(Inventory::getName)
                .containsExactlyInAnyOrder("High", "Full");
    }

    @Test
    void findFullInventories_ShouldReturnOnlyFullInventories() {
        // Given
        Inventory almostFull = createInventory("AlmostFull", 100, 99, Inventory.InventoryStatus.ACTIVE); // 99%
        Inventory exactlyFull = createInventory("ExactlyFull", 100, 100, Inventory.InventoryStatus.ACTIVE); // 100%
        Inventory overFull = createInventory("OverFull", 100, 105, Inventory.InventoryStatus.ACTIVE); // 105%
        
        entityManager.persistAndFlush(almostFull);
        entityManager.persistAndFlush(exactlyFull);
        entityManager.persistAndFlush(overFull);

        // When
        List<Inventory> fullInventories = inventoryRepository.findFullInventories();

        // Then
        assertThat(fullInventories).hasSize(2);
        assertThat(fullInventories).extracting(Inventory::getName)
                .containsExactlyInAnyOrder("ExactlyFull", "OverFull");
    }

    @Test
    void findInventoriesNearCapacity_ShouldIgnoreInventoriesWithoutCapacity() {
        // Given
        Inventory withCapacity = createInventory("WithCapacity", 100, 90, Inventory.InventoryStatus.ACTIVE); // 90%
        Inventory withoutCapacity = createInventory("WithoutCapacity", null, 50, Inventory.InventoryStatus.ACTIVE);
        
        entityManager.persistAndFlush(withCapacity);
        entityManager.persistAndFlush(withoutCapacity);

        // When
        List<Inventory> nearCapacity = inventoryRepository.findInventoriesNearCapacity(80.0);

        // Then
        assertThat(nearCapacity).hasSize(1);
        assertThat(nearCapacity.get(0).getName()).isEqualTo("WithCapacity");
    }

    @Test
    void getAverageCapacityUtilization_ShouldHandleZeroCurrentStock() {
        // Given
        Inventory emptyInventory = createInventory("Empty", 100, 0, Inventory.InventoryStatus.ACTIVE); // 0%
        Inventory halfFullInventory = createInventory("HalfFull", 100, 50, Inventory.InventoryStatus.ACTIVE); // 50%
        
        entityManager.persistAndFlush(emptyInventory);
        entityManager.persistAndFlush(halfFullInventory);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Average: (0 + 50) / 2 = 25%
        assertThat(averageUtilization).isEqualTo(25.0);
    }

    @Test
    void getAverageCapacityUtilization_ShouldHandleOverCapacityInventories() {
        // Given
        Inventory normalInventory = createInventory("Normal", 100, 50, Inventory.InventoryStatus.ACTIVE); // 50%
        Inventory overCapacityInventory = createInventory("OverCapacity", 100, 120, Inventory.InventoryStatus.ACTIVE); // 120%
        
        entityManager.persistAndFlush(normalInventory);
        entityManager.persistAndFlush(overCapacityInventory);

        // When
        Double averageUtilization = inventoryRepository.getAverageCapacityUtilization();

        // Then
        // Average: (50 + 120) / 2 = 85%
        assertThat(averageUtilization).isEqualTo(85.0);
    }

    private Inventory createInventory(String name, Integer capacity, Integer currentStock, Inventory.InventoryStatus status) {
        return Inventory.builder()
                .name(name)
                .description("Test inventory: " + name)
                .location("Test Location")
                .capacity(capacity)
                .currentStockCount(currentStock)
                .status(status)
                .warehouseCode("WH" + name.toUpperCase())
                .isMainWarehouse(false)
                .build();
    }
}
