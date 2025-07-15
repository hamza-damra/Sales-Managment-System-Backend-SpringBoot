#!/bin/bash

# Test Render.com Deployment Configuration Locally
# This script helps test the Render.com configuration before actual deployment

set -e

echo "🚀 Testing Render.com Deployment Configuration Locally"
echo "======================================================="

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

# Check if required files exist
print_status "Checking required files..."

required_files=(
    "Dockerfile"
    "docker-compose.render.yml"
    "src/main/resources/application-render.properties"
    ".env.render.template"
)

for file in "${required_files[@]}"; do
    if [[ -f "$file" ]]; then
        print_success "✓ $file exists"
    else
        print_error "✗ $file is missing"
        exit 1
    fi
done

# Check if .env.render exists
if [[ ! -f ".env.render" ]]; then
    print_warning "⚠ .env.render file not found"
    print_status "Creating .env.render from template..."
    cp .env.render.template .env.render
    print_warning "Please edit .env.render with your actual values before continuing"
    echo ""
    echo "Required variables to set in .env.render:"
    echo "- DATABASE_URL (or individual DB_* variables)"
    echo "- JWT_SECRET (minimum 256 bits)"
    echo ""
    read -p "Press Enter after editing .env.render to continue..."
fi

# Load environment variables
if [[ -f ".env.render" ]]; then
    print_status "Loading environment variables from .env.render..."
    export $(grep -v '^#' .env.render | xargs)
    print_success "Environment variables loaded"
else
    print_error ".env.render file is required"
    exit 1
fi

# Validate required environment variables
print_status "Validating required environment variables..."

required_vars=(
    "JWT_SECRET"
)

# Check if DATABASE_URL or individual DB variables are set
if [[ -z "$DATABASE_URL" ]]; then
    required_vars+=(
        "DB_HOST"
        "DB_NAME"
        "DB_USERNAME"
        "DB_PASSWORD"
    )
    print_status "Using individual database variables"
else
    print_status "Using DATABASE_URL"
fi

missing_vars=()
for var in "${required_vars[@]}"; do
    if [[ -z "${!var}" ]]; then
        missing_vars+=("$var")
    fi
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
    print_error "Missing required environment variables:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    exit 1
fi

# Validate JWT_SECRET length
if [[ ${#JWT_SECRET} -lt 32 ]]; then
    print_error "JWT_SECRET must be at least 32 characters long (256 bits)"
    exit 1
fi

print_success "All required environment variables are set"

# Check Docker
print_status "Checking Docker..."
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_success "Docker is available"

# Check Docker Compose
print_status "Checking Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed or not in PATH"
    exit 1
fi

print_success "Docker Compose is available"

# Build the application
print_status "Building the application..."
if ! mvn clean package -DskipTests; then
    print_error "Maven build failed"
    exit 1
fi

print_success "Application built successfully"

# Start the services
print_status "Starting services with Render.com configuration..."
docker-compose -f docker-compose.render.yml --env-file .env.render up -d

# Wait for services to be healthy
print_status "Waiting for services to be healthy..."
sleep 30

# Check database health
print_status "Checking database health..."
max_attempts=30
attempt=1

while [[ $attempt -le $max_attempts ]]; do
    if docker-compose -f docker-compose.render.yml exec -T mysql-db mysqladmin ping -h 127.0.0.1 -u "${DB_USERNAME:-sales_user}" -p"${DB_PASSWORD:-sales_password}" &> /dev/null; then
        print_success "Database is healthy"
        break
    fi
    
    if [[ $attempt -eq $max_attempts ]]; then
        print_error "Database health check failed after $max_attempts attempts"
        docker-compose -f docker-compose.render.yml logs mysql-db
        exit 1
    fi
    
    print_status "Waiting for database... (attempt $attempt/$max_attempts)"
    sleep 5
    ((attempt++))
done

# Check application health
print_status "Checking application health..."
max_attempts=30
attempt=1

while [[ $attempt -le $max_attempts ]]; do
    if curl -f "http://localhost:${PORT:-8081}/actuator/health" &> /dev/null; then
        print_success "Application is healthy"
        break
    fi
    
    if [[ $attempt -eq $max_attempts ]]; then
        print_error "Application health check failed after $max_attempts attempts"
        docker-compose -f docker-compose.render.yml logs backend
        exit 1
    fi
    
    print_status "Waiting for application... (attempt $attempt/$max_attempts)"
    sleep 5
    ((attempt++))
done

# Test endpoints
print_status "Testing endpoints..."

# Test health endpoint
if curl -f "http://localhost:${PORT:-8081}/actuator/health" &> /dev/null; then
    print_success "✓ Health endpoint is working"
else
    print_error "✗ Health endpoint failed"
fi

# Test auth test endpoint
if curl -f "http://localhost:${PORT:-8081}/api/auth/test" &> /dev/null; then
    print_success "✓ Auth test endpoint is working"
else
    print_warning "⚠ Auth test endpoint failed (this might be expected if authentication is required)"
fi

# Show service status
print_status "Service status:"
docker-compose -f docker-compose.render.yml ps

# Show application logs (last 20 lines)
print_status "Recent application logs:"
docker-compose -f docker-compose.render.yml logs --tail=20 backend

echo ""
print_success "🎉 Render.com configuration test completed successfully!"
echo ""
echo "Your application is running at: http://localhost:${PORT:-8081}"
echo "Health endpoint: http://localhost:${PORT:-8081}/actuator/health"
echo "Admin interface: http://localhost:${PORT:-8081}/admin/"
echo ""
echo "To stop the services, run:"
echo "docker-compose -f docker-compose.render.yml down"
echo ""
echo "To view logs, run:"
echo "docker-compose -f docker-compose.render.yml logs -f backend"
echo ""
print_status "Ready for Render.com deployment! 🚀"
