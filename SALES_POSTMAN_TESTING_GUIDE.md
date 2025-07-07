# Sales Management API - Postman Testing Guide

## Overview

This guide provides comprehensive Postman testing examples for all Sales Management API endpoints. Each endpoint includes request examples, expected responses, and test scenarios.

## Base Configuration

### Environment Variables
```json
{
  "base_url": "http://localhost:8081",
  "api_prefix": "/api",
  "auth_token": "{{jwt_token}}"
}
```

### Headers
```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {{auth_token}}"
}
```

## Authentication Setup

### 1. Login to Get JWT Token
**POST** `{{base_url}}{{api_prefix}}/auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**Test Script:**
```javascript
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.accessToken).to.exist;
    pm.environment.set("jwt_token", response.accessToken);
});
```

## Sales Endpoints Testing

### 1. Create Sale
**POST** `{{base_url}}{{api_prefix}}/sales`

**Request Body:**
```json
{
  "customerId": 1,
  "customerName": "John Doe",
  "saleDate": "2025-07-06T14:30:00",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "items": [
    {
      "productId": 1,
      "productName": "Smartphone",
      "quantity": 1,
      "unitPrice": 999.99,
      "originalUnitPrice": 999.99,
      "costPrice": 600.00,
      "discountPercentage": 0.0,
      "discountAmount": 0.0,
      "taxPercentage": 15.0,
      "taxAmount": 149.99,
      "subtotal": 999.99,
      "totalPrice": 1149.98,
      "unitOfMeasure": "PCS"
    }
  ],
  "subtotal": 999.99,
  "discountAmount": 0.0,
  "discountPercentage": 0.0,
  "taxAmount": 149.99,
  "taxPercentage": 15.0,
  "shippingCost": 0.0,
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "billingAddress": "123 Main St, City, State",
  "shippingAddress": "123 Main St, City, State",
  "salesPerson": "Sales Rep",
  "salesChannel": "IN_STORE",
  "saleType": "RETAIL",
  "currency": "USD",
  "exchangeRate": 1.0,
  "deliveryStatus": "NOT_SHIPPED",
  "isGift": false,
  "loyaltyPointsEarned": 99,
  "loyaltyPointsUsed": 0,
  "isReturn": false
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "saleDate": "2025-07-06T14:30:00",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "saleNumber": "SALE-2025-000001",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Smartphone",
      "quantity": 1,
      "unitPrice": 999.99,
      "totalPrice": 1149.98
    }
  ],
  "createdAt": "2025-07-06T14:30:00",
  "updatedAt": "2025-07-06T14:30:00"
}
```

**Test Script:**
```javascript
pm.test("Sale created successfully", function () {
    pm.response.to.have.status(201);
    const response = pm.response.json();
    pm.expect(response.id).to.exist;
    pm.expect(response.saleNumber).to.exist;
    pm.expect(response.status).to.equal("PENDING");
    pm.environment.set("sale_id", response.id);
});

pm.test("Sale has correct customer", function () {
    const response = pm.response.json();
    pm.expect(response.customerId).to.equal(1);
    pm.expect(response.customerName).to.equal("John Doe");
});

pm.test("Sale has items", function () {
    const response = pm.response.json();
    pm.expect(response.items).to.be.an('array');
    pm.expect(response.items.length).to.be.greaterThan(0);
});
```

### 2. Get All Sales
**GET** `{{base_url}}{{api_prefix}}/sales?page=0&size=10&sortBy=saleDate&sortDir=desc`

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "saleDate": "2025-07-06T14:30:00",
      "totalAmount": 1149.45,
      "status": "PENDING",
      "saleNumber": "SALE-2025-000001"
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

**Test Script:**
```javascript
pm.test("Sales retrieved successfully", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.content).to.be.an('array');
    pm.expect(response.totalElements).to.be.a('number');
});

pm.test("Pagination info present", function () {
    const response = pm.response.json();
    pm.expect(response.pageable).to.exist;
    pm.expect(response.totalPages).to.exist;
    pm.expect(response.first).to.be.a('boolean');
    pm.expect(response.last).to.be.a('boolean');
});
```

### 3. Get Sale by ID
**GET** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "saleDate": "2025-07-06T14:30:00",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "saleNumber": "SALE-2025-000001",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Smartphone",
      "quantity": 1,
      "unitPrice": 999.99,
      "totalPrice": 1149.98
    }
  ]
}
```

**Test Script:**
```javascript
pm.test("Sale retrieved by ID", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.id).to.equal(parseInt(pm.environment.get("sale_id")));
    pm.expect(response.items).to.be.an('array');
});
```

### 4. Get Sales by Customer
**GET** `{{base_url}}{{api_prefix}}/sales/customer/1?page=0&size=10`

**Test Script:**
```javascript
pm.test("Customer sales retrieved", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.content).to.be.an('array');
    response.content.forEach(sale => {
        pm.expect(sale.customerId).to.equal(1);
    });
});
```

### 5. Complete Sale
**POST** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}/complete`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "paymentStatus": "PAID",
  "paymentDate": "2025-07-06T14:35:00",
  "updatedAt": "2025-07-06T14:35:00"
}
```

**Test Script:**
```javascript
pm.test("Sale completed successfully", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.status).to.equal("COMPLETED");
    pm.expect(response.paymentDate).to.exist;
});
```

### 6. Cancel Sale
**POST** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}/cancel`

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "updatedAt": "2025-07-06T14:35:00"
}
```

**Test Script:**
```javascript
pm.test("Sale cancelled successfully", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.status).to.equal("CANCELLED");
});
```

### 7. Update Sale
**PUT** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}`

**Request Body:**
```json
{
  "id": 1,
  "customerId": 1,
  "totalAmount": 1200.00,
  "notes": "Updated sale with additional notes",
  "internalNotes": "Internal note for staff"
}
```

**Test Script:**
```javascript
pm.test("Sale updated successfully", function () {
    pm.response.to.have.status(200);
    const response = pm.response.json();
    pm.expect(response.notes).to.equal("Updated sale with additional notes");
});
```

### 8. Delete Sale
**DELETE** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}`

**Expected Response (204 No Content)**

**Test Script:**
```javascript
pm.test("Sale deleted successfully", function () {
    pm.response.to.have.status(204);
});
```

## Error Testing Scenarios

### 1. Create Sale with Invalid Customer
**POST** `{{base_url}}{{api_prefix}}/sales`

**Request Body:**
```json
{
  "customerId": 99999,
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "unitPrice": 100.00
    }
  ]
}
```

**Expected Response (404 Not Found):**
```json
{
  "error": {
    "code": "CUSTOMER_NOT_FOUND",
    "message": "Customer not found with id: 99999",
    "timestamp": "2025-07-06T14:30:00"
  }
}
```

### 2. Create Sale with Insufficient Stock
**Request Body:**
```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1000,
      "unitPrice": 100.00
    }
  ]
}
```

**Expected Response (409 Conflict):**
```json
{
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Insufficient stock for product 'Smartphone'. Available: 50, Requested: 1000",
    "details": {
      "productId": 1,
      "availableStock": 50,
      "requestedQuantity": 1000
    }
  }
}
```

### 3. Complete Already Completed Sale
**POST** `{{base_url}}{{api_prefix}}/sales/{{sale_id}}/complete`

**Expected Response (409 Conflict):**
```json
{
  "error": {
    "code": "SALE_ALREADY_COMPLETED",
    "message": "Sale is already completed",
    "timestamp": "2025-07-06T14:30:00"
  }
}
```

## Collection Variables for Testing

```json
{
  "base_url": "http://localhost:8081",
  "api_prefix": "/api",
  "jwt_token": "",
  "sale_id": "",
  "customer_id": "1",
  "product_id": "1"
}
```

## Test Execution Order

1. **Authentication** - Login to get JWT token
2. **Create Sale** - Test sale creation with valid data
3. **Get All Sales** - Verify sale appears in list
4. **Get Sale by ID** - Retrieve specific sale details
5. **Get Customer Sales** - Filter sales by customer
6. **Update Sale** - Modify sale information
7. **Complete Sale** - Mark sale as completed
8. **Cancel Sale** - Test cancellation (create new sale first)
9. **Delete Sale** - Test deletion (create new sale first)
10. **Error Scenarios** - Test various error conditions

## Performance Testing

### Load Testing Parameters
- **Concurrent Users**: 10-50
- **Test Duration**: 5-10 minutes
- **Endpoints to Test**: Create Sale, Get Sales, Get Sale by ID
- **Expected Response Time**: < 500ms for GET, < 1000ms for POST

### Stress Testing
- **Peak Load**: 100+ concurrent users
- **Monitor**: Response times, error rates, database connections
- **Thresholds**: 95% success rate, < 2s response time

This Postman testing guide provides comprehensive coverage of all Sales Management API endpoints with realistic test scenarios and validation scripts.
