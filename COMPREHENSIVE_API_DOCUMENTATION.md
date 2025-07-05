# Sales Management System - Comprehensive API Documentation

## Base URL
```
http://localhost:8081/api
```

## Authentication Endpoints

### 1. User Registration
**Endpoint:** `POST /auth/signup`

**Request Body:**
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

**Response (201 Created):**
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
    "createdAt": "2025-07-04T10:00:00"
  }
}
```

### 2. User Login
**Endpoint:** `POST /auth/login`

**Request Body:**
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

### 3. Refresh Token
**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440001",
  "tokenType": "Bearer"
}
```

## Customer Endpoints

### 1. Get All Customers
**Endpoint:** `GET /customers`

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
      "dateOfBirth": "1990-01-15",
      "gender": "MALE",
      "customerType": "REGULAR",
      "customerStatus": "ACTIVE",
      "billingAddress": "123 Main St",
      "shippingAddress": "123 Main St",
      "preferredPaymentMethod": "CREDIT_CARD",
      "creditLimit": 5000.00,
      "currentBalance": 0.00,
      "loyaltyPoints": 150,
      "taxNumber": "TAX123456",
      "companyName": null,
      "website": null,
      "notes": "VIP customer",
      "lastPurchaseDate": "2025-07-01T14:30:00",
      "totalPurchases": 2500.00,
      "isEmailVerified": true,
      "isPhoneVerified": false,
      "createdAt": "2025-06-01T10:00:00",
      "updatedAt": "2025-07-04T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

### 2. Get Customer by ID
**Endpoint:** `GET /customers/{id}`

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
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "customerType": "REGULAR",
  "customerStatus": "ACTIVE",
  "billingAddress": "123 Main St",
  "shippingAddress": "123 Main St",
  "preferredPaymentMethod": "CREDIT_CARD",
  "creditLimit": 5000.00,
  "currentBalance": 0.00,
  "loyaltyPoints": 150,
  "taxNumber": "TAX123456",
  "companyName": null,
  "website": null,
  "notes": "VIP customer",
  "lastPurchaseDate": "2025-07-01T14:30:00",
  "totalPurchases": 2500.00,
  "isEmailVerified": true,
  "isPhoneVerified": false,
  "createdAt": "2025-06-01T10:00:00",
  "updatedAt": "2025-07-04T10:00:00"
}
```

### 3. Create Customer
**Endpoint:** `POST /customers`

**Request Body:**
```json
{
  "name": "Jane Smith",
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane@example.com",
  "phone": "+1987654321",
  "address": "456 Oak Ave",
  "dateOfBirth": "1985-05-20",
  "gender": "FEMALE",
  "customerType": "VIP",
  "customerStatus": "ACTIVE",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave",
  "preferredPaymentMethod": "DEBIT_CARD",
  "creditLimit": 10000.00,
  "taxNumber": "TAX789012",
  "companyName": "Smith Corp",
  "website": "https://smithcorp.com",
  "notes": "Corporate customer"
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
  "phone": "+1987654321",
  "address": "456 Oak Ave",
  "dateOfBirth": "1985-05-20",
  "gender": "FEMALE",
  "customerType": "VIP",
  "customerStatus": "ACTIVE",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave",
  "preferredPaymentMethod": "DEBIT_CARD",
  "creditLimit": 10000.00,
  "currentBalance": 0.00,
  "loyaltyPoints": 0,
  "taxNumber": "TAX789012",
  "companyName": "Smith Corp",
  "website": "https://smithcorp.com",
  "notes": "Corporate customer",
  "lastPurchaseDate": null,
  "totalPurchases": 0.00,
  "isEmailVerified": false,
  "isPhoneVerified": false,
  "createdAt": "2025-07-04T10:00:00",
  "updatedAt": "2025-07-04T10:00:00"
}
```

### 4. Update Customer
**Endpoint:** `PUT /customers/{id}`

**Request Body:** Same as Create Customer

**Response (200 OK):** Updated customer object

### 5. Delete Customer
**Endpoint:** `DELETE /customers/{id}`

**Response (204 No Content):** Empty response

### 6. Search Customers
**Endpoint:** `GET /customers/search`

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Response (200 OK):** Paginated customer list matching search criteria

## Enum Values Reference

### Customer Enums
- **Gender:** `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`
- **CustomerType:** `REGULAR`, `VIP`, `PREMIUM`, `CORPORATE`, `WHOLESALE`
- **CustomerStatus:** `ACTIVE`, `INACTIVE`, `SUSPENDED`, `BLACKLISTED`

### User Roles
- **Role:** `USER`, `ADMIN`, `MANAGER`

## Error Responses

### Validation Error (400 Bad Request)
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2025-07-04T10:00:00",
  "details": {
    "email": "Email should be valid",
    "phone": "Phone number should be valid"
  }
}
```

### Resource Not Found (404 Not Found)
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Customer not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2025-07-04T10:00:00"
}
```

### Unauthorized (401 Unauthorized)
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Access token is missing or invalid",
  "errorCode": "UNAUTHORIZED",
  "timestamp": "2025-07-04T10:00:00"
}
```

## Product Endpoints

### 1. Get All Products
**Endpoint:** `GET /products`

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="asc") - Sort direction (asc/desc)
- `category` (string, optional) - Filter by category

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Wireless Headphones",
      "description": "High-quality wireless headphones with noise cancellation",
      "price": 199.99,
      "costPrice": 120.00,
      "stockQuantity": 50,
      "category": "Electronics",
      "sku": "WH001",
      "brand": "TechBrand",
      "modelNumber": "TB-WH-2024",
      "barcode": "1234567890123",
      "weight": 0.35,
      "length": 20.0,
      "width": 18.0,
      "height": 8.0,
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
      "tags": ["wireless", "audio", "bluetooth"],
      "imageUrl": "https://example.com/images/wh001.jpg",
      "additionalImages": ["https://example.com/images/wh001-2.jpg"],
      "isSerialized": true,
      "isDigital": false,
      "isTaxable": true,
      "taxRate": 8.25,
      "unitOfMeasure": "PCS",
      "discountPercentage": 0.00,
      "locationInWarehouse": "A1-B2-C3",
      "totalSold": 125,
      "totalRevenue": 24987.50,
      "lastSoldDate": "2025-07-03T15:30:00",
      "lastRestockedDate": "2025-06-15T09:00:00",
      "notes": "Popular item, restock frequently",
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-07-04T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

### 2. Get Product by ID
**Endpoint:** `GET /products/{id}`

**Response (200 OK):** Same structure as individual product in the list above

### 3. Create Product
**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "name": "Gaming Mouse",
  "description": "High-precision gaming mouse with RGB lighting",
  "price": 79.99,
  "costPrice": 45.00,
  "stockQuantity": 75,
  "category": "Electronics",
  "sku": "GM002",
  "brand": "GameTech",
  "modelNumber": "GT-GM-2024",
  "barcode": "9876543210987",
  "weight": 0.12,
  "length": 12.5,
  "width": 6.8,
  "height": 4.2,
  "productStatus": "ACTIVE",
  "minStockLevel": 5,
  "maxStockLevel": 150,
  "reorderPoint": 10,
  "reorderQuantity": 50,
  "supplierName": "Gaming Supplier Co",
  "supplierCode": "GS002",
  "warrantyPeriod": 12,
  "manufacturingDate": "2024-02-10",
  "tags": ["gaming", "mouse", "rgb"],
  "imageUrl": "https://example.com/images/gm002.jpg",
  "isSerialized": false,
  "isDigital": false,
  "isTaxable": true,
  "taxRate": 8.25,
  "unitOfMeasure": "PCS",
  "discountPercentage": 5.00,
  "locationInWarehouse": "B2-C1-D4",
  "notes": "New gaming product line"
}
```

**Response (201 Created):** Created product object with generated ID and timestamps

### 4. Update Product
**Endpoint:** `PUT /products/{id}`

**Request Body:** Same as Create Product

**Response (200 OK):** Updated product object

### 5. Delete Product
**Endpoint:** `DELETE /products/{id}`

**Response (204 No Content):** Empty response

### 6. Search Products
**Endpoint:** `GET /products/search`

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Response (200 OK):** Paginated product list matching search criteria

### 7. Update Product Stock
**Endpoint:** `PUT /products/{id}/stock`

**Request Body:**
```json
{
  "stockQuantity": 100,
  "notes": "Restocked from supplier"
}
```

**Response (200 OK):** Updated product object

## Sales Endpoints

### 1. Get All Sales
**Endpoint:** `GET /sales`

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="desc") - Sort direction (asc/desc)
- `status` (string, optional) - Filter by status (PENDING, COMPLETED, CANCELLED)
- `startDate` (datetime, optional) - Filter by start date (ISO format)
- `endDate` (datetime, optional) - Filter by end date (ISO format)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "saleDate": "2025-07-04T14:30:00",
      "totalAmount": 279.98,
      "status": "COMPLETED",
      "saleNumber": "SALE-2025-000001",
      "referenceNumber": "REF-001",
      "subtotal": 259.98,
      "discountAmount": 0.00,
      "discountPercentage": 0.00,
      "taxAmount": 20.00,
      "taxPercentage": 8.25,
      "shippingCost": 0.00,
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PAID",
      "paymentDate": "2025-07-04T14:35:00",
      "dueDate": null,
      "billingAddress": "123 Main St",
      "shippingAddress": "123 Main St",
      "salesPerson": "Alice Johnson",
      "salesChannel": "ONLINE",
      "saleType": "RETAIL",
      "currency": "USD",
      "exchangeRate": 1.00,
      "notes": "Customer requested express delivery",
      "internalNotes": "VIP customer - priority handling",
      "termsAndConditions": "Standard terms apply",
      "warrantyInfo": "2-year warranty included",
      "deliveryDate": null,
      "expectedDeliveryDate": "2025-07-06",
      "deliveryStatus": "PROCESSING",
      "trackingNumber": null,
      "isGift": false,
      "giftMessage": null,
      "loyaltyPointsEarned": 27,
      "loyaltyPointsUsed": 0,
      "isReturn": false,
      "originalSaleId": null,
      "returnReason": null,
      "profitMargin": 45.50,
      "costOfGoodsSold": 165.00,
      "createdAt": "2025-07-04T14:30:00",
      "updatedAt": "2025-07-04T14:35:00",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Wireless Headphones",
          "quantity": 1,
          "unitPrice": 199.99,
          "originalUnitPrice": 199.99,
          "costPrice": 120.00,
          "discountPercentage": 0.00,
          "discountAmount": 0.00,
          "taxPercentage": 8.25,
          "taxAmount": 16.50,
          "subtotal": 199.99,
          "totalPrice": 216.49,
          "serialNumbers": "SN123456789",
          "warrantyInfo": "2-year manufacturer warranty",
          "notes": "Customer preferred black color",
          "isReturned": false,
          "returnedQuantity": 0,
          "unitOfMeasure": "PCS"
        },
        {
          "id": 2,
          "productId": 2,
          "productName": "Gaming Mouse",
          "quantity": 1,
          "unitPrice": 59.99,
          "originalUnitPrice": 79.99,
          "costPrice": 45.00,
          "discountPercentage": 25.00,
          "discountAmount": 20.00,
          "taxPercentage": 8.25,
          "taxAmount": 4.95,
          "subtotal": 59.99,
          "totalPrice": 64.94,
          "serialNumbers": null,
          "warrantyInfo": "1-year manufacturer warranty",
          "notes": null,
          "isReturned": false,
          "returnedQuantity": 0,
          "unitOfMeasure": "PCS"
        }
      ]
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

### 2. Get Sale by ID
**Endpoint:** `GET /sales/{id}`

**Response (200 OK):** Same structure as individual sale in the list above

### 3. Get Sales by Customer
**Endpoint:** `GET /sales/customer/{customerId}`

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Response (200 OK):** Paginated sales list for the specified customer

### 4. Create Sale
**Endpoint:** `POST /sales`

**Request Body:**
```json
{
  "customerId": 1,
  "saleDate": "2025-07-04T15:00:00",
  "totalAmount": 159.99,
  "status": "PENDING",
  "saleNumber": "SALE-2025-000002",
  "referenceNumber": "REF-002",
  "subtotal": 149.99,
  "discountAmount": 0.00,
  "discountPercentage": 0.00,
  "taxAmount": 10.00,
  "taxPercentage": 8.25,
  "shippingCost": 0.00,
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "billingAddress": "123 Main St",
  "shippingAddress": "123 Main St",
  "salesPerson": "Bob Smith",
  "salesChannel": "IN_STORE",
  "saleType": "RETAIL",
  "currency": "USD",
  "exchangeRate": 1.00,
  "notes": "Walk-in customer",
  "expectedDeliveryDate": "2025-07-05",
  "deliveryStatus": "NOT_SHIPPED",
  "isGift": false,
  "items": [
    {
      "productId": 3,
      "quantity": 1,
      "unitPrice": 149.99,
      "discountPercentage": 0.00,
      "taxPercentage": 8.25,
      "notes": "Customer preferred blue color"
    }
  ]
}
```

**Response (201 Created):** Created sale object with generated ID and timestamps

### 5. Update Sale
**Endpoint:** `PUT /sales/{id}`

**Request Body:** Same as Create Sale

**Response (200 OK):** Updated sale object

### 6. Delete Sale
**Endpoint:** `DELETE /sales/{id}`

**Response (204 No Content):** Empty response

### 7. Complete Sale
**Endpoint:** `POST /sales/{id}/complete`

**Response (200 OK):** Sale object with status updated to COMPLETED

### 8. Cancel Sale
**Endpoint:** `POST /sales/{id}/cancel`

**Response (200 OK):** Sale object with status updated to CANCELLED

## Reports Endpoints

### 1. Sales Report
**Endpoint:** `GET /reports/sales`

**Query Parameters:**
- `startDate` (datetime, required) - Start date (ISO format)
- `endDate` (datetime, required) - End date (ISO format)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-07-04T23:59:59"
  },
  "summary": {
    "totalRevenue": 15750.50,
    "totalSales": 45,
    "averageOrderValue": 350.01,
    "totalProfit": 6825.25,
    "profitMargin": 43.33
  },
  "salesByStatus": {
    "COMPLETED": 38,
    "PENDING": 5,
    "CANCELLED": 2
  },
  "dailyRevenue": [
    {
      "date": "2025-06-01",
      "revenue": 1250.00,
      "salesCount": 3
    },
    {
      "date": "2025-06-02",
      "revenue": 890.50,
      "salesCount": 2
    }
  ],
  "topCustomers": [
    {
      "customerId": 1,
      "customerName": "John Doe",
      "totalSpent": 2500.00,
      "orderCount": 8
    }
  ],
  "productPerformance": [
    {
      "productId": 1,
      "productName": "Wireless Headphones",
      "quantitySold": 25,
      "revenue": 4999.75,
      "profit": 1999.75
    }
  ]
}
```

### 2. Revenue Trends Report
**Endpoint:** `GET /reports/revenue`

**Query Parameters:**
- `months` (int, default=6) - Number of months to include

**Response (200 OK):**
```json
{
  "period": "Last 6 months",
  "monthlyTrends": [
    {
      "month": "2025-01",
      "revenue": 12500.00,
      "salesCount": 35,
      "profit": 5250.00
    },
    {
      "month": "2025-02",
      "revenue": 14750.50,
      "salesCount": 42,
      "profit": 6125.25
    }
  ],
  "totalRevenue": 78250.00,
  "totalProfit": 32500.00,
  "averageMonthlyRevenue": 13041.67,
  "growthRate": 18.5
}
```

### 3. Top Selling Products Report
**Endpoint:** `GET /reports/top-products`

**Query Parameters:**
- `startDate` (datetime, required) - Start date (ISO format)
- `endDate` (datetime, required) - End date (ISO format)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-07-04T23:59:59"
  },
  "topProducts": [
    {
      "productId": 1,
      "productName": "Wireless Headphones",
      "category": "Electronics",
      "quantitySold": 45,
      "revenue": 8999.55,
      "profit": 3599.55,
      "profitMargin": 39.99
    },
    {
      "productId": 2,
      "productName": "Gaming Mouse",
      "category": "Electronics",
      "quantitySold": 32,
      "revenue": 2559.68,
      "profit": 1119.68,
      "profitMargin": 43.75
    }
  ],
  "totalProductsSold": 125,
  "totalCategories": 5
}
```

### 4. Customer Analytics Report
**Endpoint:** `GET /reports/customer-analytics`

**Response (200 OK):**
```json
{
  "totalCustomers": 150,
  "activeCustomers": 125,
  "newCustomersThisMonth": 12,
  "customerRetentionRate": 83.33,
  "averageCustomerValue": 1250.75,
  "customersByType": {
    "REGULAR": 100,
    "VIP": 25,
    "PREMIUM": 15,
    "CORPORATE": 8,
    "WHOLESALE": 2
  },
  "customersByStatus": {
    "ACTIVE": 125,
    "INACTIVE": 20,
    "SUSPENDED": 3,
    "BLACKLISTED": 2
  },
  "topCustomers": [
    {
      "customerId": 1,
      "customerName": "John Doe",
      "totalSpent": 5250.00,
      "orderCount": 15,
      "loyaltyPoints": 525
    }
  ],
  "loyaltyProgram": {
    "totalPointsIssued": 125000,
    "totalPointsRedeemed": 45000,
    "activeParticipants": 95
  }
}
```

### 5. Inventory Report
**Endpoint:** `GET /reports/inventory`

**Response (200 OK):**
```json
{
  "totalProducts": 250,
  "totalInventoryValue": 125750.50,
  "lowStockProducts": 15,
  "outOfStockProducts": 3,
  "stockLevels": {
    "ACTIVE": 220,
    "INACTIVE": 15,
    "DISCONTINUED": 10,
    "OUT_OF_STOCK": 3,
    "COMING_SOON": 2
  },
  "categoryBreakdown": [
    {
      "category": "Electronics",
      "productCount": 85,
      "totalValue": 65250.00,
      "averageValue": 767.65
    },
    {
      "category": "Accessories",
      "productCount": 45,
      "totalValue": 15750.50,
      "averageValue": 350.01
    }
  ],
  "reorderAlerts": [
    {
      "productId": 15,
      "productName": "USB Cable",
      "currentStock": 5,
      "reorderPoint": 10,
      "reorderQuantity": 50
    }
  ]
}
```

### 6. Dashboard Summary
**Endpoint:** `GET /reports/dashboard`

**Response (200 OK):**
```json
{
  "period": "Last 30 days",
  "sales": {
    "totalRevenue": 25750.50,
    "totalSales": 75,
    "averageOrderValue": 343.34,
    "completedSales": 68,
    "pendingSales": 5,
    "cancelledSales": 2
  },
  "customers": {
    "totalCustomers": 150,
    "activeCustomers": 125,
    "newCustomers": 12,
    "retentionRate": 83.33
  },
  "inventory": {
    "totalProducts": 250,
    "lowStockAlerts": 15,
    "outOfStockAlerts": 3,
    "totalValue": 125750.50
  },
  "revenue": {
    "thisMonth": 25750.50,
    "lastMonth": 22100.25,
    "growthRate": 16.52,
    "profitMargin": 42.15
  },
  "generatedAt": "2025-07-04T10:00:00"
}
```

## Complete Enum Values Reference

### Product Enums
- **ProductStatus:** `ACTIVE`, `INACTIVE`, `DISCONTINUED`, `OUT_OF_STOCK`, `COMING_SOON`

### Sale Enums
- **SaleStatus:** `PENDING`, `COMPLETED`, `CANCELLED`
- **PaymentMethod:** `CASH`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `CHECK`, `PAYPAL`, `STRIPE`, `SQUARE`, `OTHER`, `NET_30`
- **PaymentStatus:** `PENDING`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`, `REFUNDED`, `CANCELLED`
- **SaleType:** `RETAIL`, `WHOLESALE`, `B2B`, `ONLINE`, `SUBSCRIPTION`, `RETURN`
- **DeliveryStatus:** `NOT_SHIPPED`, `PROCESSING`, `SHIPPED`, `IN_TRANSIT`, `DELIVERED`, `RETURNED`, `CANCELLED`, `PICKED_UP`

## Authentication Headers

All protected endpoints require the following header:
```
Authorization: Bearer <access_token>
```

## Common HTTP Status Codes

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no content returned
- **400 Bad Request** - Invalid request data
- **401 Unauthorized** - Authentication required or invalid
- **403 Forbidden** - Access denied
- **404 Not Found** - Resource not found
- **409 Conflict** - Resource conflict (e.g., duplicate email)
- **422 Unprocessable Entity** - Validation errors
- **500 Internal Server Error** - Server error
```
