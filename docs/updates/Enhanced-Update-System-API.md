# Enhanced Update System API Documentation

## Overview

The Enhanced Update System provides comprehensive backend functionality for distributing versioned JAR files to Kotlin Compose desktop applications. It extends the existing update infrastructure with advanced features including differential updates, compatibility validation, rate limiting, real-time notifications, and multi-channel release management.

## Architecture Components

### Core Features
- **JAR-Specific Updates**: Optimized for JAR file distribution with metadata-only endpoints
- **Resumable Downloads**: HTTP Range header support for interrupted downloads
- **Compatibility Validation**: Pre-update system compatibility checks
- **Version Rollback**: Management and distribution of previous stable versions
- **Differential Updates**: Delta updates for efficient bandwidth usage
- **Real-Time Notifications**: WebSocket-based update notifications
- **Rate Limiting**: Per-client request throttling with exponential backoff
- **Release Channels**: Support for stable, beta, nightly, LTS, and hotfix channels
- **Analytics**: Comprehensive update analytics and reporting

### Database Schema Extensions
- **ReleaseChannel**: Enum for channel types (STABLE, BETA, NIGHTLY, LTS, HOTFIX)
- **VersionHistory**: Tracks update/rollback actions for audit trails
- **UpdateAnalytics**: Detailed analytics for downloads, installations, and errors
- **RateLimitTracker**: Per-client rate limiting with violation tracking

## API Endpoints

### Base URL
```
http://localhost:8081/api/v1/updates
```

### Authentication
All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### Rate Limiting
- **Headers**: `X-RateLimit-Remaining`, `X-RateLimit-Reset`
- **Status Code**: `429 Too Many Requests` when rate limit exceeded
- **Limits**: Configurable per endpoint type (see configuration section)

## Client Update Endpoints

### 1. Get Version Metadata (NEW)
```http
GET /api/v1/updates/metadata/{version}
```

**Description**: Retrieve JAR file metadata without initiating download.

**Parameters**:
- `version` (path): Version number (e.g., "2.1.0")

**Response**:
```json
{
  "success": true,
  "data": {
    "versionNumber": "2.1.0",
    "releaseDate": "2024-01-15T10:30:00",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Bug fixes and performance improvements",
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

**Rate Limit**: 30 requests per minute

### 2. Check System Compatibility (NEW)
```http
GET /api/v1/updates/compatibility/{version}?clientVersion={clientVersion}&java.version={javaVersion}&os.name={osName}&available.memory.mb={memoryMB}&available.disk.mb={diskMB}
```

**Description**: Validate client system compatibility before update.

**Parameters**:
- `version` (path): Target version number
- `clientVersion` (query): Current client version
- System info parameters (query): Java version, OS details, memory, disk space

**Response**:
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
      "vendor": "Eclipse Adoptium",
      "isCompatible": true
    },
    "operatingSystem": {
      "name": "Windows 10",
      "version": "10.0",
      "architecture": "amd64",
      "isSupported": true
    },
    "systemRequirements": {
      "minimumMemoryMB": 512,
      "availableMemoryMB": 8192,
      "minimumDiskSpaceMB": 1024,
      "availableDiskSpaceMB": 50000,
      "additionalRequirements": {
        "Network Connection": "Required for download",
        "Administrator Rights": "May be required for installation"
      }
    },
    "compatibilityIssues": [],
    "recommendations": [],
    "canProceed": true,
    "warningLevel": "NONE"
  },
  "message": "Compatibility check completed successfully"
}
```

**Rate Limit**: 10 requests per minute

### 3. Get Differential Update (NEW)
```http
GET /api/v1/updates/delta/{fromVersion}/{toVersion}
```

**Description**: Get differential update information between two versions.

**Parameters**:
- `fromVersion` (path): Source version
- `toVersion` (path): Target version

**Response**:
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
    "compressionRatio": 0.9,
    "deltaChecksum": "sha256:def456...",
    "deltaDownloadUrl": "/api/v1/updates/delta/2.0.0/2.1.0/download",
    "fullDownloadUrl": "/api/v1/updates/download/2.1.0",
    "changedFiles": [
      {
        "path": "com/example/UpdatedClass.class",
        "operation": "MODIFIED",
        "oldChecksum": "sha256:old123...",
        "newChecksum": "sha256:new456...",
        "size": 2048
      }
    ],
    "patchInstructions": [
      {
        "order": 1,
        "operation": "EXTRACT",
        "target": "com/example/UpdatedClass.class",
        "source": "delta.zip:com/example/UpdatedClass.class",
        "checksum": "sha256:new456..."
      }
    ],
    "fallbackToFull": false,
    "estimatedApplyTimeSeconds": 15,
    "createdAt": "2024-01-15T10:30:00",
    "expiresAt": "2024-02-14T10:30:00"
  },
  "message": "Differential update information retrieved successfully"
}
```

**Rate Limit**: 5 requests per minute

### 4. Download Differential Update (NEW)
```http
GET /api/v1/updates/delta/{fromVersion}/{toVersion}/download
```

**Description**: Download differential update file.

**Headers**:
- `Range` (optional): For resumable downloads

**Response**: Binary delta file with appropriate headers

**Rate Limit**: 5 downloads per minute

### 5. Get Release Channels (NEW)
```http
GET /api/v1/updates/channels
```

**Description**: Get available release channels.

**Response**:
```json
{
  "success": true,
  "data": {
    "stable": {
      "channel": "STABLE",
      "description": "Stable releases with thorough testing",
      "isActive": true,
      "stabilityLevel": "STABLE",
      "autoUpdateEnabled": true,
      "requiresApproval": false
    },
    "beta": {
      "channel": "BETA",
      "description": "Beta releases for testing new features",
      "isActive": true,
      "stabilityLevel": "BETA",
      "autoUpdateEnabled": false,
      "requiresApproval": true
    }
  },
  "message": "Available release channels retrieved successfully"
}
```

### 6. Get Latest Version for Channel (NEW)
```http
GET /api/v1/updates/channels/{channel}/latest
```

**Description**: Get latest version for specific release channel.

**Parameters**:
- `channel` (path): Channel name (stable, beta, nightly, lts, hotfix)

**Response**: Same as existing `/latest` endpoint

## WebSocket Real-Time Notifications

### Connection
```
ws://localhost:8081/ws/updates?token={jwt-token}
```

### Authentication
- JWT token via query parameter: `?token={jwt-token}`
- Or via header: `Authorization: Bearer {jwt-token}`

### Message Format
```json
{
  "type": "MESSAGE_TYPE",
  "data": {
    // Message-specific data
  }
}
```

### Client Messages

#### Register Client
```json
{
  "type": "REGISTER",
  "data": {
    "clientVersion": "2.0.0",
    "clientId": "unique-client-identifier"
  }
}
```

#### Subscribe to Channel
```json
{
  "type": "SUBSCRIBE",
  "data": {
    "channel": "beta"
  }
}
```

#### Heartbeat
```json
{
  "type": "PING",
  "data": {}
}
```

### Server Messages

#### New Version Available
```json
{
  "type": "NEW_VERSION_AVAILABLE",
  "data": {
    "version": "2.1.0",
    "releaseDate": "2024-01-15T10:30:00",
    "isMandatory": false,
    "releaseNotes": "Bug fixes and improvements",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "checksum": "sha256:abc123...",
    "releaseChannel": "STABLE",
    "timestamp": "2024-01-15T10:30:00"
  }
}
```

#### Download Progress
```json
{
  "type": "DOWNLOAD_PROGRESS",
  "data": {
    "clientId": "client-123",
    "version": "2.1.0",
    "progressPercentage": 45,
    "bytesDownloaded": 23592960,
    "totalBytes": 52428800,
    "timestamp": "2024-01-15T10:35:00"
  }
}
```

#### Installation Progress
```json
{
  "type": "INSTALLATION_PROGRESS",
  "data": {
    "clientId": "client-123",
    "version": "2.1.0",
    "phase": "EXTRACTING",
    "progressPercentage": 75,
    "currentOperation": "Extracting JAR contents",
    "timestamp": "2024-01-15T10:40:00"
  }
}
```

#### Rate Limited
```json
{
  "type": "RATE_LIMITED",
  "data": {
    "clientId": "client-123",
    "endpoint": "DOWNLOAD",
    "resetTimeSeconds": 300,
    "message": "Rate limit exceeded. Please wait before making more requests.",
    "timestamp": "2024-01-15T10:45:00"
  }
}
```

## Configuration

### Application Properties
```properties
# Enhanced Update System Configuration
app.updates.storage-path=./versions
app.updates.max-file-size=524288000
app.updates.allowed-extensions=jar,exe,msi,dmg,deb,rpm
app.updates.enable-resumable-downloads=true
app.updates.cleanup-orphaned-files=true

# WebSocket Configuration
app.updates.websocket.heartbeat-interval=30000
app.updates.websocket.connection-timeout=300000

# Security Configuration
app.updates.security.admin-role=ADMIN
app.updates.security.rate-limit=10

# Differential Updates
app.updates.delta.max-size-mb=100
app.updates.delta.compression-threshold=0.3

# Rate Limiting (requests per minute)
app.updates.rate-limit.update-check=20
app.updates.rate-limit.download=5
app.updates.rate-limit.metadata=30
app.updates.rate-limit.compatibility=10
app.updates.rate-limit.analytics=15
app.updates.rate-limit.rollback=3
app.updates.rate-limit.delta=5
app.updates.rate-limit.websocket=10
```

## Error Handling

### Common Error Responses

#### Rate Limited (429)
```json
{
  "success": false,
  "message": "Rate limit exceeded. Try again in 300 seconds.",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Version Not Found (404)
```json
{
  "success": false,
  "message": "Version not found: 2.1.0",
  "timestamp": "2024-01-15T10:30:00"
}
```

#### Compatibility Issues (200 with warnings)
```json
{
  "success": true,
  "data": {
    "isCompatible": false,
    "compatibilityIssues": [
      {
        "type": "JAVA_VERSION",
        "severity": "CRITICAL",
        "description": "Java 8 detected, but Java 11 or higher is required",
        "resolution": "Install Java 11 or higher",
        "component": "Java Runtime"
      }
    ],
    "canProceed": false,
    "warningLevel": "CRITICAL"
  }
}
```

## Integration Examples

### Kotlin Compose Desktop Client

#### Basic Update Check
```kotlin
class UpdateService {
    private val client = HttpClient()
    private val baseUrl = "http://localhost:8081/api/v1/updates"
    
    suspend fun checkForUpdates(currentVersion: String): UpdateCheckResponse {
        return client.get("$baseUrl/check") {
            parameter("currentVersion", currentVersion)
            header("Authorization", "Bearer $jwtToken")
        }.body()
    }
    
    suspend fun getVersionMetadata(version: String): UpdateMetadata {
        return client.get("$baseUrl/metadata/$version") {
            header("Authorization", "Bearer $jwtToken")
        }.body()
    }
    
    suspend fun checkCompatibility(
        targetVersion: String,
        clientVersion: String,
        systemInfo: Map<String, String>
    ): CompatibilityCheck {
        return client.get("$baseUrl/compatibility/$targetVersion") {
            parameter("clientVersion", clientVersion)
            systemInfo.forEach { (key, value) ->
                parameter(key, value)
            }
            header("Authorization", "Bearer $jwtToken")
        }.body()
    }
}
```

#### WebSocket Integration
```kotlin
class UpdateNotificationService {
    private var webSocket: WebSocket? = null
    
    fun connect(token: String) {
        val client = HttpClient {
            install(WebSockets)
        }
        
        client.webSocket(
            method = HttpMethod.Get,
            host = "localhost",
            port = 8081,
            path = "/ws/updates?token=$token"
        ) {
            // Register client
            send(Frame.Text("""
                {
                    "type": "REGISTER",
                    "data": {
                        "clientVersion": "2.0.0",
                        "clientId": "desktop-client-123"
                    }
                }
            """.trimIndent()))
            
            // Listen for messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val message = Json.decodeFromString<WebSocketMessage>(frame.readText())
                        handleMessage(message)
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun handleMessage(message: WebSocketMessage) {
        when (message.type) {
            "NEW_VERSION_AVAILABLE" -> {
                // Show update notification to user
                showUpdateNotification(message.data)
            }
            "DOWNLOAD_PROGRESS" -> {
                // Update progress bar
                updateDownloadProgress(message.data)
            }
            "RATE_LIMITED" -> {
                // Show rate limit warning
                showRateLimitWarning(message.data)
            }
        }
    }
}
```

## Security Considerations

1. **Authentication**: All endpoints require valid JWT tokens
2. **Rate Limiting**: Prevents abuse with configurable limits per endpoint
3. **Input Validation**: All parameters are validated and sanitized
4. **File Integrity**: SHA-256 checksums for all downloads
5. **WebSocket Security**: Token-based authentication for WebSocket connections
6. **CORS**: Configurable allowed origins for web clients

## Performance Optimizations

1. **Differential Updates**: Reduce bandwidth usage by up to 90%
2. **Resumable Downloads**: Handle network interruptions gracefully
3. **Caching**: Metadata and compatibility checks are cached
4. **Async Processing**: Non-blocking operations for better throughput
5. **Connection Pooling**: Efficient database connection management

## Monitoring and Analytics

The system provides comprehensive analytics through the existing statistics endpoints, enhanced with:

- Download success/failure rates by version and channel
- Geographic distribution of downloads
- Client version adoption metrics
- Rate limiting statistics
- WebSocket connection metrics
- Differential update usage statistics

For detailed analytics API documentation, see the existing Update System API documentation.
