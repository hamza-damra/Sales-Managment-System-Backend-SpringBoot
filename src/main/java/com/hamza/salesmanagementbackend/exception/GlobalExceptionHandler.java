package com.hamza.salesmanagementbackend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Resource Not Found")
                .message(ex.getMessage())
                .errorCode("RESOURCE_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .suggestions("Please verify the provided information and try again. If the problem persists, contact support.")
                .build();

        if (ex.getResourceType() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("resourceType", ex.getResourceType());
            details.put("searchField", ex.getField());
            details.put("searchValue", ex.getValue());
            errorResponse.setDetails(details);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(BusinessLogicException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getUserMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .suggestions("Please review the requirements and adjust your input accordingly.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Insufficient Stock")
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .suggestions("Please reduce the quantity, choose a different product, or check back later for restocked items.")
                .build();

        if (ex.getProductName() != null) {
            Map<String, Object> details = new HashMap<>();
            details.put("productName", ex.getProductName());
            details.put("availableStock", ex.getAvailableStock());
            details.put("requestedQuantity", ex.getRequestedQuantity());
            details.put("shortfall", ex.getRequestedQuantity() - ex.getAvailableStock());
            errorResponse.setDetails(details);
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityException(DataIntegrityException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(ex.getUserMessage())
                .errorCode(ex.getErrorCode())
                .timestamp(LocalDateTime.now())
                .suggestions(ex.getSuggestion())
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("resourceType", ex.getResourceType());
        details.put("resourceId", ex.getResourceId());
        details.put("dependentResource", ex.getDependentResource());
        errorResponse.setDetails(details);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String userFriendlyMessage = "Cannot perform this operation due to existing data dependencies.";
        String suggestion = "Please remove or reassign dependent records before attempting this operation.";
        String errorCode = "DATABASE_CONSTRAINT_VIOLATION";

        // Try to extract meaningful information from the exception message
        String originalMessage = ex.getMessage();
        if (originalMessage != null) {
            if (originalMessage.contains("foreign key constraint")) {
                if (originalMessage.contains("returns") && originalMessage.contains("original_sale_id")) {
                    userFriendlyMessage = "Cannot delete sale because it has associated returns.";
                    suggestion = "Please process or cancel all associated returns before deleting this sale.";
                    errorCode = "SALE_HAS_RETURNS";
                } else if (originalMessage.contains("sales") && originalMessage.contains("customer_id")) {
                    userFriendlyMessage = "Cannot delete customer because they have associated sales.";
                    suggestion = "Please complete, cancel, or reassign all customer sales before deleting this customer.";
                    errorCode = "CUSTOMER_HAS_SALES";
                } else if (originalMessage.contains("sale_items") && originalMessage.contains("product_id")) {
                    userFriendlyMessage = "Cannot delete product because it appears in sales records.";
                    suggestion = "This product has been sold and cannot be deleted. Consider marking it as inactive instead.";
                    errorCode = "PRODUCT_HAS_SALES";
                } else if (originalMessage.contains("products") && originalMessage.contains("category_id")) {
                    userFriendlyMessage = "Cannot delete category because it contains products.";
                    suggestion = "Please move all products to another category or delete them before removing this category.";
                    errorCode = "CATEGORY_HAS_PRODUCTS";
                }
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(userFriendlyMessage)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .suggestions(suggestion)
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("operation", "DELETE");
        details.put("constraint", "FOREIGN_KEY");
        errorResponse.setDetails(details);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing + "; " + replacement
                ));

        String mainMessage = fieldErrors.size() == 1
            ? "There is 1 validation error that needs to be corrected:"
            : String.format("There are %d validation errors that need to be corrected:", fieldErrors.size());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message(mainMessage)
                .errorCode("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .validationErrors(fieldErrors)
                .suggestions("Please correct the highlighted fields and submit again. All required fields must be properly filled.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String providedValue = ex.getValue() != null ? ex.getValue().toString() : "null";

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected a valid %s.",
                providedValue, parameterName, expectedType.toLowerCase());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Parameter Type")
                .message(message)
                .errorCode("INVALID_PARAMETER_TYPE")
                .timestamp(LocalDateTime.now())
                .suggestions("Please ensure all parameters are in the correct format. Check the API documentation for expected data types.")
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("parameterName", parameterName);
        details.put("expectedType", expectedType);
        details.put("providedValue", providedValue);
        errorResponse.setDetails(details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReferenceException(PropertyReferenceException ex) {
        String propertyName = ex.getPropertyName();
        String entityType = ex.getType().getType().getSimpleName();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Sort Parameter")
                .message(String.format("Invalid sort field '%s' for %s. Please use a valid field name.", propertyName, entityType))
                .errorCode("INVALID_SORT_FIELD")
                .timestamp(LocalDateTime.now())
                .suggestions("Please check the API documentation for valid sort fields or use 'id' as a default sort field.")
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("invalidField", propertyName);
        details.put("entityType", entityType);
        details.put("availableFields", getSuggestedFields(entityType));
        errorResponse.setDetails(details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Invalid JSON format in request body";
        if (ex.getMessage() != null && ex.getMessage().contains("JSON parse error")) {
            message = "The request body contains malformed JSON. Please check the syntax and try again.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Malformed JSON")
                .message(message)
                .errorCode("MALFORMED_JSON")
                .timestamp(LocalDateTime.now())
                .suggestions("Please ensure the request body is valid JSON format. Check for missing quotes, brackets, or commas.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        String supportedTypes = ex.getSupportedMediaTypes().stream()
                .map(mediaType -> mediaType.toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("application/json");

        String message = String.format("Content-Type '%s' is not supported. Supported types: %s",
                ex.getContentType(), supportedTypes);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .error("Unsupported Media Type")
                .message(message)
                .errorCode("UNSUPPORTED_MEDIA_TYPE")
                .timestamp(LocalDateTime.now())
                .suggestions("Please set the Content-Type header to 'application/json' and ensure the request body is valid JSON.")
                .build();

        Map<String, Object> details = new HashMap<>();
        details.put("providedContentType", ex.getContentType() != null ? ex.getContentType().toString() : "unknown");
        details.put("supportedContentTypes", ex.getSupportedMediaTypes());
        errorResponse.setDetails(details);

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message("The request contains invalid parameters: " + ex.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .timestamp(LocalDateTime.now())
                .suggestions("Please verify your request parameters and ensure they meet the required format and constraints.")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred while processing your request. Our technical team has been notified.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .suggestions("Please try again in a few moments. If the problem persists, contact our support team with the error details.")
                .build();

        // Log the actual exception for debugging (in production, use proper logging)
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Helper method to provide suggested valid fields based on entity type
     */
    private java.util.List<String> getSuggestedFields(String entityType) {
        switch (entityType.toLowerCase()) {
            case "customer":
                return java.util.List.of("id", "name", "firstName", "lastName", "email", "createdAt", "updatedAt");
            case "product":
                return java.util.List.of("id", "name", "price", "stockQuantity", "category", "sku", "createdAt", "updatedAt");
            case "sale":
                return java.util.List.of("id", "saleDate", "totalAmount", "status", "paymentMethod", "createdAt", "updatedAt");
            case "supplier":
                return java.util.List.of("id", "name", "email", "phone", "rating", "status", "createdAt", "updatedAt");
            case "return":
                return java.util.List.of("id", "returnDate", "reason", "status", "totalRefundAmount", "createdAt", "updatedAt");
            case "promotion":
                return java.util.List.of("id", "name", "type", "discountValue", "startDate", "endDate", "isActive", "createdAt", "updatedAt");
            case "warehouse":
                return java.util.List.of("id", "name", "code", "address", "city", "isActive", "createdAt", "updatedAt");
            case "stockmovement":
                return java.util.List.of("id", "movementType", "quantity", "date", "reference", "createdAt");
            default:
                return java.util.List.of("id", "createdAt", "updatedAt");
        }
    }

    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private LocalDateTime timestamp;
        private String suggestions;
        private Map<String, String> validationErrors;
        private Map<String, Object> details;

        // Builder pattern for easy construction
        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Constructors
        public ErrorResponse() {}

        public ErrorResponse(int status, String error, String message, String errorCode,
                           LocalDateTime timestamp, String suggestions) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.errorCode = errorCode;
            this.timestamp = timestamp;
            this.suggestions = suggestions;
        }

        // Getters and setters
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getSuggestions() { return suggestions; }
        public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

        public Map<String, String> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }

        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }

        public static class ErrorResponseBuilder {
            private ErrorResponse errorResponse = new ErrorResponse();

            public ErrorResponseBuilder status(int status) {
                errorResponse.status = status;
                return this;
            }

            public ErrorResponseBuilder error(String error) {
                errorResponse.error = error;
                return this;
            }

            public ErrorResponseBuilder message(String message) {
                errorResponse.message = message;
                return this;
            }

            public ErrorResponseBuilder errorCode(String errorCode) {
                errorResponse.errorCode = errorCode;
                return this;
            }

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
                errorResponse.timestamp = timestamp;
                return this;
            }

            public ErrorResponseBuilder suggestions(String suggestions) {
                errorResponse.suggestions = suggestions;
                return this;
            }

            public ErrorResponseBuilder validationErrors(Map<String, String> validationErrors) {
                errorResponse.validationErrors = validationErrors;
                return this;
            }

            public ErrorResponseBuilder details(Map<String, Object> details) {
                errorResponse.details = details;
                return this;
            }

            public ErrorResponse build() {
                return errorResponse;
            }
        }
    }
}
