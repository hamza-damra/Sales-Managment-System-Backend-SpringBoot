# Sales-Promotion Integration API Documentation

## Overview

This document describes the enhanced Sales API with integrated promotion functionality. The system now supports applying promotions during sale creation, managing promotions on existing sales, and automatic promotion detection.

## Key Features

- **Promotion Application**: Apply promotions during sale creation or to existing sales
- **Automatic Promotion Detection**: Auto-apply eligible promotions based on customer and order criteria
- **Promotion Validation**: Real-time validation of coupon codes and promotion eligibility
- **Detailed Pricing Breakdown**: Complete breakdown of original prices, discounts, and final totals
- **Multiple Promotion Support**: Support for applying multiple promotions to a single sale
- **Promotion Management**: Add or remove promotions from pending sales

## API Endpoints

### Base URL
```
/api/sales
```

### 1. Create Sale with Optional Promotion

**Endpoint:** `POST /api/sales`

**Description:** Creates a new sale with optional promotion application via coupon code

**Parameters:**
- `couponCode` (query parameter, optional): Coupon code to apply to the sale

**Request Body:**
```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 100.00
    }
  ],
  "totalAmount": 200.00,
  "paymentMethod": "CREDIT_CARD",
  "notes": "Customer order with promotion"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "saleNumber": "SALE-2024-001",
  "saleDate": "2024-07-09T10:30:00",
  "subtotal": 200.00,
  "originalTotal": 200.00,
  "promotionDiscountAmount": 20.00,
  "finalTotal": 180.00,
  "totalAmount": 180.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Sample Product",
      "quantity": 2,
      "unitPrice": 100.00,
      "totalPrice": 200.00
    }
  ],
  "appliedPromotions": [
    {
      "id": 1,
      "promotionId": 1,
      "promotionName": "Summer Sale 2024",
      "promotionType": "PERCENTAGE",
      "couponCode": "SUMMER20",
      "discountAmount": 20.00,
      "discountPercentage": 10.00,
      "originalAmount": 200.00,
      "finalAmount": 180.00,
      "isAutoApplied": false,
      "appliedAt": "2024-07-09T10:30:00",
      "displayText": "Summer Sale 2024 (10.0% off)",
      "typeDisplay": "Percentage Discount"
    }
  ],
  "promotionDetails": {
    "id": 1,
    "name": "Summer Sale 2024",
    "type": "PERCENTAGE",
    "discountValue": 10.00,
    "couponCode": "SUMMER20"
  },
  "totalSavings": 20.00,
  "hasPromotions": true,
  "promotionCount": 1,
  "createdAt": "2024-07-09T10:30:00",
  "updatedAt": "2024-07-09T10:30:00"
}
```

### 2. Apply Promotion to Existing Sale

**Endpoint:** `POST /api/sales/{id}/apply-promotion`

**Description:** Applies a promotion to an existing pending sale

**Path Parameters:**
- `id`: Sale ID

**Query Parameters:**
- `couponCode`: Coupon code to apply

**Response (200 OK):**
```json
{
  "id": 1,
  "customerId": 1,
  "originalTotal": 200.00,
  "promotionDiscountAmount": 20.00,
  "finalTotal": 180.00,
  "appliedPromotions": [
    {
      "id": 2,
      "promotionId": 1,
      "promotionName": "Flash Sale",
      "couponCode": "FLASH10",
      "discountAmount": 20.00,
      "isAutoApplied": false,
      "appliedAt": "2024-07-09T11:00:00"
    }
  ],
  "totalSavings": 20.00,
  "hasPromotions": true,
  "promotionCount": 1
}
```

**Error Responses:**
- `400 Bad Request`: Invalid sale ID or coupon code
- `404 Not Found`: Sale not found
- `400 Bad Request`: Business logic error (e.g., invalid coupon, sale not pending)

### 3. Remove Promotion from Sale

**Endpoint:** `DELETE /api/sales/{id}/remove-promotion`

**Description:** Removes a specific promotion from a pending sale

**Path Parameters:**
- `id`: Sale ID

**Query Parameters:**
- `promotionId`: ID of the promotion to remove

**Response (200 OK):**
```json
{
  "id": 1,
  "customerId": 1,
  "originalTotal": 200.00,
  "promotionDiscountAmount": 0.00,
  "finalTotal": 200.00,
  "appliedPromotions": [],
  "totalSavings": 0.00,
  "hasPromotions": false,
  "promotionCount": 0
}
```

**Error Responses:**
- `400 Bad Request`: Invalid sale ID or promotion ID
- `404 Not Found`: Sale not found
- `400 Bad Request`: Business logic error (e.g., promotion not found, sale not pending)

### 4. Get Eligible Promotions for Sale

**Endpoint:** `GET /api/sales/{id}/eligible-promotions`

**Description:** Retrieves all promotions eligible for a specific sale

**Path Parameters:**
- `id`: Sale ID

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Summer Sale 2024",
    "description": "20% off all summer items",
    "type": "PERCENTAGE",
    "discountValue": 20.00,
    "minimumOrderAmount": 50.00,
    "maximumDiscountAmount": 100.00,
    "couponCode": "SUMMER20",
    "isCurrentlyActive": true,
    "customerEligibility": "ALL",
    "applicableCategories": ["CLOTHING", "ACCESSORIES"]
  },
  {
    "id": 2,
    "name": "New Customer Discount",
    "type": "FIXED_AMOUNT",
    "discountValue": 15.00,
    "couponCode": "WELCOME15",
    "isCurrentlyActive": true,
    "customerEligibility": "NEW_CUSTOMERS",
    "autoApply": true
  }
]
```

### 5. Enhanced Get Sale by ID

**Endpoint:** `GET /api/sales/{id}`

**Description:** Retrieves detailed sale information including applied promotions

**Response includes all promotion-related fields as shown in the create sale response above.**

## Data Models

### AppliedPromotionDTO

```json
{
  "id": 1,
  "saleId": 1,
  "promotionId": 1,
  "promotionName": "Summer Sale 2024",
  "promotionType": "PERCENTAGE",
  "couponCode": "SUMMER20",
  "discountAmount": 20.00,
  "discountPercentage": 10.00,
  "originalAmount": 200.00,
  "finalAmount": 180.00,
  "isAutoApplied": false,
  "appliedAt": "2024-07-09T10:30:00",
  "displayText": "Summer Sale 2024 (10.0% off)",
  "typeDisplay": "Percentage Discount",
  "savingsAmount": 20.00,
  "isPercentageDiscount": true,
  "isFixedAmountDiscount": false
}
```

### Enhanced SaleDTO Fields

**New Promotion-Related Fields:**
- `promotionId`: ID of the primary promotion applied
- `couponCode`: Coupon code used (if any)
- `originalTotal`: Total before promotions
- `finalTotal`: Total after promotions
- `promotionDiscountAmount`: Total discount from promotions
- `appliedPromotions`: List of all applied promotions
- `promotionDetails`: Details of the primary promotion
- `totalSavings`: Total amount saved from all promotions
- `hasPromotions`: Boolean indicating if promotions are applied
- `promotionCount`: Number of promotions applied

## Business Logic

### Promotion Eligibility Rules

1. **Customer Eligibility**: Promotions can be restricted to specific customer types (VIP, NEW_CUSTOMERS, etc.)
2. **Product/Category Applicability**: Promotions can be limited to specific products or categories
3. **Minimum Order Amount**: Promotions may require a minimum order value
4. **Date Range**: Promotions must be within their active date range
5. **Usage Limits**: Promotions may have usage count restrictions
6. **Stacking Rules**: Some promotions can be combined, others cannot

### Automatic Promotion Application

- Promotions marked with `autoApply: true` are automatically applied during sale creation
- Auto-applied promotions are evaluated based on customer eligibility and order criteria
- Manual promotions require explicit coupon code application

### Promotion Calculation Priority

1. Product-specific promotions
2. Category-specific promotions  
3. Order-level promotions
4. Customer-specific promotions

### Error Handling

**Common Error Scenarios:**
- Invalid or expired coupon codes
- Promotions not applicable to customer type
- Minimum order amount not met
- Promotion usage limit exceeded
- Attempting to modify completed sales
- Product/category restrictions not met

## Usage Examples

### Example 1: Create Sale with Coupon Code

```bash
curl -X POST "http://localhost:8080/api/sales?couponCode=SUMMER20" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 100.00
      }
    ],
    "totalAmount": 200.00
  }'
```

### Example 2: Apply Promotion to Existing Sale

```bash
curl -X POST "http://localhost:8080/api/sales/1/apply-promotion?couponCode=FLASH10"
```

### Example 3: Get Eligible Promotions

```bash
curl -X GET "http://localhost:8080/api/sales/1/eligible-promotions"
```

### Example 4: Remove Promotion

```bash
curl -X DELETE "http://localhost:8080/api/sales/1/remove-promotion?promotionId=1"
```

## Testing

The integration includes comprehensive unit and integration tests covering:
- Promotion application scenarios
- Validation logic
- Error handling
- Edge cases
- Controller endpoint testing
- Service layer testing

## Notes

- Promotions can only be added/removed from sales in PENDING status
- Applied promotions are preserved when sales are completed
- Promotion usage counts are automatically managed
- All monetary calculations use BigDecimal for precision
- Promotion details are cached in AppliedPromotion for historical accuracy
