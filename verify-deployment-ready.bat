@echo off
setlocal enabledelayedexpansion

echo 🔍 Verifying Deployment Readiness
echo ==================================

REM Check if required files exist
echo [INFO] Checking required files...

if exist "Dockerfile" (
    echo [SUCCESS] ✓ Dockerfile exists
) else (
    echo [ERROR] ✗ Dockerfile is missing
    exit /b 1
)

if exist "pom.xml" (
    echo [SUCCESS] ✓ pom.xml exists
) else (
    echo [ERROR] ✗ pom.xml is missing
    exit /b 1
)

if exist ".env.render" (
    echo [SUCCESS] ✓ .env.render exists
) else (
    echo [WARNING] ⚠ .env.render not found, but template exists
)

if exist "build-render-image.bat" (
    echo [SUCCESS] ✓ build-render-image.bat exists
) else (
    echo [ERROR] ✗ build-render-image.bat is missing
    exit /b 1
)

REM Check if Maven wrapper exists
if exist "mvnw.cmd" (
    echo [SUCCESS] ✓ Maven wrapper exists
) else (
    echo [ERROR] ✗ Maven wrapper (mvnw.cmd) is missing
    exit /b 1
)

REM Check if target directory and JAR exist
if exist "target\sales-management-backend-0.0.1-SNAPSHOT.jar" (
    echo [SUCCESS] ✓ Application JAR exists (already built)
) else (
    echo [INFO] ℹ Application JAR not found (needs to be built)
)

REM Check Docker
echo [INFO] Checking Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ✗ Docker is not installed or not in PATH
    exit /b 1
) else (
    echo [SUCCESS] ✓ Docker is installed
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [ERROR] ✗ Docker daemon is not running
    echo [INFO] Please start Docker Desktop and try again
    exit /b 1
) else (
    echo [SUCCESS] ✓ Docker daemon is running
)

REM Check if image already exists
docker image inspect sales-management-backend:render >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] ✓ Docker image already exists
) else (
    echo [INFO] ℹ Docker image not found (needs to be built)
)

echo.
echo [SUCCESS] 🎉 System is ready for deployment!
echo.
echo Next steps:
echo 1. Run: build-render-image.bat
echo 2. Test the image locally
echo 3. Push to container registry
echo 4. Deploy to Render.com
echo.
echo Quick start command:
echo   build-render-image.bat
echo.

pause
