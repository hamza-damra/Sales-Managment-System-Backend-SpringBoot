# Missing Sales Endpoints - Backend Implementation Guide

## Overview

Based on the SalesScreen frontend implementation and HTTP 404 error logs, this document identifies the sales-related API endpoints that are currently missing from the backend but are expected by the frontend application. The frontend is successfully connecting to the correct base URL (`http://localhost:8081/api/sales`) but receiving 404 errors for specific endpoints.

## Analysis Summary

**Frontend Implementation Status:**
- ✅ SalesScreenEnhanced with two-tab system (New Sale + Sales History)
- ✅ SalesViewModel with comprehensive state management
- ✅ SalesRepository with full CRUD operations
- ✅ SalesApiService with correct endpoint URLs
- ✅ Complete data models (SaleDTO, SaleItemDTO)

**Backend Implementation Status:**
- ❌ Most sales endpoints returning 404 errors
- ✅ Products and Categories APIs working correctly
- ✅ Base URL configuration correct (`http://localhost:8081/api`)

---

## 1. Missing Sales Endpoints

### 1.1 Core Sales Management Endpoints

#### **POST /api/sales** (CRITICAL - Currently 404)
**Status:** Missing - Frontend fails when creating new sales
**Priority:** HIGH
**Frontend Usage:** Called when user clicks "إتمام البيع" (Complete Sale) button

#### **GET /api/sales** (CRITICAL - Currently 404)
**Status:** Missing - Sales history tab shows no data
**Priority:** HIGH
**Frontend Usage:** Called when loading sales history tab

#### **GET /api/sales/{id}** (HIGH)
**Status:** Missing - Sale details dialog fails
**Priority:** HIGH
**Frontend Usage:** Called when user clicks on sale in history

#### **GET /api/sales/customer/{customerId}** (MEDIUM)
**Status:** Missing - Customer-specific sales not available
**Priority:** MEDIUM
**Frontend Usage:** Potential future feature for customer sales history

#### **POST /api/sales/{id}/complete** (HIGH)
**Status:** Missing - Cannot complete pending sales
**Priority:** HIGH
**Frontend Usage:** Called when user clicks "إتمام" (Complete) button on pending sales

#### **POST /api/sales/{id}/cancel** (HIGH)
**Status:** Missing - Cannot cancel pending sales
**Priority:** HIGH
**Frontend Usage:** Called when user clicks "إلغاء" (Cancel) button on pending sales

#### **PUT /api/sales/{id}** (MEDIUM)
**Status:** Missing - Cannot edit existing sales
**Priority:** MEDIUM
**Frontend Usage:** Future enhancement for sale editing

#### **DELETE /api/sales/{id}** (LOW)
**Status:** Missing - Cannot delete sales
**Priority:** LOW
**Frontend Usage:** Administrative function

---

## 2. Required Request/Response Formats

### 2.1 POST /api/sales - Create Sale

**Request Body Structure:**
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
  ],
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
  "notes": null,
  "internalNotes": null,
  "termsAndConditions": null,
  "warrantyInfo": null,
  "deliveryDate": null,
  "expectedDeliveryDate": null,
  "deliveryStatus": "NOT_SHIPPED",
  "trackingNumber": null,
  "isGift": false,
  "giftMessage": null,
  "loyaltyPointsEarned": 99,
  "loyaltyPointsUsed": 0,
  "isReturn": false,
  "originalSaleId": null,
  "returnReason": null,
  "profitMargin": 399.99,
  "costOfGoodsSold": 600.00,
  "createdAt": "2025-07-06T14:30:00",
  "updatedAt": "2025-07-06T14:30:00"
}
```

### 2.2 GET /api/sales - Get All Sales

**Query Parameters:**
- `page` (int, default=0) - Page number for pagination
- `size` (int, default=20) - Page size (max 100)
- `sortBy` (string, default="saleDate") - Sort field
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

### 2.3 POST /api/sales/{id}/complete - Complete Sale

**Request:** No body required

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

### 2.4 POST /api/sales/{id}/cancel - Cancel Sale

**Request:** No body required

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "updatedAt": "2025-07-06T14:35:00"
}
```

---

## 3. Business Logic Requirements

### 3.1 Sale Creation Logic (POST /api/sales)

**Core Requirements:**
1. **Inventory Management:**
   - Validate product availability and stock levels
   - Reserve/decrease stock quantities for each sale item
   - Handle insufficient stock scenarios with appropriate error messages
   - Support for different unit of measures (PCS, KG, LITER, etc.)

2. **Pricing and Calculations:**
   - Validate unit prices against current product prices
   - Calculate subtotals: `quantity × unitPrice`
   - Apply discounts: `subtotal - discountAmount`
   - Calculate taxes: `(subtotal - discountAmount) × taxPercentage / 100`
   - Calculate total: `subtotal - discountAmount + taxAmount + shippingCost`
   - Validate that frontend calculations match backend calculations

3. **Customer Integration:**
   - Validate customer exists and is active
   - Update customer's purchase history and statistics
   - Handle loyalty points calculation and assignment
   - Update customer's total spent amount

4. **Sale Number Generation:**
   - Auto-generate unique sale numbers (e.g., "SALE-2025-000001")
   - Auto-generate reference numbers if not provided
   - Ensure uniqueness across the system

5. **Audit Trail:**
   - Set creation timestamp (`createdAt`)
   - Set initial update timestamp (`updatedAt`)
   - Log sale creation event for audit purposes

### 3.2 Sale Completion Logic (POST /api/sales/{id}/complete)

**Core Requirements:**
1. **Status Validation:**
   - Verify sale is in "PENDING" status
   - Prevent completion of already completed or cancelled sales

2. **Payment Processing:**
   - Update payment status to "PAID"
   - Set payment date to current timestamp
   - Handle different payment methods appropriately

3. **Inventory Finalization:**
   - Confirm stock reduction (if not done during creation)
   - Update product sales statistics
   - Update inventory movement logs

4. **Customer Updates:**
   - Award loyalty points to customer account
   - Update customer's purchase history
   - Send completion notifications if configured

5. **Financial Records:**
   - Update revenue tracking
   - Calculate and record profit margins
   - Update cost of goods sold (COGS)

### 3.3 Sale Cancellation Logic (POST /api/sales/{id}/cancel)

**Core Requirements:**
1. **Status Validation:**
   - Verify sale is in "PENDING" status
   - Prevent cancellation of completed sales

2. **Inventory Restoration:**
   - Restore stock quantities for all sale items
   - Reverse inventory movements
   - Update product availability

3. **Customer Impact:**
   - Reverse any loyalty points awarded (if applicable)
   - Update customer statistics
   - Handle refund processing if payment was made

4. **Audit and Notifications:**
   - Log cancellation reason and timestamp
   - Send cancellation notifications if configured
   - Maintain audit trail for compliance

---

## 4. Database Schema Considerations

### 4.1 Required Tables

#### **sales** Table
```sql
CREATE TABLE sales (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(255),
    sale_date DATETIME NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
    sale_number VARCHAR(50) UNIQUE NOT NULL,
    reference_number VARCHAR(50),
    subtotal DECIMAL(10,2),
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_percentage DECIMAL(5,2) DEFAULT 0.00,
    shipping_cost DECIMAL(10,2) DEFAULT 0.00,
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'CHECK', 'DIGITAL_WALLET'),
    payment_status ENUM('PENDING', 'PAID', 'PARTIAL', 'OVERDUE', 'REFUNDED') DEFAULT 'PENDING',
    payment_date DATETIME,
    due_date DATETIME,
    billing_address TEXT,
    shipping_address TEXT,
    sales_person VARCHAR(255),
    sales_channel ENUM('ONLINE', 'IN_STORE', 'PHONE', 'EMAIL') DEFAULT 'IN_STORE',
    sale_type ENUM('RETAIL', 'WHOLESALE', 'B2B') DEFAULT 'RETAIL',
    currency VARCHAR(3) DEFAULT 'USD',
    exchange_rate DECIMAL(10,4) DEFAULT 1.0000,
    notes TEXT,
    internal_notes TEXT,
    terms_and_conditions TEXT,
    warranty_info TEXT,
    delivery_date DATETIME,
    expected_delivery_date DATETIME,
    delivery_status ENUM('NOT_SHIPPED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'RETURNED') DEFAULT 'NOT_SHIPPED',
    tracking_number VARCHAR(100),
    is_gift BOOLEAN DEFAULT FALSE,
    gift_message TEXT,
    loyalty_points_earned INT DEFAULT 0,
    loyalty_points_used INT DEFAULT 0,
    is_return BOOLEAN DEFAULT FALSE,
    original_sale_id BIGINT,
    return_reason TEXT,
    profit_margin DECIMAL(10,2),
    cost_of_goods_sold DECIMAL(10,2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (original_sale_id) REFERENCES sales(id),
    INDEX idx_sale_date (sale_date),
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_sale_number (sale_number)
);
```

#### **sale_items** Table
```sql
CREATE TABLE sale_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    original_unit_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    discount_percentage DECIMAL(5,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_percentage DECIMAL(5,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    subtotal DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    serial_numbers TEXT,
    warranty_info TEXT,
    notes TEXT,
    is_returned BOOLEAN DEFAULT FALSE,
    returned_quantity INT DEFAULT 0,
    unit_of_measure VARCHAR(10) DEFAULT 'PCS',

    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_sale_id (sale_id),
    INDEX idx_product_id (product_id)
);
```

### 4.2 Required Relationships

1. **sales.customer_id → customers.id**
   - Foreign key relationship
   - Cascade updates, restrict deletes

2. **sale_items.sale_id → sales.id**
   - Foreign key relationship
   - Cascade deletes (when sale is deleted, items are deleted)

3. **sale_items.product_id → products.id**
   - Foreign key relationship
   - Restrict deletes (cannot delete product with sale history)

4. **sales.original_sale_id → sales.id**
   - Self-referencing for returns
   - Nullable for original sales

### 4.3 Required Indexes

```sql
-- Performance indexes for common queries
CREATE INDEX idx_sales_date_status ON sales(sale_date, status);
CREATE INDEX idx_sales_customer_date ON sales(customer_id, sale_date);
CREATE INDEX idx_sale_items_product_date ON sale_items(product_id, created_at);
CREATE INDEX idx_sales_payment_status ON sales(payment_status, payment_date);
```

---

## 5. Integration Points

### 5.1 Customer System Integration

**Required Integrations:**
1. **Customer Validation:**
   - Verify customer exists and is active before sale creation
   - Fetch customer details for sale record

2. **Customer Statistics Updates:**
   - Update total purchases amount
   - Update purchase count
   - Update last purchase date
   - Update loyalty points balance

3. **Customer History:**
   - Add sale to customer's purchase history
   - Update customer lifetime value calculations

### 5.2 Product/Inventory System Integration

**Required Integrations:**
1. **Stock Validation:**
   - Check product availability before sale creation
   - Validate requested quantities against available stock

2. **Stock Updates:**
   - Decrease stock quantities when sale is created/completed
   - Restore stock quantities when sale is cancelled
   - Update inventory movement logs

3. **Product Statistics:**
   - Update product sales count
   - Update product revenue totals
   - Update last sold date

### 5.3 Financial System Integration

**Required Integrations:**
1. **Revenue Tracking:**
   - Update daily/monthly/yearly revenue totals
   - Track payment method statistics
   - Update profit margin calculations

2. **Tax Calculations:**
   - Integrate with tax calculation service
   - Handle different tax rates by location/product type
   - Generate tax reports

3. **Reporting Integration:**
   - Update dashboard statistics
   - Feed data to analytics systems
   - Generate sales reports

---

## 6. Error Handling

### 6.1 HTTP Status Codes

#### **POST /api/sales - Create Sale**
- `201 Created` - Sale created successfully
- `400 Bad Request` - Invalid request data, validation errors
- `404 Not Found` - Customer or product not found
- `409 Conflict` - Insufficient stock, duplicate sale number
- `422 Unprocessable Entity` - Business logic validation failed
- `500 Internal Server Error` - Database or system error

#### **GET /api/sales - Get Sales**
- `200 OK` - Sales retrieved successfully
- `400 Bad Request` - Invalid query parameters
- `500 Internal Server Error` - Database error

#### **POST /api/sales/{id}/complete - Complete Sale**
- `200 OK` - Sale completed successfully
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale already completed or cancelled
- `422 Unprocessable Entity` - Cannot complete sale (business rules)
- `500 Internal Server Error` - Database or system error

#### **POST /api/sales/{id}/cancel - Cancel Sale**
- `200 OK` - Sale cancelled successfully
- `404 Not Found` - Sale not found
- `409 Conflict` - Sale already completed or cancelled
- `422 Unprocessable Entity` - Cannot cancel sale (business rules)
- `500 Internal Server Error` - Database or system error

### 6.2 Error Response Format

**Standard Error Response:**
```json
{
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Insufficient stock for product 'Smartphone'. Available: 5, Requested: 10",
    "details": {
      "productId": 1,
      "productName": "Smartphone",
      "availableStock": 5,
      "requestedQuantity": 10
    },
    "timestamp": "2025-07-06T14:30:00",
    "path": "/api/sales"
  }
}
```

### 6.3 Validation Error Scenarios

#### **Sale Creation Validation:**
1. **Customer Validation:**
   - Customer ID does not exist
   - Customer is inactive or suspended
   - Customer has overdue payments (if credit sales)

2. **Product Validation:**
   - Product ID does not exist
   - Product is inactive or discontinued
   - Insufficient stock quantity
   - Invalid unit price (doesn't match current price)

3. **Business Logic Validation:**
   - Total amount calculation mismatch
   - Invalid tax calculations
   - Invalid discount amounts
   - Minimum order value not met

4. **Data Validation:**
   - Missing required fields
   - Invalid date formats
   - Invalid enum values (status, payment method, etc.)
   - Negative quantities or prices

#### **Sale Status Change Validation:**
1. **Complete Sale:**
   - Sale not in PENDING status
   - Payment validation failed
   - Inventory issues during completion

2. **Cancel Sale:**
   - Sale already completed
   - Cannot restore inventory
   - Refund processing failed

---

## 7. Implementation Priority

### Phase 1 (Critical - Immediate Implementation)
1. **POST /api/sales** - Enable sale creation
2. **GET /api/sales** - Enable sales history viewing
3. **GET /api/sales/{id}** - Enable sale details viewing

### Phase 2 (High Priority - Next Sprint)
4. **POST /api/sales/{id}/complete** - Enable sale completion
5. **POST /api/sales/{id}/cancel** - Enable sale cancellation

### Phase 3 (Medium Priority - Future Enhancement)
6. **GET /api/sales/customer/{customerId}** - Customer-specific sales
7. **PUT /api/sales/{id}** - Sale editing capability

### Phase 4 (Low Priority - Administrative Features)
8. **DELETE /api/sales/{id}** - Administrative deletion

---

## 8. Testing Requirements

### 8.1 Unit Tests Required
- Sale creation with valid data
- Sale creation with invalid data (validation tests)
- Stock validation and updates
- Customer integration
- Tax and discount calculations
- Sale status transitions

### 8.2 Integration Tests Required
- End-to-end sale creation flow
- Inventory system integration
- Customer system integration
- Payment processing integration

### 8.3 Performance Tests Required
- Sales list pagination performance
- Concurrent sale creation handling
- Database query optimization
- Large dataset handling

---

## 9. Security Considerations

### 9.1 Authentication & Authorization
- Verify user authentication for all endpoints
- Implement role-based access control
- Validate user permissions for customer access
- Audit trail for all sale operations

### 9.2 Data Validation
- Sanitize all input data
- Validate business rules server-side
- Prevent SQL injection attacks
- Validate file uploads (if any)

### 9.3 Financial Security
- Encrypt sensitive financial data
- Implement transaction logging
- Prevent price manipulation
- Validate calculation integrity

---

This document provides a comprehensive guide for implementing the missing sales endpoints. The frontend SalesScreen is fully implemented and ready to consume these APIs once they are developed on the backend.
