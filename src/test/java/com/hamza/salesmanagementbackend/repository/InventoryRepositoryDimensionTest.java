package com.hamza.salesmanagementbackend.repository;

import com.hamza.salesmanagementbackend.entity.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused tests for dimension-related queries in InventoryRepository,
 * testing the new dimension-based functionality.
 */
@DataJpaTest
@ActiveProfiles("test")
public class InventoryRepositoryDimensionTest {

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
    void findInventoriesWithDimensions_ShouldReturnOnlyInventoriesWithAllDimensions() {
        // Given
        Inventory withAllDimensions = createInventoryWithDimensions("WithAll",
            new BigDecimal("10.0"), new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory withoutLength = createInventoryWithDimensions("WithoutLength",
            null, new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory withoutWidth = createInventoryWithDimensions("WithoutWidth",
            new BigDecimal("10.0"), null, new BigDecimal("3.0"));
        Inventory withoutHeight = createInventoryWithDimensions("WithoutHeight",
            new BigDecimal("10.0"), new BigDecimal("5.0"), null);

        entityManager.persistAndFlush(withAllDimensions);
        entityManager.persistAndFlush(withoutLength);
        entityManager.persistAndFlush(withoutWidth);
        entityManager.persistAndFlush(withoutHeight);

        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithDimensions();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("WithAll");
    }

    @Test
    void findInventoriesWithoutDimensions_ShouldReturnInventoriesMissingAnyDimension() {
        // Given
        Inventory withAllDimensions = createInventoryWithDimensions("WithAll",
            new BigDecimal("10.0"), new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory withoutLength = createInventoryWithDimensions("WithoutLength",
            null, new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory withoutWidth = createInventoryWithDimensions("WithoutWidth",
            new BigDecimal("10.0"), null, new BigDecimal("3.0"));
        Inventory withoutHeight = createInventoryWithDimensions("WithoutHeight",
            new BigDecimal("10.0"), new BigDecimal("5.0"), null);
        Inventory withoutAnyDimensions = createInventoryWithDimensions("WithoutAny",
            null, null, null);

        entityManager.persistAndFlush(withAllDimensions);
        entityManager.persistAndFlush(withoutLength);
        entityManager.persistAndFlush(withoutWidth);
        entityManager.persistAndFlush(withoutHeight);
        entityManager.persistAndFlush(withoutAnyDimensions);

        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithoutDimensions();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Inventory::getName)
            .containsExactlyInAnyOrder("WithoutLength", "WithoutWidth", "WithoutHeight", "WithoutAny");
    }

    @Test
    void findInventoriesWithDimensions_ShouldReturnEmptyList_WhenNoneHaveAllDimensions() {
        // Given
        Inventory withoutLength = createInventoryWithDimensions("WithoutLength",
            null, new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory withoutWidth = createInventoryWithDimensions("WithoutWidth",
            new BigDecimal("10.0"), null, new BigDecimal("3.0"));

        entityManager.persistAndFlush(withoutLength);
        entityManager.persistAndFlush(withoutWidth);

        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithDimensions();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findInventoriesWithoutDimensions_ShouldReturnEmptyList_WhenAllHaveAllDimensions() {
        // Given
        Inventory inventory1 = createInventoryWithDimensions("Inventory1",
            new BigDecimal("10.0"), new BigDecimal("5.0"), new BigDecimal("3.0"));
        Inventory inventory2 = createInventoryWithDimensions("Inventory2",
            new BigDecimal("15.0"), new BigDecimal("8.0"), new BigDecimal("4.0"));

        entityManager.persistAndFlush(inventory1);
        entityManager.persistAndFlush(inventory2);

        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithoutDimensions();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findInventoriesWithDimensions_ShouldHandleMixedStatuses() {
        // Given
        Inventory activeWithDimensions = createInventoryWithDimensions("ActiveWithDimensions",
            new BigDecimal("10.0"), new BigDecimal("5.0"), new BigDecimal("3.0"));
        activeWithDimensions.setStatus(Inventory.InventoryStatus.ACTIVE);

        Inventory inactiveWithDimensions = createInventoryWithDimensions("InactiveWithDimensions",
            new BigDecimal("15.0"), new BigDecimal("8.0"), new BigDecimal("4.0"));
        inactiveWithDimensions.setStatus(Inventory.InventoryStatus.INACTIVE);

        Inventory activeWithoutDimensions = createInventoryWithDimensions("ActiveWithoutDimensions",
            new BigDecimal("10.0"), null, new BigDecimal("3.0"));
        activeWithoutDimensions.setStatus(Inventory.InventoryStatus.ACTIVE);

        entityManager.persistAndFlush(activeWithDimensions);
        entityManager.persistAndFlush(inactiveWithDimensions);
        entityManager.persistAndFlush(activeWithoutDimensions);

        // When
        List<Inventory> result = inventoryRepository.findInventoriesWithDimensions();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Inventory::getName)
            .containsExactlyInAnyOrder("ActiveWithDimensions", "InactiveWithDimensions");
    }

    private Inventory createInventoryWithDimensions(String name, BigDecimal length, BigDecimal width, BigDecimal height) {
        return Inventory.builder()
                .name(name)
                .description("Test inventory: " + name)
                .location("Test Location")
                .length(length)
                .width(width)
                .height(height)
                .currentStockCount(0)
                .status(Inventory.InventoryStatus.ACTIVE)
                .warehouseCode("WH" + name.toUpperCase())
                .isMainWarehouse(false)
                .startWorkTime(LocalTime.of(9, 0))
                .endWorkTime(LocalTime.of(17, 0))
                .build();
    }
}
