# Promotion API Documentation

## Overview

The Promotion Service manages promotional campaigns and discount codes within the Sales Management System. It provides comprehensive CRUD operations for promotion management, including discount calculations, coupon code validation, customer eligibility checks, and usage tracking. The service supports multiple promotion types (percentage, fixed amount, buy-X-get-Y, free shipping) with advanced features like automatic coupon generation, usage limits, customer targeting, and stackable promotions.

## Table of Contents

1. [API Endpoints](#api-endpoints)
2. [Data Models](#data-models)
3. [Business Logic](#business-logic)
4. [Error Handling](#error-handling)
5. [Usage Examples](#usage-examples)
6. [Testing](#testing)

## API Endpoints

### Base URL
```
/api/promotions
```

### 1. Create Promotion

**Endpoint:** `POST /api/promotions`

**Description:** Creates a new promotion with validation and automatic coupon code generation

**Request Body:**
```json
{
  "name": "Summer Sale 2024",
  "description": "20% off all summer items",
  "type": "PERCENTAGE",
  "discountValue": 20.00,
  "minimumOrderAmount": 50.00,
  "maximumDiscountAmount": 100.00,
  "startDate": "2024-06-01T00:00:00",
  "endDate": "2024-08-31T23:59:59",
  "isActive": true,
  "applicableProducts": [1, 2, 3],
  "applicableCategories": ["CLOTHING", "ACCESSORIES"],
  "usageLimit": 1000,
  "customerEligibility": "ALL",
  "autoApply": false,
  "stackable": false
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Summer Sale 2024",
  "description": "20% off all summer items",
  "type": "PERCENTAGE",
  "discountValue": 20.00,
  "minimumOrderAmount": 50.00,
  "maximumDiscountAmount": 100.00,
  "startDate": "2024-06-01T00:00:00",
  "endDate": "2024-08-31T23:59:59",
  "isActive": true,
  "applicableProducts": [1, 2, 3],
  "applicableCategories": ["CLOTHING", "ACCESSORIES"],
  "usageLimit": 1000,
  "usageCount": 0,
  "customerEligibility": "ALL",
  "couponCode": "SUMMER2024-ABC123",
  "autoApply": false,
  "stackable": false,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "statusDisplay": "Active",
  "typeDisplay": "Percentage Discount",
  "eligibilityDisplay": "All Customers",
  "isCurrentlyActive": true,
  "isExpired": false,
  "isNotYetStarted": false,
  "isUsageLimitReached": false,
  "daysUntilExpiry": 198,
  "remainingUsage": 1000,
  "usagePercentage": 0.0
}
```

### 2. Get All Promotions (Paginated)

**Endpoint:** `GET /api/promotions`

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "id"): Sort field
- `sortDir` (string, default: "desc"): Sort direction (asc/desc)
- `isActive` (boolean, optional): Filter by active status

**Example:** `GET /api/promotions?page=0&size=5&sortBy=name&sortDir=asc&isActive=true`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Summer Sale 2024",
      "type": "PERCENTAGE",
      "discountValue": 20.00,
      "startDate": "2024-06-01T00:00:00",
      "endDate": "2024-08-31T23:59:59",
      "isActive": true,
      "usageCount": 45,
      "usageLimit": 1000,
      "isCurrentlyActive": true,
      "statusDisplay": "Active"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 5,
    "sort": {
      "sorted": true,
      "ascending": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 3. Get Promotion by ID

**Endpoint:** `GET /api/promotions/{id}`

**Path Parameters:**
- `id` (long): Promotion ID

**Response:** `200 OK` (same structure as create response)

**Error Responses:**
- `400 Bad Request`: Invalid ID (≤ 0)
- `404 Not Found`: Promotion not found

### 4. Update Promotion

**Endpoint:** `PUT /api/promotions/{id}`

**Path Parameters:**
- `id` (long): Promotion ID

**Request Body:** Same as create request

**Response:** `200 OK` (updated promotion object)

**Error Responses:**
- `400 Bad Request`: Invalid ID or validation errors
- `404 Not Found`: Promotion not found

### 5. Delete Promotion

**Endpoint:** `DELETE /api/promotions/{id}`

**Path Parameters:**
- `id` (long): Promotion ID

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request`: Invalid ID or promotion is currently active
- `404 Not Found`: Promotion not found

### 6. Search Promotions

**Endpoint:** `GET /api/promotions/search`

**Query Parameters:**
- `query` (string, required): Search term
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "id"): Sort field
- `sortDir` (string, default: "desc"): Sort direction

**Example:** `GET /api/promotions/search?query=summer&page=0&size=10`

**Response:** `200 OK` (paginated results)

### 7. Get Active Promotions

**Endpoint:** `GET /api/promotions/active`

**Description:** Returns currently active promotions (within date range and not usage-limited)

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Summer Sale 2024",
    "type": "PERCENTAGE",
    "discountValue": 20.00,
    "couponCode": "SUMMER2024-ABC123",
    "isCurrentlyActive": true
  }
]
```

### 8. Get Available Promotions

**Endpoint:** `GET /api/promotions/available`

**Description:** Returns promotions that are active and haven't reached usage limits

**Response:** `200 OK` (list of available promotions)

### 9. Get Expired Promotions

**Endpoint:** `GET /api/promotions/expired`

**Description:** Returns promotions that have passed their end date

**Response:** `200 OK` (list of expired promotions)

### 10. Get Scheduled Promotions

**Endpoint:** `GET /api/promotions/scheduled`

**Description:** Returns promotions that haven't started yet

**Response:** `200 OK` (list of scheduled promotions)

### 11. Get Promotions by Type

**Endpoint:** `GET /api/promotions/type/{type}`

**Path Parameters:**
- `type` (string): Promotion type (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y, FREE_SHIPPING)

**Response:** `200 OK` (list of promotions)

### 12. Get Promotions by Customer Eligibility

**Endpoint:** `GET /api/promotions/eligibility/{eligibility}`

**Path Parameters:**
- `eligibility` (string): Customer eligibility (ALL, VIP_ONLY, NEW_CUSTOMERS, RETURNING_CUSTOMERS, PREMIUM_ONLY)

**Response:** `200 OK` (list of promotions)

### 13. Activate Promotion

**Endpoint:** `POST /api/promotions/{id}/activate`

**Path Parameters:**
- `id` (long): Promotion ID

**Response:** `200 OK` (activated promotion object)

### 14. Deactivate Promotion

**Endpoint:** `POST /api/promotions/{id}/deactivate`

**Path Parameters:**
- `id` (long): Promotion ID

**Response:** `200 OK` (deactivated promotion object)

### 15. Get Promotions for Product

**Endpoint:** `GET /api/promotions/product/{productId}`

**Path Parameters:**
- `productId` (long): Product ID

**Response:** `200 OK` (list of applicable promotions)

### 16. Get Promotions for Category

**Endpoint:** `GET /api/promotions/category/{category}`

**Path Parameters:**
- `category` (string): Category name

**Response:** `200 OK` (list of applicable promotions)

### 17. Validate Coupon Code

**Endpoint:** `GET /api/promotions/coupon/{couponCode}`

**Path Parameters:**
- `couponCode` (string): Coupon code to validate

**Response:** `200 OK` (promotion details if valid)

**Error Responses:**
- `400 Bad Request`: Invalid, expired, or usage-limited coupon

### 18. Apply Promotion

**Endpoint:** `POST /api/promotions/{id}/apply`

**Path Parameters:**
- `id` (long): Promotion ID

**Query Parameters:**
- `orderAmount` (BigDecimal, required): Order amount to calculate discount

**Response:** `200 OK`
```json
{
  "discountAmount": 15.00,
  "orderAmount": 75.00,
  "finalAmount": 60.00
}
```

## Data Models

### PromotionDTO (API Request/Response)

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Auto | Primary key |
| name | String | Yes | Promotion name |
| description | String | No | Detailed description |
| type | PromotionType | Yes | Discount type |
| discountValue | BigDecimal | Yes | Discount amount/percentage (> 0) |
| minimumOrderAmount | BigDecimal | No | Minimum order requirement |
| maximumDiscountAmount | BigDecimal | No | Maximum discount cap |
| startDate | LocalDateTime | Yes | Promotion start date |
| endDate | LocalDateTime | Yes | Promotion end date |
| isActive | Boolean | No | Active status (default: true) |
| applicableProducts | List&lt;Long&gt; | No | Applicable product IDs |
| applicableCategories | List&lt;String&gt; | No | Applicable category names |
| usageLimit | Integer | No | Maximum usage count |
| usageCount | Integer | Auto | Current usage count |
| customerEligibility | CustomerEligibility | No | Customer targeting (default: ALL) |
| couponCode | String | Auto | Unique coupon code |
| autoApply | Boolean | No | Auto-apply flag (default: false) |
| stackable | Boolean | No | Stackable with other promotions (default: false) |
| createdAt | LocalDateTime | Auto | Creation timestamp |
| updatedAt | LocalDateTime | Auto | Last update timestamp |

### Computed Fields (Response Only)

| Field | Type | Description |
|-------|------|-------------|
| statusDisplay | String | Human-readable status |
| typeDisplay | String | Human-readable type |
| eligibilityDisplay | String | Human-readable eligibility |
| isCurrentlyActive | Boolean | Currently active flag |
| isExpired | Boolean | Expired flag |
| isNotYetStarted | Boolean | Not yet started flag |
| isUsageLimitReached | Boolean | Usage limit reached flag |
| daysUntilExpiry | Long | Days until expiration |
| remainingUsage | Integer | Remaining usage count |
| usagePercentage | Double | Usage percentage |

### PromotionType Enum

- `PERCENTAGE`: Percentage-based discount
- `FIXED_AMOUNT`: Fixed amount discount
- `BUY_X_GET_Y`: Buy X get Y promotion
- `FREE_SHIPPING`: Free shipping promotion

### CustomerEligibility Enum

- `ALL`: All customers
- `VIP_ONLY`: VIP customers only
- `NEW_CUSTOMERS`: New customers (no previous purchases)
- `RETURNING_CUSTOMERS`: Returning customers (with previous purchases)
- `PREMIUM_ONLY`: Premium customers only

## Business Logic

### Validation Rules

#### 1. Date Validation
- Start date must be before end date
- Promotions cannot be created with past end dates
- Date ranges are validated during creation and updates

**Error Messages:**
- `"Start date must be before end date"`
- `"End date cannot be in the past"`

#### 2. Coupon Code Uniqueness
- Coupon codes must be unique across all promotions
- Automatic generation for non-auto-apply promotions
- Case-sensitive validation

**Error Message:**
- `"Coupon code already exists: {couponCode}"`

#### 3. Discount Value Validation
- Discount value must be greater than 0
- For percentage discounts: typically 0-100%
- For fixed amounts: positive monetary values

**Error Message:**
- `"Discount value must be greater than 0"`

#### 4. Active Promotion Constraints
- Active promotions cannot be deleted
- Must deactivate before deletion
- Usage count cannot exceed usage limit

**Error Messages:**
- `"Cannot delete an active promotion"`
- `"Promotion is not currently active"`
- `"Coupon code usage limit has been reached: {couponCode}"`

### Discount Calculation Logic

#### 1. Percentage Discounts
```
discount = orderAmount × (discountValue / 100)
```
- Rounded to 2 decimal places using HALF_UP rounding
- Subject to maximum discount amount if specified

#### 2. Fixed Amount Discounts
```
discount = discountValue
```
- Applied directly if order meets minimum amount
- Cannot exceed order amount

#### 3. Minimum Order Requirements
- Order must meet `minimumOrderAmount` to qualify
- Returns zero discount if requirement not met

#### 4. Maximum Discount Limits
- Applied after discount calculation
- Prevents excessive discounts on large orders

### Customer Eligibility Logic

#### Customer Type Matching
- **VIP_ONLY**: Matches customers with `CustomerType.VIP`
- **PREMIUM_ONLY**: Matches customers with `CustomerType.PREMIUM`
- **NEW_CUSTOMERS**: Matches customers with zero total purchases
- **RETURNING_CUSTOMERS**: Matches customers with purchases > 0
- **ALL**: Matches any customer (including anonymous)

### Promotion Status Logic

#### Currently Active Determination
A promotion is currently active when ALL conditions are met:
1. `isActive` flag is true
2. Current date/time is after `startDate`
3. Current date/time is before `endDate`
4. Usage limit not reached (if specified)

#### Usage Tracking
- `usageCount` incremented when promotion is applied
- `usageLimit` enforced during validation
- Percentage calculation: `(usageCount / usageLimit) × 100`

### Auto-Apply vs Manual Promotions

#### Auto-Apply Promotions
- Automatically applied during checkout
- No coupon code required
- System evaluates eligibility automatically

#### Manual Promotions
- Require coupon code entry
- Customer-initiated application
- Coupon code validation required

### Stackable Promotions

#### Stackable Logic
- Multiple stackable promotions can be combined
- Non-stackable promotions are mutually exclusive
- Business rules determine stacking order and limits

## Error Handling

### Common HTTP Status Codes

| Status Code | Description | Common Scenarios |
|-------------|-------------|------------------|
| 200 OK | Success | GET, PUT operations |
| 201 Created | Resource created | POST operations |
| 204 No Content | Success, no content | DELETE operations |
| 400 Bad Request | Validation error | Invalid data, business rule violations |
| 404 Not Found | Resource not found | Invalid ID, non-existent promotion |
| 500 Internal Server Error | Server error | Unexpected system errors |

### Exception Types

#### BusinessLogicException (400 Bad Request)
Thrown when business rules are violated:
- Invalid date ranges
- Duplicate coupon codes
- Active promotion deletion attempts
- Coupon validation failures
- Usage limit violations

#### ResourceNotFoundException (404 Not Found)
Thrown when requested resources don't exist:
- Invalid promotion ID
- Non-existent coupon code
- Missing promotion for product/category

#### ValidationException (400 Bad Request)
Thrown when input validation fails:
- Missing required fields
- Invalid data types
- Constraint violations

### Error Response Format

```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Coupon code already exists: SUMMER2024",
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "timestamp": "2024-01-15T10:30:00",
  "suggestions": "Please review the requirements and adjust your input accordingly."
}
```

### Common Error Scenarios

#### 1. Invalid Promotion Creation
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Start date must be before end date",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 2. Coupon Code Validation Failure
```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Coupon code is not currently active: EXPIRED2023",
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 3. Promotion Not Found
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Promotion not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### 4. Active Promotion Deletion
```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Cannot delete an active promotion",
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "timestamp": "2024-01-15T10:30:00",
  "suggestions": "Please deactivate the promotion before attempting to delete it."
}
```

## Usage Examples

### Creating a Percentage Discount Promotion

```bash
curl -X POST http://localhost:8081/api/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Black Friday Sale",
    "description": "25% off everything",
    "type": "PERCENTAGE",
    "discountValue": 25.00,
    "minimumOrderAmount": 100.00,
    "maximumDiscountAmount": 200.00,
    "startDate": "2024-11-29T00:00:00",
    "endDate": "2024-11-29T23:59:59",
    "usageLimit": 5000,
    "customerEligibility": "ALL",
    "autoApply": true
  }'
```

### Creating a Fixed Amount Discount

```bash
curl -X POST http://localhost:8081/api/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Customer Welcome",
    "description": "$10 off first order",
    "type": "FIXED_AMOUNT",
    "discountValue": 10.00,
    "minimumOrderAmount": 50.00,
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-12-31T23:59:59",
    "customerEligibility": "NEW_CUSTOMERS",
    "couponCode": "WELCOME10"
  }'
```

### Creating a VIP-Only Promotion

```bash
curl -X POST http://localhost:8081/api/promotions \
  -H "Content-Type: application/json" \
  -d '{
    "name": "VIP Exclusive",
    "description": "30% off for VIP members",
    "type": "PERCENTAGE",
    "discountValue": 30.00,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-06-30T23:59:59",
    "customerEligibility": "VIP_ONLY",
    "stackable": true
  }'
```

### Searching for Promotions

```bash
curl "http://localhost:8081/api/promotions/search?query=summer&page=0&size=10&sortBy=name&sortDir=asc"
```

### Getting Active Promotions

```bash
curl "http://localhost:8081/api/promotions/active"
```

### Validating a Coupon Code

```bash
curl "http://localhost:8081/api/promotions/coupon/SUMMER2024-ABC123"
```

### Applying a Promotion to Calculate Discount

```bash
curl -X POST "http://localhost:8081/api/promotions/1/apply?orderAmount=150.00"
```

### Getting Promotions for a Specific Product

```bash
curl "http://localhost:8081/api/promotions/product/123"
```

### Updating a Promotion

```bash
curl -X PUT http://localhost:8081/api/promotions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Summer Sale",
    "description": "Extended summer promotion",
    "type": "PERCENTAGE",
    "discountValue": 25.00,
    "minimumOrderAmount": 75.00,
    "startDate": "2024-06-01T00:00:00",
    "endDate": "2024-09-15T23:59:59",
    "isActive": true
  }'
```

### Deactivating a Promotion

```bash
curl -X POST http://localhost:8081/api/promotions/1/deactivate
```

### Deleting a Promotion

```bash
# First deactivate if active
curl -X POST http://localhost:8081/api/promotions/1/deactivate

# Then delete
curl -X DELETE http://localhost:8081/api/promotions/1
```

## Testing

### Test Coverage

The Promotion service should include comprehensive test coverage following the project's testing patterns:

#### Unit Tests (`PromotionServiceTest`)
- **CRUD Operations**: Create, read, update, delete functionality
- **Validation Tests**: Date validation, coupon code uniqueness, discount value validation
- **Business Logic**: Discount calculations, customer eligibility checks, promotion status logic
- **Error Scenarios**: Invalid data, duplicate coupon codes, active promotion deletion
- **Edge Cases**: Null values, boundary conditions, usage limit scenarios

#### Integration Tests (`PromotionControllerTest`)
- **HTTP Endpoints**: All REST endpoints with various scenarios
- **Request/Response Validation**: JSON serialization/deserialization
- **Error Handling**: Exception mapping to HTTP status codes
- **Pagination**: Sorting and pagination functionality
- **Authentication**: Security constraints and access control

#### Repository Tests (`PromotionRepositoryTest`)
- **Custom Queries**: Active promotions, expired promotions, search functionality
- **Data Integrity**: Unique constraints, foreign key relationships
- **Performance**: Query optimization and indexing

### Key Test Scenarios

1. **Successful Operations**
   - Create promotion with valid data
   - Update existing promotion
   - Retrieve promotions with pagination
   - Search and filter functionality
   - Discount calculation accuracy

2. **Validation Failures**
   - Invalid date ranges
   - Duplicate coupon codes
   - Missing required fields
   - Invalid discount values
   - Customer eligibility mismatches

3. **Business Logic**
   - Promotion status determination
   - Usage limit enforcement
   - Customer eligibility validation
   - Discount calculation algorithms
   - Auto-apply vs manual promotion logic

4. **Error Handling**
   - Resource not found scenarios
   - Business rule violations
   - Invalid request parameters
   - Concurrent modification handling

### Recommended Test Structure

```
src/test/java/com/hamza/salesmanagementbackend/
├── service/
│   └── PromotionServiceTest.java
├── controller/
│   └── PromotionControllerTest.java
├── repository/
│   └── PromotionRepositoryTest.java
└── integration/
    └── PromotionIntegrationTest.java
```

### Running Tests

```bash
# Run all promotion tests
./mvnw test -Dtest="*Promotion*Test"

# Run service tests
./mvnw test -Dtest=PromotionServiceTest

# Run controller tests
./mvnw test -Dtest=PromotionControllerTest

# Run specific test method
./mvnw test -Dtest=PromotionServiceTest#createPromotion_ShouldCreateSuccessfully_WhenValidData
```

### Test Data Setup

Tests should use consistent test data following the project patterns:
- Mock objects for unit tests
- H2 in-memory database for integration tests
- Test profiles for environment-specific configurations
- Proper cleanup between test methods

---

**Note:** This documentation covers the core Promotion API functionality. The promotion system integrates with the Customer and Product modules for eligibility checks and applicability rules. For order processing and sales integration, refer to the Sales API documentation.
