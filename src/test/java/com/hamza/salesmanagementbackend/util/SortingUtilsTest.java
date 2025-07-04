package com.hamza.salesmanagementbackend.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;

class SortingUtilsTest {

    @Test
    void validateCustomerSortField_ValidField_ReturnsField() {
        // Test valid fields
        assertEquals("id", SortingUtils.validateCustomerSortField("id"));
        assertEquals("name", SortingUtils.validateCustomerSortField("name"));
        assertEquals("email", SortingUtils.validateCustomerSortField("email"));
        assertEquals("createdAt", SortingUtils.validateCustomerSortField("createdAt"));
    }

    @Test
    void validateCustomerSortField_InvalidField_ReturnsId() {
        // Test invalid fields
        assertEquals("id", SortingUtils.validateCustomerSortField("invalidField"));
        assertEquals("id", SortingUtils.validateCustomerSortField("123"));
        assertEquals("id", SortingUtils.validateCustomerSortField("randomString"));
    }

    @Test
    void validateCustomerSortField_NullOrEmpty_ReturnsId() {
        assertEquals("id", SortingUtils.validateCustomerSortField(null));
        assertEquals("id", SortingUtils.validateCustomerSortField(""));
        assertEquals("id", SortingUtils.validateCustomerSortField("   "));
    }

    @Test
    void validateCustomerSortField_CaseInsensitive_ReturnsCorrectField() {
        assertEquals("name", SortingUtils.validateCustomerSortField("NAME"));
        assertEquals("email", SortingUtils.validateCustomerSortField("Email"));
        assertEquals("firstName", SortingUtils.validateCustomerSortField("firstname"));
    }

    @Test
    void validateProductSortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validateProductSortField("id"));
        assertEquals("name", SortingUtils.validateProductSortField("name"));
        assertEquals("price", SortingUtils.validateProductSortField("price"));
        assertEquals("stockQuantity", SortingUtils.validateProductSortField("stockQuantity"));
    }

    @Test
    void validateProductSortField_InvalidField_ReturnsId() {
        assertEquals("id", SortingUtils.validateProductSortField("invalidField"));
        assertEquals("id", SortingUtils.validateProductSortField("456"));
    }

    @Test
    void validateSaleSortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validateSaleSortField("id"));
        assertEquals("saleDate", SortingUtils.validateSaleSortField("saleDate"));
        assertEquals("totalAmount", SortingUtils.validateSaleSortField("totalAmount"));
        assertEquals("status", SortingUtils.validateSaleSortField("status"));
    }

    @Test
    void validateSaleSortField_InvalidField_ReturnsId() {
        assertEquals("id", SortingUtils.validateSaleSortField("invalidField"));
        assertEquals("id", SortingUtils.validateSaleSortField("789"));
    }

    @Test
    void validateSortDirection_ValidDirection_ReturnsDirection() {
        assertEquals("asc", SortingUtils.validateSortDirection("asc"));
        assertEquals("desc", SortingUtils.validateSortDirection("desc"));
        assertEquals("asc", SortingUtils.validateSortDirection("ASC"));
        assertEquals("desc", SortingUtils.validateSortDirection("DESC"));
    }

    @Test
    void validateSortDirection_InvalidDirection_ReturnsAsc() {
        assertEquals("asc", SortingUtils.validateSortDirection("invalid"));
        assertEquals("asc", SortingUtils.validateSortDirection("up"));
        assertEquals("asc", SortingUtils.validateSortDirection("down"));
        assertEquals("asc", SortingUtils.validateSortDirection(null));
        assertEquals("asc", SortingUtils.validateSortDirection(""));
    }

    @Test
    void createCustomerSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createCustomerSort("name", "asc");
        assertEquals("name: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createCustomerSort("email", "desc");
        assertEquals("email: DESC", descSort.toString());
    }

    @Test
    void createCustomerSort_InvalidParameters_ReturnsDefaultSort() {
        Sort invalidFieldSort = SortingUtils.createCustomerSort("invalidField", "asc");
        assertEquals("id: ASC", invalidFieldSort.toString());

        Sort invalidDirectionSort = SortingUtils.createCustomerSort("name", "invalid");
        assertEquals("name: ASC", invalidDirectionSort.toString());
    }

    @Test
    void createProductSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createProductSort("price", "asc");
        assertEquals("price: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createProductSort("stockQuantity", "desc");
        assertEquals("stockQuantity: DESC", descSort.toString());
    }

    @Test
    void createSaleSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createSaleSort("saleDate", "asc");
        assertEquals("saleDate: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createSaleSort("totalAmount", "desc");
        assertEquals("totalAmount: DESC", descSort.toString());
    }

    @Test
    void validatePaginationParams_ValidParams_ReturnsCorrectValues() {
        SortingUtils.PaginationParams params = SortingUtils.validatePaginationParams(2, 20);
        assertEquals(2, params.page);
        assertEquals(20, params.size);
    }

    @Test
    void validatePaginationParams_InvalidParams_ReturnsValidatedValues() {
        // Negative page should become 0
        SortingUtils.PaginationParams negativePageParams = SortingUtils.validatePaginationParams(-1, 10);
        assertEquals(0, negativePageParams.page);
        assertEquals(10, negativePageParams.size);

        // Size 0 should become 1
        SortingUtils.PaginationParams zeroSizeParams = SortingUtils.validatePaginationParams(0, 0);
        assertEquals(0, zeroSizeParams.page);
        assertEquals(1, zeroSizeParams.size);

        // Size > 100 should become 100
        SortingUtils.PaginationParams largeSizeParams = SortingUtils.validatePaginationParams(0, 150);
        assertEquals(0, largeSizeParams.page);
        assertEquals(100, largeSizeParams.size);

        // Negative size should become 1
        SortingUtils.PaginationParams negativeSizeParams = SortingUtils.validatePaginationParams(0, -5);
        assertEquals(0, negativeSizeParams.page);
        assertEquals(1, negativeSizeParams.size);
    }

    @Test
    void validSortFields_ContainExpectedFields() {
        // Test that our valid field sets contain the expected fields
        assertTrue(SortingUtils.VALID_CUSTOMER_SORT_FIELDS.contains("id"));
        assertTrue(SortingUtils.VALID_CUSTOMER_SORT_FIELDS.contains("name"));
        assertTrue(SortingUtils.VALID_CUSTOMER_SORT_FIELDS.contains("email"));
        assertTrue(SortingUtils.VALID_CUSTOMER_SORT_FIELDS.contains("createdAt"));

        assertTrue(SortingUtils.VALID_PRODUCT_SORT_FIELDS.contains("id"));
        assertTrue(SortingUtils.VALID_PRODUCT_SORT_FIELDS.contains("name"));
        assertTrue(SortingUtils.VALID_PRODUCT_SORT_FIELDS.contains("price"));
        assertTrue(SortingUtils.VALID_PRODUCT_SORT_FIELDS.contains("stockQuantity"));

        assertTrue(SortingUtils.VALID_SALE_SORT_FIELDS.contains("id"));
        assertTrue(SortingUtils.VALID_SALE_SORT_FIELDS.contains("saleDate"));
        assertTrue(SortingUtils.VALID_SALE_SORT_FIELDS.contains("totalAmount"));
        assertTrue(SortingUtils.VALID_SALE_SORT_FIELDS.contains("status"));
    }

    @Test
    void validSortDirections_ContainExpectedDirections() {
        assertTrue(SortingUtils.VALID_SORT_DIRECTIONS.contains("asc"));
        assertTrue(SortingUtils.VALID_SORT_DIRECTIONS.contains("desc"));
        assertEquals(2, SortingUtils.VALID_SORT_DIRECTIONS.size());
    }
}
