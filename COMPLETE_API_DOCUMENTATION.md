# Sales Management System - Complete API Documentation

## Base URL
```
http://localhost:8081/api
```

## Authentication

All endpoints except authentication endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

---

## 🔐 Authentication Endpoints

### 1. User Registration
**Request:** `POST http://localhost:8081/api/auth/signup`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "createdAt": "2025-07-05T10:00:00"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Username is already taken!",
  "timestamp": "2025-07-05T10:00:00"
}
```

### 2. User Login
**Request:** `POST http://localhost:8081/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid username or password",
  "timestamp": "2025-07-05T10:00:00"
}
```

### 3. Refresh Token
**Request:** `POST http://localhost:8081/api/auth/refresh`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

---

## 👥 Customer Management

### 1. Get All Customers
**Request:** `GET http://localhost:8081/api/customers?page=0&size=10&sortBy=id&sortDir=asc`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction (asc/desc)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "John Doe",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "phone": "+1234567890",
      "address": "123 Main St",
      "dateOfBirth": "1990-01-01",
      "gender": "MALE",
      "customerType": "REGULAR",
      "customerStatus": "ACTIVE",
      "billingAddress": "123 Main St",
      "shippingAddress": "123 Main St",
      "preferredPaymentMethod": "CREDIT_CARD",
      "creditLimit": 5000.00,
      "currentBalance": 0.00,
      "loyaltyPoints": 100,
      "taxNumber": "TAX123456",
      "companyName": "Doe Corp",
      "website": "https://doecorp.com",
      "notes": "VIP Customer",
      "lastPurchaseDate": "2025-07-01T10:00:00",
      "totalPurchases": 15000.00,
      "isEmailVerified": true,
      "isPhoneVerified": true,
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-07-01T10:00:00"
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

### 2. Get Customer by ID
**Request:** `GET http://localhost:8081/api/customers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main St",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "customerType": "REGULAR",
  "customerStatus": "ACTIVE",
  "billingAddress": "123 Main St",
  "shippingAddress": "123 Main St",
  "preferredPaymentMethod": "CREDIT_CARD",
  "creditLimit": 5000.00,
  "currentBalance": 0.00,
  "loyaltyPoints": 100,
  "taxNumber": "TAX123456",
  "companyName": "Doe Corp",
  "website": "https://doecorp.com",
  "notes": "VIP Customer",
  "lastPurchaseDate": "2025-07-01T10:00:00",
  "totalPurchases": 15000.00,
  "isEmailVerified": true,
  "isPhoneVerified": true,
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-07-01T10:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Customer not found with id: 1",
  "timestamp": "2025-07-05T10:00:00"
}
```

### 3. Create Customer
**Request:** `POST http://localhost:8081/api/customers`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Jane Smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "phone": "+1234567891",
  "address": "456 Oak Ave",
  "dateOfBirth": "1985-05-15",
  "gender": "FEMALE",
  "customerType": "PREMIUM",
  "customerStatus": "ACTIVE",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave",
  "preferredPaymentMethod": "DEBIT_CARD",
  "creditLimit": 10000.00,
  "loyaltyPoints": 0,
  "taxNumber": "TAX789012",
  "companyName": "Smith LLC",
  "website": "https://smithllc.com",
  "notes": "New premium customer"
}
```

**Response (201 Created):**
```json
{
  "id": 2,
  "name": "Jane Smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "phone": "+1234567891",
  "address": "456 Oak Ave",
  "dateOfBirth": "1985-05-15",
  "gender": "FEMALE",
  "customerType": "PREMIUM",
  "customerStatus": "ACTIVE",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave",
  "preferredPaymentMethod": "DEBIT_CARD",
  "creditLimit": 10000.00,
  "currentBalance": 0.00,
  "loyaltyPoints": 0,
  "taxNumber": "TAX789012",
  "companyName": "Smith LLC",
  "website": "https://smithllc.com",
  "notes": "New premium customer",
  "lastPurchaseDate": null,
  "totalPurchases": 0.00,
  "isEmailVerified": false,
  "isPhoneVerified": false,
  "createdAt": "2025-07-05T10:00:00",
  "updatedAt": "2025-07-05T10:00:00"
}
```

### 4. Update Customer
**Request:** `PUT http://localhost:8081/api/customers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Customer

**Response (200 OK):** Updated customer object

### 5. Delete Customer
**Request:** `DELETE http://localhost:8081/api/customers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Search Customers
**Request:** `GET http://localhost:8081/api/customers/search?query=john&page=0&size=10`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Response (200 OK):** Paginated customer list matching search criteria

---

## 📦 Product Management

### 1. Get All Products
**Request:** `GET http://localhost:8081/api/products?page=0&size=10&sortBy=id&sortDir=asc&category=Electronics`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction
- `category` (string, optional) - Filter by category

**Response (200 OK):**
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
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Product by ID
**Request:** `GET http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Single product object (same structure as above)

### 3. Create Product
**Request:** `POST http://localhost:8081/api/products`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "costPrice": 15.00,
  "stockQuantity": 100,
  "category": "Electronics",
  "sku": "MOU001",
  "brand": "TechBrand",
  "modelNumber": "TB-WM-2024",
  "barcode": "1234567890124",
  "weight": 0.1,
  "length": 12.0,
  "width": 6.0,
  "height": 4.0,
  "productStatus": "ACTIVE",
  "minStockLevel": 20,
  "maxStockLevel": 200,
  "reorderPoint": 30,
  "reorderQuantity": 50,
  "supplierName": "Tech Supplier Inc",
  "supplierCode": "TS001",
  "warrantyPeriod": 12,
  "manufacturingDate": "2024-06-01",
  "tags": ["mouse", "wireless", "computer"],
  "imageUrl": "https://example.com/mouse.jpg",
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.5,
  "unitOfMeasure": "piece",
  "locationInWarehouse": "B2-C3-D4",
  "notes": "Best seller"
}
```

**Response (201 Created):** Created product object

### 4. Update Product
**Request:** `PUT http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Product

**Response (200 OK):** Updated product object

### 5. Delete Product
**Request:** `DELETE http://localhost:8081/api/products/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Search Products
**Request:** `GET http://localhost:8081/api/products/search?query=laptop&page=0&size=10&sortBy=name&sortDir=asc`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction

**Response (200 OK):** Paginated product list matching search criteria

### 7. Update Product Stock
**Request:** `PUT http://localhost:8081/api/products/{id}/stock`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "stockQuantity": 75,
  "notes": "Stock adjustment after inventory count"
}
```

**Response (200 OK):** Updated product object

---

## 💰 Sales Management

### 1. Get All Sales
**Request:** `GET http://localhost:8081/api/sales?page=0&size=10&sortBy=id&sortDir=desc&status=COMPLETED&startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="desc") - Sort direction
- `status` (string, optional) - Filter by status (PENDING, COMPLETED, CANCELLED)
- `startDate` (datetime, optional) - Filter from date (ISO format)
- `endDate` (datetime, optional) - Filter to date (ISO format)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "saleDate": "2025-07-05T10:00:00",
      "totalAmount": 1329.98,
      "status": "COMPLETED",
      "saleNumber": "SALE-2025-0001",
      "referenceNumber": "REF-001",
      "subtotal": 1299.99,
      "discountAmount": 0.00,
      "discountPercentage": 0.0,
      "taxAmount": 29.99,
      "taxPercentage": 8.5,
      "shippingCost": 0.00,
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PAID",
      "paymentDate": "2025-07-05T10:05:00",
      "dueDate": "2025-07-05",
      "billingAddress": "123 Main St",
      "shippingAddress": "123 Main St",
      "salesPerson": "Alice Johnson",
      "salesChannel": "ONLINE",
      "saleType": "REGULAR",
      "currency": "USD",
      "exchangeRate": 1.0,
      "notes": "Customer requested express delivery",
      "internalNotes": "VIP customer",
      "termsAndConditions": "Standard terms apply",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "unitPrice": 1299.99,
          "totalPrice": 1299.99,
          "discountAmount": 0.00,
          "taxAmount": 29.99
        }
      ],
      "createdAt": "2025-07-05T10:00:00",
      "updatedAt": "2025-07-05T10:05:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Sale by ID
**Request:** `GET http://localhost:8081/api/sales/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Single sale object (same structure as above)

### 3. Create Sale
**Request:** `POST http://localhost:8081/api/sales`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "customerId": 1,
  "totalAmount": 59.98,
  "status": "PENDING",
  "saleNumber": "SALE-2025-0002",
  "referenceNumber": "REF-002",
  "subtotal": 59.98,
  "discountAmount": 0.00,
  "discountPercentage": 0.0,
  "taxAmount": 5.10,
  "taxPercentage": 8.5,
  "shippingCost": 0.00,
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "dueDate": "2025-07-06",
  "billingAddress": "123 Main St",
  "shippingAddress": "123 Main St",
  "salesPerson": "Bob Smith",
  "salesChannel": "IN_STORE",
  "saleType": "REGULAR",
  "currency": "USD",
  "exchangeRate": 1.0,
  "notes": "Customer will pick up tomorrow",
  "items": [
    {
      "productId": 2,
      "quantity": 2,
      "unitPrice": 29.99
    }
  ]
}
```

**Response (201 Created):** Created sale object

### 4. Update Sale
**Request:** `PUT http://localhost:8081/api/sales/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Sale

**Response (200 OK):** Updated sale object

### 5. Delete Sale
**Request:** `DELETE http://localhost:8081/api/sales/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Get Sales by Customer
**Request:** `GET http://localhost:8081/api/sales/customer/{customerId}?page=0&size=10`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Paginated sales list for specific customer

---

## 🏢 Supplier Management

### 1. Get All Suppliers
**Request:** `GET http://localhost:8081/api/suppliers?page=0&size=10&sortBy=id&sortDir=asc&status=ACTIVE`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction
- `status` (string, optional) - Filter by status (ACTIVE, INACTIVE, SUSPENDED)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Tech Supplier Inc",
      "contactPerson": "John Smith",
      "phone": "+1234567890",
      "email": "contact@techsupplier.com",
      "address": "123 Business Ave",
      "city": "Tech City",
      "country": "USA",
      "taxNumber": "TAX-SUP-001",
      "paymentTerms": "NET_30",
      "deliveryTerms": "FOB_DESTINATION",
      "rating": 4.5,
      "status": "ACTIVE",
      "totalOrders": 25,
      "totalAmount": 125000.00,
      "lastOrderDate": "2025-07-01T10:00:00",
      "notes": "Reliable supplier for electronics",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-07-01T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Supplier by ID
**Request:** `GET http://localhost:8081/api/suppliers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Single supplier object (same structure as above)

### 3. Create Supplier
**Request:** `POST http://localhost:8081/api/suppliers`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Office Supplies Co",
  "contactPerson": "Jane Doe",
  "phone": "+1234567891",
  "email": "sales@officesupplies.com",
  "address": "456 Commerce St",
  "city": "Business City",
  "country": "USA",
  "taxNumber": "TAX-SUP-002",
  "paymentTerms": "NET_15",
  "deliveryTerms": "FOB_ORIGIN",
  "rating": 4.0,
  "status": "ACTIVE",
  "notes": "Specializes in office equipment"
}
```

**Response (201 Created):** Created supplier object

### 4. Update Supplier
**Request:** `PUT http://localhost:8081/api/suppliers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Supplier

**Response (200 OK):** Updated supplier object

### 5. Delete Supplier
**Request:** `DELETE http://localhost:8081/api/suppliers/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Search Suppliers
**Request:** `GET http://localhost:8081/api/suppliers/search?query=tech&page=0&size=10&sortBy=name&sortDir=asc`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction

**Response (200 OK):** Paginated supplier list matching search criteria

### 7. Get Supplier Analytics
**Request:** `GET http://localhost:8081/api/suppliers/{id}/analytics`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "supplierId": 1,
  "supplierName": "Tech Supplier Inc",
  "totalOrders": 25,
  "totalAmount": 125000.00,
  "averageOrderValue": 5000.00,
  "lastOrderDate": "2025-07-01T10:00:00",
  "rating": 4.5,
  "onTimeDeliveryRate": 95.0,
  "qualityRating": 4.8,
  "monthlyOrderTrends": [
    {
      "month": "2025-01",
      "orderCount": 5,
      "totalAmount": 25000.00
    },
    {
      "month": "2025-02",
      "orderCount": 4,
      "totalAmount": 20000.00
    }
  ]
}
```

---

## 🔄 Returns Management

### 1. Get All Returns
**Request:** `GET http://localhost:8081/api/returns?page=0&size=10&sortBy=id&sortDir=desc&status=PENDING`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="desc") - Sort direction
- `status` (string, optional) - Filter by status (PENDING, APPROVED, REJECTED, REFUNDED, EXCHANGED)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "returnNumber": "RET-2025-0001",
      "originalSaleId": 1,
      "originalSaleNumber": "SALE-2025-0001",
      "customerId": 1,
      "customerName": "John Doe",
      "returnDate": "2025-07-05T15:00:00",
      "reason": "DEFECTIVE",
      "status": "PENDING",
      "totalRefundAmount": 1329.98,
      "notes": "Screen flickering issue",
      "processedBy": null,
      "processedDate": null,
      "refundMethod": "ORIGINAL_PAYMENT",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Laptop Pro",
          "quantity": 1,
          "unitPrice": 1299.99,
          "totalRefundAmount": 1329.98,
          "reason": "DEFECTIVE",
          "condition": "DAMAGED"
        }
      ],
      "createdAt": "2025-07-05T15:00:00",
      "updatedAt": "2025-07-05T15:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Return by ID
**Request:** `GET http://localhost:8081/api/returns/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Single return object (same structure as above)

### 3. Create Return
**Request:** `POST http://localhost:8081/api/returns`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "originalSaleId": 1,
  "customerId": 1,
  "reason": "CUSTOMER_CHANGE_MIND",
  "totalRefundAmount": 59.98,
  "notes": "Customer no longer needs the item",
  "refundMethod": "STORE_CREDIT",
  "items": [
    {
      "productId": 2,
      "quantity": 2,
      "unitPrice": 29.99,
      "reason": "CUSTOMER_CHANGE_MIND",
      "condition": "NEW"
    }
  ]
}
```

**Response (201 Created):** Created return object

### 4. Update Return
**Request:** `PUT http://localhost:8081/api/returns/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Return

**Response (200 OK):** Updated return object

### 5. Delete Return
**Request:** `DELETE http://localhost:8081/api/returns/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Approve Return
**Request:** `POST http://localhost:8081/api/returns/{id}/approve`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "notes": "Return approved after inspection",
  "processedBy": "Manager Alice"
}
```

**Response (200 OK):** Updated return object with APPROVED status

### 7. Reject Return
**Request:** `POST http://localhost:8081/api/returns/{id}/reject`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "notes": "Return rejected - item shows signs of misuse",
  "processedBy": "Manager Alice"
}
```

**Response (200 OK):** Updated return object with REJECTED status

### 8. Process Refund
**Request:** `POST http://localhost:8081/api/returns/{id}/refund`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "refundMethod": "ORIGINAL_PAYMENT",
  "notes": "Refund processed to original payment method",
  "processedBy": "Manager Alice"
}
```

**Response (200 OK):** Updated return object with REFUNDED status

---

## 🎯 Promotions Management

### 1. Get All Promotions
**Request:** `GET http://localhost:8081/api/promotions?page=0&size=10&sortBy=id&sortDir=desc&isActive=true`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="desc") - Sort direction
- `isActive` (boolean, optional) - Filter by active status

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Summer Sale 2025",
      "description": "20% off on all electronics",
      "type": "PERCENTAGE",
      "discountValue": 20.0,
      "minimumOrderAmount": 100.00,
      "maximumDiscountAmount": 500.00,
      "startDate": "2025-06-01T00:00:00",
      "endDate": "2025-08-31T23:59:59",
      "isActive": true,
      "applicableProducts": [1, 2, 3],
      "applicableCategories": ["Electronics"],
      "usageLimit": 1000,
      "usageCount": 150,
      "customerEligibility": "ALL",
      "couponCode": "SUMMER20",
      "autoApply": false,
      "stackable": true,
      "statusDisplay": "Active",
      "typeDisplay": "Percentage Discount",
      "eligibilityDisplay": "All Customers",
      "isCurrentlyActive": true,
      "isExpired": false,
      "isNotYetStarted": false,
      "isUsageLimitReached": false,
      "daysUntilExpiry": 57,
      "remainingUsage": 850,
      "usagePercentage": 15.0,
      "createdAt": "2025-05-15T10:00:00",
      "updatedAt": "2025-07-05T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Promotion by ID
**Request:** `GET http://localhost:8081/api/promotions/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Single promotion object (same structure as above)

### 3. Create Promotion
**Request:** `POST http://localhost:8081/api/promotions`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:**
```json
{
  "name": "Black Friday 2025",
  "description": "Massive discounts on all products",
  "type": "PERCENTAGE",
  "discountValue": 30.0,
  "minimumOrderAmount": 50.00,
  "maximumDiscountAmount": 1000.00,
  "startDate": "2025-11-29T00:00:00",
  "endDate": "2025-11-29T23:59:59",
  "isActive": false,
  "applicableProducts": [],
  "applicableCategories": ["Electronics", "Clothing", "Home"],
  "usageLimit": 5000,
  "customerEligibility": "ALL",
  "couponCode": "BLACKFRIDAY30",
  "autoApply": true,
  "stackable": false
}
```

**Response (201 Created):** Created promotion object

### 4. Update Promotion
**Request:** `PUT http://localhost:8081/api/promotions/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
Content-Type: application/json
```

**Body:** Same as Create Promotion

**Response (200 OK):** Updated promotion object

### 5. Delete Promotion
**Request:** `DELETE http://localhost:8081/api/promotions/{id}`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (204 No Content):** Empty response body

### 6. Activate Promotion
**Request:** `POST http://localhost:8081/api/promotions/{id}/activate`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Updated promotion object with isActive=true

### 7. Deactivate Promotion
**Request:** `POST http://localhost:8081/api/promotions/{id}/deactivate`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** Updated promotion object with isActive=false

### 8. Get Active Promotions
**Request:** `GET http://localhost:8081/api/promotions/active`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):** List of currently active promotions

### 9. Get Promotion Analytics
**Request:** `GET http://localhost:8081/api/promotions/{id}/analytics`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "promotionId": 1,
  "promotionName": "Summer Sale 2025",
  "totalUsage": 150,
  "totalDiscountGiven": 15000.00,
  "totalRevenue": 75000.00,
  "conversionRate": 12.5,
  "averageOrderValue": 500.00,
  "topProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro",
      "usageCount": 50,
      "discountGiven": 6500.00
    }
  ],
  "dailyUsage": [
    {
      "date": "2025-07-01",
      "usageCount": 10,
      "discountGiven": 1000.00
    }
  ]
}
```

---

## 📊 Reports and Analytics

### 1. Sales Report
**Request:** `GET http://localhost:8081/api/reports/sales?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `startDate` (datetime, required) - Start date (ISO format)
- `endDate` (datetime, required) - End date (ISO format)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  },
  "summary": {
    "totalSales": 50,
    "totalRevenue": 125000.00,
    "averageOrderValue": 2500.00,
    "totalItemsSold": 200,
    "totalCustomers": 25
  },
  "salesByStatus": {
    "COMPLETED": 45,
    "PENDING": 3,
    "CANCELLED": 2
  },
  "salesByPaymentMethod": {
    "CREDIT_CARD": 30,
    "CASH": 15,
    "DEBIT_CARD": 5
  },
  "topSellingProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro",
      "quantitySold": 50,
      "revenue": 64999.50
    }
  ],
  "salesTrends": [
    {
      "date": "2025-01-01",
      "salesCount": 2,
      "revenue": 2599.98
    }
  ]
}
```

### 2. Revenue Report
**Request:** `GET http://localhost:8081/api/reports/revenue?months=6`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `months` (int, default=6) - Number of months to include

**Response (200 OK):**
```json
{
  "period": "Last 6 months",
  "totalRevenue": 125000.00,
  "averageMonthlyRevenue": 20833.33,
  "growthRate": 15.5,
  "monthlyTrends": [
    {
      "month": "2025-01",
      "revenue": 18000.00,
      "salesCount": 8,
      "averageOrderValue": 2250.00
    },
    {
      "month": "2025-02",
      "revenue": 22000.00,
      "salesCount": 10,
      "averageOrderValue": 2200.00
    }
  ],
  "revenueByCategory": {
    "Electronics": 75000.00,
    "Clothing": 30000.00,
    "Home": 20000.00
  },
  "revenueByPaymentMethod": {
    "CREDIT_CARD": 87500.00,
    "CASH": 25000.00,
    "DEBIT_CARD": 12500.00
  }
}
```

### 3. Top Products Report
**Request:** `GET http://localhost:8081/api/reports/top-products?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Query Parameters:**
- `startDate` (datetime, required) - Start date (ISO format)
- `endDate` (datetime, required) - End date (ISO format)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-01-01T00:00:00",
    "endDate": "2025-12-31T23:59:59"
  },
  "topSellingProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro",
      "category": "Electronics",
      "quantitySold": 50,
      "revenue": 64999.50,
      "averagePrice": 1299.99,
      "profitMargin": 38.46
    }
  ],
  "topRevenueProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro",
      "revenue": 64999.50,
      "quantitySold": 50
    }
  ],
  "categoryPerformance": [
    {
      "category": "Electronics",
      "totalRevenue": 95000.00,
      "totalQuantity": 120,
      "productCount": 15
    }
  ]
}
```

### 4. Customer Analytics
**Request:** `GET http://localhost:8081/api/reports/customer-analytics`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "totalCustomers": 150,
  "newCustomersThisMonth": 12,
  "activeCustomers": 125,
  "customerGrowthRate": 8.7,
  "averageCustomerValue": 2500.00,
  "customerRetentionRate": 85.5,
  "customersByType": {
    "REGULAR": 100,
    "PREMIUM": 35,
    "VIP": 15
  },
  "customersByStatus": {
    "ACTIVE": 125,
    "INACTIVE": 20,
    "SUSPENDED": 5
  },
  "topCustomers": [
    {
      "customerId": 1,
      "customerName": "John Doe",
      "totalPurchases": 15000.00,
      "orderCount": 8,
      "averageOrderValue": 1875.00,
      "lastPurchaseDate": "2025-07-01T10:00:00"
    }
  ],
  "customerAcquisitionTrends": [
    {
      "month": "2025-01",
      "newCustomers": 15,
      "totalRevenue": 37500.00
    }
  ]
}
```

### 5. Inventory Report
**Request:** `GET http://localhost:8081/api/reports/inventory`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "totalProducts": 250,
  "totalStockValue": 500000.00,
  "lowStockProducts": 15,
  "outOfStockProducts": 5,
  "expiredProducts": 2,
  "averageStockLevel": 45.5,
  "stockTurnoverRate": 6.2,
  "categoryBreakdown": [
    {
      "category": "Electronics",
      "productCount": 100,
      "totalValue": 300000.00,
      "averageStockLevel": 50
    }
  ],
  "lowStockAlerts": [
    {
      "productId": 5,
      "productName": "Wireless Headphones",
      "currentStock": 8,
      "reorderPoint": 15,
      "reorderQuantity": 25
    }
  ],
  "topValueProducts": [
    {
      "productId": 1,
      "productName": "Laptop Pro",
      "stockQuantity": 50,
      "unitValue": 1299.99,
      "totalValue": 64999.50
    }
  ]
}
```

### 6. Dashboard Summary
**Request:** `GET http://localhost:8081/api/reports/dashboard`

**Headers:**
```
Authorization: Bearer <your_jwt_token>
```

**Response (200 OK):**
```json
{
  "period": "Last 30 days",
  "generatedAt": "2025-07-05T10:00:00",
  "sales": {
    "totalSales": 25,
    "totalRevenue": 62500.00,
    "averageOrderValue": 2500.00,
    "growthRate": 12.5
  },
  "customers": {
    "totalCustomers": 150,
    "newCustomers": 8,
    "activeCustomers": 125,
    "retentionRate": 85.5
  },
  "inventory": {
    "totalProducts": 250,
    "lowStockAlerts": 15,
    "outOfStockProducts": 5,
    "totalStockValue": 500000.00
  },
  "revenue": {
    "monthlyRevenue": 62500.00,
    "yearlyRevenue": 750000.00,
    "profitMargin": 35.2,
    "topCategory": "Electronics"
  }
}
```

---

## ❌ Missing Endpoints (To Be Implemented)

The following endpoints are identified as missing from the current backend implementation based on frontend requirements:

### 🛒 Purchase Orders Management
```
GET    /api/purchase-orders                    - Get all purchase orders
POST   /api/purchase-orders                    - Create new purchase order
GET    /api/purchase-orders/{id}               - Get purchase order by ID
PUT    /api/purchase-orders/{id}               - Update purchase order
DELETE /api/purchase-orders/{id}               - Delete purchase order
POST   /api/purchase-orders/{id}/receive       - Mark order as received
GET    /api/suppliers/{id}/orders              - Get purchase orders for supplier
```

### 🎫 Coupons Management
```
GET    /api/coupons                            - Get all coupons
POST   /api/coupons                            - Create new coupon
GET    /api/coupons/{id}                       - Get coupon by ID
PUT    /api/coupons/{id}                       - Update coupon
DELETE /api/coupons/{id}                       - Delete coupon
POST   /api/coupons/validate                   - Validate coupon code
```

### 📦 Advanced Inventory Management
```
GET    /api/inventory/movements                - Get stock movements
POST   /api/inventory/movements                - Record stock movement
GET    /api/inventory/warehouses               - Get all warehouses
POST   /api/inventory/warehouses               - Create warehouse
GET    /api/inventory/alerts                   - Get inventory alerts
POST   /api/inventory/adjustments              - Stock adjustments
GET    /api/inventory/transfers                - Inter-warehouse transfers
POST   /api/inventory/transfers                - Create transfer
GET    /api/inventory/valuation                - Inventory valuation report
```

### ⚙️ Settings and Configuration
```
GET    /api/settings/system                    - Get system settings
PUT    /api/settings/system                    - Update system settings
GET    /api/settings/user                      - Get user preferences
PUT    /api/settings/user                      - Update user preferences
GET    /api/settings/company                   - Get company information
PUT    /api/settings/company                   - Update company information
GET    /api/settings/tax                       - Get tax configuration
PUT    /api/settings/tax                       - Update tax settings
GET    /api/settings/payment-methods           - Get payment methods
PUT    /api/settings/payment-methods           - Update payment methods
```

---

## 🔒 Authentication & Authorization

### JWT Token Structure
- **Header:** `Authorization: Bearer <jwt_token>`
- **Token Expiration:** 24 hours (86400000 ms)
- **Refresh Token:** Available for token renewal

### User Roles
- **USER** - Standard user with basic permissions
- **ADMIN** - Administrator with full system access
- **MANAGER** - Manager with elevated permissions

### Protected Endpoints
All endpoints except `/api/auth/*` require valid JWT authentication.

---

## 🌐 CORS Configuration
- **Allowed Origins:** `*` (configured for development)
- **Allowed Methods:** `GET, POST, PUT, DELETE, OPTIONS`
- **Allowed Headers:** `*`

---

## 📝 Error Response Format

All error responses follow this standard format:

```json
{
  "message": "Error description",
  "timestamp": "2025-07-05T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/endpoint"
}
```

### Common HTTP Status Codes
- **200 OK** - Successful GET, PUT requests
- **201 Created** - Successful POST requests
- **204 No Content** - Successful DELETE requests
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Missing or invalid authentication
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

## 📋 Notes for Frontend Developers

1. **Base URL:** Always use `http://localhost:8081/api` as the base URL
2. **Authentication:** Include JWT token in Authorization header for all protected endpoints
3. **Content-Type:** Use `application/json` for all POST/PUT requests
4. **Pagination:** Most list endpoints support pagination with `page`, `size`, `sortBy`, `sortDir` parameters
5. **Date Format:** Use ISO 8601 format for all date/datetime fields
6. **Error Handling:** Always check response status codes and handle errors appropriately
7. **Token Refresh:** Implement automatic token refresh using the refresh endpoint when access token expires

---

*This documentation covers all currently implemented endpoints and identifies missing endpoints that need to be developed to fully support the frontend requirements.*
