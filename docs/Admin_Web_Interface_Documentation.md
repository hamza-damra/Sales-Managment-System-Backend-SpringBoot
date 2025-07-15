# Admin Web Interface Documentation

## Overview

The Admin Web Interface provides a comprehensive, modern web-based interface for managing application version uploads in the Sales Management System. This interface allows administrators to upload new application versions, manage existing versions, and monitor update statistics.

## Features

### 1. Authentication & Security
- **JWT-based Authentication**: Secure login using JWT tokens
- **Role-based Access Control**: Only users with ADMIN role can access the interface
- **Session Management**: Automatic token refresh and expiration handling
- **CSRF Protection**: Built-in protection against cross-site request forgery

### 2. File Upload Management
- **Drag & Drop Support**: Modern file upload with drag-and-drop functionality
- **File Validation**: Automatic validation of file types and sizes
- **Progress Tracking**: Real-time upload progress with visual feedback
- **Supported Formats**: JAR, EXE, MSI, DMG, DEB, RPM files up to 500MB

### 3. Version Management
- **Version Listing**: Paginated table view of all application versions
- **Filtering & Search**: Filter by status, type, and search across version data
- **Status Management**: Toggle version active/inactive status
- **Detailed View**: Comprehensive version details in modal dialogs

### 4. User Interface
- **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices
- **Modern Styling**: Clean, professional interface with intuitive navigation
- **Real-time Feedback**: Instant alerts and notifications for user actions
- **Accessibility**: Keyboard navigation and screen reader support

## Getting Started

### Prerequisites
- Sales Management System backend running
- Admin user account with ADMIN role
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Accessing the Interface

1. **Login Page**: Navigate to `/admin/login.html`
2. **Enter Credentials**: Use your admin username and password
3. **Main Interface**: After successful login, you'll be redirected to `/admin/index.html`

### First Time Setup

1. Ensure you have an admin user account created in the system
2. Verify the backend is running and accessible
3. Check that the update system is properly configured

## User Guide

### Uploading a New Version

1. **Navigate to Upload Section**: The upload form is prominently displayed at the top
2. **Fill Required Fields**:
   - **Version Number**: Use semantic versioning (e.g., 1.0.0)
   - **Release Notes**: Describe what's new in this version
   - **Release Date**: Set the release date and time
3. **Optional Fields**:
   - **Minimum Client Version**: Specify compatibility requirements
   - **Mandatory Update**: Check if this update is required
4. **Upload File**: 
   - Drag and drop your file onto the upload area, or
   - Click to browse and select your file
5. **Submit**: Click "Upload Version" to start the upload process
6. **Monitor Progress**: Watch the progress bar for upload status

### Managing Existing Versions

#### Viewing Versions
- The versions table shows all uploaded versions with key information
- Use pagination controls to navigate through multiple pages
- Apply filters to find specific versions quickly

#### Version Actions
- **View Details**: Click the eye icon to see comprehensive version information
- **Edit**: Click the edit icon to modify version details (future feature)
- **Toggle Status**: Click the play/pause icon to activate/deactivate versions
- **Delete**: Click the trash icon to permanently remove a version

#### Filtering and Search
- **Status Filter**: Show only active or inactive versions
- **Type Filter**: Filter by mandatory or optional updates
- **Search**: Enter text to search across version numbers, notes, and filenames

### Understanding Version Information

#### Version Table Columns
- **Version**: Version number and minimum client version (if specified)
- **Release Date**: When the version was released
- **Status**: Active (green) or Inactive (gray) badge
- **Type**: Mandatory (red) or Optional (blue) badge
- **File Size**: Formatted file size (e.g., 45.2 MB)
- **Downloads**: Total downloads and successful count
- **Created By**: Username of the admin who uploaded the version
- **Actions**: Available actions for the version

#### Version Details Modal
- Complete version information including:
  - File checksum for integrity verification
  - Download statistics and success rates
  - Creation and modification timestamps
  - Full release notes

## Technical Specifications

### File Requirements
- **Maximum Size**: 500MB per file
- **Allowed Extensions**: .jar, .exe, .msi, .dmg, .deb, .rpm
- **Validation**: Automatic file type and size validation

### Version Number Format
- **Pattern**: Semantic versioning (MAJOR.MINOR.PATCH)
- **Examples**: 1.0.0, 2.1.3, 1.0.0-beta
- **Validation**: Automatic format validation

### Browser Compatibility
- **Chrome**: Version 80+
- **Firefox**: Version 75+
- **Safari**: Version 13+
- **Edge**: Version 80+

### Security Features
- **JWT Authentication**: Secure token-based authentication
- **Role Verification**: Admin role required for access
- **HTTPS Support**: Secure communication (when configured)
- **Input Validation**: Client and server-side validation

## API Integration

The interface integrates with the following backend endpoints:

### Authentication
- `POST /api/auth/login` - User authentication
- `POST /api/auth/refresh` - Token refresh

### Version Management
- `GET /api/v1/admin/updates/versions` - List versions with pagination
- `POST /api/v1/admin/updates/versions` - Upload new version
- `GET /api/v1/admin/updates/versions/{id}` - Get version details
- `PUT /api/v1/admin/updates/versions/{id}` - Update version information
- `PATCH /api/v1/admin/updates/versions/{id}/toggle-status` - Toggle version status
- `DELETE /api/v1/admin/updates/versions/{id}` - Delete version

### Statistics
- `GET /api/v1/admin/updates/statistics` - Get update statistics
- `GET /api/v1/admin/updates/health` - Get system health metrics

## Error Handling

### Common Error Scenarios
1. **Authentication Failures**: Invalid credentials or expired tokens
2. **File Upload Errors**: File too large, invalid format, or network issues
3. **Validation Errors**: Invalid version numbers or missing required fields
4. **Server Errors**: Backend unavailable or internal server errors

### Error Display
- **Alert Messages**: Color-coded alerts for different error types
- **Form Validation**: Real-time validation with helpful error messages
- **Network Errors**: Clear messages for connectivity issues

## Troubleshooting

### Common Issues

#### Cannot Access Admin Interface
- Verify you have an admin account with ADMIN role
- Check that the backend server is running
- Ensure you're using the correct URL

#### File Upload Fails
- Check file size (must be under 500MB)
- Verify file extension is supported
- Ensure stable internet connection

#### Version Not Appearing
- Check if filters are applied that might hide the version
- Refresh the page to reload data
- Verify the upload completed successfully

### Getting Help
- Check browser console for JavaScript errors
- Review backend logs for server-side issues
- Verify network connectivity and CORS configuration

## Future Enhancements

### Planned Features
- **Bulk Operations**: Upload multiple versions at once
- **Version Comparison**: Compare different versions side by side
- **Advanced Analytics**: Detailed download and usage statistics
- **Automated Testing**: Integration with CI/CD pipelines
- **Notification System**: Email alerts for new versions

### Customization Options
- **Theming**: Custom color schemes and branding
- **Localization**: Multi-language support
- **Custom Fields**: Additional metadata fields for versions
- **Integration**: Webhooks and third-party integrations

## Support

For technical support or feature requests, please contact the development team or refer to the main project documentation.

## Version History

- **v1.0.0**: Initial release with core functionality
  - File upload with drag & drop
  - Version management and filtering
  - JWT authentication
  - Responsive design
