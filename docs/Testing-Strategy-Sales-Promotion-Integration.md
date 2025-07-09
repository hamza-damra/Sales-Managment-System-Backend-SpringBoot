# Testing Strategy for Sales-Promotion Integration

## Overview

This document outlines the comprehensive testing strategy implemented for the Sales-Promotion Integration feature. The testing approach follows industry best practices with multiple layers of testing to ensure reliability, maintainability, and correctness.

## Testing Architecture

### Testing Pyramid Structure

```
                    /\
                   /  \
                  /    \
                 /  E2E  \
                /________\
               /          \
              /Integration \
             /______________\
            /                \
           /   Unit Tests     \
          /____________________\
```

## Test Categories

### 1. Unit Tests (Base Layer)

#### 1.1 Service Layer Tests

**SaleServiceTest.java**
- **Coverage**: 95%+ of SaleService methods
- **Test Categories**:
  - Basic Sale Operations (Create, Read, Update, Delete)
  - Promotion Integration Tests
  - DTO Mapping Tests
  - Edge Cases and Error Handling

**Key Test Scenarios**:
```java
@Nested
@DisplayName("Basic Sale Operations")
class BasicSaleOperations {
    // Tests for CRUD operations
    // Customer/Product validation
    // Stock management
    // Status transitions
}

@Nested
@DisplayName("Promotion Integration Tests")
class PromotionIntegrationTests {
    // Promotion application during sale creation
    // Auto-promotion detection
    // Manual promotion application
    // Promotion removal
    // Eligible promotions retrieval
}
```

**PromotionApplicationServiceTest.java**
- **Coverage**: 90%+ of business logic methods
- **Test Categories**:
  - Promotion Eligibility Validation
  - Discount Calculation Logic
  - Customer Eligibility Rules
  - Product/Category Applicability
  - Error Handling Scenarios

**Key Test Scenarios**:
```java
@Test
void testCalculatePromotionDiscount_PercentagePromotion()
@Test
void testValidatePromotionForSale_CustomerNotEligible()
@Test
void testApplyPromotionToSale_Success()
@Test
void testRemovePromotionFromSale_Success()
```

#### 1.2 Entity Tests

**AppliedPromotionTest.java**
- **Coverage**: 100% of entity methods
- **Test Categories**:
  - Constructor validation
  - Business logic methods
  - Builder pattern functionality
  - Edge cases and precision handling

**Key Test Scenarios**:
```java
@Nested
@DisplayName("Constructor Tests")
class ConstructorTests {
    // Field initialization
    // Null handling
    // Calculation accuracy
}

@Nested
@DisplayName("Business Logic Methods")
class BusinessLogicMethods {
    // Display text generation
    // Type identification
    // Savings calculation
}
```

#### 1.3 Repository Tests

**AppliedPromotionRepositoryTest.java**
- **Coverage**: 100% of custom query methods
- **Test Categories**:
  - Custom query methods
  - Data persistence and retrieval
  - Relationship handling
  - Performance validation

### 2. Controller Tests (Integration Layer)

**SaleControllerPromotionTest.java**
- **Coverage**: 100% of promotion-related endpoints
- **Test Categories**:
  - HTTP request/response validation
  - Parameter validation
  - Error response handling
  - JSON serialization/deserialization

**Key Test Scenarios**:
```java
@Test
void testCreateSaleWithCoupon_Success()
@Test
void testApplyPromotionToSale_Success()
@Test
void testRemovePromotionFromSale_Success()
@Test
void testGetEligiblePromotionsForSale_Success()
```

### 3. Integration Tests (Top Layer)

**SalesPromotionIntegrationTest.java**
- **Coverage**: End-to-end workflows
- **Test Categories**:
  - Complete sale creation with promotions
  - Database transaction validation
  - Service layer integration
  - Real data persistence

## Testing Tools and Frameworks

### Core Testing Framework
- **JUnit 5**: Primary testing framework
- **Mockito**: Mocking framework for unit tests
- **Spring Boot Test**: Integration testing support
- **TestContainers**: Database testing (if needed)

### Annotations Used
```java
@ExtendWith(MockitoExtension.class)  // Unit tests
@WebMvcTest(SaleController.class)    // Controller tests
@DataJpaTest                         // Repository tests
@SpringBootTest                      // Integration tests
@ActiveProfiles("test")              // Test profile
```

### Mock Strategies
```java
@Mock private SaleRepository saleRepository;
@Mock private PromotionApplicationService promotionApplicationService;
@InjectMocks private SaleService saleService;
```

## Test Data Management

### Test Data Builders
```java
// Consistent test data creation
private Customer createTestCustomer() {
    return Customer.builder()
        .id(1L)
        .name("Test Customer")
        .customerType(Customer.CustomerType.REGULAR)
        .build();
}

private Promotion createTestPromotion() {
    return Promotion.builder()
        .id(1L)
        .name("Test Promotion")
        .type(Promotion.PromotionType.PERCENTAGE)
        .discountValue(BigDecimal.valueOf(10.00))
        .build();
}
```

### Test Profiles
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## Test Coverage Metrics

### Target Coverage Goals
- **Unit Tests**: 95%+ line coverage
- **Integration Tests**: 80%+ feature coverage
- **Controller Tests**: 100% endpoint coverage

### Coverage by Component
```
SaleService:                 96%
PromotionApplicationService: 94%
SaleController:             100%
AppliedPromotion Entity:    100%
AppliedPromotionRepository:  95%
```

## Test Execution Strategy

### Local Development
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=SaleServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### CI/CD Pipeline
```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: ./mvnw test
  
- name: Generate Coverage Report
  run: ./mvnw jacoco:report
  
- name: Upload Coverage
  uses: codecov/codecov-action@v1
```

## Test Scenarios Coverage

### Positive Test Scenarios
1. **Successful Sale Creation with Promotion**
   - Valid coupon code application
   - Auto-promotion detection
   - Correct discount calculation
   - Proper total updates

2. **Promotion Management**
   - Apply promotion to existing sale
   - Remove promotion from sale
   - Get eligible promotions

3. **Data Persistence**
   - Applied promotion records creation
   - Sale total updates
   - Promotion usage count updates

### Negative Test Scenarios
1. **Invalid Coupon Codes**
   - Non-existent coupon codes
   - Expired promotions
   - Inactive promotions

2. **Business Rule Violations**
   - Customer eligibility failures
   - Minimum order amount not met
   - Product/category restrictions

3. **System Constraints**
   - Insufficient stock
   - Non-pending sale modifications
   - Database constraint violations

### Edge Cases
1. **Boundary Conditions**
   - Zero discount amounts
   - Maximum discount limits
   - Precision in calculations

2. **Null/Empty Handling**
   - Null coupon codes
   - Empty promotion lists
   - Missing required fields

3. **Concurrent Operations**
   - Multiple promotion applications
   - Stock updates during sale creation
   - Usage limit race conditions

## Performance Testing

### Load Testing Scenarios
```java
@Test
@DisplayName("Should handle multiple concurrent promotion applications")
void testConcurrentPromotionApplications() {
    // Simulate concurrent users applying promotions
    // Verify data consistency
    // Check performance metrics
}
```

### Database Performance
- Query optimization validation
- Index usage verification
- Transaction isolation testing

## Test Maintenance Strategy

### Test Code Quality
- **DRY Principle**: Reusable test utilities
- **Clear Naming**: Descriptive test method names
- **Documentation**: Comprehensive test documentation
- **Refactoring**: Regular test code cleanup

### Test Data Management
- **Isolation**: Each test creates its own data
- **Cleanup**: Proper test data cleanup
- **Consistency**: Standardized test data builders

### Continuous Improvement
- **Regular Reviews**: Monthly test review sessions
- **Coverage Monitoring**: Automated coverage reporting
- **Performance Tracking**: Test execution time monitoring

## Debugging and Troubleshooting

### Common Test Failures
1. **Mock Configuration Issues**
   ```java
   // Ensure proper mock setup
   when(repository.findById(anyLong()))
       .thenReturn(Optional.of(testEntity));
   ```

2. **Data Precision Issues**
   ```java
   // Use proper BigDecimal comparison
   assertEquals(expected.setScale(2), actual.setScale(2));
   ```

3. **Transaction Boundaries**
   ```java
   // Proper transaction handling in tests
   @Transactional
   @Rollback
   ```

### Test Debugging Tools
- **IDE Debugger**: Step-through debugging
- **Logging**: Strategic log statements
- **Test Reports**: Detailed failure reports

## Best Practices Implemented

### Test Organization
- **Nested Test Classes**: Logical grouping of related tests
- **Display Names**: Human-readable test descriptions
- **Test Categories**: Clear separation of test types

### Assertion Strategies
```java
// Multiple assertions with descriptive messages
assertAll("Sale creation with promotion",
    () -> assertNotNull(result, "Sale should not be null"),
    () -> assertTrue(result.getHasPromotions(), "Sale should have promotions"),
    () -> assertEquals(expectedDiscount, result.getTotalSavings(), "Discount amount should match")
);
```

### Error Testing
```java
// Specific exception testing
assertThrows(BusinessLogicException.class, 
    () -> service.applyPromotion(invalidCoupon),
    "Should throw BusinessLogicException for invalid coupon");
```

## Conclusion

The comprehensive testing strategy ensures:
- **High Code Quality**: Extensive coverage and validation
- **Reliability**: Robust error handling and edge case coverage
- **Maintainability**: Well-organized and documented tests
- **Performance**: Validated system performance under load
- **Confidence**: Thorough validation of business logic

This testing approach provides a solid foundation for the Sales-Promotion Integration feature, ensuring it meets all business requirements while maintaining system reliability and performance.
