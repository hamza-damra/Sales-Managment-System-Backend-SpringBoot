# Sales Management System - Frontend API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Base Configuration](#base-configuration)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)

## Overview

This document provides comprehensive API documentation for frontend developers working with the Sales Management System backend. The backend is built with Spring Boot and provides RESTful APIs for managing customers, products, sales, and generating reports.

## Base Configuration

### Base URL
```
http://localhost:8081/api
```

### Headers
All requests should include:
```javascript
{
  "Content-Type": "application/json",
  "Accept": "application/json"
}
```

For authenticated requests, include:
```javascript
{
  "Authorization": "Bearer <your-jwt-token>"
}
```

### CORS
The backend supports CORS with origins set to "*" for development. In production, this should be configured to specific domains.

## Authentication

### User Roles
The system supports three user roles:
- `USER` - Standard user with basic permissions
- `ADMIN` - Administrator with full system access
- `MANAGER` - Manager with elevated permissions

### Sign Up
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

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "createdAt": "2025-07-03T10:00:00"
  }
}
```

### Sign In
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
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER",
    "createdAt": "2025-07-03T10:00:00"
  }
}
```

### Refresh Token
**Endpoint:** `POST /auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## API Endpoints

### Customers

#### Get All Customers
**Endpoint:** `GET /customers`

**Query Parameters:**
- `page` (int, default=0) - Page number
- `size` (int, default=10, max=100) - Page size
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
      "customerType": "REGULAR",
      "customerStatus": "ACTIVE",
      "totalPurchases": 1500.00,
      "loyaltyPoints": 150,
      "createdAt": "2025-07-03T10:00:00",
      "updatedAt": "2025-07-03T10:00:00"
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
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

#### Get Customer by ID
**Endpoint:** `GET /customers/{id}`

**Response (200 OK):** Single CustomerDTO object

#### Create Customer
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
  "customerType": "PREMIUM",
  "billingAddress": "456 Oak Ave",
  "shippingAddress": "456 Oak Ave"
}
```

**Response (201 Created):** Created CustomerDTO object

#### Update Customer
**Endpoint:** `PUT /customers/{id}`

**Request Body:** Same as Create Customer

**Response (200 OK):** Updated CustomerDTO object

#### Delete Customer
**Endpoint:** `DELETE /customers/{id}`

**Response (204 No Content)**

#### Search Customers
**Endpoint:** `GET /customers/search`

**Query Parameters:**
- `query` (string, required) - Search term
- `page` (int, default=0)
- `size` (int, default=10)

**Response (200 OK):** Paginated CustomerDTO list

### Products

#### Get All Products
**Endpoint:** `GET /products`

**Query Parameters:**
- `page` (int, default=0)
- `size` (int, default=10, max=100)
- `sortBy` (string, default="id")
- `sortDir` (string, default="asc")
- `category` (string, optional) - Filter by category

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Laptop Pro",
      "description": "High-performance laptop",
      "price": 1299.99,
      "costPrice": 800.00,
      "stockQuantity": 50,
      "category": "Electronics",
      "sku": "LAP-001",
      "brand": "TechBrand",
      "productStatus": "ACTIVE",
      "minStockLevel": 10,
      "reorderPoint": 15,
      "isTaxable": true,
      "taxRate": 8.5,
      "createdAt": "2025-07-03T10:00:00",
      "updatedAt": "2025-07-03T10:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 10
}
```

#### Get Product by ID
**Endpoint:** `GET /products/{id}`

#### Create Product
**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "costPrice": 15.00,
  "stockQuantity": 100,
  "category": "Electronics",
  "sku": "MOU-001",
  "brand": "TechBrand",
  "minStockLevel": 20,
  "reorderPoint": 30,
  "isTaxable": true,
  "taxRate": 8.5
}
```

#### Update Product
**Endpoint:** `PUT /products/{id}`

#### Delete Product
**Endpoint:** `DELETE /products/{id}`

#### Search Products
**Endpoint:** `GET /products/search`

**Query Parameters:**
- `query` (string, required)
- `page`, `size` (pagination)

### Sales

#### Get All Sales
**Endpoint:** `GET /sales`

**Query Parameters:**
- `page`, `size`, `sortBy`, `sortDir` (pagination/sorting)
- `status` (SaleStatus, optional) - Filter by status
- `startDate` (ISO datetime, optional) - Filter from date
- `endDate` (ISO datetime, optional) - Filter to date

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "customerId": 1,
      "customerName": "John Doe",
      "saleDate": "2025-07-03T14:30:00",
      "totalAmount": 159.98,
      "status": "COMPLETED",
      "paymentMethod": "CREDIT_CARD",
      "paymentStatus": "PAID",
      "items": [
        {
          "id": 1,
          "productId": 1,
          "productName": "Wireless Mouse",
          "quantity": 2,
          "unitPrice": 29.99,
          "totalPrice": 59.98
        }
      ]
    }
  ]
}
```

#### Get Sale by ID
**Endpoint:** `GET /sales/{id}`

#### Create Sale
**Endpoint:** `POST /sales`

**Request Body:**
```json
{
  "customerId": 1,
  "paymentMethod": "CREDIT_CARD",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 29.99
    }
  ],
  "notes": "Customer requested express delivery"
}
```

#### Update Sale
**Endpoint:** `PUT /sales/{id}`

#### Delete Sale
**Endpoint:** `DELETE /sales/{id}`

#### Get Sales by Customer
**Endpoint:** `GET /sales/customer/{customerId}`

### Reports

#### Sales Report
**Endpoint:** `GET /reports/sales`

**Query Parameters:**
- `startDate` (ISO datetime, required)
- `endDate` (ISO datetime, required)

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-06-01T00:00:00",
    "endDate": "2025-07-03T23:59:59"
  },
  "summary": {
    "totalRevenue": 15000.00,
    "totalSales": 45,
    "averageOrderValue": 333.33
  },
  "salesByStatus": {
    "COMPLETED": 40,
    "PENDING": 3,
    "CANCELLED": 2
  },
  "dailyRevenue": {
    "2025-07-01": 500.00,
    "2025-07-02": 750.00,
    "2025-07-03": 600.00
  }
}
```

#### Revenue Trends
**Endpoint:** `GET /reports/revenue`

**Query Parameters:**
- `months` (int, default=6) - Number of months to analyze

#### Top Products Report
**Endpoint:** `GET /reports/top-products`

**Query Parameters:**
- `startDate` (ISO datetime, required)
- `endDate` (ISO datetime, required)

#### Customer Analytics
**Endpoint:** `GET /reports/customer-analytics`

#### Inventory Report
**Endpoint:** `GET /reports/inventory`

#### Dashboard Summary
**Endpoint:** `GET /reports/dashboard`

**Response (200 OK):**
```json
{
  "period": "Last 30 days",
  "sales": {
    "totalRevenue": 25000.00,
    "totalSales": 75
  },
  "customers": {
    "totalCustomers": 150,
    "activeCustomers": 120
  },
  "inventory": {
    "totalProducts": 200,
    "lowStockItems": 15
  },
  "generatedAt": "2025-07-03T15:30:00"
}
```

## Data Models

### CustomerDTO
```typescript
interface CustomerDTO {
  id?: number;
  name: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  dateOfBirth?: string; // ISO date
  gender?: 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
  customerType?: 'REGULAR' | 'PREMIUM' | 'VIP';
  customerStatus?: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  billingAddress?: string;
  shippingAddress?: string;
  preferredPaymentMethod?: string;
  creditLimit?: number;
  currentBalance?: number;
  loyaltyPoints?: number;
  taxNumber?: string;
  companyName?: string;
  website?: string;
  notes?: string;
  lastPurchaseDate?: string; // ISO datetime
  totalPurchases?: number;
  isEmailVerified?: boolean;
  isPhoneVerified?: boolean;
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
}
```

### ProductDTO
```typescript
interface ProductDTO {
  id?: number;
  name: string;
  description?: string;
  price: number;
  costPrice?: number;
  stockQuantity?: number;
  category?: string;
  sku?: string;
  brand?: string;
  modelNumber?: string;
  barcode?: string;
  weight?: number;
  length?: number;
  width?: number;
  height?: number;
  productStatus?: 'ACTIVE' | 'INACTIVE' | 'DISCONTINUED';
  minStockLevel?: number;
  maxStockLevel?: number;
  reorderPoint?: number;
  reorderQuantity?: number;
  supplierName?: string;
  supplierCode?: string;
  warrantyPeriod?: number;
  expiryDate?: string; // ISO date
  manufacturingDate?: string; // ISO date
  tags?: string[];
  imageUrl?: string;
  additionalImages?: string[];
  isSerialized?: boolean;
  isDigital?: boolean;
  isTaxable?: boolean;
  taxRate?: number;
  unitOfMeasure?: string;
  discountPercentage?: number;
  locationInWarehouse?: string;
  totalSold?: number;
  totalRevenue?: number;
  lastSoldDate?: string; // ISO datetime
  lastRestockedDate?: string; // ISO datetime
  notes?: string;
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
}
```

### SaleDTO
```typescript
interface SaleDTO {
  id?: number;
  customerId: number;
  customerName?: string;
  saleDate?: string; // ISO datetime
  totalAmount: number;
  status?: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED';
  items: SaleItemDTO[];
  saleNumber?: string;
  referenceNumber?: string;
  subtotal?: number;
  discountAmount?: number;
  discountPercentage?: number;
  taxAmount?: number;
  taxPercentage?: number;
  shippingCost?: number;
  paymentMethod?: 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'BANK_TRANSFER' | 'CHECK' | 'DIGITAL_WALLET';
  paymentStatus?: 'PENDING' | 'PAID' | 'PARTIAL' | 'OVERDUE' | 'REFUNDED';
  paymentDate?: string; // ISO datetime
  dueDate?: string; // ISO date
  billingAddress?: string;
  shippingAddress?: string;
  salesPerson?: string;
  salesChannel?: string;
  saleType?: 'REGULAR' | 'WHOLESALE' | 'RETAIL' | 'ONLINE' | 'RETURN';
  currency?: string;
  exchangeRate?: number;
  notes?: string;
  internalNotes?: string;
  termsAndConditions?: string;
  warrantyInfo?: string;
  deliveryDate?: string; // ISO datetime
  expectedDeliveryDate?: string; // ISO date
  deliveryStatus?: 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
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
  createdAt?: string; // ISO datetime
  updatedAt?: string; // ISO datetime
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

### User Roles
```typescript
type UserRole = 'USER' | 'ADMIN' | 'MANAGER';
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

### Error Response Format
All API errors follow a consistent format:

```typescript
interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  errorCode: string;
  timestamp: string; // ISO datetime
  suggestions?: string;
  details?: Record<string, any>;
}
```

### HTTP Status Codes

#### 200 OK
- Successful GET, PUT requests
- Successful operations

#### 201 Created
- Successful POST requests (resource creation)

#### 204 No Content
- Successful DELETE requests
- No response body

#### 400 Bad Request
- Validation errors
- Invalid request parameters
- Business logic violations

**Example Response:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "There are 2 validation errors that need to be corrected:",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "Please review the highlighted fields and correct the validation errors.",
  "details": {
    "fieldErrors": {
      "email": "Email should be valid",
      "price": "Price must be greater than 0"
    }
  }
}
```

#### 401 Unauthorized
- Missing or invalid authentication token
- Token expired

**Example Response:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired or invalid",
  "errorCode": "INVALID_TOKEN",
  "timestamp": "2025-07-03T15:30:00"
}
```

#### 403 Forbidden
- Insufficient permissions
- Access denied

#### 404 Not Found
- Resource not found

**Example Response:**
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Customer not found with id: 123",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "Please verify the provided information and try again.",
  "details": {
    "resourceType": "Customer",
    "searchField": "id",
    "searchValue": "123"
  }
}
```

#### 409 Conflict
- Resource conflicts (e.g., duplicate email, SKU)
- Insufficient stock
- Data integrity violations (foreign key constraints)

**Insufficient Stock Example:**
```json
{
  "status": 409,
  "error": "Insufficient Stock",
  "message": "Requested quantity (50) exceeds available stock (25) for product 'Laptop Pro'",
  "errorCode": "INSUFFICIENT_STOCK",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "Please reduce the quantity, choose a different product, or check back later for restocked items.",
  "details": {
    "productId": 1,
    "productName": "Laptop Pro",
    "requestedQuantity": 50,
    "availableStock": 25
  }
}
```

**Data Integrity Violation Examples:**

*Sale with Associated Returns:*
```json
{
  "status": 409,
  "error": "Data Integrity Violation",
  "message": "Cannot delete sale because it has 3 associated returns",
  "errorCode": "SALE_HAS_RETURNS",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "Please process or cancel all associated returns before deleting this sale.",
  "details": {
    "resourceType": "Sale",
    "resourceId": 123,
    "dependentResource": "Returns"
  }
}
```

*Customer with Associated Sales:*
```json
{
  "status": 409,
  "error": "Data Integrity Violation",
  "message": "Cannot delete customer because they have 5 associated sales",
  "errorCode": "CUSTOMER_HAS_SALES",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "Please complete, cancel, or reassign all customer sales before deleting this customer.",
  "details": {
    "resourceType": "Customer",
    "resourceId": 456,
    "dependentResource": "Sales"
  }
}
```

*Product with Associated Sale Items:*
```json
{
  "status": 409,
  "error": "Data Integrity Violation",
  "message": "Cannot delete product because it appears in 10 sale records",
  "errorCode": "PRODUCT_HAS_SALE_ITEMS",
  "timestamp": "2025-07-03T15:30:00",
  "suggestions": "This product has been sold and cannot be deleted. Consider marking it as inactive instead.",
  "details": {
    "resourceType": "Product",
    "resourceId": 789,
    "dependentResource": "Sale Items"
  }
}
```

#### 500 Internal Server Error
- Unexpected server errors

### Common Error Codes

| Error Code | Description | Typical Status |
|------------|-------------|----------------|
| `VALIDATION_ERROR` | Request validation failed | 400 |
| `RESOURCE_NOT_FOUND` | Requested resource not found | 404 |
| `DUPLICATE_EMAIL` | Email already exists | 400 |
| `DUPLICATE_SKU` | Product SKU already exists | 400 |
| `INSUFFICIENT_STOCK` | Not enough stock for operation | 409 |
| `INVALID_TOKEN` | JWT token invalid/expired | 401 |
| `BUSINESS_RULE_VIOLATION` | Business logic constraint violated | 400 |
| `SALE_HAS_RETURNS` | Sale cannot be deleted due to associated returns | 409 |
| `CUSTOMER_HAS_SALES` | Customer cannot be deleted due to associated sales | 409 |
| `CUSTOMER_HAS_RETURNS` | Customer cannot be deleted due to associated returns | 409 |
| `PRODUCT_HAS_SALE_ITEMS` | Product cannot be deleted due to sale history | 409 |
| `PRODUCT_HAS_RETURN_ITEMS` | Product cannot be deleted due to return history | 409 |
| `CATEGORY_HAS_PRODUCTS` | Category cannot be deleted due to associated products | 409 |
| `SUPPLIER_HAS_PURCHASE_ORDERS` | Supplier cannot be deleted due to active orders | 409 |
| `DATABASE_CONSTRAINT_VIOLATION` | Generic database constraint violation | 409 |
| `INTERNAL_SERVER_ERROR` | Unexpected server error | 500 |

## Best Practices

### Authentication
1. **Store tokens securely**: Use secure storage (e.g., httpOnly cookies or secure localStorage)
2. **Handle token expiration**: Implement automatic token refresh
3. **Include Authorization header**: Always include `Bearer <token>` for protected endpoints

```javascript
// Example token refresh implementation
async function refreshToken() {
  try {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: getStoredRefreshToken() })
    });

    if (response.ok) {
      const data = await response.json();
      storeTokens(data.accessToken, data.refreshToken);
      return data.accessToken;
    }
  } catch (error) {
    // Redirect to login
    redirectToLogin();
  }
}
```

### Error Handling
1. **Check response status**: Always check HTTP status codes
2. **Parse error responses**: Extract meaningful error messages
3. **Provide user feedback**: Show appropriate error messages to users

```javascript
async function apiCall(url, options = {}) {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAccessToken()}`,
        ...options.headers
      }
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new ApiError(errorData);
    }

    return await response.json();
  } catch (error) {
    if (error.status === 401) {
      // Try to refresh token
      const newToken = await refreshToken();
      if (newToken) {
        // Retry the original request
        return apiCall(url, options);
      }
    }
    throw error;
  }
}
```

### Pagination
1. **Use consistent pagination**: Always include page, size parameters
2. **Handle empty results**: Check for empty content arrays
3. **Implement infinite scroll or pagination controls**

```javascript
// Example pagination implementation
const [customers, setCustomers] = useState([]);
const [pagination, setPagination] = useState({
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0
});

async function loadCustomers(page = 0) {
  const response = await apiCall(
    `/api/customers?page=${page}&size=${pagination.size}&sortBy=name&sortDir=asc`
  );

  setCustomers(response.content);
  setPagination({
    ...pagination,
    page: response.pageable.pageNumber,
    totalPages: response.totalPages,
    totalElements: response.totalElements
  });
}
```

### Data Validation
1. **Validate on frontend**: Implement client-side validation
2. **Handle server validation**: Process validation error responses
3. **Provide real-time feedback**: Show validation errors as user types

### Performance
1. **Use pagination**: Don't load all data at once
2. **Implement search**: Use search endpoints for large datasets
3. **Cache frequently used data**: Cache reference data (categories, etc.)
4. **Debounce search inputs**: Avoid excessive API calls

```javascript
// Example debounced search
import { debounce } from 'lodash';

const debouncedSearch = debounce(async (query) => {
  if (query.length >= 2) {
    const results = await apiCall(`/api/customers/search?query=${query}`);
    setSearchResults(results.content);
  }
}, 300);
```

### Date Handling
1. **Use ISO format**: All dates should be in ISO 8601 format
2. **Handle timezones**: Consider user timezone for display
3. **Validate date ranges**: Ensure start date is before end date

```javascript
// Example date formatting
const formatDate = (isoString) => {
  return new Date(isoString).toLocaleDateString();
};

const formatDateTime = (isoString) => {
  return new Date(isoString).toLocaleString();
};
```

### Security
1. **Sanitize inputs**: Always sanitize user inputs
2. **Use HTTPS**: Ensure all API calls use HTTPS in production
3. **Don't log sensitive data**: Avoid logging tokens or personal information
4. **Implement proper logout**: Clear all stored tokens on logout

### Testing
1. **Mock API responses**: Use tools like MSW for testing
2. **Test error scenarios**: Test how your app handles various error responses
3. **Test pagination**: Ensure pagination works correctly
4. **Test authentication flows**: Test login, logout, and token refresh

This documentation provides a comprehensive guide for frontend developers to integrate with the Sales Management System backend. For additional support or questions, please refer to the backend team or create an issue in the project repository.
