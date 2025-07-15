# HTTP-Based Update System Frontend Integration Guide

## Overview

The HTTP-based update system provides a simple and reliable way for clients to check for new application versions and download updates through REST API endpoints. This system follows a traditional client-initiated approach where the client application periodically checks for updates and downloads them when available.

### Key Features
- **Manual Update Checking**: Clients initiate update checks via HTTP requests
- **Version Comparison**: Server compares client version with latest available version
- **Secure Downloads**: JWT-authenticated download endpoints with integrity verification
- **Download Tracking**: Server tracks download statistics and completion status
- **Flexible Scheduling**: Clients can check for updates on their own schedule

### System Architecture
The update system consists of two main HTTP endpoints:
1. **Update Check Endpoint**: `/api/v1/updates/check?currentVersion={version}`
2. **Download Endpoint**: `/api/v1/updates/download/{version}`

## API Endpoints

### 1. Check for Updates

**Endpoint:** `GET /api/v1/updates/check`

**Parameters:**
- `currentVersion` (required): The client's current application version

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Example:**
```http
GET /api/v1/updates/check?currentVersion=2.0.0
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Format:**
```json
{
  "success": true,
  "message": "Update check completed successfully",
  "data": {
    "updateAvailable": true,
    "latestVersion": "2.1.0",
    "currentVersion": "2.0.0",
    "isMandatory": false,
    "releaseNotes": "Bug fixes and performance improvements",
    "downloadUrl": "/api/v1/updates/download/2.1.0",
    "fileSize": 52428800,
    "formattedFileSize": "50.0 MB",
    "checksum": "sha256:abc123def456...",
    "minimumClientVersion": "2.0.0"
  }
}
```

**Response when no update available:**
```json
{
  "success": true,
  "message": "Update check completed successfully",
  "data": {
    "updateAvailable": false,
    "latestVersion": "2.0.0",
    "currentVersion": "2.0.0",
    "isMandatory": false,
    "releaseNotes": null,
    "downloadUrl": null,
    "fileSize": null,
    "formattedFileSize": null,
    "checksum": null,
    "minimumClientVersion": null
  }
}
```

### 2. Download Update

**Endpoint:** `GET /api/v1/updates/download/{version}`

**Parameters:**
- `version` (path parameter): The version number to download

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
X-Client-ID: unique-client-identifier (optional)
```

**Request Example:**
```http
GET /api/v1/updates/download/2.1.0
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Client-ID: client-12345
```

**Response:**
- **Content-Type:** `application/octet-stream`
- **Content-Disposition:** `attachment; filename="sales-management-2.1.0.exe"`
- **Content-Length:** File size in bytes
- **X-Checksum:** SHA256 checksum for integrity verification
- **X-Version:** Version number being downloaded

### 3. Get Latest Version

**Endpoint:** `GET /api/v1/updates/latest`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "success": true,
  "message": "Latest version retrieved successfully",
  "data": {
    "versionNumber": "2.1.0",
    "releaseDate": "2025-01-15T10:00:00Z",
    "isMandatory": false,
    "isActive": true,
    "releaseNotes": "Latest features and improvements",
    "minimumClientVersion": "2.0.0",
    "fileName": "sales-management-2.1.0.exe",
    "fileSize": 52428800,
    "fileChecksum": "sha256:abc123def456...",
    "downloadUrl": "/api/v1/updates/download/2.1.0"
  }
}
```

### 4. Get Version Information

**Endpoint:** `GET /api/v1/updates/version/{version}`

**Response:** Same format as latest version endpoint but for specific version.

### 5. Health Check

**Endpoint:** `GET /api/v1/updates/health`

**Response:**
```json
{
  "success": true,
  "message": "Health check successful",
  "data": "Update service is running"
}
```

## Frontend Implementation

### JavaScript/TypeScript Implementation

```javascript
class UpdateManager {
    constructor(baseUrl, authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
        this.currentVersion = this.getCurrentVersion();
        this.checkInterval = null;
    }

    /**
     * Check for updates
     */
    async checkForUpdates() {
        try {
            const response = await fetch(
                `${this.baseUrl}/api/v1/updates/check?currentVersion=${this.currentVersion}`,
                {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${this.authToken}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            
            if (result.success && result.data.updateAvailable) {
                this.handleUpdateAvailable(result.data);
            } else {
                console.log('No updates available');
            }

            return result.data;

        } catch (error) {
            console.error('Failed to check for updates:', error);
            throw error;
        }
    }

    /**
     * Download update
     */
    async downloadUpdate(version) {
        try {
            const response = await fetch(
                `${this.baseUrl}/api/v1/updates/download/${version}`,
                {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${this.authToken}`,
                        'X-Client-ID': this.getClientId()
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`Download failed! status: ${response.status}`);
            }

            // Get file information from headers
            const contentDisposition = response.headers.get('Content-Disposition');
            const filename = this.extractFilename(contentDisposition);
            const checksum = response.headers.get('X-Checksum');
            const fileSize = response.headers.get('Content-Length');

            // Get file blob
            const blob = await response.blob();

            // Verify checksum if provided
            if (checksum) {
                const isValid = await this.verifyChecksum(blob, checksum);
                if (!isValid) {
                    throw new Error('File integrity check failed');
                }
            }

            // Trigger download
            this.triggerFileDownload(blob, filename);

            console.log(`Successfully downloaded ${filename}`);
            return {
                filename,
                size: fileSize,
                checksum,
                verified: !!checksum
            };

        } catch (error) {
            console.error('Failed to download update:', error);
            throw error;
        }
    }

    /**
     * Start automatic update checking
     */
    startPeriodicCheck(intervalMinutes = 60) {
        this.stopPeriodicCheck(); // Clear any existing interval
        
        this.checkInterval = setInterval(() => {
            this.checkForUpdates().catch(error => {
                console.error('Periodic update check failed:', error);
            });
        }, intervalMinutes * 60 * 1000);

        console.log(`Started periodic update checking every ${intervalMinutes} minutes`);
    }

    /**
     * Stop automatic update checking
     */
    stopPeriodicCheck() {
        if (this.checkInterval) {
            clearInterval(this.checkInterval);
            this.checkInterval = null;
            console.log('Stopped periodic update checking');
        }
    }

    /**
     * Handle when update is available
     */
    handleUpdateAvailable(updateInfo) {
        console.log('Update available:', updateInfo);
        
        // Show notification to user
        this.showUpdateNotification(updateInfo);
        
        // Trigger custom event
        window.dispatchEvent(new CustomEvent('updateAvailable', {
            detail: updateInfo
        }));
    }

    /**
     * Show update notification to user
     */
    showUpdateNotification(updateInfo) {
        // Browser notification
        if (Notification.permission === 'granted') {
            new Notification('Update Available', {
                body: `Version ${updateInfo.latestVersion} is available (${updateInfo.formattedFileSize})`,
                icon: '/favicon.ico',
                tag: 'update-notification'
            });
        }

        // Custom UI notification
        this.createUpdateModal(updateInfo);
    }

    /**
     * Create update modal
     */
    createUpdateModal(updateInfo) {
        // Remove existing modal if present
        const existingModal = document.getElementById('update-modal');
        if (existingModal) {
            existingModal.remove();
        }

        // Create modal HTML
        const modal = document.createElement('div');
        modal.id = 'update-modal';
        modal.className = 'update-modal-overlay';
        modal.innerHTML = `
            <div class="update-modal">
                <div class="update-header">
                    <h3>Update Available</h3>
                    <span class="update-badge ${updateInfo.isMandatory ? 'mandatory' : 'optional'}">
                        ${updateInfo.isMandatory ? 'Required' : 'Optional'}
                    </span>
                </div>
                <div class="update-content">
                    <p><strong>Current Version:</strong> ${updateInfo.currentVersion}</p>
                    <p><strong>Latest Version:</strong> ${updateInfo.latestVersion}</p>
                    <p><strong>Size:</strong> ${updateInfo.formattedFileSize}</p>
                    ${updateInfo.releaseNotes ? `
                        <div class="release-notes">
                            <h4>Release Notes:</h4>
                            <p>${updateInfo.releaseNotes}</p>
                        </div>
                    ` : ''}
                </div>
                <div class="update-actions">
                    <button class="btn btn-primary" onclick="updateManager.downloadAndInstall('${updateInfo.latestVersion}')">
                        Download Update
                    </button>
                    ${!updateInfo.isMandatory ? `
                        <button class="btn btn-secondary" onclick="updateManager.dismissUpdate()">
                            Later
                        </button>
                    ` : ''}
                </div>
            </div>
        `;

        document.body.appendChild(modal);
    }

    /**
     * Download and install update
     */
    async downloadAndInstall(version) {
        try {
            // Show progress
            this.showDownloadProgress();
            
            // Download the update
            const downloadResult = await this.downloadUpdate(version);
            
            // Hide progress
            this.hideDownloadProgress();
            
            // Show success message
            this.showDownloadSuccess(downloadResult);
            
        } catch (error) {
            this.hideDownloadProgress();
            this.showDownloadError(error.message);
        }
    }

    /**
     * Dismiss update notification
     */
    dismissUpdate() {
        const modal = document.getElementById('update-modal');
        if (modal) {
            modal.remove();
        }
    }

    // Helper methods
    getCurrentVersion() {
        return window.APP_VERSION || '1.0.0';
    }

    getClientId() {
        let clientId = localStorage.getItem('clientId');
        if (!clientId) {
            clientId = 'client_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
            localStorage.setItem('clientId', clientId);
        }
        return clientId;
    }

    extractFilename(contentDisposition) {
        if (!contentDisposition) return 'update.exe';
        
        const filenameMatch = contentDisposition.match(/filename="(.+)"/);
        return filenameMatch ? filenameMatch[1] : 'update.exe';
    }

    async verifyChecksum(blob, expectedChecksum) {
        try {
            const arrayBuffer = await blob.arrayBuffer();
            const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
            const hashArray = Array.from(new Uint8Array(hashBuffer));
            const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
            const actualChecksum = 'sha256:' + hashHex;
            
            return actualChecksum === expectedChecksum;
        } catch (error) {
            console.error('Checksum verification failed:', error);
            return false;
        }
    }

    triggerFileDownload(blob, filename) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }

    showDownloadProgress() {
        // Implement progress UI
        console.log('Download started...');
    }

    hideDownloadProgress() {
        // Hide progress UI
        console.log('Download completed');
    }

    showDownloadSuccess(result) {
        alert(`Update downloaded successfully: ${result.filename}`);
    }

    showDownloadError(message) {
        alert(`Download failed: ${message}`);
    }
}

// Usage
const updateManager = new UpdateManager(
    'http://localhost:8081', // Base URL
    localStorage.getItem('authToken') // JWT token
);

// Check for updates immediately
updateManager.checkForUpdates();

// Start periodic checking every hour
updateManager.startPeriodicCheck(60);

// Listen for update events
window.addEventListener('updateAvailable', (event) => {
    console.log('Update event received:', event.detail);
});
```

### React Component Implementation

```jsx
import React, { useState, useEffect, useCallback } from 'react';

const UpdateManager = ({ authToken, baseUrl = 'http://localhost:8081' }) => {
    const [updateInfo, setUpdateInfo] = useState(null);
    const [isChecking, setIsChecking] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);
    const [lastCheckTime, setLastCheckTime] = useState(null);

    const getCurrentVersion = () => {
        return process.env.REACT_APP_VERSION || '1.0.0';
    };

    const checkForUpdates = useCallback(async () => {
        if (isChecking) return;

        setIsChecking(true);
        try {
            const response = await fetch(
                `${baseUrl}/api/v1/updates/check?currentVersion=${getCurrentVersion()}`,
                {
                    headers: {
                        'Authorization': `Bearer ${authToken}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            const result = await response.json();

            if (result.success && result.data.updateAvailable) {
                setUpdateInfo(result.data);

                // Show browser notification
                if (Notification.permission === 'granted') {
                    new Notification('Update Available', {
                        body: `Version ${result.data.latestVersion} is available`,
                        icon: '/favicon.ico'
                    });
                }
            } else {
                setUpdateInfo(null);
            }

            setLastCheckTime(new Date());
        } catch (error) {
            console.error('Update check failed:', error);
        } finally {
            setIsChecking(false);
        }
    }, [authToken, baseUrl, isChecking]);

    const downloadUpdate = async (version) => {
        setIsDownloading(true);
        try {
            const response = await fetch(
                `${baseUrl}/api/v1/updates/download/${version}`,
                {
                    headers: {
                        'Authorization': `Bearer ${authToken}`,
                        'X-Client-ID': localStorage.getItem('clientId') || 'web-client'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`Download failed: ${response.status}`);
            }

            const blob = await response.blob();
            const filename = response.headers.get('Content-Disposition')
                ?.match(/filename="(.+)"/)?.[1] || `update-${version}.exe`;

            // Trigger download
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);

            setUpdateInfo(null); // Hide update notification after download
        } catch (error) {
            console.error('Download failed:', error);
            alert(`Download failed: ${error.message}`);
        } finally {
            setIsDownloading(false);
        }
    };

    // Periodic update checking
    useEffect(() => {
        // Check immediately
        checkForUpdates();

        // Set up periodic checking (every hour)
        const interval = setInterval(checkForUpdates, 60 * 60 * 1000);

        return () => clearInterval(interval);
    }, [checkForUpdates]);

    // Request notification permission
    useEffect(() => {
        if (Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }, []);

    return (
        <div className="update-manager">
            {/* Update Status Indicator */}
            <div className="update-status">
                <span className={`status-indicator ${isChecking ? 'checking' : 'idle'}`}>
                    {isChecking ? '🔄' : '✅'}
                </span>
                <span className="status-text">
                    {isChecking ? 'Checking for updates...' :
                     lastCheckTime ? `Last checked: ${lastCheckTime.toLocaleTimeString()}` :
                     'Ready'}
                </span>
                <button
                    onClick={checkForUpdates}
                    disabled={isChecking}
                    className="btn btn-sm btn-outline"
                >
                    Check Now
                </button>
            </div>

            {/* Update Notification Modal */}
            {updateInfo && (
                <div className="update-modal-overlay">
                    <div className="update-modal">
                        <div className="update-header">
                            <h3>Update Available</h3>
                            <span className={`update-badge ${updateInfo.isMandatory ? 'mandatory' : 'optional'}`}>
                                {updateInfo.isMandatory ? 'Required' : 'Optional'}
                            </span>
                        </div>

                        <div className="update-content">
                            <div className="version-info">
                                <p><strong>Current:</strong> {updateInfo.currentVersion}</p>
                                <p><strong>Latest:</strong> {updateInfo.latestVersion}</p>
                                <p><strong>Size:</strong> {updateInfo.formattedFileSize}</p>
                            </div>

                            {updateInfo.releaseNotes && (
                                <div className="release-notes">
                                    <h4>What's New:</h4>
                                    <p>{updateInfo.releaseNotes}</p>
                                </div>
                            )}
                        </div>

                        <div className="update-actions">
                            <button
                                onClick={() => downloadUpdate(updateInfo.latestVersion)}
                                disabled={isDownloading}
                                className="btn btn-primary"
                            >
                                {isDownloading ? 'Downloading...' : 'Download Update'}
                            </button>
                            {!updateInfo.isMandatory && (
                                <button
                                    onClick={() => setUpdateInfo(null)}
                                    className="btn btn-secondary"
                                >
                                    Later
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UpdateManager;
```

### Vue.js Component Implementation

```vue
<template>
  <div class="update-manager">
    <!-- Update Status -->
    <div class="update-status">
      <span :class="['status-indicator', { checking: isChecking }]">
        {{ isChecking ? '🔄' : '✅' }}
      </span>
      <span class="status-text">
        {{ statusText }}
      </span>
      <button
        @click="checkForUpdates"
        :disabled="isChecking"
        class="btn btn-sm btn-outline"
      >
        Check Now
      </button>
    </div>

    <!-- Update Modal -->
    <div v-if="updateInfo" class="update-modal-overlay">
      <div class="update-modal">
        <div class="update-header">
          <h3>Update Available</h3>
          <span :class="['update-badge', updateInfo.isMandatory ? 'mandatory' : 'optional']">
            {{ updateInfo.isMandatory ? 'Required' : 'Optional' }}
          </span>
        </div>

        <div class="update-content">
          <div class="version-info">
            <p><strong>Current:</strong> {{ updateInfo.currentVersion }}</p>
            <p><strong>Latest:</strong> {{ updateInfo.latestVersion }}</p>
            <p><strong>Size:</strong> {{ updateInfo.formattedFileSize }}</p>
          </div>

          <div v-if="updateInfo.releaseNotes" class="release-notes">
            <h4>What's New:</h4>
            <p>{{ updateInfo.releaseNotes }}</p>
          </div>
        </div>

        <div class="update-actions">
          <button
            @click="downloadUpdate(updateInfo.latestVersion)"
            :disabled="isDownloading"
            class="btn btn-primary"
          >
            {{ isDownloading ? 'Downloading...' : 'Download Update' }}
          </button>
          <button
            v-if="!updateInfo.isMandatory"
            @click="updateInfo = null"
            class="btn btn-secondary"
          >
            Later
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UpdateManager',
  props: {
    authToken: {
      type: String,
      required: true
    },
    baseUrl: {
      type: String,
      default: 'http://localhost:8081'
    }
  },
  data() {
    return {
      updateInfo: null,
      isChecking: false,
      isDownloading: false,
      lastCheckTime: null,
      checkInterval: null
    };
  },
  computed: {
    statusText() {
      if (this.isChecking) return 'Checking for updates...';
      if (this.lastCheckTime) return `Last checked: ${this.lastCheckTime.toLocaleTimeString()}`;
      return 'Ready';
    }
  },
  async mounted() {
    await this.checkForUpdates();
    this.startPeriodicCheck();
    this.requestNotificationPermission();
  },
  beforeUnmount() {
    this.stopPeriodicCheck();
  },
  methods: {
    async checkForUpdates() {
      if (this.isChecking) return;

      this.isChecking = true;
      try {
        const response = await fetch(
          `${this.baseUrl}/api/v1/updates/check?currentVersion=${this.getCurrentVersion()}`,
          {
            headers: {
              'Authorization': `Bearer ${this.authToken}`,
              'Content-Type': 'application/json'
            }
          }
        );

        const result = await response.json();

        if (result.success && result.data.updateAvailable) {
          this.updateInfo = result.data;
          this.showNotification(result.data);
        } else {
          this.updateInfo = null;
        }

        this.lastCheckTime = new Date();
      } catch (error) {
        console.error('Update check failed:', error);
      } finally {
        this.isChecking = false;
      }
    },

    async downloadUpdate(version) {
      this.isDownloading = true;
      try {
        const response = await fetch(
          `${this.baseUrl}/api/v1/updates/download/${version}`,
          {
            headers: {
              'Authorization': `Bearer ${this.authToken}`,
              'X-Client-ID': this.getClientId()
            }
          }
        );

        if (!response.ok) {
          throw new Error(`Download failed: ${response.status}`);
        }

        const blob = await response.blob();
        const filename = this.extractFilename(response.headers.get('Content-Disposition')) || `update-${version}.exe`;

        this.triggerDownload(blob, filename);
        this.updateInfo = null;
      } catch (error) {
        console.error('Download failed:', error);
        alert(`Download failed: ${error.message}`);
      } finally {
        this.isDownloading = false;
      }
    },

    startPeriodicCheck() {
      this.checkInterval = setInterval(() => {
        this.checkForUpdates();
      }, 60 * 60 * 1000); // Every hour
    },

    stopPeriodicCheck() {
      if (this.checkInterval) {
        clearInterval(this.checkInterval);
        this.checkInterval = null;
      }
    },

    getCurrentVersion() {
      return process.env.VUE_APP_VERSION || '1.0.0';
    },

    getClientId() {
      let clientId = localStorage.getItem('clientId');
      if (!clientId) {
        clientId = 'client_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        localStorage.setItem('clientId', clientId);
      }
      return clientId;
    },

    extractFilename(contentDisposition) {
      if (!contentDisposition) return null;
      const match = contentDisposition.match(/filename="(.+)"/);
      return match ? match[1] : null;
    },

    triggerDownload(blob, filename) {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    },

    showNotification(updateData) {
      if (Notification.permission === 'granted') {
        new Notification('Update Available', {
          body: `Version ${updateData.latestVersion} is available`,
          icon: '/favicon.ico'
        });
      }
    },

    requestNotificationPermission() {
      if (Notification.permission === 'default') {
        Notification.requestPermission();
      }
    }
  }
};
</script>
```

## CSS Styles

```css
/* Update Manager Styles */
.update-manager {
    position: fixed;
    top: 20px;
    right: 20px;
    z-index: 10000;
}

.update-status {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: #f8f9fa;
    border: 1px solid #dee2e6;
    border-radius: 6px;
    font-size: 14px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.status-indicator {
    font-size: 16px;
    transition: transform 0.3s ease;
}

.status-indicator.checking {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.update-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10001;
}

.update-modal {
    background: white;
    border-radius: 12px;
    padding: 24px;
    max-width: 500px;
    width: 90%;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
    animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.update-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
}

.update-header h3 {
    margin: 0;
    color: #333;
    font-size: 20px;
}

.update-badge {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
}

.update-badge.mandatory {
    background: #dc3545;
    color: white;
}

.update-badge.optional {
    background: #28a745;
    color: white;
}

.update-content {
    margin-bottom: 24px;
}

.version-info p {
    margin: 8px 0;
    color: #666;
}

.release-notes {
    margin-top: 16px;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #007bff;
}

.release-notes h4 {
    margin: 0 0 8px 0;
    color: #333;
    font-size: 14px;
}

.release-notes p {
    margin: 0;
    color: #666;
    line-height: 1.5;
}

.update-actions {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
}

.btn {
    padding: 10px 20px;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s ease;
    text-decoration: none;
    display: inline-block;
    text-align: center;
}

.btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.btn-primary {
    background: #007bff;
    color: white;
}

.btn-primary:hover:not(:disabled) {
    background: #0056b3;
}

.btn-secondary {
    background: #6c757d;
    color: white;
}

.btn-secondary:hover:not(:disabled) {
    background: #545b62;
}

.btn-outline {
    background: transparent;
    color: #007bff;
    border: 1px solid #007bff;
}

.btn-outline:hover:not(:disabled) {
    background: #007bff;
    color: white;
}

.btn-sm {
    padding: 6px 12px;
    font-size: 12px;
}

/* Responsive Design */
@media (max-width: 768px) {
    .update-manager {
        top: 10px;
        right: 10px;
        left: 10px;
    }

    .update-modal {
        margin: 20px;
        width: auto;
    }

    .update-actions {
        flex-direction: column;
    }

    .btn {
        width: 100%;
    }
}
```

## Error Handling

### Common Error Scenarios

```javascript
class UpdateErrorHandler {
    static handleUpdateCheckError(error) {
        console.error('Update check failed:', error);

        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            return {
                type: 'NETWORK_ERROR',
                message: 'Unable to connect to update server. Please check your internet connection.',
                retry: true
            };
        }

        if (error.message.includes('401')) {
            return {
                type: 'AUTH_ERROR',
                message: 'Authentication failed. Please log in again.',
                retry: false,
                requiresReauth: true
            };
        }

        if (error.message.includes('403')) {
            return {
                type: 'PERMISSION_ERROR',
                message: 'You do not have permission to check for updates.',
                retry: false
            };
        }

        if (error.message.includes('500')) {
            return {
                type: 'SERVER_ERROR',
                message: 'Server error occurred. Please try again later.',
                retry: true
            };
        }

        return {
            type: 'UNKNOWN_ERROR',
            message: 'An unexpected error occurred while checking for updates.',
            retry: true
        };
    }

    static handleDownloadError(error) {
        console.error('Download failed:', error);

        if (error.message.includes('404')) {
            return {
                type: 'FILE_NOT_FOUND',
                message: 'The requested update file was not found.',
                retry: false
            };
        }

        if (error.message.includes('insufficient storage')) {
            return {
                type: 'STORAGE_ERROR',
                message: 'Insufficient storage space to download the update.',
                retry: false
            };
        }

        if (error.message.includes('integrity check failed')) {
            return {
                type: 'INTEGRITY_ERROR',
                message: 'Downloaded file is corrupted. Please try downloading again.',
                retry: true
            };
        }

        return {
            type: 'DOWNLOAD_ERROR',
            message: 'Failed to download the update. Please try again.',
            retry: true
        };
    }
}

// Usage in UpdateManager
class UpdateManager {
    async checkForUpdates() {
        try {
            // ... existing code
        } catch (error) {
            const errorInfo = UpdateErrorHandler.handleUpdateCheckError(error);
            this.handleError(errorInfo);
        }
    }

    async downloadUpdate(version) {
        try {
            // ... existing code
        } catch (error) {
            const errorInfo = UpdateErrorHandler.handleDownloadError(error);
            this.handleError(errorInfo);
        }
    }

    handleError(errorInfo) {
        // Show user-friendly error message
        this.showErrorNotification(errorInfo);

        // Log for debugging
        console.error('Update operation failed:', errorInfo);

        // Trigger error event
        window.dispatchEvent(new CustomEvent('updateError', {
            detail: errorInfo
        }));

        // Auto-retry if applicable
        if (errorInfo.retry && errorInfo.type === 'NETWORK_ERROR') {
            setTimeout(() => {
                this.checkForUpdates();
            }, 30000); // Retry after 30 seconds
        }

        // Handle re-authentication
        if (errorInfo.requiresReauth) {
            this.requestReauthentication();
        }
    }

    showErrorNotification(errorInfo) {
        // Create error notification UI
        const notification = document.createElement('div');
        notification.className = 'error-notification';
        notification.innerHTML = `
            <div class="error-content">
                <span class="error-icon">⚠️</span>
                <span class="error-message">${errorInfo.message}</span>
                ${errorInfo.retry ? '<button class="retry-btn">Retry</button>' : ''}
            </div>
        `;

        document.body.appendChild(notification);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000);
    }

    requestReauthentication() {
        // Redirect to login or show login modal
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
    }
}
```

## Security Considerations

### Authentication
- **JWT Tokens**: All API calls require valid JWT authentication tokens
- **Token Refresh**: Implement automatic token refresh before expiration
- **Secure Storage**: Store tokens securely (httpOnly cookies preferred over localStorage)

### File Integrity
- **Checksum Verification**: Always verify downloaded files using SHA256 checksums
- **HTTPS Only**: Use HTTPS in production to prevent man-in-the-middle attacks
- **Content-Type Validation**: Validate file types and content before execution

### Client Security
```javascript
class SecureUpdateManager extends UpdateManager {
    constructor(baseUrl, authToken) {
        super(baseUrl, authToken);
        this.maxRetries = 3;
        this.retryDelay = 5000;
    }

    async secureDownload(version) {
        // Validate version format
        if (!this.isValidVersion(version)) {
            throw new Error('Invalid version format');
        }

        // Check available storage
        if (!await this.hasEnoughStorage()) {
            throw new Error('Insufficient storage space');
        }

        // Download with retry logic
        return this.downloadWithRetry(version);
    }

    isValidVersion(version) {
        // Semantic version validation
        const semverRegex = /^\d+\.\d+\.\d+$/;
        return semverRegex.test(version);
    }

    async hasEnoughStorage() {
        if ('storage' in navigator && 'estimate' in navigator.storage) {
            const estimate = await navigator.storage.estimate();
            const availableSpace = estimate.quota - estimate.usage;
            return availableSpace > 100 * 1024 * 1024; // 100MB minimum
        }
        return true; // Assume sufficient if API not available
    }

    async downloadWithRetry(version, attempt = 1) {
        try {
            return await this.downloadUpdate(version);
        } catch (error) {
            if (attempt < this.maxRetries && this.isRetryableError(error)) {
                console.log(`Download attempt ${attempt} failed, retrying...`);
                await this.delay(this.retryDelay * attempt);
                return this.downloadWithRetry(version, attempt + 1);
            }
            throw error;
        }
    }

    isRetryableError(error) {
        const retryableErrors = ['NETWORK_ERROR', 'SERVER_ERROR', 'TIMEOUT'];
        return retryableErrors.includes(error.type);
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}
```

## Testing

### Unit Tests (Jest)

```javascript
// updateManager.test.js
import { UpdateManager } from './updateManager';

describe('UpdateManager', () => {
    let updateManager;
    let mockFetch;

    beforeEach(() => {
        mockFetch = jest.fn();
        global.fetch = mockFetch;
        updateManager = new UpdateManager('http://localhost:8081', 'test-token');
    });

    afterEach(() => {
        jest.restoreAllMocks();
    });

    test('should check for updates successfully', async () => {
        const mockResponse = {
            success: true,
            data: {
                updateAvailable: true,
                latestVersion: '2.1.0',
                currentVersion: '2.0.0'
            }
        };

        mockFetch.mockResolvedValueOnce({
            ok: true,
            json: () => Promise.resolve(mockResponse)
        });

        const result = await updateManager.checkForUpdates();

        expect(mockFetch).toHaveBeenCalledWith(
            'http://localhost:8081/api/v1/updates/check?currentVersion=1.0.0',
            expect.objectContaining({
                headers: expect.objectContaining({
                    'Authorization': 'Bearer test-token'
                })
            })
        );

        expect(result.updateAvailable).toBe(true);
        expect(result.latestVersion).toBe('2.1.0');
    });

    test('should handle network errors gracefully', async () => {
        mockFetch.mockRejectedValueOnce(new Error('Network error'));

        await expect(updateManager.checkForUpdates()).rejects.toThrow('Network error');
    });

    test('should download update file', async () => {
        const mockBlob = new Blob(['test content'], { type: 'application/octet-stream' });

        mockFetch.mockResolvedValueOnce({
            ok: true,
            blob: () => Promise.resolve(mockBlob),
            headers: new Map([
                ['Content-Disposition', 'attachment; filename="test-update.exe"'],
                ['X-Checksum', 'sha256:test-checksum']
            ])
        });

        // Mock URL.createObjectURL
        global.URL.createObjectURL = jest.fn(() => 'blob:test-url');
        global.URL.revokeObjectURL = jest.fn();

        const result = await updateManager.downloadUpdate('2.1.0');

        expect(result.filename).toBe('test-update.exe');
        expect(result.checksum).toBe('sha256:test-checksum');
    });
});
```

### Integration Tests

```javascript
// integration.test.js
describe('Update System Integration', () => {
    test('should complete full update flow', async () => {
        // Start with version 2.0.0
        const updateManager = new UpdateManager('http://localhost:8081', 'valid-token');

        // Check for updates
        const updateCheck = await updateManager.checkForUpdates();
        expect(updateCheck.updateAvailable).toBe(true);

        // Download update
        const downloadResult = await updateManager.downloadUpdate(updateCheck.latestVersion);
        expect(downloadResult.verified).toBe(true);

        // Verify file integrity
        expect(downloadResult.checksum).toBeTruthy();
    });
});
```

## Best Practices

### 1. Update Frequency
- **Regular Checks**: Check for updates every 1-4 hours during active use
- **Background Checks**: Perform checks when app starts or becomes active
- **User-Initiated**: Always allow manual update checks

### 2. User Experience
- **Non-Intrusive**: Don't interrupt user workflow with update notifications
- **Clear Information**: Show version numbers, file sizes, and release notes
- **Progress Feedback**: Show download progress for large files
- **Graceful Degradation**: Handle offline scenarios gracefully

### 3. Performance
- **Caching**: Cache update check results for short periods
- **Compression**: Use gzip compression for API responses
- **Chunked Downloads**: Consider chunked downloads for very large files
- **Background Downloads**: Download updates in background when possible

### 4. Error Recovery
- **Retry Logic**: Implement exponential backoff for failed requests
- **Fallback Mechanisms**: Provide alternative download sources if needed
- **User Feedback**: Always inform users about errors and recovery options

## Deployment Considerations

### Production Configuration

```javascript
// config/production.js
const productionConfig = {
    updateCheck: {
        baseUrl: 'https://your-api-domain.com',
        checkInterval: 4 * 60 * 60 * 1000, // 4 hours
        retryAttempts: 3,
        retryDelay: 30000 // 30 seconds
    },
    download: {
        chunkSize: 1024 * 1024, // 1MB chunks
        timeout: 300000, // 5 minutes
        verifyChecksum: true
    },
    notifications: {
        enabled: true,
        requirePermission: true,
        showBrowserNotification: true
    }
};

// Initialize with production config
const updateManager = new UpdateManager(
    productionConfig.updateCheck.baseUrl,
    getAuthToken()
);

updateManager.configure(productionConfig);
```

### Environment Variables

```bash
# Frontend Environment Variables
REACT_APP_API_BASE_URL=https://your-api-domain.com
REACT_APP_VERSION=2.0.0
REACT_APP_UPDATE_CHECK_INTERVAL=14400000
REACT_APP_ENABLE_AUTO_UPDATE_CHECK=true
```

## Conclusion

This HTTP-based update system provides a robust, secure, and user-friendly way to handle application updates. The system is designed to be:

- **Simple**: Easy to integrate and use
- **Reliable**: Handles errors gracefully with retry mechanisms
- **Secure**: Uses JWT authentication and file integrity verification
- **Flexible**: Supports different frontend frameworks and deployment scenarios
- **User-Friendly**: Provides clear feedback and non-intrusive notifications

The implementation can be easily customized to fit specific requirements while maintaining the core functionality of checking for updates and downloading them securely.
