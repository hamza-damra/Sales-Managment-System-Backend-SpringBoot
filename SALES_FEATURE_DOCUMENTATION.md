# Sales Management Feature Documentation

## Overview

The Sales Management System provides comprehensive functionality for managing sales transactions, including creation, tracking, completion, and cancellation of sales. The system integrates with customer management, inventory control, and financial tracking.

## Features

### ✅ **Core Sales Management**
- **Sale Creation**: Create new sales with multiple items, discounts, taxes, and shipping
- **Sale Tracking**: Track sales through different statuses (PENDING, COMPLETED, CANCELLED)
- **Sale History**: View complete sales history with pagination and filtering
- **Sale Details**: Access detailed information for individual sales
- **Customer Sales**: View sales history for specific customers

### ✅ **Advanced Features**
- **Payment Management**: Track payment methods, status, and due dates
- **Delivery Tracking**: Monitor delivery status and tracking information
- **Loyalty Points**: Automatic calculation and assignment of loyalty points
- **Inventory Integration**: Automatic stock reduction and restoration
- **Financial Calculations**: Profit margins, cost of goods sold, and revenue tracking

### ✅ **Business Logic**
- **Stock Validation**: Ensures sufficient inventory before sale creation
- **Price Calculations**: Automatic calculation of subtotals, taxes, discounts, and totals
- **Customer Integration**: Updates customer purchase history and statistics
- **Audit Trail**: Complete tracking of sale creation and modifications

## API Endpoints

### Sales Management Endpoints

#### **GET /api/sales**
Retrieve all sales with pagination and filtering options.

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size (max 100)
- `sortBy` (string, default="id") - Sort field
- `sortDir` (string, default="desc") - Sort direction
- `status` (SaleStatus, optional) - Filter by status
- `startDate` (datetime, optional) - Filter by start date
- `endDate` (datetime, optional) - Filter by end date

**Response:** Paginated list of SaleDTO objects

#### **GET /api/sales/{id}**
Retrieve a specific sale by ID with all details and items.

**Path Parameters:**
- `id` (Long) - Sale ID

**Response:** Complete SaleDTO with items

#### **GET /api/sales/customer/{customerId}**
Retrieve all sales for a specific customer.

**Path Parameters:**
- `customerId` (Long) - Customer ID

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10) - Page size

**Response:** Paginated list of customer's sales

#### **POST /api/sales**
Create a new sale transaction.

**Request Body:** SaleDTO with items
**Response (201):** Created SaleDTO with generated sale number

#### **PUT /api/sales/{id}**
Update an existing sale (only if status is PENDING).

**Path Parameters:**
- `id` (Long) - Sale ID

**Request Body:** Updated SaleDTO
**Response:** Updated SaleDTO

#### **DELETE /api/sales/{id}**
Cancel a sale (soft delete by setting status to CANCELLED).

**Path Parameters:**
- `id` (Long) - Sale ID

**Response:** 204 No Content

#### **POST /api/sales/{id}/complete**
Mark a sale as completed and process final calculations.

**Path Parameters:**
- `id` (Long) - Sale ID

**Response:** Updated SaleDTO with COMPLETED status

#### **POST /api/sales/{id}/cancel**
Cancel a pending sale and restore inventory.

**Path Parameters:**
- `id` (Long) - Sale ID

**Response:** Updated SaleDTO with CANCELLED status

## Data Models

### SaleDTO Structure
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
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
  "billingAddress": "123 Main St",
  "shippingAddress": "123 Main St",
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
  "updatedAt": "2025-07-06T14:30:00"
}
```

### Sale Status Enum
- `PENDING` - Sale created but not yet completed
- `COMPLETED` - Sale finalized and payment processed
- `CANCELLED` - Sale cancelled and inventory restored
- `REFUNDED` - Sale refunded (for returns)

### Payment Method Enum
- `CASH` - Cash payment
- `CREDIT_CARD` - Credit card payment
- `DEBIT_CARD` - Debit card payment
- `BANK_TRANSFER` - Bank transfer
- `CHECK` - Check payment
- `PAYPAL` - PayPal payment
- `STRIPE` - Stripe payment
- `SQUARE` - Square payment
- `OTHER` - Other payment method
- `NET_30` - Net 30 terms

### Payment Status Enum
- `PENDING` - Payment not yet received
- `PAID` - Payment completed
- `PARTIALLY_PAID` - Partial payment received
- `OVERDUE` - Payment overdue
- `REFUNDED` - Payment refunded
- `CANCELLED` - Payment cancelled

### Sale Type Enum
- `RETAIL` - Retail sale
- `WHOLESALE` - Wholesale sale
- `B2B` - Business-to-business sale
- `ONLINE` - Online sale
- `SUBSCRIPTION` - Subscription sale
- `RETURN` - Return transaction

### Delivery Status Enum
- `NOT_SHIPPED` - Not yet shipped
- `PROCESSING` - Being processed for shipment
- `SHIPPED` - Shipped to customer
- `IN_TRANSIT` - In transit
- `DELIVERED` - Delivered to customer
- `RETURNED` - Returned by customer
- `CANCELLED` - Delivery cancelled
- `PICKED_UP` - Picked up by customer

## Business Logic

### Sale Creation Process
1. **Validation**: Validate customer exists and sale data is correct
2. **Stock Check**: Verify sufficient inventory for all items
3. **Price Calculation**: Calculate subtotals, taxes, discounts, and total
4. **Inventory Update**: Reduce stock quantities for sold items
5. **Customer Update**: Update customer purchase history and loyalty points
6. **Sale Number Generation**: Generate unique sale number
7. **Audit Trail**: Record creation timestamp and user

### Sale Completion Process
1. **Status Validation**: Ensure sale is in PENDING status
2. **Payment Processing**: Update payment status and date
3. **Loyalty Points**: Award loyalty points to customer
4. **Financial Records**: Update revenue and profit calculations
5. **Notifications**: Send completion notifications if configured

### Sale Cancellation Process
1. **Status Validation**: Ensure sale can be cancelled
2. **Inventory Restoration**: Restore stock quantities
3. **Customer Impact**: Reverse loyalty points if applicable
4. **Financial Adjustment**: Adjust revenue and profit records
5. **Audit Trail**: Record cancellation reason and timestamp

## Error Handling

### Common Error Responses
- `400 Bad Request` - Invalid request data or validation errors
- `404 Not Found` - Sale, customer, or product not found
- `409 Conflict` - Business logic conflicts (insufficient stock, invalid status)
- `422 Unprocessable Entity` - Business rule violations
- `500 Internal Server Error` - System errors

### Validation Rules
- Customer must exist and be active
- All products must exist and have sufficient stock
- Quantities must be positive integers
- Prices must be positive decimals
- Sale status transitions must follow business rules
- Payment amounts must match calculated totals

## Integration Points

### Customer System
- Validates customer existence and status
- Updates customer purchase history
- Manages loyalty points balance
- Updates customer lifetime value

### Inventory System
- Validates product availability
- Updates stock quantities
- Records inventory movements
- Updates product sales statistics

### Financial System
- Calculates taxes and discounts
- Tracks revenue and profit margins
- Updates cost of goods sold
- Generates financial reports

## Performance Considerations

### Database Optimization
- Indexed fields: sale_date, customer_id, status, sale_number
- Pagination for large result sets
- Lazy loading for related entities
- Query optimization for complex filters

### Caching Strategy
- Cache frequently accessed sales data
- Cache customer and product information
- Invalidate cache on updates
- Use Redis for distributed caching

## Security Features

### Authentication & Authorization
- JWT-based authentication required
- Role-based access control
- User permissions validation
- Audit logging for all operations

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection
- Sensitive data encryption

## Testing Coverage

### Unit Tests
- Sale creation with valid/invalid data
- Stock validation and updates
- Customer integration
- Tax and discount calculations
- Status transitions
- Error handling scenarios

### Integration Tests
- End-to-end sale workflows
- Database transactions
- External service integration
- Performance testing
- Security testing

## Monitoring & Logging

### Application Metrics
- Sale creation rate
- Average sale value
- Error rates
- Response times
- Database performance

### Business Metrics
- Daily/monthly sales volume
- Revenue trends
- Customer acquisition
- Product performance
- Inventory turnover

This documentation provides a comprehensive overview of the Sales Management feature, including all available functionality, API endpoints, data models, and business logic.
