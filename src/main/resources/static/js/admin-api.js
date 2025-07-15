/**
 * API management module for Admin interface
 * Handles all API calls to the backend update management system
 */

class AdminAPI {
    constructor() {
        // Use port 8081 for the backend API
        this.baseUrl = window.location.protocol + '//' + window.location.hostname + ':8081';
        this.apiBase = '/api/v1/admin/updates';
    }

    /**
     * Get all versions with pagination
     */
    async getAllVersions(page = 0, size = 10) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions?page=${page}&size=${size}`;
            const response = await authManager.makeAuthenticatedRequest(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch versions');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching versions:', error);
            throw error;
        }
    }

    /**
     * Get all active versions
     */
    async getAllActiveVersions() {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions/active`;
            const response = await authManager.makeAuthenticatedRequest(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch active versions');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching active versions:', error);
            throw error;
        }
    }

    /**
     * Create new version with file upload
     */
    async createVersion(formData, onProgress = null) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions`;
            
            // Create XMLHttpRequest for progress tracking
            return new Promise((resolve, reject) => {
                const xhr = new XMLHttpRequest();
                
                // Set up progress tracking
                if (onProgress) {
                    xhr.upload.addEventListener('progress', (event) => {
                        if (event.lengthComputable) {
                            const percentComplete = (event.loaded / event.total) * 100;
                            onProgress(percentComplete);
                        }
                    });
                }
                
                // Set up response handling
                xhr.addEventListener('load', () => {
                    console.log('XHR Response Status:', xhr.status);
                    console.log('XHR Response Text:', xhr.responseText);

                    if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                            const response = JSON.parse(xhr.responseText);
                            console.log('Parsed response:', response);
                            resolve(response);
                        } catch (error) {
                            console.error('JSON parse error:', error);
                            reject(new Error('Invalid response format: ' + xhr.responseText));
                        }
                    } else {
                        try {
                            const errorData = JSON.parse(xhr.responseText);
                            console.error('Server error response:', errorData);
                            reject(new Error(errorData.message || `Upload failed with status: ${xhr.status}`));
                        } catch (error) {
                            console.error('Error parsing error response:', error);
                            reject(new Error(`Upload failed with status: ${xhr.status} - ${xhr.responseText}`));
                        }
                    }
                });
                
                xhr.addEventListener('error', () => {
                    console.error('XHR Network error');
                    reject(new Error('Network error during upload. Please check your connection and try again.'));
                });

                xhr.addEventListener('abort', () => {
                    console.error('XHR Upload aborted');
                    reject(new Error('Upload was aborted'));
                });

                xhr.addEventListener('timeout', () => {
                    console.error('XHR Upload timeout');
                    reject(new Error('Upload timed out. Please try again.'));
                });
                
                // Open request and set headers
                xhr.open('POST', url);
                
                const token = authManager.getToken();
                if (token) {
                    xhr.setRequestHeader('Authorization', `Bearer ${token}`);
                }
                
                // Send the form data
                xhr.send(formData);
            });
        } catch (error) {
            console.error('Error creating version:', error);
            throw error;
        }
    }

    /**
     * Update version information
     */
    async updateVersion(id, versionData) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions/${id}`;
            const response = await authManager.makeAuthenticatedRequest(url, {
                method: 'PUT',
                body: JSON.stringify(versionData)
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to update version');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error updating version:', error);
            throw error;
        }
    }

    /**
     * Get version by ID
     */
    async getVersionById(id) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions/${id}`;
            const response = await authManager.makeAuthenticatedRequest(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch version');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching version:', error);
            throw error;
        }
    }

    /**
     * Toggle version active status
     */
    async toggleVersionStatus(id) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions/${id}/toggle-status`;
            const response = await authManager.makeAuthenticatedRequest(url, {
                method: 'PATCH'
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to toggle version status');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error toggling version status:', error);
            throw error;
        }
    }

    /**
     * Delete version
     */
    async deleteVersion(id) {
        try {
            const url = `${this.baseUrl}${this.apiBase}/versions/${id}`;
            const response = await authManager.makeAuthenticatedRequest(url, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to delete version');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error deleting version:', error);
            throw error;
        }
    }

    /**
     * Get update statistics
     */
    async getUpdateStatistics() {
        try {
            const url = `${this.baseUrl}${this.apiBase}/statistics`;
            const response = await authManager.makeAuthenticatedRequest(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch statistics');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching statistics:', error);
            throw error;
        }
    }

    /**
     * Get system health metrics
     */
    async getSystemHealth() {
        try {
            const url = `${this.baseUrl}${this.apiBase}/health`;
            const response = await authManager.makeAuthenticatedRequest(url);
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to fetch system health');
            }
            
            return await response.json();
        } catch (error) {
            console.error('Error fetching system health:', error);
            throw error;
        }
    }

    /**
     * Validate file before upload
     */
    validateFile(file) {
        const maxSize = 500 * 1024 * 1024; // 500MB
        const allowedExtensions = ['jar', 'exe', 'msi', 'dmg', 'deb', 'rpm'];
        
        // Check file size
        if (file.size > maxSize) {
            throw new Error(`File size exceeds maximum limit of 500MB. Current size: ${this.formatFileSize(file.size)}`);
        }
        
        // Check file extension
        const fileName = file.name.toLowerCase();
        const extension = fileName.split('.').pop();
        
        if (!allowedExtensions.includes(extension)) {
            throw new Error(`Invalid file type. Allowed extensions: ${allowedExtensions.join(', ')}`);
        }
        
        return true;
    }

    /**
     * Format file size for display
     */
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    /**
     * Validate version number format
     */
    validateVersionNumber(version) {
        const versionRegex = /^\d+\.\d+\.\d+$/;
        return versionRegex.test(version);
    }

    /**
     * Compare version numbers
     */
    compareVersions(version1, version2) {
        const v1Parts = version1.split('.').map(Number);
        const v2Parts = version2.split('.').map(Number);
        
        for (let i = 0; i < Math.max(v1Parts.length, v2Parts.length); i++) {
            const v1Part = v1Parts[i] || 0;
            const v2Part = v2Parts[i] || 0;
            
            if (v1Part > v2Part) return 1;
            if (v1Part < v2Part) return -1;
        }
        
        return 0;
    }

    /**
     * Format date for display
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString();
    }

    /**
     * Format date for input field
     */
    formatDateForInput(dateString) {
        const date = new Date(dateString);
        return date.toISOString().slice(0, 16);
    }
}

// Global API instance
const adminAPI = new AdminAPI();

// Export for use in other modules
window.adminAPI = adminAPI;
