#!/bin/bash

# Test script for Product Performance Report API
# This script tests the fixed product performance report endpoint

BASE_URL="http://abusaker.zapto.org:8081"
API_ENDPOINT="/api/v1/reports/products/performance"

# Test parameters
START_DATE="2025-06-10T00:00:00"
END_DATE="2025-07-10T23:59:59"

echo "Testing Product Performance Report API"
echo "======================================"
echo ""

# Test 1: Basic API call without authentication (should return 401)
echo "Test 1: Testing without authentication (expecting 401)..."
curl -s -w "\nHTTP Status: %{http_code}\n" \
  "${BASE_URL}${API_ENDPOINT}?startDate=${START_DATE}&endDate=${END_DATE}"
echo ""
echo "----------------------------------------"
echo ""

# Test 2: API call with mock authentication (replace with actual token)
echo "Test 2: Testing with authentication (replace TOKEN with actual JWT)..."
echo "Note: You need to replace 'YOUR_JWT_TOKEN_HERE' with an actual JWT token"
echo ""

# Uncomment and modify the following lines when you have a valid JWT token:
# TOKEN="YOUR_JWT_TOKEN_HERE"
# curl -s -H "Authorization: Bearer ${TOKEN}" \
#   -H "Content-Type: application/json" \
#   -w "\nHTTP Status: %{http_code}\n" \
#   "${BASE_URL}${API_ENDPOINT}?startDate=${START_DATE}&endDate=${END_DATE}" | jq '.'

echo "curl -s -H \"Authorization: Bearer \${TOKEN}\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -w \"\\nHTTP Status: %{http_code}\\n\" \\"
echo "  \"${BASE_URL}${API_ENDPOINT}?startDate=${START_DATE}&endDate=${END_DATE}\" | jq '.'"
echo ""
echo "----------------------------------------"
echo ""

# Test 3: Check if application is running
echo "Test 3: Checking if application is running..."
curl -s -w "\nHTTP Status: %{http_code}\n" \
  "${BASE_URL}/actuator/health" 2>/dev/null || echo "Application not running or health endpoint not available"
echo ""
echo "----------------------------------------"
echo ""

echo "Test Instructions:"
echo "=================="
echo "1. Start the Spring Boot application: ./mvnw spring-boot:run"
echo "2. Obtain a JWT token by logging in through the authentication endpoint"
echo "3. Replace 'YOUR_JWT_TOKEN_HERE' in this script with the actual token"
echo "4. Run this script to test the API"
echo ""
echo "Expected Response Structure:"
echo "{"
echo "  \"success\": true,"
echo "  \"data\": {"
echo "    \"productRankings\": {"
echo "      \"topProductsByRevenue\": [...],"
echo "      \"topProductsByQuantity\": [...],"
echo "      \"topProductsByProfit\": [...],"
echo "      \"summary\": {...}"
echo "    },"
echo "    \"profitabilityAnalysis\": {"
echo "      \"profitabilityMetrics\": {...},"
echo "      \"profitMarginDistribution\": {...},"
echo "      \"categoryProfitability\": {...}"
echo "    },"
echo "    \"categoryPerformance\": {"
echo "      \"categoryMetrics\": [...],"
echo "      \"topCategoriesByRevenue\": [...]"
echo "    },"
echo "    \"productTrends\": {"
echo "      \"dailyTrends\": {...},"
echo "      \"weeklyTrends\": {...},"
echo "      \"trendingProducts\": [...]"
echo "    },"
echo "    \"crossSellAnalysis\": {"
echo "      \"productPairs\": [...],"
echo "      \"crossSellOpportunities\": [...],"
echo "      \"basketAnalysis\": {...}"
echo "    }"
echo "  },"
echo "  \"metadata\": {"
echo "    \"reportType\": \"PRODUCT_PERFORMANCE\","
echo "    \"reportName\": \"Product Performance Report\","
echo "    \"generatedAt\": \"2025-07-10T15:30:00\","
echo "    \"executionTimeMs\": 245"
echo "  }"
echo "}"
