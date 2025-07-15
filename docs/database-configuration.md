# Database Configuration Guide

## Overview

The Sales Management System Backend now supports multiple database configurations through Spring profiles and centralized constants in `ApplicationConstants.java`.

## Database Configurations

### 1. Aiven MySQL Database (Production)

**Profile**: `aiven`  
**Configuration File**: `application-aiven.properties`

#### Database Details
- **Host**: `${DB_HOST}` (configured via environment variables)
- **Port**: `${DB_PORT}` (configured via environment variables)
- **Database**: `${DB_NAME}` (configured via environment variables)
- **Username**: `${DB_USERNAME}` (configured via environment variables)
- **Password**: `${DB_PASSWORD}` (configured via environment variables)
- **SSL**: Required

#### Connection URLs
- **Primary**: `jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?ssl-mode=REQUIRED`
- **Replica**: `jdbc:mysql://replica-${DB_HOST}:${DB_PORT}/${DB_NAME}?ssl-mode=REQUIRED`

### 2. Render.com Database (Cloud Deployment)

**Profile**: `render`  
**Configuration File**: `application-render.properties`

Uses environment variables for database configuration, typically provided by Render.com.

### 3. Local Development Database

**Profile**: `default`  
**Configuration File**: `application.properties`

Supports both local MySQL and H2 in-memory database for development.

## ApplicationConstants.java

All database configuration constants are centralized in:
```
src/main/java/com/hamza/salesmanagementbackend/config/ApplicationConstants.java
```

### Database Constants

```java
// Database Configuration (values loaded from environment variables)
public static final String DB_HOST = System.getenv("DB_HOST");
public static final String DB_PORT = System.getenv("DB_PORT");
public static final String DB_NAME = System.getenv("DB_NAME");
public static final String DB_USERNAME = System.getenv("DB_USERNAME");
public static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?ssl-mode=REQUIRED";
```

## Usage

### Running with Aiven Database

```bash
# Using Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=aiven

# Using JAR
java -jar -Dspring.profiles.active=aiven target/sales-management-backend-*.jar
```

### Docker Deployment

The Dockerfile is configured to use the `aiven` profile by default:

```dockerfile
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-aiven}
```

### Environment Variables (Optional)

For additional security, you can override database credentials using environment variables:

```bash
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export DATABASE_URL=your_database_url
```

## Security Considerations

1. **SSL/TLS**: All connections to Aiven MySQL use SSL with `ssl-mode=REQUIRED`
2. **Connection Pooling**: Configured with HikariCP for optimal performance
3. **Environment Variables**: Sensitive data can be externalized using environment variables
4. **Production Settings**: Error details are hidden in production profiles

## Migration and Flyway

Database migrations are managed by Flyway and located in:
```
src/main/resources/db/migration/
```

Flyway is enabled by default in the Aiven profile and will automatically apply migrations on startup.

## Troubleshooting

### Connection Issues
1. Verify SSL configuration
2. Check firewall settings
3. Ensure correct credentials
4. Validate connection pool settings

### Performance Issues
1. Monitor connection pool metrics
2. Adjust pool size based on load
3. Review query performance
4. Check network latency

## Health Checks

The application includes health check endpoints:
- `/actuator/health` - Overall application health
- `/actuator/health/db` - Database connectivity status

## Monitoring

Database connection metrics are available through:
- HikariCP metrics
- Spring Boot Actuator
- Application logs
