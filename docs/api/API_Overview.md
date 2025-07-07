# Sales Management System - API Documentation Overview

## Introduction

Welcome to the comprehensive API documentation for the Sales Management System. This documentation is designed specifically for frontend developers to integrate with the backend services effectively.

## System Architecture

The Sales Management System provides a robust REST API built with Spring Boot, offering comprehensive sales, inventory, customer, and analytics functionality.

**Base URL:** `http://localhost:8081/api`

## API Documentation Structure

This documentation is organized into separate files for better maintainability and ease of use:

### 📊 [Sales Management API](./Sales_Management_API.md)
Complete documentation for core sales operations including:
- **Sales CRUD Operations** - Create, read, update, delete sales
- **Sales Status Management** - Complete and cancel sales
- **Customer Sales History** - Retrieve sales by customer
- **Pagination & Filtering** - Advanced querying capabilities

**Key Endpoints:**
- `GET /api/sales` - Get all sales with pagination and filtering
- `POST /api/sales` - Create new sale
- `GET /api/sales/{id}` - Get sale details
- `POST /api/sales/{id}/complete` - Complete sale
- `POST /api/sales/{id}/cancel` - Cancel sale

### 🔄 [Returns Management API](./Returns_Management_API.md)
Comprehensive returns and refunds management including:
- **Return Request Processing** - Create and manage return requests
- **Approval Workflow** - Approve or reject returns
- **Refund Processing** - Handle refunds with multiple payment methods
- **Inventory Management** - Restock or dispose returned items

**Key Endpoints:**
- `GET /api/returns` - Get all returns with filtering
- `POST /api/returns` - Create return request
- `POST /api/returns/{id}/approve` - Approve return
- `POST /api/returns/{id}/refund` - Process refund

### 📈 [Reports & Analytics API](./Reports_Analytics_API.md)
Business intelligence and reporting capabilities including:
- **Sales Analytics** - Comprehensive sales reporting
- **Revenue Trends** - Monthly and yearly revenue analysis
- **Customer Analytics** - Customer behavior and segmentation
- **Inventory Reports** - Stock levels and movement analysis
- **Dashboard Metrics** - Real-time business metrics

**Key Endpoints:**
- `GET /api/reports/sales` - Sales report for date range
- `GET /api/reports/revenue` - Revenue trends analysis
- `GET /api/reports/dashboard` - Dashboard summary
- `GET /api/reports/top-products` - Top selling products

## Quick Start Guide

### 1. Authentication

All API endpoints require authentication. Include the Bearer token in your requests:

```http
Authorization: Bearer <your-token>
Content-Type: application/json
```

### 2. Basic Sales Flow

```typescript
// 1. Create a new sale
const newSale = await fetch('/api/sales', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    customerId: 1,
    items: [
      {
        productId: 1,
        quantity: 2,
        unitPrice: 99.99
      }
    ],
    paymentMethod: 'CREDIT_CARD'
  })
});

// 2. Complete the sale
const completedSale = await fetch(`/api/sales/${saleId}/complete`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// 3. Get sales history
const salesHistory = await fetch('/api/sales?page=0&size=10', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### 3. Basic Returns Flow

```typescript
// 1. Create return request
const returnRequest = await fetch('/api/returns', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    originalSaleId: 1,
    customerId: 1,
    returnReason: 'DEFECTIVE',
    items: [
      {
        originalSaleItemId: 1,
        productId: 1,
        returnQuantity: 1,
        refundAmount: 99.99,
        itemCondition: 'DAMAGED'
      }
    ]
  })
});

// 2. Approve return
const approvedReturn = await fetch(`/api/returns/${returnId}/approve`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    approvedBy: 'Manager Name',
    notes: 'Approved for refund'
  })
});

// 3. Process refund
const processedRefund = await fetch(`/api/returns/${returnId}/refund`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    refundMethod: 'CREDIT_CARD',
    refundReference: 'REF123456'
  })
});
```

## Common Data Models

### Core Entities

#### SaleDTO
```typescript
interface SaleDTO {
  id?: number;
  customerId: number;
  customerName?: string;
  saleDate?: string;
  totalAmount: number;
  status?: "PENDING" | "COMPLETED" | "CANCELLED" | "REFUNDED";
  items: SaleItemDTO[];
  paymentMethod?: string;
  paymentStatus?: string;
  // ... additional fields
}
```

#### SaleItemDTO
```typescript
interface SaleItemDTO {
  id?: number;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  totalPrice?: number;
  // ... additional fields
}
```

#### ReturnDTO
```typescript
interface ReturnDTO {
  id?: number;
  originalSaleId: number;
  customerId: number;
  returnDate?: string;
  totalRefundAmount?: number;
  status?: "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED";
  returnReason?: string;
  items?: ReturnItemDTO[];
  // ... additional fields
}
```

### Pagination Response
```typescript
interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
```

## Error Handling

### Standard Error Format
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {},
    "timestamp": "2025-07-06T14:30:00",
    "path": "/api/endpoint"
  }
}
```

### Common HTTP Status Codes
- `200 OK` - Successful GET, PUT requests
- `201 Created` - Successful POST requests
- `204 No Content` - Successful DELETE requests
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Business rule violation
- `422 Unprocessable Entity` - Validation failed
- `500 Internal Server Error` - Server error

## Best Practices

### 1. API Integration
- Always validate data before sending to API
- Implement proper error handling for all requests
- Use TypeScript interfaces for better type safety
- Handle loading states for better UX

### 2. Performance
- Use pagination for large datasets
- Implement caching for frequently accessed data
- Batch related API calls when possible
- Use optimistic updates where appropriate

### 3. Error Handling
- Display user-friendly error messages
- Implement retry mechanisms for network errors
- Log errors for debugging purposes
- Provide fallback UI states

### 4. Security
- Never expose authentication tokens in logs
- Validate user permissions on the frontend
- Sanitize user input before API calls
- Use HTTPS in production

## Development Tools

### Recommended Libraries

#### HTTP Client
```bash
# Axios for HTTP requests
npm install axios

# Or use fetch with better error handling
npm install ky
```

#### State Management
```bash
# React Query for API state management
npm install @tanstack/react-query

# Or SWR for data fetching
npm install swr
```

#### Form Handling
```bash
# React Hook Form for form management
npm install react-hook-form

# Yup for validation
npm install yup
```

### Example React Query Setup

```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

// Get sales with React Query
const useSales = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['sales', page, size],
    queryFn: () => getSales(page, size),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Create sale mutation
const useCreateSale = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: createSale,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sales'] });
    },
  });
};
```

## Testing

### API Testing Tools
- **Postman** - For manual API testing
- **Jest + MSW** - For unit testing with mocked APIs
- **Cypress** - For end-to-end testing

### Example Test Setup
```typescript
// Mock API responses for testing
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  rest.get('/api/sales', (req, res, ctx) => {
    return res(ctx.json({
      content: [],
      totalElements: 0,
      totalPages: 0
    }));
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

## Support and Resources

### Documentation Files
- [Sales Management API](./Sales_Management_API.md) - Complete sales operations
- [Returns Management API](./Returns_Management_API.md) - Returns and refunds
- [Reports & Analytics API](./Reports_Analytics_API.md) - Business intelligence

### Additional Resources
- **Postman Collection** - Import ready-to-use API collection
- **TypeScript Definitions** - Complete type definitions
- **Example Applications** - Sample frontend implementations

### Getting Help
- Check the specific API documentation for detailed examples
- Review error codes and messages for troubleshooting
- Use browser developer tools to inspect API requests/responses
- Test endpoints with Postman before frontend integration

---

**Last Updated:** July 6, 2025  
**API Version:** 1.0  
**Backend Framework:** Spring Boot 3.x  
**Database:** MySQL 8.x
