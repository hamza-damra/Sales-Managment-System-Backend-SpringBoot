# Enhanced Update System

## Overview

The Enhanced Update System is a comprehensive Spring Boot backend solution for distributing versioned JAR files to Kotlin Compose desktop applications. It extends the existing update infrastructure with advanced features while maintaining full backward compatibility.

## 🚀 Key Features

### Core Enhancements
- **JAR-Specific Updates**: Optimized for JAR file distribution with metadata-only endpoints
- **Resumable Downloads**: HTTP Range header support for interrupted downloads
- **Compatibility Validation**: Pre-update system compatibility checks
- **Version Rollback**: Management and distribution of previous stable versions
- **Differential Updates**: Delta updates for efficient bandwidth usage (up to 90% reduction)
- **Real-Time Notifications**: WebSocket-based update notifications
- **Rate Limiting**: Per-client request throttling with exponential backoff
- **Release Channels**: Support for stable, beta, nightly, LTS, and hotfix channels
- **Analytics**: Comprehensive update analytics and reporting

### Security & Performance
- JWT-based authentication for all endpoints
- Configurable rate limiting per endpoint type
- SHA-256 file integrity verification
- CORS support for web clients
- Async processing for better throughput
- Connection pooling for database efficiency

## 📋 Prerequisites

- Java 11 or higher
- Spring Boot 3.x
- MySQL 8.0 or higher
- Maven 3.6+

## 🛠️ Installation

### 1. Database Setup

Run the migration script to add the enhanced update system tables:

```sql
-- The migration script V1_7__Enhanced_Update_System.sql will be automatically
-- executed by Flyway when you start the application
```

### 2. Configuration

Add the following properties to your `application.properties`:

```properties
# Enhanced Update System Configuration
app.updates.storage-path=./versions
app.updates.max-file-size=524288000
app.updates.allowed-extensions=jar,exe,msi,dmg,deb,rpm
app.updates.enable-resumable-downloads=true

# WebSocket Configuration
app.updates.websocket.heartbeat-interval=30000
app.updates.websocket.connection-timeout=300000

# Rate Limiting (requests per minute)
app.updates.rate-limit.update-check=20
app.updates.rate-limit.download=5
app.updates.rate-limit.metadata=30
app.updates.rate-limit.compatibility=10
app.updates.rate-limit.analytics=15
app.updates.rate-limit.rollback=3
app.updates.rate-limit.delta=5
app.updates.rate-limit.websocket=10

# Differential Updates
app.updates.delta.max-size-mb=100
app.updates.delta.compression-threshold=0.3

# Security
app.updates.security.rate-limit=10
```

### 3. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

## 📚 API Documentation

### New Endpoints

#### Get Version Metadata
```http
GET /api/v1/updates/metadata/{version}
Authorization: Bearer {jwt-token}
```

Returns JAR file metadata without initiating download.

#### Check System Compatibility
```http
GET /api/v1/updates/compatibility/{version}?clientVersion={version}&java.version={version}&os.name={os}
Authorization: Bearer {jwt-token}
```

Validates client system compatibility before update.

#### Get Differential Update
```http
GET /api/v1/updates/delta/{fromVersion}/{toVersion}
Authorization: Bearer {jwt-token}
```

Returns differential update information between two versions.

#### Download Differential Update
```http
GET /api/v1/updates/delta/{fromVersion}/{toVersion}/download
Authorization: Bearer {jwt-token}
Range: bytes=0-1023 (optional, for resumable downloads)
```

Downloads the differential update file.

#### Get Release Channels
```http
GET /api/v1/updates/channels
Authorization: Bearer {jwt-token}
```

Returns available release channels (stable, beta, nightly, lts, hotfix).

#### Get Latest Version for Channel
```http
GET /api/v1/updates/channels/{channel}/latest
Authorization: Bearer {jwt-token}
```

Returns the latest version for a specific release channel.

### WebSocket Real-Time Notifications

Connect to WebSocket endpoint for real-time update notifications:

```
ws://localhost:8081/ws/updates?token={jwt-token}
```

#### Message Types
- `NEW_VERSION_AVAILABLE`: New version released
- `DOWNLOAD_PROGRESS`: Download progress updates
- `INSTALLATION_PROGRESS`: Installation progress updates
- `RATE_LIMITED`: Rate limit notifications
- `COMPATIBILITY_ISSUE`: Compatibility warnings

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest=*IntegrationTest
```

### Test Coverage
```bash
mvn jacoco:report
```

## 🔧 Configuration Options

### Rate Limiting
Configure different rate limits for each endpoint type:

```properties
# Requests per minute per client
app.updates.rate-limit.update-check=20    # Version checks
app.updates.rate-limit.download=5         # File downloads
app.updates.rate-limit.metadata=30        # Metadata requests
app.updates.rate-limit.compatibility=10   # Compatibility checks
app.updates.rate-limit.delta=5            # Differential updates
```

### WebSocket Settings
```properties
app.updates.websocket.heartbeat-interval=30000    # 30 seconds
app.updates.websocket.connection-timeout=300000   # 5 minutes
```

### Differential Updates
```properties
app.updates.delta.max-size-mb=100                 # Max delta file size
app.updates.delta.compression-threshold=0.3       # Min compression ratio
```

## 📊 Monitoring & Analytics

### Database Views

The system creates several views for monitoring:

- `update_statistics_view`: Download and installation statistics
- `rate_limit_statistics_view`: Rate limiting statistics

### Analytics Endpoints

Access analytics through the existing statistics endpoints, enhanced with:

- Download success/failure rates by version and channel
- Geographic distribution of downloads
- Client version adoption metrics
- Rate limiting statistics
- WebSocket connection metrics
- Differential update usage statistics

### Cleanup Procedures

Use the provided stored procedure to clean up old records:

```sql
CALL CleanupOldUpdateRecords(90); -- Keep 90 days of data
```

## 🔒 Security Considerations

1. **Authentication**: All endpoints require valid JWT tokens
2. **Rate Limiting**: Prevents abuse with configurable limits
3. **Input Validation**: All parameters are validated and sanitized
4. **File Integrity**: SHA-256 checksums for all downloads
5. **WebSocket Security**: Token-based authentication
6. **CORS**: Configurable allowed origins

## 🚀 Performance Optimizations

1. **Differential Updates**: Reduce bandwidth usage by up to 90%
2. **Resumable Downloads**: Handle network interruptions gracefully
3. **Caching**: Metadata and compatibility checks are cached
4. **Async Processing**: Non-blocking operations
5. **Connection Pooling**: Efficient database connections

## 🐛 Troubleshooting

### Common Issues

#### Rate Limiting Errors (429)
- Check rate limit configuration
- Verify client identifier uniqueness
- Review rate limiting logs

#### WebSocket Connection Issues
- Verify JWT token validity
- Check CORS configuration
- Review WebSocket logs

#### Differential Update Failures
- Ensure source and target versions exist
- Check delta file generation logs
- Verify compression threshold settings

### Logging

Enable debug logging for troubleshooting:

```properties
logging.level.com.hamza.salesmanagementbackend.service.UpdateCompatibilityService=DEBUG
logging.level.com.hamza.salesmanagementbackend.service.RateLimitingService=DEBUG
logging.level.com.hamza.salesmanagementbackend.service.DifferentialUpdateService=DEBUG
logging.level.com.hamza.salesmanagementbackend.websocket=DEBUG
```

## 📈 Migration from Basic Update System

The enhanced system is fully backward compatible. Existing clients will continue to work without changes. To take advantage of new features:

1. Update client applications to use new endpoints
2. Implement WebSocket connection for real-time notifications
3. Add compatibility checking before updates
4. Use differential updates for bandwidth optimization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add comprehensive tests
4. Update documentation
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:

1. Check the troubleshooting section
2. Review the API documentation
3. Check existing issues in the repository
4. Create a new issue with detailed information

## 🔄 Version History

### v2.1.0 (Enhanced Update System)
- Added JAR-specific update endpoints
- Implemented differential updates
- Added real-time WebSocket notifications
- Introduced rate limiting
- Added compatibility validation
- Implemented release channels
- Enhanced analytics and monitoring

### v2.0.0 (Basic Update System)
- Initial update system implementation
- Basic file upload/download
- Version management
- Admin interface

## 🎯 Roadmap

### Upcoming Features
- Automatic rollback on failed updates
- A/B testing for updates
- Update scheduling
- Client-side update verification
- Enhanced security features
- Performance monitoring dashboard

### Long-term Goals
- Multi-tenant support
- Cloud storage integration
- Advanced analytics dashboard
- Machine learning for update optimization
