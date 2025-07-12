package com.hamza.salesmanagementbackend.util;

import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Set;

/**
 * Utility class for handling sorting parameters and validation
 */
public class SortingUtils {

    // Valid sort fields for Customer entity
    public static final Set<String> VALID_CUSTOMER_SORT_FIELDS = Set.of(
            "id", "name", "firstName", "lastName", "email", "phone", "address",
            "customerType", "customerStatus", "totalPurchases", "loyaltyPoints",
            "lastPurchaseDate", "createdAt", "updatedAt"
    );

    // Valid sort fields for Product entity
    public static final Set<String> VALID_PRODUCT_SORT_FIELDS = Set.of(
            "id", "name", "description", "price", "costPrice", "stockQuantity",
            "category", "sku", "brand", "productStatus", "minStockLevel",
            "reorderPoint", "totalSold", "totalRevenue", "lastSoldDate",
            "createdAt", "updatedAt"
    );

    // Valid sort fields for Sale entity
    public static final Set<String> VALID_SALE_SORT_FIELDS = Set.of(
            "id", "saleDate", "totalAmount", "status", "saleNumber",
            "subtotal", "paymentMethod", "paymentStatus", "paymentDate",
            "dueDate", "salesPerson", "saleType", "deliveryStatus",
            "createdAt", "updatedAt"
    );

    // Valid sort fields for Supplier entity
    public static final Set<String> VALID_SUPPLIER_SORT_FIELDS = Set.of(
            "id", "name", "contactPerson", "phone", "email", "address",
            "city", "country", "taxNumber", "rating", "status",
            "totalOrders", "totalAmount", "lastOrderDate", "createdAt", "updatedAt"
    );

    // Valid sort fields for Return entity
    public static final Set<String> VALID_RETURN_SORT_FIELDS = Set.of(
            "id", "returnDate", "reason", "status", "totalRefundAmount",
            "processedDate", "refundMethod", "createdAt", "updatedAt"
    );

    // Valid sort fields for Promotion entity
    public static final Set<String> VALID_PROMOTION_SORT_FIELDS = Set.of(
            "id", "name", "type", "discountValue", "startDate", "endDate",
            "isActive", "usageCount", "usageLimit", "createdAt", "updatedAt"
    );

    // Valid sort fields for Warehouse entity
    public static final Set<String> VALID_WAREHOUSE_SORT_FIELDS = Set.of(
            "id", "name", "code", "address", "city", "country",
            "isActive", "createdAt", "updatedAt"
    );

    // Valid sort fields for StockMovement entity
    public static final Set<String> VALID_STOCK_MOVEMENT_SORT_FIELDS = Set.of(
            "id", "movementType", "quantity", "unitCost", "totalValue",
            "date", "reference", "createdAt"
    );

    // Valid sort fields for Category entity
    public static final Set<String> VALID_CATEGORY_SORT_FIELDS = Set.of(
            "id", "name", "description", "displayOrder", "status",
            "createdAt", "updatedAt"
    );

    // Valid sort fields for Inventory entity
    public static final Set<String> VALID_INVENTORY_SORT_FIELDS = Set.of(
            "id", "name", "description", "location", "address", "managerName",
            "length", "width", "height", "currentStockCount", "status", "warehouseCode",
            "isMainWarehouse", "startWorkTime", "endWorkTime", "createdAt", "updatedAt"
    );

    // Valid sort fields for PurchaseOrder entity
    public static final Set<String> VALID_PURCHASE_ORDER_SORT_FIELDS = Set.of(
            "id", "orderNumber", "orderDate", "expectedDeliveryDate", "actualDeliveryDate",
            "status", "priority", "totalAmount", "subtotal", "taxAmount", "discountAmount",
            "shippingCost", "createdBy", "approvedBy", "approvedDate", "sentDate",
            "createdAt", "updatedAt"
    );

    // Valid sort directions
    public static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");

    /**
     * Validates and returns a safe sort field for Customer entity
     */
    public static String validateCustomerSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }
        
        String cleanSortBy = sortBy.trim().toLowerCase();
        
        // Check if the field exists in our valid fields (case-insensitive)
        for (String validField : VALID_CUSTOMER_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }
        
        // Default to 'id' if invalid field provided
        return "id";
    }

    /**
     * Validates and returns a safe sort field for Product entity
     */
    public static String validateProductSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }
        
        String cleanSortBy = sortBy.trim().toLowerCase();
        
        for (String validField : VALID_PRODUCT_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }
        
        return "id";
    }

    /**
     * Validates and returns a safe sort field for Sale entity
     */
    public static String validateSaleSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }
        
        String cleanSortBy = sortBy.trim().toLowerCase();
        
        for (String validField : VALID_SALE_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }
        
        return "id";
    }

    /**
     * Validates and returns a safe sort direction
     */
    public static String validateSortDirection(String sortDir) {
        if (sortDir == null || sortDir.trim().isEmpty()) {
            return "asc";
        }
        
        String cleanSortDir = sortDir.trim().toLowerCase();
        return VALID_SORT_DIRECTIONS.contains(cleanSortDir) ? cleanSortDir : "asc";
    }

    /**
     * Creates a safe Sort object for Customer entity
     */
    public static Sort createCustomerSort(String sortBy, String sortDir) {
        String validSortBy = validateCustomerSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);
        
        return validSortDir.equals("desc") ? 
                Sort.by(validSortBy).descending() : 
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Product entity
     */
    public static Sort createProductSort(String sortBy, String sortDir) {
        String validSortBy = validateProductSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);
        
        return validSortDir.equals("desc") ? 
                Sort.by(validSortBy).descending() : 
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Sale entity
     */
    public static Sort createSaleSort(String sortBy, String sortDir) {
        String validSortBy = validateSaleSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Validates and returns a safe sort field for Supplier entity
     */
    public static String validateSupplierSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_SUPPLIER_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "id";
    }

    /**
     * Validates and returns a safe sort field for Return entity
     */
    public static String validateReturnSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_RETURN_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "id";
    }

    /**
     * Validates and returns a safe sort field for Promotion entity
     */
    public static String validatePromotionSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_PROMOTION_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "id";
    }

    /**
     * Validates and returns a safe sort field for Warehouse entity
     */
    public static String validateWarehouseSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_WAREHOUSE_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "id";
    }

    /**
     * Validates and returns a safe sort field for StockMovement entity
     */
    public static String validateStockMovementSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "id";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_STOCK_MOVEMENT_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "id";
    }

    /**
     * Validates and returns a safe sort field for Category entity
     */
    public static String validateCategorySortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "displayOrder";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_CATEGORY_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "displayOrder";
    }

    /**
     * Validates pagination parameters
     */
    public static class PaginationParams {
        public final int page;
        public final int size;
        
        public PaginationParams(int page, int size) {
            this.page = Math.max(0, page); // Ensure page is not negative
            this.size = Math.min(Math.max(1, size), 100); // Ensure size is between 1 and 100
        }
    }

    /**
     * Creates validated pagination parameters
     */
    public static PaginationParams validatePaginationParams(int page, int size) {
        return new PaginationParams(page, size);
    }

    /**
     * Creates a safe Sort object for Supplier entity
     */
    public static Sort createSupplierSort(String sortBy, String sortDir) {
        String validSortBy = validateSupplierSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Return entity
     */
    public static Sort createReturnSort(String sortBy, String sortDir) {
        String validSortBy = validateReturnSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Promotion entity
     */
    public static Sort createPromotionSort(String sortBy, String sortDir) {
        String validSortBy = validatePromotionSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Warehouse entity
     */
    public static Sort createWarehouseSort(String sortBy, String sortDir) {
        String validSortBy = validateWarehouseSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for StockMovement entity
     */
    public static Sort createStockMovementSort(String sortBy, String sortDir) {
        String validSortBy = validateStockMovementSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Creates a safe Sort object for Category entity
     */
    public static Sort createCategorySort(String sortBy, String sortDir) {
        String validSortBy = validateCategorySortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Validates and returns a safe sort field for Inventory entity
     */
    public static String validateInventorySortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "name";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_INVENTORY_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "name";
    }

    /**
     * Creates a safe Sort object for Inventory entity
     */
    public static Sort createInventorySort(String sortBy, String sortDir) {
        String validSortBy = validateInventorySortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }

    /**
     * Validates and returns a safe sort field for PurchaseOrder entity
     */
    public static String validatePurchaseOrderSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "orderDate";
        }

        String cleanSortBy = sortBy.trim().toLowerCase();

        for (String validField : VALID_PURCHASE_ORDER_SORT_FIELDS) {
            if (validField.toLowerCase().equals(cleanSortBy)) {
                return validField;
            }
        }

        return "orderDate";
    }

    /**
     * Creates a safe Sort object for PurchaseOrder entity
     */
    public static Sort createPurchaseOrderSort(String sortBy, String sortDir) {
        String validSortBy = validatePurchaseOrderSortField(sortBy);
        String validSortDir = validateSortDirection(sortDir);

        return validSortDir.equals("desc") ?
                Sort.by(validSortBy).descending() :
                Sort.by(validSortBy).ascending();
    }
}
