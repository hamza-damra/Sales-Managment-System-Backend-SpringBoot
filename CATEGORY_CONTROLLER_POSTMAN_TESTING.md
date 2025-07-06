# Category Controller - Postman Testing Guide

## Base Configuration

**Base URL:** `http://localhost:8081/api/categories`

**Authentication:** All endpoints require JWT Bearer token
```
Authorization: Bearer <your_jwt_token>
```

**Content-Type:** `application/json` (for POST/PUT requests)

---

## 🔐 Prerequisites

### 1. Get JWT Token First
Before testing Category endpoints, you need to authenticate:

**Login Request:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

Copy the `accessToken` and use it in all Category API requests.

---

## 📂 Category Controller Endpoints

### 1. Get All Categories (Paginated)

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters (Optional):**
- `page=0` (default: 0)
- `size=10` (default: 10)
- `sortBy=displayOrder` (default: displayOrder) - Options: id, name, displayOrder, status, createdAt, updatedAt
- `sortDir=asc` (default: asc) - Options: asc, desc

**Example URLs:**
```
GET http://localhost:8081/api/categories
GET http://localhost:8081/api/categories?page=0&size=5
GET http://localhost:8081/api/categories?sortBy=name&sortDir=asc
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Electronics",
      "description": "Electronic devices and accessories",
      "displayOrder": 1,
      "status": "ACTIVE",
      "imageUrl": null,
      "icon": "electronics-icon",
      "colorCode": "#007bff",
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00",
      "productCount": 8
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
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### 2. Get All Active Categories

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/active`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "displayOrder": 1,
    "status": "ACTIVE",
    "imageUrl": null,
    "icon": "electronics-icon",
    "colorCode": "#007bff",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "productCount": 8
  }
]
```

### 3. Get Category by ID

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:**
```
GET http://localhost:8081/api/categories/1
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "displayOrder": 1,
  "status": "ACTIVE",
  "imageUrl": null,
  "icon": "electronics-icon",
  "colorCode": "#007bff",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "productCount": 8
}
```

### 4. Get Category by Name

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/name/{name}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:**
```
GET http://localhost:8081/api/categories/name/Electronics
```

### 5. Create New Category

**Method:** `POST`
**URL:** `http://localhost:8081/api/categories`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body (Minimal):**
```json
{
  "name": "Clothing",
  "description": "Apparel and fashion items"
}
```

**Request Body (Complete):**
```json
{
  "name": "Home & Garden",
  "description": "Home improvement and gardening supplies",
  "displayOrder": 3,
  "status": "ACTIVE",
  "imageUrl": "https://example.com/home-garden.jpg",
  "icon": "home-icon",
  "colorCode": "#28a745"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 3,
  "name": "Home & Garden",
  "description": "Home improvement and gardening supplies",
  "displayOrder": 3,
  "status": "ACTIVE",
  "imageUrl": "https://example.com/home-garden.jpg",
  "icon": "home-icon",
  "colorCode": "#28a745",
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00",
  "productCount": 0
}
```

### 6. Update Category

**Method:** `PUT`
**URL:** `http://localhost:8081/api/categories/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Example:**
```
PUT http://localhost:8081/api/categories/1
```

**Request Body:**
```json
{
  "name": "Electronics & Technology",
  "description": "Electronic devices, gadgets, and technology products",
  "displayOrder": 1,
  "status": "ACTIVE",
  "imageUrl": "https://example.com/electronics-updated.jpg",
  "icon": "tech-icon",
  "colorCode": "#0056b3"
}
```

### 7. Delete Category

**Method:** `DELETE`
**URL:** `http://localhost:8081/api/categories/{id}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:**
```
DELETE http://localhost:8081/api/categories/3
```

**Expected Response (204 No Content):** Empty body

**Note:** Categories with associated products cannot be deleted.

### 8. Search Categories

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/search`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters:**
- `query` (required) - Search term
- `page=0` (optional)
- `size=10` (optional)

**Example:**
```
GET http://localhost:8081/api/categories/search?query=electronics&page=0&size=5
```

### 9. Get Categories by Status

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/status/{status}`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Status Options:** ACTIVE, INACTIVE, ARCHIVED

**Example:**
```
GET http://localhost:8081/api/categories/status/ACTIVE
```

### 10. Get Empty Categories

**Method:** `GET`
**URL:** `http://localhost:8081/api/categories/empty`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Example:**
```
GET http://localhost:8081/api/categories/empty
```

### 11. Update Category Status

**Method:** `PUT`
**URL:** `http://localhost:8081/api/categories/{id}/status`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "INACTIVE"
}
```

**Example:**
```
PUT http://localhost:8081/api/categories/1/status
```

---

## 🧪 Testing Scenarios

### Scenario 1: Complete Category Management
1. **Create a new category** (POST /api/categories)
2. **Get all categories** (GET /api/categories)
3. **Get category by ID** (GET /api/categories/{id})
4. **Update category** (PUT /api/categories/{id})
5. **Search categories** (GET /api/categories/search)
6. **Update status** (PUT /api/categories/{id}/status)
7. **Delete category** (DELETE /api/categories/{id})

### Scenario 2: Category Status Management
1. **Create category with ACTIVE status**
2. **Change status to INACTIVE**
3. **Get categories by status**
4. **Change status back to ACTIVE**

### Scenario 3: Category Validation Testing
1. **Try to create category with duplicate name** (should fail)
2. **Try to create category with empty name** (should fail)
3. **Try to delete category with products** (should fail)
4. **Try to get non-existent category** (should return 404)

---

## 📝 Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T11:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Category name is required",
  "path": "/api/categories"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T11:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category not found with id: 999",
  "path": "/api/categories/999"
}
```

### 409 Conflict
```json
{
  "timestamp": "2024-01-15T11:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Category with name 'Electronics' already exists",
  "path": "/api/categories"
}
```
