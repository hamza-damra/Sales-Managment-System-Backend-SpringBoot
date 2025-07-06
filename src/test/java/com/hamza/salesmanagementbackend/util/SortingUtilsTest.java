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
    }

    // Tests for new Supplier sorting methods
    @Test
    void validateSupplierSortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validateSupplierSortField("id"));
        assertEquals("name", SortingUtils.validateSupplierSortField("name"));
        assertEquals("email", SortingUtils.validateSupplierSortField("email"));
        assertEquals("rating", SortingUtils.validateSupplierSortField("rating"));
        assertEquals("totalAmount", SortingUtils.validateSupplierSortField("totalAmount"));
    }

    @Test
    void validateSupplierSortField_InvalidField_ReturnsId() {
        assertEquals("id", SortingUtils.validateSupplierSortField("invalidField"));
        assertEquals("id", SortingUtils.validateSupplierSortField("nonExistentField"));
        assertEquals("id", SortingUtils.validateSupplierSortField(null));
        assertEquals("id", SortingUtils.validateSupplierSortField(""));
    }

    @Test
    void createSupplierSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createSupplierSort("name", "asc");
        assertEquals("name: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createSupplierSort("rating", "desc");
        assertEquals("rating: DESC", descSort.toString());
    }

    @Test
    void createSupplierSort_InvalidParameters_ReturnsDefaultSort() {
        Sort invalidFieldSort = SortingUtils.createSupplierSort("invalidField", "asc");
        assertEquals("id: ASC", invalidFieldSort.toString());

        Sort invalidDirectionSort = SortingUtils.createSupplierSort("name", "invalid");
        assertEquals("name: ASC", invalidDirectionSort.toString());
    }

    // Tests for new Return sorting methods
    @Test
    void validateReturnSortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validateReturnSortField("id"));
        assertEquals("returnDate", SortingUtils.validateReturnSortField("returnDate"));
        assertEquals("status", SortingUtils.validateReturnSortField("status"));
        assertEquals("totalRefundAmount", SortingUtils.validateReturnSortField("totalRefundAmount"));
    }

    @Test
    void validateReturnSortField_InvalidField_ReturnsId() {
        assertEquals("id", SortingUtils.validateReturnSortField("invalidField"));
        assertEquals("id", SortingUtils.validateReturnSortField(null));
        assertEquals("id", SortingUtils.validateReturnSortField(""));
    }

    @Test
    void createReturnSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createReturnSort("returnDate", "asc");
        assertEquals("returnDate: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createReturnSort("status", "desc");
        assertEquals("status: DESC", descSort.toString());
    }

    // Tests for new Promotion sorting methods
    @Test
    void validatePromotionSortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validatePromotionSortField("id"));
        assertEquals("name", SortingUtils.validatePromotionSortField("name"));
        assertEquals("startDate", SortingUtils.validatePromotionSortField("startDate"));
        assertEquals("endDate", SortingUtils.validatePromotionSortField("endDate"));
        assertEquals("discountValue", SortingUtils.validatePromotionSortField("discountValue"));
    }

    @Test
    void validatePromotionSortField_InvalidField_ReturnsId() {
        assertEquals("id", SortingUtils.validatePromotionSortField("invalidField"));
        assertEquals("id", SortingUtils.validatePromotionSortField(null));
        assertEquals("id", SortingUtils.validatePromotionSortField(""));
    }

    @Test
    void createPromotionSort_ValidParameters_ReturnsCorrectSort() {
        Sort ascSort = SortingUtils.createPromotionSort("name", "asc");
        assertEquals("name: ASC", ascSort.toString());

        Sort descSort = SortingUtils.createPromotionSort("startDate", "desc");
        assertEquals("startDate: DESC", descSort.toString());
    }

    // Tests for PropertyReferenceException prevention
    @Test
    void sortingUtils_PreventPropertyReferenceException() {
        // Test that invalid sort fields are safely handled and don't cause PropertyReferenceException

        // Customer sorting with invalid fields
        Sort customerSort = SortingUtils.createCustomerSort("nonExistentField", "asc");
        assertEquals("id: ASC", customerSort.toString());

        // Product sorting with invalid fields
        Sort productSort = SortingUtils.createProductSort("invalidProperty", "desc");
        assertEquals("id: DESC", productSort.toString());

        // Sale sorting with invalid fields
        Sort saleSort = SortingUtils.createSaleSort("badField", "asc");
        assertEquals("id: ASC", saleSort.toString());

        // Supplier sorting with invalid fields
        Sort supplierSort = SortingUtils.createSupplierSort("wrongField", "desc");
        assertEquals("id: DESC", supplierSort.toString());

        // Return sorting with invalid fields
        Sort returnSort = SortingUtils.createReturnSort("invalidField", "asc");
        assertEquals("id: ASC", returnSort.toString());

        // Promotion sorting with invalid fields
        Sort promotionSort = SortingUtils.createPromotionSort("badProperty", "desc");
        assertEquals("id: DESC", promotionSort.toString());
    }

    @Test
    void sortingUtils_CaseInsensitiveValidation() {
        // Test that field validation is case-insensitive
        assertEquals("name", SortingUtils.validateCustomerSortField("NAME"));
        assertEquals("name", SortingUtils.validateCustomerSortField("Name"));
        assertEquals("email", SortingUtils.validateSupplierSortField("EMAIL"));
        assertEquals("returnDate", SortingUtils.validateReturnSortField("RETURNDATE"));
        assertEquals("startDate", SortingUtils.validatePromotionSortField("STARTDATE"));
    }

    @Test
    void sortingUtils_DirectionValidation() {
        // Test that sort direction validation works correctly
        assertEquals("asc", SortingUtils.validateSortDirection("ASC"));
        assertEquals("desc", SortingUtils.validateSortDirection("DESC"));
        assertEquals("asc", SortingUtils.validateSortDirection("Asc"));
        assertEquals("desc", SortingUtils.validateSortDirection("Desc"));
        assertEquals("asc", SortingUtils.validateSortDirection("invalid"));
        assertEquals("asc", SortingUtils.validateSortDirection(null));
        assertEquals("asc", SortingUtils.validateSortDirection(""));
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

    // Category sorting tests
    @Test
    void validateCategorySortField_ValidField_ReturnsField() {
        assertEquals("id", SortingUtils.validateCategorySortField("id"));
        assertEquals("name", SortingUtils.validateCategorySortField("name"));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("displayOrder"));
        assertEquals("status", SortingUtils.validateCategorySortField("status"));
        assertEquals("createdAt", SortingUtils.validateCategorySortField("createdAt"));
        assertEquals("updatedAt", SortingUtils.validateCategorySortField("updatedAt"));
    }

    @Test
    void validateCategorySortField_InvalidField_ReturnsDisplayOrder() {
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("invalidField"));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("123"));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("randomString"));
    }

    @Test
    void validateCategorySortField_NullOrEmpty_ReturnsDisplayOrder() {
        assertEquals("displayOrder", SortingUtils.validateCategorySortField(null));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField(""));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("   "));
    }

    @Test
    void validateCategorySortField_CaseInsensitive_ReturnsCorrectField() {
        assertEquals("name", SortingUtils.validateCategorySortField("NAME"));
        assertEquals("displayOrder", SortingUtils.validateCategorySortField("DisplayOrder"));
        assertEquals("status", SortingUtils.validateCategorySortField("STATUS"));
    }

    @Test
    void createCategorySort_AscendingOrder() {
        Sort sort = SortingUtils.createCategorySort("name", "asc");

        assertNotNull(sort);
        assertTrue(sort.isSorted());
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("name").getDirection());
        assertEquals("name", sort.getOrderFor("name").getProperty());
    }

    @Test
    void createCategorySort_DescendingOrder() {
        Sort sort = SortingUtils.createCategorySort("displayOrder", "desc");

        assertNotNull(sort);
        assertTrue(sort.isSorted());
        assertEquals(Sort.Direction.DESC, sort.getOrderFor("displayOrder").getDirection());
        assertEquals("displayOrder", sort.getOrderFor("displayOrder").getProperty());
    }

    @Test
    void createCategorySort_DefaultValues() {
        Sort sort = SortingUtils.createCategorySort(null, null);

        assertNotNull(sort);
        assertTrue(sort.isSorted());
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("displayOrder").getDirection());
        assertEquals("displayOrder", sort.getOrderFor("displayOrder").getProperty());
    }

    @Test
    void createCategorySort_InvalidValues() {
        Sort sort = SortingUtils.createCategorySort("invalidField", "invalidDirection");

        assertNotNull(sort);
        assertTrue(sort.isSorted());
        assertEquals(Sort.Direction.ASC, sort.getOrderFor("displayOrder").getDirection());
        assertEquals("displayOrder", sort.getOrderFor("displayOrder").getProperty());
    }

    @Test
    void validCategorySortFields_ContainExpectedFields() {
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("id"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("name"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("description"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("displayOrder"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("status"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("createdAt"));
        assertTrue(SortingUtils.VALID_CATEGORY_SORT_FIELDS.contains("updatedAt"));
    }
}
