package com.hamza.salesmanagementbackend.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final String field;
    private final Object value;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = null;
        this.field = null;
        this.value = null;
    }

    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(String.format("%s with %s '%s' was not found. Please verify the %s and try again.",
              resourceType, field, value, field));
        this.resourceType = resourceType;
        this.field = field;
        this.value = value;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
