# File Storage and Version Management System

## Overview

The Sales Management System now includes a comprehensive file storage and version management system for handling application updates. This system provides secure, versioned file storage with proper authentication, validation, and download tracking.

## Architecture

### Directory Structure
```
versions/
├── 1.0.0/
│   └── sales-management-1.0.0.jar
├── 2.0.0/
│   └── sales-management-2.0.0.jar
├── 2.1.0/
│   └── sales-management-2.1.0.jar
└── 3.0.0/
    └── sales-management-3.0.0.jar
```

### Key Components

1. **FileManagementService** - Handles file storage, validation, and retrieval
2. **UpdateManagementService** - Manages version metadata and download tracking
3. **UpdateController** - Client-facing REST API endpoints
4. **AdminUpdateController** - Admin management endpoints

## Configuration

### Application Properties
```properties
# Update System Configuration
app.updates.storage-path=./versions
app.updates.max-file-size=524288000
app.updates.allowed-extensions=jar,exe,msi,dmg,deb,rpm
app.updates.enable-resumable-downloads=true
app.updates.cleanup-orphaned-files=true
```

### File Upload Limits
```properties
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

## API Endpoints

### Client Update Endpoints

#### Get Latest Version
```http
GET /api/v1/updates/latest
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "versionNumber": "2.1.0",
    "releaseDate": "2024-01-15T10:30:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Bug fixes and performance improvements",
    "minimumClientVersion": "2.0.0",
    "fileName": "versions/2.1.0/sales-management-2.1.0.jar",
    "fileSize": 52428800,
    "fileChecksum": "sha256:abc123def456",
    "downloadUrl": "/api/v1/updates/download/2.1.0"
  },
  "message": "Latest version retrieved successfully"
}
```

#### Check for Updates
```http
GET /api/v1/updates/check?currentVersion=2.0.0
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "updateAvailable": true,
    "latestVersion": "2.1.0",
    "currentVersion": "2.0.0",
    "isMandatory": false,
    "releaseNotes": "Bug fixes and performance improvements",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "fileSize": 52428800,
    "checksum": "sha256:abc123def456"
  },
  "message": "Update check completed successfully"
}
```

#### Download Update
```http
GET /api/v1/updates/download/2.1.0
Authorization: Bearer <token>
Range: bytes=0-1023 (optional, for resumable downloads)
```

**Response Headers:**
```
Content-Type: application/java-archive
Content-Disposition: attachment; filename="sales-management-2.1.0.jar"
Content-Length: 52428800
X-Checksum: sha256:abc123def456
X-Version: 2.1.0
Accept-Ranges: bytes
X-Download-ID: 123
Cache-Control: private, max-age=3600
ETag: "sha256:abc123def456"
```

#### Get Version Information
```http
GET /api/v1/updates/version/2.1.0
Authorization: Bearer <token>
```

### Admin Update Endpoints

#### Get All Versions (Paginated)
```http
GET /api/v1/admin/updates/versions?page=0&size=10
Authorization: Bearer <admin-token>
```

#### Create New Version
```http
POST /api/v1/admin/updates/versions
Authorization: Bearer <admin-token>
Content-Type: multipart/form-data

Form Data:
- file: sales-management-2.2.0.jar
- versionNumber: 2.2.0
- isMandatory: false
- releaseNotes: New features and improvements
- minimumClientVersion: 2.0.0
```

#### Toggle Version Status
```http
PATCH /api/v1/admin/updates/versions/1/toggle-status
Authorization: Bearer <admin-token>
```

#### Delete Version
```http
DELETE /api/v1/admin/updates/versions/1
Authorization: Bearer <admin-token>
```

#### Get Statistics
```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer <admin-token>
```

## Security Features

### Authentication & Authorization
- All endpoints require authentication
- Admin endpoints require ADMIN role
- Client endpoints accept USER or ADMIN roles

### File Validation
- File type validation (jar, exe, msi, dmg, deb, rpm)
- File size limits (configurable, default 500MB)
- Version number format validation (semantic versioning)
- Path traversal protection
- Checksum verification (SHA-256)

### Security Headers
- Content-Disposition for safe downloads
- ETag for caching
- Cache-Control for security
- Custom headers for version tracking

## File Operations

### Upload Process
1. **Validation** - File type, size, and version number validation
2. **Directory Creation** - Create version-specific directory
3. **File Storage** - Store file with standardized naming
4. **Integrity Check** - Calculate and verify checksums
5. **Database Record** - Create version metadata record

### Download Process
1. **Authentication** - Verify user permissions
2. **Version Lookup** - Find version in database
3. **Download Tracking** - Record download attempt
4. **File Serving** - Stream file with proper headers
5. **Range Support** - Handle resumable downloads

### Cleanup Process
1. **File Deletion** - Remove physical files
2. **Directory Cleanup** - Remove empty version directories
3. **Database Cleanup** - Remove version records
4. **Orphan Detection** - Identify and clean orphaned files

## Database Integration

### ApplicationVersion Entity
```sql
CREATE TABLE application_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_number VARCHAR(50) UNIQUE NOT NULL,
    release_date DATETIME NOT NULL,
    is_mandatory BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    release_notes TEXT,
    minimum_client_version VARCHAR(50),
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_checksum VARCHAR(255) NOT NULL,
    download_url VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL
);
```

### UpdateDownload Entity
```sql
CREATE TABLE update_downloads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_version_id BIGINT NOT NULL,
    client_identifier VARCHAR(255) NOT NULL,
    client_ip VARCHAR(45) NOT NULL,
    user_agent TEXT,
    download_started_at DATETIME NOT NULL,
    download_completed_at DATETIME,
    download_status ENUM('STARTED', 'COMPLETED', 'FAILED') NOT NULL,
    bytes_downloaded BIGINT DEFAULT 0,
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (application_version_id) REFERENCES application_versions(id)
);
```

## Error Handling

### Common Error Responses

#### File Not Found
```json
{
  "success": false,
  "message": "Version not found: 999.0.0",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### Invalid File Type
```json
{
  "success": false,
  "message": "Invalid file type. Allowed types: jar, exe, msi, dmg, deb, rpm",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

#### File Too Large
```json
{
  "success": false,
  "message": "File size exceeds maximum allowed size of 500MB",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Testing

### Unit Tests
- FileManagementServiceTest - File operations and validation
- UpdateManagementServiceTest - Version management logic
- UpdateControllerTest - HTTP REST API endpoints
- AdminUpdateControllerTest - Admin functionality

### Integration Tests
- UpdateSystemIntegrationTest - End-to-end workflows
- File upload and download scenarios
- Security and authentication testing
- Database integration testing

### Test Coverage Areas
- File storage and retrieval
- Version comparison logic
- Download tracking
- Security validation
- Error handling
- Resumable downloads
- File cleanup operations

## Performance Considerations

### Caching
- ETag headers for client-side caching
- File metadata caching
- Version comparison optimization

### File Streaming
- Efficient file streaming for large downloads
- Memory-conscious file operations
- Range request support for resumable downloads

### Database Optimization
- Indexed version lookups
- Efficient pagination
- Download statistics aggregation

## Monitoring and Logging

### Key Metrics
- Download success/failure rates
- File storage usage
- Version adoption rates
- Client update patterns

### Log Events
- File upload/download operations
- Version management actions
- Security violations
- Error conditions

## Deployment Notes

### Directory Permissions
Ensure the application has read/write permissions to the versions directory:
```bash
chmod 755 versions/
chown app-user:app-group versions/
```

### Backup Strategy
- Regular backup of versions directory
- Database backup including version metadata
- Disaster recovery procedures

### Scaling Considerations
- File storage on shared filesystem for multi-instance deployments
- CDN integration for global file distribution
- Load balancer configuration for file downloads
