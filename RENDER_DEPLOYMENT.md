# Render.com Deployment Guide

## Environment Variables Required

For successful deployment on Render.com, you need to set the following environment variables in your Render.com service settings:

### Database Configuration

**Option 1: Using DATABASE_URL (Recommended)**
```
DATABASE_URL=mysql://username:password@host:port/database?ssl-mode=REQUIRED&useSSL=true&requireSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&maxReconnects=10
```

**Option 2: Using Individual Variables**
```
DB_HOST=your_mysql_host
DB_PORT=3306
DB_NAME=your_database_name
DB_USERNAME=your_username
DB_PASSWORD=your_password
```

### Required Environment Variables
```
# Spring Profile (automatically set by Dockerfile)
SPRING_PROFILES_ACTIVE=render

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here

# Optional: Database Pool Configuration
DB_POOL_MAX_SIZE=15
DB_POOL_MIN_IDLE=3

# Optional: Logging Level
LOG_LEVEL=INFO

# Optional: CORS Configuration
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://localhost:3000
```

## Render.com Service Configuration

### Build Settings
- **Build Command**: `./mvnw clean package -DskipTests`
- **Start Command**: `java -jar target/sales-management-backend-*.jar`

### Environment
- **Runtime**: Docker
- **Dockerfile Path**: `./Dockerfile`

### Health Check
The application provides health check endpoints:
- `/actuator/health` - Overall application health
- `/actuator/health/db` - Database connectivity

### Port Configuration
The application automatically uses Render.com's `PORT` environment variable (default: 8080).

## Database Setup

### Using Aiven MySQL (Recommended)
1. Create an Aiven MySQL service
2. Get the connection details
3. Set the `DATABASE_URL` environment variable in Render.com

### Using Render.com PostgreSQL
If you prefer PostgreSQL, you'll need to:
1. Update the database driver in `pom.xml`
2. Update the dialect in application properties
3. Modify the migration scripts

## Deployment Steps

1. **Fork/Clone the Repository**
2. **Connect to Render.com**
   - Connect your GitHub repository to Render.com
   - Choose "Web Service"
   - Select your repository

3. **Configure Environment Variables**
   - Go to your service settings
   - Add all required environment variables listed above

4. **Deploy**
   - Render.com will automatically build and deploy
   - Monitor the build logs for any issues

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify `DATABASE_URL` is correctly formatted
   - Check database credentials
   - Ensure database allows connections from Render.com IPs

2. **Build Failures**
   - Check Maven dependencies
   - Verify Java version compatibility
   - Review build logs for specific errors

3. **Application Won't Start**
   - Check environment variables are set
   - Verify Spring profile is set to `render`
   - Review application logs

### Logs
Access logs through Render.com dashboard:
- Build logs: Available during deployment
- Application logs: Available after deployment starts

## Performance Optimization

The application is configured with:
- Connection pooling optimized for cloud deployment
- Reduced memory footprint
- Efficient logging configuration
- Health checks for monitoring

## Security

- All sensitive data uses environment variables
- CORS is configured for production
- Error details are hidden in production
- SSL/TLS is enforced for database connections

## Monitoring

Use Render.com's built-in monitoring or integrate with:
- Application health endpoints
- Database connection monitoring
- Custom metrics through Spring Boot Actuator
