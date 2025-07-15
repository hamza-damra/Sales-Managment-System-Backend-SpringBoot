/**
 * UI management module for Admin interface
 * Handles all UI interactions, modals, alerts, and form management
 */

class AdminUI {
    constructor() {
        this.currentPage = 0;
        this.pageSize = 10;
        this.totalPages = 0;
        this.allVersions = [];
        this.filteredVersions = [];
        this.confirmCallback = null;
    }

    /**
     * Initialize UI components
     */
    init() {
        console.log('AdminUI init() called'); // Debug log
        this.setupEventListeners();
        this.setDefaultDateTime();
        this.loadVersions();
        this.loadStatistics();
        console.log('AdminUI initialization complete'); // Debug log
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // File upload drag and drop
        this.setupFileUpload();
        
        // Form submission
        const uploadForm = document.getElementById('uploadForm');
        console.log('Upload form element:', uploadForm); // Debug log
        if (uploadForm) {
            uploadForm.addEventListener('submit', this.handleFormSubmit.bind(this));
            console.log('Form submit event listener attached'); // Debug log
        } else {
            console.error('Upload form not found!'); // Debug log
        }

        // Modal close on background click
        document.addEventListener('click', (event) => {
            if (event.target.classList.contains('modal')) {
                this.closeModal(event.target.id);
            }
        });

        // Escape key to close modals
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                const openModal = document.querySelector('.modal.show');
                if (openModal) {
                    this.closeModal(openModal.id);
                }
            }
        });
    }

    /**
     * Setup file upload drag and drop functionality
     */
    setupFileUpload() {
        const fileUploadArea = document.getElementById('fileUploadArea');
        const fileInput = document.getElementById('fileInput');

        if (!fileUploadArea || !fileInput) return;

        // Prevent default drag behaviors
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            fileUploadArea.addEventListener(eventName, this.preventDefaults, false);
            document.body.addEventListener(eventName, this.preventDefaults, false);
        });

        // Highlight drop area when item is dragged over it
        ['dragenter', 'dragover'].forEach(eventName => {
            fileUploadArea.addEventListener(eventName, () => {
                fileUploadArea.classList.add('dragover');
            }, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            fileUploadArea.addEventListener(eventName, () => {
                fileUploadArea.classList.remove('dragover');
            }, false);
        });

        // Handle dropped files
        fileUploadArea.addEventListener('drop', (event) => {
            const files = event.dataTransfer.files;
            if (files.length > 0) {
                this.handleFileSelect(files[0]);
            }
        }, false);

        // Handle file input change
        fileInput.addEventListener('change', (event) => {
            if (event.target.files.length > 0) {
                this.handleFileSelect(event.target.files[0]);
            }
        });

        // Handle click on upload area
        fileUploadArea.addEventListener('click', () => {
            fileInput.click();
        });
    }

    /**
     * Prevent default drag behaviors
     */
    preventDefaults(event) {
        event.preventDefault();
        event.stopPropagation();
    }

    /**
     * Handle file selection
     */
    handleFileSelect(file) {
        try {
            // Validate file
            adminAPI.validateFile(file);
            
            // Update UI to show selected file
            this.displaySelectedFile(file);
            
            // Hide upload area and show file info
            document.getElementById('fileUploadArea').style.display = 'none';
            document.getElementById('fileInfo').style.display = 'flex';
            
        } catch (error) {
            this.showAlert('error', error.message);
        }
    }

    /**
     * Display selected file information
     */
    displaySelectedFile(file) {
        const fileName = document.getElementById('fileName');
        const fileSize = document.getElementById('fileSize');
        
        if (fileName) fileName.textContent = file.name;
        if (fileSize) fileSize.textContent = adminAPI.formatFileSize(file.size);
    }

    /**
     * Clear selected file
     */
    clearFile() {
        const fileInput = document.getElementById('fileInput');
        if (fileInput) fileInput.value = '';
        
        document.getElementById('fileUploadArea').style.display = 'block';
        document.getElementById('fileInfo').style.display = 'none';
    }

    /**
     * Set default date and time to current
     */
    setDefaultDateTime() {
        const releaseDateInput = document.getElementById('releaseDate');
        if (releaseDateInput) {
            const now = new Date();
            releaseDateInput.value = now.toISOString().slice(0, 16);
        }
    }

    /**
     * Handle form submission
     */
    async handleFormSubmit(event) {
        event.preventDefault();

        console.log('Form submission started'); // Keep minimal logging

        const form = event.target;
        const formData = new FormData(form);
        const submitBtn = document.getElementById('submitBtn');

        try {
            // Handle checkbox value explicitly (checkboxes don't send false values)
            const isMandatoryCheckbox = document.getElementById('isMandatory');
            if (isMandatoryCheckbox) {
                // Remove any existing isMandatory value and set the correct boolean value
                formData.delete('isMandatory');
                formData.append('isMandatory', isMandatoryCheckbox.checked ? 'true' : 'false');
            }

            // Validate form
            this.validateForm(formData);

            // Disable submit button and show progress
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
            this.showProgress(0);

            console.log('Starting file upload...');

            // Upload file
            const response = await adminAPI.createVersion(formData, (progress) => {
                this.updateProgress(progress);
            });

            console.log('Upload response:', response);

            if (response && response.success) {
                this.showAlert('success', 'Version uploaded successfully!');
                this.resetForm();
                this.loadVersions(); // Refresh versions list
            } else {
                throw new Error(response?.message || 'Upload failed - no response received');
            }

        } catch (error) {
            console.error('Upload error:', error);
            this.showAlert('error', `Upload failed: ${error.message}`);
        } finally {
            // Reset submit button
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-upload"></i> Upload Version';
            this.hideProgress();
        }
    }

    /**
     * Validate form data
     */
    validateForm(formData) {
        const versionNumber = formData.get('versionNumber');
        const releaseNotes = formData.get('releaseNotes');
        const releaseDate = formData.get('releaseDate');
        const file = formData.get('file');

        if (!versionNumber || !adminAPI.validateVersionNumber(versionNumber)) {
            throw new Error('Please enter a valid version number (e.g., 1.0.0)');
        }

        if (!releaseNotes || releaseNotes.trim().length < 10) {
            throw new Error('Release notes must be at least 10 characters long');
        }

        if (!releaseDate || releaseDate.trim().length === 0) {
            throw new Error('Please select a release date');
        }

        if (!file || file.size === 0) {
            throw new Error('Please select a file to upload');
        }

        // Validate minimum client version if provided
        const minimumClientVersion = formData.get('minimumClientVersion');
        if (minimumClientVersion && !adminAPI.validateVersionNumber(minimumClientVersion)) {
            throw new Error('Please enter a valid minimum client version (e.g., 1.0.0)');
        }
    }

    /**
     * Show upload progress
     */
    showProgress(percent) {
        const progressContainer = document.getElementById('uploadProgress');
        const progressFill = document.getElementById('progressFill');
        const progressPercent = document.getElementById('progressPercent');
        
        if (progressContainer) progressContainer.style.display = 'block';
        if (progressFill) progressFill.style.width = '0%';
        if (progressPercent) progressPercent.textContent = '0%';
    }

    /**
     * Update upload progress
     */
    updateProgress(percent) {
        const progressFill = document.getElementById('progressFill');
        const progressPercent = document.getElementById('progressPercent');
        const progressText = document.getElementById('progressText');
        
        const roundedPercent = Math.round(percent);
        
        if (progressFill) progressFill.style.width = `${roundedPercent}%`;
        if (progressPercent) progressPercent.textContent = `${roundedPercent}%`;
        
        if (progressText) {
            if (roundedPercent < 100) {
                progressText.textContent = 'Uploading...';
            } else {
                progressText.textContent = 'Processing...';
            }
        }
    }

    /**
     * Hide upload progress
     */
    hideProgress() {
        const progressContainer = document.getElementById('uploadProgress');
        if (progressContainer) {
            setTimeout(() => {
                progressContainer.style.display = 'none';
            }, 1000);
        }
    }

    /**
     * Reset form to initial state
     */
    resetForm() {
        const form = document.getElementById('uploadForm');
        if (form) form.reset();
        
        this.clearFile();
        this.setDefaultDateTime();
        this.hideProgress();
    }

    /**
     * Show alert message
     */
    showAlert(type, message) {
        const alertContainer = document.getElementById('alertContainer');
        if (!alertContainer) return;
        
        const alertId = 'alert-' + Date.now();
        const alertHtml = `
            <div id="${alertId}" class="alert alert-${type}">
                <i class="fas fa-${this.getAlertIcon(type)}"></i>
                <span>${message}</span>
                <button onclick="this.parentElement.remove()" style="margin-left: auto; background: none; border: none; cursor: pointer;">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;
        
        alertContainer.insertAdjacentHTML('beforeend', alertHtml);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            const alert = document.getElementById(alertId);
            if (alert) alert.remove();
        }, 5000);
    }

    /**
     * Get icon for alert type
     */
    getAlertIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    /**
     * Show modal
     */
    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('show');
            document.body.style.overflow = 'hidden';
        }
    }

    /**
     * Close modal
     */
    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('show');
            document.body.style.overflow = '';
        }
    }

    /**
     * Show confirmation dialog
     */
    showConfirmation(title, message, callback) {
        const confirmTitle = document.getElementById('confirmTitle');
        const confirmMessage = document.getElementById('confirmMessage');
        
        if (confirmTitle) confirmTitle.textContent = title;
        if (confirmMessage) confirmMessage.textContent = message;
        
        this.confirmCallback = callback;
        this.showModal('confirmModal');
    }

    /**
     * Handle confirmation action
     */
    confirmAction() {
        if (this.confirmCallback) {
            this.confirmCallback();
            this.confirmCallback = null;
        }
        this.closeModal('confirmModal');
    }

    /**
     * Load versions list (placeholder for future implementation)
     */
    async loadVersions() {
        try {
            console.log('Loading versions...');
            // TODO: Implement version loading and display
            // const versions = await adminAPI.getAllVersions();
            // this.displayVersions(versions);
        } catch (error) {
            console.error('Error loading versions:', error);
        }
    }

    /**
     * Load statistics (placeholder for future implementation)
     */
    async loadStatistics() {
        try {
            console.log('Loading statistics...');
            // TODO: Implement statistics loading and display
            // const stats = await adminAPI.getUpdateStatistics();
            // this.displayStatistics(stats);
        } catch (error) {
            console.error('Error loading statistics:', error);
        }
    }
}

// Global UI instance
const adminUI = new AdminUI();

// Global functions for HTML onclick handlers
window.clearFile = () => adminUI.clearFile();
window.resetForm = () => adminUI.resetForm();
window.showModal = (modalId) => adminUI.showModal(modalId);
window.closeModal = (modalId) => adminUI.closeModal(modalId);
window.confirmAction = () => adminUI.confirmAction();
window.showAlert = (type, message) => adminUI.showAlert(type, message);

// Export for use in other modules
window.adminUI = adminUI;
