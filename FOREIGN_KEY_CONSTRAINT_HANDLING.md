# Foreign Key Constraint Handling Documentation

## Overview

This document describes the comprehensive foreign key constraint handling implementation in the Sales Management System. The system now provides user-friendly error messages instead of exposing technical database constraint details when attempting to delete resources with dependent records.

## Problem Statement

Previously, when attempting to delete a record that had dependent records (e.g., deleting a Sale that has associated Returns), the system would throw a raw `DataIntegrityViolationException` with technical database error messages like:

```
Cannot delete or update a parent row: a foreign key constraint fails 
(`sales_management`.`returns`, CONSTRAINT `FKeyaqjk5heqbphujdvhu14rpij` 
FOREIGN KEY (`original_sale_id`) REFERENCES `sales` (`id`))
```

## Solution Implementation

### 1. Custom Exception Classes

#### DataIntegrityException
A custom exception class that provides user-friendly error messages for data integrity violations.

**Key Features:**
- User-friendly error messages
- Specific error codes for different scenarios
- Helpful suggestions for resolution
- Factory methods for common scenarios

**Example Usage:**
```java
throw DataIntegrityException.saleHasReturns(saleId, returnCount);
```

### 2. Enhanced Global Exception Handler

The `GlobalExceptionHandler` now includes handlers for:

#### DataIntegrityException Handler
- Returns HTTP 409 Conflict status
- Provides structured error response with suggestions
- Includes resource details for frontend processing

#### DataIntegrityViolationException Handler
- Catches raw database constraint violations
- Parses constraint messages to provide user-friendly responses
- Falls back to generic messages for unknown constraints

### 3. Service Layer Validation

Each service now includes proactive validation before attempting deletions:

#### SaleService
- Checks for associated Returns before deletion
- Throws `DataIntegrityException.saleHasReturns()` if returns exist

#### CustomerService
- Checks for associated Sales and Returns
- Throws appropriate exceptions with specific counts

#### ProductService
- Checks for associated SaleItems and ReturnItems
- Provides suggestions to mark products as inactive instead of deletion

#### CategoryService
- Checks for associated Products
- Suggests moving products to another category

#### SupplierService
- Checks for active Purchase Orders
- Counts active orders for detailed error messages

### 4. Repository Enhancements

Added count methods to repositories for dependency checking:

```java
// SaleRepository
@Query("SELECT COUNT(r) FROM Return r WHERE r.originalSale.id = :saleId")
Long countReturnsBySaleId(@Param("saleId") Long saleId);

// CustomerRepository
@Query("SELECT COUNT(s) FROM Sale s WHERE s.customer.id = :customerId")
Long countSalesByCustomerId(@Param("customerId") Long customerId);

// ProductRepository
@Query("SELECT COUNT(si) FROM SaleItem si WHERE si.product.id = :productId")
Long countSaleItemsByProductId(@Param("productId") Long productId);
```

## Error Response Format

### Standard Data Integrity Error Response

```json
{
  "status": 409,
  "error": "Data Integrity Violation",
  "message": "Cannot delete sale because it has 3 associated returns",
  "errorCode": "SALE_HAS_RETURNS",
  "timestamp": "2025-07-08T10:00:00",
  "suggestions": "Please process or cancel all associated returns before deleting this sale.",
  "details": {
    "resourceType": "Sale",
    "resourceId": 123,
    "dependentResource": "Returns"
  }
}
```

## Supported Error Codes

| Error Code | Description | Suggestion |
|------------|-------------|------------|
| `SALE_HAS_RETURNS` | Sale has associated returns | Process or cancel all returns first |
| `CUSTOMER_HAS_SALES` | Customer has associated sales | Complete, cancel, or reassign sales |
| `CUSTOMER_HAS_RETURNS` | Customer has associated returns | Process all customer returns |
| `PRODUCT_HAS_SALE_ITEMS` | Product appears in sales | Mark as inactive instead |
| `PRODUCT_HAS_RETURN_ITEMS` | Product appears in returns | Cannot delete, has return history |
| `CATEGORY_HAS_PRODUCTS` | Category contains products | Move products to another category |
| `SUPPLIER_HAS_PURCHASE_ORDERS` | Supplier has active orders | Complete or cancel orders |
| `DATABASE_CONSTRAINT_VIOLATION` | Generic constraint violation | Remove dependent records |

## Frontend Integration

### Error Handling Example

```javascript
async function deleteSale(saleId) {
  try {
    await fetch(`/api/sales/${saleId}`, { method: 'DELETE' });
    showSuccess('Sale deleted successfully');
  } catch (error) {
    if (error.status === 409) {
      const errorData = await error.json();
      
      switch (errorData.errorCode) {
        case 'SALE_HAS_RETURNS':
          showError(errorData.message, errorData.suggestions);
          // Optionally show returns management UI
          break;
        default:
          showError(errorData.message, errorData.suggestions);
      }
    } else {
      showError('An unexpected error occurred');
    }
  }
}
```

### User Experience Improvements

1. **Clear Error Messages**: Users see business-friendly messages instead of technical jargon
2. **Actionable Suggestions**: Each error includes specific steps to resolve the issue
3. **Contextual Information**: Error details help frontend applications provide better UX
4. **Consistent Format**: All data integrity errors follow the same response structure

## Testing

### Unit Tests
- `DataIntegrityExceptionTest`: Tests custom exception creation and messages
- `SaleServiceDataIntegrityTest`: Tests sale deletion with returns
- `CustomerServiceDataIntegrityTest`: Tests customer deletion with dependencies
- `ProductServiceDataIntegrityTest`: Tests product deletion with sale/return history
- `GlobalExceptionHandlerDataIntegrityTest`: Tests exception handler responses

### Integration Tests
- `DataIntegrityConstraintIntegrationTest`: End-to-end testing of constraint handling

## Best Practices

### For Developers

1. **Always Check Dependencies**: Before deleting any resource, check for dependent records
2. **Use Specific Exceptions**: Use the factory methods in `DataIntegrityException` for consistency
3. **Provide Helpful Suggestions**: Include actionable advice in error messages
4. **Test Edge Cases**: Ensure proper handling of single vs. multiple dependencies

### For Frontend Developers

1. **Handle 409 Status Codes**: Always check for conflict responses on delete operations
2. **Parse Error Codes**: Use specific error codes to provide tailored user experiences
3. **Show Suggestions**: Display the suggestions field to help users resolve issues
4. **Provide Alternative Actions**: For products, offer "Mark as Inactive" instead of delete

## Migration Notes

### Backward Compatibility
- Existing API endpoints maintain the same URLs and request formats
- Only error responses have been enhanced
- No breaking changes to successful operation responses

### Database Considerations
- No database schema changes required
- Foreign key constraints remain in place for data integrity
- Application-level validation prevents constraint violations

## Future Enhancements

1. **Soft Delete Implementation**: Consider implementing soft deletes for critical business entities
2. **Cascade Delete Options**: Provide controlled cascade delete for appropriate relationships
3. **Bulk Operations**: Extend constraint handling to bulk delete operations
4. **Audit Trail**: Log constraint violation attempts for business intelligence

## Conclusion

This implementation provides a professional, user-friendly approach to handling foreign key constraints while maintaining data integrity. The solution balances technical robustness with excellent user experience, making the system more maintainable and user-friendly.
