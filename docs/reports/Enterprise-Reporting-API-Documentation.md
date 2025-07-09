# Enterprise Reporting API Documentation

## Overview

The Enterprise Reporting API provides comprehensive business intelligence and analytics capabilities for the Sales Management System. This production-ready reporting module offers detailed insights across all business entities with advanced features including caching, export functionality, role-based access control, and real-time KPIs.

## Key Features

- **Comprehensive Entity Coverage**: Reports for Sales, Customers, Products, Inventory, Promotions, Financial data, and Users
- **Advanced Analytics**: Trend analysis, forecasting, customer segmentation, and profitability analysis
- **Multiple Export Formats**: PDF, Excel, CSV, and JSON exports with professional templates
- **Performance Optimization**: Redis-based caching, pagination, and asynchronous processing
- **Enterprise Security**: Role-based access control with audit trails
- **Real-time Dashboards**: Executive and operational dashboards with live KPIs

## API Endpoints

### Base URL
```
/api/v1/reports
```

## Sales Reports

### 1. Comprehensive Sales Report
**Endpoint:** `GET /api/v1/reports/sales/comprehensive`

**Description:** Generate detailed sales analytics with trends, customer insights, and product performance

**Security:** Requires `ADMIN`, `MANAGER`, or `SALES_ANALYST` role

**Parameters:**
- `startDate` (required): Start date in ISO format (yyyy-MM-ddTHH:mm:ss)
- `endDate` (required): End date in ISO format
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20, max: 100)
- `sortBy` (optional): Sort field (default: createdAt)
- `sortDirection` (optional): Sort direction (ASC/DESC, default: DESC)

**Response:**
```json
{
  "success": true,
  "message": "Report generated successfully",
  "data": {
    "summary": {
      "totalSales": 1250,
      "totalRevenue": 125000.00,
      "averageOrderValue": 100.00,
      "totalDiscounts": 5000.00,
      "totalTax": 12500.00,
      "netRevenue": 120000.00,
      "conversionRate": 85.5,
      "uniqueCustomers": 450,
      "revenueGrowth": 12.5,
      "salesGrowth": 8.3
    },
    "dailyBreakdown": [
      {
        "date": "2024-01-15",
        "salesCount": 45,
        "revenue": 4500.00,
        "averageOrderValue": 100.00,
        "uniqueCustomers": 38,
        "discountAmount": 225.00
      }
    ],
    "topCustomers": [
      {
        "customerId": 123,
        "customerName": "John Doe",
        "customerEmail": "john@example.com",
        "totalOrders": 15,
        "totalSpent": 5000.00,
        "averageOrderValue": 333.33,
        "lastPurchase": "2024-01-15T10:30:00",
        "customerSegment": "VIP"
      }
    ],
    "topProducts": [
      {
        "productId": 456,
        "productName": "Premium Widget",
        "category": "Electronics",
        "quantitySold": 150,
        "revenue": 15000.00,
        "averagePrice": 100.00,
        "profitMargin": 25.5,
        "uniqueCustomers": 75
      }
    ],
    "trends": [
      {
        "period": "2024-01",
        "revenue": 25000.00,
        "salesCount": 250,
        "growthRate": 15.2,
        "trendDirection": "Growing"
      }
    ],
    "paymentAnalysis": {
      "countByMethod": {
        "CREDIT_CARD": 800,
        "CASH": 300,
        "BANK_TRANSFER": 150
      },
      "revenueByMethod": {
        "CREDIT_CARD": 80000.00,
        "CASH": 30000.00,
        "BANK_TRANSFER": 15000.00
      },
      "mostPopularMethod": "CREDIT_CARD",
      "highestRevenueMethod": "CREDIT_CARD"
    }
  },
  "metadata": {
    "reportType": "SALES_COMPREHENSIVE",
    "reportName": "Comprehensive Sales Analytics",
    "generatedAt": "2024-01-15T14:30:00",
    "period": {
      "startDate": "2024-01-01T00:00:00",
      "endDate": "2024-01-15T23:59:59",
      "description": "Custom date range",
      "daysIncluded": 15
    },
    "executionTimeMs": 1250,
    "version": "1.0"
  }
}
```

### 2. Sales Summary Report
**Endpoint:** `GET /api/v1/reports/sales/summary`

**Description:** Quick sales overview with key metrics and caching support

**Security:** Requires `ADMIN`, `MANAGER`, or `SALES_ANALYST` role

**Parameters:**
- `startDate` (required): Start date in ISO format
- `endDate` (required): End date in ISO format
- `useCache` (optional): Enable caching (default: false)

### 3. Sales Trends Analysis
**Endpoint:** `GET /api/v1/reports/sales/trends`

**Description:** Historical sales trends with forecasting capabilities

**Security:** Requires `ADMIN`, `MANAGER`, or `SALES_ANALYST` role

**Parameters:**
- `months` (optional): Number of months to analyze (1-60, default: 12)
- `groupBy` (optional): Grouping method (DAY/WEEK/MONTH, default: MONTH)

## Customer Reports

### 1. Customer Analytics
**Endpoint:** `GET /api/v1/reports/customers/analytics`

**Description:** Comprehensive customer behavior analysis and segmentation

**Security:** Requires `ADMIN`, `MANAGER`, or `CUSTOMER_ANALYST` role

**Parameters:**
- `includeInactive` (optional): Include inactive customers (default: false)
- `months` (optional): Analysis period in months (1-60, default: 12)

### 2. Customer Lifetime Value
**Endpoint:** `GET /api/v1/reports/customers/lifetime-value`

**Description:** Customer lifetime value analysis with pagination

**Security:** Requires `ADMIN`, `MANAGER`, or `CUSTOMER_ANALYST` role

**Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (1-100, default: 20)
- `sortBy` (optional): Sort field (default: totalValue)

### 3. Customer Retention Analysis
**Endpoint:** `GET /api/v1/reports/customers/retention`

**Description:** Customer retention metrics and cohort analysis

**Security:** Requires `ADMIN`, `MANAGER`, or `CUSTOMER_ANALYST` role

**Parameters:**
- `months` (optional): Analysis period in months (1-36, default: 12)

## Product Reports

### 1. Product Performance
**Endpoint:** `GET /api/v1/reports/products/performance`

**Description:** Detailed product sales performance and profitability analysis

**Security:** Requires `ADMIN`, `MANAGER`, or `PRODUCT_ANALYST` role

**Parameters:**
- `startDate` (required): Start date in ISO format
- `endDate` (required): End date in ISO format
- `categoryIds` (optional): Filter by category IDs
- `productIds` (optional): Filter by specific product IDs

### 2. Inventory Turnover
**Endpoint:** `GET /api/v1/reports/products/inventory-turnover`

**Description:** Product inventory turnover analysis and optimization recommendations

**Security:** Requires `ADMIN`, `MANAGER`, or `INVENTORY_ANALYST` role

**Parameters:**
- `months` (optional): Analysis period in months (1-24, default: 12)
- `categoryIds` (optional): Filter by category IDs

## Inventory Reports

### 1. Inventory Status
**Endpoint:** `GET /api/v1/reports/inventory/status`

**Description:** Current inventory levels, stock alerts, and valuation

**Security:** Requires `ADMIN`, `MANAGER`, or `INVENTORY_ANALYST` role

**Parameters:**
- `includeInactive` (optional): Include inactive products (default: false)
- `warehouseIds` (optional): Filter by warehouse IDs

### 2. Inventory Valuation
**Endpoint:** `GET /api/v1/reports/inventory/valuation`

**Description:** Inventory valuation by cost and market value

**Security:** Requires `ADMIN`, `MANAGER`, or `FINANCIAL_ANALYST` role

**Parameters:**
- `valuationMethod` (optional): Valuation method (FIFO/LIFO/AVERAGE, default: FIFO)
- `categoryIds` (optional): Filter by category IDs

## Promotion Reports

### 1. Promotion Effectiveness
**Endpoint:** `GET /api/v1/reports/promotions/effectiveness`

**Description:** Promotion performance analysis and ROI calculation

**Security:** Requires `ADMIN`, `MANAGER`, or `MARKETING_ANALYST` role

**Parameters:**
- `startDate` (required): Start date in ISO format
- `endDate` (required): End date in ISO format

### 2. Promotion Usage Statistics
**Endpoint:** `GET /api/v1/reports/promotions/usage`

**Description:** Detailed promotion usage patterns and customer behavior

**Security:** Requires `ADMIN`, `MANAGER`, or `MARKETING_ANALYST` role

**Parameters:**
- `promotionIds` (optional): Filter by specific promotion IDs
- `days` (optional): Analysis period in days (1-365, default: 30)

## Financial Reports

### 1. Financial Revenue Report
**Endpoint:** `GET /api/v1/reports/financial/revenue`

**Description:** Comprehensive revenue analysis with profit margins

**Security:** Requires `ADMIN`, `MANAGER`, or `FINANCIAL_ANALYST` role

**Parameters:**
- `startDate` (required): Start date in ISO format
- `endDate` (required): End date in ISO format

## Export Functionality

### 1. Export Report
**Endpoint:** `POST /api/v1/reports/export`

**Description:** Export any report in specified format (PDF, Excel, CSV)

**Security:** Requires `ADMIN` or `MANAGER` role

**Request Body:**
```json
{
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "exportFormat": "PDF",
  "reportType": "SALES_COMPREHENSIVE",
  "filters": {
    "customerIds": [1, 2, 3],
    "categoryIds": [10, 20]
  }
}
```

### 2. Async Export
**Endpoint:** `GET /api/v1/reports/export/async/{reportType}`

**Description:** Start asynchronous report generation for large datasets

**Security:** Requires `ADMIN` or `MANAGER` role

## Dashboard Reports

### 1. Executive Dashboard
**Endpoint:** `GET /api/v1/reports/dashboard/executive`

**Description:** High-level KPIs and metrics for executive overview

**Security:** Requires `ADMIN` or `EXECUTIVE` role

**Parameters:**
- `days` (optional): Analysis period in days (1-365, default: 30)

### 2. Operational Dashboard
**Endpoint:** `GET /api/v1/reports/dashboard/operational`

**Description:** Operational metrics for day-to-day management

**Security:** Requires `ADMIN`, `MANAGER`, or `OPERATIONS` role

### 3. Real-time KPIs
**Endpoint:** `GET /api/v1/reports/kpi/real-time`

**Description:** Real-time key performance indicators

**Security:** Requires `ADMIN` or `MANAGER` role

## Error Handling

All endpoints return standardized error responses:

```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "VALIDATION_ERROR",
  "errorDetails": "Detailed error information"
}
```

Common error codes:
- `VALIDATION_ERROR`: Invalid request parameters
- `UNAUTHORIZED`: Insufficient permissions
- `REPORT_GENERATION_FAILED`: Error during report generation
- `EXPORT_FAILED`: Error during report export
- `CACHE_ERROR`: Caching service error

## Performance Considerations

- **Caching**: Use `useCache=true` parameter for frequently accessed reports
- **Pagination**: Always use pagination for large datasets
- **Async Processing**: Use async export for reports with large data volumes
- **Date Ranges**: Limit date ranges for better performance
- **Filtering**: Apply filters to reduce data processing overhead

## Security and Audit

- All report access is logged with user information and timestamps
- Role-based access control ensures data security
- Sensitive data is masked based on user permissions
- Export activities are tracked for compliance

## Rate Limiting

- Standard reports: 100 requests per minute per user
- Export operations: 10 requests per minute per user
- Real-time KPIs: 60 requests per minute per user
