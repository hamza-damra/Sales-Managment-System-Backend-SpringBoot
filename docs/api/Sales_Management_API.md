# Sales Management API Documentation

## Overview

This document provides comprehensive API documentation for the Sales Management endpoints in the Sales Management System. These endpoints are designed for frontend developers to integrate sales functionality into their applications.

**Base URL:** `http://localhost:8081/api/sales`

## Table of Contents

1. [Authentication](#authentication)
2. [Core Sales Endpoints](#core-sales-endpoints)
3. [Request/Response Models](#requestresponse-models)
4. [Error Handling](#error-handling)
5. [Examples](#examples)

## Authentication

All endpoints require proper authentication. Include the authentication token in the request headers:

```http
Authorization: Bearer <your-token>
Content-Type: application/json
```

## Core Sales Endpoints

### 1. Get All Sales

**Endpoint:** `GET /api/sales`

**Description:** Retrieve all sales with pagination, sorting, and filtering options.

**Query Parameters:**
- `page` (int, default=0) - Page number (0-based)
- `size` (int, default=10, max=100) - Number of items per page
- `sortBy` (string, default="id") - Field to sort by (id, saleDate, totalAmount, status)
- `sortDir` (string, default="desc") - Sort direction (asc/desc)
- `status` (string, optional) - Filter by status (PENDING, COMPLETED, CANCELLED, REFUNDED)
- `startDate` (datetime, optional) - Filter by start date (ISO format: 2025-07-06T14:30:00)
- `endDate` (datetime, optional) - Filter by end date (ISO format: 2025-07-06T14:30:00)

**Example Request:**
```http
GET /api/sales?page=0&size=20&sortBy=saleDate&sortDir=desc&status=PENDING
```

**Response (200 OK):**
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
      "saleNumber": "SALE-2025-000001",
      "paymentMethod": "CASH",
      "paymentStatus": "PENDING",
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
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 20
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

**Endpoint:** `GET /api/sales/{id}`

**Description:** Retrieve a specific sale with complete details and all items.

**Path Parameters:**
- `id` (Long) - Sale ID

**Example Request:**
```http
GET /api/sales/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "أحمد محمد",
  "saleDate": "2025-07-06T14:30:00",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "saleNumber": "SALE-2025-000001",
  "referenceNumber": "REF-001",
  "subtotal": 999.99,
  "discountAmount": 0.0,
  "discountPercentage": 0.0,
  "taxAmount": 149.99,
  "taxPercentage": 15.0,
  "shippingCost": 0.0,
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "paymentDate": null,
  "dueDate": null,
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
  "isReturn": false,
  "profitMargin": 399.99,
  "costOfGoodsSold": 600.00,
  "createdAt": "2025-07-06T14:30:00",
  "updatedAt": "2025-07-06T14:30:00",
  "items": [
    {
      "id": 1,
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
      "serialNumbers": null,
      "warrantyInfo": "1-year warranty",
      "notes": null,
      "isReturned": false,
      "returnedQuantity": 0,
      "unitOfMeasure": "PCS"
    }
  ]
}
```

**Error Responses:**
- `400 Bad Request` - Invalid ID parameter
- `404 Not Found` - Sale not found

### 3. Get Sales by Customer

**Endpoint:** `GET /api/sales/customer/{customerId}`

**Description:** Retrieve all sales for a specific customer with pagination.

**Path Parameters:**
- `customerId` (Long) - Customer ID

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Example Request:**
```http
GET /api/sales/customer/1?page=0&size=10
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "أحمد محمد",
      "saleDate": "2025-07-06T14:30:00",
      "totalAmount": 1149.45,
      "status": "COMPLETED",
      "saleNumber": "SALE-2025-000001"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Error Responses:**
- `400 Bad Request` - Invalid customer ID

### 4. Create New Sale

**Endpoint:** `POST /api/sales`

**Description:** Create a new sale transaction with items.

**Request Body:**
```json
{
  "customerId": 1,
  "customerName": "أحمد محمد",
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

**Response (201 Created):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "أحمد محمد",
  "saleDate": "2025-07-06T14:30:00",
  "totalAmount": 1149.45,
  "status": "PENDING",
  "saleNumber": "SALE-2025-000001",
  "referenceNumber": "REF-001",
  "createdAt": "2025-07-06T14:30:00",
  "updatedAt": "2025-07-06T14:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid request data, validation errors
- `404 Not Found` - Customer or product not found
- `409 Conflict` - Insufficient stock, duplicate sale number
- `422 Unprocessable Entity` - Business logic validation failed

### 5. Update Sale

**Endpoint:** `PUT /api/sales/{id}`

**Description:** Update an existing sale (only if status is PENDING).

**Path Parameters:**
- `id` (Long) - Sale ID

**Request Body:** Same as Create Sale

**Response (200 OK):** Updated SaleDTO

**Error Responses:**
- `400 Bad Request` - Invalid ID or request data
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale cannot be updated (not in PENDING status)

### 6. Delete/Cancel Sale

**Endpoint:** `DELETE /api/sales/{id}`

**Description:** Cancel a sale (soft delete by setting status to CANCELLED).

**Path Parameters:**
- `id` (Long) - Sale ID

**Response (204 No Content)**

**Error Responses:**
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale cannot be cancelled

### 7. Complete Sale

**Endpoint:** `POST /api/sales/{id}/complete`

**Description:** Mark a pending sale as completed and update payment status.

**Path Parameters:**
- `id` (Long) - Sale ID

**Request Body:** None required

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "paymentStatus": "PAID",
  "paymentDate": "2025-07-06T14:35:00",
  "updatedAt": "2025-07-06T14:35:00"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale already completed or cancelled
- `422 Unprocessable Entity` - Cannot complete sale (business rules)

### 8. Cancel Sale

**Endpoint:** `POST /api/sales/{id}/cancel`

**Description:** Cancel a pending sale and restore inventory.

**Path Parameters:**
- `id` (Long) - Sale ID

**Request Body:** None required

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "updatedAt": "2025-07-06T14:35:00"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid ID
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale already completed or cancelled
- `422 Unprocessable Entity` - Cannot cancel sale (business rules)

## Request/Response Models

### SaleDTO

```typescript
interface SaleDTO {
  id?: number;
  customerId: number;
  customerName?: string;
  saleDate?: string; // ISO datetime format
  totalAmount: number;
  status?: "PENDING" | "COMPLETED" | "CANCELLED" | "REFUNDED";
  items: SaleItemDTO[];

  // Enhanced attributes
  saleNumber?: string;
  referenceNumber?: string;
  subtotal?: number;
  discountAmount?: number;
  discountPercentage?: number;
  taxAmount?: number;
  taxPercentage?: number;
  shippingCost?: number;
  paymentMethod?: "CASH" | "CREDIT_CARD" | "DEBIT_CARD" | "BANK_TRANSFER" | "CHECK" | "DIGITAL_WALLET";
  paymentStatus?: "PENDING" | "PAID" | "PARTIAL" | "OVERDUE" | "REFUNDED";
  paymentDate?: string; // ISO datetime format
  dueDate?: string; // ISO date format
  billingAddress?: string;
  shippingAddress?: string;
  salesPerson?: string;
  salesChannel?: string;
  saleType?: "RETAIL" | "WHOLESALE" | "B2B";
  currency?: string;
  exchangeRate?: number;
  notes?: string;
  internalNotes?: string;
  termsAndConditions?: string;
  warrantyInfo?: string;
  deliveryDate?: string; // ISO datetime format
  expectedDeliveryDate?: string; // ISO date format
  deliveryStatus?: "NOT_SHIPPED" | "PROCESSING" | "SHIPPED" | "DELIVERED" | "RETURNED";
  trackingNumber?: string;
  isGift?: boolean;
  giftMessage?: string;
  loyaltyPointsEarned?: number;
  loyaltyPointsUsed?: number;
  isReturn?: boolean;
  originalSaleId?: number;
  returnReason?: string;
  profitMargin?: number;
  costOfGoodsSold?: number;
  createdAt?: string; // ISO datetime format
  updatedAt?: string; // ISO datetime format
}
```

### SaleItemDTO

```typescript
interface SaleItemDTO {
  id?: number;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;

  // Enhanced attributes
  originalUnitPrice?: number;
  costPrice?: number;
  discountPercentage?: number;
  discountAmount?: number;
  taxPercentage?: number;
  taxAmount?: number;
  subtotal?: number;
  totalPrice?: number;
  serialNumbers?: string;
  warrantyInfo?: string;
  notes?: string;
  isReturned?: boolean;
  returnedQuantity?: number;
  unitOfMeasure?: string;
}
```

### Pagination Response

```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    sort: {
      sorted: boolean;
      unsorted: boolean;
    };
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
```

## Error Handling

### Standard Error Response Format

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {
      "field": "Additional error details"
    },
    "timestamp": "2025-07-06T14:30:00",
    "path": "/api/sales"
  }
}
```

### Common Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| `INSUFFICIENT_STOCK` | Product stock is insufficient | 409 |
| `CUSTOMER_NOT_FOUND` | Customer does not exist | 404 |
| `PRODUCT_NOT_FOUND` | Product does not exist | 404 |
| `SALE_NOT_FOUND` | Sale does not exist | 404 |
| `INVALID_SALE_STATUS` | Sale status prevents operation | 409 |
| `VALIDATION_ERROR` | Request validation failed | 400 |
| `CALCULATION_MISMATCH` | Total calculation mismatch | 422 |

### Validation Rules

#### Sale Creation Validation:
- `customerId` is required and must exist
- `totalAmount` must be greater than 0
- `items` array must not be empty
- Each item must have valid `productId`, `quantity` > 0, and `unitPrice` > 0
- Stock availability is checked for each product
- Total amount calculations are validated

#### Sale Update Validation:
- Sale must exist and be in `PENDING` status
- Same validation rules as creation apply
- Cannot update completed or cancelled sales

#### Sale Completion Validation:
- Sale must exist and be in `PENDING` status
- All business rules must be satisfied
- Inventory updates must be successful

#### Sale Cancellation Validation:
- Sale must exist and be in `PENDING` status
- Inventory restoration must be possible
- Cannot cancel completed sales

## Examples

### Frontend Integration Examples

#### JavaScript/TypeScript Example

```typescript
// Create a new sale
const createSale = async (saleData: SaleDTO): Promise<SaleDTO> => {
  const response = await fetch('/api/sales', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(saleData)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error.message);
  }

  return response.json();
};

// Get sales with pagination
const getSales = async (page = 0, size = 10, status?: string): Promise<PageResponse<SaleDTO>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });

  if (status) {
    params.append('status', status);
  }

  const response = await fetch(`/api/sales?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
};

// Complete a sale
const completeSale = async (saleId: number): Promise<SaleDTO> => {
  const response = await fetch(`/api/sales/${saleId}/complete`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error.message);
  }

  return response.json();
};
```

#### React Hook Example

```typescript
import { useState, useEffect } from 'react';

const useSales = (page = 0, size = 10, status?: string) => {
  const [sales, setSales] = useState<PageResponse<SaleDTO> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchSales = async () => {
      try {
        setLoading(true);
        const data = await getSales(page, size, status);
        setSales(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchSales();
  }, [page, size, status]);

  return { sales, loading, error };
};
```

### Sample Sale Data

```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 99.99,
      "taxPercentage": 15.0,
      "unitOfMeasure": "PCS"
    },
    {
      "productId": 2,
      "quantity": 1,
      "unitPrice": 49.99,
      "discountPercentage": 10.0,
      "taxPercentage": 15.0,
      "unitOfMeasure": "PCS"
    }
  ],
  "paymentMethod": "CREDIT_CARD",
  "billingAddress": "123 Main St, City, Country",
  "shippingAddress": "123 Main St, City, Country",
  "salesPerson": "John Doe",
  "notes": "Customer requested express delivery"
}
```

## Best Practices

1. **Always validate data on the frontend** before sending to the API
2. **Handle pagination properly** for large datasets
3. **Implement proper error handling** for all API calls
4. **Use loading states** to improve user experience
5. **Cache frequently accessed data** like customer and product information
6. **Implement optimistic updates** for better perceived performance
7. **Use proper TypeScript types** for better development experience
8. **Handle network errors gracefully** with retry mechanisms
