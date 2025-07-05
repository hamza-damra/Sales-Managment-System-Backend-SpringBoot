package com.hamza.salesmanagementbackend.exception;

import com.hamza.salesmanagementbackend.entity.Customer;
import com.hamza.salesmanagementbackend.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private MethodArgumentTypeMismatchException methodArgumentTypeMismatchException;

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    void handleResourceNotFoundException_Success() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Customer not found with id: 1");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertEquals("Customer not found with id: 1", response.getBody().getMessage());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleResourceNotFoundException_WithDetails_Success() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Customer", "id", "1");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource Not Found", response.getBody().getError());
        assertNotNull(response.getBody().getDetails());
        assertEquals("Customer", response.getBody().getDetails().get("resourceType"));
        assertEquals("id", response.getBody().getDetails().get("searchField"));
        assertEquals("1", response.getBody().getDetails().get("searchValue"));
    }

    @Test
    void handleBusinessLogicException_Success() {
        // Given - Fixed constructor parameter order: errorCode, userMessage, technicalMessage
        BusinessLogicException exception = new BusinessLogicException(
                "EMAIL_ALREADY_EXISTS", "Email already exists", "Email validation failed");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessLogicException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Business Rule Violation", response.getBody().getError());
        assertEquals("Email already exists", response.getBody().getMessage()); // Fixed: Now getUserMessage() returns the correct user message
        assertEquals("EMAIL_ALREADY_EXISTS", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handlePropertyReferenceException_Success() {
        // Given - Create PropertyReferenceException directly without using PropertyPath.from()
        TypeInformation<Customer> typeInfo = TypeInformation.of(Customer.class);
        PropertyReferenceException exception = new PropertyReferenceException(
                "invalidField", typeInfo, java.util.Collections.emptyList());

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePropertyReferenceException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Sort Parameter", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Invalid sort field 'invalidField' for Customer"));
        assertEquals("INVALID_SORT_FIELD", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getTimestamp());
        assertTrue(response.getBody().getSuggestions().contains("valid sort fields"));
    }

    @Test
    void handleMethodArgumentNotValidException_Success() {
        // Given
        FieldError fieldError1 = new FieldError("customer", "email", "Email is required");
        FieldError fieldError2 = new FieldError("customer", "name", "Name cannot be empty");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("Email is required", response.getBody().getValidationErrors().get("email"));
        assertEquals("Name cannot be empty", response.getBody().getValidationErrors().get("name"));
    }

    @Test
    void handleMethodArgumentTypeMismatchException_Success() {
        // Given
        when(methodArgumentTypeMismatchException.getName()).thenReturn("page");
        when(methodArgumentTypeMismatchException.getValue()).thenReturn("invalid");
        when(methodArgumentTypeMismatchException.getRequiredType()).thenReturn((Class) Integer.class);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTypeMismatchException(methodArgumentTypeMismatchException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Parameter Type", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Invalid value 'invalid' for parameter 'page'"));
        assertEquals("INVALID_PARAMETER_TYPE", response.getBody().getErrorCode());
    }

    @Test
    void handleGenericException_Success() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
        // Fixed: The actual message is a generic message, not the exception message
        assertTrue(response.getBody().getMessage().contains("unexpected error occurred") ||
                   response.getBody().getMessage().contains("An unexpected error occurred"));
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleIllegalArgumentException_Success() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid enum value: INVALID_STATUS");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Request", response.getBody().getError()); // Fixed: Actual error message from handler
        assertTrue(response.getBody().getMessage().contains("Invalid enum value: INVALID_STATUS"));
        assertEquals("INVALID_ARGUMENT", response.getBody().getErrorCode());
    }

    @Test
    void propertyReferenceExceptionHandling_PreventsSortingErrors() {
        // This test verifies that PropertyReferenceException is properly handled
        // to prevent sorting parameter errors from causing 500 errors

        // Test with different entity types - Create exception directly
        TypeInformation<Customer> customerType = TypeInformation.of(Customer.class);
        PropertyReferenceException customerException = new PropertyReferenceException(
                "invalidCustomerField", customerType, java.util.Collections.emptyList());

        ResponseEntity<ErrorResponse> customerResponse = globalExceptionHandler.handlePropertyReferenceException(customerException);

        assertEquals(HttpStatus.BAD_REQUEST, customerResponse.getStatusCode());
        assertTrue(customerResponse.getBody().getMessage().contains("Customer"));
        assertEquals("INVALID_SORT_FIELD", customerResponse.getBody().getErrorCode());
    }

    @Test
    void errorResponse_ContainsAllRequiredFields() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getStatus());
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getErrorCode());
        assertNotNull(errorResponse.getTimestamp());
        assertNotNull(errorResponse.getSuggestions());
        
        // Verify status code matches HTTP status
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
    }

    @Test
    void errorResponse_SuggestionsAreHelpful() {
        // Test that error responses contain helpful suggestions

        // PropertyReferenceException should suggest valid fields - Create exception directly
        TypeInformation<Customer> typeInfo = TypeInformation.of(Customer.class);
        PropertyReferenceException propException = new PropertyReferenceException(
                "badField", typeInfo, java.util.Collections.emptyList());

        ResponseEntity<ErrorResponse> propResponse = globalExceptionHandler.handlePropertyReferenceException(propException);
        assertTrue(propResponse.getBody().getSuggestions().contains("valid sort fields"));

        // BusinessLogicException should suggest reviewing requirements
        BusinessLogicException bizException = new BusinessLogicException("Business rule violated");
        ResponseEntity<ErrorResponse> bizResponse = globalExceptionHandler.handleBusinessLogicException(bizException);
        assertTrue(bizResponse.getBody().getSuggestions().contains("review the requirements"));

        // ResourceNotFoundException should suggest verification
        ResourceNotFoundException resException = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> resResponse = globalExceptionHandler.handleResourceNotFoundException(resException);
        assertTrue(resResponse.getBody().getSuggestions().contains("verify the provided information"));
    }
}
