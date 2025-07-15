/**
 * RTL (Right-to-Left) Support Module
 * Handles language direction switching for Arabic, Hebrew, and other RTL languages
 */

class RTLSupport {
    constructor() {
        this.isRTL = false;
        this.storageKey = 'admin-rtl-direction';
        this.init();
    }

    /**
     * Initialize RTL support
     */
    init() {
        // Load saved direction preference
        this.loadDirection();
        
        // Apply initial direction
        this.applyDirection();
        
        // Setup keyboard shortcut (Ctrl+Shift+R)
        this.setupKeyboardShortcut();
        
        console.log('RTL Support initialized. Current direction:', this.isRTL ? 'RTL' : 'LTR');
    }

    /**
     * Load direction preference from localStorage
     */
    loadDirection() {
        const saved = localStorage.getItem(this.storageKey);
        if (saved !== null) {
            this.isRTL = saved === 'true';
        } else {
            // Auto-detect based on browser language
            this.isRTL = this.detectRTLLanguage();
        }
    }

    /**
     * Save direction preference to localStorage
     */
    saveDirection() {
        localStorage.setItem(this.storageKey, this.isRTL.toString());
    }

    /**
     * Detect if the browser language is RTL
     */
    detectRTLLanguage() {
        const rtlLanguages = ['ar', 'he', 'fa', 'ur', 'ku', 'dv'];
        const browserLang = navigator.language.split('-')[0];
        return rtlLanguages.includes(browserLang);
    }

    /**
     * Toggle between RTL and LTR
     */
    toggle() {
        this.isRTL = !this.isRTL;
        this.applyDirection();
        this.saveDirection();
        
        // Show notification
        this.showDirectionNotification();
        
        console.log('Direction toggled to:', this.isRTL ? 'RTL' : 'LTR');
    }

    /**
     * Apply the current direction to the document
     */
    applyDirection() {
        const html = document.documentElement;
        
        if (this.isRTL) {
            html.setAttribute('dir', 'rtl');
            html.setAttribute('lang', 'ar'); // Default to Arabic for RTL
        } else {
            html.setAttribute('dir', 'ltr');
            html.setAttribute('lang', 'en'); // Default to English for LTR
        }

        // Update toggle button icon
        this.updateToggleButton();
        
        // Trigger custom event for other components
        window.dispatchEvent(new CustomEvent('directionChanged', {
            detail: { isRTL: this.isRTL }
        }));
    }

    /**
     * Update the toggle button icon and tooltip
     */
    updateToggleButton() {
        const toggleBtn = document.querySelector('.rtl-toggle');
        if (toggleBtn) {
            const icon = toggleBtn.querySelector('i');
            const tooltip = this.isRTL ? 'Switch to LTR' : 'Switch to RTL';
            
            toggleBtn.setAttribute('title', tooltip);
            
            // Update icon based on direction
            if (icon) {
                icon.className = this.isRTL ? 'fas fa-align-left' : 'fas fa-align-right';
            }
        }
    }

    /**
     * Show direction change notification
     */
    showDirectionNotification() {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = 'direction-notification';
        notification.innerHTML = `
            <i class="fas fa-${this.isRTL ? 'align-right' : 'align-left'}"></i>
            <span>Switched to ${this.isRTL ? 'RTL' : 'LTR'} mode</span>
        `;
        
        // Add styles
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'var(--primary-color)',
            color: 'white',
            padding: '0.75rem 1.5rem',
            borderRadius: 'var(--border-radius)',
            boxShadow: 'var(--shadow-lg)',
            zIndex: '10000',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            fontSize: '0.875rem',
            fontWeight: '500',
            opacity: '0',
            transition: 'all 0.3s ease'
        });
        
        document.body.appendChild(notification);
        
        // Animate in
        requestAnimationFrame(() => {
            notification.style.opacity = '1';
            notification.style.transform = 'translateX(-50%) translateY(0)';
        });
        
        // Remove after 3 seconds
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateX(-50%) translateY(-20px)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }

    /**
     * Setup keyboard shortcut for direction toggle
     */
    setupKeyboardShortcut() {
        document.addEventListener('keydown', (event) => {
            // Ctrl+Shift+R to toggle direction
            if (event.ctrlKey && event.shiftKey && event.key === 'R') {
                event.preventDefault();
                this.toggle();
            }
        });
    }

    /**
     * Get current direction
     */
    getDirection() {
        return this.isRTL ? 'rtl' : 'ltr';
    }

    /**
     * Set direction programmatically
     */
    setDirection(direction) {
        const newIsRTL = direction === 'rtl';
        if (newIsRTL !== this.isRTL) {
            this.isRTL = newIsRTL;
            this.applyDirection();
            this.saveDirection();
        }
    }

    /**
     * Force RTL mode
     */
    forceRTL() {
        this.setDirection('rtl');
    }

    /**
     * Force LTR mode
     */
    forceLTR() {
        this.setDirection('ltr');
    }
}

// Global RTL support instance
const rtlSupport = new RTLSupport();

// Global function for HTML onclick handlers
window.toggleRTL = () => rtlSupport.toggle();

// Export for use in other modules
window.rtlSupport = rtlSupport;

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        rtlSupport.init();
    });
} else {
    rtlSupport.init();
}
