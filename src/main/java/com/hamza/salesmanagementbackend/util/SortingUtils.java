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
}
