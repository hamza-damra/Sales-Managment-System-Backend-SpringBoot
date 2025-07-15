/**
 * Authentication module for Admin interface
 * Handles JWT token management and user authentication
 */

class AuthManager {
    constructor() {
        // Use port 8081 for the backend API
        this.baseUrl = window.location.protocol + '//' + window.location.hostname + ':8081';
        this.apiBase = '/api/auth';
        this.tokenKey = 'admin_jwt_token';
        this.userKey = 'admin_user_info';
        this.refreshTokenKey = 'admin_refresh_token';
    }

    /**
     * Get stored JWT token
     */
    getToken() {
        return localStorage.getItem(this.tokenKey);
    }

    /**
     * Store JWT token
     */
    setToken(token) {
        localStorage.setItem(this.tokenKey, token);
    }

    /**
     * Get stored refresh token
     */
    getRefreshToken() {
        return localStorage.getItem(this.refreshTokenKey);
    }

    /**
     * Store refresh token
     */
    setRefreshToken(token) {
        localStorage.setItem(this.refreshTokenKey, token);
    }

    /**
     * Get stored user information
     */
    getUserInfo() {
        const userInfo = localStorage.getItem(this.userKey);
        return userInfo ? JSON.parse(userInfo) : null;
    }

    /**
     * Store user information
     */
    setUserInfo(userInfo) {
        localStorage.setItem(this.userKey, JSON.stringify(userInfo));
    }

    /**
     * Clear all stored authentication data
     */
    clearAuth() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.refreshTokenKey);
        localStorage.removeItem(this.userKey);
    }

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;

        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Date.now() / 1000;
            return payload.exp > currentTime;
        } catch (error) {
            console.error('Error parsing token:', error);
            return false;
        }
    }

    /**
     * Check if user has admin role
     */
    isAdmin() {
        const userInfo = this.getUserInfo();
        if (!userInfo) return false;

        // Check for various admin role formats
        const role = userInfo.role;
        return role === 'ADMIN' || role === 'ROLE_ADMIN' || role === 'admin';
    }

    /**
     * Login user with credentials
     */
    async login(username, password) {
        try {
            const response = await fetch(`${this.baseUrl}${this.apiBase}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    username: username,
                    password: password
                })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Login failed');
            }

            const data = await response.json();

            // Store authentication data
            this.setToken(data.accessToken);
            if (data.refreshToken) {
                this.setRefreshToken(data.refreshToken);
            }

            // Extract user info from response (not from token)
            let userInfo;
            if (data.user) {
                // Use user info from response
                userInfo = {
                    username: data.user.username,
                    role: data.user.role,
                    email: data.user.email,
                    firstName: data.user.firstName,
                    lastName: data.user.lastName,
                    id: data.user.id
                };
            } else {
                // Fallback to extracting from token
                userInfo = this.extractUserInfoFromToken(data.accessToken);
            }

            this.setUserInfo(userInfo);

            // Verify admin role
            if (!userInfo || userInfo.role !== 'ADMIN') {
                this.clearAuth();
                throw new Error('Access denied. Admin privileges required.');
            }

            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    }

    /**
     * Logout user
     */
    logout() {
        this.clearAuth();
        window.location.href = '/admin/login.html';
    }

    /**
     * Extract user information from JWT token
     */
    extractUserInfoFromToken(token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return {
                username: payload.sub,
                role: payload.role || 'USER',
                exp: payload.exp,
                iat: payload.iat
            };
        } catch (error) {
            console.error('Error extracting user info from token:', error);
            return null;
        }
    }

    /**
     * Refresh authentication token
     */
    async refreshToken() {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            throw new Error('No refresh token available');
        }

        try {
            const response = await fetch(`${this.baseUrl}${this.apiBase}/refresh`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${refreshToken}`
                }
            });

            if (!response.ok) {
                throw new Error('Token refresh failed');
            }

            const data = await response.json();
            this.setToken(data.accessToken);
            
            if (data.refreshToken) {
                this.setRefreshToken(data.refreshToken);
            }

            const userInfo = this.extractUserInfoFromToken(data.accessToken);
            this.setUserInfo(userInfo);

            return data;
        } catch (error) {
            console.error('Token refresh error:', error);
            this.clearAuth();
            throw error;
        }
    }

    /**
     * Get authorization headers for API requests
     */
    getAuthHeaders() {
        const token = this.getToken();
        if (!token) {
            throw new Error('No authentication token available');
        }

        return {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        };
    }

    /**
     * Make authenticated API request
     */
    async makeAuthenticatedRequest(url, options = {}) {
        if (!this.isAuthenticated()) {
            try {
                await this.refreshToken();
            } catch (error) {
                this.logout();
                throw new Error('Authentication required');
            }
        }

        const headers = {
            ...this.getAuthHeaders(),
            ...options.headers
        };

        const response = await fetch(url, {
            ...options,
            headers
        });

        // Handle token expiration
        if (response.status === 401) {
            try {
                await this.refreshToken();
                // Retry the request with new token
                const newHeaders = {
                    ...this.getAuthHeaders(),
                    ...options.headers
                };
                return await fetch(url, {
                    ...options,
                    headers: newHeaders
                });
            } catch (error) {
                this.logout();
                throw new Error('Authentication failed');
            }
        }

        return response;
    }

    /**
     * Initialize authentication check
     */
    init() {
        const isOnLoginPage = window.location.pathname.includes('login');

        // If on login page and already authenticated with admin role, redirect to dashboard
        if (isOnLoginPage && this.isAuthenticated() && this.isAdmin()) {
            window.location.href = '/admin/index.html';
            return false;
        }

        // If not on login page and not authenticated or not admin, redirect to login
        if (!isOnLoginPage && (!this.isAuthenticated() || !this.isAdmin())) {
            window.location.href = '/admin/login.html';
            return false;
        }

        // Update UI with user info if not on login page
        if (!isOnLoginPage) {
            this.updateUserDisplay();
        }

        return true;
    }

    /**
     * Update user display in navigation
     */
    updateUserDisplay() {
        const userInfo = this.getUserInfo();
        const userElement = document.getElementById('currentUser');
        
        if (userElement && userInfo) {
            userElement.textContent = userInfo.username;
        }
    }

    /**
     * Check token expiration and show warning
     */
    checkTokenExpiration() {
        const userInfo = this.getUserInfo();
        if (!userInfo) return;

        const currentTime = Date.now() / 1000;
        const timeUntilExpiry = userInfo.exp - currentTime;
        
        // Show warning if token expires in less than 5 minutes
        if (timeUntilExpiry < 300 && timeUntilExpiry > 0) {
            this.showExpirationWarning(Math.floor(timeUntilExpiry / 60));
        }
    }

    /**
     * Show token expiration warning
     */
    showExpirationWarning(minutesLeft) {
        const message = `Your session will expire in ${minutesLeft} minute(s). Please save your work.`;
        showAlert('warning', message);
    }
}

// Global auth manager instance
const authManager = new AuthManager();

// Global logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        authManager.logout();
    }
}

// Check token expiration every minute
setInterval(() => {
    if (authManager.isAuthenticated()) {
        authManager.checkTokenExpiration();
    }
}, 60000);

// Export for use in other modules
window.authManager = authManager;
