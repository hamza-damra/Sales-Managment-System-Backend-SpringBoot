#!/bin/bash

# Test script for Docker setup validation
# This script tests the complete Docker containerization setup

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[TEST]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

print_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Test Docker availability
test_docker() {
    print_status "Testing Docker availability..."
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        return 1
    fi
    
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running"
        return 1
    fi
    
    print_success "Docker is available and running"
}

# Test Docker Compose availability
test_docker_compose() {
    print_status "Testing Docker Compose availability..."
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        return 1
    fi
    
    print_success "Docker Compose is available"
}

# Test Maven availability
test_maven() {
    print_status "Testing Maven availability..."
    
    if [ -f "./mvnw" ]; then
        chmod +x ./mvnw
        print_success "Maven wrapper found"
        return 0
    fi
    
    if command -v mvn &> /dev/null; then
        print_success "Maven is available"
        return 0
    fi
    
    print_error "Neither Maven wrapper nor Maven is available"
    return 1
}

# Test required files
test_required_files() {
    print_status "Testing required files..."
    
    local files=(
        "Dockerfile"
        "docker-compose.yml"
        "src/main/resources/application-docker.properties"
        ".dockerignore"
        "docker/mysql/init/01-init.sql"
        "pom.xml"
    )
    
    for file in "${files[@]}"; do
        if [ ! -f "$file" ]; then
            print_error "Required file missing: $file"
            return 1
        fi
    done
    
    print_success "All required files are present"
}

# Test port availability
test_ports() {
    print_status "Testing port availability..."
    
    local ports=(8081 3307 8080)
    local unavailable_ports=()
    
    for port in "${ports[@]}"; do
        if netstat -tuln 2>/dev/null | grep -q ":$port "; then
            unavailable_ports+=($port)
        elif ss -tuln 2>/dev/null | grep -q ":$port "; then
            unavailable_ports+=($port)
        elif lsof -i :$port 2>/dev/null | grep -q LISTEN; then
            unavailable_ports+=($port)
        fi
    done
    
    if [ ${#unavailable_ports[@]} -gt 0 ]; then
        print_warning "Ports in use: ${unavailable_ports[*]}"
        print_warning "You may need to stop services or modify docker-compose.yml"
    else
        print_success "Required ports (8081, 3307, 8080) are available"
    fi
}

# Test build process
test_build() {
    print_status "Testing application build..."
    
    if [ -f "./mvnw" ]; then
        ./mvnw clean package -DskipTests -q
    else
        mvn clean package -DskipTests -q
    fi
    
    if [ ! -f "target/sales-management-backend-"*".jar" ]; then
        print_error "JAR file not created"
        return 1
    fi
    
    print_success "Application builds successfully"
}

# Test Docker image build
test_docker_build() {
    print_status "Testing Docker image build..."
    
    docker build -t sales-management-backend:test . > /dev/null 2>&1
    
    if [ $? -ne 0 ]; then
        print_error "Docker image build failed"
        return 1
    fi
    
    print_success "Docker image builds successfully"
    
    # Clean up test image
    docker rmi sales-management-backend:test > /dev/null 2>&1 || true
}

# Test Docker Compose syntax
test_compose_syntax() {
    print_status "Testing Docker Compose syntax..."
    
    docker-compose config > /dev/null 2>&1
    
    if [ $? -ne 0 ]; then
        print_error "Docker Compose syntax error"
        return 1
    fi
    
    print_success "Docker Compose syntax is valid"
}

# Test environment variables
test_environment() {
    print_status "Testing environment variable configuration..."
    
    # Check if application-docker.properties has environment variable placeholders
    if ! grep -q "\${DB_HOST:" src/main/resources/application-docker.properties; then
        print_error "Environment variables not configured in application-docker.properties"
        return 1
    fi
    
    # Check if main application.properties supports environment variables
    if ! grep -q "\${DB_HOST:" src/main/resources/application.properties; then
        print_error "Environment variables not configured in application.properties"
        return 1
    fi
    
    print_success "Environment variables are properly configured"
}

# Run all tests
run_all_tests() {
    print_status "Starting Docker setup validation..."
    echo
    
    local failed_tests=0
    
    test_docker || ((failed_tests++))
    test_docker_compose || ((failed_tests++))
    test_maven || ((failed_tests++))
    test_required_files || ((failed_tests++))
    test_ports
    test_environment || ((failed_tests++))
    test_compose_syntax || ((failed_tests++))
    test_build || ((failed_tests++))
    test_docker_build || ((failed_tests++))
    
    echo
    if [ $failed_tests -eq 0 ]; then
        print_success "All tests passed! Docker setup is ready."
        echo
        print_status "To start the application:"
        echo "  ./docker-build.sh all"
        echo "  # or"
        echo "  docker-compose up -d"
        echo
        print_status "To test the application:"
        echo "  curl http://abusaker.zapto.org:8081/api/auth/test"
    else
        print_error "$failed_tests test(s) failed. Please fix the issues before proceeding."
        return 1
    fi
}

# Main execution
if [ "$1" = "help" ] || [ "$1" = "-h" ] || [ "$1" = "--help" ]; then
    echo "Docker Setup Test Script"
    echo
    echo "Usage: $0 [test_name]"
    echo
    echo "Available tests:"
    echo "  docker      - Test Docker availability"
    echo "  compose     - Test Docker Compose"
    echo "  maven       - Test Maven availability"
    echo "  files       - Test required files"
    echo "  ports       - Test port availability"
    echo "  build       - Test application build"
    echo "  docker-build - Test Docker image build"
    echo "  syntax      - Test Docker Compose syntax"
    echo "  env         - Test environment configuration"
    echo "  all         - Run all tests (default)"
    echo
else
    case "${1:-all}" in
        "docker") test_docker ;;
        "compose") test_docker_compose ;;
        "maven") test_maven ;;
        "files") test_required_files ;;
        "ports") test_ports ;;
        "build") test_build ;;
        "docker-build") test_docker_build ;;
        "syntax") test_compose_syntax ;;
        "env") test_environment ;;
        "all") run_all_tests ;;
        *) 
            print_error "Unknown test: $1"
            echo "Run '$0 help' for available options"
            exit 1
            ;;
    esac
fi
