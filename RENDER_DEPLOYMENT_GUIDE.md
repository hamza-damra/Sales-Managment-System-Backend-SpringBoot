# Render.com Deployment Guide for Sales Management System

This guide provides comprehensive instructions for deploying the Sales Management System backend to Render.com with secure environment variable configuration.

## Prerequisites

1. **Render.com Account**: Sign up at [render.com](https://render.com)
2. **MySQL Database**: Set up a MySQL database (can be on Render.com or external provider)
3. **GitHub Repository**: Your code should be in a GitHub repository
4. **Domain/SSL**: Render.com provides free SSL certificates

## Required Environment Variables

### 🔐 Database Configuration (Required)

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | Complete MySQL connection URL | `mysql://username:password@host:port/database` | ✅ |
| `DB_HOST` | Database host (alternative to DATABASE_URL) | `mysql-db.render.com` | ⚠️ |
| `DB_PORT` | Database port | `3306` | ⚠️ |
| `DB_NAME` | Database name | `sales_management` | ⚠️ |
| `DB_USERNAME` | Database username | `sales_user` | ⚠️ |
| `DB_PASSWORD` | Database password | `secure_password_123` | ⚠️ |

**Note**: Use either `DATABASE_URL` OR the individual DB_* variables, not both.

### 🔑 Security Configuration (Required)

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `JWT_SECRET` | JWT signing secret (min 256 bits) | `your-super-secure-jwt-secret-key-here` | ✅ |
| `JWT_EXPIRATION` | JWT token expiration in milliseconds | `86400000` (24 hours) | ✅ |

### 🌐 Server Configuration (Optional)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `PORT` | Server port (Render.com sets this automatically) | `8081` | ❌ |
| `SPRING_PROFILES_ACTIVE` | Spring profile to activate | `render` | ❌ |
| `CONTEXT_PATH` | Application context path | `/` | ❌ |

### 🔗 CORS Configuration (Optional)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `https://*.onrender.com` | ❌ |
| `CORS_ALLOWED_METHODS` | Allowed HTTP methods | `GET,POST,PUT,DELETE,OPTIONS` | ❌ |
| `CORS_ALLOWED_HEADERS` | Allowed headers | `*` | ❌ |

### 📊 Database Pool Configuration (Optional)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_POOL_MAX_SIZE` | Maximum connection pool size | `15` | ❌ |
| `DB_POOL_MIN_IDLE` | Minimum idle connections | `3` | ❌ |
| `DB_POOL_CONNECTION_TIMEOUT` | Connection timeout (ms) | `30000` | ❌ |
| `DB_POOL_IDLE_TIMEOUT` | Idle timeout (ms) | `300000` | ❌ |

### 📝 Logging Configuration (Optional)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `LOG_LEVEL` | Application log level | `INFO` | ❌ |
| `LOG_LEVEL_WEB` | Web layer log level | `WARN` | ❌ |
| `LOG_LEVEL_SQL` | SQL log level | `WARN` | ❌ |

### 📁 File Upload Configuration (Optional)

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MAX_FILE_SIZE` | Maximum file upload size | `500MB` | ❌ |
| `MAX_REQUEST_SIZE` | Maximum request size | `500MB` | ❌ |
| `UPDATE_STORAGE_PATH` | File storage path | `/tmp/versions` | ❌ |

## Step-by-Step Deployment Instructions

### Step 1: Prepare Your Database

#### Option A: Use Render.com MySQL (Recommended)
1. Go to Render.com Dashboard
2. Click "New" → "PostgreSQL" or use external MySQL provider
3. Note down the connection details

#### Option B: External MySQL Provider
1. Use services like PlanetScale, AWS RDS, or Google Cloud SQL
2. Ensure the database is accessible from Render.com
3. Note down the connection details

### Step 2: Create Web Service on Render.com

1. **Connect Repository**:
   - Go to Render.com Dashboard
   - Click "New" → "Web Service"
   - Connect your GitHub repository

2. **Configure Service**:
   - **Name**: `sales-management-backend`
   - **Environment**: `Docker`
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your deployment branch)
   - **Dockerfile Path**: `./Dockerfile`

3. **Set Build Command** (if needed):
   ```bash
   # Usually not needed with Dockerfile, but if required:
   docker build -t sales-management-backend .
   ```

4. **Set Start Command** (if needed):
   ```bash
   # Usually not needed with Dockerfile, but if required:
   java $JAVA_OPTS -jar app.jar
   ```

### Step 3: Configure Environment Variables

In the Render.com dashboard, go to your service → Environment tab and add:

#### Required Variables:
```bash
# Database (choose one approach)
DATABASE_URL=mysql://username:password@host:port/database

# OR individual variables:
DB_HOST=your-mysql-host.com
DB_PORT=3306
DB_NAME=sales_management
DB_USERNAME=your_username
DB_PASSWORD=your_secure_password

# Security
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits-long
JWT_EXPIRATION=86400000

# Spring Profile
SPRING_PROFILES_ACTIVE=render
```

#### Optional Variables:
```bash
# CORS (adjust for your frontend domain)
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://localhost:3000

# Logging
LOG_LEVEL=INFO

# Database Pool
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=2
```

### Step 4: Deploy

1. Click "Create Web Service"
2. Render.com will automatically build and deploy your application
3. Monitor the build logs for any issues
4. Once deployed, test the health endpoint: `https://your-app.onrender.com/actuator/health`

## Database Setup

### Initial Database Schema

Your application uses Hibernate with `ddl-auto=validate` in production. You need to create the database schema first:

#### Option 1: Use H2 locally to generate schema
1. Run application locally with H2
2. Export the schema
3. Import to your production MySQL database

#### Option 2: Use Flyway/Liquibase migrations
1. Add migration files to `src/main/resources/db/migration/`
2. Configure Flyway in your `pom.xml`

#### Option 3: Manual schema creation
Create the necessary tables manually in your MySQL database.

## Security Best Practices

### 1. Environment Variables Security
- ✅ Never commit sensitive data to Git
- ✅ Use Render.com's environment variable encryption
- ✅ Rotate JWT secrets regularly
- ✅ Use strong database passwords

### 2. Database Security
- ✅ Use SSL connections (`useSSL=true`)
- ✅ Restrict database access to Render.com IPs only
- ✅ Use dedicated database user with minimal permissions
- ✅ Enable database backups

### 3. Application Security
- ✅ Use HTTPS only (Render.com provides free SSL)
- ✅ Configure proper CORS origins
- ✅ Enable security headers
- ✅ Monitor application logs

## Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```bash
# Check these environment variables:
DATABASE_URL  # or DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
```

#### 2. Application Won't Start
```bash
# Check logs for:
- Missing environment variables
- Database connectivity
- Port conflicts (use PORT environment variable)
```

#### 3. JWT Authentication Issues
```bash
# Ensure JWT_SECRET is set and sufficiently long (256+ bits)
JWT_SECRET=your-super-secure-jwt-secret-key-minimum-256-bits-long
```

#### 4. File Upload Issues
```bash
# Check file size limits:
MAX_FILE_SIZE=500MB
MAX_REQUEST_SIZE=500MB
```

### Health Check Endpoints

- **Application Health**: `GET /actuator/health`
- **Database Health**: `GET /actuator/health/db`
- **Authentication Test**: `GET /api/auth/test`

## Monitoring and Maintenance

### 1. Application Monitoring
- Use Render.com's built-in metrics
- Monitor response times and error rates
- Set up alerts for downtime

### 2. Database Monitoring
- Monitor connection pool usage
- Track query performance
- Set up database backups

### 3. Log Management
- Use Render.com's log aggregation
- Set appropriate log levels for production
- Monitor for errors and warnings

## Cost Optimization

### 1. Resource Sizing
- Start with Render.com's smallest plan
- Monitor resource usage
- Scale up as needed

### 2. Database Optimization
- Use connection pooling
- Optimize queries
- Consider read replicas for high traffic

## Support and Resources

- **Render.com Documentation**: [docs.render.com](https://docs.render.com)
- **Spring Boot Documentation**: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- **MySQL Documentation**: [dev.mysql.com/doc](https://dev.mysql.com/doc/)

---

## Quick Deployment Checklist

- [ ] Database created and accessible
- [ ] Repository connected to Render.com
- [ ] All required environment variables set
- [ ] Database schema created
- [ ] Application deployed successfully
- [ ] Health checks passing
- [ ] Authentication working
- [ ] File uploads working (if needed)
- [ ] CORS configured for frontend
- [ ] SSL certificate active
- [ ] Monitoring set up
