# Returns API - Complete Documentation

## Base URL
```
http://localhost:8081/api/returns
```

## Authentication
All endpoints require Bearer token authentication:
```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints Overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/returns` | Get all returns (paginated) |
| GET | `/api/returns/{id}` | Get return by ID |
| POST | `/api/returns` | Create new return |
| PUT | `/api/returns/{id}` | Update return |
| DELETE | `/api/returns/{id}` | Delete return |
| POST | `/api/returns/{id}/approve` | Approve return |
| POST | `/api/returns/{id}/reject` | Reject return |
| POST | `/api/returns/{id}/refund` | Process refund |
| GET | `/api/returns/{id}/items` | Get return with items |
| GET | `/api/returns/customer/{customerId}` | Get returns by customer |
| GET | `/api/returns/search` | Search returns |

---

## 1. Create Return Request

**POST** `/api/returns`

Creates a new return request for a completed sale.

### Request Headers
```
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### Request Body
```json
{
  "originalSaleId": 8,
  "customerId": 1,
  "reason": "DAMAGED_IN_SHIPPING",
  "status": "PENDING",
  "totalRefundAmount": 299.00,
  "notes": "Customer reported damaged Jabra headphones",
  "refundMethod": "CASH",
  "items": [
    {
      "originalSaleItemId": 11,
      "productId": 9,
      "returnQuantity": 1,
      "originalUnitPrice": 299.00,
      "refundAmount": 299.00,
      "itemCondition": "DAMAGED",
      "conditionNotes": "Headphones damaged during shipping",
      "isRestockable": false
    }
  ]
}
```

### Response (201 Created)
```json
{
  "id": 1,
  "returnNumber": "RET-20250707-001",
  "originalSaleId": 8,
  "originalSaleNumber": "SALE-1751889823850-1-5BBB",
  "customerId": 1,
  "customerName": "John Doe",
  "returnDate": "2025-07-07T15:10:00",
  "reason": "DAMAGED_IN_SHIPPING",
  "status": "PENDING",
  "totalRefundAmount": 299.00,
  "notes": "Customer reported damaged Jabra headphones",
  "refundMethod": "CASH",
  "createdAt": "2025-07-07T15:10:00",
  "updatedAt": "2025-07-07T15:10:00",
  "items": [
    {
      "id": 1,
      "returnId": 1,
      "originalSaleItemId": 11,
      "productId": 9,
      "productName": "سماعات بلوتوث Jabra Elite 75t",
      "returnQuantity": 1,
      "originalUnitPrice": 299.00,
      "refundAmount": 299.00,
      "itemCondition": "DAMAGED",
      "conditionNotes": "Headphones damaged during shipping",
      "isRestockable": false
    }
  ]
}
```

---

## 2. Get All Returns

**GET** `/api/returns`

Retrieves a paginated list of all returns.

### Query Parameters
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `sortBy` (optional): Sort field (default: "id")
- `sortDir` (optional): Sort direction (default: "desc")
- `status` (optional): Filter by status

### Example Request
```
GET /api/returns?page=0&size=10&sortBy=returnDate&sortDir=desc&status=PENDING
```

### Response (200 OK)
```json
{
  "content": [
    {
      "id": 1,
      "returnNumber": "RET-20250707-001",
      "originalSaleId": 8,
      "customerId": 1,
      "customerName": "John Doe",
      "status": "PENDING",
      "totalRefundAmount": 299.00,
      "returnDate": "2025-07-07T15:10:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0
}
```

---

## 3. Get Return by ID

**GET** `/api/returns/{id}`

Retrieves a specific return by its ID with full details.

### Response (200 OK)
```json
{
  "id": 1,
  "returnNumber": "RET-20250707-001",
  "originalSaleId": 8,
  "originalSaleNumber": "SALE-1751889823850-1-5BBB",
  "customerId": 1,
  "customerName": "John Doe",
  "returnDate": "2025-07-07T15:10:00",
  "reason": "DAMAGED_IN_SHIPPING",
  "status": "PENDING",
  "totalRefundAmount": 299.00,
  "notes": "Customer reported damaged Jabra headphones",
  "refundMethod": "CASH",
  "items": [
    {
      "id": 1,
      "originalSaleItemId": 11,
      "productId": 9,
      "productName": "سماعات بلوتوث Jabra Elite 75t",
      "returnQuantity": 1,
      "originalUnitPrice": 299.00,
      "refundAmount": 299.00,
      "itemCondition": "DAMAGED",
      "isRestockable": false
    }
  ]
}
```

---

## 4. Approve Return

**POST** `/api/returns/{id}/approve`

Approves a pending return request.

### Request Body
```json
{
  "approvedBy": "admin@company.com"
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "status": "APPROVED",
  "processedBy": "admin@company.com",
  "processedDate": "2025-07-07T15:15:00"
}
```

---

## 5. Reject Return

**POST** `/api/returns/{id}/reject`

Rejects a return request with a reason.

### Request Body
```json
{
  "rejectedBy": "admin@company.com",
  "rejectionReason": "Items not eligible for return"
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "status": "REJECTED",
  "processedBy": "admin@company.com",
  "processedDate": "2025-07-07T15:15:00",
  "notes": "Items not eligible for return"
}
```

---

## 6. Process Refund

**POST** `/api/returns/{id}/refund`

Processes the refund for an approved return.

### Request Body
```json
{
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456789"
}
```

### Response (200 OK)
```json
{
  "id": 1,
  "status": "REFUNDED",
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456789",
  "refundDate": "2025-07-07T15:20:00"
}
```

---

## Data Models

### Return Status Enum
- `PENDING` - Initial status when return is created
- `APPROVED` - Return has been approved by administrator
- `REJECTED` - Return has been rejected
- `REFUNDED` - Refund has been processed
- `CANCELLED` - Return has been cancelled

### Return Reason Enum
- `DEFECTIVE` - Product is defective
- `WRONG_ITEM` - Wrong item was shipped
- `NOT_AS_DESCRIBED` - Item doesn't match description
- `CUSTOMER_CHANGE_MIND` - Customer changed their mind
- `DAMAGED_IN_SHIPPING` - Item was damaged during shipping
- `OTHER` - Other reason

### Refund Method Enum
- `CASH` - Cash refund
- `CREDIT_CARD` - Credit card refund
- `BANK_TRANSFER` - Bank transfer
- `STORE_CREDIT` - Store credit
- `ORIGINAL_PAYMENT` - Refund to original payment method

### Item Condition Enum
- `NEW` - Item is in new condition
- `LIKE_NEW` - Item is like new
- `GOOD` - Item is in good condition
- `FAIR` - Item is in fair condition
- `POOR` - Item is in poor condition
- `DAMAGED` - Item is damaged
- `DEFECTIVE` - Item is defective

---

## Required Fields

### Return Request
- `originalSaleId` - ID of the original sale
- `customerId` - ID of the customer
- `reason` - Reason for return (enum)
- `totalRefundAmount` - Total amount to be refunded

### Return Item
- `originalSaleItemId` - ID of the original sale item
- `productId` - ID of the product
- `returnQuantity` - Quantity being returned
- `originalUnitPrice` - Original unit price from sale
- `refundAmount` - Amount to be refunded for this item

---

## Error Responses

### 400 Bad Request
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "There are validation errors",
  "validationErrors": {
    "originalSaleId": "Original sale ID is required"
  }
}
```

### 404 Not Found
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Original sale not found with id: 999. Available sales: 1, 2, 3, 4, 5, 6, 7, 8"
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

---

## Step-by-Step Testing Guide

### Prerequisites
1. Ensure the application is running on `http://localhost:8081`
2. Have a valid JWT token for authentication
3. Ensure test data exists (customers, products, sales)

### Step 1: Get Authentication Token
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "your-username",
  "password": "your-password"
}
```

### Step 2: Check Available Data
```http
### Get customers
GET http://localhost:8081/api/customers
Authorization: Bearer <jwt-token>

### Get products
GET http://localhost:8081/api/products
Authorization: Bearer <jwt-token>

### Get sales
GET http://localhost:8081/api/sales
Authorization: Bearer <jwt-token>
```

### Step 3: Create a Sale (if needed)
```http
POST http://localhost:8081/api/sales
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 299.00,
  "subtotal": 299.00,
  "status": "COMPLETED",
  "paymentStatus": "PAID",
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "productId": 9,
      "quantity": 1,
      "unitPrice": 299.00,
      "totalPrice": 299.00
    }
  ]
}
```

### Step 4: Get Sale Details
```http
GET http://localhost:8081/api/sales/{sale-id}
Authorization: Bearer <jwt-token>
```

### Step 5: Create Return Request
```http
POST http://localhost:8081/api/returns
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "originalSaleId": 8,
  "customerId": 1,
  "reason": "DAMAGED_IN_SHIPPING",
  "status": "PENDING",
  "totalRefundAmount": 299.00,
  "notes": "Customer reported damaged Jabra headphones",
  "refundMethod": "CASH",
  "items": [
    {
      "originalSaleItemId": 11,
      "productId": 9,
      "returnQuantity": 1,
      "originalUnitPrice": 299.00,
      "refundAmount": 299.00,
      "itemCondition": "DAMAGED",
      "conditionNotes": "Headphones damaged during shipping",
      "isRestockable": false
    }
  ]
}
```

### Step 6: Test Return Management
```http
### Approve return
POST http://localhost:8081/api/returns/{return-id}/approve
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "approvedBy": "admin@company.com"
}

### Process refund
POST http://localhost:8081/api/returns/{return-id}/refund
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456789"
}
```

---

## Common Issues and Solutions

### Issue: "Original sale not found"
**Solution**: Use actual sale IDs from your database. Check `/api/sales` to see available sales.

### Issue: "Original sale item not found"
**Solution**: Ensure the sale has items. Create a new sale with items if needed.

### Issue: "Customer not found"
**Solution**: Use actual customer IDs from `/api/customers`.

### Issue: "Product not found"
**Solution**: Use actual product IDs from `/api/products`.

### Issue: Missing required fields
**Solution**: Ensure all required fields are included:
- `originalSaleId`, `customerId`, `reason`, `totalRefundAmount`
- For items: `originalSaleItemId`, `productId`, `returnQuantity`, `originalUnitPrice`, `refundAmount`

---

## Best Practices

1. **Always validate data first**: Check that sales, customers, and products exist before creating returns
2. **Use completed sales**: Only create returns for sales with status "COMPLETED"
3. **Match amounts**: Ensure refund amounts don't exceed original sale amounts
4. **Proper item conditions**: Set appropriate item conditions for restocking decisions
5. **Clear notes**: Provide clear notes for customer service reference
6. **Track references**: Use refund references for accounting purposes
