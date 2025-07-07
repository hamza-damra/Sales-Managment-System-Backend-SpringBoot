# Returns API - Quick Reference Guide

## 🚀 Quick Start

### 1. Authentication
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

### 2. Create Return (Working Example)
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

---

## 📋 Required Fields Checklist

### Return Request ✅
- [x] `originalSaleId` - Must exist in database
- [x] `customerId` - Must exist in database  
- [x] `reason` - Use valid enum value
- [x] `totalRefundAmount` - Must be > 0

### Return Items ✅
- [x] `originalSaleItemId` - Must exist in database
- [x] `productId` - Must exist in database
- [x] `returnQuantity` - Must be > 0
- [x] `originalUnitPrice` - Must match sale item price
- [x] `refundAmount` - Amount to refund

---

## 🔍 Data Validation Steps

### Before Creating Return:
1. **Check Sale Exists**: `GET /api/sales/{id}`
2. **Verify Sale Has Items**: Ensure `items` array is not empty
3. **Confirm Customer**: `GET /api/customers/{id}`
4. **Validate Products**: `GET /api/products/{id}`

### Example Data Check:
```http
# 1. Check if sale exists and has items
GET http://localhost:8081/api/sales/8
Authorization: Bearer <jwt-token>

# Response should show:
# - Sale exists
# - Has items array with sale item IDs
# - Status is COMPLETED (recommended)
```

---

## 🎯 Valid Enum Values

### Return Reasons
```
DEFECTIVE
WRONG_ITEM  
NOT_AS_DESCRIBED
CUSTOMER_CHANGE_MIND
DAMAGED_IN_SHIPPING
OTHER
```

### Return Status
```
PENDING
APPROVED
REJECTED
REFUNDED
CANCELLED
```

### Refund Methods
```
CASH
CREDIT_CARD
BANK_TRANSFER
STORE_CREDIT
ORIGINAL_PAYMENT
```

### Item Conditions
```
NEW
LIKE_NEW
GOOD
FAIR
POOR
DAMAGED
DEFECTIVE
```

---

## ⚡ Common API Calls

### Get All Returns
```http
GET http://localhost:8081/api/returns?page=0&size=10
Authorization: Bearer <jwt-token>
```

### Get Return by ID
```http
GET http://localhost:8081/api/returns/1
Authorization: Bearer <jwt-token>
```

### Approve Return
```http
POST http://localhost:8081/api/returns/1/approve
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "approvedBy": "admin@company.com"
}
```

### Process Refund
```http
POST http://localhost:8081/api/returns/1/refund
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "refundMethod": "CREDIT_CARD",
  "refundReference": "REF123456789"
}
```

---

## 🚨 Common Errors & Solutions

### ❌ "Original sale not found with id: X"
**Solution**: Use actual sale ID from `/api/sales`

### ❌ "Original sale item not found with id: X"  
**Solution**: Check sale has items, use actual sale item ID

### ❌ "Customer not found with id: X"
**Solution**: Use actual customer ID from `/api/customers`

### ❌ "Product not found with id: X"
**Solution**: Use actual product ID from `/api/products`

### ❌ "Total amount is required"
**Solution**: Include `totalRefundAmount` field

---

## 🛠️ Testing Workflow

### Step 1: Prepare Data
```http
# Get available customers
GET /api/customers

# Get available products  
GET /api/products

# Get existing sales
GET /api/sales
```

### Step 2: Create Sale (if needed)
```http
POST /api/sales
{
  "customerId": 1,
  "totalAmount": 299.00,
  "items": [{"productId": 9, "quantity": 1, "unitPrice": 299.00}]
}
```

### Step 3: Create Return
```http
POST /api/returns
{
  "originalSaleId": [ACTUAL_SALE_ID],
  "customerId": [ACTUAL_CUSTOMER_ID],
  "items": [{"originalSaleItemId": [ACTUAL_SALE_ITEM_ID], ...}]
}
```

### Step 4: Process Return
```http
# Approve
POST /api/returns/{id}/approve

# Refund  
POST /api/returns/{id}/refund
```

---

## 📁 Documentation Files

- **Complete API Docs**: `docs/api/Returns_API_Complete_Documentation.md`
- **Postman Collection**: `docs/postman/Returns_API_Working_Examples.postman_collection.json`
- **Testing Examples**: `docs/testing/Valid_Return_Request_Examples.md`
- **Solution Guide**: `RETURNS_ENDPOINT_SOLUTION.md`

---

## 🔗 Useful Endpoints

| Purpose | Endpoint | Method |
|---------|----------|---------|
| Get test data | `/api/test-data/info` | GET |
| Valid return example | `/api/test-data/valid-return-request` | GET |
| All customers | `/api/customers` | GET |
| All products | `/api/products` | GET |
| All sales | `/api/sales` | GET |
| Create return | `/api/returns` | POST |
| Get returns | `/api/returns` | GET |

---

## 💡 Pro Tips

1. **Always check data first** - Use test-data endpoints to see what's available
2. **Use completed sales** - Only create returns for completed sales
3. **Match amounts** - Ensure refund amounts don't exceed original amounts
4. **Set proper conditions** - Use appropriate item conditions for restocking
5. **Include clear notes** - Add detailed notes for customer service
6. **Test error scenarios** - Verify error handling with invalid data

---

## 🎯 Success Response Example

```json
{
  "id": 1,
  "returnNumber": "RET-20250707-001",
  "originalSaleId": 8,
  "customerId": 1,
  "customerName": "John Doe",
  "status": "PENDING",
  "totalRefundAmount": 299.00,
  "items": [
    {
      "id": 1,
      "productName": "سماعات بلوتوث Jabra Elite 75t",
      "returnQuantity": 1,
      "refundAmount": 299.00,
      "itemCondition": "DAMAGED"
    }
  ]
}
```
