# Docker Setup for Sales Management System Backend

This document provides comprehensive instructions for running the Sales Management System Backend using Docker containers.

## 🚀 Quick Start

### Prerequisites
- Docker Desktop installed and running
- Docker Compose (included with Docker Desktop)
- At least 2GB of available RAM
- Ports 8081, 3307, and 8080 available

### One-Command Setup
```bash
# For Linux/macOS
./docker-build.sh all

# For Windows
docker-build.bat all
```

This will:
1. Build the Spring Boot application
2. Create the Docker image
3. Start MySQL and Backend services
4. Set up networking and volumes

## 📋 Detailed Setup Instructions

### 1. Build the Application
```bash
# Linux/macOS
./docker-build.sh build

# Windows
docker-build.bat build

# Or manually
mvn clean package -DskipTests
docker build -t sales-management-backend:latest .
```

### 2. Start the Services
```bash
# Start core services (MySQL + Backend)
docker-compose up -d

# Start with phpMyAdmin for database management
docker-compose --profile tools up -d
```

### 3. Verify Services
```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f

# Test backend health
curl http://abusaker.zapto.org:8081/api/auth/test
```

## 🏗️ Architecture Overview

### Services
- **Backend**: Spring Boot application (Port 8081)
- **MySQL**: Database server (Port 3307)
- **phpMyAdmin**: Database management tool (Port 8080) - Optional

### Network
- Custom bridge network: `sales-network`
- Internal service communication via service names
- External access via mapped ports

### Volumes
- `mysql_data`: Persistent MySQL data storage
- `./logs`: Application logs (mounted from host)

## 🔧 Configuration

### Environment Variables
The following environment variables can be customized in `docker-compose.yml`:

#### Database Configuration
```yaml
DB_HOST: mysql-db
DB_PORT: 3306
DB_NAME: sales_management
DB_USERNAME: sales_user
DB_PASSWORD: sales_password
```

#### Application Configuration
```yaml
JWT_SECRET: your-jwt-secret-key
JWT_EXPIRATION: 86400000
JAVA_OPTS: "-Xmx1g -Xms512m"
```

### Custom Configuration Files
- `application-docker.properties`: Docker-specific Spring Boot configuration
- `docker/mysql/init/01-init.sql`: MySQL initialization script

## 📊 Service Details

### Backend Service
- **Image**: Built from local Dockerfile
- **Port**: 8081 (mapped to host)
- **Health Check**: `/api/auth/test` endpoint
- **Startup Time**: ~60-120 seconds
- **Dependencies**: MySQL database

### MySQL Service
- **Image**: mysql:8.0
- **Port**: 3307 (mapped to host, internal 3306)
- **Data Persistence**: Named volume `mysql_data`
- **Health Check**: mysqladmin ping
- **Startup Time**: ~30-60 seconds

### phpMyAdmin Service (Optional)
- **Image**: phpmyadmin/phpmyadmin:latest
- **Port**: 8080 (mapped to host)
- **Access**: http://abusaker.zapto.org:8080
- **Credentials**: sales_user / sales_password

## 🛠️ Management Commands

### Using Build Scripts
```bash
# Linux/macOS
./docker-build.sh [command]

# Windows
docker-build.bat [command]
```

Available commands:
- `build`: Build application and Docker image
- `start`: Start the Docker stack
- `stop`: Stop the Docker stack
- `restart`: Restart the Docker stack
- `logs`: Show logs from all services
- `clean`: Clean up Docker resources
- `all`: Build and start everything (default)
- `help`: Show help message

### Using Docker Compose Directly
```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Scale services
docker-compose up -d --scale backend=2

# Rebuild and restart
docker-compose up -d --build
```

## 🔍 Troubleshooting

### Common Issues

#### Port Conflicts
If ports are already in use, modify `docker-compose.yml`:
```yaml
ports:
  - "8082:8081"  # Change host port
  - "3308:3306"  # Change MySQL host port
```

#### Memory Issues
Increase Docker Desktop memory allocation or reduce Java heap:
```yaml
environment:
  JAVA_OPTS: "-Xmx512m -Xms256m"
```

#### Database Connection Issues
1. Check MySQL container health:
   ```bash
   docker-compose logs mysql-db
   ```

2. Verify network connectivity:
   ```bash
   docker-compose exec backend ping mysql-db
   ```

3. Check database credentials in logs

#### Slow Startup
- Increase health check timeouts in `docker-compose.yml`
- Monitor startup logs: `docker-compose logs -f backend`
- Ensure sufficient system resources

### Debugging Commands
```bash
# Enter backend container
docker-compose exec backend bash

# Enter MySQL container
docker-compose exec mysql-db mysql -u sales_user -p sales_management

# Check container resource usage
docker stats

# Inspect network
docker network inspect sales-managment-system-backend-springboot_sales-network
```

## 📈 Performance Optimization

### Production Recommendations
1. **Resource Limits**: Set memory and CPU limits in `docker-compose.yml`
2. **JVM Tuning**: Optimize `JAVA_OPTS` for your environment
3. **Database Tuning**: Adjust MySQL configuration in init script
4. **Logging**: Reduce log levels in production
5. **Health Checks**: Adjust intervals for production workloads

### Monitoring
- Application logs: `./logs/sales-management.log`
- Container metrics: `docker stats`
- Health endpoints: `http://localhost:8081/actuator/health`

## 🔒 Security Considerations

### Default Credentials (Change in Production)
- MySQL root password: `root_password`
- MySQL user: `sales_user` / `sales_password`
- JWT secret: Default value (change in production)

### Security Improvements
1. Use Docker secrets for sensitive data
2. Enable SSL/TLS for database connections
3. Use non-root user in containers (already implemented)
4. Regular security updates for base images
5. Network segmentation for production

## 📚 Additional Resources

### Useful Links
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Hub](https://hub.docker.com/_/mysql)

### API Documentation
After starting the services, the API will be available at:
- Base URL: `http://abusaker.zapto.org:8081`
- Health Check: `http://abusaker.zapto.org:8081/api/auth/test`
- API Endpoints: See existing documentation in `docs/` directory

## 🤝 Contributing

When making changes to the Docker setup:
1. Test changes locally with `docker-build.sh all`
2. Update this documentation if needed
3. Verify all services start correctly
4. Test API functionality after startup
