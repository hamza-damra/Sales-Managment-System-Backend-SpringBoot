# JPA Cascade Configuration Documentation

## Overview

This document describes the cascade configurations and orphan removal settings implemented across all JPA entity relationships in the Sales Management System. These configurations ensure proper data integrity and efficient deletion of parent records with their associated child records.

## Cascade Configuration Summary

### 1. Customer Entity

**Relationships:**
- `Customer → Sales`: `CascadeType.ALL, orphanRemoval = true`
- `Customer → Returns`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a Customer is deleted, all associated Sales and Returns are automatically deleted
- When a Sale or Return is removed from the Customer's collection, it's automatically deleted (orphan removal)
- This ensures complete cleanup of customer data while maintaining referential integrity

### 2. Sale Entity

**Relationships:**
- `Sale → SaleItems`: `CascadeType.ALL, orphanRemoval = true`
- `Sale → Returns`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a Sale is deleted, all associated SaleItems and Returns are automatically deleted
- Removing items from collections triggers orphan removal
- Maintains transactional integrity for sales data

### 3. Product Entity

**Relationships:**
- `Product → SaleItems`: `CascadeType.ALL, orphanRemoval = true`
- `Product → ReturnItems`: `CascadeType.ALL, orphanRemoval = true`
- `Product → PurchaseOrderItems`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a Product is deleted, all related transaction items are automatically deleted
- This prevents orphaned transaction items that reference non-existent products
- Ensures data consistency across all product-related transactions

### 4. Category Entity

**Relationships:**
- `Category → Products`: `CascadeType.PERSIST, CascadeType.MERGE` (NO orphanRemoval)

**Behavior:**
- Category deletion does NOT automatically delete products
- Products can exist without a category (category field becomes null)
- This prevents accidental loss of product data when reorganizing categories
- Only persist and merge operations are cascaded to maintain product-category associations

### 5. Return Entity

**Relationships:**
- `Return → ReturnItems`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a Return is deleted, all associated ReturnItems are automatically deleted
- Removing items from the return triggers orphan removal
- Maintains integrity of return transaction data

### 6. Supplier Entity

**Relationships:**
- `Supplier → PurchaseOrders`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a Supplier is deleted, all associated PurchaseOrders are automatically deleted
- This includes cascading deletion of PurchaseOrderItems through the PurchaseOrder cascade
- Ensures complete cleanup of supplier-related procurement data

### 7. PurchaseOrder Entity

**Relationships:**
- `PurchaseOrder → PurchaseOrderItems`: `CascadeType.ALL, orphanRemoval = true`

**Behavior:**
- When a PurchaseOrder is deleted, all associated PurchaseOrderItems are automatically deleted
- Removing items from the order triggers orphan removal
- Maintains integrity of procurement transaction data

## Cascade Types Explained

### CascadeType.ALL
- Includes: PERSIST, MERGE, REMOVE, REFRESH, DETACH
- Used for true parent-child relationships where children should not exist without parents
- Applied to most relationships in the system

### CascadeType.PERSIST + CascadeType.MERGE
- Only cascades save and update operations
- Used for Category → Products to prevent accidental deletion
- Allows products to exist independently of categories

### orphanRemoval = true
- Automatically deletes child entities when removed from parent collection
- Ensures no orphaned records remain in the database
- Applied to all parent-child relationships except Category → Products

## Business Rules and Considerations

### 1. Data Integrity
- All cascade configurations maintain referential integrity
- Foreign key constraints are properly handled
- No orphaned records are left in the database

### 2. Performance Considerations
- Cascade operations are performed in single transactions
- Bulk deletions are handled efficiently by JPA
- Lazy loading is used to prevent unnecessary data fetching

### 3. Business Logic Alignment
- Category deletion doesn't remove products (business requirement)
- Customer deletion removes all transaction history (compliance requirement)
- Product deletion removes all transaction items (data consistency requirement)

### 4. Error Handling
- DataIntegrityViolationException is properly handled in service layers
- User-friendly error messages are provided for constraint violations
- Pre-deletion validation checks prevent invalid operations

## Testing

Comprehensive tests are provided in `CascadeConfigurationTest.java` to verify:
- Proper cascade deletion behavior
- Orphan removal functionality
- Data integrity maintenance
- Business rule compliance

## Migration Considerations

When updating existing databases:
1. Ensure all foreign key constraints are properly defined
2. Test cascade operations in a staging environment
3. Backup data before applying cascade configuration changes
4. Monitor performance impact of cascade operations

## Best Practices

1. **Always use transactions** when performing cascade operations
2. **Test cascade behavior** thoroughly before production deployment
3. **Monitor database performance** after implementing cascade configurations
4. **Document business rules** that influence cascade decisions
5. **Use appropriate fetch strategies** to optimize performance
6. **Implement proper error handling** for constraint violations

## Troubleshooting

### Common Issues:
1. **ConstraintViolationException**: Check for circular dependencies or missing cascade configurations
2. **LazyInitializationException**: Ensure proper transaction boundaries
3. **Performance Issues**: Review fetch strategies and consider batch operations
4. **Data Loss**: Verify cascade configurations align with business requirements

### Solutions:
- Use `@Transactional` annotations appropriately
- Implement proper exception handling
- Use batch operations for large datasets
- Regular database maintenance and optimization
