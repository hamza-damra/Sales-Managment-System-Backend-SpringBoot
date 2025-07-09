# Inventory Module Test Documentation

This directory contains comprehensive unit and integration tests for the Inventory module of the Sales Management System.

## Test Structure

### 1. Repository Tests (`InventoryRepositoryTest.java`)
- **Type**: Integration tests using `@DataJpaTest`
- **Database**: H2 in-memory database
- **Coverage**: 
  - Custom query methods
  - Search and filtering functionality
  - Status-based queries
  - Main warehouse queries
  - Capacity-related queries
  - Existence checks

### 2. Repository Capacity Tests (`InventoryRepositoryCapacityTest.java`)
- **Type**: Focused integration tests for capacity functionality
- **Special Focus**: Tests the recently fixed `getAverageCapacityUtilization()` method
- **Coverage**:
  - Average capacity utilization calculations
  - Edge cases (null capacity, zero capacity, over-capacity)
  - Status filtering in capacity calculations
  - Near-capacity and full inventory detection

### 3. Service Tests (`InventoryServiceTest.java`)
- **Type**: Unit tests with mocked dependencies using `@ExtendWith(MockitoExtension.class)`
- **Mocked Dependencies**: `InventoryRepository`
- **Coverage**:
  - CRUD operations (Create, Read, Update, Delete)
  - Business logic validation
  - Exception handling scenarios
  - Main warehouse management logic
  - Capacity utilization calculations
  - Data mapping between entities and DTOs

### 4. Controller Tests (`InventoryControllerTest.java`)
- **Type**: Web layer integration tests using `@WebMvcTest`
- **Mocked Dependencies**: `InventoryService`
- **Coverage**:
  - HTTP request/response testing
  - Input validation testing
  - Error response handling
  - REST endpoint functionality
  - Parameter validation

## Key Test Scenarios

### Business Logic Tests
- ✅ Inventory name uniqueness validation
- ✅ Main warehouse constraint (only one allowed)
- ✅ Warehouse code uniqueness
- ✅ Required field validation
- ✅ Data integrity constraints

### Capacity Management Tests
- ✅ Capacity utilization calculation
- ✅ Near-capacity detection (80% threshold)
- ✅ Full inventory detection
- ✅ Average capacity utilization across active inventories
- ✅ Edge cases: null capacity, zero capacity, over-capacity

### Error Handling Tests
- ✅ `ResourceNotFoundException` scenarios
- ✅ `BusinessLogicException` scenarios
- ✅ `DataIntegrityException` scenarios
- ✅ Invalid input validation
- ✅ HTTP error status codes

### Fixed Query Tests
The `getAverageCapacityUtilization()` method was fixed to properly calculate capacity utilization:
- **Before**: `SELECT AVG(i.capacityUtilization) FROM Inventory i...` (field didn't exist)
- **After**: `SELECT AVG(CAST(i.currentStockCount AS DOUBLE) / i.capacity * 100) FROM Inventory i...`

Tests verify:
- Correct mathematical calculation
- Proper filtering (only active inventories with valid capacity)
- Handling of edge cases

## Running the Tests

### Run All Inventory Tests
```bash
mvn test -Dtest=InventoryModuleTestSuite
```

### Run Individual Test Classes
```bash
# Repository tests
mvn test -Dtest=InventoryRepositoryTest

# Capacity-specific tests
mvn test -Dtest=InventoryRepositoryCapacityTest

# Service tests
mvn test -Dtest=InventoryServiceTest

# Controller tests
mvn test -Dtest=InventoryControllerTest
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

## Test Configuration

### Test Properties (`application-test.properties`)
- H2 in-memory database configuration
- JPA/Hibernate test settings
- Logging configuration for debugging
- Flyway disabled (using JPA schema generation)

### Test Dependencies
- JUnit 5 (Jupiter)
- Mockito for mocking
- Spring Boot Test
- AssertJ for fluent assertions
- H2 Database for testing

## Test Data Setup

Each test class uses `@BeforeEach` methods to set up test data:
- **Repository Tests**: Use `TestEntityManager` to persist test entities
- **Service Tests**: Create mock objects and configure mock behavior
- **Controller Tests**: Create DTOs for request/response testing

## Assertions and Verification

### Repository Tests
- Use AssertJ assertions for fluent, readable tests
- Verify query results and data integrity
- Test pagination and sorting

### Service Tests
- Mock repository interactions
- Verify method calls with `verify()`
- Test exception scenarios with `assertThatThrownBy()`

### Controller Tests
- Use MockMvc for HTTP testing
- Verify JSON responses with `jsonPath()`
- Test HTTP status codes and headers

## Coverage Goals

- **Line Coverage**: > 90%
- **Branch Coverage**: > 85%
- **Method Coverage**: 100%

## Best Practices Followed

1. **Test Isolation**: Each test is independent and can run in any order
2. **Clear Naming**: Test method names describe the scenario and expected outcome
3. **AAA Pattern**: Arrange, Act, Assert structure in all tests
4. **Edge Cases**: Comprehensive testing of boundary conditions
5. **Error Scenarios**: Thorough testing of exception paths
6. **Mock Verification**: Proper verification of mock interactions
7. **Test Data**: Realistic test data that represents actual use cases

## Maintenance Notes

- Update tests when business rules change
- Add new tests for new functionality
- Keep test data realistic and representative
- Regularly review and refactor tests for clarity
- Ensure tests remain fast and reliable
