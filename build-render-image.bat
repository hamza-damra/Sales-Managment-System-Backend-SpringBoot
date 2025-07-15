@echo off
setlocal enabledelayedexpansion

REM Build Docker Image for Render.com Deployment
REM This script creates an optimized Docker image for the Sales Management System

echo 🐳 Building Docker Image for Render.com Deployment
echo ==================================================

REM Configuration
set IMAGE_NAME=sales-management-backend
set IMAGE_TAG=render
set FULL_IMAGE_NAME=%IMAGE_NAME%:%IMAGE_TAG%

REM Check if Docker is available
echo [INFO] Checking Docker availability...
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

REM Check if required files exist
echo [INFO] Checking required files...

if not exist "Dockerfile" (
    echo [ERROR] ✗ Dockerfile is missing
    exit /b 1
)
echo [SUCCESS] ✓ Dockerfile exists

if not exist "pom.xml" (
    echo [ERROR] ✗ pom.xml is missing
    exit /b 1
)
echo [SUCCESS] ✓ pom.xml exists

if not exist "src\main\java" (
    echo [ERROR] ✗ src\main\java is missing
    exit /b 1
)
echo [SUCCESS] ✓ src\main\java exists

if not exist "src\main\resources" (
    echo [ERROR] ✗ src\main\resources is missing
    exit /b 1
)
echo [SUCCESS] ✓ src\main\resources exists

REM Clean previous builds
echo [INFO] Cleaning previous builds...
call mvn clean -q
if errorlevel 1 (
    echo [ERROR] Maven clean failed
    exit /b 1
)
echo [SUCCESS] Previous builds cleaned

REM Build the application
echo [INFO] Building Spring Boot application...
call mvn package -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Maven build failed
    exit /b 1
)
echo [SUCCESS] Application built successfully

REM Check if JAR file was created
if not exist "target\*.jar" (
    echo [ERROR] JAR file not found in target directory
    exit /b 1
)
echo [SUCCESS] JAR file created

REM Remove existing image if it exists
echo [INFO] Removing existing image if present...
docker image inspect %FULL_IMAGE_NAME% >nul 2>&1
if not errorlevel 1 (
    docker rmi %FULL_IMAGE_NAME% >nul 2>&1
    echo [SUCCESS] Existing image removed
)

REM Build Docker image
echo [INFO] Building Docker image: %FULL_IMAGE_NAME%
echo This may take a few minutes...

docker build -t %FULL_IMAGE_NAME% .
if errorlevel 1 (
    echo [ERROR] Docker build failed
    exit /b 1
)
echo [SUCCESS] Docker image built successfully

REM Get image information
for /f "tokens=*" %%i in ('docker images %FULL_IMAGE_NAME% --format "{{.Size}}"') do set IMAGE_SIZE=%%i
for /f "tokens=*" %%i in ('docker images %FULL_IMAGE_NAME% --format "{{.ID}}"') do set IMAGE_ID=%%i

echo [SUCCESS] Image Details:
echo   Name: %FULL_IMAGE_NAME%
echo   ID: %IMAGE_ID%
echo   Size: %IMAGE_SIZE%

REM Test the image
echo [INFO] Testing the Docker image...

REM Create a test container
set CONTAINER_NAME=sales-management-test-%RANDOM%
echo [INFO] Starting test container: %CONTAINER_NAME%

REM Start container in detached mode with test environment variables
docker run -d --name %CONTAINER_NAME% -p 8082:8081 ^
    -e SPRING_PROFILES_ACTIVE=render ^
    -e JWT_SECRET=test-jwt-secret-for-docker-build-testing-only-not-for-production-use ^
    -e DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE ^
    -e LOG_LEVEL=INFO ^
    %FULL_IMAGE_NAME%

if errorlevel 1 (
    echo [ERROR] Failed to start test container
    exit /b 1
)
echo [SUCCESS] Test container started

REM Wait for application to start
echo [INFO] Waiting for application to start...
timeout /t 30 /nobreak >nul

REM Test health endpoint
echo [INFO] Testing health endpoint...
set /a attempt=1
set /a max_attempts=10

:health_check_loop
curl -f "http://localhost:8082/actuator/health" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] Health check passed
    goto :health_check_done
)

if !attempt! geq !max_attempts! (
    echo [ERROR] Health check failed after !max_attempts! attempts
    echo [INFO] Container logs:
    docker logs %CONTAINER_NAME%
    docker stop %CONTAINER_NAME% >nul 2>&1
    docker rm %CONTAINER_NAME% >nul 2>&1
    exit /b 1
)

echo [INFO] Waiting for health check... (attempt !attempt!/!max_attempts!)
timeout /t 5 /nobreak >nul
set /a attempt+=1
goto :health_check_loop

:health_check_done

REM Clean up test container
echo [INFO] Cleaning up test container...
docker stop %CONTAINER_NAME% >nul 2>&1
docker rm %CONTAINER_NAME% >nul 2>&1
echo [SUCCESS] Test container cleaned up

REM Create additional tags
echo [INFO] Creating additional tags...
docker tag %FULL_IMAGE_NAME% %IMAGE_NAME%:latest

REM Create timestamp tag
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "timestamp=%YYYY%%MM%%DD%-%HH%%Min%%Sec%"

docker tag %FULL_IMAGE_NAME% %IMAGE_NAME%:%timestamp%
echo [SUCCESS] Additional tags created

REM Show final image list
echo [INFO] Available images:
docker images %IMAGE_NAME%

echo.
echo [SUCCESS] 🎉 Docker image for Render.com deployment created successfully!
echo.
echo Image Details:
echo   Primary Tag: %FULL_IMAGE_NAME%
echo   Latest Tag: %IMAGE_NAME%:latest
echo   Timestamped Tag: %IMAGE_NAME%:%timestamp%
echo   Size: %IMAGE_SIZE%
echo.
echo Next Steps:
echo 1. Test locally: docker run -p 8081:8081 --env-file .env.render %FULL_IMAGE_NAME%
echo 2. Push to registry: docker push %FULL_IMAGE_NAME%
echo 3. Deploy to Render.com using this image
echo.
echo For Render.com deployment:
echo - Use Docker environment
echo - Set Dockerfile path: ./Dockerfile
echo - Configure environment variables in Render.com dashboard
echo.
echo [INFO] Ready for Render.com deployment! 🚀

pause
