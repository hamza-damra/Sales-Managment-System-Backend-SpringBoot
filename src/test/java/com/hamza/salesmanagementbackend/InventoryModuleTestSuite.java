package com.hamza.salesmanagementbackend;

import com.hamza.salesmanagementbackend.controller.InventoryControllerTest;
import com.hamza.salesmanagementbackend.repository.InventoryRepositoryCapacityTest;
import com.hamza.salesmanagementbackend.repository.InventoryRepositoryTest;
import com.hamza.salesmanagementbackend.service.InventoryServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite for the complete Inventory module.
 * 
 * This suite runs all inventory-related tests including:
 * - Repository layer tests (including the fixed getAverageCapacityUtilization query)
 * - Service layer tests with mocked dependencies
 * - Controller layer integration tests
 * - Capacity-specific functionality tests
 * 
 * Run this suite to verify the complete inventory module functionality.
 */
@Suite
@SelectClasses({
    InventoryRepositoryTest.class,
    InventoryRepositoryCapacityTest.class,
    InventoryServiceTest.class,
    InventoryControllerTest.class
})
public class InventoryModuleTestSuite {
    // This class serves as a test suite runner
    // No implementation needed - annotations handle the configuration
}
