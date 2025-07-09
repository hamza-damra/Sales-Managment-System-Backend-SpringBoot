# Inventory API Documentation

## Overview

The Inventory Service manages warehouse and storage locations within the Sales Management System. It provides comprehensive CRUD operations for inventory management, including warehouse creation, capacity tracking, stock monitoring, and main warehouse designation. The service ensures data integrity through validation rules and supports advanced features like capacity utilization tracking and near-capacity alerts.

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
/api/inventories
```

### 1. Create Inventory

**Endpoint:** `POST /api/inventories`

**Description:** Creates a new inventory/warehouse with validation

**Request Body:**
```json
{
  "name": "Main Warehouse",
  "description": "Primary storage facility",
  "location": "New York",
  "address": "123 Storage St, NY 10001",
  "managerName": "John Smith",
  "managerPhone": "+1-555-0123",
  "managerEmail": "john.smith@company.com",
  "capacity": 1000,
  "currentStockCount": 0,
  "warehouseCode": "MW001",
  "isMainWarehouse": true,
  "operatingHours": "8:00 AM - 6:00 PM",
  "contactPhone": "+1-555-0124",
  "contactEmail": "warehouse@company.com",
  "notes": "Climate controlled facility"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Main Warehouse",
  "description": "Primary storage facility",
  "location": "New York",
  "address": "123 Storage St, NY 10001",
  "managerName": "John Smith",
  "managerPhone": "+1-555-0123",
  "managerEmail": "john.smith@company.com",
  "capacity": 1000,
  "currentStockCount": 0,
  "status": "ACTIVE",
  "warehouseCode": "MW001",
  "isMainWarehouse": true,
  "operatingHours": "8:00 AM - 6:00 PM",
  "contactPhone": "+1-555-0124",
  "contactEmail": "warehouse@company.com",
  "notes": "Climate controlled facility",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "categoryCount": 0,
  "capacityUtilization": 0.0,
  "isNearCapacity": false
}
```

### 2. Get All Inventories (Paginated)

**Endpoint:** `GET /api/inventories`

**Query Parameters:**
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "name"): Sort field
- `sortDir` (string, default: "asc"): Sort direction (asc/desc)
- `status` (string, optional): Filter by status

**Example:** `GET /api/inventories?page=0&size=5&sortBy=name&sortDir=asc`

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "name": "Main Warehouse",
      "location": "New York",
      "capacity": 1000,
      "currentStockCount": 250,
      "status": "ACTIVE",
      "isMainWarehouse": true,
      "capacityUtilization": 25.0,
      "isNearCapacity": false
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

### 3. Get Inventory by ID

**Endpoint:** `GET /api/inventories/{id}`

**Path Parameters:**
- `id` (long): Inventory ID

**Response:** `200 OK` (same structure as create response)

**Error Responses:**
- `400 Bad Request`: Invalid ID (≤ 0)
- `404 Not Found`: Inventory not found

### 4. Update Inventory

**Endpoint:** `PUT /api/inventories/{id}`

**Path Parameters:**
- `id` (long): Inventory ID

**Request Body:** Same as create request

**Response:** `200 OK` (updated inventory object)

**Error Responses:**
- `400 Bad Request`: Invalid ID or validation errors
- `404 Not Found`: Inventory not found

### 5. Delete Inventory

**Endpoint:** `DELETE /api/inventories/{id}`

**Path Parameters:**
- `id` (long): Inventory ID

**Response:** `204 No Content`

**Error Responses:**
- `400 Bad Request`: Invalid ID or inventory has categories
- `404 Not Found`: Inventory not found

### 6. Search Inventories

**Endpoint:** `GET /api/inventories/search`

**Query Parameters:**
- `query` (string, required): Search term
- `page` (int, default: 0): Page number
- `size` (int, default: 10): Page size
- `sortBy` (string, default: "name"): Sort field
- `sortDir` (string, default: "asc"): Sort direction

**Example:** `GET /api/inventories/search?query=warehouse&page=0&size=10`

**Response:** `200 OK` (paginated results)

### 7. Get Active Inventories

**Endpoint:** `GET /api/inventories/active`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Main Warehouse",
    "status": "ACTIVE",
    "location": "New York"
  }
]
```

### 8. Get Main Warehouses

**Endpoint:** `GET /api/inventories/main-warehouses`

**Response:** `200 OK` (list of main warehouses)

### 9. Get Inventory by Name

**Endpoint:** `GET /api/inventories/name/{name}`

**Path Parameters:**
- `name` (string): Inventory name

**Response:** `200 OK` or `404 Not Found`

### 10. Get Inventory by Warehouse Code

**Endpoint:** `GET /api/inventories/warehouse-code/{warehouseCode}`

**Path Parameters:**
- `warehouseCode` (string): Warehouse code

**Response:** `200 OK` or `404 Not Found`

### 11. Get Inventories by Status

**Endpoint:** `GET /api/inventories/status/{status}`

**Path Parameters:**
- `status` (string): Status (ACTIVE, INACTIVE, ARCHIVED, MAINTENANCE)

**Response:** `200 OK` (list of inventories)

### 12. Get Empty Inventories

**Endpoint:** `GET /api/inventories/empty`

**Description:** Returns inventories with no categories

**Response:** `200 OK` (list of empty inventories)

### 13. Get Inventories Near Capacity

**Endpoint:** `GET /api/inventories/near-capacity`

**Query Parameters:**
- `threshold` (double, default: 80.0): Capacity threshold percentage (0-100)

**Example:** `GET /api/inventories/near-capacity?threshold=85.0`

**Response:** `200 OK` (list of inventories near capacity)

### 14. Update Inventory Status

**Endpoint:** `PUT /api/inventories/{id}/status`

**Path Parameters:**
- `id` (long): Inventory ID

**Request Body:**
```json
{
  "status": "MAINTENANCE"
}
```

**Response:** `200 OK` (updated inventory object)

## Data Models

### Inventory Entity

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Auto | Primary key |
| name | String | Yes | Inventory name (unique) |
| description | String | No | Detailed description |
| location | String | Yes | Physical location |
| address | String | No | Full address |
| managerName | String | No | Manager's name |
| managerPhone | String | No | Manager's phone |
| managerEmail | String | No | Manager's email |
| capacity | Integer | No | Maximum capacity |
| currentStockCount | Integer | No | Current stock count (default: 0) |
| status | InventoryStatus | No | Status (default: ACTIVE) |
| warehouseCode | String | No | Unique warehouse code |
| isMainWarehouse | Boolean | No | Main warehouse flag (default: false) |
| operatingHours | String | No | Operating hours |
| contactPhone | String | No | Contact phone |
| contactEmail | String | No | Contact email |
| notes | String | No | Additional notes |
| createdAt | LocalDateTime | Auto | Creation timestamp |
| updatedAt | LocalDateTime | Auto | Last update timestamp |

### InventoryDTO (API Response)

Includes all entity fields plus:

| Field | Type | Description |
|-------|------|-------------|
| categoryCount | Integer | Number of categories in inventory |
| capacityUtilization | Double | Capacity utilization percentage |
| isNearCapacity | Boolean | Near capacity flag (>80% by default) |

### InventoryStatus Enum

- `ACTIVE`: Operational and available
- `INACTIVE`: Temporarily unavailable
- `ARCHIVED`: Permanently closed
- `MAINTENANCE`: Under maintenance

## Business Logic

### Validation Rules

#### 1. Name Uniqueness
- Inventory names must be unique (case-insensitive)
- Names are automatically trimmed
- Empty or null names are rejected

**Error Messages:**
- `"Inventory name cannot be empty"`
- `"Inventory with name 'X' already exists"`

#### 2. Location Validation
- Location is required and cannot be empty
- Locations are automatically trimmed

**Error Message:**
- `"Inventory location is required"`

#### 3. Warehouse Code Validation
- Warehouse codes must be unique if provided
- Optional field, but if provided must be unique

**Error Message:**
- `"Warehouse code 'X' already exists"`

#### 4. Main Warehouse Constraint
- Only one main warehouse is allowed in the system
- When setting `isMainWarehouse: true`, system checks for existing main warehouses

**Error Message:**
- `"Only one main warehouse is allowed. Please unset the current main warehouse first."`

#### 5. Capacity Management
- Capacity utilization is automatically calculated: `(currentStockCount / capacity) * 100`
- `isNearCapacity` flag is set when utilization > 80%
- Negative stock counts are not allowed

#### 6. Deletion Constraints
- Inventories with associated categories cannot be deleted
- Must remove all categories before deletion

**Error Message:**
- `"Cannot delete inventory because it has X associated categories"`

### Data Integrity

#### Automatic Field Processing
- Names and locations are trimmed of whitespace
- Default values are set for optional fields:
  - `currentStockCount`: 0
  - `status`: ACTIVE
  - `isMainWarehouse`: false

#### Capacity Calculations
- Capacity utilization is calculated only when capacity > 0
- Near capacity threshold is configurable (default: 80%)

## Error Handling

### Common HTTP Status Codes

| Status Code | Description | Common Scenarios |
|-------------|-------------|------------------|
| 200 OK | Success | GET, PUT operations |
| 201 Created | Resource created | POST operations |
| 204 No Content | Success, no content | DELETE operations |
| 400 Bad Request | Validation error | Invalid data, business rule violations |
| 404 Not Found | Resource not found | Invalid ID, non-existent resource |
| 500 Internal Server Error | Server error | Unexpected system errors |

### Exception Types

#### BusinessLogicException (400 Bad Request)
Thrown when business rules are violated:
- Duplicate names
- Duplicate warehouse codes
- Multiple main warehouses
- Empty required fields

#### ResourceNotFoundException (404 Not Found)
Thrown when requested resources don't exist:
- Invalid inventory ID
- Non-existent inventory name/warehouse code

#### DataIntegrityException (400 Bad Request)
Thrown when data integrity constraints are violated:
- Attempting to delete inventory with categories

### Error Response Format

```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Inventory with name 'Main Warehouse' already exists",
  "errorCode": "BUSINESS_RULE_VIOLATION",
  "timestamp": "2024-01-15T10:30:00",
  "suggestions": "Please review the requirements and adjust your input accordingly."
}
```

## Usage Examples

### Creating a Basic Warehouse

```bash
curl -X POST http://localhost:8081/api/inventories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Storage Facility A",
    "location": "Chicago",
    "capacity": 500
  }'
```

### Creating a Main Warehouse

```bash
curl -X POST http://localhost:8081/api/inventories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Main Distribution Center",
    "location": "Los Angeles",
    "capacity": 2000,
    "warehouseCode": "MDC001",
    "isMainWarehouse": true,
    "managerName": "Jane Doe",
    "managerEmail": "jane.doe@company.com"
  }'
```

### Searching for Warehouses

```bash
curl "http://localhost:8081/api/inventories/search?query=warehouse&page=0&size=10"
```

### Getting Near-Capacity Warehouses

```bash
curl "http://localhost:8081/api/inventories/near-capacity?threshold=85.0"
```

### Updating Warehouse Status

```bash
curl -X PUT http://localhost:8081/api/inventories/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "MAINTENANCE"}'
```

## Testing

### Test Coverage

The Inventory service includes comprehensive test coverage:

#### Unit Tests (`InventoryServiceTest`)
- **CRUD Operations**: Create, read, update, delete functionality
- **Validation Tests**: Name uniqueness, location validation, main warehouse constraints
- **Business Logic**: Capacity calculations, near-capacity detection
- **Error Scenarios**: Invalid data, duplicate entries, constraint violations
- **Edge Cases**: Null values, empty strings, boundary conditions

#### Integration Tests (`InventoryControllerTest`)
- **HTTP Endpoints**: All REST endpoints with various scenarios
- **Request/Response Validation**: JSON serialization/deserialization
- **Error Handling**: Exception mapping to HTTP status codes
- **Pagination**: Sorting and pagination functionality

### Key Test Scenarios

1. **Successful Operations**
   - Create inventory with valid data
   - Update existing inventory
   - Retrieve inventories with pagination
   - Search functionality

2. **Validation Failures**
   - Duplicate inventory names
   - Empty required fields
   - Invalid warehouse codes
   - Multiple main warehouses

3. **Business Logic**
   - Capacity utilization calculations
   - Near-capacity detection
   - Automatic field trimming
   - Default value assignment

4. **Error Handling**
   - Resource not found scenarios
   - Data integrity violations
   - Invalid request parameters

### Running Tests

```bash
# Run all inventory tests
./mvnw test -Dtest=InventoryServiceTest

# Run controller tests
./mvnw test -Dtest=InventoryControllerTest

# Run specific test method
./mvnw test -Dtest=InventoryServiceTest#createInventory_ShouldCreateSuccessfully_WhenValidData
```

---

**Note:** This documentation covers the core Inventory API functionality. For advanced features like analytics and reporting, refer to the Reports API documentation.
