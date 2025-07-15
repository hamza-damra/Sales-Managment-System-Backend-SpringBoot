# Frontend Update System Documentation

## Overview

This document provides comprehensive guidance for frontend developers to integrate with the Sales Management System's update system. The update system provides HTTP-based endpoints for checking updates, downloading files, and managing application versions.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [API Endpoints](#api-endpoints)
3. [Data Models](#data-models)
4. [Authentication](#authentication)
5. [Integration Guide](#integration-guide)
6. [Error Handling](#error-handling)
7. [Security Considerations](#security-considerations)
8. [Testing](#testing)
9. [Examples](#examples)

## Architecture Overview

### System Components

- **UpdateController**: Client-facing REST API endpoints
- **AdminUpdateController**: Administrative management endpoints
- **FileManagementService**: Secure file storage and download handling
- **UpdateManagementService**: Core update logic and version management
- **UpdateStatisticsService**: Analytics and reporting

### Communication Protocol

The update system uses **HTTP-based communication** for all operations:
- REST API endpoints for update checks and downloads
- JSON data format for all responses
- JWT authentication for secure access
- Resumable downloads with Range header support

## API Endpoints

### Base URLs

```
Production: https://your-domain.com/api/v1/updates
Development: http://localhost:8081/api/v1/updates
Admin: http://localhost:8081/api/v1/admin/updates
```

### Client Update Endpoints

#### 1. Get Latest Version

```http
GET /api/v1/updates/latest
Authorization: Bearer {jwt-token}
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
    "fileName": "sales-management-2.1.0.jar",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "fileChecksum": "sha256:abc123def456...",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "downloadCount": 150,
    "successfulDownloads": 145,
    "failedDownloads": 5
  },
  "message": "Latest version retrieved successfully"
}
```

#### 2. Check for Updates

```http
GET /api/v1/updates/check?currentVersion={version}
Authorization: Bearer {jwt-token}
```

**Parameters:**
- `currentVersion` (required): Current client version (e.g., "2.0.0")

**Response:**
```json
{
  "success": true,
  "data": {
    "updateAvailable": true,
    "latestVersion": "2.1.0",
    "currentVersion": "2.0.0",
    "isMandatory": false,
    "releaseNotes": "New features:\n- Enhanced reporting\n- Bug fixes",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "checksum": "sha256:abc123def456...",
    "minimumClientVersion": "2.0.0"
  },
  "message": "Update check completed successfully"
}
```

#### 3. Download Update

```http
GET /api/v1/updates/download/{version}
Authorization: Bearer {jwt-token}
Range: bytes=0-1023 (optional, for resumable downloads)
```

**Parameters:**
- `version` (path): Version number to download (e.g., "2.1.0")
- `Range` (header, optional): Byte range for resumable downloads

**Response:**
- **Success**: Binary file stream with appropriate headers
- **Headers**:
  - `Content-Type`: application/octet-stream
  - `Content-Length`: File size in bytes
  - `Content-Disposition`: attachment; filename="sales-management-2.1.0.jar"
  - `ETag`: File checksum for caching
  - `Accept-Ranges`: bytes (for resumable downloads)

### Admin Endpoints

#### 1. List All Versions

```http
GET /api/v1/admin/updates/versions?page=0&size=10&sort=releaseDate,desc
Authorization: Bearer {admin-jwt-token}
```

#### 2. Create New Version

```http
POST /api/v1/admin/updates/versions
Authorization: Bearer {admin-jwt-token}
Content-Type: multipart/form-data

Form Data:
- file: sales-management-2.2.0.jar
- versionNumber: 2.2.0
- isMandatory: false
- releaseNotes: New features and improvements
- minimumClientVersion: 2.0.0
```

#### 3. Update Version

```http
PUT /api/v1/admin/updates/versions/{id}
Authorization: Bearer {admin-jwt-token}
Content-Type: application/json

{
  "versionNumber": "2.1.1",
  "isMandatory": true,
  "isActive": true,
  "releaseNotes": "Critical security update",
  "minimumClientVersion": "2.0.0"
}
```

#### 4. Toggle Version Status

```http
PATCH /api/v1/admin/updates/versions/{id}/toggle-status
Authorization: Bearer {admin-jwt-token}
```

#### 5. Delete Version

```http
DELETE /api/v1/admin/updates/versions/{id}
Authorization: Bearer {admin-jwt-token}
```

#### 6. Get Statistics

```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer {admin-jwt-token}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "totalVersions": 5,
    "activeVersions": 3,
    "mandatoryVersions": 1,
    "averageFileSize": 45000000.0,
    "formattedAverageFileSize": "42.9 MB",
    "totalDownloads": 1250,
    "successfulDownloads": 1200,
    "failedDownloads": 50,
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
        "downloadCount": 500
      }
    ]
  }
}
```

## Data Models

### ApplicationVersionDTO

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
  downloadUrl: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  downloadCount?: number;
  successfulDownloads?: number;
  failedDownloads?: number;
}
```

### UpdateCheckResponseDTO

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

### UpdateStatisticsDTO

```typescript
interface UpdateStatisticsDTO {
  totalVersions: number;
  activeVersions: number;
  mandatoryVersions: number;
  averageFileSize: number;
  formattedAverageFileSize: string;
  totalDownloads: number;
  successfulDownloads: number;
  failedDownloads: number;
  downloadSuccessRate: number;
  dailyDownloadCounts: DailyDownloadCount[];
  topVersionsByDownloads: VersionDownloadStat[];
}

interface DailyDownloadCount {
  date: string; // YYYY-MM-DD format
  downloadCount: number;
}

interface VersionDownloadStat {
  versionNumber: string;
  downloadCount: number;
}
```

### ApiResponse Wrapper

```typescript
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message: string;
  timestamp?: string;
}

interface ErrorResponse {
  success: false;
  error: {
    status: number;
    error: string;
    message: string;
    errorCode: string;
    timestamp: string;
    suggestions?: string;
  };
}
```

## Authentication

### JWT Token Requirements

All API endpoints require JWT authentication:

```javascript
const headers = {
  'Authorization': `Bearer ${jwtToken}`,
  'Content-Type': 'application/json'
};
```

### Role-Based Access

- **USER role**: Access to client update endpoints
- **ADMIN role**: Access to all endpoints including admin management

### Token Refresh

Handle token expiration gracefully:

```javascript
if (response.status === 401) {
  // Token expired, refresh or redirect to login
  await refreshToken();
  // Retry the request
}
```

## Integration Guide

### 1. Basic Update Check Implementation

```javascript
class UpdateManager {
  constructor(baseUrl, authToken) {
    this.baseUrl = baseUrl;
    this.authToken = authToken;
    this.currentVersion = '2.0.0'; // Your app version
  }

  async checkForUpdates() {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/v1/updates/check?currentVersion=${this.currentVersion}`,
        {
          headers: {
            'Authorization': `Bearer ${this.authToken}`,
            'Content-Type': 'application/json'
          }
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const result = await response.json();

      if (result.success && result.data.updateAvailable) {
        this.handleUpdateAvailable(result.data);
      } else {
        console.log('No updates available');
      }

      return result.data;
    } catch (error) {
      console.error('Update check failed:', error);
      throw error;
    }
  }

  handleUpdateAvailable(updateInfo) {
    const message = updateInfo.isMandatory
      ? 'A mandatory update is available and must be installed.'
      : 'A new update is available. Would you like to download it?';

    if (confirm(`${message}\n\nVersion: ${updateInfo.latestVersion}\nSize: ${updateInfo.formattedFileSize}\n\nRelease Notes:\n${updateInfo.releaseNotes}`)) {
      this.downloadUpdate(updateInfo.latestVersion);
    }
  }

  async downloadUpdate(version) {
    try {
      const response = await fetch(
        `${this.baseUrl}/api/v1/updates/download/${version}`,
        {
          headers: {
            'Authorization': `Bearer ${this.authToken}`
          }
        }
      );

      if (!response.ok) {
        throw new Error(`Download failed: ${response.status}`);
      }

      // Handle the file download
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `sales-management-${version}.jar`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      console.log(`Update ${version} downloaded successfully`);
    } catch (error) {
      console.error('Download failed:', error);
      throw error;
    }
  }
}

// Usage
const updateManager = new UpdateManager('http://localhost:8081', 'your-jwt-token');
updateManager.checkForUpdates();
```

### 2. Resumable Download Implementation

```javascript
class ResumableDownloader {
  constructor(baseUrl, authToken) {
    this.baseUrl = baseUrl;
    this.authToken = authToken;
  }

  async downloadWithProgress(version, onProgress) {
    const downloadUrl = `${this.baseUrl}/api/v1/updates/download/${version}`;

    // First, get the file size
    const headResponse = await fetch(downloadUrl, {
      method: 'HEAD',
      headers: { 'Authorization': `Bearer ${this.authToken}` }
    });

    const totalSize = parseInt(headResponse.headers.get('Content-Length'));
    let downloadedBytes = 0;
    const chunks = [];

    const chunkSize = 1024 * 1024; // 1MB chunks

    while (downloadedBytes < totalSize) {
      const start = downloadedBytes;
      const end = Math.min(downloadedBytes + chunkSize - 1, totalSize - 1);

      const response = await fetch(downloadUrl, {
        headers: {
          'Authorization': `Bearer ${this.authToken}`,
          'Range': `bytes=${start}-${end}`
        }
      });

      if (!response.ok) {
        throw new Error(`Download failed: ${response.status}`);
      }

      const chunk = await response.arrayBuffer();
      chunks.push(chunk);
      downloadedBytes += chunk.byteLength;

      // Report progress
      const progress = (downloadedBytes / totalSize) * 100;
      onProgress(progress, downloadedBytes, totalSize);
    }

    // Combine chunks into final blob
    const blob = new Blob(chunks);
    return blob;
  }
}

// Usage with progress tracking
const downloader = new ResumableDownloader('http://localhost:8081', 'your-jwt-token');

downloader.downloadWithProgress('2.1.0', (progress, downloaded, total) => {
  console.log(`Download progress: ${progress.toFixed(1)}% (${downloaded}/${total} bytes)`);
  // Update your progress bar here
}).then(blob => {
  console.log('Download completed');
  // Save the blob as a file
}).catch(error => {
  console.error('Download failed:', error);
});
```

### 3. Admin Panel Integration

```javascript
class AdminUpdateManager {
  constructor(baseUrl, adminToken) {
    this.baseUrl = baseUrl;
    this.adminToken = adminToken;
  }

  async getAllVersions(page = 0, size = 10) {
    const response = await fetch(
      `${this.baseUrl}/api/v1/admin/updates/versions?page=${page}&size=${size}&sort=releaseDate,desc`,
      {
        headers: {
          'Authorization': `Bearer ${this.adminToken}`,
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch versions: ${response.status}`);
    }

    return await response.json();
  }

  async createVersion(formData) {
    const response = await fetch(
      `${this.baseUrl}/api/v1/admin/updates/versions`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.adminToken}`
          // Don't set Content-Type for FormData
        },
        body: formData
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to create version: ${response.status}`);
    }

    return await response.json();
  }

  async toggleVersionStatus(versionId) {
    const response = await fetch(
      `${this.baseUrl}/api/v1/admin/updates/versions/${versionId}/toggle-status`,
      {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${this.adminToken}`
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to toggle version status: ${response.status}`);
    }

    return await response.json();
  }

  async deleteVersion(versionId) {
    const response = await fetch(
      `${this.baseUrl}/api/v1/admin/updates/versions/${versionId}`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${this.adminToken}`
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to delete version: ${response.status}`);
    }

    return await response.json();
  }

  async getStatistics() {
    const response = await fetch(
      `${this.baseUrl}/api/v1/admin/updates/statistics`,
      {
        headers: {
          'Authorization': `Bearer ${this.adminToken}`,
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to fetch statistics: ${response.status}`);
    }

    return await response.json();
  }
}
```

## Error Handling

### Common Error Scenarios

#### 1. Authentication Errors

```javascript
class UpdateErrorHandler {
  static handleApiError(error, response) {
    if (response?.status === 401) {
      return {
        type: 'AUTH_ERROR',
        message: 'Authentication failed. Please log in again.',
        requiresReauth: true,
        retry: false
      };
    }

    if (response?.status === 403) {
      return {
        type: 'PERMISSION_ERROR',
        message: 'You do not have permission to perform this action.',
        retry: false
      };
    }

    if (response?.status === 404) {
      return {
        type: 'NOT_FOUND',
        message: 'The requested version was not found.',
        retry: false
      };
    }

    if (response?.status >= 500) {
      return {
        type: 'SERVER_ERROR',
        message: 'Server error occurred. Please try again later.',
        retry: true
      };
    }

    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      return {
        type: 'NETWORK_ERROR',
        message: 'Network connection failed. Please check your internet connection.',
        retry: true
      };
    }

    return {
      type: 'UNKNOWN_ERROR',
      message: error.message || 'An unexpected error occurred.',
      retry: false
    };
  }

  static async handleWithRetry(operation, maxRetries = 3, delay = 1000) {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        return await operation();
      } catch (error) {
        const errorInfo = this.handleApiError(error);

        if (!errorInfo.retry || attempt === maxRetries) {
          throw errorInfo;
        }

        console.warn(`Attempt ${attempt} failed, retrying in ${delay}ms...`);
        await new Promise(resolve => setTimeout(resolve, delay));
        delay *= 2; // Exponential backoff
      }
    }
  }
}

// Usage
try {
  const result = await UpdateErrorHandler.handleWithRetry(
    () => updateManager.checkForUpdates(),
    3, // max retries
    1000 // initial delay
  );
} catch (errorInfo) {
  if (errorInfo.requiresReauth) {
    // Redirect to login
    window.location.href = '/login';
  } else {
    // Show error message to user
    showErrorNotification(errorInfo.message);
  }
}
```

#### 2. File Upload Error Handling

```javascript
async function uploadVersionFile(file, versionData, onProgress) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('versionNumber', versionData.versionNumber);
  formData.append('isMandatory', versionData.isMandatory);
  formData.append('releaseNotes', versionData.releaseNotes);
  formData.append('minimumClientVersion', versionData.minimumClientVersion || '');

  try {
    const xhr = new XMLHttpRequest();

    return new Promise((resolve, reject) => {
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          const progress = (e.loaded / e.total) * 100;
          onProgress(progress);
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve(JSON.parse(xhr.responseText));
        } else {
          const error = JSON.parse(xhr.responseText);
          reject(new Error(error.message || `Upload failed: ${xhr.status}`));
        }
      });

      xhr.addEventListener('error', () => {
        reject(new Error('Network error during upload'));
      });

      xhr.addEventListener('timeout', () => {
        reject(new Error('Upload timeout'));
      });

      xhr.open('POST', `${baseUrl}/api/v1/admin/updates/versions`);
      xhr.setRequestHeader('Authorization', `Bearer ${adminToken}`);
      xhr.timeout = 300000; // 5 minutes timeout
      xhr.send(formData);
    });
  } catch (error) {
    throw new Error(`Upload preparation failed: ${error.message}`);
  }
}
```

### Error Response Format

All API errors follow this format:

```json
{
  "success": false,
  "error": {
    "status": 404,
    "error": "Not Found",
    "message": "Update version '999.0.0' not found or not active",
    "errorCode": "UPDATE_NOT_FOUND",
    "timestamp": "2024-01-15T10:30:00Z",
    "suggestions": "Please verify the version number or check if there are any active versions available."
  }
}
```

## Security Considerations

### 1. Input Validation

```javascript
class ValidationUtils {
  static validateVersionNumber(version) {
    const versionRegex = /^\d+\.\d+\.\d+$/;
    if (!versionRegex.test(version)) {
      throw new Error('Version number must be in format X.Y.Z (e.g., 2.1.0)');
    }
    return true;
  }

  static validateFileType(file) {
    const allowedTypes = [
      'application/java-archive', // .jar
      'application/x-msdownload', // .exe
      'application/x-msi', // .msi
      'application/x-apple-diskimage', // .dmg
      'application/x-debian-package', // .deb
      'application/x-rpm' // .rpm
    ];

    const allowedExtensions = ['jar', 'exe', 'msi', 'dmg', 'deb', 'rpm'];
    const fileExtension = file.name.split('.').pop().toLowerCase();

    if (!allowedExtensions.includes(fileExtension)) {
      throw new Error(`File type not allowed. Allowed types: ${allowedExtensions.join(', ')}`);
    }

    return true;
  }

  static validateFileSize(file, maxSizeMB = 500) {
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxSizeBytes) {
      throw new Error(`File size exceeds maximum allowed size of ${maxSizeMB}MB`);
    }
    return true;
  }

  static sanitizeInput(input) {
    if (typeof input !== 'string') return input;

    // Remove potentially dangerous characters
    return input
      .replace(/[<>]/g, '') // Remove angle brackets
      .replace(/javascript:/gi, '') // Remove javascript: protocol
      .replace(/on\w+=/gi, '') // Remove event handlers
      .trim();
  }
}

// Usage
try {
  ValidationUtils.validateVersionNumber('2.1.0');
  ValidationUtils.validateFileType(selectedFile);
  ValidationUtils.validateFileSize(selectedFile);

  const sanitizedNotes = ValidationUtils.sanitizeInput(releaseNotes);
} catch (error) {
  showValidationError(error.message);
}
```

### 2. Secure File Handling

```javascript
class SecureFileHandler {
  static async verifyChecksum(file, expectedChecksum) {
    const arrayBuffer = await file.arrayBuffer();
    const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
    const calculatedChecksum = `sha256:${hashHex}`;

    if (calculatedChecksum !== expectedChecksum) {
      throw new Error('File checksum verification failed. File may be corrupted.');
    }

    return true;
  }

  static createSecureDownloadLink(blob, filename) {
    // Create object URL with security considerations
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.style.display = 'none';

    // Add to DOM temporarily
    document.body.appendChild(link);
    link.click();

    // Clean up immediately
    document.body.removeChild(link);

    // Revoke object URL after a short delay to ensure download starts
    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  static async scanFileContent(file) {
    // Basic file content validation
    const firstBytes = await file.slice(0, 1024).arrayBuffer();
    const bytes = new Uint8Array(firstBytes);

    // Check for common file signatures
    const signatures = {
      jar: [0x50, 0x4B], // ZIP/JAR signature
      exe: [0x4D, 0x5A], // PE executable signature
    };

    const fileExt = file.name.split('.').pop().toLowerCase();
    const expectedSig = signatures[fileExt];

    if (expectedSig && !expectedSig.every((byte, i) => bytes[i] === byte)) {
      console.warn('File signature does not match expected type');
    }

    return true;
  }
}
```

## Testing

### 1. Unit Testing Examples

```javascript
// Jest test examples
describe('UpdateManager', () => {
  let updateManager;
  let mockFetch;

  beforeEach(() => {
    mockFetch = jest.fn();
    global.fetch = mockFetch;
    updateManager = new UpdateManager('http://localhost:8081', 'test-token');
  });

  test('should check for updates successfully', async () => {
    const mockResponse = {
      success: true,
      data: {
        updateAvailable: true,
        latestVersion: '2.1.0',
        currentVersion: '2.0.0',
        isMandatory: false
      }
    };

    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: () => Promise.resolve(mockResponse)
    });

    const result = await updateManager.checkForUpdates();

    expect(result.updateAvailable).toBe(true);
    expect(result.latestVersion).toBe('2.1.0');
    expect(mockFetch).toHaveBeenCalledWith(
      'http://localhost:8081/api/v1/updates/check?currentVersion=2.0.0',
      expect.objectContaining({
        headers: expect.objectContaining({
          'Authorization': 'Bearer test-token'
        })
      })
    );
  });

  test('should handle network errors', async () => {
    mockFetch.mockRejectedValueOnce(new Error('Network error'));

    await expect(updateManager.checkForUpdates()).rejects.toThrow('Network error');
  });

  test('should handle HTTP errors', async () => {
    mockFetch.mockResolvedValueOnce({
      ok: false,
      status: 404,
      statusText: 'Not Found'
    });

    await expect(updateManager.checkForUpdates()).rejects.toThrow('HTTP 404: Not Found');
  });
});

describe('ValidationUtils', () => {
  test('should validate version numbers correctly', () => {
    expect(() => ValidationUtils.validateVersionNumber('2.1.0')).not.toThrow();
    expect(() => ValidationUtils.validateVersionNumber('invalid')).toThrow();
    expect(() => ValidationUtils.validateVersionNumber('2.1')).toThrow();
  });

  test('should validate file types', () => {
    const jarFile = new File(['content'], 'app.jar', { type: 'application/java-archive' });
    const txtFile = new File(['content'], 'readme.txt', { type: 'text/plain' });

    expect(() => ValidationUtils.validateFileType(jarFile)).not.toThrow();
    expect(() => ValidationUtils.validateFileType(txtFile)).toThrow();
  });
});
```

### 2. Integration Testing

```javascript
// Cypress integration test example
describe('Update System Integration', () => {
  beforeEach(() => {
    cy.login('admin', 'password'); // Custom command for authentication
    cy.visit('/admin/updates');
  });

  it('should display version list', () => {
    cy.get('[data-testid="version-list"]').should('be.visible');
    cy.get('[data-testid="version-item"]').should('have.length.greaterThan', 0);
  });

  it('should create new version', () => {
    cy.get('[data-testid="create-version-btn"]').click();

    cy.get('[data-testid="version-number"]').type('2.2.0');
    cy.get('[data-testid="release-notes"]').type('Test release notes');
    cy.get('[data-testid="file-input"]').selectFile('test-files/app-2.2.0.jar');

    cy.get('[data-testid="submit-btn"]').click();

    cy.get('[data-testid="success-message"]').should('contain', 'Version created successfully');
  });

  it('should check for updates', () => {
    cy.visit('/app');
    cy.get('[data-testid="check-updates-btn"]').click();

    cy.get('[data-testid="update-dialog"]').should('be.visible');
    cy.get('[data-testid="update-info"]').should('contain', 'Version');
  });
});
```

### 3. Performance Testing

```javascript
// Performance testing utilities
class PerformanceMonitor {
  static async measureDownloadSpeed(downloadFunction) {
    const startTime = performance.now();
    const startBytes = 0;

    let totalBytes = 0;
    const onProgress = (progress, downloaded, total) => {
      totalBytes = downloaded;
    };

    await downloadFunction(onProgress);

    const endTime = performance.now();
    const duration = (endTime - startTime) / 1000; // seconds
    const speed = totalBytes / duration; // bytes per second

    return {
      duration,
      totalBytes,
      speed: speed / (1024 * 1024), // MB/s
      speedFormatted: `${(speed / (1024 * 1024)).toFixed(2)} MB/s`
    };
  }

  static async testConcurrentDownloads(downloadFunction, concurrency = 5) {
    const promises = Array(concurrency).fill().map(() => downloadFunction());

    const startTime = performance.now();
    const results = await Promise.allSettled(promises);
    const endTime = performance.now();

    const successful = results.filter(r => r.status === 'fulfilled').length;
    const failed = results.filter(r => r.status === 'rejected').length;

    return {
      duration: endTime - startTime,
      successful,
      failed,
      successRate: (successful / concurrency) * 100
    };
  }
}

// Usage
const stats = await PerformanceMonitor.measureDownloadSpeed(
  (onProgress) => downloader.downloadWithProgress('2.1.0', onProgress)
);
console.log(`Download completed at ${stats.speedFormatted}`);
```

## Examples

### 1. Complete React Component Example

```jsx
import React, { useState, useEffect } from 'react';
import { UpdateManager, UpdateErrorHandler } from './updateUtils';

const UpdateChecker = ({ authToken, currentVersion }) => {
  const [updateInfo, setUpdateInfo] = useState(null);
  const [isChecking, setIsChecking] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadProgress, setDownloadProgress] = useState(0);
  const [error, setError] = useState(null);

  const updateManager = new UpdateManager('http://localhost:8081', authToken);

  useEffect(() => {
    checkForUpdates();
  }, []);

  const checkForUpdates = async () => {
    setIsChecking(true);
    setError(null);

    try {
      const result = await UpdateErrorHandler.handleWithRetry(
        () => updateManager.checkForUpdates()
      );

      setUpdateInfo(result);
    } catch (errorInfo) {
      setError(errorInfo);
    } finally {
      setIsChecking(false);
    }
  };

  const handleDownload = async () => {
    if (!updateInfo) return;

    setIsDownloading(true);
    setDownloadProgress(0);

    try {
      await updateManager.downloadWithProgress(
        updateInfo.latestVersion,
        (progress) => setDownloadProgress(progress)
      );

      alert('Update downloaded successfully!');
    } catch (error) {
      setError({ message: 'Download failed: ' + error.message });
    } finally {
      setIsDownloading(false);
      setDownloadProgress(0);
    }
  };

  if (isChecking) {
    return <div className="update-checker">Checking for updates...</div>;
  }

  if (error) {
    return (
      <div className="update-checker error">
        <p>Error: {error.message}</p>
        <button onClick={checkForUpdates}>Retry</button>
      </div>
    );
  }

  if (!updateInfo?.updateAvailable) {
    return (
      <div className="update-checker">
        <p>You're running the latest version ({currentVersion})</p>
        <button onClick={checkForUpdates}>Check Again</button>
      </div>
    );
  }

  return (
    <div className="update-checker">
      <h3>Update Available</h3>
      <p>
        New version {updateInfo.latestVersion} is available
        {updateInfo.isMandatory && ' (Mandatory)'}
      </p>
      <p>Size: {updateInfo.formattedFileSize}</p>

      <details>
        <summary>Release Notes</summary>
        <pre>{updateInfo.releaseNotes}</pre>
      </details>

      {isDownloading ? (
        <div className="download-progress">
          <div className="progress-bar">
            <div
              className="progress-fill"
              style={{ width: `${downloadProgress}%` }}
            />
          </div>
          <p>{downloadProgress.toFixed(1)}% downloaded</p>
        </div>
      ) : (
        <div className="update-actions">
          <button onClick={handleDownload} className="download-btn">
            Download Update
          </button>
          {!updateInfo.isMandatory && (
            <button onClick={() => setUpdateInfo(null)} className="dismiss-btn">
              Dismiss
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default UpdateChecker;
```

### 2. Admin Dashboard Component

```jsx
import React, { useState, useEffect } from 'react';
import { AdminUpdateManager } from './updateUtils';

const AdminDashboard = ({ adminToken }) => {
  const [versions, setVersions] = useState([]);
  const [statistics, setStatistics] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const adminManager = new AdminUpdateManager('http://localhost:8081', adminToken);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [versionsResult, statsResult] = await Promise.all([
        adminManager.getAllVersions(),
        adminManager.getStatistics()
      ]);

      setVersions(versionsResult.data.content);
      setStatistics(statsResult.data);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleStatus = async (versionId) => {
    try {
      await adminManager.toggleVersionStatus(versionId);
      await loadData(); // Refresh data
    } catch (error) {
      alert('Failed to toggle version status: ' + error.message);
    }
  };

  const handleDelete = async (versionId) => {
    if (!confirm('Are you sure you want to delete this version?')) return;

    try {
      await adminManager.deleteVersion(versionId);
      await loadData(); // Refresh data
    } catch (error) {
      alert('Failed to delete version: ' + error.message);
    }
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <div className="admin-dashboard">
      <h2>Update System Dashboard</h2>

      {statistics && (
        <div className="statistics">
          <div className="stat-card">
            <h3>Total Versions</h3>
            <p>{statistics.totalVersions}</p>
          </div>
          <div className="stat-card">
            <h3>Active Versions</h3>
            <p>{statistics.activeVersions}</p>
          </div>
          <div className="stat-card">
            <h3>Total Downloads</h3>
            <p>{statistics.totalDownloads}</p>
          </div>
          <div className="stat-card">
            <h3>Success Rate</h3>
            <p>{statistics.downloadSuccessRate.toFixed(1)}%</p>
          </div>
        </div>
      )}

      <div className="versions-list">
        <h3>Versions</h3>
        <table>
          <thead>
            <tr>
              <th>Version</th>
              <th>Release Date</th>
              <th>Status</th>
              <th>Mandatory</th>
              <th>Downloads</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {versions.map(version => (
              <tr key={version.id}>
                <td>{version.versionNumber}</td>
                <td>{new Date(version.releaseDate).toLocaleDateString()}</td>
                <td>
                  <span className={`status ${version.isActive ? 'active' : 'inactive'}`}>
                    {version.isActive ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>{version.isMandatory ? 'Yes' : 'No'}</td>
                <td>{version.downloadCount || 0}</td>
                <td>
                  <button onClick={() => handleToggleStatus(version.id)}>
                    {version.isActive ? 'Deactivate' : 'Activate'}
                  </button>
                  <button onClick={() => handleDelete(version.id)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminDashboard;
```

## Configuration

### Environment Variables

```javascript
// config.js
const config = {
  apiBaseUrl: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081',
  updateCheckInterval: parseInt(process.env.REACT_APP_UPDATE_CHECK_INTERVAL) || 3600000, // 1 hour
  maxFileSize: parseInt(process.env.REACT_APP_MAX_FILE_SIZE) || 524288000, // 500MB
  allowedFileTypes: (process.env.REACT_APP_ALLOWED_FILE_TYPES || 'jar,exe,msi,dmg,deb,rpm').split(','),
  enableAutoUpdates: process.env.REACT_APP_ENABLE_AUTO_UPDATES === 'true',
  retryAttempts: parseInt(process.env.REACT_APP_RETRY_ATTEMPTS) || 3,
  retryDelay: parseInt(process.env.REACT_APP_RETRY_DELAY) || 1000
};

export default config;
```

### CSS Styles

```css
/* update-system.css */
.update-checker {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin: 20px 0;
}

.update-checker.error {
  border-color: #dc3545;
  background-color: #f8d7da;
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #e9ecef;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #007bff;
  transition: width 0.3s ease;
}

.statistics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
}

.status.active {
  color: #28a745;
  font-weight: bold;
}

.status.inactive {
  color: #6c757d;
}
```

This comprehensive documentation provides frontend developers with everything they need to integrate with the update system, including complete code examples, error handling strategies, security considerations, and testing approaches.
```
```
