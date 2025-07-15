# Backend Update System Implementation Guide

## Overview

This document outlines the comprehensive backend implementation requirements for the remote update system for the Kotlin Compose Desktop Sales Management Application. The system enables real-time update notifications, automatic downloads, and seamless application updates.

## Current Application Context

- **Application Name**: نظام إدارة المبيعات (Sales Management System)
- **Current Version**: 2.0.0
- **Backend Base URL**: `http://localhost:8081`
- **Authentication**: JWT-based with Bearer tokens
- **Client Technology**: Kotlin Compose Desktop with Ktor HTTP client

## System Architecture

### Components Overview
1. **Update Management Service**: Core update logic and version management
2. **WebSocket Service**: Real-time notifications to connected clients
3. **File Management Service**: Secure upload, storage, and download of update packages
4. **Admin Web Interface**: Web-based administration panel
5. **Security Layer**: Authentication, authorization, and file validation

## Database Schema

### 1. Application Versions Table
```sql
CREATE TABLE application_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_number VARCHAR(20) NOT NULL UNIQUE,
    release_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    release_notes TEXT,
    minimum_client_version VARCHAR(20),
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_checksum VARCHAR(64) NOT NULL,
    download_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    INDEX idx_version_number (version_number),
    INDEX idx_is_active (is_active),
    INDEX idx_release_date (release_date)
);
```

### 2. Update Downloads Table
```sql
CREATE TABLE update_downloads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version_id BIGINT NOT NULL,
    client_identifier VARCHAR(255),
    download_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    download_completed_at TIMESTAMP NULL,
    download_status ENUM('STARTED', 'IN_PROGRESS', 'COMPLETED', 'FAILED') DEFAULT 'STARTED',
    client_ip VARCHAR(45),
    user_agent TEXT,
    
    FOREIGN KEY (version_id) REFERENCES application_versions(id) ON DELETE CASCADE,
    INDEX idx_version_id (version_id),
    INDEX idx_download_status (download_status),
    INDEX idx_client_identifier (client_identifier)
);
```

### 3. Connected Clients Table
```sql
CREATE TABLE connected_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    client_version VARCHAR(20),
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_ping_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(45),
    is_active BOOLEAN DEFAULT TRUE,
    
    INDEX idx_session_id (session_id),
    INDEX idx_client_version (client_version),
    INDEX idx_is_active (is_active)
);
```

## REST API Endpoints

### 1. Version Management Endpoints

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
        "releaseDate": "2025-01-15T10:00:00Z",
        "isMandatory": false,
        "releaseNotes": "Bug fixes and performance improvements",
        "minimumClientVersion": "2.0.0",
        "fileSize": 52428800,
        "checksum": "sha256:abc123...",
        "downloadUrl": "/api/v1/updates/download/2.1.0"
    }
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
        "releaseNotes": "Bug fixes and performance improvements"
    }
}
```

#### Download Update Package
```http
GET /api/v1/updates/download/{version}
Authorization: Bearer {token}
```

**Response:** Binary file stream with appropriate headers:
```http
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="sales-management-2.1.0.jar"
Content-Length: 52428800
X-Checksum: sha256:abc123...
```

### 2. Admin Management Endpoints

#### Get All Versions
```http
GET /api/v1/admin/updates/versions
Authorization: Bearer {admin-token}
```

#### Create New Version
```http
POST /api/v1/admin/updates/versions
Authorization: Bearer {admin-token}
Content-Type: multipart/form-data

{
    "versionNumber": "2.1.0",
    "isMandatory": false,
    "releaseNotes": "Bug fixes and performance improvements",
    "minimumClientVersion": "2.0.0",
    "file": [binary-file]
}
```

#### Update Version Status
```http
PUT /api/v1/admin/updates/versions/{id}/status
Authorization: Bearer {admin-token}

{
    "isActive": true
}
```

#### Delete Version
```http
DELETE /api/v1/admin/updates/versions/{id}
Authorization: Bearer {admin-token}
```

#### Get Download Statistics
```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer {admin-token}
```

## WebSocket Implementation

### 1. WebSocket Configuration

**Endpoint:** `ws://localhost:8081/ws/updates`

**Authentication:** JWT token via query parameter or header
```
ws://localhost:8081/ws/updates?token={jwt-token}
```

### 2. Message Formats

#### Client Registration Message
```json
{
    "type": "REGISTER",
    "data": {
        "clientVersion": "2.0.0",
        "clientId": "unique-client-identifier"
    }
}
```

#### Server Update Notification
```json
{
    "type": "UPDATE_AVAILABLE",
    "data": {
        "versionNumber": "2.1.0",
        "isMandatory": false,
        "releaseNotes": "Bug fixes and performance improvements",
        "downloadUrl": "/api/v1/updates/download/2.1.0",
        "fileSize": 52428800
    }
}
```

#### Client Heartbeat
```json
{
    "type": "PING",
    "timestamp": "2025-01-15T10:00:00Z"
}
```

#### Server Heartbeat Response
```json
{
    "type": "PONG",
    "timestamp": "2025-01-15T10:00:00Z"
}
```

## File Management System

### 1. Upload Requirements
- **Maximum File Size**: 500MB
- **Allowed File Types**: `.jar`, `.exe`, `.msi`, `.dmg`, `.deb`, `.rpm`
- **Storage Location**: `/var/updates/files/` or configurable directory
- **Naming Convention**: `{app-name}-{version}.{extension}`

### 2. File Validation
- **Checksum Verification**: SHA-256 hash calculation and storage
- **File Integrity**: Virus scanning integration (optional)
- **Size Validation**: Ensure file size matches uploaded content
- **Format Validation**: Verify file format matches extension

### 3. Storage Structure
```
/var/updates/
├── files/
│   ├── sales-management-2.0.0.jar
│   ├── sales-management-2.1.0.jar
│   └── sales-management-2.2.0.jar
├── temp/
│   └── [temporary upload files]
└── logs/
    └── [upload and download logs]
```

## Security Implementation

### 1. Authentication & Authorization
- **Admin Access**: Separate admin role with elevated permissions
- **Client Access**: Standard JWT authentication for update checks and downloads
- **Token Validation**: Verify token validity for all protected endpoints
- **Rate Limiting**: Implement rate limiting for download endpoints

### 2. File Security
- **Upload Validation**: Strict file type and size validation
- **Checksum Verification**: SHA-256 hash verification for integrity
- **Access Control**: Secure file access with proper authorization
- **Audit Logging**: Log all file operations and access attempts

### 3. WebSocket Security
- **Authentication**: JWT token validation for WebSocket connections
- **Connection Limits**: Limit concurrent connections per client
- **Message Validation**: Validate all incoming WebSocket messages
- **Heartbeat Monitoring**: Detect and handle stale connections

## Admin Web Interface Requirements

### 1. Dashboard Overview
- **Current Version Status**: Display active version information
- **Connected Clients**: Show real-time connected client count
- **Download Statistics**: Charts showing download trends
- **System Health**: Server status and performance metrics

### 2. Version Management
- **Version List**: Paginated table of all versions with status
- **Upload Interface**: Drag-and-drop file upload with progress
- **Version Details**: Edit release notes, mandatory status, etc.
- **Activation Controls**: Enable/disable version availability

### 3. Client Management
- **Connected Clients**: Real-time list of connected clients
- **Client Details**: Version information and connection status
- **Notification Controls**: Send targeted update notifications

### 4. Analytics & Reporting
- **Download Reports**: Detailed download statistics and trends
- **Version Adoption**: Charts showing version distribution
- **Error Monitoring**: Failed downloads and error tracking
- **Export Functionality**: CSV/Excel export of statistics

## Implementation Technologies

### 1. Spring Boot Dependencies
```xml
<!-- WebSocket Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- File Upload -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2. Configuration Properties
```yaml
# application.yml
app:
  updates:
    storage-path: /var/updates/files
    max-file-size: 500MB
    allowed-extensions: jar,exe,msi,dmg,deb,rpm
    websocket:
      heartbeat-interval: 30s
      connection-timeout: 5m
    security:
      admin-role: ADMIN
      rate-limit: 10/minute
```

## Error Handling

### 1. HTTP Error Responses
```json
{
    "success": false,
    "error": {
        "code": "UPDATE_NOT_FOUND",
        "message": "No update available for the specified version",
        "timestamp": "2025-01-15T10:00:00Z"
    }
}
```

### 2. WebSocket Error Messages
```json
{
    "type": "ERROR",
    "data": {
        "code": "AUTHENTICATION_FAILED",
        "message": "Invalid or expired token"
    }
}
```

## Monitoring & Logging

### 1. Application Metrics
- **Download Success Rate**: Percentage of successful downloads
- **WebSocket Connections**: Active connection count
- **File Storage Usage**: Disk space utilization
- **Response Times**: API endpoint performance

### 2. Audit Logging
- **File Operations**: Upload, download, and deletion events
- **Admin Actions**: Version management and configuration changes
- **Client Activities**: Connection events and update checks
- **Security Events**: Authentication failures and suspicious activities

## Deployment Considerations

### 1. Infrastructure Requirements
- **Storage**: Adequate disk space for multiple version files
- **Bandwidth**: Sufficient bandwidth for concurrent downloads
- **Database**: MySQL/PostgreSQL with proper indexing
- **Load Balancing**: Support for horizontal scaling

### 2. Backup & Recovery
- **File Backup**: Regular backup of update files
- **Database Backup**: Automated database backups
- **Disaster Recovery**: Recovery procedures for system failures
- **Version Rollback**: Ability to rollback to previous versions

This comprehensive guide provides all necessary specifications for implementing a robust, secure, and scalable update system for the Kotlin Compose Desktop Sales Management Application.
