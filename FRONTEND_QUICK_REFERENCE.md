# Sales Management System - Quick Reference Guide

## Base URL
```
http://localhost:8081/api
```

## Authentication Quick Start

### 1. Sign Up
```javascript
const signUp = async (userData) => {
  const response = await fetch('/api/auth/signup', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: userData.username,
      email: userData.email,
      password: userData.password,
      firstName: userData.firstName,
      lastName: userData.lastName,
      role: userData.role // 'USER', 'ADMIN', or 'MANAGER'
    })
  });
  return response.json();
};
```

### 2. Sign In
```javascript
const signIn = async (username, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  return response.json();
};
```

### 3. Authenticated Request Helper
```javascript
const apiCall = async (endpoint, options = {}) => {
  const token = localStorage.getItem('accessToken');
  
  const response = await fetch(`/api${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      ...options.headers
    }
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  return response.json();
};
```

## Common Operations

### Customers

```javascript
// Get all customers with pagination
const getCustomers = (page = 0, size = 10) => 
  apiCall(`/customers?page=${page}&size=${size}`);

// Get customer by ID
const getCustomer = (id) => apiCall(`/customers/${id}`);

// Create customer
const createCustomer = (customer) => 
  apiCall('/customers', {
    method: 'POST',
    body: JSON.stringify(customer)
  });

// Update customer
const updateCustomer = (id, customer) => 
  apiCall(`/customers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(customer)
  });

// Delete customer
const deleteCustomer = (id) => 
  apiCall(`/customers/${id}`, { method: 'DELETE' });

// Search customers
const searchCustomers = (query, page = 0) => 
  apiCall(`/customers/search?query=${encodeURIComponent(query)}&page=${page}`);
```

### Products

```javascript
// Get all products
const getProducts = (page = 0, size = 10, category = null) => {
  let url = `/products?page=${page}&size=${size}`;
  if (category) url += `&category=${encodeURIComponent(category)}`;
  return apiCall(url);
};

// Create product
const createProduct = (product) => 
  apiCall('/products', {
    method: 'POST',
    body: JSON.stringify(product)
  });

// Update product
const updateProduct = (id, product) => 
  apiCall(`/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(product)
  });

// Search products
const searchProducts = (query) => 
  apiCall(`/products/search?query=${encodeURIComponent(query)}`);
```

### Sales

```javascript
// Get all sales
const getSales = (page = 0, size = 10, filters = {}) => {
  let url = `/sales?page=${page}&size=${size}`;
  
  if (filters.status) url += `&status=${filters.status}`;
  if (filters.startDate) url += `&startDate=${filters.startDate}`;
  if (filters.endDate) url += `&endDate=${filters.endDate}`;
  
  return apiCall(url);
};

// Create sale
const createSale = (sale) => 
  apiCall('/sales', {
    method: 'POST',
    body: JSON.stringify(sale)
  });

// Get sales by customer
const getCustomerSales = (customerId, page = 0) => 
  apiCall(`/sales/customer/${customerId}?page=${page}`);
```

### Reports

```javascript
// Sales report
const getSalesReport = (startDate, endDate) => 
  apiCall(`/reports/sales?startDate=${startDate}&endDate=${endDate}`);

// Revenue trends
const getRevenueTrends = (months = 6) => 
  apiCall(`/reports/revenue?months=${months}`);

// Dashboard summary
const getDashboard = () => apiCall('/reports/dashboard');

// Top products
const getTopProducts = (startDate, endDate) => 
  apiCall(`/reports/top-products?startDate=${startDate}&endDate=${endDate}`);

// Customer analytics
const getCustomerAnalytics = () => apiCall('/reports/customer-analytics');

// Inventory report
const getInventoryReport = () => apiCall('/reports/inventory');
```

## Error Handling Pattern

```javascript
const handleApiCall = async (apiFunction) => {
  try {
    const result = await apiFunction();
    return { success: true, data: result };
  } catch (error) {
    console.error('API Error:', error);
    
    // Handle specific error types
    if (error.message.includes('401')) {
      // Token expired, redirect to login
      localStorage.removeItem('accessToken');
      window.location.href = '/login';
    }
    
    return { 
      success: false, 
      error: error.message || 'An unexpected error occurred' 
    };
  }
};

// Usage
const loadCustomers = async () => {
  const result = await handleApiCall(() => getCustomers(0, 10));
  
  if (result.success) {
    setCustomers(result.data.content);
  } else {
    setError(result.error);
  }
};
```

## React Hook Examples

### useApi Hook
```javascript
import { useState, useEffect } from 'react';

const useApi = (apiFunction, dependencies = []) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        const result = await apiFunction();
        setData(result);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, dependencies);

  return { data, loading, error, refetch: () => fetchData() };
};

// Usage
const CustomerList = () => {
  const { data: customers, loading, error } = useApi(() => getCustomers());

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      {customers?.content?.map(customer => (
        <div key={customer.id}>{customer.name}</div>
      ))}
    </div>
  );
};
```

### usePagination Hook
```javascript
const usePagination = (fetchFunction, initialSize = 10) => {
  const [data, setData] = useState([]);
  const [pagination, setPagination] = useState({
    page: 0,
    size: initialSize,
    totalPages: 0,
    totalElements: 0
  });
  const [loading, setLoading] = useState(false);

  const loadPage = async (page = 0) => {
    setLoading(true);
    try {
      const result = await fetchFunction(page, pagination.size);
      setData(result.content);
      setPagination({
        page: result.pageable.pageNumber,
        size: result.pageable.pageSize,
        totalPages: result.totalPages,
        totalElements: result.totalElements
      });
    } catch (error) {
      console.error('Pagination error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPage(0);
  }, []);

  return {
    data,
    pagination,
    loading,
    loadPage,
    nextPage: () => loadPage(pagination.page + 1),
    prevPage: () => loadPage(pagination.page - 1),
    canGoNext: pagination.page < pagination.totalPages - 1,
    canGoPrev: pagination.page > 0
  };
};
```

## Form Validation Examples

### Customer Form Validation
```javascript
const validateCustomer = (customer) => {
  const errors = {};

  if (!customer.name || customer.name.trim().length < 2) {
    errors.name = 'Name must be at least 2 characters long';
  }

  if (customer.email && !/\S+@\S+\.\S+/.test(customer.email)) {
    errors.email = 'Email format is invalid';
  }

  if (customer.phone && !/^[+]?[0-9]{10,15}$/.test(customer.phone)) {
    errors.phone = 'Phone number must be 10-15 digits';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

### Product Form Validation
```javascript
const validateProduct = (product) => {
  const errors = {};

  if (!product.name || product.name.trim().length < 2) {
    errors.name = 'Product name is required';
  }

  if (!product.price || product.price <= 0) {
    errors.price = 'Price must be greater than 0';
  }

  if (product.stockQuantity < 0) {
    errors.stockQuantity = 'Stock quantity cannot be negative';
  }

  if (product.sku && (product.sku.length < 3 || product.sku.length > 20)) {
    errors.sku = 'SKU must be between 3 and 20 characters';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

## Date Utilities

```javascript
// Format dates for API calls (ISO format)
const formatDateForApi = (date) => {
  return date instanceof Date ? date.toISOString() : date;
};

// Format dates for display
const formatDateForDisplay = (isoString) => {
  return new Date(isoString).toLocaleDateString();
};

const formatDateTimeForDisplay = (isoString) => {
  return new Date(isoString).toLocaleString();
};

// Get date range for reports
const getDateRange = (period) => {
  const endDate = new Date();
  const startDate = new Date();

  switch (period) {
    case 'today':
      startDate.setHours(0, 0, 0, 0);
      break;
    case 'week':
      startDate.setDate(endDate.getDate() - 7);
      break;
    case 'month':
      startDate.setMonth(endDate.getMonth() - 1);
      break;
    case 'quarter':
      startDate.setMonth(endDate.getMonth() - 3);
      break;
    case 'year':
      startDate.setFullYear(endDate.getFullYear() - 1);
      break;
  }

  return {
    startDate: formatDateForApi(startDate),
    endDate: formatDateForApi(endDate)
  };
};
```

## Environment Configuration

```javascript
// config.js
const config = {
  development: {
    API_BASE_URL: 'http://localhost:8081/api',
    ENABLE_LOGGING: true
  },
  production: {
    API_BASE_URL: 'https://your-production-api.com/api',
    ENABLE_LOGGING: false
  }
};

export default config[process.env.NODE_ENV || 'development'];
```

This quick reference provides the most commonly used patterns and functions for integrating with the Sales Management System API. For complete documentation, refer to the main API documentation file.
