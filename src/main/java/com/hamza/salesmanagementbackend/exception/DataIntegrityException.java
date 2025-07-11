package com.hamza.salesmanagementbackend.exception;

import lombok.Getter;

/**
 * Custom exception for data integrity violations, particularly foreign key constraints.
 * Provides user-friendly error messages instead of exposing technical database details.
 */
@Getter
public class DataIntegrityException extends RuntimeException {
    
    private final String resourceType;
    private final String dependentResource;
    private final Long resourceId;
    private final String userMessage;
    private final String errorCode;
    private final String suggestion;

    public DataIntegrityException(String resourceType, Long resourceId, String dependentResource, String userMessage) {
        super(userMessage);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.dependentResource = dependentResource;
        this.userMessage = userMessage;
        this.errorCode = "DATA_INTEGRITY_VIOLATION";
        this.suggestion = generateSuggestion(resourceType, dependentResource);
    }

    public DataIntegrityException(String resourceType, Long resourceId, String dependentResource, 
                                String userMessage, String errorCode) {
        super(userMessage);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.dependentResource = dependentResource;
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.suggestion = generateSuggestion(resourceType, dependentResource);
    }

    public DataIntegrityException(String resourceType, Long resourceId, String dependentResource, 
                                String userMessage, String errorCode, String suggestion) {
        super(userMessage);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.dependentResource = dependentResource;
        this.userMessage = userMessage;
        this.errorCode = errorCode;
        this.suggestion = suggestion;
    }

    private String generateSuggestion(String resourceType, String dependentResource) {
        return switch (resourceType.toLowerCase()) {
            case "sale" -> switch (dependentResource.toLowerCase()) {
                case "returns" -> "Please process or cancel all associated returns before deleting this sale.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            case "customer" -> switch (dependentResource.toLowerCase()) {
                case "sales" -> "Please complete, cancel, or reassign all customer sales before deleting this customer.";
                case "returns" -> "Please process all customer returns before deleting this customer.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            case "product" -> switch (dependentResource.toLowerCase()) {
                case "sale items" -> "This product has been sold and cannot be deleted. Consider marking it as inactive instead.";
                case "return items" -> "This product has associated returns and cannot be deleted.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            case "category" -> switch (dependentResource.toLowerCase()) {
                case "products" -> "Please move all products to another category or delete them before removing this category.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            case "supplier" -> switch (dependentResource.toLowerCase()) {
                case "purchase orders" -> "Please complete or cancel all purchase orders before deleting this supplier.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            case "inventory" -> switch (dependentResource.toLowerCase()) {
                case "categories" -> "Please move all categories to another inventory or delete them before removing this inventory.";
                default -> "Please remove or reassign all dependent records before deletion.";
            };
            default -> "Please remove or reassign all dependent records before deletion.";
        };
    }

    /**
     * Factory methods for common scenarios
     */
    public static DataIntegrityException saleHasReturns(Long saleId, int returnCount) {
        String message = String.format("Cannot delete sale because it has %d associated return%s", 
                                     returnCount, returnCount == 1 ? "" : "s");
        return new DataIntegrityException("Sale", saleId, "Returns", message, "SALE_HAS_RETURNS");
    }

    public static DataIntegrityException customerHasSales(Long customerId, int salesCount) {
        String message = String.format("Cannot delete customer because they have %d associated sale%s", 
                                     salesCount, salesCount == 1 ? "" : "s");
        return new DataIntegrityException("Customer", customerId, "Sales", message, "CUSTOMER_HAS_SALES");
    }

    public static DataIntegrityException customerHasReturns(Long customerId, int returnCount) {
        String message = String.format("Cannot delete customer because they have %d associated return%s", 
                                     returnCount, returnCount == 1 ? "" : "s");
        return new DataIntegrityException("Customer", customerId, "Returns", message, "CUSTOMER_HAS_RETURNS");
    }

    public static DataIntegrityException productHasSaleItems(Long productId, int saleItemCount) {
        String message = String.format("Cannot delete product because it appears in %d sale record%s", 
                                     saleItemCount, saleItemCount == 1 ? "" : "s");
        return new DataIntegrityException("Product", productId, "Sale Items", message, "PRODUCT_HAS_SALE_ITEMS");
    }

    public static DataIntegrityException productHasReturnItems(Long productId, int returnItemCount) {
        String message = String.format("Cannot delete product because it appears in %d return record%s", 
                                     returnItemCount, returnItemCount == 1 ? "" : "s");
        return new DataIntegrityException("Product", productId, "Return Items", message, "PRODUCT_HAS_RETURN_ITEMS");
    }

    public static DataIntegrityException categoryHasProducts(Long categoryId, int productCount) {
        String message = String.format("Cannot delete category because it contains %d product%s", 
                                     productCount, productCount == 1 ? "" : "s");
        return new DataIntegrityException("Category", categoryId, "Products", message, "CATEGORY_HAS_PRODUCTS");
    }

    public static DataIntegrityException supplierHasPurchaseOrders(Long supplierId, int orderCount) {
        String message = String.format("Cannot delete supplier because they have %d active purchase order%s",
                                     orderCount, orderCount == 1 ? "" : "s");
        return new DataIntegrityException("Supplier", supplierId, "Purchase Orders", message, "SUPPLIER_HAS_PURCHASE_ORDERS");
    }

    public static DataIntegrityException inventoryHasCategories(Long inventoryId, int categoryCount) {
        String message = String.format("Cannot delete inventory because it contains %d categor%s",
                                     categoryCount, categoryCount == 1 ? "y" : "ies");
        return new DataIntegrityException("Inventory", inventoryId, "Categories", message, "INVENTORY_HAS_CATEGORIES");
    }
}
