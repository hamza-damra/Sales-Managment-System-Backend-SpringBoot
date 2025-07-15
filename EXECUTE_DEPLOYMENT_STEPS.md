# Execute All Deployment Steps - Complete Guide

This guide will walk you through executing all the deployment steps for your Sales Management System Docker image.

## Prerequisites

Before starting, ensure you have:
- ✅ Docker Desktop installed and running
- ✅ Maven or Java development environment
- ✅ Git (for pushing to repository)
- ✅ Account on Docker Hub or GitHub Container Registry

## Step 1: Start Docker Desktop

**Windows:**
1. Open Docker Desktop from Start Menu
2. Wait for Docker to start (whale icon in system tray should be stable)
3. Verify Docker is running:
   ```cmd
   docker --version
   docker info
   ```

## Step 2: Build the Docker Image

### Option A: Using the Build Script (Recommended)

**Windows:**
```cmd
# Navigate to project directory
cd "C:\Users\Hamza Damra\IdeaProjects\Sales-Managment-System-Backend-SpringBoot"

# Run the build script
build-render-image.bat
```

**Linux/macOS:**
```bash
# Navigate to project directory
cd /path/to/Sales-Managment-System-Backend-SpringBoot

# Make script executable and run
chmod +x build-render-image.sh
./build-render-image.sh
```

### Option B: Manual Build Steps

```cmd
# 1. Clean and build the application
./mvnw clean package -DskipTests

# 2. Build Docker image
docker build -t sales-management-backend:render .

# 3. Create additional tags
docker tag sales-management-backend:render sales-management-backend:latest

# 4. Verify image was created
docker images sales-management-backend
```

**Expected Output:**
```
REPOSITORY                  TAG       IMAGE ID       CREATED         SIZE
sales-management-backend    render    abc123def456   2 minutes ago   350MB
sales-management-backend    latest    abc123def456   2 minutes ago   350MB
```

## Step 3: Test the Docker Image Locally

### Option A: Using the Test Script

**Linux/macOS:**
```bash
./test-docker-image.sh
```

### Option B: Manual Testing

```cmd
# 1. Start container with environment variables
docker run -d --name sales-management-test -p 8081:8081 --env-file .env.render sales-management-backend:render

# 2. Wait for application to start (30-45 seconds)
timeout /t 30

# 3. Test health endpoint
curl http://localhost:8081/actuator/health

# 4. Test admin interface
curl http://localhost:8081/admin/

# 5. Check container logs
docker logs sales-management-test

# 6. Stop and remove test container
docker stop sales-management-test
docker rm sales-management-test
```

**Expected Health Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

## Step 4: Push to Container Registry

### Option A: Using the Push Script

**Linux/macOS:**
```bash
./push-render-image.sh
```

### Option B: Manual Push to Docker Hub

```cmd
# 1. Login to Docker Hub
docker login

# 2. Tag image for Docker Hub (replace 'yourusername' with your Docker Hub username)
docker tag sales-management-backend:render yourusername/sales-management-backend:render
docker tag sales-management-backend:render yourusername/sales-management-backend:latest

# 3. Push images
docker push yourusername/sales-management-backend:render
docker push yourusername/sales-management-backend:latest
```

### Option C: Manual Push to GitHub Container Registry

```cmd
# 1. Login to GitHub Container Registry
echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u yourusername --password-stdin

# 2. Tag image for GHCR (replace 'yourusername' with your GitHub username)
docker tag sales-management-backend:render ghcr.io/yourusername/sales-management-backend:render
docker tag sales-management-backend:render ghcr.io/yourusername/sales-management-backend:latest

# 3. Push images
docker push ghcr.io/yourusername/sales-management-backend:render
docker push ghcr.io/yourusername/sales-management-backend:latest
```

## Step 5: Deploy to Render.com

### Method 1: Deploy from Container Registry (Recommended)

1. **Go to Render.com Dashboard**
   - Visit [render.com](https://render.com)
   - Sign in to your account

2. **Create New Web Service**
   - Click "New" → "Web Service"
   - Choose "Deploy an existing image from a registry"

3. **Configure Service**
   - **Image URL**: `yourusername/sales-management-backend:render` (or GHCR URL)
   - **Service Name**: `sales-management-backend`
   - **Region**: Choose closest to your users
   - **Instance Type**: Start with "Starter" ($7/month)

4. **Set Environment Variables**
   Click "Environment" tab and add:
   ```
   DATABASE_URL=mysql://username:password@host:port/database
   JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits-long
   SPRING_PROFILES_ACTIVE=render
   CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
   LOG_LEVEL=INFO
   ```

5. **Deploy**
   - Click "Create Web Service"
   - Monitor deployment logs
   - Wait for "Live" status

### Method 2: Deploy from GitHub Repository

1. **Connect Repository**
   - Click "New" → "Web Service"
   - Connect your GitHub repository

2. **Configure Build**
   - **Environment**: Docker
   - **Dockerfile Path**: `./Dockerfile`
   - **Build Command**: (leave empty)
   - **Start Command**: (leave empty)

3. **Set Environment Variables** (same as Method 1)

4. **Deploy**

## Step 6: Configure Environment Variables in Render.com

### Required Variables

| Variable | Value | Description |
|----------|-------|-------------|
| `DATABASE_URL` | `mysql://user:pass@host:port/db` | Database connection URL |
| `JWT_SECRET` | `your-secure-secret-key` | JWT signing secret (256+ bits) |
| `SPRING_PROFILES_ACTIVE` | `render` | Spring profile |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `CORS_ALLOWED_ORIGINS` | `https://*.onrender.com` | Allowed CORS origins |
| `LOG_LEVEL` | `INFO` | Application log level |
| `PORT` | `8081` | Server port (auto-set by Render) |

### Database Setup Options

#### Option A: Render.com PostgreSQL
1. Create PostgreSQL service on Render.com
2. Use the provided `DATABASE_URL`

#### Option B: External MySQL (PlanetScale, AWS RDS, etc.)
1. Set up MySQL database with your provider
2. Configure `DATABASE_URL` with connection details

## Step 7: Test Deployed Application

### Health Check
```bash
curl https://your-app-name.onrender.com/actuator/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Admin Interface
```bash
# Visit in browser
https://your-app-name.onrender.com/admin/
```

### API Endpoints
```bash
# Test auth endpoint
curl https://your-app-name.onrender.com/api/auth/test

# Test with authentication (after login)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" https://your-app-name.onrender.com/api/v1/updates/latest
```

## Troubleshooting

### Common Issues

#### 1. Docker Build Fails
```cmd
# Check Docker is running
docker info

# Clean build
docker system prune -f
./mvnw clean package -DskipTests
docker build --no-cache -t sales-management-backend:render .
```

#### 2. Container Won't Start
```cmd
# Check logs
docker logs container-name

# Check environment variables
docker exec container-name env | grep -E "(DB_|JWT_|SPRING_)"
```

#### 3. Health Check Fails
```cmd
# Check application logs
docker logs container-name

# Verify database connection
curl http://localhost:8081/actuator/health/db
```

#### 4. Render.com Deployment Issues
- Check deployment logs in Render.com dashboard
- Verify all required environment variables are set
- Ensure database is accessible from Render.com

### Debug Commands

```cmd
# View container logs
docker logs -f container-name

# Interactive shell in container
docker exec -it container-name /bin/bash

# Check container stats
docker stats container-name

# Test endpoints locally
curl -v http://localhost:8081/actuator/health
```

## Success Checklist

- [ ] Docker Desktop is running
- [ ] Maven build completed successfully
- [ ] Docker image built without errors
- [ ] Local container test passed
- [ ] Health endpoint responds correctly
- [ ] Image pushed to registry successfully
- [ ] Render.com service created
- [ ] Environment variables configured
- [ ] Deployment completed successfully
- [ ] Production health check passes
- [ ] Admin interface accessible
- [ ] API endpoints working

## Next Steps After Deployment

1. **Set up monitoring** - Configure alerts in Render.com
2. **Custom domain** - Add your custom domain
3. **SSL certificate** - Render.com provides free SSL
4. **Database backups** - Set up regular backups
5. **Performance monitoring** - Monitor response times and errors

---

**You're ready to deploy! 🚀**

Start with Step 1 and work through each step systematically. The entire process should take about 30-60 minutes depending on build times and network speed.
