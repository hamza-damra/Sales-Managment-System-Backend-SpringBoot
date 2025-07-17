package com.hamza.salesmanagementbackend.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials
 * This should result in HTTP 401 (Unauthorized) status code
 */
public class AuthenticationFailedException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;

    public AuthenticationFailedException(String message) {
        super(message);
        this.errorCode = "AUTHENTICATION_FAILED";
        this.userMessage = message;
    }

    public AuthenticationFailedException(String errorCode, String userMessage) {
        super(userMessage);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }

    public AuthenticationFailedException(String errorCode, String userMessage, String technicalMessage) {
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

    // Predefined authentication exceptions
    public static AuthenticationFailedException invalidCredentials() {
        return new AuthenticationFailedException(
            "INVALID_CREDENTIALS",
            "Invalid username or password"
        );
    }

    public static AuthenticationFailedException accountLocked(String username) {
        return new AuthenticationFailedException(
            "ACCOUNT_LOCKED",
            "Your account has been locked. Please contact support for assistance.",
            String.format("Account locked for user: %s", username)
        );
    }

    public static AuthenticationFailedException accountDisabled(String username) {
        return new AuthenticationFailedException(
            "ACCOUNT_DISABLED",
            "Your account has been disabled. Please contact support for assistance.",
            String.format("Account disabled for user: %s", username)
        );
    }

    public static AuthenticationFailedException tokenExpired() {
        return new AuthenticationFailedException(
            "TOKEN_EXPIRED",
            "Your session has expired. Please log in again."
        );
    }

    public static AuthenticationFailedException invalidToken() {
        return new AuthenticationFailedException(
            "INVALID_TOKEN",
            "Invalid authentication token. Please log in again."
        );
    }
}
