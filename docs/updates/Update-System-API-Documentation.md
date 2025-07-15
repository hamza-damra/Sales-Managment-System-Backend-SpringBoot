# Update System API Documentation

## Overview

The Update System provides comprehensive backend functionality for managing application updates in the Sales Management System. It enables HTTP-based update checking, secure file downloads, version management, and administrative control of application versions.

### Key Features
- **HTTP-based Communication**: RESTful API endpoints for all update operations
- **Secure Authentication**: JWT-based authentication with role-based access control
- **File Management**: Secure upload, storage, and download of application files
- **Version Control**: Comprehensive version management with release channels
- **Rate Limiting**: Built-in protection against abuse with configurable limits
- **Resumable Downloads**: Support for interrupted download recovery
- **Differential Updates**: Delta updates to minimize download sizes
- **Analytics**: Comprehensive tracking and reporting of update activities

### Architecture Components
- **UpdateController**: Client-facing REST API endpoints
- **AdminUpdateController**: Administrative management endpoints
- **UpdateManagementService**: Core update logic and version management
- **FileManagementService**: Secure file storage and download handling
- **RateLimitingService**: Request throttling and abuse prevention
- **UpdateStatisticsService**: Analytics and reporting

## Connection Setup

### Base URL
```
http://localhost:8081/api/v1/updates
```

### Authentication Requirements
All endpoints require JWT authentication via the `Authorization` header:

```http
Authorization: Bearer <jwt-token>
```

### Content Types
- **Request**: `application/json` (for JSON payloads) or `multipart/form-data` (for file uploads)
- **Response**: `application/json`

### Required Headers
```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
Accept: application/json
```

## API Endpoints

### Client Update Endpoints

#### 1. Check for Updates
Check if a newer version is available for the current client version.

```http
GET /api/v1/updates/check?currentVersion={version}
Authorization: Bearer <jwt-token>
```

**Parameters:**
- `currentVersion` (query, required): Current client version (e.g., "2.0.0")

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
    "formattedFileSize": "50.0 MB",
    "checksum": "sha256:abc123...",
    "minimumClientVersion": "2.0.0"
  },
  "message": "Update check completed successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Status Codes:**
- `200 OK`: Update check successful
- `400 Bad Request`: Invalid version format
- `401 Unauthorized`: Invalid or missing JWT token
- `429 Too Many Requests`: Rate limit exceeded

#### 2. Get Latest Version
Retrieve information about the latest available version.

```http
GET /api/v1/updates/latest
Authorization: Bearer <jwt-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "versionNumber": "2.1.0",
    "releaseDate": "2024-01-15T10:00:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Latest features and improvements",
    "fileName": "sales-management-2.1.0.jar",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "fileChecksum": "sha256:abc123...",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "releaseChannel": "STABLE"
  },
  "message": "Latest version retrieved successfully"
}
```

#### 3. Download Version
Download a specific version file with support for resumable downloads.

```http
GET /api/v1/updates/download/{version}
Authorization: Bearer <jwt-token>
Range: bytes=0-1023 (optional, for resumable downloads)
```

**Parameters:**
- `version` (path, required): Version number to download (e.g., "2.1.0")

**Headers:**
- `Range` (optional): For resumable downloads (e.g., "bytes=0-1023")

**Response:**
- **Success**: Binary file content with appropriate headers
- **Partial Content**: For range requests (status 206)

**Response Headers:**
```http
Content-Type: application/java-archive
Content-Length: 52428800
Content-Disposition: attachment; filename="sales-management-2.1.0.jar"
Accept-Ranges: bytes
ETag: "abc123..."
```

#### 4. Get Version Metadata
Retrieve detailed metadata for a specific version without downloading.

```http
GET /api/v1/updates/version/{version}
Authorization: Bearer <jwt-token>
```

**Parameters:**
- `version` (path, required): Version number (e.g., "2.1.0")

**Response:**
```json
{
  "success": true,
  "data": {
    "versionNumber": "2.1.0",
    "releaseDate": "2024-01-15T10:00:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Detailed release notes...",
    "minimumClientVersion": "2.0.0",
    "fileName": "sales-management-2.1.0.jar",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "fileChecksum": "sha256:abc123...",
    "checksumAlgorithm": "SHA-256",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "releaseChannel": "STABLE",
    "createdBy": "admin",
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T09:00:00"
  },
  "message": "Version metadata retrieved successfully"
}
```

#### 5. Check System Compatibility
Validate system compatibility for a specific version.

```http
GET /api/v1/updates/compatibility/{version}?clientVersion={clientVersion}&os={os}&arch={arch}&javaVersion={javaVersion}
Authorization: Bearer <jwt-token>
```

**Parameters:**
- `version` (path, required): Target version to check
- `clientVersion` (query, required): Current client version
- `os` (query, optional): Operating system
- `arch` (query, optional): System architecture
- `javaVersion` (query, optional): Java version

**Response:**
```json
{
  "success": true,
  "data": {
    "isCompatible": true,
    "targetVersion": "2.1.0",
    "clientVersion": "2.0.0",
    "minimumRequiredVersion": "2.0.0",
    "javaVersion": {
      "required": "11+",
      "detected": "17.0.1",
      "isCompatible": true,
      "vendor": "Eclipse Adoptium"
    },
    "operatingSystem": {
      "name": "Windows",
      "version": "10",
      "architecture": "x64",
      "isSupported": true
    },
    "systemRequirements": {
      "minimumMemoryMB": 512,
      "availableMemoryMB": 8192,
      "minimumDiskSpaceMB": 100,
      "availableDiskSpaceMB": 50000
    },
    "compatibilityIssues": [],
    "recommendations": [],
    "canProceed": true,
    "warningLevel": "NONE"
  },
  "message": "Compatibility check completed"
}
```

#### 6. Get Differential Update
Retrieve differential update information between two versions.

```http
GET /api/v1/updates/delta/{fromVersion}/{toVersion}
Authorization: Bearer <jwt-token>
```

**Parameters:**
- `fromVersion` (path, required): Source version
- `toVersion` (path, required): Target version

**Response:**
```json
{
  "success": true,
  "data": {
    "fromVersion": "2.0.0",
    "toVersion": "2.1.0",
    "deltaAvailable": true,
    "deltaSize": 5242880,
    "formattedDeltaSize": "5.0 MB",
    "fullUpdateSize": 52428800,
    "formattedFullUpdateSize": "50.0 MB",
    "compressionRatio": 90.0,
    "deltaChecksum": "sha256:def456...",
    "deltaDownloadUrl": "/api/v1/updates/delta/download/2.0.0/2.1.0",
    "fullDownloadUrl": "/api/v1/updates/download/2.1.0",
    "changedFiles": [
      {
        "path": "com/example/Service.class",
        "operation": "MODIFIED",
        "size": 1024,
        "checksum": "sha256:ghi789..."
      }
    ],
    "fallbackToFull": false,
    "estimatedApplyTimeSeconds": 30,
    "createdAt": "2024-01-15T10:00:00",
    "expiresAt": "2024-01-22T10:00:00"
  },
  "message": "Differential update information retrieved"
}
```

### Admin Update Endpoints

Admin endpoints require `ADMIN` role in addition to valid JWT authentication.

#### 1. List All Versions
Retrieve paginated list of all versions with optional filtering.

```http
GET /api/v1/admin/updates/versions?page=0&size=10&sort=releaseDate,desc
Authorization: Bearer <admin-jwt-token>
```

**Parameters:**
- `page` (query, optional): Page number (default: 0)
- `size` (query, optional): Page size (default: 10)
- `sort` (query, optional): Sort criteria (default: "releaseDate,desc")

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "versionNumber": "2.1.0",
        "releaseDate": "2024-01-15T10:00:00",
        "isMandatory": false,
        "isActive": true,
        "releaseNotes": "Latest version",
        "fileName": "sales-management-2.1.0.jar",
        "fileSize": 52428800,
        "downloadCount": 150,
        "successfulDownloads": 145,
        "failedDownloads": 5
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "ascending": false
      }
    },
    "totalElements": 5,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "message": "Versions retrieved successfully"
}
```

#### 2. Create New Version
Upload and create a new application version.

```http
POST /api/v1/admin/updates/versions
Authorization: Bearer <admin-jwt-token>
Content-Type: multipart/form-data
```

**Form Data:**
- `file` (file, required): Application JAR file
- `versionNumber` (string, required): Version identifier (e.g., "2.2.0")
- `isMandatory` (boolean, optional): Whether update is mandatory (default: false)
- `releaseNotes` (string, required): Release notes and changelog
- `minimumClientVersion` (string, optional): Minimum compatible client version
- `releaseDate` (string, optional): Release date (ISO format, default: current time)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "versionNumber": "2.2.0",
    "releaseDate": "2024-01-16T10:00:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "New features and improvements",
    "minimumClientVersion": "2.0.0",
    "fileName": "sales-management-2.2.0.jar",
    "fileSize": 53477376,
    "formattedFileSize": "51.0 MB",
    "fileChecksum": "sha256:xyz789...",
    "downloadUrl": "/api/v1/updates/download/2.2.0",
    "releaseChannel": "STABLE",
    "createdBy": "admin",
    "createdAt": "2024-01-16T09:30:00"
  },
  "message": "Version created successfully"
}
```

#### 3. Update Version Information
Update metadata for an existing version.

```http
PUT /api/v1/admin/updates/versions/{id}
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "isMandatory": true,
  "isActive": true,
  "releaseNotes": "Updated release notes with security fixes",
  "minimumClientVersion": "2.1.0"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "versionNumber": "2.1.0",
    "isMandatory": true,
    "isActive": true,
    "releaseNotes": "Updated release notes with security fixes",
    "minimumClientVersion": "2.1.0",
    "updatedAt": "2024-01-16T11:00:00"
  },
  "message": "Version updated successfully"
}
```

#### 4. Get Version Details
Retrieve detailed information about a specific version.

```http
GET /api/v1/admin/updates/versions/{id}
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "versionNumber": "2.1.0",
    "releaseDate": "2024-01-15T10:00:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Comprehensive release notes...",
    "minimumClientVersion": "2.0.0",
    "fileName": "sales-management-2.1.0.jar",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "fileChecksum": "sha256:abc123...",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "releaseChannel": "STABLE",
    "createdBy": "admin",
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T09:00:00",
    "downloadCount": 150,
    "successfulDownloads": 145,
    "failedDownloads": 5
  },
  "message": "Version details retrieved successfully"
}
```

#### 5. Toggle Version Status
Activate or deactivate a version.

```http
PATCH /api/v1/admin/updates/versions/{id}/toggle-status
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "versionNumber": "2.1.0",
    "isActive": false,
    "updatedAt": "2024-01-16T12:00:00"
  },
  "message": "Version status updated successfully"
}
```

#### 6. Delete Version
Remove a version and its associated file.

```http
DELETE /api/v1/admin/updates/versions/{id}
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "message": "Version deleted successfully"
}
```

#### 7. Get Update Statistics
Retrieve comprehensive statistics about the update system.

```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer <admin-jwt-token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalVersions": 5,
    "activeVersions": 3,
    "mandatoryVersions": 1,
    "averageFileSize": 51380224,
    "formattedAverageFileSize": "49.0 MB",
    "totalDownloads": 750,
    "successfulDownloads": 720,
    "failedDownloads": 30,
    "downloadSuccessRate": 96.0,
    "dailyDownloadCounts": [
      {
        "date": "2024-01-15",
        "downloadCount": 45
      }
    ],
    "topVersionsByDownloads": [
      {
        "versionNumber": "2.1.0",
        "downloadCount": 150
      }
    ]
  },
  "message": "Statistics retrieved successfully"
}
```

## Message Formats

### Standard API Response Structure

All API responses follow a consistent structure using the `ApiResponse<T>` wrapper:

```typescript
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message: string;
  timestamp?: string;
}
```

### Success Response Example
```json
{
  "success": true,
  "data": {
    // Response data object
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response Structure
```json
{
  "success": false,
  "error": {
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid version format",
    "errorCode": "INVALID_VERSION",
    "timestamp": "2024-01-15T10:30:00",
    "suggestions": "Version must follow semantic versioning (e.g., 2.1.0)"
  }
}
```

### Core Data Models

#### ApplicationVersionDTO
```typescript
interface ApplicationVersionDTO {
  id?: number;
  versionNumber: string;
  releaseDate: string; // ISO 8601 format
  isMandatory: boolean;
  isActive: boolean;
  releaseNotes: string;
  minimumClientVersion?: string;
  fileName: string;
  fileSize: number;
  formattedFileSize: string; // Human-readable size
  fileChecksum: string; // SHA-256 hash
  checksumAlgorithm: string; // "SHA-256"
  downloadUrl: string;
  releaseChannel: string; // "STABLE", "BETA", "NIGHTLY", etc.
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  downloadCount?: number;
  successfulDownloads?: number;
  failedDownloads?: number;
}
```

#### UpdateCheckResponseDTO
```typescript
interface UpdateCheckResponseDTO {
  updateAvailable: boolean;
  latestVersion: string;
  currentVersion: string;
  isMandatory: boolean;
  releaseNotes: string;
  downloadUrl: string;
  fileSize: number;
  formattedFileSize: string;
  checksum: string;
  minimumClientVersion?: string;
}
```

#### DifferentialUpdateDTO
```typescript
interface DifferentialUpdateDTO {
  fromVersion: string;
  toVersion: string;
  deltaAvailable: boolean;
  deltaSize: number;
  formattedDeltaSize: string;
  fullUpdateSize: number;
  formattedFullUpdateSize: string;
  compressionRatio: number; // Percentage
  deltaChecksum: string;
  deltaDownloadUrl: string;
  fullDownloadUrl: string;
  changedFiles: ChangedFile[];
  patchInstructions: PatchInstruction[];
  fallbackToFull: boolean;
  fallbackReason?: string;
  estimatedApplyTimeSeconds: number;
  createdAt: string;
  expiresAt: string;
}

interface ChangedFile {
  path: string;
  operation: "ADDED" | "MODIFIED" | "DELETED" | "MOVED" | "RENAMED";
  size: number;
  checksum: string;
}

interface PatchInstruction {
  order: number;
  operation: "COPY" | "EXTRACT" | "DELETE" | "MOVE" | "VERIFY";
  target: string;
  source?: string;
  checksum: string;
}
```

#### CompatibilityCheckDTO
```typescript
interface CompatibilityCheckDTO {
  isCompatible: boolean;
  targetVersion: string;
  clientVersion: string;
  minimumRequiredVersion: string;
  javaVersion: JavaVersionInfo;
  operatingSystem: OperatingSystemInfo;
  systemRequirements: SystemRequirements;
  compatibilityIssues: CompatibilityIssue[];
  recommendations: string[];
  canProceed: boolean;
  warningLevel: "NONE" | "INFO" | "WARNING" | "CRITICAL";
}

interface JavaVersionInfo {
  required: string;
  detected: string;
  isCompatible: boolean;
  vendor: string;
}

interface OperatingSystemInfo {
  name: string;
  version: string;
  architecture: string;
  isSupported: boolean;
}

interface SystemRequirements {
  minimumMemoryMB: number;
  availableMemoryMB: number;
  minimumDiskSpaceMB: number;
  availableDiskSpaceMB: number;
  additionalRequirements: Record<string, string>;
}

interface CompatibilityIssue {
  type: "JAVA_VERSION" | "OPERATING_SYSTEM" | "MEMORY" | "DISK_SPACE" | "ARCHITECTURE" | "DEPENDENCY" | "CONFIGURATION";
  severity: "CRITICAL" | "WARNING" | "INFO";
  description: string;
  resolution: string;
  component: string;
}
```

## Authentication & Authorization

### JWT Token Requirements

All API endpoints require JWT authentication with the following claims:

```json
{
  "sub": "user@example.com",
  "roles": ["USER"] // or ["ADMIN"] for admin endpoints,
  "iat": 1642248000,
  "exp": 1642334400
}
```

### Role-Based Access Control

#### USER Role
- Access to all client update endpoints
- Can check for updates, download files, and view version metadata
- Cannot access admin endpoints

#### ADMIN Role
- Full access to all endpoints
- Can manage versions, upload files, and view statistics
- Inherits all USER permissions

### Authentication Headers

```javascript
const headers = {
  'Authorization': `Bearer ${jwtToken}`,
  'Content-Type': 'application/json',
  'Accept': 'application/json'
};
```

### Token Validation

The system validates JWT tokens for:
- **Signature**: Ensures token integrity
- **Expiration**: Checks token validity period
- **Issuer**: Validates token source
- **Audience**: Confirms intended recipient
- **Roles**: Verifies required permissions

### Authentication Errors

```json
{
  "success": false,
  "error": {
    "status": 401,
    "error": "Unauthorized",
    "message": "JWT token is expired",
    "errorCode": "TOKEN_EXPIRED",
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

## Rate Limiting

The update system implements comprehensive rate limiting to prevent abuse and ensure fair resource usage.

### Rate Limit Configuration

| Endpoint Type | Requests per Minute | Window Duration |
|---------------|-------------------|-----------------|
| Update Check | 20 | 60 seconds |
| Download | 5 | 60 seconds |
| Metadata | 30 | 60 seconds |
| Compatibility | 10 | 60 seconds |
| Analytics | 15 | 60 seconds |
| Rollback | 3 | 60 seconds |
| Delta Updates | 5 | 60 seconds |

### Rate Limit Headers

All responses include rate limiting information:

```http
X-RateLimit-Remaining: 15
X-RateLimit-Reset: 1642248060
X-RateLimit-Limit: 20
```

### Rate Limit Exceeded Response

When rate limits are exceeded, the API returns:

```json
{
  "success": false,
  "error": {
    "status": 429,
    "error": "Too Many Requests",
    "message": "Rate limit exceeded. Try again in 300 seconds.",
    "errorCode": "RATE_LIMIT_EXCEEDED",
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

### Rate Limiting Strategy

1. **Client Identification**: Based on JWT subject and IP address
2. **Sliding Window**: Rolling time window for request counting
3. **Progressive Blocking**: Escalating block durations for repeat violations
4. **Endpoint-Specific**: Different limits for different endpoint types

### Block Duration Escalation

| Violation Count | Block Duration |
|----------------|----------------|
| 1st violation | 5 minutes |
| 2nd violation | 15 minutes |
| 3rd violation | 30 minutes |
| 4th+ violation | 60 minutes |

## Integration Guide

### Step 1: Authentication Setup

First, obtain a JWT token through the authentication system:

```javascript
// Login to get JWT token
const loginResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password'
  })
});

const { data } = await loginResponse.json();
const jwtToken = data.accessToken;
```

### Step 2: Check for Updates

```javascript
async function checkForUpdates(currentVersion) {
  const response = await fetch(`/api/v1/updates/check?currentVersion=${currentVersion}`, {
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Accept': 'application/json'
    }
  });

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
  }

  const result = await response.json();
  return result.data;
}

// Usage
const updateInfo = await checkForUpdates('2.0.0');
if (updateInfo.updateAvailable) {
  console.log(`Update available: ${updateInfo.latestVersion}`);
  console.log(`Download size: ${updateInfo.formattedFileSize}`);
  console.log(`Mandatory: ${updateInfo.isMandatory}`);
}
```

### Step 3: Download Updates with Progress

```javascript
async function downloadUpdate(version, onProgress) {
  const response = await fetch(`/api/v1/updates/download/${version}`, {
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });

  if (!response.ok) {
    throw new Error(`Download failed: ${response.status}`);
  }

  const contentLength = response.headers.get('Content-Length');
  const total = parseInt(contentLength, 10);
  let loaded = 0;

  const reader = response.body.getReader();
  const chunks = [];

  while (true) {
    const { done, value } = await reader.read();

    if (done) break;

    chunks.push(value);
    loaded += value.length;

    if (onProgress) {
      onProgress({
        loaded,
        total,
        percentage: Math.round((loaded / total) * 100)
      });
    }
  }

  return new Blob(chunks);
}

// Usage with progress callback
const blob = await downloadUpdate('2.1.0', (progress) => {
  console.log(`Download progress: ${progress.percentage}%`);
});
```

### Step 4: Resumable Downloads

```javascript
async function resumableDownload(version, existingData = null) {
  const startByte = existingData ? existingData.byteLength : 0;

  const headers = {
    'Authorization': `Bearer ${jwtToken}`
  };

  if (startByte > 0) {
    headers['Range'] = `bytes=${startByte}-`;
  }

  const response = await fetch(`/api/v1/updates/download/${version}`, {
    headers
  });

  if (response.status === 206 || response.status === 200) {
    const newData = await response.arrayBuffer();

    if (existingData && response.status === 206) {
      // Append new data to existing
      const combined = new Uint8Array(existingData.byteLength + newData.byteLength);
      combined.set(new Uint8Array(existingData), 0);
      combined.set(new Uint8Array(newData), existingData.byteLength);
      return combined.buffer;
    }

    return newData;
  }

  throw new Error(`Resume failed: ${response.status}`);
}
```

### Step 5: Verify File Integrity

```javascript
async function verifyChecksum(fileData, expectedChecksum) {
  const hashBuffer = await crypto.subtle.digest('SHA-256', fileData);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

  return `sha256:${hashHex}` === expectedChecksum;
}

// Usage
const isValid = await verifyChecksum(downloadedFile, updateInfo.checksum);
if (!isValid) {
  throw new Error('File integrity check failed');
}
```

### Step 6: Handle Differential Updates

```javascript
async function checkDifferentialUpdate(fromVersion, toVersion) {
  const response = await fetch(`/api/v1/updates/delta/${fromVersion}/${toVersion}`, {
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Accept': 'application/json'
    }
  });

  const result = await response.json();

  if (result.success && result.data.deltaAvailable) {
    console.log(`Delta update available: ${result.data.formattedDeltaSize}`);
    console.log(`Compression ratio: ${result.data.compressionRatio}%`);
    return result.data;
  }

  return null;
}
```

## Error Handling

### Common HTTP Status Codes

| Status Code | Description | Common Causes |
|-------------|-------------|---------------|
| 200 | OK | Request successful |
| 206 | Partial Content | Resumable download range request |
| 400 | Bad Request | Invalid parameters, malformed request |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions (role-based) |
| 404 | Not Found | Version or resource not found |
| 409 | Conflict | Version already exists, file conflicts |
| 413 | Payload Too Large | File size exceeds limits |
| 415 | Unsupported Media Type | Invalid file type |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server-side errors |

### Error Response Format

All errors follow a consistent structure:

```json
{
  "success": false,
  "error": {
    "status": 404,
    "error": "Not Found",
    "message": "Version not found: 2.1.0",
    "errorCode": "VERSION_NOT_FOUND",
    "timestamp": "2024-01-15T10:30:00",
    "suggestions": "Check available versions using /api/v1/updates/latest"
  }
}
```

### Error Codes Reference

| Error Code | Description | Resolution |
|------------|-------------|------------|
| `INVALID_VERSION` | Version format is invalid | Use semantic versioning (e.g., 2.1.0) |
| `VERSION_NOT_FOUND` | Requested version doesn't exist | Check available versions |
| `TOKEN_EXPIRED` | JWT token has expired | Refresh authentication token |
| `INSUFFICIENT_PERMISSIONS` | User lacks required role | Contact administrator for permissions |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Wait for rate limit reset |
| `FILE_TOO_LARGE` | Upload exceeds size limit | Reduce file size or contact administrator |
| `INVALID_FILE_TYPE` | Unsupported file format | Use supported formats (.jar) |
| `CHECKSUM_MISMATCH` | File integrity verification failed | Re-download the file |
| `COMPATIBILITY_ISSUE` | System compatibility problems | Check system requirements |

### Error Handling Best Practices

```javascript
async function handleApiCall(apiFunction) {
  try {
    return await apiFunction();
  } catch (error) {
    if (error.response) {
      const errorData = await error.response.json();

      switch (errorData.error.status) {
        case 401:
          // Token expired, refresh authentication
          await refreshToken();
          return await apiFunction(); // Retry

        case 429:
          // Rate limited, wait and retry
          const retryAfter = error.response.headers.get('X-RateLimit-Reset');
          await new Promise(resolve => setTimeout(resolve, retryAfter * 1000));
          return await apiFunction(); // Retry

        case 404:
          console.error('Resource not found:', errorData.error.message);
          break;

        default:
          console.error('API Error:', errorData.error.message);
      }
    }

    throw error;
  }
}
```

## Usage Examples

### Complete Update Check and Download Flow

```javascript
class UpdateManager {
  constructor(jwtToken, currentVersion) {
    this.jwtToken = jwtToken;
    this.currentVersion = currentVersion;
    this.baseUrl = '/api/v1/updates';
  }

  async checkAndDownloadUpdate(onProgress) {
    try {
      // Step 1: Check for updates
      const updateCheck = await this.checkForUpdates();

      if (!updateCheck.updateAvailable) {
        console.log('No updates available');
        return null;
      }

      console.log(`Update available: ${updateCheck.latestVersion}`);

      // Step 2: Check compatibility
      const compatibility = await this.checkCompatibility(updateCheck.latestVersion);

      if (!compatibility.canProceed) {
        console.error('Compatibility issues found:', compatibility.compatibilityIssues);
        return null;
      }

      // Step 3: Check for differential update
      const deltaUpdate = await this.checkDifferentialUpdate(
        this.currentVersion,
        updateCheck.latestVersion
      );

      // Step 4: Download (delta or full)
      let downloadedFile;
      if (deltaUpdate && deltaUpdate.deltaAvailable) {
        console.log(`Downloading delta update: ${deltaUpdate.formattedDeltaSize}`);
        downloadedFile = await this.downloadFile(deltaUpdate.deltaDownloadUrl, onProgress);
      } else {
        console.log(`Downloading full update: ${updateCheck.formattedFileSize}`);
        downloadedFile = await this.downloadFile(updateCheck.downloadUrl, onProgress);
      }

      // Step 5: Verify integrity
      const expectedChecksum = deltaUpdate ? deltaUpdate.deltaChecksum : updateCheck.checksum;
      const isValid = await this.verifyChecksum(downloadedFile, expectedChecksum);

      if (!isValid) {
        throw new Error('File integrity verification failed');
      }

      console.log('Update downloaded and verified successfully');
      return {
        file: downloadedFile,
        version: updateCheck.latestVersion,
        isDelta: !!deltaUpdate,
        metadata: updateCheck
      };

    } catch (error) {
      console.error('Update process failed:', error);
      throw error;
    }
  }

  async checkForUpdates() {
    const response = await fetch(`${this.baseUrl}/check?currentVersion=${this.currentVersion}`, {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });

    const result = await response.json();
    return result.data;
  }

  async checkCompatibility(version) {
    const systemInfo = this.getSystemInfo();
    const params = new URLSearchParams({
      clientVersion: this.currentVersion,
      ...systemInfo
    });

    const response = await fetch(`${this.baseUrl}/compatibility/${version}?${params}`, {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });

    const result = await response.json();
    return result.data;
  }

  async checkDifferentialUpdate(fromVersion, toVersion) {
    const response = await fetch(`${this.baseUrl}/delta/${fromVersion}/${toVersion}`, {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });

    const result = await response.json();
    return result.success ? result.data : null;
  }

  async downloadFile(url, onProgress) {
    const response = await fetch(url, {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });

    if (!response.ok) {
      throw new Error(`Download failed: ${response.status}`);
    }

    const contentLength = response.headers.get('Content-Length');
    const total = parseInt(contentLength, 10);
    let loaded = 0;

    const reader = response.body.getReader();
    const chunks = [];

    while (true) {
      const { done, value } = await reader.read();

      if (done) break;

      chunks.push(value);
      loaded += value.length;

      if (onProgress) {
        onProgress({
          loaded,
          total,
          percentage: Math.round((loaded / total) * 100)
        });
      }
    }

    return new Blob(chunks);
  }

  async verifyChecksum(fileData, expectedChecksum) {
    const arrayBuffer = await fileData.arrayBuffer();
    const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

    return `sha256:${hashHex}` === expectedChecksum;
  }

  getSystemInfo() {
    return {
      os: navigator.platform,
      userAgent: navigator.userAgent,
      // Add other system information as needed
    };
  }
}

// Usage
const updateManager = new UpdateManager(jwtToken, '2.0.0');

updateManager.checkAndDownloadUpdate((progress) => {
  console.log(`Download progress: ${progress.percentage}%`);
}).then((result) => {
  if (result) {
    console.log(`Successfully downloaded ${result.version}`);
    // Proceed with installation
  }
}).catch((error) => {
  console.error('Update failed:', error);
});
```

## Testing References

### Unit Testing

The update system includes comprehensive unit tests covering all endpoints and business logic. Tests are located in:

- `src/test/java/com/hamza/salesmanagementbackend/controller/UpdateControllerTest.java`
- `src/test/java/com/hamza/salesmanagementbackend/controller/AdminUpdateControllerTest.java`
- `src/test/java/com/hamza/salesmanagementbackend/service/UpdateManagementServiceTest.java`

### Integration Testing

Run the complete test suite:

```bash
# Run all update system tests
mvn test -Dtest=UpdateSystemTestSuite

# Run specific controller tests
mvn test -Dtest=UpdateControllerTest
mvn test -Dtest=AdminUpdateControllerTest

# Run service layer tests
mvn test -Dtest=UpdateManagementServiceTest
```

### API Testing with Postman/Insomnia

#### Environment Variables
```json
{
  "baseUrl": "http://localhost:8081",
  "jwtToken": "{{auth_token}}",
  "currentVersion": "2.0.0"
}
```

#### Test Collection Structure
1. **Authentication**
   - Login and get JWT token
   - Refresh token

2. **Client Endpoints**
   - Check for updates
   - Get latest version
   - Download version
   - Get version metadata
   - Check compatibility
   - Get differential update

3. **Admin Endpoints**
   - List versions
   - Create version
   - Update version
   - Get version details
   - Toggle status
   - Delete version
   - Get statistics

4. **Error Scenarios**
   - Invalid authentication
   - Rate limiting
   - Invalid parameters
   - Non-existent resources

### Frontend Integration Testing

#### Test Update Check Flow
```javascript
describe('Update System Integration', () => {
  let updateManager;

  beforeEach(() => {
    updateManager = new UpdateManager(mockJwtToken, '2.0.0');
  });

  test('should check for updates successfully', async () => {
    const updateCheck = await updateManager.checkForUpdates();

    expect(updateCheck).toHaveProperty('updateAvailable');
    expect(updateCheck).toHaveProperty('latestVersion');
    expect(updateCheck).toHaveProperty('currentVersion', '2.0.0');
  });

  test('should handle rate limiting gracefully', async () => {
    // Mock rate limited response
    fetchMock.mockResponseOnce(
      JSON.stringify({
        success: false,
        error: {
          status: 429,
          message: 'Rate limit exceeded'
        }
      }),
      { status: 429 }
    );

    await expect(updateManager.checkForUpdates()).rejects.toThrow('Rate limit exceeded');
  });

  test('should verify file integrity', async () => {
    const mockFile = new Blob(['test content']);
    const expectedChecksum = 'sha256:expected_hash';

    const isValid = await updateManager.verifyChecksum(mockFile, expectedChecksum);
    expect(typeof isValid).toBe('boolean');
  });
});
```

### Performance Testing

#### Load Testing with Artillery
```yaml
config:
  target: 'http://localhost:8081'
  phases:
    - duration: 60
      arrivalRate: 10
  defaults:
    headers:
      Authorization: 'Bearer {{jwt_token}}'

scenarios:
  - name: 'Update Check Load Test'
    requests:
      - get:
          url: '/api/v1/updates/check?currentVersion=2.0.0'

  - name: 'Download Load Test'
    requests:
      - get:
          url: '/api/v1/updates/download/2.1.0'
```

### Manual Testing Checklist

#### Client Endpoints
- [ ] Update check with valid version
- [ ] Update check with invalid version
- [ ] Get latest version
- [ ] Download existing version
- [ ] Download non-existent version
- [ ] Resumable download with Range header
- [ ] Version metadata retrieval
- [ ] Compatibility check with system info
- [ ] Differential update between versions

#### Admin Endpoints
- [ ] List versions with pagination
- [ ] Create new version with file upload
- [ ] Update version metadata
- [ ] Get version details
- [ ] Toggle version status
- [ ] Delete version
- [ ] Get update statistics

#### Error Handling
- [ ] Invalid JWT token
- [ ] Expired JWT token
- [ ] Insufficient permissions
- [ ] Rate limit exceeded
- [ ] Invalid file format
- [ ] File size exceeded
- [ ] Network interruption during download

#### Security Testing
- [ ] Unauthorized access attempts
- [ ] Role-based access control
- [ ] File upload validation
- [ ] SQL injection attempts
- [ ] XSS prevention
- [ ] CSRF protection

### Monitoring and Logging

The system provides comprehensive logging for debugging and monitoring:

```bash
# View update system logs
tail -f logs/application.log | grep "UpdateController\|AdminUpdateController"

# Monitor rate limiting
tail -f logs/application.log | grep "RateLimitingService"

# Track file operations
tail -f logs/application.log | grep "FileManagementService"
```

### Configuration for Testing

#### Test Environment Properties
```properties
# Test database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop

# Test file storage
app.updates.storage-path=./test-versions
app.updates.max-file-size=10485760

# Relaxed rate limiting for testing
app.updates.rate-limit.update-check=100
app.updates.rate-limit.download=50

# Test JWT settings
jwt.secret=test-secret-key
jwt.expiration=3600000
```

---

## Summary

This documentation provides comprehensive coverage of the Update System HTTP-based APIs, including:

- **Complete endpoint reference** with request/response examples
- **Authentication and authorization** requirements
- **Rate limiting policies** and error handling
- **Step-by-step integration guide** with practical code examples
- **Comprehensive testing strategies** for validation

The system is designed for frontend developers to easily integrate update functionality into their applications while maintaining security, performance, and reliability standards.

For additional support or questions, refer to the test suite in `src/test/java/com/hamza/salesmanagementbackend/UpdateSystemTestSuite.java` or contact the development team.
```
```
```