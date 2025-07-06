# Category Controller Postman Testing Guide

## Overview

This guide provides comprehensive Postman testing examples for the Category Controller endpoints in the Sales Management System Backend.

## Base Configuration

### Environment Variables
```
{{baseUrl}} = http://localhost:8080
{{categoryId}} = 1
{{authToken}} = your_jwt_token_here
```

### Headers (Global)
```
Content-Type: application/json
Authorization: Bearer {{authToken}}
```

## Test Collection: Category Management

### 1. Create Category

**Endpoint:** `POST {{baseUrl}}/api/categories`

**Request Body:**
```json
{
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "displayOrder": 1,
    "imageUrl": "https://example.com/electronics.jpg",
    "icon": "electronics-icon",
    "colorCode": "#007bff"
}
```

**Expected Response:** `201 Created`
```json
{
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "displayOrder": 1,
    "status": "ACTIVE",
    "imageUrl": "https://example.com/electronics.jpg",
    "icon": "electronics-icon",
    "colorCode": "#007bff",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "productCount": 0
}
```

**Tests:**
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has category ID", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.be.a('number');
    pm.environment.set("categoryId", responseJson.id);
});

pm.test("Category name matches request", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.name).to.eql("Electronics");
});
```

### 2. Get All Categories (Paginated)

**Endpoint:** `GET {{baseUrl}}/api/categories?page=0&size=10&sortBy=displayOrder&sortDir=asc`

**Expected Response:** `200 OK`
```json
{
    "content": [
        {
            "id": 1,
            "name": "Electronics",
            "description": "Electronic devices and accessories",
            "displayOrder": 1,
            "status": "ACTIVE",
            "productCount": 0
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "sorted": true,
            "unsorted": false
        }
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has pagination info", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('content');
    pm.expect(responseJson).to.have.property('totalElements');
    pm.expect(responseJson).to.have.property('totalPages');
});
```

### 3. Get All Active Categories

**Endpoint:** `GET {{baseUrl}}/api/categories/active`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices and accessories",
        "displayOrder": 1,
        "status": "ACTIVE",
        "productCount": 0
    }
]
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All categories are active", function () {
    const responseJson = pm.response.json();
    responseJson.forEach(category => {
        pm.expect(category.status).to.eql("ACTIVE");
    });
});
```

### 4. Get Category by ID

**Endpoint:** `GET {{baseUrl}}/api/categories/{{categoryId}}`

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "displayOrder": 1,
    "status": "ACTIVE",
    "imageUrl": "https://example.com/electronics.jpg",
    "icon": "electronics-icon",
    "colorCode": "#007bff",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00",
    "productCount": 0
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Category ID matches request", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.id).to.eql(parseInt(pm.environment.get("categoryId")));
});
```

### 5. Get Category by Name

**Endpoint:** `GET {{baseUrl}}/api/categories/name/Electronics`

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "status": "ACTIVE",
    "productCount": 0
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Category name matches request", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.name).to.eql("Electronics");
});
```

### 6. Update Category

**Endpoint:** `PUT {{baseUrl}}/api/categories/{{categoryId}}`

**Request Body:**
```json
{
    "name": "Electronics & Gadgets",
    "description": "Electronic devices, gadgets, and accessories",
    "displayOrder": 1,
    "imageUrl": "https://example.com/electronics-updated.jpg",
    "icon": "electronics-icon",
    "colorCode": "#0056b3"
}
```

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "name": "Electronics & Gadgets",
    "description": "Electronic devices, gadgets, and accessories",
    "displayOrder": 1,
    "status": "ACTIVE",
    "imageUrl": "https://example.com/electronics-updated.jpg",
    "icon": "electronics-icon",
    "colorCode": "#0056b3",
    "updatedAt": "2024-01-15T11:30:00",
    "productCount": 0
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Category name updated", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.name).to.eql("Electronics & Gadgets");
});

pm.test("Updated timestamp changed", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.updatedAt).to.not.be.null;
});
```

### 7. Search Categories

**Endpoint:** `GET {{baseUrl}}/api/categories/search?q=electronic&page=0&size=10`

**Expected Response:** `200 OK`
```json
{
    "content": [
        {
            "id": 1,
            "name": "Electronics & Gadgets",
            "description": "Electronic devices, gadgets, and accessories",
            "status": "ACTIVE",
            "productCount": 0
        }
    ],
    "totalElements": 1,
    "totalPages": 1
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Search results contain search term", function () {
    const responseJson = pm.response.json();
    const searchTerm = "electronic";
    responseJson.content.forEach(category => {
        const nameMatch = category.name.toLowerCase().includes(searchTerm);
        const descMatch = category.description.toLowerCase().includes(searchTerm);
        pm.expect(nameMatch || descMatch).to.be.true;
    });
});
```

### 8. Get Categories by Status

**Endpoint:** `GET {{baseUrl}}/api/categories/status/ACTIVE`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "name": "Electronics & Gadgets",
        "status": "ACTIVE",
        "productCount": 0
    }
]
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All categories have requested status", function () {
    const responseJson = pm.response.json();
    responseJson.forEach(category => {
        pm.expect(category.status).to.eql("ACTIVE");
    });
});
```

### 9. Update Category Status

**Endpoint:** `PUT {{baseUrl}}/api/categories/{{categoryId}}/status`

**Request Body:**
```json
{
    "status": "INACTIVE"
}
```

**Expected Response:** `200 OK`
```json
{
    "id": 1,
    "name": "Electronics & Gadgets",
    "status": "INACTIVE",
    "updatedAt": "2024-01-15T12:00:00"
}
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Status updated correctly", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.status).to.eql("INACTIVE");
});
```

### 10. Get Empty Categories

**Endpoint:** `GET {{baseUrl}}/api/categories/empty`

**Expected Response:** `200 OK`
```json
[
    {
        "id": 1,
        "name": "Electronics & Gadgets",
        "status": "INACTIVE",
        "productCount": 0
    }
]
```

**Tests:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("All categories have zero products", function () {
    const responseJson = pm.response.json();
    responseJson.forEach(category => {
        pm.expect(category.productCount).to.eql(0);
    });
});
```

### 11. Delete Category

**Endpoint:** `DELETE {{baseUrl}}/api/categories/{{categoryId}}`

**Expected Response:** `204 No Content`

**Tests:**
```javascript
pm.test("Status code is 204", function () {
    pm.response.to.have.status(204);
});

pm.test("Response body is empty", function () {
    pm.expect(pm.response.text()).to.be.empty;
});
```

## Error Scenarios

### 1. Create Category with Duplicate Name

**Endpoint:** `POST {{baseUrl}}/api/categories`

**Request Body:**
```json
{
    "name": "Electronics",
    "description": "Duplicate category"
}
```

**Expected Response:** `400 Bad Request`
```json
{
    "error": "Business Logic Error",
    "message": "Category name already exists: Electronics",
    "timestamp": "2024-01-15T12:30:00"
}
```

### 2. Get Non-existent Category

**Endpoint:** `GET {{baseUrl}}/api/categories/999`

**Expected Response:** `404 Not Found`

### 3. Delete Category with Products

**Endpoint:** `DELETE {{baseUrl}}/api/categories/1`

**Expected Response:** `400 Bad Request`
```json
{
    "error": "Business Logic Error",
    "message": "Cannot delete category with 5 associated products",
    "timestamp": "2024-01-15T12:45:00"
}
```

### 4. Invalid Status Value

**Endpoint:** `GET {{baseUrl}}/api/categories/status/INVALID`

**Expected Response:** `400 Bad Request`

## Collection Variables

Set these variables in your Postman collection:

```json
{
    "baseUrl": "http://localhost:8080",
    "categoryId": "",
    "authToken": "",
    "searchTerm": "electronic"
}
```

## Pre-request Scripts

### Authentication Setup
```javascript
// Set auth token if available
const token = pm.environment.get("authToken");
if (token) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + token
    });
}
```

### Dynamic Category ID
```javascript
// Use the last created category ID
const categoryId = pm.environment.get("lastCreatedCategoryId");
if (categoryId) {
    pm.environment.set("categoryId", categoryId);
}
```

## Test Execution Order

1. Create Category
2. Get All Categories
3. Get Active Categories
4. Get Category by ID
5. Get Category by Name
6. Update Category
7. Search Categories
8. Get Categories by Status
9. Update Category Status
10. Get Empty Categories
11. Delete Category (last, as it removes the test data)

## Notes

- Ensure the backend server is running on `http://localhost:8080`
- Some tests depend on previous tests (e.g., getting a category requires creating it first)
- Authentication may be required depending on your security configuration
- Test data is cleaned up by the delete operation at the end
