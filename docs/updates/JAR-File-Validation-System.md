# JAR File Validation System

## Overview

The JAR File Validation System provides comprehensive security and integrity validation for JAR file uploads in the update management system. This system ensures that only valid, secure JAR files are accepted for application updates, protecting against malicious uploads and maintaining system security.

## Features

### 1. File Extension Validation
- **Case-Insensitive**: Accepts `.jar`, `.JAR`, `.Jar`, `.jAr` extensions
- **Strict Enforcement**: Only JAR files are allowed for application updates
- **Path Security**: Prevents path traversal attempts in filenames

### 2. MIME Type Validation
- **Accepted Types**:
  - `application/java-archive` (preferred)
  - `application/x-java-archive`
  - `application/zip` (JAR files are ZIP archives)
  - `application/octet-stream` (generic binary)
- **Rejected Types**: All other MIME types including executables, documents, images

### 3. File Size Limits
- **Maximum Size**: 500MB (524,288,000 bytes)
- **Configurable**: Can be adjusted via `app.updates.max-file-size` property
- **Clear Error Messages**: Provides human-readable file size information

### 4. Magic Byte Validation
- **ZIP Signature Check**: Validates ZIP magic bytes (0x50 0x4B)
- **Signature Variants**: Supports all valid ZIP signature combinations
- **Early Detection**: Catches non-archive files immediately

### 5. JAR Structure Validation
- **ZIP Entry Validation**: Verifies internal JAR structure
- **Manifest Checking**: Validates MANIFEST.MF file format
- **Entry Count Limits**: Prevents zip bomb attacks (max 10,000 entries)
- **Entry Size Limits**: Validates individual entry sizes

### 6. Security Validation
- **Path Traversal Protection**: Blocks `../` and absolute paths in entries
- **Suspicious File Detection**: Warns about executable files within JARs
- **Filename Length Limits**: Prevents excessively long entry names
- **Manifest Security**: Checks for potentially dangerous manifest attributes

## Configuration

### Application Properties

```properties
# Update System Configuration
app.updates.max-file-size=524288000
app.updates.allowed-extensions=jar

# JAR File Validation Configuration
app.updates.jar-validation.strict-mime-type=true
app.updates.jar-validation.require-manifest=false
app.updates.jar-validation.max-entries=10000
app.updates.jar-validation.max-manifest-size=65536

# File Upload Configuration
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

## API Integration

### Upload Endpoint

```http
POST /api/v1/admin/updates/versions
Authorization: Bearer <admin-token>
Content-Type: multipart/form-data

Form Data:
- file: application.jar (required)
- versionNumber: 2.2.0 (required)
- isMandatory: false (optional)
- releaseNotes: Release notes (required)
- minimumClientVersion: 2.0.0 (optional)
```

### Validation Process

1. **Pre-validation Checks**
   - File not empty
   - File size within limits
   - Filename validation

2. **Extension Validation**
   - Case-insensitive `.jar` check
   - Path traversal prevention

3. **MIME Type Validation**
   - Acceptable MIME type verification
   - Warning for missing MIME types

4. **Structure Validation**
   - Magic byte verification
   - ZIP entry enumeration
   - Manifest validation
   - Security checks

## Error Handling

### Error Response Format

```json
{
  "success": false,
  "error": "File Upload Error",
  "message": "Detailed error message",
  "errorCode": "FILE_UPLOAD_ERROR",
  "timestamp": "2025-07-14T21:17:54.123Z",
  "suggestions": "Please check the file format, size, and ensure it meets the upload requirements."
}
```

### Common Error Types

| Error Type | Description | Error Code |
|------------|-------------|------------|
| Invalid Extension | Non-JAR file uploaded | `FILE_UPLOAD_ERROR` |
| Invalid MIME Type | Unsupported content type | `FILE_UPLOAD_ERROR` |
| File Too Large | Exceeds size limit | `FILE_UPLOAD_ERROR` |
| Invalid Structure | Corrupted or invalid JAR | `FILE_UPLOAD_ERROR` |
| Security Violation | Malicious content detected | `FILE_UPLOAD_ERROR` |
| Path Traversal | Dangerous entry names | `FILE_UPLOAD_ERROR` |

### Specific Error Messages

```
// Extension validation
"Invalid file type for 'app.txt'. Only JAR files are allowed for application updates"

// MIME type validation  
"Invalid MIME type for file 'app.jar'. Expected: application/java-archive or application/x-java-archive, but got: text/plain"

// Size validation
"File 'large-app.jar' is too large (600.0 MB). Maximum allowed size: 500.0 MB"

// Structure validation
"Invalid file structure for 'app.jar': File does not have valid ZIP/JAR magic bytes"

// Security validation
"Suspicious content detected in JAR file 'app.jar': Entry contains path traversal sequence: ../../../malicious.txt"
```

## Security Features

### Path Traversal Prevention
- Blocks `../` and `..\\` sequences
- Prevents absolute paths (`/`, `\`, `C:\`)
- Validates all ZIP entry names

### Zip Bomb Protection
- Limits total number of entries (10,000)
- Validates individual entry sizes
- Prevents excessive manifest sizes

### Malicious Content Detection
- Warns about executable files (`.exe`, `.dll`, `.so`, `.bat`, `.sh`)
- Checks for suspicious manifest attributes
- Validates entry name lengths

### Manifest Security
- Validates manifest format
- Checks for agent-related attributes
- Limits manifest file size

## Testing

### Unit Tests

The system includes comprehensive unit tests covering:

- **Extension Validation**: Case-insensitive JAR extension acceptance
- **MIME Type Validation**: Valid and invalid content types
- **Structure Validation**: Magic bytes and ZIP structure
- **Security Validation**: Path traversal and malicious content
- **Error Handling**: Proper exception throwing and messages

### Test Coverage

```bash
# Run JAR validation tests
./mvnw test -Dtest=FileManagementServiceUnitTest

# Run all update system tests
./mvnw test -Dtest=UpdateSystemTestSuite
```

## Implementation Details

### Key Classes

1. **FileManagementService**
   - Main validation logic
   - File storage and retrieval
   - Checksum calculation

2. **FileUploadException**
   - Specialized exception handling
   - Detailed error messages
   - Static factory methods

3. **GlobalExceptionHandler**
   - Centralized error handling
   - Consistent API responses
   - User-friendly messages

### Validation Flow

```
File Upload Request
       ↓
Basic Validation (size, empty, filename)
       ↓
Extension Validation (.jar only)
       ↓
MIME Type Validation (strict checking)
       ↓
Magic Byte Validation (ZIP signature)
       ↓
JAR Structure Validation (entries, manifest)
       ↓
Security Validation (path traversal, suspicious content)
       ↓
File Storage (with checksum verification)
       ↓
Success Response
```

## Best Practices

### For Administrators

1. **File Preparation**
   - Ensure JAR files are properly built
   - Include valid MANIFEST.MF files
   - Avoid suspicious file names

2. **Security Considerations**
   - Regularly update validation rules
   - Monitor upload logs for suspicious activity
   - Implement additional scanning if needed

3. **Performance Optimization**
   - Keep JAR files reasonably sized
   - Avoid unnecessary files in JARs
   - Use compression effectively

### For Developers

1. **Error Handling**
   - Always handle FileUploadException
   - Provide clear user feedback
   - Log validation failures

2. **Testing**
   - Test with various JAR file types
   - Validate error scenarios
   - Ensure security measures work

3. **Monitoring**
   - Track validation metrics
   - Monitor file upload patterns
   - Alert on security violations

## Troubleshooting

### Common Issues

1. **"Invalid MIME type" Error**
   - Ensure proper Content-Type header
   - Use supported MIME types
   - Check browser/client settings

2. **"File too large" Error**
   - Reduce JAR file size
   - Check configuration limits
   - Optimize JAR contents

3. **"Invalid structure" Error**
   - Verify JAR file integrity
   - Rebuild corrupted JARs
   - Check ZIP compatibility

4. **"Path traversal" Error**
   - Review JAR entry names
   - Remove suspicious paths
   - Rebuild JAR properly

### Debug Information

Enable debug logging for detailed validation information:

```properties
logging.level.com.hamza.salesmanagementbackend.service.FileManagementService=DEBUG
```

This will provide detailed logs about:
- Validation steps
- File processing
- Security checks
- Error details
