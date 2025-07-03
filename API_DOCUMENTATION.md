# Sales Management Backend API Documentation

This document provides an overview of the available RESTful API endpoints for the Sales Management Backend application. Frontend developers can use these endpoints to perform CRUD operations and retrieve reports.

---

## Base URL

```
http://<host>:<port>/api
```

Replace `<host>` and `<port>` with your backend server address and port (default Spring Boot port is 8080).

---

## Customers

### Get All Customers

- **Endpoint:** `GET /customers`
- **Query Parameters:**
  - `page` (int, default=0)
  - `size` (int, default=10)
  - `sortBy` (string, default="id")
  - `sortDir` (string, "asc" or "desc", default="asc")

**Response:**
```json
{
  "content": [ { /* CustomerDTO */ } ],
  "pageable": { /* pagination info */ },
  "totalElements": 123,
  "totalPages": 13
}
```

### Get Customer by ID

- **Endpoint:** `GET /customers/{id}`
- **Path Parameter:** `id` (long)

**Response:**
```json
{ /* CustomerDTO */ }
```

### Create Customer

- **Endpoint:** `POST /customers`
- **Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  /* other fields */
}
```

**Response (201 Created):**
```json
{ /* Created CustomerDTO */ }
```

### Update Customer

- **Endpoint:** `PUT /customers/{id}`
- **Path Parameter:** `id` (long)
- **Body:** same as Create

**Response:**
```json
{ /* Updated CustomerDTO */ }
```

### Delete Customer

- **Endpoint:** `DELETE /customers/{id}`
- **Path Parameter:** `id` (long)

**Response:** `204 No Content`

### Search Customers

- **Endpoint:** `GET /customers/search`
- **Query Parameters:**
  - `query` (string)
  - `page`, `size` (pagination)

**Response:** Paged CustomerDTO list

---

## Products

### Get All Products

- **Endpoint:** `GET /products`
- **Query Parameters:**
  - `page`, `size`, `sortBy`, `sortDir`
  - `category` (optional)

**Response:** Paged ProductDTO list

### Get Product by ID

- **Endpoint:** `GET /products/{id}`

**Response:** ProductDTO

### Create Product

- **Endpoint:** `POST /products`
- **Body:** ProductDTO fields

**Response (201):** Created ProductDTO

### Update Product

- **Endpoint:** `PUT /products/{id}`

**Response:** Updated ProductDTO

### Delete Product

- **Endpoint:** `DELETE /products/{id}`

**Response:** `204 No Content`

### Search Products

- **Endpoint:** `GET /products/search`
- **Query Parameters:** `query`, `page`, `size`

### Stock Management

- **Update Stock Directly**  
  - `PUT /products/{id}/stock`  
  - Body: `{ "stockQuantity": 100 }`

- **Increase Stock**  
  - `POST /products/{id}/stock/increase`  
  - Body: `{ "quantity": 10 }`

- **Reduce Stock**  
  - `POST /products/{id}/stock/reduce`  
  - Body: `{ "quantity": 5 }`

Each returns the updated ProductDTO

---

## Sales

### Get All Sales

- **Endpoint:** `GET /sales`
- **Query Parameters:**
  - `page`, `size`, `sortBy`, `sortDir`
  - `status` (enum)
  - `startDate`, `endDate` (ISO date-time)

**Response:** Paged SaleDTO list

### Get Sale by ID

- **Endpoint:** `GET /sales/{id}`

**Response:** SaleDTO

### Get Sales by Customer

- **Endpoint:** `GET /sales/customer/{customerId}`
- **Query Parameters:** `page`, `size`

**Response:** Paged SaleDTO list

### Create Sale

- **Endpoint:** `POST /sales`
- **Body:** SaleDTO with items

**Response (201):** Created SaleDTO

### Update Sale

- **Endpoint:** `PUT /sales/{id}`

**Response:** Updated SaleDTO

### Delete Sale

- **Endpoint:** `DELETE /sales/{id}`

**Response:** `204 No Content`

### Complete Sale

- **Endpoint:** `POST /sales/{id}/complete`

### Cancel Sale

- **Endpoint:** `POST /sales/{id}/cancel`

Both return the updated SaleDTO

---

## Reports

All report endpoints return JSON objects or maps.

### Sales Report

- **Endpoint:** `GET /reports/sales`
- **Query Parameters:** `startDate`, `endDate` (ISO date-time)

### Revenue Trends

- **Endpoint:** `GET /reports/revenue`
- **Query Parameter:** `months` (int, default=6)

### Top Selling Products

- **Endpoint:** `GET /reports/top-products`
- **Parameters:** `startDate`, `endDate`

### Customer Analytics

- **Endpoint:** `GET /reports/customer-analytics`

### Inventory Report

- **Endpoint:** `GET /reports/inventory`

### Dashboard Summary

- **Endpoint:** `GET /reports/dashboard`

---

## Error Handling

- `400 Bad Request` for validation failures or invalid IDs.
- `404 Not Found` when a resource is not found.

---

_For more information, refer to the API source code or contact the backend team._

