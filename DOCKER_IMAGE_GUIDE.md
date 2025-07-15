# Docker Image Guide for Render.com Deployment

This guide provides comprehensive instructions for building, testing, and deploying Docker images for the Sales Management System on Render.com.

## 🐳 Overview

The Sales Management System includes optimized Docker configuration for Render.com deployment with:

- ✅ **Multi-stage build** for optimized image size
- ✅ **Security hardening** with non-root user
- ✅ **Environment variable support** for all configurations
- ✅ **Health checks** for monitoring
- ✅ **Graceful shutdown** handling
- ✅ **Container optimization** for cloud deployment

## 🚀 Quick Start

### 1. Build Docker Image

```bash
# Linux/macOS
./build-render-image.sh

# Windows
build-render-image.bat
```

This will:
- Build the Spring Boot application
- Create optimized Docker image
- Test the image automatically
- Create multiple tags (render, latest, timestamped)

### 2. Test Docker Image

```bash
# Comprehensive testing
./test-docker-image.sh

# Manual testing
docker run -p 8081:8081 --env-file .env.render sales-management-backend:render
```

### 3. Push to Registry

```bash
# Interactive push script
./push-render-image.sh

# Manual push to Docker Hub
docker tag sales-management-backend:render username/sales-management-backend:render
docker push username/sales-management-backend:render
```

## 📋 Scripts Overview

### Build Scripts

| Script | Platform | Purpose |
|--------|----------|---------|
| `build-render-image.sh` | Linux/macOS | Build optimized Docker image |
| `build-render-image.bat` | Windows | Build optimized Docker image |

**Features:**
- Validates environment and dependencies
- Builds Spring Boot application with Maven
- Creates Docker image with multiple tags
- Performs basic health testing
- Provides deployment instructions

### Testing Scripts

| Script | Platform | Purpose |
|--------|----------|---------|
| `test-docker-image.sh` | Linux/macOS | Comprehensive image testing |
| `test-render-deployment.sh` | Linux/macOS | Test full Render.com config |

**Test Coverage:**
- Container startup and health checks
- API endpoint accessibility
- Environment variable injection
- Database connectivity
- Resource usage analysis
- Security configuration
- Graceful shutdown

### Push Scripts

| Script | Platform | Purpose |
|--------|----------|---------|
| `push-render-image.sh` | Linux/macOS | Push to container registry |

**Supported Registries:**
- Docker Hub (docker.io)
- GitHub Container Registry (ghcr.io)
- Custom registries

## 🏗️ Docker Image Details

### Image Specifications

| Aspect | Details |
|--------|---------|
| **Base Image** | `openjdk:17-jre-slim` |
| **Size** | ~300-400 MB (optimized) |
| **User** | Non-root (`appuser`) |
| **Port** | Configurable via `PORT` env var |
| **Health Check** | Built-in health endpoint monitoring |

### Multi-Stage Build

```dockerfile
# Stage 1: Build application
FROM openjdk:17-jdk-slim AS build
# Maven build process

# Stage 2: Runtime image
FROM openjdk:17-jre-slim
# Optimized runtime environment
```

**Benefits:**
- Smaller final image size
- No build tools in production image
- Better security posture
- Faster deployment

### Security Features

- ✅ **Non-root user** - Runs as `appuser` (UID: 1000)
- ✅ **Minimal base image** - JRE-only, no unnecessary packages
- ✅ **Signal handling** - Proper shutdown with `dumb-init`
- ✅ **Health monitoring** - Built-in health checks
- ✅ **Environment isolation** - No hardcoded secrets

## 🔧 Environment Configuration

### Required Environment Variables

```bash
# Database (choose one approach)
DATABASE_URL=mysql://username:password@host:port/database

# OR individual variables
DB_HOST=your-mysql-host.com
DB_PORT=3306
DB_NAME=sales_management
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Security
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits
```

### Optional Environment Variables

```bash
# Server configuration
PORT=8081
SPRING_PROFILES_ACTIVE=render

# JVM optimization
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0

# CORS configuration
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com

# Logging
LOG_LEVEL=INFO
```

## 🧪 Testing Strategy

### Local Testing

1. **Build Testing**
   ```bash
   ./build-render-image.sh
   ```

2. **Comprehensive Testing**
   ```bash
   ./test-docker-image.sh
   ```

3. **Manual Testing**
   ```bash
   docker run -p 8081:8081 --env-file .env.render sales-management-backend:render
   curl http://localhost:8081/actuator/health
   ```

### Test Coverage

| Test Category | Description |
|---------------|-------------|
| **Startup** | Container starts successfully |
| **Health** | Health endpoints respond correctly |
| **API** | All API endpoints accessible |
| **Environment** | Environment variables injected properly |
| **Database** | Database connectivity (if external) |
| **Resources** | Memory and CPU usage reasonable |
| **Security** | Non-root user, proper permissions |
| **Shutdown** | Graceful shutdown handling |

## 📦 Registry Deployment

### Docker Hub Deployment

```bash
# Tag for Docker Hub
docker tag sales-management-backend:render username/sales-management-backend:render

# Login and push
docker login
docker push username/sales-management-backend:render
```

### GitHub Container Registry

```bash
# Tag for GHCR
docker tag sales-management-backend:render ghcr.io/username/sales-management-backend:render

# Login and push
echo $GITHUB_TOKEN | docker login ghcr.io -u username --password-stdin
docker push ghcr.io/username/sales-management-backend:render
```

### Using Push Script

```bash
./push-render-image.sh
# Follow interactive prompts to select registry and configure push
```

## 🌐 Render.com Deployment

### Method 1: Deploy from Registry

1. **Push image to registry** (Docker Hub, GHCR, etc.)
2. **Create Web Service** on Render.com
3. **Choose "Deploy an existing image from a registry"**
4. **Enter image URL**: `username/sales-management-backend:render`
5. **Configure environment variables**
6. **Deploy**

### Method 2: Deploy from Repository

1. **Connect GitHub repository** to Render.com
2. **Create Web Service**
3. **Choose Docker environment**
4. **Set Dockerfile path**: `./Dockerfile`
5. **Configure environment variables**
6. **Deploy**

### Render.com Configuration

| Setting | Value |
|---------|-------|
| **Environment** | Docker |
| **Dockerfile Path** | `./Dockerfile` |
| **Build Command** | (leave empty) |
| **Start Command** | (leave empty) |
| **Port** | Auto-detected from `PORT` env var |

### Required Environment Variables in Render.com

```bash
DATABASE_URL=mysql://username:password@host:port/database
JWT_SECRET=your-super-secure-jwt-secret-key
SPRING_PROFILES_ACTIVE=render
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Build Failures

```bash
# Check Maven build
mvn clean package -DskipTests

# Check Docker daemon
docker info

# Check Dockerfile syntax
docker build --no-cache -t test-image .
```

#### 2. Container Startup Issues

```bash
# Check logs
docker logs container-name

# Check environment variables
docker exec container-name env

# Test health endpoint
curl http://localhost:8081/actuator/health
```

#### 3. Database Connection Issues

```bash
# Verify DATABASE_URL format
DATABASE_URL=mysql://username:password@host:port/database

# Test database connectivity
docker exec container-name curl http://localhost:8081/actuator/health/db
```

#### 4. Memory Issues

```bash
# Check container stats
docker stats container-name

# Adjust JVM settings
JAVA_OPTS=-Xmx256m -Xms128m -XX:+UseContainerSupport
```

### Debug Commands

```bash
# Interactive shell in container
docker exec -it container-name /bin/bash

# View application logs
docker logs -f container-name

# Check process list
docker exec container-name ps aux

# Check disk usage
docker exec container-name df -h
```

## 📊 Performance Optimization

### Image Size Optimization

- ✅ Multi-stage build reduces image size by ~60%
- ✅ JRE instead of JDK saves ~200MB
- ✅ Minimal base image with only required packages
- ✅ Layer caching for faster builds

### Runtime Optimization

- ✅ Container-aware JVM settings
- ✅ Optimized garbage collection
- ✅ Connection pooling for database
- ✅ Compression enabled for responses

### Resource Limits

```yaml
# Recommended Render.com plan
resources:
  memory: 512MB
  cpu: 0.5 vCPU
```

## 📚 Additional Resources

- **Render.com Documentation**: [docs.render.com](https://docs.render.com)
- **Docker Best Practices**: [docs.docker.com/develop/best-practices](https://docs.docker.com/develop/best-practices/)
- **Spring Boot Docker Guide**: [spring.io/guides/gs/spring-boot-docker](https://spring.io/guides/gs/spring-boot-docker/)

## 🎯 Next Steps

1. ✅ Build Docker image: `./build-render-image.sh`
2. ✅ Test image locally: `./test-docker-image.sh`
3. ✅ Push to registry: `./push-render-image.sh`
4. ✅ Deploy to Render.com using pushed image
5. ✅ Configure environment variables
6. ✅ Test deployed application
7. ✅ Set up monitoring and alerts

---

**Ready to deploy your Docker image to Render.com! 🚀**
