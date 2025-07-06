# Category Feature Documentation

## Overview

The Category feature has been successfully implemented in the Sales Management System Backend. This feature provides comprehensive category management functionality including CRUD operations, search capabilities, status management, and integration with the Product entity.

## Table of Contents

1. [Entity Structure](#entity-structure)
2. [API Endpoints](#api-endpoints)
3. [Data Transfer Objects](#data-transfer-objects)
4. [Service Layer](#service-layer)
5. [Repository Layer](#repository-layer)
6. [Validation & Business Rules](#validation--business-rules)
7. [Integration with Products](#integration-with-products)
8. [Testing](#testing)
9. [Usage Examples](#usage-examples)

## Entity Structure

### Category Entity

The `Category` entity is the core domain model with the following structure:

**Table:** `categories`

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `id` | Long | Primary Key, Auto-generated | Unique identifier |
| `name` | String | Not null, Unique, Not blank | Category name |
| `description` | String | Text field | Category description |
| `displayOrder` | Integer | Default: 0 | Display order for sorting |
| `status` | CategoryStatus | Enum, Default: ACTIVE | Category status |
| `imageUrl` | String | Optional | Category image URL |
| `icon` | String | Optional | Category icon identifier |
| `colorCode` | String | Optional | Category color code |
| `createdAt` | LocalDateTime | Auto-generated | Creation timestamp |
| `updatedAt` | LocalDateTime | Auto-updated | Last update timestamp |
| `products` | List<Product> | One-to-Many | Associated products |

### Category Status Enum

```java
public enum CategoryStatus {
    ACTIVE,    // Category is active and visible
    INACTIVE,  // Category is inactive but not deleted
    ARCHIVED   // Category is archived
}
```

### Key Features

- **Unique name constraint** prevents duplicate category names
- **Soft delete support** through status management
- **Display order** for custom sorting
- **Bidirectional relationship** with Product entity
- **Automatic timestamps** for audit trail
- **Business logic methods** for common operations

## API Endpoints

### Base URL: `/api/categories`

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/` | Get all categories with pagination | - | Page<CategoryDTO> |
| GET | `/active` | Get all active categories | - | List<CategoryDTO> |
| GET | `/{id}` | Get category by ID | - | CategoryDTO |
| GET | `/name/{name}` | Get category by name | - | CategoryDTO |
| POST | `/` | Create new category | CategoryDTO | CategoryDTO |
| PUT | `/{id}` | Update category | CategoryDTO | CategoryDTO |
| DELETE | `/{id}` | Delete category | - | 204 No Content |
| GET | `/search` | Search categories | query param | Page<CategoryDTO> |
| GET | `/status/{status}` | Get categories by status | - | List<CategoryDTO> |
| GET | `/empty` | Get empty categories | - | List<CategoryDTO> |
| PUT | `/{id}/status` | Update category status | {"status": "ACTIVE"} | CategoryDTO |

### Query Parameters

**Pagination & Sorting (GET `/`):**
- `page` (default: 0) - Page number
- `size` (default: 10) - Page size
- `sortBy` (default: "displayOrder") - Sort field
- `sortDir` (default: "asc") - Sort direction
- `status` (optional) - Filter by status

**Search (GET `/search`):**
- `q` - Search term (searches name and description)
- Standard pagination parameters

## Data Transfer Objects

### CategoryDTO

```java
public class CategoryDTO {
    private Long id;
    private String name;                    // Required
    private String description;
    private Integer displayOrder;
    private CategoryStatus status;
    private String imageUrl;
    private String icon;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer productCount;           // Read-only field
}
```

### Validation Rules

- **name**: Required, not blank
- **displayOrder**: Defaults to 0 if not provided
- **status**: Defaults to ACTIVE if not provided
- **productCount**: Automatically calculated, read-only

## Service Layer

### CategoryService

The service layer provides comprehensive business logic:

#### Core Operations
- `createCategory(CategoryDTO)` - Create new category with validation
- `getAllCategories(Pageable)` - Get paginated categories
- `getAllActiveCategories()` - Get active categories ordered by display order
- `getCategoryById(Long)` - Get category by ID
- `getCategoryByName(String)` - Get category by name
- `updateCategory(Long, CategoryDTO)` - Update existing category
- `deleteCategory(Long)` - Delete category (with product validation)

#### Advanced Operations
- `searchCategories(String, Pageable)` - Search with pagination
- `getCategoriesByStatus(CategoryStatus)` - Filter by status
- `getEmptyCategories()` - Get categories without products
- `updateCategoryStatus(Long, CategoryStatus)` - Update status only

#### Business Rules
- **Name uniqueness validation** across all categories
- **Product association check** before deletion
- **Automatic trimming** of category names
- **Default value assignment** for optional fields

## Repository Layer

### CategoryRepository

Custom query methods for advanced functionality:

```java
// Basic finders
Optional<Category> findByName(String name);
Optional<Category> findByNameIgnoreCase(String name);
List<Category> findByStatus(CategoryStatus status);

// Advanced queries
@Query("SELECT c FROM Category c ORDER BY c.displayOrder ASC, c.name ASC")
List<Category> findAllOrderedByDisplayOrder();

@Query("SELECT c FROM Category c WHERE c.status = :status ORDER BY c.displayOrder ASC, c.name ASC")
List<Category> findByStatusOrderedByDisplayOrder(@Param("status") CategoryStatus status);

// Search functionality
@Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
Page<Category> searchCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

// Product relationship queries
@Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
Long countProductsByCategoryId(@Param("categoryId") Long categoryId);

@Query("SELECT c FROM Category c WHERE SIZE(c.products) = 0")
List<Category> findEmptyCategories();
```

## Validation & Business Rules

### Input Validation
1. **Category name** must be provided and not blank
2. **Name uniqueness** enforced at database and service level
3. **Display order** defaults to 0 if not provided
4. **Status** defaults to ACTIVE if not provided

### Business Logic
1. **Deletion protection**: Categories with associated products cannot be deleted
2. **Name trimming**: Category names are automatically trimmed
3. **Case-insensitive search**: Search functionality ignores case
4. **Status management**: Supports soft delete through status changes

### Error Handling
- `ResourceNotFoundException` for non-existent categories
- `BusinessLogicException` for business rule violations
- `ValidationException` for invalid input data

## Integration with Products

### Bidirectional Relationship
- Categories can have multiple products (`@OneToMany`)
- Products belong to one category (`@ManyToOne`)
- Lazy loading for performance optimization

### Product Integration Features
- **Product count calculation** in CategoryDTO
- **Category validation** in ProductService
- **Cascade operations** for data consistency
- **Foreign key constraints** for referential integrity

### Usage in Product Operations
```java
// Create product with category ID
ProductDTO product = ProductDTO.builder()
    .name("Laptop")
    .categoryId(1L)  // References category
    .build();

// Create product with category name
ProductDTO product = ProductDTO.builder()
    .name("Laptop")
    .categoryName("Electronics")  // Resolves to category
    .build();
```

## Testing

### Test Coverage

The category feature includes comprehensive test coverage:

#### Unit Tests
- **CategoryServiceTest**: Service layer business logic
- **CategoryRepositoryTest**: Repository query methods
- **CategoryControllerTest**: REST API endpoints

#### Integration Tests
- **CategoryIntegrationTest**: End-to-end category operations
- **ProductCategoryIntegrationTest**: Product-category relationship testing

#### Test Scenarios Covered
1. **CRUD Operations**: Create, read, update, delete categories
2. **Validation**: Name uniqueness, required fields, business rules
3. **Search Functionality**: Text search with pagination
4. **Status Management**: Active, inactive, archived categories
5. **Product Integration**: Category assignment and validation
6. **Error Handling**: Exception scenarios and edge cases

### Running Tests
```bash
# Run all category tests
mvn test -Dtest="*Category*Test"

# Run specific test class
mvn test -Dtest=CategoryServiceTest

# Run integration tests
mvn test -Dtest=CategoryIntegrationTest
```

## Usage Examples

### 1. Create a New Category

**Request:**
```http
POST /api/categories
Content-Type: application/json

{
    "name": "Electronics",
    "description": "Electronic devices and accessories",
    "displayOrder": 1,
    "imageUrl": "https://example.com/electronics.jpg",
    "icon": "electronics-icon",
    "colorCode": "#007bff"
}
```

**Response:**
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

### 2. Get All Active Categories

**Request:**
```http
GET /api/categories/active
```

**Response:**
```json
[
    {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices and accessories",
        "displayOrder": 1,
        "status": "ACTIVE",
        "productCount": 5
    },
    {
        "id": 2,
        "name": "Books",
        "description": "Books and literature",
        "displayOrder": 2,
        "status": "ACTIVE",
        "productCount": 12
    }
]
```

### 3. Search Categories

**Request:**
```http
GET /api/categories/search?q=electronic&page=0&size=10
```

**Response:**
```json
{
    "content": [
        {
            "id": 1,
            "name": "Electronics",
            "description": "Electronic devices and accessories",
            "status": "ACTIVE",
            "productCount": 5
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
}
```

### 4. Update Category Status

**Request:**
```http
PUT /api/categories/1/status
Content-Type: application/json

{
    "status": "INACTIVE"
}
```

**Response:**
```json
{
    "id": 1,
    "name": "Electronics",
    "status": "INACTIVE",
    "updatedAt": "2024-01-15T11:45:00"
}
```

### 5. Delete Category

**Request:**
```http
DELETE /api/categories/1
```

**Response:**
```http
204 No Content
```

**Error Response (if category has products):**
```json
{
    "error": "Business Logic Error",
    "message": "Cannot delete category with 5 associated products",
    "timestamp": "2024-01-15T12:00:00"
}
```

## Frontend Integration

### JavaScript/TypeScript Examples

```javascript
// Category service class
class CategoryService {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async getAllActiveCategories() {
        const response = await fetch(`${this.baseUrl}/api/categories/active`);
        return response.json();
    }

    async createCategory(categoryData) {
        const response = await fetch(`${this.baseUrl}/api/categories`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(categoryData)
        });
        return response.json();
    }

    async searchCategories(searchTerm, page = 0, size = 10) {
        const params = new URLSearchParams({
            q: searchTerm,
            page: page.toString(),
            size: size.toString()
        });
        const response = await fetch(`${this.baseUrl}/api/categories/search?${params}`);
        return response.json();
    }
}
```

## Database Schema

### Categories Table

```sql
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    display_order INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    image_url VARCHAR(500),
    icon VARCHAR(100),
    color_code VARCHAR(7),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name),
    INDEX idx_category_status (status),
    INDEX idx_category_display_order (display_order)
);
```

### Foreign Key Relationships

```sql
-- Products table references categories
ALTER TABLE products
ADD CONSTRAINT fk_product_category
FOREIGN KEY (category_id) REFERENCES categories(id);
```

## Performance Considerations

### Indexing Strategy
- **Primary index** on `id` (auto-created)
- **Unique index** on `name` for uniqueness constraint
- **Composite index** on `status` and `display_order` for filtering and sorting
- **Text index** on `name` and `description` for search functionality

### Query Optimization
- **Lazy loading** for product relationships
- **Pagination** for large result sets
- **Selective field loading** in DTOs
- **Caching strategies** for frequently accessed categories

### Best Practices
1. Use pagination for category lists
2. Implement caching for active categories
3. Optimize search queries with proper indexing
4. Use batch operations for bulk updates
5. Monitor query performance with database profiling

## Security Considerations

### Access Control
- All endpoints require authentication
- Role-based access control for administrative operations
- Input validation and sanitization
- SQL injection prevention through parameterized queries

### Data Protection
- Sensitive data encryption at rest
- Secure API communication over HTTPS
- Input validation and output encoding
- Audit logging for category modifications

## Conclusion

The Category feature provides a robust foundation for product categorization in the Sales Management System. It offers:

- **Complete CRUD operations** with comprehensive validation
- **Advanced search and filtering** capabilities
- **Seamless integration** with the Product entity
- **Flexible status management** for soft deletes
- **Performance-optimized** queries and indexing
- **Comprehensive test coverage** for reliability
- **RESTful API design** for easy frontend integration

The implementation follows Spring Boot best practices and provides a scalable solution for category management in e-commerce applications.
