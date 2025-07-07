# Reports and Analytics API Documentation

## Overview

This document provides comprehensive API documentation for the Reports and Analytics endpoints in the Sales Management System. These endpoints provide sales analytics, revenue reports, and business intelligence data.

**Base URL:** `http://localhost:8081/api/reports`

## Table of Contents

1. [Authentication](#authentication)
2. [Reports Endpoints](#reports-endpoints)
3. [Response Models](#response-models)
4. [Error Handling](#error-handling)
5. [Examples](#examples)

## Authentication

All endpoints require proper authentication. Include the authentication token in the request headers:

```http
Authorization: Bearer <your-token>
Content-Type: application/json
```

## Reports Endpoints

### 1. Sales Report

**Endpoint:** `GET /api/reports/sales`

**Description:** Generate comprehensive sales report for a specific date range.

**Query Parameters:**
- `startDate` (datetime, required) - Start date in ISO format (2025-07-06T00:00:00)
- `endDate` (datetime, required) - End date in ISO format (2025-07-06T23:59:59)

**Example Request:**
```http
GET /api/reports/sales?startDate=2025-07-01T00:00:00&endDate=2025-07-31T23:59:59
```

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-07-01T00:00:00",
    "endDate": "2025-07-31T23:59:59"
  },
  "summary": {
    "totalSales": 15,
    "totalRevenue": 15749.85,
    "averageOrderValue": 1049.99,
    "completedSales": 12,
    "pendingSales": 2,
    "cancelledSales": 1
  },
  "dailyBreakdown": [
    {
      "date": "2025-07-01",
      "salesCount": 3,
      "revenue": 2999.97,
      "averageOrderValue": 999.99
    }
  ],
  "topProducts": [
    {
      "productId": 1,
      "productName": "Smartphone",
      "quantitySold": 8,
      "revenue": 7999.92
    }
  ],
  "paymentMethods": [
    {
      "method": "CREDIT_CARD",
      "count": 8,
      "totalAmount": 8999.92,
      "percentage": 57.14
    }
  ]
}
```

### 2. Revenue Trends Report

**Endpoint:** `GET /api/reports/revenue`

**Description:** Generate revenue trends report for the specified number of months.

**Query Parameters:**
- `months` (int, default=6) - Number of months to include in the report

**Example Request:**
```http
GET /api/reports/revenue?months=12
```

**Response (200 OK):**
```json
{
  "period": {
    "months": 12,
    "startDate": "2024-07-01T00:00:00",
    "endDate": "2025-07-31T23:59:59"
  },
  "monthlyRevenue": [
    {
      "month": "2024-07",
      "revenue": 12500.00,
      "salesCount": 25,
      "averageOrderValue": 500.00,
      "growthPercentage": 15.5
    },
    {
      "month": "2024-08",
      "revenue": 14750.00,
      "salesCount": 30,
      "averageOrderValue": 491.67,
      "growthPercentage": 18.0
    }
  ],
  "totalRevenue": 156750.00,
  "averageMonthlyRevenue": 13062.50,
  "bestMonth": {
    "month": "2024-12",
    "revenue": 18900.00
  },
  "worstMonth": {
    "month": "2024-02",
    "revenue": 8500.00
  },
  "overallGrowthRate": 22.5
}
```

### 3. Top Selling Products Report

**Endpoint:** `GET /api/reports/top-products`

**Description:** Generate report of top-selling products for a specific date range.

**Query Parameters:**
- `startDate` (datetime, required) - Start date in ISO format
- `endDate` (datetime, required) - End date in ISO format

**Example Request:**
```http
GET /api/reports/top-products?startDate=2025-07-01T00:00:00&endDate=2025-07-31T23:59:59
```

**Response (200 OK):**
```json
{
  "period": {
    "startDate": "2025-07-01T00:00:00",
    "endDate": "2025-07-31T23:59:59"
  },
  "topSellingProducts": [
    {
      "productId": 1,
      "productName": "Smartphone",
      "productSku": "SP-001",
      "quantitySold": 25,
      "revenue": 24999.75,
      "averagePrice": 999.99,
      "profitMargin": 9999.90,
      "rank": 1
    },
    {
      "productId": 2,
      "productName": "Laptop",
      "productSku": "LP-001",
      "quantitySold": 15,
      "revenue": 22499.85,
      "averagePrice": 1499.99,
      "profitMargin": 7499.95,
      "rank": 2
    }
  ],
  "totalProductsSold": 150,
  "totalRevenue": 75999.50,
  "averageProductRevenue": 3799.98
}
```

### 4. Customer Report

**Endpoint:** `GET /api/reports/customers`

**Description:** Generate customer analytics report.

**Example Request:**
```http
GET /api/reports/customers
```

**Response (200 OK):**
```json
{
  "summary": {
    "totalCustomers": 150,
    "activeCustomers": 120,
    "newCustomersThisMonth": 15,
    "averageCustomerValue": 1250.00
  },
  "topCustomers": [
    {
      "customerId": 1,
      "customerName": "أحمد محمد",
      "totalPurchases": 12,
      "totalSpent": 15999.88,
      "averageOrderValue": 1333.32,
      "lastPurchaseDate": "2025-07-05T14:30:00"
    }
  ],
  "customerSegments": [
    {
      "segment": "VIP",
      "customerCount": 25,
      "totalRevenue": 125000.00,
      "averageSpent": 5000.00
    },
    {
      "segment": "Regular",
      "customerCount": 95,
      "totalRevenue": 95000.00,
      "averageSpent": 1000.00
    }
  ],
  "acquisitionTrends": [
    {
      "month": "2025-07",
      "newCustomers": 15,
      "retentionRate": 85.5
    }
  ]
}
```

### 5. Inventory Report

**Endpoint:** `GET /api/reports/inventory`

**Description:** Generate inventory status and analytics report.

**Example Request:**
```http
GET /api/reports/inventory
```

**Response (200 OK):**
```json
{
  "summary": {
    "totalProducts": 250,
    "totalStockValue": 125000.00,
    "lowStockItems": 15,
    "outOfStockItems": 3,
    "averageStockLevel": 45.5
  },
  "lowStockAlerts": [
    {
      "productId": 5,
      "productName": "Wireless Headphones",
      "currentStock": 2,
      "minimumStock": 10,
      "reorderLevel": 20
    }
  ],
  "topMovingProducts": [
    {
      "productId": 1,
      "productName": "Smartphone",
      "stockTurnover": 8.5,
      "averageDaysToSell": 42
    }
  ],
  "categoryBreakdown": [
    {
      "categoryId": 1,
      "categoryName": "Electronics",
      "productCount": 85,
      "stockValue": 85000.00,
      "averageStockLevel": 35.2
    }
  ]
}
```

### 6. Dashboard Summary

**Endpoint:** `GET /api/reports/dashboard`

**Description:** Generate comprehensive dashboard summary with key metrics.

**Example Request:**
```http
GET /api/reports/dashboard
```

**Response (200 OK):**
```json
{
  "period": "Last 30 days",
  "salesMetrics": {
    "totalSales": 45,
    "totalRevenue": 67499.55,
    "averageOrderValue": 1499.99,
    "growthRate": 15.5
  },
  "customerMetrics": {
    "totalCustomers": 150,
    "newCustomers": 12,
    "activeCustomers": 89,
    "customerRetentionRate": 85.5
  },
  "inventoryMetrics": {
    "totalProducts": 250,
    "lowStockItems": 15,
    "outOfStockItems": 3,
    "stockValue": 125000.00
  },
  "recentActivity": [
    {
      "type": "SALE_COMPLETED",
      "description": "Sale #SALE-2025-000045 completed",
      "timestamp": "2025-07-06T14:30:00",
      "amount": 1999.99
    },
    {
      "type": "LOW_STOCK_ALERT",
      "description": "Wireless Headphones stock is low (2 remaining)",
      "timestamp": "2025-07-06T13:15:00",
      "productId": 5
    }
  ],
  "alerts": [
    {
      "type": "LOW_STOCK",
      "message": "15 products are running low on stock",
      "severity": "WARNING",
      "count": 15
    },
    {
      "type": "OUT_OF_STOCK",
      "message": "3 products are out of stock",
      "severity": "CRITICAL",
      "count": 3
    }
  ]
}
```

### 7. Performance Metrics

**Endpoint:** `GET /api/reports/performance`

**Description:** Generate business performance metrics report.

**Query Parameters:**
- `period` (string, default="month") - Period for metrics (day, week, month, quarter, year)

**Example Request:**
```http
GET /api/reports/performance?period=month
```

**Response (200 OK):**
```json
{
  "period": "month",
  "metrics": {
    "salesGrowth": 15.5,
    "revenueGrowth": 18.2,
    "customerGrowth": 8.5,
    "averageOrderValueGrowth": 5.2,
    "inventoryTurnover": 6.8,
    "profitMargin": 35.5
  },
  "comparisons": {
    "previousPeriod": {
      "salesGrowth": 12.3,
      "revenueGrowth": 14.8,
      "customerGrowth": 6.2
    },
    "yearOverYear": {
      "salesGrowth": 25.8,
      "revenueGrowth": 28.5,
      "customerGrowth": 15.2
    }
  },
  "targets": {
    "salesTarget": 50,
    "salesAchieved": 45,
    "salesTargetPercentage": 90.0,
    "revenueTarget": 75000.00,
    "revenueAchieved": 67499.55,
    "revenueTargetPercentage": 89.99
  }
}
```

## Response Models

### Sales Report Response

```typescript
interface SalesReportResponse {
  period: {
    startDate: string;
    endDate: string;
  };
  summary: {
    totalSales: number;
    totalRevenue: number;
    averageOrderValue: number;
    completedSales: number;
    pendingSales: number;
    cancelledSales: number;
  };
  dailyBreakdown: Array<{
    date: string;
    salesCount: number;
    revenue: number;
    averageOrderValue: number;
  }>;
  topProducts: Array<{
    productId: number;
    productName: string;
    quantitySold: number;
    revenue: number;
  }>;
  paymentMethods: Array<{
    method: string;
    count: number;
    totalAmount: number;
    percentage: number;
  }>;
}
```

### Revenue Trends Response

```typescript
interface RevenueTrendsResponse {
  period: {
    months: number;
    startDate: string;
    endDate: string;
  };
  monthlyRevenue: Array<{
    month: string;
    revenue: number;
    salesCount: number;
    averageOrderValue: number;
    growthPercentage: number;
  }>;
  totalRevenue: number;
  averageMonthlyRevenue: number;
  bestMonth: {
    month: string;
    revenue: number;
  };
  worstMonth: {
    month: string;
    revenue: number;
  };
  overallGrowthRate: number;
}
```

### Dashboard Summary Response

```typescript
interface DashboardSummaryResponse {
  period: string;
  salesMetrics: {
    totalSales: number;
    totalRevenue: number;
    averageOrderValue: number;
    growthRate: number;
  };
  customerMetrics: {
    totalCustomers: number;
    newCustomers: number;
    activeCustomers: number;
    customerRetentionRate: number;
  };
  inventoryMetrics: {
    totalProducts: number;
    lowStockItems: number;
    outOfStockItems: number;
    stockValue: number;
  };
  recentActivity: Array<{
    type: string;
    description: string;
    timestamp: string;
    amount?: number;
    productId?: number;
  }>;
  alerts: Array<{
    type: string;
    message: string;
    severity: "INFO" | "WARNING" | "CRITICAL";
    count: number;
  }>;
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
    "path": "/api/reports/sales"
  }
}
```

### Common Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| `INVALID_DATE_RANGE` | Start date is after end date | 400 |
| `DATE_RANGE_TOO_LARGE` | Date range exceeds maximum allowed | 400 |
| `MISSING_REQUIRED_PARAMETER` | Required parameter is missing | 400 |
| `INVALID_PERIOD_FORMAT` | Invalid period format | 400 |
| `REPORT_GENERATION_FAILED` | Report generation failed | 500 |
| `INSUFFICIENT_DATA` | Not enough data for report | 422 |

## Examples

### Frontend Integration Examples

#### JavaScript/TypeScript Example

```typescript
// Get sales report
const getSalesReport = async (startDate: string, endDate: string): Promise<SalesReportResponse> => {
  const params = new URLSearchParams({
    startDate,
    endDate
  });

  const response = await fetch(`/api/reports/sales?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.error.message);
  }

  return response.json();
};

// Get revenue trends
const getRevenueTrends = async (months = 6): Promise<RevenueTrendsResponse> => {
  const response = await fetch(`/api/reports/revenue?months=${months}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
};

// Get dashboard summary
const getDashboardSummary = async (): Promise<DashboardSummaryResponse> => {
  const response = await fetch('/api/reports/dashboard', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
};

// Get top products report
const getTopProductsReport = async (startDate: string, endDate: string) => {
  const params = new URLSearchParams({
    startDate,
    endDate
  });

  const response = await fetch(`/api/reports/top-products?${params}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  return response.json();
};
```

#### React Hook Example

```typescript
import { useState, useEffect } from 'react';

const useDashboardData = () => {
  const [dashboardData, setDashboardData] = useState<DashboardSummaryResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const data = await getDashboardSummary();
        setDashboardData(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  return { dashboardData, loading, error };
};

// Sales report hook with date range
const useSalesReport = (startDate: string, endDate: string) => {
  const [reportData, setReportData] = useState<SalesReportResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!startDate || !endDate) return;

    const fetchReport = async () => {
      try {
        setLoading(true);
        const data = await getSalesReport(startDate, endDate);
        setReportData(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchReport();
  }, [startDate, endDate]);

  return { reportData, loading, error };
};

// Revenue trends hook
const useRevenueTrends = (months = 6) => {
  const [trendsData, setTrendsData] = useState<RevenueTrendsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTrends = async () => {
      try {
        setLoading(true);
        const data = await getRevenueTrends(months);
        setTrendsData(data);
        setError(null);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error');
      } finally {
        setLoading(false);
      }
    };

    fetchTrends();
  }, [months]);

  return { trendsData, loading, error };
};
```

#### Chart Integration Example

```typescript
// Example for Chart.js integration
const prepareChartData = (reportData: SalesReportResponse) => {
  return {
    labels: reportData.dailyBreakdown.map(day => day.date),
    datasets: [
      {
        label: 'Daily Revenue',
        data: reportData.dailyBreakdown.map(day => day.revenue),
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1
      },
      {
        label: 'Daily Sales Count',
        data: reportData.dailyBreakdown.map(day => day.salesCount),
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        yAxisID: 'y1'
      }
    ]
  };
};

// Revenue trends chart data
const prepareRevenueTrendsChart = (trendsData: RevenueTrendsResponse) => {
  return {
    labels: trendsData.monthlyRevenue.map(month => month.month),
    datasets: [
      {
        label: 'Monthly Revenue',
        data: trendsData.monthlyRevenue.map(month => month.revenue),
        borderColor: 'rgb(54, 162, 235)',
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        fill: true
      }
    ]
  };
};
```

### Date Range Utilities

```typescript
// Utility functions for date handling
const getDateRange = (period: 'today' | 'week' | 'month' | 'quarter' | 'year') => {
  const now = new Date();
  const startDate = new Date();

  switch (period) {
    case 'today':
      startDate.setHours(0, 0, 0, 0);
      break;
    case 'week':
      startDate.setDate(now.getDate() - 7);
      break;
    case 'month':
      startDate.setMonth(now.getMonth() - 1);
      break;
    case 'quarter':
      startDate.setMonth(now.getMonth() - 3);
      break;
    case 'year':
      startDate.setFullYear(now.getFullYear() - 1);
      break;
  }

  return {
    startDate: startDate.toISOString(),
    endDate: now.toISOString()
  };
};

// Format date for API
const formatDateForAPI = (date: Date): string => {
  return date.toISOString().split('.')[0]; // Remove milliseconds
};

// Parse API date response
const parseAPIDate = (dateString: string): Date => {
  return new Date(dateString);
};
```

## Best Practices

1. **Cache report data** when possible to improve performance
2. **Use appropriate date ranges** - avoid overly large ranges that may timeout
3. **Implement loading states** for better user experience
4. **Handle errors gracefully** with user-friendly messages
5. **Use charts and visualizations** to make data more digestible
6. **Implement real-time updates** for dashboard metrics when needed
7. **Optimize API calls** by combining related data requests
8. **Validate date ranges** on the frontend before API calls
9. **Use pagination** for large datasets in detailed reports
10. **Implement export functionality** for reports (PDF, Excel, CSV)

## Performance Considerations

- Reports with large date ranges may take longer to generate
- Consider implementing caching for frequently requested reports
- Use pagination for detailed reports with many records
- Implement progressive loading for dashboard components
- Consider using WebSockets for real-time dashboard updates
- Optimize database queries for better report generation performance
```
