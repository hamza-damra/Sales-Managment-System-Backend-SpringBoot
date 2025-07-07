# Returns Management API Documentation

## Overview

This document provides comprehensive API documentation for the Returns Management endpoints in the Sales Management System. These endpoints handle product returns, refunds, and return processing workflows.

**Base URL:** `http://localhost:8081/api/returns`

## Table of Contents

1. [Authentication](#authentication)
2. [Returns Endpoints](#returns-endpoints)
3. [Request/Response Models](#requestresponse-models)
4. [Error Handling](#error-handling)
5. [Examples](#examples)

## Authentication

All endpoints require proper authentication. Include the authentication token in the request headers:

```http
Authorization: Bearer <your-token>
Content-Type: application/json
```

## Returns Endpoints

### 1. Get All Returns

**Endpoint:** `GET /api/returns`

**Description:** Retrieve all returns with pagination, sorting, and filtering options.

**Query Parameters:**
- `page` (int, default=0) - Page number (0-based)
- `size` (int, default=10, max=100) - Number of items per page
- `sortBy` (string, default="id") - Field to sort by (id, returnDate, totalRefundAmount, status)
- `sortDir` (string, default="desc") - Sort direction (asc/desc)
- `status` (string, optional) - Filter by status (PENDING, APPROVED, REJECTED, COMPLETED)

**Example Request:**
```http
GET /api/returns?page=0&size=20&sortBy=returnDate&sortDir=desc&status=PENDING
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "originalSaleId": 1,
      "customerId": 1,
      "customerName": "أحمد محمد",
      "returnDate": "2025-07-06T15:30:00",
      "totalRefundAmount": 999.99,
      "status": "PENDING",
      "returnNumber": "RET-2025-000001",
      "returnReason": "DEFECTIVE",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Smartphone",
          "returnQuantity": 1,
          "refundAmount": 999.99,
          "itemCondition": "DAMAGED"
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 2. Get Return by ID

**Endpoint:** `GET /api/returns/{id}`

**Description:** Retrieve a specific return with complete details and all items.

**Path Parameters:**
- `id` (Long) - Return ID

**Example Request:**
```http
GET /api/returns/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "originalSaleId": 1,
  "customerId": 1,
  "customerName": "أحمد محمد",
  "returnDate": "2025-07-06T15:30:00",
  "totalRefundAmount": 999.99,
  "status": "PENDING",
  "returnNumber": "RET-2025-000001",
  "returnReason": "DEFECTIVE",
  "returnType": "FULL_RETURN",
  "requestedBy": "Customer",
  "approvedBy": null,
  "approvalDate": null,
  "rejectedBy": null,
  "rejectionDate": null,
  "rejectionReason": null,
  "refundMethod": null,
  "refundReference": null,
  "refundDate": null,
  "restockingFee": 0.0,
  "notes": "Product arrived damaged",
  "internalNotes": null,
  "createdAt": "2025-07-06T15:30:00",
  "updatedAt": "2025-07-06T15:30:00",
  "items": [
    {
      "id": 1,
      "originalSaleItemId": 1,
      "productId": 1,
      "productName": "Smartphone",
      "productSku": "SP-001",
      "returnQuantity": 1,
      "originalUnitPrice": 999.99,
      "refundAmount": 999.99,
      "restockingFee": 0.0,
      "conditionNotes": "Screen cracked",
      "itemCondition": "DAMAGED",
      "serialNumbers": "SN123456",
      "isRestockable": false,
      "disposalReason": "Damaged beyond repair"
    }
  ]
}
```

### 3. Create Return

**Endpoint:** `POST /api/returns`

**Description:** Create a new return request for a sale.

**Request Body:**
```json
{
  "originalSaleId": 1,
  "customerId": 1,
  "returnReason": "DEFECTIVE",
  "returnType": "FULL_RETURN",
  "requestedBy": "Customer",
  "notes": "Product arrived damaged",
  "items": [
    {
      "originalSaleItemId": 1,
      "productId": 1,
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

**Response (201 Created):**
```json
{
  "id": 1,
  "originalSaleId": 1,
  "customerId": 1,
  "returnDate": "2025-07-06T15:30:00",
  "totalRefundAmount": 999.99,
  "status": "PENDING",
  "returnNumber": "RET-2025-000001",
  "createdAt": "2025-07-06T15:30:00"
}
```

### 4. Update Return

**Endpoint:** `PUT /api/returns/{id}`

**Description:** Update an existing return (only if status is PENDING).

**Path Parameters:**
- `id` (Long) - Return ID

**Request Body:** Same as Create Return

**Response (200 OK):** Updated ReturnDTO

### 5. Delete Return

**Endpoint:** `DELETE /api/returns/{id}`

**Description:** Delete a return request (only if status is PENDING).

**Path Parameters:**
- `id` (Long) - Return ID

**Response (204 No Content)**

### 6. Approve Return

**Endpoint:** `POST /api/returns/{id}/approve`

**Description:** Approve a pending return request.

**Path Parameters:**
- `id` (Long) - Return ID

**Request Body:**
```json
{
  "approvedBy": "Manager Name",
  "notes": "Approval notes"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "APPROVED",
  "approvedBy": "Manager Name",
  "approvalDate": "2025-07-06T16:00:00",
  "updatedAt": "2025-07-06T16:00:00"
}
```

### 7. Reject Return

**Endpoint:** `POST /api/returns/{id}/reject`

**Description:** Reject a pending return request.

**Path Parameters:**
- `id` (Long) - Return ID

**Request Body:**
```json
{
  "rejectedBy": "Manager Name",
  "rejectionReason": "Return period expired"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "REJECTED",
  "rejectedBy": "Manager Name",
  "rejectionDate": "2025-07-06T16:00:00",
  "rejectionReason": "Return period expired",
  "updatedAt": "2025-07-06T16:00:00"
}
```

### 8. Process Refund

**Endpoint:** `POST /api/returns/{id}/refund`

**Description:** Process refund for an approved return.

**Path Parameters:**
- `id` (Long) - Return ID

**Request Body:**
```json
{
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "COMPLETED",
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456",
  "refundDate": "2025-07-06T16:30:00",
  "updatedAt": "2025-07-06T16:30:00"
}
```

### 9. Get Return with Items

**Endpoint:** `GET /api/returns/{id}/items`

**Description:** Get return details with all items included.

**Path Parameters:**
- `id` (Long) - Return ID

**Response (200 OK):** Complete ReturnDTO with items array

### 10. Get Returns by Customer

**Endpoint:** `GET /api/returns/customer/{customerId}`

**Description:** Get all returns for a specific customer.

**Path Parameters:**
- `customerId` (Long) - Customer ID

**Response (200 OK):** Array of ReturnDTO objects

## Request/Response Models

### ReturnDTO

```typescript
interface ReturnDTO {
  id?: number;
  originalSaleId: number;
  customerId: number;
  customerName?: string;
  returnDate?: string; // ISO datetime format
  totalRefundAmount?: number;
  status?: "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED";
  returnNumber?: string;
  returnReason?: "DEFECTIVE" | "WRONG_ITEM" | "NOT_AS_DESCRIBED" | "CHANGED_MIND" | "DAMAGED_SHIPPING" | "OTHER";
  returnType?: "FULL_RETURN" | "PARTIAL_RETURN" | "EXCHANGE";
  requestedBy?: string;
  approvedBy?: string;
  approvalDate?: string; // ISO datetime format
  rejectedBy?: string;
  rejectionDate?: string; // ISO datetime format
  rejectionReason?: string;
  refundMethod?: "CASH" | "CREDIT_CARD" | "BANK_TRANSFER" | "STORE_CREDIT" | "EXCHANGE";
  refundReference?: string;
  refundDate?: string; // ISO datetime format
  restockingFee?: number;
  notes?: string;
  internalNotes?: string;
  createdAt?: string; // ISO datetime format
  updatedAt?: string; // ISO datetime format
  items?: ReturnItemDTO[];
}
```

### ReturnItemDTO

```typescript
interface ReturnItemDTO {
  id?: number;
  returnId?: number;
  originalSaleItemId: number;
  productId: number;
  productName?: string;
  productSku?: string;
  returnQuantity: number;
  originalUnitPrice: number;
  refundAmount: number;
  restockingFee?: number;
  conditionNotes?: string;
  itemCondition?: "NEW" | "GOOD" | "FAIR" | "POOR" | "DAMAGED" | "DEFECTIVE";
  serialNumbers?: string;
  isRestockable?: boolean;
  disposalReason?: string;
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
    "path": "/api/returns"
  }
}
```

### Common Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| `RETURN_NOT_FOUND` | Return does not exist | 404 |
| `SALE_NOT_FOUND` | Original sale does not exist | 404 |
| `INVALID_RETURN_STATUS` | Return status prevents operation | 409 |
| `RETURN_PERIOD_EXPIRED` | Return period has expired | 422 |
| `INVALID_RETURN_QUANTITY` | Return quantity exceeds available | 422 |
| `ALREADY_RETURNED` | Item already fully returned | 409 |
| `VALIDATION_ERROR` | Request validation failed | 400 |
| `REFUND_PROCESSING_ERROR` | Refund processing failed | 500 |

### Validation Rules

#### Return Creation Validation:
- `originalSaleId` must exist and be completed
- `customerId` must match the original sale
- Return must be within allowed return period
- `returnQuantity` cannot exceed available quantity
- Items cannot be already fully returned

#### Return Approval/Rejection Validation:
- Return must be in `PENDING` status
- `approvedBy` or `rejectedBy` is required
- Cannot approve/reject already processed returns

#### Refund Processing Validation:
- Return must be in `APPROVED` status
- `refundMethod` is required
- Refund amount must be valid
- Cannot process refund twice

## Examples

### Frontend Integration Examples

#### JavaScript/TypeScript Example

```typescript
// Create a return request
const createReturn = async (returnData: ReturnDTO): Promise<ReturnDTO> => {
  const response = await fetch('/api/returns', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(returnData)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error.message);
  }

  return response.json();
};

// Get returns with pagination
const getReturns = async (page = 0, size = 10, status?: string): Promise<PageResponse<ReturnDTO>> => {
  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });

  if (status) {
    params.append('status', status);
  }

  const response = await fetch(`/api/returns?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
};

// Approve a return
const approveReturn = async (returnId: number, approvedBy: string, notes?: string): Promise<ReturnDTO> => {
  const response = await fetch(`/api/returns/${returnId}/approve`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ approvedBy, notes })
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error.message);
  }

  return response.json();
};

// Process refund
const processRefund = async (returnId: number, refundMethod: string, refundReference?: string): Promise<ReturnDTO> => {
  const response = await fetch(`/api/returns/${returnId}/refund`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ refundMethod, refundReference })
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

const useReturns = (page = 0, size = 10, status?: string) => {
  const [returns, setReturns] = useState<PageResponse<ReturnDTO> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchReturns = async () => {
      try {
        setLoading(true);
        const data = await getReturns(page, size, status);
        setReturns(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchReturns();
  }, [page, size, status]);

  return { returns, loading, error };
};

// Return management hook
const useReturnManagement = () => {
  const [processing, setProcessing] = useState(false);

  const approveReturn = async (returnId: number, approvedBy: string, notes?: string) => {
    setProcessing(true);
    try {
      const result = await approveReturn(returnId, approvedBy, notes);
      return result;
    } finally {
      setProcessing(false);
    }
  };

  const rejectReturn = async (returnId: number, rejectedBy: string, rejectionReason: string) => {
    setProcessing(true);
    try {
      const response = await fetch(`/api/returns/${returnId}/reject`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ rejectedBy, rejectionReason })
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error.message);
      }

      return response.json();
    } finally {
      setProcessing(false);
    }
  };

  return { approveReturn, rejectReturn, processing };
};
```

### Sample Return Data

```json
{
  "originalSaleId": 1,
  "customerId": 1,
  "returnReason": "DEFECTIVE",
  "returnType": "PARTIAL_RETURN",
  "requestedBy": "Customer Service",
  "notes": "Customer reported screen flickering issue",
  "items": [
    {
      "originalSaleItemId": 1,
      "productId": 1,
      "returnQuantity": 1,
      "refundAmount": 999.99,
      "conditionNotes": "Screen flickering, otherwise good condition",
      "itemCondition": "DEFECTIVE",
      "serialNumbers": "SN123456789",
      "isRestockable": false,
      "disposalReason": "Defective unit - return to manufacturer"
    }
  ]
}
```

## Return Workflow

### 1. Return Request Creation
1. Customer or staff creates return request
2. System validates return eligibility
3. Return is created with `PENDING` status
4. Return number is auto-generated

### 2. Return Review Process
1. Manager reviews return request
2. Manager can approve or reject the return
3. If approved, return status becomes `APPROVED`
4. If rejected, return status becomes `REJECTED` with reason

### 3. Refund Processing
1. For approved returns, refund can be processed
2. Refund method and reference are recorded
3. Return status becomes `COMPLETED`
4. Inventory is updated if items are restockable

### 4. Inventory Management
- Restockable items are added back to inventory
- Non-restockable items are marked for disposal
- Product statistics are updated

## Best Practices

1. **Validate return eligibility** before creating return requests
2. **Check return policies** (time limits, condition requirements)
3. **Document item conditions** thoroughly for audit purposes
4. **Handle refund processing** with proper error handling
5. **Update inventory** correctly based on item restockability
6. **Maintain audit trail** for all return operations
7. **Implement proper authorization** for approval/rejection actions
8. **Handle partial returns** correctly with quantity tracking
```
