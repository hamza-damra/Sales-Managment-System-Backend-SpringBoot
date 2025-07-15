#!/bin/bash

# Push Docker Image to Container Registry for Render.com Deployment
# Supports Docker Hub, GitHub Container Registry, and other registries

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
LOCAL_IMAGE_NAME="sales-management-backend"
LOCAL_IMAGE_TAG="render"
LOCAL_FULL_IMAGE="${LOCAL_IMAGE_NAME}:${LOCAL_IMAGE_TAG}"

echo "📦 Push Docker Image to Container Registry"
echo "=========================================="

# Check if local image exists
print_status "Checking if local image exists..."
if ! docker image inspect "$LOCAL_FULL_IMAGE" &> /dev/null; then
    print_error "Local image $LOCAL_FULL_IMAGE not found"
    print_status "Please run build-render-image.sh first"
    exit 1
fi

print_success "Local image found: $LOCAL_FULL_IMAGE"

# Registry selection
echo ""
echo "Select container registry:"
echo "1) Docker Hub (docker.io)"
echo "2) GitHub Container Registry (ghcr.io)"
echo "3) Custom registry"
echo ""
read -p "Enter your choice (1-3): " registry_choice

case $registry_choice in
    1)
        REGISTRY="docker.io"
        print_status "Selected: Docker Hub"
        read -p "Enter your Docker Hub username: " username
        REMOTE_IMAGE="${username}/${LOCAL_IMAGE_NAME}"
        ;;
    2)
        REGISTRY="ghcr.io"
        print_status "Selected: GitHub Container Registry"
        read -p "Enter your GitHub username: " username
        REMOTE_IMAGE="ghcr.io/${username}/${LOCAL_IMAGE_NAME}"
        ;;
    3)
        print_status "Selected: Custom registry"
        read -p "Enter registry URL (e.g., registry.example.com): " REGISTRY
        read -p "Enter image name (e.g., username/sales-management-backend): " image_name
        REMOTE_IMAGE="${REGISTRY}/${image_name}"
        ;;
    *)
        print_error "Invalid choice"
        exit 1
        ;;
esac

# Confirm settings
echo ""
print_status "Push Configuration:"
echo "  Local Image: $LOCAL_FULL_IMAGE"
echo "  Remote Image: $REMOTE_IMAGE"
echo "  Registry: $REGISTRY"
echo ""
read -p "Continue with push? (y/N): " confirm

if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    print_warning "Push cancelled"
    exit 0
fi

# Login to registry
print_status "Logging in to registry..."
case $registry_choice in
    1)
        print_status "Please login to Docker Hub"
        if ! docker login; then
            print_error "Docker Hub login failed"
            exit 1
        fi
        ;;
    2)
        print_status "Please login to GitHub Container Registry"
        print_status "Use your GitHub username and a Personal Access Token with 'write:packages' scope"
        if ! docker login ghcr.io; then
            print_error "GitHub Container Registry login failed"
            exit 1
        fi
        ;;
    3)
        print_status "Please login to custom registry"
        if ! docker login "$REGISTRY"; then
            print_error "Custom registry login failed"
            exit 1
        fi
        ;;
esac

print_success "Successfully logged in to registry"

# Tag images for remote registry
print_status "Tagging images for remote registry..."

# Tag with 'render' tag
REMOTE_RENDER_TAG="${REMOTE_IMAGE}:render"
docker tag "$LOCAL_FULL_IMAGE" "$REMOTE_RENDER_TAG"
print_success "Tagged: $REMOTE_RENDER_TAG"

# Tag with 'latest' tag
REMOTE_LATEST_TAG="${REMOTE_IMAGE}:latest"
docker tag "$LOCAL_FULL_IMAGE" "$REMOTE_LATEST_TAG"
print_success "Tagged: $REMOTE_LATEST_TAG"

# Tag with timestamp
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
REMOTE_TIMESTAMP_TAG="${REMOTE_IMAGE}:${TIMESTAMP}"
docker tag "$LOCAL_FULL_IMAGE" "$REMOTE_TIMESTAMP_TAG"
print_success "Tagged: $REMOTE_TIMESTAMP_TAG"

# Push images
print_status "Pushing images to registry..."

# Push render tag
print_status "Pushing render tag..."
if docker push "$REMOTE_RENDER_TAG"; then
    print_success "Pushed: $REMOTE_RENDER_TAG"
else
    print_error "Failed to push: $REMOTE_RENDER_TAG"
    exit 1
fi

# Push latest tag
print_status "Pushing latest tag..."
if docker push "$REMOTE_LATEST_TAG"; then
    print_success "Pushed: $REMOTE_LATEST_TAG"
else
    print_error "Failed to push: $REMOTE_LATEST_TAG"
    exit 1
fi

# Push timestamp tag
print_status "Pushing timestamp tag..."
if docker push "$REMOTE_TIMESTAMP_TAG"; then
    print_success "Pushed: $REMOTE_TIMESTAMP_TAG"
else
    print_error "Failed to push: $REMOTE_TIMESTAMP_TAG"
    exit 1
fi

# Get image information
IMAGE_SIZE=$(docker images "$REMOTE_IMAGE" --format "table {{.Size}}" | tail -n 1)

echo ""
print_success "🎉 Docker images pushed successfully!"
echo ""
echo "Pushed Images:"
echo "  Render Tag: $REMOTE_RENDER_TAG"
echo "  Latest Tag: $REMOTE_LATEST_TAG"
echo "  Timestamp Tag: $REMOTE_TIMESTAMP_TAG"
echo "  Size: $IMAGE_SIZE"
echo ""
echo "For Render.com deployment:"
echo "1. Go to Render.com Dashboard"
echo "2. Create new Web Service"
echo "3. Choose 'Deploy an existing image from a registry'"
echo "4. Enter image URL: $REMOTE_RENDER_TAG"
echo "5. Set environment variables:"
echo "   - DATABASE_URL"
echo "   - JWT_SECRET"
echo "   - SPRING_PROFILES_ACTIVE=render"
echo "   - Other optional variables"
echo ""
echo "Alternative Render.com deployment (from repository):"
echo "1. Connect your GitHub repository"
echo "2. Choose Docker environment"
echo "3. Set Dockerfile path: ./Dockerfile"
echo "4. Configure environment variables"
echo ""
print_status "Ready for Render.com deployment! 🚀"

# Clean up local tags (optional)
echo ""
read -p "Remove local remote tags? (y/N): " cleanup

if [[ "$cleanup" =~ ^[Yy]$ ]]; then
    print_status "Cleaning up local remote tags..."
    docker rmi "$REMOTE_RENDER_TAG" &> /dev/null || true
    docker rmi "$REMOTE_LATEST_TAG" &> /dev/null || true
    docker rmi "$REMOTE_TIMESTAMP_TAG" &> /dev/null || true
    print_success "Local remote tags cleaned up"
fi

echo ""
print_success "Push completed successfully! 🎉"
