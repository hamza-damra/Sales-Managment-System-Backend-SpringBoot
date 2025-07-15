# Update System API Documentation

## Overview

The Update System provides comprehensive backend functionality for managing application updates in the Sales Management System. It enables real-time update notifications, secure file downloads, and administrative management of application versions.

## Architecture Components

### 1. Core Entities
- **ApplicationVersion**: Stores version metadata, file information, and release details
- **UpdateDownload**: Tracks download statistics and client activity
- **ConnectedClient**: Manages WebSocket client connections for real-time notifications

### 2. Services
- **UpdateManagementService**: Core update logic and version management
- **FileManagementService**: Secure file upload, storage, and download handling
- **UpdateStatisticsService**: Analytics and reporting for update system

### 3. Controllers
- **UpdateController**: Client-facing update endpoints
- **AdminUpdateController**: Administrative management endpoints

## API Endpoints

### Client Update Endpoints

#### Get Latest Version
```http
GET /api/v1/updates/latest
Authorization: Bearer {token}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "versionNumber": "2.1.0",
        "releaseDate": "2025-01-15T10:00:00",
        "isMandatory": false,
        "releaseNotes": "Bug fixes and performance improvements",
        "minimumClientVersion": "2.0.0",
        "fileSize": 52428800,
        "formattedFileSize": "50.0 MB",
        "checksum": "sha256:abc123...",
        "downloadUrl": "/api/v1/updates/download/2.1.0"
    },
    "message": "Latest version retrieved successfully"
}
```

#### Check for Updates
```http
GET /api/v1/updates/check?currentVersion={version}
Authorization: Bearer {token}
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
        "checksum": "sha256:abc123..."
    },
    "message": "Update check completed successfully"
}
```

#### Download Update Package
```http
GET /api/v1/updates/download/{version}
Authorization: Bearer {token}
```

**Response Headers:**
```http
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="sales-management-2.1.0.jar"
Content-Length: 52428800
X-Checksum: sha256:abc123...
X-Version: 2.1.0
```

#### Get Version Information
```http
GET /api/v1/updates/version/{version}
Authorization: Bearer {token}
```

### Administrative Endpoints

#### Get All Versions
```http
GET /api/v1/admin/updates/versions?page=0&size=10
Authorization: Bearer {admin-token}
```

#### Create New Version
```http
POST /api/v1/admin/updates/versions
Authorization: Bearer {admin-token}
Content-Type: multipart/form-data

Form Data:
- versionNumber: "2.1.0"
- isMandatory: false
- releaseNotes: "Bug fixes and performance improvements"
- minimumClientVersion: "2.0.0"
- file: [binary-file]
```

#### Update Version Status
```http
PUT /api/v1/admin/updates/versions/{id}/status
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "isActive": true
}
```

#### Delete Version
```http
DELETE /api/v1/admin/updates/versions/{id}
Authorization: Bearer {admin-token}
```

#### Get Update Statistics
```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer {admin-token}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "totalVersions": 5,
        "activeVersions": 3,
        "mandatoryVersions": 1,
        "totalDownloads": 150,
        "successfulDownloads": 145,
        "failedDownloads": 5,
        "downloadSuccessRate": 96.67,
        "activeClients": 25,
        "clientVersionDistribution": {
            "2.0.0": 15,
            "2.1.0": 10
        }
    }
}
```

## Security Configuration

### Authentication & Authorization
- **Client Access**: Requires `USER` or `ADMIN` role for update checks and downloads
- **Admin Access**: Requires `ADMIN` role for version management operations
- **JWT Token**: All endpoints require valid JWT authentication

### File Security
- **Upload Validation**: Strict file type validation (jar, exe, msi, dmg, deb, rpm)
- **Size Limits**: Maximum file size of 500MB
- **Checksum Verification**: SHA-256 hash calculation and verification
- **Secure Storage**: Files stored in configurable secure directory

## Configuration Properties

```properties
# Update System Configuration
app.updates.storage-path=./updates/files
app.updates.max-file-size=500MB
app.updates.allowed-extensions=jar,exe,msi,dmg,deb,rpm
app.updates.websocket.heartbeat-interval=30000
app.updates.websocket.connection-timeout=300000
app.updates.security.admin-role=ADMIN
app.updates.security.rate-limit=10

# File Upload Configuration
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB
```

## Error Handling

### Common Error Responses

#### Update Not Found (404)
```json
{
    "status": 404,
    "error": "Update Not Found",
    "message": "Update version '2.1.0' not found or not active",
    "errorCode": "UPDATE_NOT_FOUND",
    "timestamp": "2025-01-15T10:00:00",
    "suggestions": "Please verify the version number or check if there are any active versions available."
}
```

#### File Upload Error (400)
```json
{
    "status": 400,
    "error": "File Upload Error",
    "message": "Invalid file type for 'app.txt'. Allowed types: jar, exe, msi, dmg, deb, rpm",
    "errorCode": "FILE_UPLOAD_ERROR",
    "timestamp": "2025-01-15T10:00:00",
    "suggestions": "Please check the file format, size, and ensure it meets the upload requirements."
}
```

## Database Schema

### ApplicationVersion Table
- `id`: Primary key (BIGINT)
- `version_number`: Unique version identifier (VARCHAR 20)
- `release_date`: Version release timestamp
- `is_mandatory`: Boolean flag for mandatory updates
- `is_active`: Boolean flag for version availability
- `release_notes`: Detailed release information (TEXT)
- `minimum_client_version`: Minimum compatible client version
- `file_name`: Stored file name
- `file_size`: File size in bytes
- `file_checksum`: SHA-256 hash for integrity verification
- `download_url`: Download endpoint URL
- `created_at`, `updated_at`: Audit timestamps
- `created_by`: User who created the version

### UpdateDownload Table
- `id`: Primary key (BIGINT)
- `version_id`: Foreign key to ApplicationVersion
- `client_identifier`: Unique client identifier
- `download_started_at`: Download initiation timestamp
- `download_completed_at`: Download completion timestamp
- `download_status`: ENUM (STARTED, IN_PROGRESS, COMPLETED, FAILED)
- `client_ip`: Client IP address
- `user_agent`: Client user agent string

### ConnectedClient Table
- `id`: Primary key (BIGINT)
- `session_id`: Unique WebSocket session identifier
- `client_version`: Client application version
- `connected_at`: Connection establishment timestamp
- `last_ping_at`: Last heartbeat timestamp
- `client_ip`: Client IP address
- `is_active`: Connection status flag

## Usage Examples

### Frontend Integration

```javascript
// Check for updates
const checkUpdates = async (currentVersion) => {
    const response = await fetch(`/api/v1/updates/check?currentVersion=${currentVersion}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return response.json();
};

// Download update
const downloadUpdate = async (version) => {
    const response = await fetch(`/api/v1/updates/download/${version}`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'X-Client-ID': 'unique-client-identifier'
        }
    });
    return response.blob();
};
```

### Admin Operations

```javascript
// Upload new version
const uploadVersion = async (versionData, file) => {
    const formData = new FormData();
    formData.append('versionNumber', versionData.versionNumber);
    formData.append('isMandatory', versionData.isMandatory);
    formData.append('releaseNotes', versionData.releaseNotes);
    formData.append('file', file);
    
    const response = await fetch('/api/v1/admin/updates/versions', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${adminToken}`
        },
        body: formData
    });
    return response.json();
};
```

## Testing

The update system includes comprehensive unit tests covering:
- Service layer functionality
- Repository operations
- Controller endpoints
- Error handling scenarios
- File upload/download operations

Run tests with:
```bash
mvn test -Dtest=*Update*Test
```

## Monitoring & Analytics

The system provides detailed analytics including:
- Download success rates
- Version adoption metrics
- Client version distribution
- System health indicators
- Performance metrics

Access admin dashboard at `/api/v1/admin/updates/statistics` for comprehensive reporting.
