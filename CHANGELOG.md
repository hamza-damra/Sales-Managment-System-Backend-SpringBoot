# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1] - 2025-07-04

### Added
- Initial release of Sales Management System Backend
- JWT Authentication with access and refresh tokens
- Role-based access control (USER, ADMIN)
- Customer management CRUD operations
- Product management with inventory tracking
- Sales management with order processing
- Reporting system for sales analytics
- Global exception handling with standardized error responses
- CORS configuration for cross-origin requests
- Comprehensive input validation
- Database integration with MySQL and JPA/Hibernate
- Pagination and sorting for large datasets

### Security
- Secure password encryption using BCrypt
- JWT token-based authentication
- Protected API endpoints with role-based access
- CORS configuration for secure cross-origin requests

### Fixed
- JWT secret key configuration (Base64 encoding)
- User entity default values for account status fields
- Authentication error handling and logging
- Account lockout issues for new users
- Proper error responses instead of generic 500 errors

### Technical Details
- Spring Boot 3.2.0
- Java 17
- MySQL 8.0 database
- Maven build system
- Comprehensive test suite
- Professional project structure

### Documentation
- Complete README with setup instructions
- API documentation with endpoint details
- Project structure documentation
- Configuration guidelines
- Deployment instructions
