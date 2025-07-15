package com.hamza.salesmanagementbackend.exception;

/**
 * Exception thrown when a requested update version is not found
 */
public class UpdateNotFoundException extends RuntimeException {

    public UpdateNotFoundException(String message) {
        super(message);
    }

    public UpdateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static UpdateNotFoundException forVersion(String versionNumber) {
        return new UpdateNotFoundException("Update version '" + versionNumber + "' not found or not active");
    }

    public static UpdateNotFoundException noActiveVersions() {
        return new UpdateNotFoundException("No active update versions available");
    }

    public static UpdateNotFoundException forId(Long id) {
        return new UpdateNotFoundException("Update version with ID '" + id + "' not found");
    }
}
