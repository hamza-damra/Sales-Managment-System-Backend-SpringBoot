# Sales Management System - Render.com Deployment

This document provides comprehensive instructions for deploying the Sales Management System backend to Render.com with secure environment variable configuration.

## 🌟 Overview

The Sales Management System backend has been fully configured for secure deployment to Render.com with:

- ✅ **Environment Variable Based Configuration** - All sensitive data externalized
- ✅ **MySQL Database Support** - Compatible with Render.com and external MySQL providers
- ✅ **Docker Optimization** - Optimized Dockerfile for cloud deployment
- ✅ **Security Best Practices** - SSL, connection pooling, and secure defaults
- ✅ **Health Checks** - Built-in health monitoring endpoints
- ✅ **Local Testing** - Test Render.com configuration locally before deployment

## 🚀 Quick Deployment Guide

### Step 1: Prepare Environment Variables

1. Copy the environment template:
   ```bash
   cp .env.render.template .env.render
   ```

2. Edit `.env.render` with your actual values:
   ```bash
   # Required - Database Configuration
   DATABASE_URL=mysql://username:password@host:port/database
   
   # Required - Security
   JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits-long
   
   # Optional - CORS (adjust for your frontend)
   CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
   ```

### Step 2: Test Locally (Recommended)

Test the Render.com configuration locally before deployment:

```bash
# Linux/macOS
./test-render-deployment.sh

# Windows
test-render-deployment.bat
```

This will:
- Validate all environment variables
- Build and start services using Render.com configuration
- Test all endpoints
- Verify database connectivity

### Step 3: Deploy to Render.com

1. **Connect Repository** to Render.com
2. **Create Web Service** with these settings:
   - Environment: `Docker`
   - Dockerfile Path: `./Dockerfile`
   - Build Command: (leave empty - handled by Dockerfile)
   - Start Command: (leave empty - handled by Dockerfile)

3. **Set Environment Variables** in Render.com dashboard:
   ```bash
   DATABASE_URL=mysql://username:password@host:port/database
   JWT_SECRET=your-super-secure-jwt-secret-key
   SPRING_PROFILES_ACTIVE=render
   CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
   ```

4. **Deploy** and monitor the build logs

## 📋 Environment Variables Reference

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | Complete MySQL connection URL | `mysql://user:pass@host:port/db` |
| `JWT_SECRET` | JWT signing secret (256+ bits) | `your-secure-secret-key` |

### Optional Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | `render` | Spring profile |
| `CORS_ALLOWED_ORIGINS` | `https://*.onrender.com` | Allowed CORS origins |
| `LOG_LEVEL` | `INFO` | Application log level |
| `DB_POOL_MAX_SIZE` | `15` | Max database connections |

See `RENDER_DEPLOYMENT_GUIDE.md` for complete reference.

## 🗄️ Database Setup

### Option 1: Render.com PostgreSQL (Recommended)
1. Create PostgreSQL service on Render.com
2. Use the provided `DATABASE_URL`

### Option 2: External MySQL Provider
Popular options:
- **PlanetScale** - Serverless MySQL platform
- **AWS RDS** - Amazon's managed database service
- **Google Cloud SQL** - Google's managed database service
- **DigitalOcean Managed Databases** - Simple managed databases

Example connection URLs:
```bash
# PlanetScale
DATABASE_URL=mysql://username:password@aws.connect.psdb.cloud/database?sslaccept=strict

# AWS RDS
DATABASE_URL=mysql://username:password@mydb.123456789012.us-east-1.rds.amazonaws.com:3306/sales_management

# Google Cloud SQL
DATABASE_URL=mysql://username:password@34.123.45.67:3306/sales_management
```

## 🔧 Configuration Files

### New Files Added for Render.com Deployment

1. **`application-render.properties`** - Render.com optimized configuration
2. **`docker-compose.render.yml`** - Local testing with Render.com config
3. **`.env.render.template`** - Environment variables template
4. **`test-render-deployment.sh/.bat`** - Local testing scripts
5. **`RENDER_DEPLOYMENT_GUIDE.md`** - Comprehensive deployment guide

### Updated Files

1. **`application.properties`** - Enhanced with environment variables
2. **`Dockerfile`** - Optimized for Render.com deployment
3. **`application-docker.properties`** - Updated for consistency

## 🧪 Local Testing

### Test Render.com Configuration Locally

```bash
# Linux/macOS
./test-render-deployment.sh

# Windows
test-render-deployment.bat
```

### Manual Testing

```bash
# Start services
docker-compose -f docker-compose.render.yml --env-file .env.render up -d

# Check health
curl http://localhost:8081/actuator/health

# Check auth endpoint
curl http://localhost:8081/api/auth/test

# Stop services
docker-compose -f docker-compose.render.yml down
```

## 🔒 Security Features

### Environment Variable Security
- ✅ All sensitive data externalized
- ✅ No hardcoded credentials
- ✅ Render.com environment variable encryption
- ✅ Strong JWT secret validation

### Database Security
- ✅ SSL connections enabled
- ✅ Connection pooling with leak detection
- ✅ Prepared statements (SQL injection protection)
- ✅ Connection timeout and retry logic

### Application Security
- ✅ HTTPS only (Render.com provides free SSL)
- ✅ Configurable CORS origins
- ✅ Security headers
- ✅ Non-root container user

## 📊 Monitoring and Health Checks

### Health Endpoints
- **Application Health**: `GET /actuator/health`
- **Database Health**: `GET /actuator/health/db`
- **Authentication Test**: `GET /api/auth/test`

### Render.com Integration
- ✅ Built-in health checks
- ✅ Automatic SSL certificates
- ✅ Log aggregation
- ✅ Metrics and monitoring

## 🚨 Troubleshooting

### Common Issues

1. **Database Connection Failed**
   ```bash
   # Check DATABASE_URL format
   DATABASE_URL=mysql://username:password@host:port/database
   ```

2. **JWT Authentication Issues**
   ```bash
   # Ensure JWT_SECRET is long enough (256+ bits)
   JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits-long
   ```

3. **CORS Issues**
   ```bash
   # Set correct frontend domain
   CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com
   ```

### Debug Commands

```bash
# View application logs
docker-compose -f docker-compose.render.yml logs -f backend

# Check environment variables
docker-compose -f docker-compose.render.yml exec backend env | grep -E "(DB_|JWT_|CORS_)"

# Test database connection
docker-compose -f docker-compose.render.yml exec mysql-db mysql -u sales_user -p
```

## 📚 Additional Resources

- **Complete Deployment Guide**: `RENDER_DEPLOYMENT_GUIDE.md`
- **Docker Setup**: `README-Docker.md`
- **Admin Interface**: `src/main/resources/static/admin/README.md`
- **Update System Documentation**: `docs/updates/Update-System-API-Documentation.md`

## 🎯 Next Steps

1. ✅ Test locally with `test-render-deployment.sh`
2. ✅ Set up database (Render.com PostgreSQL or external MySQL)
3. ✅ Configure environment variables in Render.com
4. ✅ Deploy to Render.com
5. ✅ Test deployed application
6. ✅ Set up monitoring and alerts
7. ✅ Configure custom domain (optional)

---

## 💡 Pro Tips

- **Use Render.com PostgreSQL** for simplicity (automatically provides `DATABASE_URL`)
- **Test locally first** to catch configuration issues early
- **Use strong JWT secrets** (generate with `openssl rand -base64 32`)
- **Monitor resource usage** and scale as needed
- **Set up database backups** for production data
- **Use environment-specific CORS origins** for security

Ready to deploy? Start with the local testing script and follow the deployment guide! 🚀
