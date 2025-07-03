package com.hamza.salesmanagementbackend.exception;

public class BusinessLogicException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;

    public BusinessLogicException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
        this.userMessage = message;
    }

    public BusinessLogicException(String errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public BusinessLogicException(String errorCode, String userMessage, String technicalMessage) {
        super(technicalMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    // Predefined business logic exceptions with professional messages
    public static BusinessLogicException duplicateEmail(String email) {
        return new BusinessLogicException(
            "DUPLICATE_EMAIL",
            String.format("An account with email address '%s' already exists. Please use a different email address or contact support if this is your account.", email)
        );
    }

    public static BusinessLogicException duplicateSku(String sku) {
        return new BusinessLogicException(
            "DUPLICATE_SKU",
            String.format("Product with SKU '%s' already exists in the system. Please use a unique SKU for this product.", sku)
        );
    }

    public static BusinessLogicException invalidCustomerName() {
        return new BusinessLogicException(
            "INVALID_CUSTOMER_NAME",
            "Customer name must be at least 2 characters long and cannot be empty. Please provide a valid name."
        );
    }

    public static BusinessLogicException invalidProductName() {
        return new BusinessLogicException(
            "INVALID_PRODUCT_NAME",
            "Product name must be at least 2 characters long and cannot be empty. Please provide a valid product name."
        );
    }

    public static BusinessLogicException invalidPrice() {
        return new BusinessLogicException(
            "INVALID_PRICE",
            "Product price must be greater than zero. Please enter a valid price amount."
        );
    }

    public static BusinessLogicException invalidSkuFormat() {
        return new BusinessLogicException(
            "INVALID_SKU_FORMAT",
            "SKU must be between 3 and 20 characters long. Please provide a valid SKU."
        );
    }

    public static BusinessLogicException negativeStock() {
        return new BusinessLogicException(
            "NEGATIVE_STOCK",
            "Stock quantity cannot be negative. Please enter a valid quantity (0 or greater)."
        );
    }

    public static BusinessLogicException emptySearchTerm() {
        return new BusinessLogicException(
            "EMPTY_SEARCH_TERM",
            "Search term cannot be empty. Please enter a keyword to search."
        );
    }

    public static BusinessLogicException cannotUpdateCompletedSale() {
        return new BusinessLogicException(
            "CANNOT_UPDATE_COMPLETED_SALE",
            "This sale has already been completed and cannot be modified. If changes are needed, please create a new sale or contact your supervisor."
        );
    }

    public static BusinessLogicException cannotDeleteCompletedSale() {
        return new BusinessLogicException(
            "CANNOT_DELETE_COMPLETED_SALE",
            "Completed sales cannot be deleted for audit purposes. If you need to reverse this transaction, please process a refund instead."
        );
    }

    public static BusinessLogicException invalidDateRange() {
        return new BusinessLogicException(
            "INVALID_DATE_RANGE",
            "Start date cannot be after end date. Please select a valid date range."
        );
    }

    public static BusinessLogicException saleWithoutItems() {
        return new BusinessLogicException(
            "SALE_WITHOUT_ITEMS",
            "Cannot complete a sale without any items. Please add at least one product to the sale."
        );
    }
}
