#!/bin/bash

# Test script for verifying the new domain configuration
# This script tests all the updated URLs to ensure they work correctly

echo "🔍 Testing New Domain Configuration: abusaker.zapto.org"
echo "=================================================="
echo ""

# Color codes for output
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

# Test URLs
BASE_URL="http://abusaker.zapto.org:8081"
MYSQL_URL="abusaker.zapto.org:3307"
PHPMYADMIN_URL="http://abusaker.zapto.org:8080"

echo "Testing the following URLs:"
echo "- Backend: $BASE_URL"
echo "- MySQL: $MYSQL_URL"
echo "- phpMyAdmin: $PHPMYADMIN_URL"
echo ""

# Test 1: DNS Resolution
print_status "Test 1: Testing DNS resolution for abusaker.zapto.org..."
if nslookup abusaker.zapto.org > /dev/null 2>&1; then
    print_success "DNS resolution successful"
else
    print_warning "DNS resolution failed or nslookup not available"
    print_status "Trying with ping..."
    if ping -c 1 abusaker.zapto.org > /dev/null 2>&1; then
        print_success "Domain is reachable via ping"
    else
        print_error "Domain is not reachable"
    fi
fi
echo ""

# Test 2: Backend Health Check
print_status "Test 2: Testing backend health check endpoint..."
print_status "URL: ${BASE_URL}/api/auth/test"

response=$(curl -s -w "HTTPSTATUS:%{http_code}" "${BASE_URL}/api/auth/test" 2>/dev/null)
if [ $? -eq 0 ]; then
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [ "$http_code" -eq 200 ]; then
        print_success "Backend health check successful (HTTP $http_code)"
        echo "Response: $body"
    elif [ "$http_code" -eq 401 ]; then
        print_success "Backend is running but requires authentication (HTTP $http_code)"
        echo "This is expected behavior for the auth endpoint"
    else
        print_warning "Backend responded with HTTP $http_code"
        echo "Response: $body"
    fi
else
    print_error "Failed to connect to backend"
    print_status "This might be because the application is not running"
fi
echo ""

# Test 3: Health Check Endpoint
print_status "Test 3: Testing actuator health endpoint..."
print_status "URL: ${BASE_URL}/actuator/health"

response=$(curl -s -w "HTTPSTATUS:%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null)
if [ $? -eq 0 ]; then
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [ "$http_code" -eq 200 ]; then
        print_success "Health endpoint successful (HTTP $http_code)"
        echo "Response: $body"
    else
        print_warning "Health endpoint responded with HTTP $http_code"
        echo "Response: $body"
    fi
else
    print_error "Failed to connect to health endpoint"
fi
echo ""

# Test 4: MySQL Connection Test
print_status "Test 4: Testing MySQL connection..."
print_status "Host: $MYSQL_URL"

if command -v mysql > /dev/null 2>&1; then
    print_status "MySQL client found, testing connection..."
    if timeout 5 mysql -h abusaker.zapto.org -P 3307 -u sales_user -psales_password -e "SELECT 1;" > /dev/null 2>&1; then
        print_success "MySQL connection successful"
    else
        print_warning "MySQL connection failed"
        print_status "This might be because:"
        print_status "  - MySQL server is not running"
        print_status "  - Credentials are incorrect"
        print_status "  - Port 3307 is not accessible"
    fi
else
    print_warning "MySQL client not found, skipping connection test"
    print_status "To test manually: mysql -h abusaker.zapto.org -P 3307 -u sales_user -p"
fi
echo ""

# Test 5: phpMyAdmin Access Test
print_status "Test 5: Testing phpMyAdmin access..."
print_status "URL: $PHPMYADMIN_URL"

response=$(curl -s -w "HTTPSTATUS:%{http_code}" "$PHPMYADMIN_URL" 2>/dev/null)
if [ $? -eq 0 ]; then
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    
    if [ "$http_code" -eq 200 ]; then
        print_success "phpMyAdmin is accessible (HTTP $http_code)"
    else
        print_warning "phpMyAdmin responded with HTTP $http_code"
    fi
else
    print_error "Failed to connect to phpMyAdmin"
    print_status "This might be because phpMyAdmin is not running"
fi
echo ""

# Test 6: Port Connectivity
print_status "Test 6: Testing port connectivity..."

for port in 8081 3307 8080; do
    print_status "Testing port $port..."
    if command -v nc > /dev/null 2>&1; then
        if timeout 3 nc -z abusaker.zapto.org $port > /dev/null 2>&1; then
            print_success "Port $port is open"
        else
            print_warning "Port $port is not accessible"
        fi
    elif command -v telnet > /dev/null 2>&1; then
        if timeout 3 telnet abusaker.zapto.org $port > /dev/null 2>&1; then
            print_success "Port $port is open"
        else
            print_warning "Port $port is not accessible"
        fi
    else
        print_warning "Neither nc nor telnet available, skipping port test for $port"
    fi
done
echo ""

# Summary
echo "🎯 Test Summary"
echo "==============="
echo ""
print_status "Domain Configuration:"
echo "  - Base Domain: abusaker.zapto.org"
echo "  - Backend Port: 8081"
echo "  - MySQL Port: 3307"
echo "  - phpMyAdmin Port: 8080"
echo ""

print_status "Key URLs to test manually:"
echo "  - Backend API: $BASE_URL/api/auth/test"
echo "  - Health Check: $BASE_URL/actuator/health"
echo "  - phpMyAdmin: $PHPMYADMIN_URL"
echo ""

print_status "To start the application:"
echo "  ./docker-build.sh all"
echo "  # or"
echo "  docker-compose up -d"
echo ""

print_status "To test with authentication:"
echo "  1. Get JWT token from login endpoint"
echo "  2. Use token in Authorization header"
echo "  3. Test protected endpoints"
echo ""

echo "✅ Domain migration verification complete!"
echo "📝 See docs/Base_URL_Migration_Summary.md for detailed changes"
