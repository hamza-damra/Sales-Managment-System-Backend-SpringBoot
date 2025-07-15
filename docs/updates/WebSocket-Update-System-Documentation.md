# WebSocket-Based Update System Documentation

## Overview

This document provides comprehensive guidance for frontend developers to integrate with the Sales Management System's enhanced WebSocket-based update system. The system combines traditional HTTP REST APIs with real-time WebSocket communication for instant update notifications and progress tracking.

## Table of Contents

1. [System Architecture](#system-architecture)
2. [WebSocket Connection Setup](#websocket-connection-setup)
3. [Real-time Message Formats](#real-time-message-formats)
4. [API Endpoints](#api-endpoints)
5. [Authentication & Authorization](#authentication--authorization)
6. [Rate Limiting & Security](#rate-limiting--security)
7. [Integration Guide](#integration-guide)
8. [Error Handling](#error-handling)
9. [Usage Examples](#usage-examples)
10. [Testing & Troubleshooting](#testing--troubleshooting)

## System Architecture

### Core Components

#### WebSocket Components
- **UpdateWebSocketHandler**: Manages WebSocket connections and message routing
- **WebSocketAuthInterceptor**: Handles JWT authentication for WebSocket connections
- **WebSocketConfig**: Configures WebSocket endpoints and CORS settings

#### Services
- **WebSocketUpdateService**: Orchestrates real-time notifications
- **UpdateManagementService**: Core update logic with WebSocket integration
- **DifferentialUpdateService**: Manages delta updates with progress notifications
- **FileManagementService**: Handles file operations with download progress
- **RateLimitingService**: Enforces rate limits for WebSocket connections
- **UpdateCompatibilityService**: Validates compatibility with notifications
- **UpdateStatisticsService**: Provides real-time analytics

#### Data Management
- **ApplicationVersionRepository**: Version metadata and queries
- **ConnectedClientRepository**: Active WebSocket client tracking
- **UpdateAnalyticsRepository**: Real-time analytics data
- **VersionHistoryRepository**: Version history and rollback support

#### Entities
- **ApplicationVersion**: Version metadata with WebSocket integration
- **ConnectedClient**: WebSocket client session management
- **RateLimitTracker**: Rate limiting for WebSocket connections

### Architecture Benefits

**Traditional HTTP vs. WebSocket-Enhanced System:**

| Feature | HTTP-Only | WebSocket-Enhanced |
|---------|-----------|-------------------|
| Update Notifications | Polling required | Real-time push |
| Download Progress | Manual checking | Live updates |
| Installation Status | No feedback | Real-time progress |
| Connection Overhead | High (repeated requests) | Low (persistent connection) |
| User Experience | Delayed notifications | Instant feedback |
| Bandwidth Usage | Higher (polling) | Lower (push-based) |

## WebSocket Connection Setup

### Connection Endpoint

```
WebSocket URL: ws://localhost:8081/ws/updates
Production: wss://your-domain.com/ws/updates
```

### Authentication Methods

The WebSocket connection supports multiple authentication methods:

1. **Query Parameter** (Recommended for web clients)
```javascript
ws://localhost:8081/ws/updates?token=your-jwt-token
```

2. **Authorization Header**
```javascript
// Headers during WebSocket handshake
Authorization: Bearer your-jwt-token
```

3. **Custom Header**
```javascript
// Custom header during handshake
X-Auth-Token: your-jwt-token
```

### Connection Configuration

```javascript
// Basic WebSocket connection
const socket = new WebSocket('ws://localhost:8081/ws/updates?token=' + jwtToken);

// With SockJS fallback (recommended)
const socket = new SockJS('http://localhost:8081/ws/updates?token=' + jwtToken);
```

### Connection Lifecycle

1. **Handshake**: JWT token validation
2. **Welcome Message**: Server sends connection confirmation
3. **Client Registration**: Client sends registration message
4. **Active Session**: Bi-directional communication
5. **Heartbeat**: Periodic ping/pong for connection health
6. **Graceful Disconnect**: Clean session termination

## Real-time Message Formats

### Client-to-Server Messages

#### 1. Client Registration
```json
{
    "type": "REGISTER",
    "data": {
        "clientVersion": "2.0.0",
        "clientId": "desktop-client-123",
        "platform": "windows",
        "architecture": "x64"
    }
}
```

#### 2. Channel Subscription
```json
{
    "type": "SUBSCRIBE",
    "data": {
        "channel": "stable"
    }
}
```

#### 3. Heartbeat/Ping
```json
{
    "type": "PING",
    "data": {
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 4. Channel Unsubscription
```json
{
    "type": "UNSUBSCRIBE",
    "data": {
        "channel": "beta"
    }
}
```

### Server-to-Client Messages

#### 1. Welcome Message
```json
{
    "type": "WELCOME",
    "data": {
        "sessionId": "session-uuid-123",
        "serverTime": "2024-01-15T10:30:00Z",
        "heartbeatInterval": 30000
    }
}
```

#### 2. New Version Available
```json
{
    "type": "NEW_VERSION_AVAILABLE",
    "data": {
        "version": "2.1.0",
        "isMandatory": false,
        "releaseNotes": "Bug fixes and improvements",
        "downloadUrl": "/api/v1/updates/download/2.1.0",
        "fileSize": 52428800,
        "checksum": "sha256-hash",
        "minimumClientVersion": "2.0.0",
        "releaseChannel": "stable",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 3. Download Progress
```json
{
    "type": "DOWNLOAD_PROGRESS",
    "data": {
        "clientId": "desktop-client-123",
        "version": "2.1.0",
        "bytesDownloaded": 26214400,
        "totalBytes": 52428800,
        "progressPercentage": 50,
        "downloadSpeed": 1048576,
        "estimatedTimeRemaining": 25,
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 4. Installation Progress
```json
{
    "type": "INSTALLATION_PROGRESS",
    "data": {
        "clientId": "desktop-client-123",
        "version": "2.1.0",
        "phase": "extracting",
        "progressPercentage": 75,
        "currentOperation": "Extracting application files",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 5. Installation Completed
```json
{
    "type": "INSTALLATION_COMPLETED",
    "data": {
        "clientId": "desktop-client-123",
        "version": "2.1.0",
        "previousVersion": "2.0.0",
        "success": true,
        "message": "Update installed successfully",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 6. Rate Limited
```json
{
    "type": "RATE_LIMITED",
    "data": {
        "clientId": "desktop-client-123",
        "retryAfter": 60,
        "message": "Rate limit exceeded. Please retry after 60 seconds",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 7. Compatibility Issue
```json
{
    "type": "COMPATIBILITY_ISSUE",
    "data": {
        "clientId": "desktop-client-123",
        "version": "2.1.0",
        "issueType": "MINIMUM_VERSION",
        "description": "Client version too old for this update",
        "resolution": "Please update to version 2.0.5 first",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 8. Error Message
```json
{
    "type": "ERROR",
    "data": {
        "code": "INVALID_MESSAGE",
        "message": "Invalid message format",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 9. Heartbeat Response
```json
{
    "type": "HEARTBEAT",
    "data": {
        "timestamp": "2024-01-15T10:30:00Z",
        "connectedClients": 42
    }
}
```

## API Endpoints

### Base URLs

```
Client API: http://localhost:8081/api/v1/updates
Admin API: http://localhost:8081/api/v1/admin/updates
WebSocket: ws://localhost:8081/ws/updates
```

### Client Update Endpoints

#### 1. Check for Updates
```http
GET /api/v1/updates/check?currentVersion=2.0.0&channel=stable
Authorization: Bearer {jwt-token}
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
        "releaseNotes": "Bug fixes and improvements",
        "downloadUrl": "/api/v1/updates/download/2.1.0",
        "fileSize": 52428800,
        "checksum": "sha256-hash",
        "minimumClientVersion": "2.0.0"
    }
}
```

#### 2. Download Update
```http
GET /api/v1/updates/download/2.1.0
Authorization: Bearer {jwt-token}
Range: bytes=0-1023 (optional for resumable downloads)
```

#### 3. Get Version Metadata
```http
GET /api/v1/updates/versions/2.1.0/metadata
Authorization: Bearer {jwt-token}
```

#### 4. Get Differential Update
```http
GET /api/v1/updates/delta/2.0.0/2.1.0
Authorization: Bearer {jwt-token}
```

### Admin Endpoints

#### 1. Create New Version
```http
POST /api/v1/admin/updates/versions
Authorization: Bearer {admin-jwt-token}
Content-Type: multipart/form-data

Form Data:
- file: sales-management-2.1.0.jar
- versionNumber: 2.1.0
- isMandatory: false
- releaseNotes: Bug fixes and improvements
- minimumClientVersion: 2.0.0
- releaseChannel: stable
```

#### 2. Get All Versions
```http
GET /api/v1/admin/updates/versions?page=0&size=10
Authorization: Bearer {admin-jwt-token}
```

#### 3. Update Version Status
```http
PATCH /api/v1/admin/updates/versions/{id}/toggle-status
Authorization: Bearer {admin-jwt-token}
```

#### 4. Get Update Statistics
```http
GET /api/v1/admin/updates/statistics
Authorization: Bearer {admin-jwt-token}
```

## Authentication & Authorization

### JWT Token Requirements

**For HTTP Endpoints:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**For WebSocket Connections:**
```javascript
// Query parameter (recommended)
const socket = new WebSocket('ws://localhost:8081/ws/updates?token=' + jwtToken);

// Or via headers during handshake
const socket = new WebSocket('ws://localhost:8081/ws/updates', [], {
    headers: {
        'Authorization': 'Bearer ' + jwtToken
    }
});
```

### Token Validation

The system validates JWT tokens for:
- **Signature verification**: Using configured secret key
- **Expiration check**: Tokens must not be expired
- **User existence**: Referenced user must exist in system
- **Role verification**: Admin endpoints require ADMIN role

### Session Management

WebSocket sessions store authentication context:
```javascript
// Session attributes stored after successful authentication
{
    "authenticated": true,
    "username": "john.doe",
    "userId": "123",
    "token": "jwt-token",
    "clientIp": "192.168.1.100"
}
```

## Rate Limiting & Security

### WebSocket Rate Limiting

The system implements comprehensive rate limiting for WebSocket connections:

**Rate Limit Configuration:**
```properties
# WebSocket-specific rate limits
app.updates.security.rate-limit=10
app.updates.websocket.heartbeat-interval=30000
app.updates.websocket.connection-timeout=300000
```

**Rate Limit Types:**
- **Connection Rate**: Maximum WebSocket connections per client per minute
- **Message Rate**: Maximum messages per client per minute
- **Download Rate**: Maximum concurrent downloads per client

**Rate Limit Response:**
```json
{
    "type": "RATE_LIMITED",
    "data": {
        "clientId": "desktop-client-123",
        "retryAfter": 60,
        "message": "Rate limit exceeded. Please retry after 60 seconds",
        "violationType": "CONNECTION_LIMIT",
        "currentCount": 15,
        "maxAllowed": 10,
        "windowMinutes": 1,
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

### Security Measures

#### 1. Connection Security
- **JWT Authentication**: Required for all WebSocket connections
- **Origin Validation**: CORS protection for web clients
- **IP Tracking**: Client IP monitoring and logging
- **Session Timeout**: Automatic disconnection after inactivity

#### 2. Message Validation
- **Schema Validation**: All messages validated against JSON schema
- **Type Checking**: Message type validation
- **Size Limits**: Maximum message size enforcement
- **Sanitization**: Input sanitization for security

#### 3. File Security
- **Checksum Verification**: SHA-256 integrity checks
- **Secure Storage**: Protected file storage location
- **Access Control**: Role-based file access
- **Virus Scanning**: Optional antivirus integration

## Integration Guide

### Frontend Integration Steps

#### 1. Establish WebSocket Connection

```javascript
class UpdateManager {
    constructor(jwtToken, baseUrl = 'ws://localhost:8081') {
        this.jwtToken = jwtToken;
        this.baseUrl = baseUrl;
        this.socket = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 1000;
    }

    connect() {
        try {
            // Use SockJS for better browser compatibility
            this.socket = new SockJS(`${this.baseUrl}/ws/updates?token=${this.jwtToken}`);

            this.socket.onopen = this.onOpen.bind(this);
            this.socket.onmessage = this.onMessage.bind(this);
            this.socket.onclose = this.onClose.bind(this);
            this.socket.onerror = this.onError.bind(this);

        } catch (error) {
            console.error('Failed to establish WebSocket connection:', error);
            this.scheduleReconnect();
        }
    }

    onOpen(event) {
        console.log('WebSocket connected');
        this.reconnectAttempts = 0;

        // Register client after connection
        this.registerClient();

        // Subscribe to update channel
        this.subscribeToChannel('stable');
    }

    registerClient() {
        const registrationMessage = {
            type: 'REGISTER',
            data: {
                clientVersion: '2.0.0',
                clientId: this.generateClientId(),
                platform: this.detectPlatform(),
                architecture: this.detectArchitecture()
            }
        };

        this.sendMessage(registrationMessage);
    }

    subscribeToChannel(channel) {
        const subscriptionMessage = {
            type: 'SUBSCRIBE',
            data: {
                channel: channel
            }
        };

        this.sendMessage(subscriptionMessage);
    }

    sendMessage(message) {
        if (this.socket && this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify(message));
        } else {
            console.warn('WebSocket not connected. Message not sent:', message);
        }
    }

    onMessage(event) {
        try {
            const message = JSON.parse(event.data);
            this.handleMessage(message);
        } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
        }
    }

    handleMessage(message) {
        switch (message.type) {
            case 'WELCOME':
                this.handleWelcome(message.data);
                break;
            case 'NEW_VERSION_AVAILABLE':
                this.handleNewVersion(message.data);
                break;
            case 'DOWNLOAD_PROGRESS':
                this.handleDownloadProgress(message.data);
                break;
            case 'INSTALLATION_PROGRESS':
                this.handleInstallationProgress(message.data);
                break;
            case 'INSTALLATION_COMPLETED':
                this.handleInstallationCompleted(message.data);
                break;
            case 'RATE_LIMITED':
                this.handleRateLimit(message.data);
                break;
            case 'COMPATIBILITY_ISSUE':
                this.handleCompatibilityIssue(message.data);
                break;
            case 'ERROR':
                this.handleError(message.data);
                break;
            case 'HEARTBEAT':
                this.handleHeartbeat(message.data);
                break;
            default:
                console.warn('Unknown message type:', message.type);
        }
    }

    handleNewVersion(data) {
        // Show update notification to user
        this.showUpdateNotification({
            version: data.version,
            isMandatory: data.isMandatory,
            releaseNotes: data.releaseNotes,
            fileSize: data.fileSize
        });
    }

    handleDownloadProgress(data) {
        // Update download progress UI
        this.updateDownloadProgress({
            percentage: data.progressPercentage,
            speed: data.downloadSpeed,
            eta: data.estimatedTimeRemaining
        });
    }

    handleInstallationProgress(data) {
        // Update installation progress UI
        this.updateInstallationProgress({
            phase: data.phase,
            percentage: data.progressPercentage,
            operation: data.currentOperation
        });
    }

    onClose(event) {
        console.log('WebSocket disconnected:', event.code, event.reason);

        if (event.code !== 1000) { // Not a normal closure
            this.scheduleReconnect();
        }
    }

    onError(error) {
        console.error('WebSocket error:', error);
    }

    scheduleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

            console.log(`Scheduling reconnect attempt ${this.reconnectAttempts} in ${delay}ms`);

            setTimeout(() => {
                this.connect();
            }, delay);
        } else {
            console.error('Max reconnect attempts reached. Giving up.');
        }
    }

    disconnect() {
        if (this.socket) {
            this.socket.close(1000, 'Client disconnect');
            this.socket = null;
        }
    }

    // Utility methods
    generateClientId() {
        return 'client-' + Math.random().toString(36).substr(2, 9);
    }

    detectPlatform() {
        return navigator.platform || 'unknown';
    }

    detectArchitecture() {
        return navigator.userAgent.includes('x64') ? 'x64' : 'x86';
    }
}
```

#### 2. HTTP API Integration

```javascript
class UpdateAPIClient {
    constructor(jwtToken, baseUrl = 'http://localhost:8081/api/v1/updates') {
        this.jwtToken = jwtToken;
        this.baseUrl = baseUrl;
    }

    async checkForUpdates(currentVersion, channel = 'stable') {
        try {
            const response = await fetch(
                `${this.baseUrl}/check?currentVersion=${currentVersion}&channel=${channel}`,
                {
                    headers: {
                        'Authorization': `Bearer ${this.jwtToken}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Failed to check for updates:', error);
            throw error;
        }
    }

    async downloadUpdate(version, onProgress) {
        try {
            const response = await fetch(`${this.baseUrl}/download/${version}`, {
                headers: {
                    'Authorization': `Bearer ${this.jwtToken}`
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
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
        } catch (error) {
            console.error('Failed to download update:', error);
            throw error;
        }
    }

    async getVersionMetadata(version) {
        try {
            const response = await fetch(`${this.baseUrl}/versions/${version}/metadata`, {
                headers: {
                    'Authorization': `Bearer ${this.jwtToken}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Failed to get version metadata:', error);
            throw error;
        }
    }

    async getDifferentialUpdate(fromVersion, toVersion) {
        try {
            const response = await fetch(`${this.baseUrl}/delta/${fromVersion}/${toVersion}`, {
                headers: {
                    'Authorization': `Bearer ${this.jwtToken}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Failed to get differential update:', error);
            throw error;
        }
    }
}
```

#### 3. Complete Integration Example

```javascript
// Complete update system integration
class CompleteUpdateSystem {
    constructor(jwtToken) {
        this.updateManager = new UpdateManager(jwtToken);
        this.apiClient = new UpdateAPIClient(jwtToken);
        this.currentVersion = '2.0.0';
        this.updateInProgress = false;
    }

    async initialize() {
        // Connect to WebSocket for real-time notifications
        this.updateManager.connect();

        // Set up event handlers
        this.setupEventHandlers();

        // Check for updates on startup
        await this.checkForUpdates();
    }

    setupEventHandlers() {
        // Override WebSocket message handlers
        this.updateManager.handleNewVersion = (data) => {
            this.showUpdateDialog(data);
        };

        this.updateManager.handleDownloadProgress = (data) => {
            this.updateDownloadUI(data);
        };

        this.updateManager.handleInstallationProgress = (data) => {
            this.updateInstallationUI(data);
        };

        this.updateManager.handleInstallationCompleted = (data) => {
            this.handleUpdateComplete(data);
        };
    }

    async checkForUpdates() {
        try {
            const result = await this.apiClient.checkForUpdates(this.currentVersion);

            if (result.data.updateAvailable) {
                this.showUpdateDialog(result.data);
            }
        } catch (error) {
            console.error('Update check failed:', error);
        }
    }

    async startUpdate(version) {
        if (this.updateInProgress) {
            console.warn('Update already in progress');
            return;
        }

        this.updateInProgress = true;

        try {
            // Show download progress
            this.showDownloadProgress();

            // Download update with progress tracking
            const updateBlob = await this.apiClient.downloadUpdate(version, (progress) => {
                this.updateDownloadUI(progress);
            });

            // Verify checksum
            await this.verifyUpdate(updateBlob, version);

            // Install update
            await this.installUpdate(updateBlob, version);

        } catch (error) {
            console.error('Update failed:', error);
            this.showUpdateError(error.message);
        } finally {
            this.updateInProgress = false;
        }
    }

    showUpdateDialog(updateData) {
        // Implementation depends on your UI framework
        console.log('New update available:', updateData);
    }

    updateDownloadUI(progress) {
        // Update download progress bar
        console.log('Download progress:', progress);
    }

    updateInstallationUI(progress) {
        // Update installation progress
        console.log('Installation progress:', progress);
    }

    handleUpdateComplete(data) {
        if (data.success) {
            this.showUpdateSuccess(data);
            this.currentVersion = data.version;
        } else {
            this.showUpdateError(data.message);
        }
    }
}

// Usage
const updateSystem = new CompleteUpdateSystem(jwtToken);
updateSystem.initialize();
```

## Error Handling

### Common Error Scenarios

#### 1. WebSocket Connection Errors

```javascript
// Connection timeout
{
    "type": "ERROR",
    "data": {
        "code": "CONNECTION_TIMEOUT",
        "message": "WebSocket connection timed out",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}

// Authentication failure
{
    "type": "ERROR",
    "data": {
        "code": "AUTHENTICATION_FAILED",
        "message": "Invalid or expired JWT token",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}

// Rate limit exceeded
{
    "type": "RATE_LIMITED",
    "data": {
        "retryAfter": 60,
        "message": "Too many connection attempts",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

#### 2. HTTP API Errors

```javascript
// HTTP error handling
class ErrorHandler {
    static handleAPIError(error, response) {
        switch (response?.status) {
            case 401:
                return {
                    code: 'UNAUTHORIZED',
                    message: 'Authentication required or token expired',
                    action: 'Please login again'
                };
            case 403:
                return {
                    code: 'FORBIDDEN',
                    message: 'Insufficient permissions',
                    action: 'Contact administrator'
                };
            case 404:
                return {
                    code: 'NOT_FOUND',
                    message: 'Requested version not found',
                    action: 'Check version number'
                };
            case 429:
                return {
                    code: 'RATE_LIMITED',
                    message: 'Too many requests',
                    action: 'Please wait and try again'
                };
            case 500:
                return {
                    code: 'SERVER_ERROR',
                    message: 'Internal server error',
                    action: 'Try again later or contact support'
                };
            default:
                return {
                    code: 'UNKNOWN_ERROR',
                    message: error.message || 'Unknown error occurred',
                    action: 'Check network connection'
                };
        }
    }
}
```

#### 3. Retry Logic

```javascript
class RetryHandler {
    static async withRetry(operation, maxRetries = 3, delay = 1000) {
        let lastError;

        for (let attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return await operation();
            } catch (error) {
                lastError = error;

                if (attempt === maxRetries) {
                    throw lastError;
                }

                // Exponential backoff
                const waitTime = delay * Math.pow(2, attempt - 1);
                console.log(`Attempt ${attempt} failed, retrying in ${waitTime}ms...`);

                await new Promise(resolve => setTimeout(resolve, waitTime));
            }
        }
    }
}

// Usage example
const updateData = await RetryHandler.withRetry(
    () => apiClient.checkForUpdates('2.0.0'),
    3,
    1000
);
```

## Usage Examples

### Example 1: Basic Update Check

```javascript
// Simple update check and notification
async function checkAndNotifyUpdates() {
    const apiClient = new UpdateAPIClient(jwtToken);

    try {
        const result = await apiClient.checkForUpdates('2.0.0');

        if (result.data.updateAvailable) {
            showNotification({
                title: 'Update Available',
                message: `Version ${result.data.latestVersion} is available`,
                type: result.data.isMandatory ? 'warning' : 'info'
            });
        }
    } catch (error) {
        console.error('Update check failed:', error);
    }
}
```

### Example 2: Real-time Update with Progress

```javascript
// Complete update flow with WebSocket notifications
class UpdateFlow {
    constructor(jwtToken) {
        this.updateManager = new UpdateManager(jwtToken);
        this.apiClient = new UpdateAPIClient(jwtToken);
    }

    async startUpdateFlow() {
        // Connect to WebSocket
        this.updateManager.connect();

        // Set up progress handlers
        this.updateManager.handleDownloadProgress = (data) => {
            this.updateProgressBar('download', data.progressPercentage);
        };

        this.updateManager.handleInstallationProgress = (data) => {
            this.updateProgressBar('installation', data.progressPercentage);
            this.updateStatusText(data.currentOperation);
        };

        // Check for updates
        const updateCheck = await this.apiClient.checkForUpdates('2.0.0');

        if (updateCheck.data.updateAvailable) {
            const userConfirmed = await this.showUpdateConfirmation(updateCheck.data);

            if (userConfirmed) {
                await this.downloadAndInstall(updateCheck.data.latestVersion);
            }
        }
    }

    updateProgressBar(type, percentage) {
        const progressBar = document.getElementById(`${type}-progress`);
        if (progressBar) {
            progressBar.style.width = `${percentage}%`;
            progressBar.textContent = `${percentage}%`;
        }
    }

    updateStatusText(text) {
        const statusElement = document.getElementById('update-status');
        if (statusElement) {
            statusElement.textContent = text;
        }
    }
}
```

### Example 3: Kotlin/Compose Desktop Integration

```kotlin
// Kotlin Compose Desktop WebSocket client
class UpdateNotificationService {
    private var webSocket: WebSocket? = null
    private val client = HttpClient {
        install(WebSockets)
    }

    suspend fun connect(token: String) {
        try {
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
                            "clientId": "desktop-client-${UUID.randomUUID()}",
                            "platform": "desktop",
                            "architecture": "x64"
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
        } catch (e: Exception) {
            println("WebSocket connection failed: ${e.message}")
        }
    }

    private fun handleMessage(message: WebSocketMessage) {
        when (message.type) {
            "NEW_VERSION_AVAILABLE" -> {
                // Show update notification in Compose UI
                showUpdateNotification(message.data)
            }
            "DOWNLOAD_PROGRESS" -> {
                // Update download progress
                updateDownloadProgress(message.data)
            }
            "INSTALLATION_PROGRESS" -> {
                // Update installation progress
                updateInstallationProgress(message.data)
            }
        }
    }
}
```

## Testing & Troubleshooting

### Testing WebSocket Connection

#### 1. Connection Test

```javascript
// Test WebSocket connection
function testWebSocketConnection(jwtToken) {
    const socket = new WebSocket(`ws://localhost:8081/ws/updates?token=${jwtToken}`);

    socket.onopen = () => {
        console.log('✅ WebSocket connection successful');

        // Test registration
        socket.send(JSON.stringify({
            type: 'REGISTER',
            data: {
                clientVersion: '2.0.0',
                clientId: 'test-client',
                platform: 'test',
                architecture: 'x64'
            }
        }));
    };

    socket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log('📨 Received message:', message);
    };

    socket.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
    };

    socket.onclose = (event) => {
        console.log('🔌 WebSocket closed:', event.code, event.reason);
    };

    // Clean up after 10 seconds
    setTimeout(() => {
        socket.close();
    }, 10000);
}
```

#### 2. API Endpoint Testing

```javascript
// Test HTTP API endpoints
async function testAPIEndpoints(jwtToken) {
    const apiClient = new UpdateAPIClient(jwtToken);

    try {
        // Test update check
        console.log('Testing update check...');
        const updateCheck = await apiClient.checkForUpdates('1.0.0');
        console.log('✅ Update check successful:', updateCheck);

        // Test version metadata
        console.log('Testing version metadata...');
        const metadata = await apiClient.getVersionMetadata('2.0.0');
        console.log('✅ Version metadata successful:', metadata);

    } catch (error) {
        console.error('❌ API test failed:', error);
    }
}
```

### Common Issues and Solutions

#### 1. WebSocket Connection Issues

**Problem**: WebSocket connection fails with 401 Unauthorized
```
Solution: Check JWT token validity and format
- Ensure token is not expired
- Verify token format: ws://localhost:8081/ws/updates?token=YOUR_JWT_TOKEN
- Check token permissions
```

**Problem**: WebSocket connection drops frequently
```
Solution: Implement proper reconnection logic
- Use exponential backoff for reconnection attempts
- Handle network connectivity changes
- Implement heartbeat/ping mechanism
```

#### 2. Rate Limiting Issues

**Problem**: Receiving RATE_LIMITED messages
```
Solution: Implement proper rate limiting handling
- Respect retryAfter values in rate limit responses
- Implement exponential backoff
- Reduce connection frequency
```

#### 3. Message Handling Issues

**Problem**: Messages not being processed correctly
```
Solution: Verify message format and handling
- Check JSON parsing for incoming messages
- Validate message types and data structure
- Implement proper error handling for malformed messages
```

### Configuration Troubleshooting

#### Server Configuration

```properties
# Verify these settings in application.properties
app.updates.websocket.heartbeat-interval=30000
app.updates.websocket.connection-timeout=300000
app.updates.security.rate-limit=10

# CORS settings
cors.allowed-origins=http://localhost:3000,https://your-domain.com

# WebSocket endpoint
# Should be accessible at: ws://localhost:8081/ws/updates
```

#### Client Configuration

```javascript
// Verify client configuration
const config = {
    websocketUrl: 'ws://localhost:8081/ws/updates',
    apiBaseUrl: 'http://localhost:8081/api/v1/updates',
    reconnectAttempts: 5,
    reconnectDelay: 1000,
    heartbeatInterval: 30000
};
```

### Debugging Tools

#### 1. Browser Developer Tools

```javascript
// Enable WebSocket debugging in browser console
localStorage.setItem('debug', 'websocket:*');

// Monitor WebSocket frames in Network tab
// Check for proper handshake and message flow
```

#### 2. Server-side Logging

```properties
# Enable debug logging for WebSocket
logging.level.com.hamza.salesmanagementbackend.websocket=DEBUG
logging.level.com.hamza.salesmanagementbackend.service.WebSocketUpdateService=DEBUG
```

#### 3. Connection Health Check

```javascript
// Implement connection health monitoring
class ConnectionHealthMonitor {
    constructor(updateManager) {
        this.updateManager = updateManager;
        this.lastHeartbeat = Date.now();
        this.healthCheckInterval = 60000; // 1 minute

        this.startHealthCheck();
    }

    startHealthCheck() {
        setInterval(() => {
            const timeSinceLastHeartbeat = Date.now() - this.lastHeartbeat;

            if (timeSinceLastHeartbeat > this.healthCheckInterval * 2) {
                console.warn('Connection appears unhealthy, attempting reconnect');
                this.updateManager.disconnect();
                this.updateManager.connect();
            }
        }, this.healthCheckInterval);
    }

    recordHeartbeat() {
        this.lastHeartbeat = Date.now();
    }
}
```

## Conclusion

The WebSocket-based update system provides a robust, real-time solution for application updates with the following key benefits:

- **Real-time Notifications**: Instant update availability notifications
- **Progress Tracking**: Live download and installation progress
- **Improved User Experience**: No polling required, immediate feedback
- **Efficient Communication**: Persistent connections reduce overhead
- **Comprehensive Security**: JWT authentication and rate limiting
- **Fallback Support**: SockJS compatibility for older browsers

For additional support or questions, refer to the existing HTTP-based documentation or contact the development team.

---

**Related Documentation:**
- [Enhanced Update System API](Enhanced-Update-System-API.md)
- [Enhanced Update System README](Enhanced-Update-System-README.md)
- [Frontend Update System Documentation](../Frontend_Update_System_Documentation.md)
- [File Storage System](../update-system/File-Storage-System.md)
