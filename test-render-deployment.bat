@echo off
setlocal enabledelayedexpansion

REM Test Render.com Deployment Configuration Locally
REM This script helps test the Render.com configuration before actual deployment

echo 🚀 Testing Render.com Deployment Configuration Locally
echo =======================================================

REM Check if required files exist
echo [INFO] Checking required files...

if not exist "Dockerfile" (
    echo [ERROR] ✗ Dockerfile is missing
    exit /b 1
)
echo [SUCCESS] ✓ Dockerfile exists

if not exist "docker-compose.render.yml" (
    echo [ERROR] ✗ docker-compose.render.yml is missing
    exit /b 1
)
echo [SUCCESS] ✓ docker-compose.render.yml exists

if not exist "src\main\resources\application-render.properties" (
    echo [ERROR] ✗ application-render.properties is missing
    exit /b 1
)
echo [SUCCESS] ✓ application-render.properties exists

if not exist ".env.render.template" (
    echo [ERROR] ✗ .env.render.template is missing
    exit /b 1
)
echo [SUCCESS] ✓ .env.render.template exists

REM Check if .env.render exists
if not exist ".env.render" (
    echo [WARNING] ⚠ .env.render file not found
    echo [INFO] Creating .env.render from template...
    copy ".env.render.template" ".env.render" >nul
    echo [WARNING] Please edit .env.render with your actual values before continuing
    echo.
    echo Required variables to set in .env.render:
    echo - DATABASE_URL (or individual DB_* variables)
    echo - JWT_SECRET (minimum 256 bits)
    echo.
    pause
)

REM Check Docker
echo [INFO] Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker is not installed or not in PATH
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker daemon is not running
    exit /b 1
)

echo [SUCCESS] Docker is available

REM Check Docker Compose
echo [INFO] Checking Docker Compose...
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose is not installed or not in PATH
    exit /b 1
)

echo [SUCCESS] Docker Compose is available

REM Build the application
echo [INFO] Building the application...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Maven build failed
    exit /b 1
)

echo [SUCCESS] Application built successfully

REM Start the services
echo [INFO] Starting services with Render.com configuration...
docker-compose -f docker-compose.render.yml --env-file .env.render up -d

REM Wait for services to be healthy
echo [INFO] Waiting for services to be healthy...
timeout /t 30 /nobreak >nul

REM Check application health
echo [INFO] Checking application health...
set /a attempt=1
set /a max_attempts=30

:health_check_loop
curl -f "http://localhost:8081/actuator/health" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] Application is healthy
    goto :health_check_done
)

if !attempt! geq !max_attempts! (
    echo [ERROR] Application health check failed after !max_attempts! attempts
    docker-compose -f docker-compose.render.yml logs backend
    exit /b 1
)

echo [INFO] Waiting for application... (attempt !attempt!/!max_attempts!)
timeout /t 5 /nobreak >nul
set /a attempt+=1
goto :health_check_loop

:health_check_done

REM Test endpoints
echo [INFO] Testing endpoints...

REM Test health endpoint
curl -f "http://localhost:8081/actuator/health" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] ✓ Health endpoint is working
) else (
    echo [ERROR] ✗ Health endpoint failed
)

REM Test auth test endpoint
curl -f "http://localhost:8081/api/auth/test" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] ✓ Auth test endpoint is working
) else (
    echo [WARNING] ⚠ Auth test endpoint failed (this might be expected if authentication is required)
)

REM Show service status
echo [INFO] Service status:
docker-compose -f docker-compose.render.yml ps

REM Show application logs (last 20 lines)
echo [INFO] Recent application logs:
docker-compose -f docker-compose.render.yml logs --tail=20 backend

echo.
echo [SUCCESS] 🎉 Render.com configuration test completed successfully!
echo.
echo Your application is running at: http://localhost:8081
echo Health endpoint: http://localhost:8081/actuator/health
echo Admin interface: http://localhost:8081/admin/
echo.
echo To stop the services, run:
echo docker-compose -f docker-compose.render.yml down
echo.
echo To view logs, run:
echo docker-compose -f docker-compose.render.yml logs -f backend
echo.
echo [INFO] Ready for Render.com deployment! 🚀

pause
