#!/bin/bash

# Docker Build Script for Sales Management System Backend
# This script builds the Docker image and optionally runs the complete stack

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to build the application
build_app() {
    print_status "Building Spring Boot application..."
    
    # Check if Maven wrapper exists
    if [ -f "./mvnw" ]; then
        chmod +x ./mvnw
        ./mvnw clean package -DskipTests
    elif command -v mvn &> /dev/null; then
        mvn clean package -DskipTests
    else
        print_error "Neither Maven wrapper nor Maven is available. Please install Maven."
        exit 1
    fi
    
    print_success "Application built successfully"
}

# Function to build Docker image
build_docker_image() {
    print_status "Building Docker image..."
    
    # Build the Docker image
    docker build -t sales-management-backend:latest .
    
    print_success "Docker image built successfully"
}

# Function to create necessary directories
create_directories() {
    print_status "Creating necessary directories..."
    
    mkdir -p logs
    mkdir -p docker/mysql/init
    
    print_success "Directories created"
}

# Function to start the complete stack
start_stack() {
    print_status "Starting the complete Docker stack..."
    
    # Start the services
    docker-compose up -d
    
    print_success "Docker stack started successfully"
    print_status "Services are starting up..."
    print_status "Backend will be available at: http://abusaker.zapto.org:8081"
    print_status "MySQL will be available at: abusaker.zapto.org:3307"
    print_status "To include phpMyAdmin, run: docker-compose --profile tools up -d"
}

# Function to show logs
show_logs() {
    print_status "Showing logs for all services..."
    docker-compose logs -f
}

# Function to stop the stack
stop_stack() {
    print_status "Stopping Docker stack..."
    docker-compose down
    print_success "Docker stack stopped"
}

# Function to clean up
cleanup() {
    print_status "Cleaning up Docker resources..."
    docker-compose down -v --remove-orphans
    docker image prune -f
    print_success "Cleanup completed"
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  build     Build the application and Docker image"
    echo "  start     Start the complete Docker stack"
    echo "  stop      Stop the Docker stack"
    echo "  restart   Restart the Docker stack"
    echo "  logs      Show logs from all services"
    echo "  clean     Clean up Docker resources"
    echo "  all       Build and start everything (default)"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build     # Just build the image"
    echo "  $0 start     # Start the stack"
    echo "  $0 all       # Build and start everything"
}

# Main script logic
main() {
    local action=${1:-all}
    
    case $action in
        "build")
            check_docker
            create_directories
            build_app
            build_docker_image
            ;;
        "start")
            check_docker
            create_directories
            start_stack
            ;;
        "stop")
            stop_stack
            ;;
        "restart")
            stop_stack
            sleep 2
            start_stack
            ;;
        "logs")
            show_logs
            ;;
        "clean")
            cleanup
            ;;
        "all")
            check_docker
            create_directories
            build_app
            build_docker_image
            start_stack
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown option: $action"
            show_help
            exit 1
            ;;
    esac
}

# Run the main function
main "$@"
