/**
 * Main application module for Admin interface
 * Handles version management, pagination, filtering, and main application logic
 */

class AdminMain {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 10;
        this.totalPages = 0;
        this.allVersions = [];
        this.filteredVersions = [];
        this.currentFilters = {
            status: '',
            mandatory: '',
            search: ''
        };
    }

    /**
     * Initialize the application
     */
    async init() {
        try {
            // Initialize authentication
            if (!authManager.init()) {
                return; // Redirected to login
            }

            // Initialize UI
            adminUI.init();

            // Load initial data
            await this.loadVersions();

            console.log('Admin interface initialized successfully');
        } catch (error) {
            console.error('Error initializing admin interface:', error);
            adminUI.showAlert('error', 'Failed to initialize admin interface');
        }
    }

    /**
     * Load versions from API
     */
    async loadVersions() {
        try {
            this.showLoading(true);
            
            const response = await adminAPI.getAllVersions(this.currentPage, this.pageSize);
            
            if (response.success) {
                this.allVersions = response.data.content || [];
                this.totalPages = response.data.totalPages || 0;
                this.applyFilters();
                this.renderVersionsTable();
                this.renderPagination();
            } else {
                throw new Error(response.message || 'Failed to load versions');
            }
        } catch (error) {
            console.error('Error loading versions:', error);
            adminUI.showAlert('error', 'Failed to load versions: ' + error.message);
            this.showEmptyState();
        } finally {
            this.showLoading(false);
        }
    }

    /**
     * Apply current filters to versions
     */
    applyFilters() {
        this.filteredVersions = this.allVersions.filter(version => {
            // Status filter
            if (this.currentFilters.status) {
                const isActive = version.isActive;
                if (this.currentFilters.status === 'active' && !isActive) return false;
                if (this.currentFilters.status === 'inactive' && isActive) return false;
            }

            // Mandatory filter
            if (this.currentFilters.mandatory) {
                const isMandatory = version.isMandatory;
                if (this.currentFilters.mandatory === 'mandatory' && !isMandatory) return false;
                if (this.currentFilters.mandatory === 'optional' && isMandatory) return false;
            }

            // Search filter
            if (this.currentFilters.search) {
                const searchTerm = this.currentFilters.search.toLowerCase();
                const searchableText = [
                    version.versionNumber,
                    version.releaseNotes,
                    version.createdBy,
                    version.fileName
                ].join(' ').toLowerCase();
                
                if (!searchableText.includes(searchTerm)) return false;
            }

            return true;
        });
    }

    /**
     * Render versions table
     */
    renderVersionsTable() {
        const tableBody = document.getElementById('versionsTableBody');
        const tableContainer = document.getElementById('versionsTable');
        const emptyState = document.getElementById('emptyState');

        if (!tableBody) return;

        if (this.filteredVersions.length === 0) {
            this.showEmptyState();
            return;
        }

        // Show table and hide empty state
        if (tableContainer) tableContainer.style.display = 'block';
        if (emptyState) emptyState.style.display = 'none';

        // Clear existing rows
        tableBody.innerHTML = '';

        // Render each version
        this.filteredVersions.forEach(version => {
            const row = this.createVersionRow(version);
            tableBody.appendChild(row);
        });
    }

    /**
     * Create a table row for a version
     */
    createVersionRow(version) {
        const row = document.createElement('tr');
        
        const statusBadge = version.isActive 
            ? '<span class="status-badge status-active"><i class="fas fa-check-circle"></i> Active</span>'
            : '<span class="status-badge status-inactive"><i class="fas fa-times-circle"></i> Inactive</span>';
            
        const typeBadge = version.isMandatory
            ? '<span class="status-badge status-mandatory"><i class="fas fa-exclamation-triangle"></i> Mandatory</span>'
            : '<span class="status-badge status-optional"><i class="fas fa-info-circle"></i> Optional</span>';

        row.innerHTML = `
            <td>
                <strong>${version.versionNumber}</strong>
                ${version.minimumClientVersion ? `<br><small>Min: ${version.minimumClientVersion}</small>` : ''}
            </td>
            <td>${adminAPI.formatDate(version.releaseDate)}</td>
            <td>${statusBadge}</td>
            <td>${typeBadge}</td>
            <td>${adminAPI.formatFileSize(version.fileSize)}</td>
            <td>
                <div>
                    <strong>${version.downloadCount || 0}</strong> total
                    <br>
                    <small>${version.successfulDownloads || 0} successful</small>
                </div>
            </td>
            <td>${version.createdBy}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn action-view" onclick="viewVersion(${version.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn action-edit" onclick="editVersion(${version.id})" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn action-toggle" onclick="toggleVersionStatus(${version.id})" 
                            title="${version.isActive ? 'Deactivate' : 'Activate'}">
                        <i class="fas fa-${version.isActive ? 'pause' : 'play'}"></i>
                    </button>
                    <button class="action-btn action-delete" onclick="deleteVersion(${version.id})" title="Delete">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;

        return row;
    }

    /**
     * Show loading state
     */
    showLoading(show) {
        const loadingElement = document.getElementById('versionsLoading');
        const tableContainer = document.getElementById('versionsTable');
        const emptyState = document.getElementById('emptyState');

        if (loadingElement) {
            loadingElement.style.display = show ? 'flex' : 'none';
        }
        
        if (!show) {
            if (tableContainer) tableContainer.style.display = 'none';
            if (emptyState) emptyState.style.display = 'none';
        }
    }

    /**
     * Show empty state
     */
    showEmptyState() {
        const tableContainer = document.getElementById('versionsTable');
        const emptyState = document.getElementById('emptyState');

        if (tableContainer) tableContainer.style.display = 'none';
        if (emptyState) emptyState.style.display = 'block';
    }

    /**
     * Render pagination controls
     */
    renderPagination() {
        const paginationContainer = document.getElementById('pagination');
        const paginationInfo = document.getElementById('paginationInfo');
        const prevButton = document.getElementById('prevPage');
        const nextButton = document.getElementById('nextPage');
        const pageNumbers = document.getElementById('pageNumbers');

        if (!paginationContainer) return;

        // Show/hide pagination based on data
        if (this.totalPages <= 1) {
            paginationContainer.style.display = 'none';
            return;
        }

        paginationContainer.style.display = 'flex';

        // Update pagination info
        const startItem = this.currentPage * this.pageSize + 1;
        const endItem = Math.min((this.currentPage + 1) * this.pageSize, this.filteredVersions.length);
        const totalItems = this.filteredVersions.length;

        if (paginationInfo) {
            paginationInfo.textContent = `Showing ${startItem}-${endItem} of ${totalItems} versions`;
        }

        // Update navigation buttons
        if (prevButton) {
            prevButton.disabled = this.currentPage === 0;
        }
        
        if (nextButton) {
            nextButton.disabled = this.currentPage >= this.totalPages - 1;
        }

        // Render page numbers
        if (pageNumbers) {
            pageNumbers.innerHTML = '';
            
            const maxVisiblePages = 5;
            let startPage = Math.max(0, this.currentPage - Math.floor(maxVisiblePages / 2));
            let endPage = Math.min(this.totalPages - 1, startPage + maxVisiblePages - 1);
            
            // Adjust start page if we're near the end
            if (endPage - startPage < maxVisiblePages - 1) {
                startPage = Math.max(0, endPage - maxVisiblePages + 1);
            }

            for (let i = startPage; i <= endPage; i++) {
                const pageButton = document.createElement('button');
                pageButton.className = `page-number ${i === this.currentPage ? 'active' : ''}`;
                pageButton.textContent = i + 1;
                pageButton.onclick = () => this.goToPage(i);
                pageNumbers.appendChild(pageButton);
            }
        }
    }

    /**
     * Go to specific page
     */
    async goToPage(page) {
        if (page >= 0 && page < this.totalPages && page !== this.currentPage) {
            this.currentPage = page;
            await this.loadVersions();
        }
    }

    /**
     * Change page by offset
     */
    async changePage(offset) {
        const newPage = this.currentPage + offset;
        await this.goToPage(newPage);
    }

    /**
     * Filter versions based on current filter values
     */
    filterVersions() {
        const statusFilter = document.getElementById('statusFilter');
        const mandatoryFilter = document.getElementById('mandatoryFilter');
        const searchFilter = document.getElementById('searchFilter');

        this.currentFilters = {
            status: statusFilter ? statusFilter.value : '',
            mandatory: mandatoryFilter ? mandatoryFilter.value : '',
            search: searchFilter ? searchFilter.value : ''
        };

        this.applyFilters();
        this.renderVersionsTable();
        this.renderPagination();
    }

    /**
     * Refresh versions list
     */
    async refreshVersions() {
        await this.loadVersions();
        adminUI.showAlert('info', 'Versions list refreshed');
    }

    /**
     * View version details
     */
    async viewVersion(id) {
        try {
            const response = await adminAPI.getVersionById(id);
            if (response.success) {
                this.showVersionDetails(response.data);
            } else {
                throw new Error(response.message || 'Failed to load version details');
            }
        } catch (error) {
            adminUI.showAlert('error', 'Failed to load version details: ' + error.message);
        }
    }

    /**
     * Show version details in modal
     */
    showVersionDetails(version) {
        const modalTitle = document.getElementById('modalTitle');
        const modalBody = document.getElementById('modalBody');

        if (modalTitle) modalTitle.textContent = `Version ${version.versionNumber} Details`;
        
        if (modalBody) {
            modalBody.innerHTML = `
                <div class="version-details">
                    <div class="detail-group">
                        <label>Version Number:</label>
                        <span>${version.versionNumber}</span>
                    </div>
                    <div class="detail-group">
                        <label>Release Date:</label>
                        <span>${adminAPI.formatDate(version.releaseDate)}</span>
                    </div>
                    <div class="detail-group">
                        <label>Status:</label>
                        <span class="status-badge ${version.isActive ? 'status-active' : 'status-inactive'}">
                            <i class="fas fa-${version.isActive ? 'check-circle' : 'times-circle'}"></i>
                            ${version.isActive ? 'Active' : 'Inactive'}
                        </span>
                    </div>
                    <div class="detail-group">
                        <label>Type:</label>
                        <span class="status-badge ${version.isMandatory ? 'status-mandatory' : 'status-optional'}">
                            <i class="fas fa-${version.isMandatory ? 'exclamation-triangle' : 'info-circle'}"></i>
                            ${version.isMandatory ? 'Mandatory' : 'Optional'}
                        </span>
                    </div>
                    ${version.minimumClientVersion ? `
                        <div class="detail-group">
                            <label>Minimum Client Version:</label>
                            <span>${version.minimumClientVersion}</span>
                        </div>
                    ` : ''}
                    <div class="detail-group">
                        <label>File Name:</label>
                        <span>${version.fileName}</span>
                    </div>
                    <div class="detail-group">
                        <label>File Size:</label>
                        <span>${adminAPI.formatFileSize(version.fileSize)}</span>
                    </div>
                    <div class="detail-group">
                        <label>File Checksum:</label>
                        <span style="font-family: monospace; font-size: 0.8em;">${version.fileChecksum}</span>
                    </div>
                    <div class="detail-group">
                        <label>Download Statistics:</label>
                        <div>
                            <div>Total Downloads: <strong>${version.downloadCount || 0}</strong></div>
                            <div>Successful: <strong>${version.successfulDownloads || 0}</strong></div>
                            <div>Failed: <strong>${version.failedDownloads || 0}</strong></div>
                        </div>
                    </div>
                    <div class="detail-group">
                        <label>Created By:</label>
                        <span>${version.createdBy}</span>
                    </div>
                    <div class="detail-group">
                        <label>Created At:</label>
                        <span>${adminAPI.formatDate(version.createdAt)}</span>
                    </div>
                    ${version.updatedAt ? `
                        <div class="detail-group">
                            <label>Last Updated:</label>
                            <span>${adminAPI.formatDate(version.updatedAt)}</span>
                        </div>
                    ` : ''}
                    <div class="detail-group">
                        <label>Release Notes:</label>
                        <div style="white-space: pre-wrap; background: var(--bg-tertiary); padding: 1rem; border-radius: var(--border-radius-sm); margin-top: 0.5rem;">${version.releaseNotes}</div>
                    </div>
                </div>
            `;
        }

        adminUI.showModal('versionModal');
    }
}

// Global main instance
const adminMain = new AdminMain();

// Global functions for HTML onclick handlers
window.refreshVersions = () => adminMain.refreshVersions();
window.filterVersions = () => adminMain.filterVersions();
window.changePage = (offset) => adminMain.changePage(offset);
window.viewVersion = (id) => adminMain.viewVersion(id);

// Placeholder functions for actions that will be implemented
window.editVersion = (id) => {
    adminUI.showAlert('info', 'Edit functionality will be implemented in a future update');
};

window.toggleVersionStatus = async (id) => {
    try {
        const response = await adminAPI.toggleVersionStatus(id);
        if (response.success) {
            adminUI.showAlert('success', 'Version status updated successfully');
            adminMain.refreshVersions();
        } else {
            throw new Error(response.message || 'Failed to update version status');
        }
    } catch (error) {
        adminUI.showAlert('error', 'Failed to update version status: ' + error.message);
    }
};

window.deleteVersion = (id) => {
    adminUI.showConfirmation(
        'Delete Version',
        'Are you sure you want to delete this version? This action cannot be undone.',
        async () => {
            try {
                const response = await adminAPI.deleteVersion(id);
                if (response.success) {
                    adminUI.showAlert('success', 'Version deleted successfully');
                    adminMain.refreshVersions();
                } else {
                    throw new Error(response.message || 'Failed to delete version');
                }
            } catch (error) {
                adminUI.showAlert('error', 'Failed to delete version: ' + error.message);
            }
        }
    );
};

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    adminMain.init();
});

// Export for use in other modules
window.adminMain = adminMain;
