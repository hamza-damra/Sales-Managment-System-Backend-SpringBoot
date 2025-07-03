# Spring Boot Backend REST API Requirements for Sales Management Desktop App

## Project Overview
A REST API backend for a Sales Management Desktop Application built with Spring Boot, providing comprehensive sales tracking, customer management, and reporting capabilities.

## Core Features

### 1. Customer Management
- **Create Customer**: Add new customers with contact information
- **Update Customer**: Modify existing customer details
- **Delete Customer**: Remove customers from the system
- **List Customers**: Retrieve all customers with pagination
- **Search Customers**: Find customers by name, email, or phone

### 2. Product Management
- **Create Product**: Add new products with pricing and inventory
- **Update Product**: Modify product details, pricing, and stock
- **Delete Product**: Remove products from catalog
- **List Products**: Retrieve all products with pagination and filtering
- **Search Products**: Find products by name, category, or SKU
- **Stock Management**: Track inventory levels

### 3. Sales Management
- **Create Sale**: Record new sales transactions
- **Update Sale**: Modify sale details (if not finalized)
- **Cancel Sale**: Mark sales as cancelled
- **List Sales**: Retrieve sales with filtering and pagination
- **Sale Details**: Get detailed sale information with line items

### 4. Reporting & Analytics
- **Sales Reports**: Generate sales reports by date range
- **Top Products**: Most sold products analysis
- **Customer Analytics**: Customer purchase patterns
- **Revenue Reports**: Daily, weekly, monthly revenue tracking

## Technical Requirements

### 1. Framework & Dependencies
- **Spring Boot 3.x**: Main framework
- **Spring Data JPA**: Database operations
- **Spring Web**: REST API endpoints
- **Spring Validation**: Input validation
- **H2 Database**: In-memory database for development
- **MySQL/PostgreSQL**: Production database support
- **Spring Boot DevTools**: Development utilities

### 2. Database Schema

#### Customers Table
- id (Primary Key, Auto-increment)
- name (VARCHAR, NOT NULL)
- email (VARCHAR, UNIQUE)
- phone (VARCHAR)
- address (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

#### Products Table
- id (Primary Key, Auto-increment)
- name (VARCHAR, NOT NULL)
- description (TEXT)
- price (DECIMAL)
- stock_quantity (INTEGER)
- category (VARCHAR)
- sku (VARCHAR, UNIQUE)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

#### Sales Table
- id (Primary Key, Auto-increment)
- customer_id (Foreign Key to Customers)
- sale_date (TIMESTAMP)
- total_amount (DECIMAL)
- status (ENUM: PENDING, COMPLETED, CANCELLED)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

#### Sale_Items Table
- id (Primary Key, Auto-increment)
- sale_id (Foreign Key to Sales)
- product_id (Foreign Key to Products)
- quantity (INTEGER)
- unit_price (DECIMAL)
- total_price (DECIMAL)

### 3. REST API Endpoints

#### Customer Endpoints
- `GET /api/customers` - List all customers (with pagination)
- `GET /api/customers/{id}` - Get customer by ID
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer
- `GET /api/customers/search?query={searchTerm}` - Search customers

#### Product Endpoints
- `GET /api/products` - List all products (with pagination)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `GET /api/products/search?query={searchTerm}` - Search products
- `PUT /api/products/{id}/stock` - Update stock quantity

#### Sales Endpoints
- `GET /api/sales` - List all sales (with pagination and filtering)
- `GET /api/sales/{id}` - Get sale by ID with details
- `POST /api/sales` - Create new sale
- `PUT /api/sales/{id}` - Update sale
- `DELETE /api/sales/{id}` - Cancel sale
- `GET /api/sales/customer/{customerId}` - Get sales by customer

#### Reporting Endpoints
- `GET /api/reports/sales?startDate={date}&endDate={date}` - Sales report
- `GET /api/reports/revenue?period={daily|weekly|monthly}` - Revenue report
- `GET /api/reports/top-products?limit={number}` - Top selling products
- `GET /api/reports/customer-analytics/{customerId}` - Customer analytics

### 4. Data Transfer Objects (DTOs)

#### CustomerDTO
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "address": "123 Main St",
  "createdAt": "2025-07-03T10:00:00",
  "updatedAt": "2025-07-03T10:00:00"
}
```

#### ProductDTO
```json
{
  "id": 1,
  "name": "Product Name",
  "description": "Product description",
  "price": 29.99,
  "stockQuantity": 100,
  "category": "Electronics",
  "sku": "PRD001",
  "createdAt": "2025-07-03T10:00:00",
  "updatedAt": "2025-07-03T10:00:00"
}
```

#### SaleDTO
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "saleDate": "2025-07-03T10:00:00",
  "totalAmount": 149.95,
  "status": "COMPLETED",
  "items": [
    {
      "productId": 1,
      "productName": "Product Name",
      "quantity": 2,
      "unitPrice": 29.99,
      "totalPrice": 59.98
    }
  ]
}
```

### 5. Validation Rules
- Customer email must be unique and valid format
- Product SKU must be unique
- Sale items must reference existing products
- Stock quantity cannot be negative
- Prices must be positive numbers

### 6. Error Handling
- Custom exception classes for business logic errors
- Global exception handler for consistent error responses
- Proper HTTP status codes
- Detailed error messages for validation failures

### 7. Cross-Origin Configuration
- CORS configuration to allow desktop app connections
- Configurable allowed origins for different environments

## Implementation Priority
1. Database setup and entity models
2. Customer management (CRUD operations)
3. Product management (CRUD operations)
4. Sales management (CRUD operations)
5. Reporting and analytics endpoints
6. Testing and documentation

## Testing Requirements
- Unit tests for service layer
- Integration tests for REST endpoints
- Test data setup for development
