# Cascade Deletion Usage Guide

## Overview

The Sales Management System implements **two-layer cascade deletion protection**:

1. **JPA Layer**: Automatic cascade deletion configured at the entity level
2. **Business Layer**: Validation checks to prevent accidental data loss

## 🔍 **Why Cascade Deletion Wasn't Working**

### The Issue:
```
2025-07-08T20:29:50.470+03:00  WARN 12064 --- [SalesManagementBackend] [nio-8081-exec-9] 
.m.m.a.ExceptionHandlerExceptionResolver : Resolved [com.hamza.salesmanagementbackend.exception.DataIntegrityException: 
Cannot delete customer because they have 3 associated sales]
```

### Root Cause:
The **business validation layer** was preventing cascade deletion by throwing `DataIntegrityException` before the JPA cascade could execute.

```java
// Business validation prevents cascade deletion
Long salesCount = customerRepository.countSalesByCustomerId(id);
if (salesCount > 0) {
    throw DataIntegrityException.customerHasSales(id, salesCount.intValue());
}
```

## ✅ **Solution Implemented**

### Two-Mode Deletion System:

1. **Safe Mode** (Default): Prevents deletion when related records exist
2. **Force Mode**: Allows cascade deletion of all related records

## 🚀 **How to Use Cascade Deletion**

### 1. **Safe Deletion** (Default Behavior)
```bash
# This will fail if customer has sales or returns
DELETE /api/customers/123
```

**Response when related records exist:**
```json
{
  "status": 409,
  "error": "Data Integrity Violation",
  "message": "Cannot delete customer because they have 3 associated sales",
  "errorCode": "CUSTOMER_HAS_SALES",
  "suggestions": "Please complete, cancel, or reassign all customer sales before deleting this customer."
}
```

### 2. **Force Deletion** (Cascade Mode)
```bash
# This will cascade delete all related sales, returns, and items
DELETE /api/customers/123?force=true
```

**What happens:**
- ✅ Customer is deleted
- ✅ All Sales are cascade deleted
- ✅ All SaleItems are cascade deleted  
- ✅ All Returns are cascade deleted
- ✅ All ReturnItems are cascade deleted
- ✅ Products remain (not cascade deleted)

## 📋 **API Usage Examples**

### Safe Deletion (Default)
```bash
curl -X DELETE "http://localhost:8081/api/customers/123"
```

### Force Deletion (Cascade)
```bash
curl -X DELETE "http://localhost:8081/api/customers/123?force=true"
```

### JavaScript/Frontend Usage
```javascript
// Safe deletion
const safeDelete = async (customerId) => {
    try {
        await fetch(`/api/customers/${customerId}`, { method: 'DELETE' });
        console.log('Customer deleted successfully');
    } catch (error) {
        if (error.status === 409) {
            console.log('Customer has related records - use force delete');
        }
    }
};

// Force deletion with confirmation
const forceDelete = async (customerId) => {
    const confirmed = confirm('This will delete the customer and ALL related sales/returns. Continue?');
    if (confirmed) {
        await fetch(`/api/customers/${customerId}?force=true`, { method: 'DELETE' });
        console.log('Customer and all related records deleted');
    }
};
```

## 🔒 **Security and Audit Features**

### Audit Logging
When force deletion is used, the system logs the operation:

```
WARN: Force deleting customer 123 with 5 sales and 2 returns - cascade deletion will occur
```

### Business Rules Enforced
- **Customer → Sales/Returns**: Cascade deletion available with force flag
- **Category → Products**: Limited cascade (PERSIST/MERGE only) - products preserved
- **Product → Transaction Items**: Full cascade deletion
- **Supplier → Purchase Orders**: Full cascade deletion

## 🎯 **Cascade Behavior Matrix**

| Entity | Child Entities | Safe Mode | Force Mode | Cascade Type |
|--------|---------------|-----------|------------|--------------|
| **Customer** | Sales, Returns | ❌ Blocked | ✅ Cascade Delete | CascadeType.ALL |
| **Sale** | SaleItems, Returns | ✅ Always Cascade | ✅ Always Cascade | CascadeType.ALL |
| **Product** | SaleItems, ReturnItems, PurchaseOrderItems | ✅ Always Cascade | ✅ Always Cascade | CascadeType.ALL |
| **Category** | Products | ✅ Products Preserved | ✅ Products Preserved | PERSIST/MERGE only |
| **Supplier** | PurchaseOrders | ❌ Blocked* | ✅ Cascade Delete* | CascadeType.ALL |
| **Return** | ReturnItems | ✅ Always Cascade | ✅ Always Cascade | CascadeType.ALL |

*Note: Supplier and other entities can be updated with similar force delete functionality if needed.

## 🛡️ **Best Practices**

### 1. **Use Safe Mode by Default**
- Protects against accidental data loss
- Forces users to consider the impact
- Provides clear error messages with suggestions

### 2. **Use Force Mode Carefully**
- Only when you're certain about the deletion
- Implement user confirmation in frontend
- Consider data backup before force deletion

### 3. **Frontend Implementation**
```javascript
const deleteCustomer = async (customerId) => {
    try {
        // Try safe deletion first
        await fetch(`/api/customers/${customerId}`, { method: 'DELETE' });
    } catch (error) {
        if (error.status === 409) {
            // Show user the related records and ask for confirmation
            const forceDelete = confirm(`Customer has related records. Delete anyway?`);
            if (forceDelete) {
                await fetch(`/api/customers/${customerId}?force=true`, { method: 'DELETE' });
            }
        }
    }
};
```

### 4. **Error Handling**
```javascript
try {
    await deleteCustomer(123);
} catch (error) {
    switch (error.status) {
        case 404:
            console.log('Customer not found');
            break;
        case 409:
            console.log('Customer has related records:', error.message);
            break;
        case 500:
            console.log('Server error during deletion');
            break;
    }
}
```

## 🔧 **Implementation Details**

### Service Layer
```java
// Safe deletion (default)
customerService.deleteCustomer(customerId);

// Force deletion (cascade)
customerService.deleteCustomer(customerId, true);
```

### Controller Layer
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteCustomer(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean force) {
    customerService.deleteCustomer(id, force);
    return ResponseEntity.noContent().build();
}
```

### JPA Configuration
```java
@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Sale> sales;

@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Return> returns;
```

## 📊 **Testing Cascade Deletion**

### Test Safe Mode
```bash
# Create customer with sales
POST /api/customers
POST /api/sales

# Try to delete (should fail)
DELETE /api/customers/123
# Expected: 409 Conflict
```

### Test Force Mode
```bash
# Delete with force flag (should succeed)
DELETE /api/customers/123?force=true
# Expected: 204 No Content
# Verify: All related sales and returns are deleted
```

## 🎉 **Summary**

The cascade deletion is now **fully functional** with two modes:

1. **✅ Safe Mode**: Protects against accidental deletion
2. **✅ Force Mode**: Enables cascade deletion when needed

**To enable cascade deletion in your application:**
```bash
DELETE /api/customers/{id}?force=true
```

This provides the best of both worlds: **data protection by default** and **cascade deletion when needed**.
