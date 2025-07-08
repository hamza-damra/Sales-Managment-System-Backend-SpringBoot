package com.hamza.salesmanagementbackend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataIntegrityExceptionTest {

    @Test
    void testSaleHasReturnsException() {
        // Given
        Long saleId = 1L;
        int returnCount = 3;

        // When
        DataIntegrityException exception = DataIntegrityException.saleHasReturns(saleId, returnCount);

        // Then
        assertEquals("Sale", exception.getResourceType());
        assertEquals(saleId, exception.getResourceId());
        assertEquals("Returns", exception.getDependentResource());
        assertEquals("Cannot delete sale because it has 3 associated returns", exception.getUserMessage());
        assertEquals("SALE_HAS_RETURNS", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("process or cancel all associated returns"));
    }

    @Test
    void testSaleHasReturnsSingular() {
        // Given
        Long saleId = 1L;
        int returnCount = 1;

        // When
        DataIntegrityException exception = DataIntegrityException.saleHasReturns(saleId, returnCount);

        // Then
        assertEquals("Cannot delete sale because it has 1 associated return", exception.getUserMessage());
    }

    @Test
    void testCustomerHasSalesException() {
        // Given
        Long customerId = 2L;
        int salesCount = 5;

        // When
        DataIntegrityException exception = DataIntegrityException.customerHasSales(customerId, salesCount);

        // Then
        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Sales", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 5 associated sales", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_SALES", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("complete, cancel, or reassign all customer sales"));
    }

    @Test
    void testCustomerHasReturnsException() {
        // Given
        Long customerId = 3L;
        int returnCount = 2;

        // When
        DataIntegrityException exception = DataIntegrityException.customerHasReturns(customerId, returnCount);

        // Then
        assertEquals("Customer", exception.getResourceType());
        assertEquals(customerId, exception.getResourceId());
        assertEquals("Returns", exception.getDependentResource());
        assertEquals("Cannot delete customer because they have 2 associated returns", exception.getUserMessage());
        assertEquals("CUSTOMER_HAS_RETURNS", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("process all customer returns"));
    }

    @Test
    void testProductHasSaleItemsException() {
        // Given
        Long productId = 4L;
        int saleItemCount = 10;

        // When
        DataIntegrityException exception = DataIntegrityException.productHasSaleItems(productId, saleItemCount);

        // Then
        assertEquals("Product", exception.getResourceType());
        assertEquals(productId, exception.getResourceId());
        assertEquals("Sale Items", exception.getDependentResource());
        assertEquals("Cannot delete product because it appears in 10 sale records", exception.getUserMessage());
        assertEquals("PRODUCT_HAS_SALE_ITEMS", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("marking it as inactive"));
    }

    @Test
    void testProductHasReturnItemsException() {
        // Given
        Long productId = 5L;
        int returnItemCount = 1;

        // When
        DataIntegrityException exception = DataIntegrityException.productHasReturnItems(productId, returnItemCount);

        // Then
        assertEquals("Product", exception.getResourceType());
        assertEquals(productId, exception.getResourceId());
        assertEquals("Return Items", exception.getDependentResource());
        assertEquals("Cannot delete product because it appears in 1 return record", exception.getUserMessage());
        assertEquals("PRODUCT_HAS_RETURN_ITEMS", exception.getErrorCode());
    }

    @Test
    void testCategoryHasProductsException() {
        // Given
        Long categoryId = 6L;
        int productCount = 7;

        // When
        DataIntegrityException exception = DataIntegrityException.categoryHasProducts(categoryId, productCount);

        // Then
        assertEquals("Category", exception.getResourceType());
        assertEquals(categoryId, exception.getResourceId());
        assertEquals("Products", exception.getDependentResource());
        assertEquals("Cannot delete category because it contains 7 products", exception.getUserMessage());
        assertEquals("CATEGORY_HAS_PRODUCTS", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("move all products to another category"));
    }

    @Test
    void testSupplierHasPurchaseOrdersException() {
        // Given
        Long supplierId = 7L;
        int orderCount = 3;

        // When
        DataIntegrityException exception = DataIntegrityException.supplierHasPurchaseOrders(supplierId, orderCount);

        // Then
        assertEquals("Supplier", exception.getResourceType());
        assertEquals(supplierId, exception.getResourceId());
        assertEquals("Purchase Orders", exception.getDependentResource());
        assertEquals("Cannot delete supplier because they have 3 active purchase orders", exception.getUserMessage());
        assertEquals("SUPPLIER_HAS_PURCHASE_ORDERS", exception.getErrorCode());
        assertTrue(exception.getSuggestion().contains("complete or cancel all purchase orders"));
    }

    @Test
    void testCustomConstructor() {
        // Given
        String resourceType = "TestResource";
        Long resourceId = 8L;
        String dependentResource = "TestDependency";
        String userMessage = "Custom test message";
        String errorCode = "CUSTOM_ERROR";
        String suggestion = "Custom suggestion";

        // When
        DataIntegrityException exception = new DataIntegrityException(
                resourceType, resourceId, dependentResource, userMessage, errorCode, suggestion);

        // Then
        assertEquals(resourceType, exception.getResourceType());
        assertEquals(resourceId, exception.getResourceId());
        assertEquals(dependentResource, exception.getDependentResource());
        assertEquals(userMessage, exception.getUserMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(suggestion, exception.getSuggestion());
    }

    @Test
    void testGeneratedSuggestionForUnknownResource() {
        // Given
        String resourceType = "UnknownResource";
        Long resourceId = 9L;
        String dependentResource = "UnknownDependency";
        String userMessage = "Test message";

        // When
        DataIntegrityException exception = new DataIntegrityException(
                resourceType, resourceId, dependentResource, userMessage);

        // Then
        assertEquals("Please remove or reassign all dependent records before deletion.", exception.getSuggestion());
    }
}
