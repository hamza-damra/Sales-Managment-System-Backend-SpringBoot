# Sales Management System - Admin Web Interface

## Quick Start

### 1. Access the Interface
- **Login**: Navigate to `/admin/login.html`
- **Main Interface**: After login, you'll be at `/admin/index.html`

### 2. Default Admin Account
If you need to create an admin account, use the backend API or database directly:

```sql
-- Example SQL to create admin user (adjust as needed)
INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('admin', 'admin@example.com', '$2a$10$encrypted_password_here', 'ADMIN', true, true, true, true, NOW(), NOW());
```

### 3. File Upload Requirements
- **Max Size**: 500MB
- **Formats**: JAR, EXE, MSI, DMG, DEB, RPM
- **Version Format**: Semantic versioning (e.g., 1.0.0)

## File Structure

```
/admin/
├── index.html          # Main admin interface
├── login.html          # Login page
└── README.md          # This file

/css/
└── admin-styles.css   # Styles for admin interface

/js/
├── admin-auth.js      # Authentication management
├── admin-api.js       # API communication
├── admin-ui.js        # UI interactions
└── admin-main.js      # Main application logic
```

## Features

### ✅ Implemented
- [x] JWT Authentication with admin role verification
- [x] File upload with drag & drop support
- [x] Version management (view, toggle status, delete)
- [x] Real-time upload progress tracking
- [x] Responsive design for all devices
- [x] Filtering and search functionality
- [x] Pagination for large datasets
- [x] Detailed version information modals
- [x] Form validation and error handling
- [x] Auto token refresh and session management
- [x] **RTL (Right-to-Left) Language Support**
  - Full RTL layout support for Arabic, Hebrew, and other RTL languages
  - Window control buttons positioned on the left for RTL mode
  - Toggle button for switching between RTL/LTR modes
  - Automatic language detection based on browser settings
  - Persistent direction preference storage
  - Keyboard shortcut (Ctrl+Shift+R) for quick direction toggle

### 🚧 Future Enhancements
- [ ] Version editing functionality
- [ ] Bulk operations
- [ ] Advanced analytics dashboard
- [ ] Email notifications
- [ ] Version comparison tools

## Security Features

- **JWT Token Authentication**: Secure login with automatic token refresh
- **Role-based Access**: Only ADMIN users can access the interface
- **Input Validation**: Client and server-side validation
- **CSRF Protection**: Built-in protection against cross-site attacks
- **Secure File Upload**: File type and size validation

## RTL Language Support

The admin interface includes comprehensive RTL (Right-to-Left) language support for Arabic, Hebrew, Persian, Urdu, and other RTL languages.

### Features
- **Automatic Detection**: Automatically detects RTL languages from browser settings
- **Manual Toggle**: Click the language button (🌐) in the top-right corner to switch modes
- **Keyboard Shortcut**: Press `Ctrl+Shift+R` to quickly toggle between RTL/LTR
- **Persistent Settings**: Your direction preference is saved and restored on next visit
- **Complete Layout Support**: All UI elements properly positioned for RTL reading

### Window Controls
In RTL mode:
- Modal close buttons (×) appear on the left side
- Navigation elements are reversed
- Form layouts adapt to RTL reading patterns
- Action buttons follow RTL conventions

### Supported Languages
- Arabic (ar)
- Hebrew (he)
- Persian/Farsi (fa)
- Urdu (ur)
- Kurdish (ku)
- Dhivehi (dv)

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## Troubleshooting

### Common Issues

1. **Cannot login**: Verify admin account exists with ADMIN role
2. **File upload fails**: Check file size and format requirements
3. **Interface not loading**: Ensure backend is running and accessible
4. **Token expired**: Login again or wait for automatic refresh

### Debug Mode

Open browser developer tools (F12) to see console logs and network requests for debugging.

## API Endpoints Used

- `POST /api/auth/login` - Authentication
- `GET /api/v1/admin/updates/versions` - List versions
- `POST /api/v1/admin/updates/versions` - Upload version
- `GET /api/v1/admin/updates/versions/{id}` - Version details
- `PATCH /api/v1/admin/updates/versions/{id}/toggle-status` - Toggle status
- `DELETE /api/v1/admin/updates/versions/{id}` - Delete version

## Configuration

The interface automatically detects the backend URL from the current domain. No additional configuration is required for basic usage.

For custom configurations, modify the JavaScript files in `/js/` directory.

## Support

For issues or questions, refer to the main project documentation or contact the development team.
