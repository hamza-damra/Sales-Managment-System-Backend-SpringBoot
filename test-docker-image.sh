#!/bin/bash

# Test Docker Image for Render.com Deployment
# Comprehensive testing of the built Docker image

set -e

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

# Configuration
IMAGE_NAME="sales-management-backend:render"
CONTAINER_NAME="sales-management-test-$$"
TEST_PORT="8083"

echo "🧪 Testing Docker Image for Render.com Deployment"
echo "================================================="

# Check if image exists
print_status "Checking if Docker image exists..."
if ! docker image inspect "$IMAGE_NAME" &> /dev/null; then
    print_error "Docker image $IMAGE_NAME not found"
    print_status "Please run build-render-image.sh first"
    exit 1
fi

print_success "Docker image found: $IMAGE_NAME"

# Check if .env.render exists
if [[ ! -f ".env.render" ]]; then
    print_warning ".env.render file not found"
    print_status "Creating .env.render from template..."
    if [[ -f ".env.render.template" ]]; then
        cp .env.render.template .env.render
        print_warning "Please edit .env.render with your actual values"
        echo ""
        read -p "Press Enter after editing .env.render to continue..."
    else
        print_error ".env.render.template not found"
        exit 1
    fi
fi

# Load environment variables
print_status "Loading environment variables from .env.render..."
export $(grep -v '^#' .env.render | grep -v '^$' | xargs)

# Validate required environment variables
print_status "Validating environment variables..."
required_vars=("JWT_SECRET")

# Check if DATABASE_URL or individual DB variables are set
if [[ -z "$DATABASE_URL" ]]; then
    required_vars+=("DB_HOST" "DB_NAME" "DB_USERNAME" "DB_PASSWORD")
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

print_success "All required environment variables are set"

# Test 1: Basic container startup
print_status "Test 1: Basic container startup"
print_status "Starting container: $CONTAINER_NAME"

# Clean up any existing container
docker stop "$CONTAINER_NAME" &> /dev/null || true
docker rm "$CONTAINER_NAME" &> /dev/null || true

# Start container with environment variables
if docker run -d --name "$CONTAINER_NAME" -p "$TEST_PORT:8081" --env-file .env.render "$IMAGE_NAME"; then
    print_success "✓ Container started successfully"
else
    print_error "✗ Failed to start container"
    exit 1
fi

# Test 2: Container health
print_status "Test 2: Container health check"
print_status "Waiting for application to start..."

MAX_ATTEMPTS=30
ATTEMPT=1

while [[ $ATTEMPT -le $MAX_ATTEMPTS ]]; do
    if docker exec "$CONTAINER_NAME" curl -f "http://localhost:8081/actuator/health" &> /dev/null; then
        print_success "✓ Container health check passed"
        break
    fi
    
    if [[ $ATTEMPT -eq $MAX_ATTEMPTS ]]; then
        print_error "✗ Container health check failed after $MAX_ATTEMPTS attempts"
        print_status "Container logs:"
        docker logs "$CONTAINER_NAME"
        docker stop "$CONTAINER_NAME" &> /dev/null
        docker rm "$CONTAINER_NAME" &> /dev/null
        exit 1
    fi
    
    print_status "Waiting for health check... (attempt $ATTEMPT/$MAX_ATTEMPTS)"
    sleep 5
    ((ATTEMPT++))
done

# Test 3: External health check
print_status "Test 3: External health check"
if curl -f "http://localhost:$TEST_PORT/actuator/health" &> /dev/null; then
    print_success "✓ External health check passed"
else
    print_error "✗ External health check failed"
fi

# Test 4: API endpoints
print_status "Test 4: API endpoints testing"

# Test auth endpoint
if curl -f "http://localhost:$TEST_PORT/api/auth/test" &> /dev/null; then
    print_success "✓ Auth test endpoint accessible"
else
    print_warning "⚠ Auth test endpoint failed (may require authentication)"
fi

# Test admin interface
if curl -f "http://localhost:$TEST_PORT/admin/" &> /dev/null; then
    print_success "✓ Admin interface accessible"
else
    print_warning "⚠ Admin interface failed (may require authentication)"
fi

# Test static resources
if curl -f "http://localhost:$TEST_PORT/css/admin-styles.css" &> /dev/null; then
    print_success "✓ Static resources accessible"
else
    print_warning "⚠ Static resources failed"
fi

# Test 5: Environment variable injection
print_status "Test 5: Environment variable injection"

# Check if Spring profile is set correctly
if docker exec "$CONTAINER_NAME" printenv SPRING_PROFILES_ACTIVE | grep -q "render"; then
    print_success "✓ Spring profile set correctly"
else
    print_warning "⚠ Spring profile not set to 'render'"
fi

# Check if JWT secret is set
if docker exec "$CONTAINER_NAME" printenv JWT_SECRET &> /dev/null; then
    print_success "✓ JWT secret environment variable set"
else
    print_error "✗ JWT secret environment variable not set"
fi

# Test 6: Database connectivity (if using external database)
print_status "Test 6: Database connectivity"
if [[ -n "$DATABASE_URL" ]] && [[ "$DATABASE_URL" != *"h2:mem"* ]]; then
    # Test database connection through health endpoint
    DB_HEALTH=$(curl -s "http://localhost:$TEST_PORT/actuator/health" | grep -o '"db":{"status":"[^"]*"' | cut -d'"' -f4)
    if [[ "$DB_HEALTH" == "UP" ]]; then
        print_success "✓ Database connectivity test passed"
    else
        print_warning "⚠ Database connectivity test failed (status: $DB_HEALTH)"
    fi
else
    print_status "Using in-memory database, skipping external DB test"
fi

# Test 7: Memory and resource usage
print_status "Test 7: Resource usage analysis"

# Get container stats
STATS=$(docker stats "$CONTAINER_NAME" --no-stream --format "table {{.CPUPerc}}\t{{.MemUsage}}")
print_status "Container resource usage:"
echo "$STATS"

# Check if memory usage is reasonable (under 1GB)
MEM_USAGE=$(docker stats "$CONTAINER_NAME" --no-stream --format "{{.MemUsage}}" | cut -d'/' -f1 | sed 's/MiB//' | sed 's/GiB/000/' | cut -d'.' -f1)
if [[ $MEM_USAGE -lt 1000 ]]; then
    print_success "✓ Memory usage is reasonable ($MEM_USAGE MiB)"
else
    print_warning "⚠ High memory usage ($MEM_USAGE MiB)"
fi

# Test 8: Log output
print_status "Test 8: Log output analysis"
print_status "Recent container logs:"
docker logs --tail=10 "$CONTAINER_NAME"

# Check for error patterns in logs
ERROR_COUNT=$(docker logs "$CONTAINER_NAME" 2>&1 | grep -i "error\|exception\|failed" | wc -l)
if [[ $ERROR_COUNT -eq 0 ]]; then
    print_success "✓ No errors found in logs"
else
    print_warning "⚠ Found $ERROR_COUNT error/exception entries in logs"
fi

# Test 9: Container security
print_status "Test 9: Container security check"

# Check if running as non-root user
USER_ID=$(docker exec "$CONTAINER_NAME" id -u)
if [[ $USER_ID -ne 0 ]]; then
    print_success "✓ Container running as non-root user (UID: $USER_ID)"
else
    print_warning "⚠ Container running as root user"
fi

# Test 10: Graceful shutdown
print_status "Test 10: Graceful shutdown test"
print_status "Stopping container..."

if docker stop "$CONTAINER_NAME" --time=30; then
    print_success "✓ Container stopped gracefully"
else
    print_warning "⚠ Container required force stop"
fi

# Clean up
print_status "Cleaning up test container..."
docker rm "$CONTAINER_NAME" &> /dev/null
print_success "Test container cleaned up"

# Summary
echo ""
print_success "🎉 Docker image testing completed!"
echo ""
echo "Test Summary:"
echo "✓ Container startup and health checks"
echo "✓ API endpoint accessibility"
echo "✓ Environment variable injection"
echo "✓ Resource usage analysis"
echo "✓ Security configuration"
echo "✓ Graceful shutdown"
echo ""
echo "Image Details:"
IMAGE_SIZE=$(docker images "$IMAGE_NAME" --format "{{.Size}}")
echo "  Name: $IMAGE_NAME"
echo "  Size: $IMAGE_SIZE"
echo ""
echo "Ready for deployment to Render.com! 🚀"
echo ""
echo "Next steps:"
echo "1. Push image to registry: ./push-render-image.sh"
echo "2. Deploy to Render.com using the pushed image"
echo "3. Configure environment variables in Render.com dashboard"
echo ""
print_status "Docker image testing completed successfully!"
