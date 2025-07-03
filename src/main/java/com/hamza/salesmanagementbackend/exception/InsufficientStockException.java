package com.hamza.salesmanagementbackend.exception;

public class InsufficientStockException extends RuntimeException {
    private final String productName;
    private final Integer availableStock;
    private final Integer requestedQuantity;
    private final String errorCode;

    public InsufficientStockException(String message) {
        super(message);
        this.productName = null;
        this.availableStock = null;
        this.requestedQuantity = null;
        this.errorCode = "INSUFFICIENT_STOCK";
    }

    public InsufficientStockException(String productName, Integer availableStock, Integer requestedQuantity) {
        super(formatMessage(productName, availableStock, requestedQuantity));
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
        this.errorCode = "INSUFFICIENT_STOCK";
    }

    private static String formatMessage(String productName, Integer available, Integer requested) {
        if (available == 0) {
            return String.format("Product '%s' is currently out of stock. You requested %d unit(s), but none are available. Please check back later or choose a different product.",
                    productName, requested);
        } else if (available == 1) {
            return String.format("Insufficient stock for product '%s'. You requested %d unit(s), but only 1 unit is available. Please adjust your quantity or choose a different product.",
                    productName, requested);
        } else {
            return String.format("Insufficient stock for product '%s'. You requested %d unit(s), but only %d units are available. Please reduce the quantity or choose a different product.",
                    productName, requested, available);
        }
    }

    public String getProductName() {
        return productName;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Factory methods for different stock scenarios
    public static InsufficientStockException outOfStock(String productName, Integer requestedQuantity) {
        return new InsufficientStockException(productName, 0, requestedQuantity);
    }

    public static InsufficientStockException partialStock(String productName, Integer availableStock, Integer requestedQuantity) {
        return new InsufficientStockException(productName, availableStock, requestedQuantity);
    }
}
