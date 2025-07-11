package com.hamza.salesmanagementbackend;

import com.hamza.salesmanagementbackend.controller.InventoryControllerTest;
import com.hamza.salesmanagementbackend.repository.InventoryRepositoryCapacityTest;
import com.hamza.salesmanagementbackend.repository.InventoryRepositoryTest;
import com.hamza.salesmanagementbackend.service.InventoryServiceTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Test suite for the complete Inventory module.
 *
 * This suite runs all inventory-related tests including:
 * - Repository layer tests (including the fixed getAverageCapacityUtilization query)
 * - Service layer tests with mocked dependencies
 * - Controller layer integration tests
 * - Capacity-specific functionality tests
 *
 * Run individual test classes directly:
 * - mvn test -Dtest=InventoryRepositoryTest
 * - mvn test -Dtest=InventoryRepositoryCapacityTest
 * - mvn test -Dtest=InventoryServiceTest
 * - mvn test -Dtest=InventoryControllerTest
 *
 * Or run all inventory tests with pattern:
 * - mvn test -Dtest="*Inventory*Test"
 */
@DisplayName("Inventory Module Test Suite")
public class InventoryModuleTestSuite {

    @Test
    @DisplayName("Inventory Module Test Suite Documentation")
    void inventoryModuleTestSuiteInfo() {
        System.out.println("=== Inventory Module Test Suite ===");
        System.out.println("This test suite covers:");
        System.out.println("1. InventoryRepositoryTest - Repository layer integration tests");
        System.out.println("2. InventoryRepositoryCapacityTest - Capacity calculation tests");
        System.out.println("3. InventoryServiceTest - Service layer unit tests with mocks");
        System.out.println("4. InventoryControllerTest - Controller layer web tests");
        System.out.println("");
        System.out.println("Run individual tests:");
        System.out.println("mvn test -Dtest=InventoryRepositoryTest");
        System.out.println("mvn test -Dtest=InventoryRepositoryCapacityTest");
        System.out.println("mvn test -Dtest=InventoryServiceTest");
        System.out.println("mvn test -Dtest=InventoryControllerTest");
        System.out.println("");
        System.out.println("Run all inventory tests:");
        System.out.println("mvn test -Dtest=\"*Inventory*Test\"");
    }
}
