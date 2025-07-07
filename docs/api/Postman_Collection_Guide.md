# Postman Collection Guide - Sales Management API

## Overview

This guide provides ready-to-use Postman examples for testing the Sales Management System API. Each example includes the complete request configuration with headers, body, and expected responses.

## Environment Setup

### Postman Environment Variables

Create a new environment in Postman with the following variables:

```json
{
  "baseUrl": "http://localhost:8081/api",
  "authToken": "your-jwt-token-here",
  "customerId": "1",
  "productId": "1",
  "saleId": "1",
  "returnId": "1"
}
```

### Global Headers

Set these headers for all requests:
- `Authorization`: `Bearer {{authToken}}`
- `Content-Type`: `application/json`

## Sales Management Endpoints

### 1. Get All Sales

**Method:** `GET`  
**URL:** `{{baseUrl}}/sales`

**Query Parameters:**
```
page: 0
size: 10
sortBy: saleDate
sortDir: desc
status: PENDING
```

**Headers:**
```
Authorization: Bearer {{authToken}}
```

**Expected Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "أحمد محمد",
      "saleDate": "2025-07-06T14:30:00",
      "totalAmount": 1149.45,
      "status": "PENDING",
      "saleNumber": "SALE-2025-000001"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

### 2. Get Sale by ID

**Method:** `GET`  
**URL:** `{{baseUrl}}/sales/{{saleId}}`

**Headers:**
```
Authorization: Bearer {{authToken}}
```

**Expected Response (200):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "أحمد محمد",
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

### 3. Create New Sale

**Method:** `POST`  
**URL:** `{{baseUrl}}/sales`

**Headers:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "customerId": {{customerId}},
  "customerName": "أحمد محمد",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "items": [
    {
      "productId": {{productId}},
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
  "billingAddress": "123 شارع الملك فهد، الرياض",
  "shippingAddress": "123 شارع الملك فهد، الرياض",
  "salesPerson": "Current User",
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

**Expected Response (201):**
```json
{
  "id": 1,
  "customerId": 1,
  "saleNumber": "SALE-2025-000001",
  "status": "PENDING",
  "totalAmount": 1149.45,
  "createdAt": "2025-07-06T14:30:00"
}
```

### 4. Complete Sale

**Method:** `POST`  
**URL:** `{{baseUrl}}/sales/{{saleId}}/complete`

**Headers:**
```
Authorization: Bearer {{authToken}}
```

**Expected Response (200):**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "paymentStatus": "PAID",
  "paymentDate": "2025-07-06T14:35:00",
  "updatedAt": "2025-07-06T14:35:00"
}
```

### 5. Cancel Sale

**Method:** `POST`  
**URL:** `{{baseUrl}}/sales/{{saleId}}/cancel`

**Headers:**
```
Authorization: Bearer {{authToken}}
```

**Expected Response (200):**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "updatedAt": "2025-07-06T14:35:00"
}
```

## Returns Management Endpoints

### 1. Get All Returns

**Method:** `GET`  
**URL:** `{{baseUrl}}/returns`

**Query Parameters:**
```
page: 0
size: 10
sortBy: returnDate
sortDir: desc
status: PENDING
```

**Headers:**
```
Authorization: Bearer {{authToken}}
```

### 2. Create Return Request

**Method:** `POST`  
**URL:** `{{baseUrl}}/returns`

**Headers:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "originalSaleId": {{saleId}},
  "customerId": {{customerId}},
  "returnReason": "DEFECTIVE",
  "returnType": "FULL_RETURN",
  "requestedBy": "Customer",
  "notes": "Product arrived damaged",
  "items": [
    {
      "originalSaleItemId": 1,
      "productId": {{productId}},
      "returnQuantity": 1,
      "refundAmount": 999.99,
      "conditionNotes": "Screen cracked",
      "itemCondition": "DAMAGED",
      "serialNumbers": "SN123456",
      "isRestockable": false,
      "disposalReason": "Damaged beyond repair"
    }
  ]
}
```

### 3. Approve Return

**Method:** `POST`  
**URL:** `{{baseUrl}}/returns/{{returnId}}/approve`

**Headers:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "approvedBy": "Manager Name",
  "notes": "Approved for refund"
}
```

### 4. Process Refund

**Method:** `POST`  
**URL:** `{{baseUrl}}/returns/{{returnId}}/refund`

**Headers:**
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456"
}
```

## Reports and Analytics Endpoints

### 1. Sales Report

**Method:** `GET`  
**URL:** `{{baseUrl}}/reports/sales`

**Query Parameters:**
```
startDate: 2025-07-01T00:00:00
endDate: 2025-07-31T23:59:59
```

**Headers:**
```
Authorization: Bearer {{authToken}}
```

### 2. Revenue Trends

**Method:** `GET`  
**URL:** `{{baseUrl}}/reports/revenue`

**Query Parameters:**
```
months: 6
```

**Headers:**
```
Authorization: Bearer {{authToken}}
```

### 3. Dashboard Summary

**Method:** `GET`  
**URL:** `{{baseUrl}}/reports/dashboard`

**Headers:**
```
Authorization: Bearer {{authToken}}
```

### 4. Top Products Report

**Method:** `GET`  
**URL:** `{{baseUrl}}/reports/top-products`

**Query Parameters:**
```
startDate: 2025-07-01T00:00:00
endDate: 2025-07-31T23:59:59
```

**Headers:**
```
Authorization: Bearer {{authToken}}
```

## Test Scenarios

### Complete Sales Flow Test

1. **Create Sale** - POST `/api/sales`
2. **Get Sale Details** - GET `/api/sales/{id}`
3. **Complete Sale** - POST `/api/sales/{id}/complete`
4. **Verify Completion** - GET `/api/sales/{id}`

### Returns Flow Test

1. **Create Return** - POST `/api/returns`
2. **Get Return Details** - GET `/api/returns/{id}`
3. **Approve Return** - POST `/api/returns/{id}/approve`
4. **Process Refund** - POST `/api/returns/{id}/refund`
5. **Verify Completion** - GET `/api/returns/{id}`

### Error Handling Tests

#### Invalid Sale Creation
**Method:** `POST`  
**URL:** `{{baseUrl}}/sales`

**Body (Invalid - missing required fields):**
```json
{
  "customerId": null,
  "items": []
}
```

**Expected Response (400):**
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Customer ID is required",
    "timestamp": "2025-07-06T14:30:00",
    "path": "/api/sales"
  }
}
```

#### Sale Not Found
**Method:** `GET`  
**URL:** `{{baseUrl}}/sales/999999`

**Expected Response (404):**
```json
{
  "error": {
    "code": "SALE_NOT_FOUND",
    "message": "Sale not found with id: 999999",
    "timestamp": "2025-07-06T14:30:00",
    "path": "/api/sales/999999"
  }
}
```

## Postman Collection JSON

You can import this collection directly into Postman:

```json
{
  "info": {
    "name": "Sales Management API",
    "description": "Complete API collection for Sales Management System",
    "version": "1.0.0"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8081/api"
    },
    {
      "key": "authToken",
      "value": "your-jwt-token-here"
    }
  ],
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{authToken}}"
      }
    ]
  }
}
```

## Testing Tips

1. **Set up environment variables** for easy switching between environments
2. **Use pre-request scripts** to generate dynamic data
3. **Add tests** to validate response structure and data
4. **Chain requests** using response data in subsequent requests
5. **Use collection variables** to store IDs from created resources
6. **Test error scenarios** to ensure proper error handling
7. **Validate response times** for performance testing

## Pre-request Script Examples

### Generate Random Customer ID
```javascript
pm.environment.set("customerId", Math.floor(Math.random() * 100) + 1);
```

### Set Current Timestamp
```javascript
pm.environment.set("currentDate", new Date().toISOString());
```

## Test Script Examples

### Validate Response Structure
```javascript
pm.test("Response has required fields", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('id');
    pm.expect(responseJson).to.have.property('status');
    pm.expect(responseJson).to.have.property('totalAmount');
});
```

### Store Response Data
```javascript
pm.test("Store sale ID for next request", function () {
    const responseJson = pm.response.json();
    pm.environment.set("saleId", responseJson.id);
});
```
