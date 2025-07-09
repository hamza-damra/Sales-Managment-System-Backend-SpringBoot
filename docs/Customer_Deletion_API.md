# Customer Deletion API Documentation

## Overview

The Customer Deletion API provides multiple strategies for handling customer deletion while maintaining data integrity and business requirements. The system supports both soft delete (recommended) and hard delete operations.

## Deletion Strategies

### 1. Soft Delete (Recommended)
Marks customers as deleted without removing records from the database. This preserves data integrity and allows for restoration.

**Endpoint:** `DELETE /api/customers/{id}?deleteType=soft&deletedBy={user}&reason={reason}`

**Parameters:**
- `id` (path): Customer ID to delete
- `deleteType` (query): "soft" (default)
- `deletedBy` (query): User who initiated the deletion (optional, defaults to "API_USER")
- `reason` (query): Reason for deletion (optional, defaults to "Customer deletion requested via API")

**Example:**
```bash
curl -X DELETE "http://localhost:8081/api/customers/1?deleteType=soft&deletedBy=admin&reason=Customer%20requested%20account%20closure" \
  -H "Authorization: Bearer {token}"
```

**Response:** `204 No Content`

### 2. Hard Delete with Cascade
Permanently removes customer and all associated records (sales, returns, etc.).

**Endpoint:** `DELETE /api/customers/{id}?deleteType=hard`

**Parameters:**
- `id` (path): Customer ID to delete
- `deleteType` (query): "hard"

**Example:**
```bash
curl -X DELETE "http://localhost:8081/api/customers/1?deleteType=hard" \
  -H "Authorization: Bearer {token}"
```

**Response:** `204 No Content`

**⚠️ Warning:** This operation is irreversible and will delete all associated sales and returns.

### 3. Force Delete (Legacy)
Backward compatibility with existing force delete functionality.

**Endpoint:** `DELETE /api/customers/{id}?deleteType=force`

**Parameters:**
- `id` (path): Customer ID to delete
- `deleteType` (query): "force"

## Customer Restoration

### Restore Soft-Deleted Customer
Restores a previously soft-deleted customer.

**Endpoint:** `POST /api/customers/{id}/restore`

**Parameters:**
- `id` (path): Customer ID to restore

**Example:**
```bash
curl -X POST "http://localhost:8081/api/customers/1/restore" \
  -H "Authorization: Bearer {token}"
```

**Response:** `200 OK` with restored customer data

## Viewing Deleted Customers

### Get Deleted Customers
Retrieves paginated list of soft-deleted customers.

**Endpoint:** `GET /api/customers/deleted?page={page}&size={size}&sortBy={field}&sortDir={direction}`

**Parameters:**
- `page` (query): Page number (default: 0)
- `size` (query): Page size (default: 10)
- `sortBy` (query): Sort field (default: "deletedAt")
- `sortDir` (query): Sort direction (default: "desc")

**Example:**
```bash
curl -X GET "http://localhost:8081/api/customers/deleted?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```

## Error Handling

### Business Logic Errors

**Customer Cannot Be Deleted (Status: 400)**
```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Customer cannot be deleted due to current status: BLACKLISTED",
  "errorCode": "BUSINESS_LOGIC_ERROR",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Customer Not Found (Status: 404)**
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Customer not found with id: 1",
  "errorCode": "RESOURCE_NOT_FOUND",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Customer Already Deleted (Status: 400)**
```json
{
  "status": 400,
  "error": "Business Rule Violation",
  "message": "Customer is not deleted and cannot be restored",
  "errorCode": "BUSINESS_LOGIC_ERROR",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Business Rules

1. **Soft Delete Default**: All deletion requests default to soft delete unless explicitly specified
2. **Status Change**: Soft-deleted customers have their status changed to INACTIVE
3. **Blacklisted Protection**: Customers with BLACKLISTED status cannot be deleted
4. **Restoration**: Only soft-deleted customers can be restored
5. **Query Filtering**: Standard customer queries exclude soft-deleted customers
6. **Audit Trail**: All deletion operations are logged with user and reason

## Migration Guide

### From Previous Version
1. Run the database migration script to add soft delete fields
2. Update client applications to use new deletion parameters
3. Existing `force=true` parameter still works for backward compatibility

### Recommended Practices
1. Use soft delete for production environments
2. Implement regular cleanup jobs for old soft-deleted records
3. Provide user interfaces for viewing and restoring deleted customers
4. Always specify deletion reason for audit purposes
