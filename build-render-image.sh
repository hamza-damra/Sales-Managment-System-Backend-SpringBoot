#!/bin/bash

# Build Docker Image for Render.com Deployment
# This script creates an optimized Docker image for the Sales Management System

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
IMAGE_NAME="sales-management-backend"
IMAGE_TAG="render"
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"

echo "🐳 Building Docker Image for Render.com Deployment"
echo "=================================================="

# Check if Docker is available
print_status "Checking Docker availability..."
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running"
    exit 1
fi

print_success "Docker is available"

# Check if required files exist
print_status "Checking required files..."
required_files=(
    "Dockerfile"
    "pom.xml"
    "src/main/java"
    "src/main/resources"
)

for file in "${required_files[@]}"; do
    if [[ -e "$file" ]]; then
        print_success "✓ $file exists"
    else
        print_error "✗ $file is missing"
        exit 1
    fi
done

# Clean previous builds
print_status "Cleaning previous builds..."
mvn clean -q
print_success "Previous builds cleaned"

# Build the application
print_status "Building Spring Boot application..."
if mvn package -DskipTests -q; then
    print_success "Application built successfully"
else
    print_error "Maven build failed"
    exit 1
fi

# Check if JAR file was created
JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
if [[ -z "$JAR_FILE" ]]; then
    print_error "JAR file not found in target directory"
    exit 1
fi

print_success "JAR file created: $JAR_FILE"

# Remove existing image if it exists
print_status "Removing existing image if present..."
if docker image inspect "$FULL_IMAGE_NAME" &> /dev/null; then
    docker rmi "$FULL_IMAGE_NAME" &> /dev/null
    print_success "Existing image removed"
fi

# Build Docker image
print_status "Building Docker image: $FULL_IMAGE_NAME"
echo "This may take a few minutes..."

if docker build -t "$FULL_IMAGE_NAME" .; then
    print_success "Docker image built successfully"
else
    print_error "Docker build failed"
    exit 1
fi

# Get image information
IMAGE_SIZE=$(docker images "$FULL_IMAGE_NAME" --format "table {{.Size}}" | tail -n 1)
IMAGE_ID=$(docker images "$FULL_IMAGE_NAME" --format "table {{.ID}}" | tail -n 1)

print_success "Image Details:"
echo "  Name: $FULL_IMAGE_NAME"
echo "  ID: $IMAGE_ID"
echo "  Size: $IMAGE_SIZE"

# Test the image
print_status "Testing the Docker image..."

# Create a test container
CONTAINER_NAME="sales-management-test-$$"
print_status "Starting test container: $CONTAINER_NAME"

# Set test environment variables
TEST_ENV_VARS=(
    "-e SPRING_PROFILES_ACTIVE=render"
    "-e JWT_SECRET=test-jwt-secret-for-docker-build-testing-only-not-for-production-use"
    "-e DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    "-e LOG_LEVEL=INFO"
)

# Start container in detached mode
if docker run -d --name "$CONTAINER_NAME" -p 8082:8081 ${TEST_ENV_VARS[@]} "$FULL_IMAGE_NAME"; then
    print_success "Test container started"
else
    print_error "Failed to start test container"
    exit 1
fi

# Wait for application to start
print_status "Waiting for application to start..."
sleep 30

# Test health endpoint
MAX_ATTEMPTS=10
ATTEMPT=1

while [[ $ATTEMPT -le $MAX_ATTEMPTS ]]; do
    if curl -f "http://localhost:8082/actuator/health" &> /dev/null; then
        print_success "Health check passed"
        break
    fi
    
    if [[ $ATTEMPT -eq $MAX_ATTEMPTS ]]; then
        print_error "Health check failed after $MAX_ATTEMPTS attempts"
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

# Clean up test container
print_status "Cleaning up test container..."
docker stop "$CONTAINER_NAME" &> /dev/null
docker rm "$CONTAINER_NAME" &> /dev/null
print_success "Test container cleaned up"

# Create additional tags
print_status "Creating additional tags..."
docker tag "$FULL_IMAGE_NAME" "${IMAGE_NAME}:latest"
docker tag "$FULL_IMAGE_NAME" "${IMAGE_NAME}:$(date +%Y%m%d-%H%M%S)"
print_success "Additional tags created"

# Show final image list
print_status "Available images:"
docker images "$IMAGE_NAME"

echo ""
print_success "🎉 Docker image for Render.com deployment created successfully!"
echo ""
echo "Image Details:"
echo "  Primary Tag: $FULL_IMAGE_NAME"
echo "  Latest Tag: ${IMAGE_NAME}:latest"
echo "  Timestamped Tag: ${IMAGE_NAME}:$(date +%Y%m%d-%H%M%S)"
echo "  Size: $IMAGE_SIZE"
echo ""
echo "Next Steps:"
echo "1. Test locally: docker run -p 8081:8081 --env-file .env.render $FULL_IMAGE_NAME"
echo "2. Push to registry: docker push $FULL_IMAGE_NAME"
echo "3. Deploy to Render.com using this image"
echo ""
echo "For Render.com deployment:"
echo "- Use Docker environment"
echo "- Set Dockerfile path: ./Dockerfile"
echo "- Configure environment variables in Render.com dashboard"
echo ""
print_status "Ready for Render.com deployment! 🚀"
