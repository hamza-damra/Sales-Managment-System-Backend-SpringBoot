# Product Controller - Postman Testing Guide

## Base Configuration

**Base URL:** `http://localhost:8081/api/products`

**Authentication:** All endpoints require JWT Bearer token
```
Authorization: Bearer <your_jwt_token>
```

**Content-Type:** `application/json` (for POST/PUT requests)

---

## 🔐 Prerequisites

### 1. Get JWT Token First
Before testing Product endpoints, you need to authenticate:

**Login Request:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "your_username",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "createdAt": "2025-07-03T10:00:00"
  }
}
```

Copy the `accessToken` and use it in all Product API requests.

---

## 📦 Product Controller Endpoints

### 1. Get All Products

**Method:** `GET`
**URL:** `http://localhost:8081/api/products`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters (Optional):**
- `page=0` (default: 0)
- `size=10` (default: 10)
- `sortBy=id` (default: id) - Options: id, name, price, stockQuantity, category, createdAt
- `sortDir=asc` (default: asc) - Options: asc, desc
- `category=Electronics` (optional filter)

**Example URLs:**
```
GET http://localhost:8081/api/products
GET http://localhost:8081/api/products?page=0&size=5
GET http://localhost:8081/api/products?sortBy=name&sortDir=desc
GET http://localhost:8081/api/products?category=Electronics
GET http://localhost:8081/api/products?page=1&size=20&sortBy=price&sortDir=desc&category=Electronics
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro",
      "description": "High-performance laptop",
      "price": 1299.99,
      "costPrice": 800.00,
      "stockQuantity": 50,
      "category": "Electronics",
      "sku": "LAP001",
      "brand": "TechBrand",
      "modelNumber": "TB-LP-2024",
      "barcode": "1234567890123",
      "weight": 2.5,
      "length": 35.0,
      "width": 25.0,
      "height": 2.0,
      "productStatus": "ACTIVE",
      "minStockLevel": 10,
      "maxStockLevel": 100,
      "reorderPoint": 15,
      "reorderQuantity": 25,
      "supplierName": "Tech Supplier Inc",
      "supplierCode": "TS001",
      "warrantyPeriod": 24,
      "expiryDate": null,
      "manufacturingDate": "2024-01-15",
      "tags": ["laptop", "computer", "portable"],
      "imageUrl": "https://example.com/laptop.jpg",
      "additionalImages": ["https://example.com/laptop2.jpg"],
      "isSerialized": true,
      "isDigital": false,
      "isTaxable": true,
      "taxRate": 8.5,
      "unitOfMeasure": "piece",
      "discountPercentage": 0.0,
      "locationInWarehouse": "A1-B2-C3",
      "totalSold": 150,
      "totalRevenue": 194999.85,
      "lastSoldDate": "2025-07-01T14:30:00",
      "lastRestockedDate": "2025-06-15T09:00:00",
      "notes": "Popular model",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-07-01T14:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "ascending": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

---

### 2. Get Product by ID

**Method:** `GET`
**URL:** `http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example URLs:**
```
GET http://localhost:8081/api/products/1
GET http://localhost:8081/api/products/5
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop Pro",
  "description": "High-performance laptop",
  "price": 1299.99,
  "costPrice": 800.00,
  "stockQuantity": 50,
  "category": "Electronics",
  "sku": "LAP001",
  "brand": "TechBrand",
  "modelNumber": "TB-LP-2024",
  "barcode": "1234567890123",
  "weight": 2.5,
  "length": 35.0,
  "width": 25.0,
  "height": 2.0,
  "productStatus": "ACTIVE",
  "minStockLevel": 10,
  "maxStockLevel": 100,
  "reorderPoint": 15,
  "reorderQuantity": 25,
  "supplierName": "Tech Supplier Inc",
  "supplierCode": "TS001",
  "warrantyPeriod": 24,
  "expiryDate": null,
  "manufacturingDate": "2024-01-15",
  "tags": ["laptop", "computer", "portable"],
  "imageUrl": "https://example.com/laptop.jpg",
  "additionalImages": ["https://example.com/laptop2.jpg"],
  "isSerialized": true,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "A1-B2-C3",
  "totalSold": 150,
  "totalRevenue": 194999.85,
  "lastSoldDate": "2025-07-01T14:30:00",
  "lastRestockedDate": "2025-06-15T09:00:00",
  "notes": "Popular model",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-07-01T14:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Product not found with id: 999",
  "timestamp": "2025-07-05T10:00:00"
}
```

**Error Response (400 Bad Request) - Invalid ID:**
```json
{
  "message": "Invalid product ID",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

### 3. Create Product

**Method:** `POST`
**URL:** `http://localhost:8081/api/products`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body (Minimal Required Fields):**
```json
{
  "name": "Wireless Mouse",
  "price": 29.99,
  "stockQuantity": 100
}
```

**Request Body (Complete Example):**
```json
{
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting",
  "price": 79.99,
  "costPrice": 35.00,
  "stockQuantity": 150,
  "category": "Electronics",
  "sku": "WGM001",
  "brand": "GameTech",
  "modelNumber": "GT-WGM-2024",
  "barcode": "1234567890124",
  "weight": 0.12,
  "length": 12.5,
  "width": 6.8,
  "height": 4.2,
  "productStatus": "ACTIVE",
  "minStockLevel": 20,
  "maxStockLevel": 300,
  "reorderPoint": 30,
  "reorderQuantity": 50,
  "supplierName": "Gaming Peripherals Co",
  "supplierCode": "GPC001",
  "warrantyPeriod": 12,
  "manufacturingDate": "2024-06-01",
  "tags": ["mouse", "gaming", "wireless", "rgb"],
  "imageUrl": "https://example.com/gaming-mouse.jpg",
  "additionalImages": [
    "https://example.com/gaming-mouse-side.jpg",
    "https://example.com/gaming-mouse-bottom.jpg"
  ],
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "B2-C3-D4",
  "notes": "Popular gaming accessory"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 2,
  "name": "Wireless Gaming Mouse",
  "description": "High-precision wireless gaming mouse with RGB lighting",
  "price": 79.99,
  "costPrice": 35.00,
  "stockQuantity": 150,
  "category": "Electronics",
  "sku": "WGM001",
  "brand": "GameTech",
  "modelNumber": "GT-WGM-2024",
  "barcode": "1234567890124",
  "weight": 0.12,
  "length": 12.5,
  "width": 6.8,
  "height": 4.2,
  "productStatus": "ACTIVE",
  "minStockLevel": 20,
  "maxStockLevel": 300,
  "reorderPoint": 30,
  "reorderQuantity": 50,
  "supplierName": "Gaming Peripherals Co",
  "supplierCode": "GPC001",
  "warrantyPeriod": 12,
  "expiryDate": null,
  "manufacturingDate": "2024-06-01",
  "tags": ["mouse", "gaming", "wireless", "rgb"],
  "imageUrl": "https://example.com/gaming-mouse.jpg",
  "additionalImages": [
    "https://example.com/gaming-mouse-side.jpg",
    "https://example.com/gaming-mouse-bottom.jpg"
  ],
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "B2-C3-D4",
  "totalSold": 0,
  "totalRevenue": 0.0,
  "lastSoldDate": null,
  "lastRestockedDate": null,
  "notes": "Popular gaming accessory",
  "createdAt": "2025-07-05T10:00:00",
  "updatedAt": "2025-07-05T10:00:00"
}
```

**Validation Error Response (400 Bad Request):**
```json
{
  "message": "Validation failed",
  "errors": [
    "Product name is required",
    "Price must be greater than 0"
  ],
  "timestamp": "2025-07-05T10:00:00"
}
```

---

### 4. Update Product

**Method:** `PUT`
**URL:** `http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example URL:**
```
PUT http://localhost:8081/api/products/2
```

**Request Body:**
```json
{
  "name": "Wireless Gaming Mouse Pro",
  "description": "Enhanced high-precision wireless gaming mouse with RGB lighting and programmable buttons",
  "price": 89.99,
  "costPrice": 40.00,
  "stockQuantity": 120,
  "category": "Electronics",
  "sku": "WGM001",
  "brand": "GameTech",
  "modelNumber": "GT-WGM-PRO-2024",
  "barcode": "1234567890124",
  "weight": 0.13,
  "length": 12.5,
  "width": 6.8,
  "height": 4.2,
  "productStatus": "ACTIVE",
  "minStockLevel": 25,
  "maxStockLevel": 300,
  "reorderPoint": 35,
  "reorderQuantity": 60,
  "supplierName": "Gaming Peripherals Co",
  "supplierCode": "GPC001",
  "warrantyPeriod": 18,
  "manufacturingDate": "2024-06-01",
  "tags": ["mouse", "gaming", "wireless", "rgb", "pro"],
  "imageUrl": "https://example.com/gaming-mouse-pro.jpg",
  "additionalImages": [
    "https://example.com/gaming-mouse-pro-side.jpg",
    "https://example.com/gaming-mouse-pro-bottom.jpg"
  ],
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 5.0,
  "locationInWarehouse": "B2-C3-D4",
  "notes": "Upgraded version with enhanced features"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 2,
  "name": "Wireless Gaming Mouse Pro",
  "description": "Enhanced high-precision wireless gaming mouse with RGB lighting and programmable buttons",
  "price": 89.99,
  "costPrice": 40.00,
  "stockQuantity": 120,
  "category": "Electronics",
  "sku": "WGM001",
  "brand": "GameTech",
  "modelNumber": "GT-WGM-PRO-2024",
  "barcode": "1234567890124",
  "weight": 0.13,
  "length": 12.5,
  "width": 6.8,
  "height": 4.2,
  "productStatus": "ACTIVE",
  "minStockLevel": 25,
  "maxStockLevel": 300,
  "reorderPoint": 35,
  "reorderQuantity": 60,
  "supplierName": "Gaming Peripherals Co",
  "supplierCode": "GPC001",
  "warrantyPeriod": 18,
  "expiryDate": null,
  "manufacturingDate": "2024-06-01",
  "tags": ["mouse", "gaming", "wireless", "rgb", "pro"],
  "imageUrl": "https://example.com/gaming-mouse-pro.jpg",
  "additionalImages": [
    "https://example.com/gaming-mouse-pro-side.jpg",
    "https://example.com/gaming-mouse-pro-bottom.jpg"
  ],
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 5.0,
  "locationInWarehouse": "B2-C3-D4",
  "totalSold": 0,
  "totalRevenue": 0.0,
  "lastSoldDate": null,
  "lastRestockedDate": null,
  "notes": "Upgraded version with enhanced features",
  "createdAt": "2025-07-05T10:00:00",
  "updatedAt": "2025-07-05T10:15:00"
}
```

---

### 5. Delete Product

**Method:** `DELETE`
**URL:** `http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example URLs:**
```
DELETE http://localhost:8081/api/products/2
DELETE http://localhost:8081/api/products/5
```

**Expected Response (204 No Content):**
```
(Empty response body)
```

**Error Response (404 Not Found):**
```json
{
  "message": "Product not found with id: 999",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

### 6. Search Products

**Method:** `GET`
**URL:** `http://localhost:8081/api/products/search`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters:**
- `query` (required) - Search term
- `page=0` (default: 0)
- `size=10` (default: 10)

**Example URLs:**
```
GET http://localhost:8081/api/products/search?query=laptop
GET http://localhost:8081/api/products/search?query=gaming&page=0&size=5
GET http://localhost:8081/api/products/search?query=mouse&page=1&size=20
GET http://localhost:8081/api/products/search?query=electronics
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro",
      "description": "High-performance laptop",
      "price": 1299.99,
      "costPrice": 800.00,
      "stockQuantity": 50,
      "category": "Electronics",
      "sku": "LAP001",
      "brand": "TechBrand",
      "modelNumber": "TB-LP-2024",
      "barcode": "1234567890123",
      "weight": 2.5,
      "productStatus": "ACTIVE",
      "minStockLevel": 10,
      "maxStockLevel": 100,
      "reorderPoint": 15,
      "reorderQuantity": 25,
      "supplierName": "Tech Supplier Inc",
      "supplierCode": "TS001",
      "warrantyPeriod": 24,
      "expiryDate": null,
      "manufacturingDate": "2024-01-15",
      "tags": ["laptop", "computer", "portable"],
      "imageUrl": "https://example.com/laptop.jpg",
      "isSerialized": true,
      "isDigital": false,
      "isTaxable": true,
      "taxRate": 8.5,
      "unitOfMeasure": "piece",
      "discountPercentage": 0.0,
      "locationInWarehouse": "A1-B2-C3",
      "totalSold": 150,
      "totalRevenue": 194999.85,
      "lastSoldDate": "2025-07-01T14:30:00",
      "lastRestockedDate": "2025-06-15T09:00:00",
      "notes": "Popular model",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-07-01T14:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1
}
```

---

## 📦 Stock Management Endpoints

### 7. Update Product Stock (Direct)

**Method:** `PUT`
**URL:** `http://localhost:8081/api/products/{id}/stock`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example URL:**
```
PUT http://localhost:8081/api/products/1/stock
```

**Request Body:**
```json
{
  "stockQuantity": 75
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop Pro",
  "description": "High-performance laptop",
  "price": 1299.99,
  "costPrice": 800.00,
  "stockQuantity": 75,
  "category": "Electronics",
  "sku": "LAP001",
  "brand": "TechBrand",
  "modelNumber": "TB-LP-2024",
  "barcode": "1234567890123",
  "weight": 2.5,
  "length": 35.0,
  "width": 25.0,
  "height": 2.0,
  "productStatus": "ACTIVE",
  "minStockLevel": 10,
  "maxStockLevel": 100,
  "reorderPoint": 15,
  "reorderQuantity": 25,
  "supplierName": "Tech Supplier Inc",
  "supplierCode": "TS001",
  "warrantyPeriod": 24,
  "expiryDate": null,
  "manufacturingDate": "2024-01-15",
  "tags": ["laptop", "computer", "portable"],
  "imageUrl": "https://example.com/laptop.jpg",
  "additionalImages": ["https://example.com/laptop2.jpg"],
  "isSerialized": true,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "A1-B2-C3",
  "totalSold": 150,
  "totalRevenue": 194999.85,
  "lastSoldDate": "2025-07-01T14:30:00",
  "lastRestockedDate": "2025-07-05T10:00:00",
  "notes": "Popular model",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-07-05T10:00:00"
}
```

**Error Response (400 Bad Request) - Missing stockQuantity:**
```json
{
  "message": "Stock quantity is required",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

### 8. Increase Product Stock

**Method:** `POST`
**URL:** `http://localhost:8081/api/products/{id}/stock/increase`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example URL:**
```
POST http://localhost:8081/api/products/1/stock/increase
```

**Request Body:**
```json
{
  "quantity": 25
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop Pro",
  "description": "High-performance laptop",
  "price": 1299.99,
  "costPrice": 800.00,
  "stockQuantity": 100,
  "category": "Electronics",
  "sku": "LAP001",
  "brand": "TechBrand",
  "modelNumber": "TB-LP-2024",
  "barcode": "1234567890123",
  "weight": 2.5,
  "length": 35.0,
  "width": 25.0,
  "height": 2.0,
  "productStatus": "ACTIVE",
  "minStockLevel": 10,
  "maxStockLevel": 100,
  "reorderPoint": 15,
  "reorderQuantity": 25,
  "supplierName": "Tech Supplier Inc",
  "supplierCode": "TS001",
  "warrantyPeriod": 24,
  "expiryDate": null,
  "manufacturingDate": "2024-01-15",
  "tags": ["laptop", "computer", "portable"],
  "imageUrl": "https://example.com/laptop.jpg",
  "additionalImages": ["https://example.com/laptop2.jpg"],
  "isSerialized": true,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "A1-B2-C3",
  "totalSold": 150,
  "totalRevenue": 194999.85,
  "lastSoldDate": "2025-07-01T14:30:00",
  "lastRestockedDate": "2025-07-05T10:00:00",
  "notes": "Popular model",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-07-05T10:00:00"
}
```

**Error Response (400 Bad Request) - Invalid quantity:**
```json
{
  "message": "Quantity must be greater than 0",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

### 9. Reduce Product Stock

**Method:** `POST`
**URL:** `http://localhost:8081/api/products/{id}/stock/reduce`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example URL:**
```
POST http://localhost:8081/api/products/1/stock/reduce
```

**Request Body:**
```json
{
  "quantity": 10
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Laptop Pro",
  "description": "High-performance laptop",
  "price": 1299.99,
  "costPrice": 800.00,
  "stockQuantity": 90,
  "category": "Electronics",
  "sku": "LAP001",
  "brand": "TechBrand",
  "modelNumber": "TB-LP-2024",
  "barcode": "1234567890123",
  "weight": 2.5,
  "length": 35.0,
  "width": 25.0,
  "height": 2.0,
  "productStatus": "ACTIVE",
  "minStockLevel": 10,
  "maxStockLevel": 100,
  "reorderPoint": 15,
  "reorderQuantity": 25,
  "supplierName": "Tech Supplier Inc",
  "supplierCode": "TS001",
  "warrantyPeriod": 24,
  "expiryDate": null,
  "manufacturingDate": "2024-01-15",
  "tags": ["laptop", "computer", "portable"],
  "imageUrl": "https://example.com/laptop.jpg",
  "additionalImages": ["https://example.com/laptop2.jpg"],
  "isSerialized": true,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "discountPercentage": 0.0,
  "locationInWarehouse": "A1-B2-C3",
  "totalSold": 160,
  "totalRevenue": 207999.84,
  "lastSoldDate": "2025-07-05T10:00:00",
  "lastRestockedDate": "2025-07-05T10:00:00",
  "notes": "Popular model",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-07-05T10:00:00"
}
```

**Error Response (400 Bad Request) - Insufficient stock:**
```json
{
  "message": "Insufficient stock. Current stock: 5, requested reduction: 10",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

## 🧪 Testing Scenarios

### Scenario 1: Complete Product Lifecycle
1. **Create a new product** (POST /api/products)
2. **Get the created product** (GET /api/products/{id})
3. **Update the product** (PUT /api/products/{id})
4. **Search for the product** (GET /api/products/search?query=productname)
5. **Update stock levels** (PUT /api/products/{id}/stock)
6. **Delete the product** (DELETE /api/products/{id})

### Scenario 2: Stock Management Testing
1. **Create a product with initial stock** (POST /api/products)
2. **Increase stock** (POST /api/products/{id}/stock/increase)
3. **Reduce stock** (POST /api/products/{id}/stock/reduce)
4. **Set specific stock level** (PUT /api/products/{id}/stock)
5. **Try to reduce more stock than available** (should fail)

### Scenario 3: Pagination and Filtering
1. **Get all products with default pagination** (GET /api/products)
2. **Get products with custom page size** (GET /api/products?size=5)
3. **Get products sorted by price** (GET /api/products?sortBy=price&sortDir=desc)
4. **Filter by category** (GET /api/products?category=Electronics)
5. **Search with pagination** (GET /api/products/search?query=laptop&page=1&size=5)

---

## ❌ Common Error Responses

### 401 Unauthorized
```json
{
  "message": "Unauthorized",
  "timestamp": "2025-07-05T10:00:00"
}
```
**Cause:** Missing or invalid JWT token

### 400 Bad Request - Validation Errors
```json
{
  "message": "Validation failed",
  "errors": [
    "Product name is required",
    "Price must be greater than 0",
    "Stock quantity cannot be negative"
  ],
  "timestamp": "2025-07-05T10:00:00"
}
```

### 404 Not Found
```json
{
  "message": "Product not found with id: 999",
  "timestamp": "2025-07-05T10:00:00"
}
```

### 500 Internal Server Error
```json
{
  "message": "Internal server error",
  "timestamp": "2025-07-05T10:00:00"
}
```

---

## 📋 Postman Collection Setup

### Environment Variables
Create a Postman environment with these variables:

```
baseUrl: http://localhost:8081/api
authToken: (will be set after login)
productId: (will be set after creating a product)
```

### Pre-request Script for Authentication
Add this to your collection's pre-request script:

```javascript
// Check if we have a valid token
if (!pm.environment.get("authToken")) {
    console.log("No auth token found, please login first");
}
```

### Test Scripts Examples

**For Create Product (POST /api/products):**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Product created successfully", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('name');

    // Save product ID for future tests
    pm.environment.set("productId", jsonData.id);
});
```

**For Get Product (GET /api/products/{id}):**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Product data is valid", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('name');
    pm.expect(jsonData).to.have.property('price');
    pm.expect(jsonData).to.have.property('stockQuantity');
});
```

---

## 🔧 Troubleshooting

### Issue: 401 Unauthorized
**Solution:**
1. Login first using POST /api/auth/login
2. Copy the accessToken from response
3. Add it to Authorization header: `Bearer <token>`

### Issue: 400 Bad Request on Create/Update
**Solution:**
1. Check required fields: name, price are mandatory
2. Ensure price is greater than 0
3. Ensure stockQuantity is not negative
4. Verify JSON format is correct

### Issue: 404 Not Found
**Solution:**
1. Verify the product ID exists
2. Check if product was deleted
3. Ensure correct endpoint URL

### Issue: Search returns empty results
**Solution:**
1. Check if search query matches product names/descriptions
2. Verify products exist in the database
3. Try broader search terms

---

## 📝 Notes

1. **Authentication Required:** All endpoints require valid JWT token
2. **Data Validation:** Server validates all input data
3. **Pagination:** Most list endpoints support pagination
4. **Case Sensitivity:** Search is typically case-insensitive
5. **Stock Management:** Stock levels are automatically updated during sales
6. **Timestamps:** All timestamps are in ISO 8601 format
7. **Decimal Precision:** Prices support up to 2 decimal places

---

*This documentation provides comprehensive testing examples for all ProductController endpoints. Use these examples in Postman to test your Sales Management System backend.*
