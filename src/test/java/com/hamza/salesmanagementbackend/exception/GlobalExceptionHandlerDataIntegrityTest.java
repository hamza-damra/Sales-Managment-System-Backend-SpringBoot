package com.hamza.salesmanagementbackend.exception;

import com.hamza.salesmanagementbackend.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerDataIntegrityTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleDataIntegrityException_SaleHasReturns() {
        // Given
        DataIntegrityException exception = DataIntegrityException.saleHasReturns(123L, 3);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Data Integrity Violation", errorResponse.getError());
        assertEquals("Cannot delete sale because it has 3 associated returns", errorResponse.getMessage());
        assertEquals("SALE_HAS_RETURNS", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("process or cancel all associated returns"));
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("Sale", errorResponse.getDetails().get("resourceType"));
        assertEquals(123L, errorResponse.getDetails().get("resourceId"));
        assertEquals("Returns", errorResponse.getDetails().get("dependentResource"));
    }

    @Test
    void handleDataIntegrityException_CustomerHasSales() {
        // Given
        DataIntegrityException exception = DataIntegrityException.customerHasSales(456L, 5);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot delete customer because they have 5 associated sales", errorResponse.getMessage());
        assertEquals("CUSTOMER_HAS_SALES", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("complete, cancel, or reassign all customer sales"));
    }

    @Test
    void handleDataIntegrityViolationException_SaleReturnsConstraint() {
        // Given
        String constraintMessage = "Cannot delete or update a parent row: a foreign key constraint fails " +
                "(`sales_management`.`returns`, CONSTRAINT `FKeyaqjk5heqbphujdvhu14rpij` " +
                "FOREIGN KEY (`original_sale_id`) REFERENCES `sales` (`id`))";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(constraintMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Data Integrity Violation", errorResponse.getError());
        assertEquals("Cannot delete sale because it has associated returns.", errorResponse.getMessage());
        assertEquals("SALE_HAS_RETURNS", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("process or cancel all associated returns"));
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("DELETE", errorResponse.getDetails().get("operation"));
        assertEquals("FOREIGN_KEY", errorResponse.getDetails().get("constraint"));
    }

    @Test
    void handleDataIntegrityViolationException_CustomerSalesConstraint() {
        // Given
        String constraintMessage = "Cannot delete or update a parent row: a foreign key constraint fails " +
                "(`sales_management`.`sales`, CONSTRAINT `FK_customer_sales` " +
                "FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`))";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(constraintMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot delete customer because they have associated sales.", errorResponse.getMessage());
        assertEquals("CUSTOMER_HAS_SALES", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("complete, cancel, or reassign all customer sales"));
    }

    @Test
    void handleDataIntegrityViolationException_ProductSaleItemsConstraint() {
        // Given
        String constraintMessage = "Cannot delete or update a parent row: a foreign key constraint fails " +
                "(`sales_management`.`sale_items`, CONSTRAINT `FK_product_sale_items` " +
                "FOREIGN KEY (`product_id`) REFERENCES `products` (`id`))";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(constraintMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot delete product because it appears in sales records.", errorResponse.getMessage());
        assertEquals("PRODUCT_HAS_SALES", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("marking it as inactive"));
    }

    @Test
    void handleDataIntegrityViolationException_CategoryProductsConstraint() {
        // Given
        String constraintMessage = "Cannot delete or update a parent row: a foreign key constraint fails " +
                "(`sales_management`.`products`, CONSTRAINT `FK_category_products` " +
                "FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`))";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(constraintMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot delete category because it contains products.", errorResponse.getMessage());
        assertEquals("CATEGORY_HAS_PRODUCTS", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("move all products to another category"));
    }

    @Test
    void handleDataIntegrityViolationException_GenericConstraint() {
        // Given
        String constraintMessage = "Some unknown foreign key constraint violation";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(constraintMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot perform this operation due to existing data dependencies.", errorResponse.getMessage());
        assertEquals("DATABASE_CONSTRAINT_VIOLATION", errorResponse.getErrorCode());
        assertTrue(errorResponse.getSuggestions().contains("remove or reassign dependent records"));
    }

    @Test
    void handleDataIntegrityViolationException_NullMessage() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException(null);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Cannot perform this operation due to existing data dependencies.", errorResponse.getMessage());
        assertEquals("DATABASE_CONSTRAINT_VIOLATION", errorResponse.getErrorCode());
    }
}
