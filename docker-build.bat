@echo off
REM Docker Build Script for Sales Management System Backend (Windows)
REM This script builds the Docker image and optionally runs the complete stack

setlocal enabledelayedexpansion

REM Function to print status messages
:print_status
echo [INFO] %~1
goto :eof

:print_success
echo [SUCCESS] %~1
goto :eof

:print_error
echo [ERROR] %~1
goto :eof

REM Function to check if Docker is running
:check_docker
docker info >nul 2>&1
if errorlevel 1 (
    call :print_error "Docker is not running. Please start Docker and try again."
    exit /b 1
)
call :print_success "Docker is running"
goto :eof

REM Function to build the application
:build_app
call :print_status "Building Spring Boot application..."

if exist "mvnw.cmd" (
    call mvnw.cmd clean package -DskipTests
) else (
    where mvn >nul 2>&1
    if errorlevel 1 (
        call :print_error "Neither Maven wrapper nor Maven is available. Please install Maven."
        exit /b 1
    )
    mvn clean package -DskipTests
)

if errorlevel 1 (
    call :print_error "Failed to build application"
    exit /b 1
)

call :print_success "Application built successfully"
goto :eof

REM Function to build Docker image
:build_docker_image
call :print_status "Building Docker image..."

docker build -t sales-management-backend:latest .
if errorlevel 1 (
    call :print_error "Failed to build Docker image"
    exit /b 1
)

call :print_success "Docker image built successfully"
goto :eof

REM Function to create necessary directories
:create_directories
call :print_status "Creating necessary directories..."

if not exist "logs" mkdir logs
if not exist "docker" mkdir docker
if not exist "docker\mysql" mkdir docker\mysql
if not exist "docker\mysql\init" mkdir docker\mysql\init

call :print_success "Directories created"
goto :eof

REM Function to start the complete stack
:start_stack
call :print_status "Starting the complete Docker stack..."

docker-compose up -d
if errorlevel 1 (
    call :print_error "Failed to start Docker stack"
    exit /b 1
)

call :print_success "Docker stack started successfully"
call :print_status "Services are starting up..."
call :print_status "Backend will be available at: http://abusaker.zapto.org:8081"
call :print_status "MySQL will be available at: abusaker.zapto.org:3307"
call :print_status "To include phpMyAdmin, run: docker-compose --profile tools up -d"
goto :eof

REM Function to show logs
:show_logs
call :print_status "Showing logs for all services..."
docker-compose logs -f
goto :eof

REM Function to stop the stack
:stop_stack
call :print_status "Stopping Docker stack..."
docker-compose down
call :print_success "Docker stack stopped"
goto :eof

REM Function to clean up
:cleanup
call :print_status "Cleaning up Docker resources..."
docker-compose down -v --remove-orphans
docker image prune -f
call :print_success "Cleanup completed"
goto :eof

REM Function to show help
:show_help
echo Usage: %~nx0 [OPTION]
echo.
echo Options:
echo   build     Build the application and Docker image
echo   start     Start the complete Docker stack
echo   stop      Stop the Docker stack
echo   restart   Restart the Docker stack
echo   logs      Show logs from all services
echo   clean     Clean up Docker resources
echo   all       Build and start everything (default)
echo   help      Show this help message
echo.
echo Examples:
echo   %~nx0 build     # Just build the image
echo   %~nx0 start     # Start the stack
echo   %~nx0 all       # Build and start everything
goto :eof

REM Main script logic
set action=%1
if "%action%"=="" set action=all

if "%action%"=="build" (
    call :check_docker
    call :create_directories
    call :build_app
    call :build_docker_image
) else if "%action%"=="start" (
    call :check_docker
    call :create_directories
    call :start_stack
) else if "%action%"=="stop" (
    call :stop_stack
) else if "%action%"=="restart" (
    call :stop_stack
    timeout /t 2 /nobreak >nul
    call :start_stack
) else if "%action%"=="logs" (
    call :show_logs
) else if "%action%"=="clean" (
    call :cleanup
) else if "%action%"=="all" (
    call :check_docker
    call :create_directories
    call :build_app
    call :build_docker_image
    call :start_stack
) else if "%action%"=="help" (
    call :show_help
) else if "%action%"=="-h" (
    call :show_help
) else if "%action%"=="--help" (
    call :show_help
) else (
    call :print_error "Unknown option: %action%"
    call :show_help
    exit /b 1
)

endlocal
