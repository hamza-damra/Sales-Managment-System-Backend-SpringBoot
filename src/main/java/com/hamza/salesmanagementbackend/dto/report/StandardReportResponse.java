package com.hamza.salesmanagementbackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized response wrapper for all report endpoints
 * Provides consistent structure across all reporting APIs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardReportResponse<T> {
    
    private Boolean success;
    private String message;
    private T data;
    private ReportMetadata metadata;
    private String errorCode;
    private String errorDetails;
    
    public static <T> StandardReportResponse<T> success(T data, ReportMetadata metadata) {
        return StandardReportResponse.<T>builder()
                .success(true)
                .message("Report generated successfully")
                .data(data)
                .metadata(metadata)
                .build();
    }
    
    public static <T> StandardReportResponse<T> success(T data, ReportMetadata metadata, String message) {
        return StandardReportResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }
    
    public static <T> StandardReportResponse<T> error(String message, String errorCode) {
        return StandardReportResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    public static <T> StandardReportResponse<T> error(String message, String errorCode, String errorDetails) {
        return StandardReportResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .build();
    }
}
