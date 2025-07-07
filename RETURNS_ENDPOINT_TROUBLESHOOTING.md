# Returns Endpoint 404 Error Troubleshooting Guide

## Problem
Frontend is getting 404 error when trying to POST to `/api/returns`:
```
[AWT-EventQueue-0] INFO io.ktor.client.HttpClient - REQUEST: http://localhost:8081/api/returns
METHOD: HttpMethod(value=POST)
[AWT-EventQueue-0] INFO io.ktor.client.HttpClient - RESPONSE: 404 
METHOD: HttpMethod(value=POST)
FROM: http://localhost:8081/api/returns
```

## Quick Diagnosis Steps

### Step 1: Verify Application is Running
```bash
# Check if port 8081 is in use
netstat -an | findstr :8081

# Or check with curl
curl -X GET http://localhost:8081/api/returns
```

### Step 2: Check Application Startup
Look for these in the console output when starting the application:
```
Started SalesManagementBackendApplication in X.XXX seconds
Tomcat started on port(s): 8081 (http)
```

### Step 3: Verify Endpoint Registration
Check the startup logs for endpoint mappings:
```
Mapped "{[/api/returns],methods=[POST]}" onto public org.springframework.http.ResponseEntity<...>
```

## Common Solutions

### Solution 1: Start the Application
If the application isn't running:

```bash
# Navigate to project directory
cd /path/to/Sales-Managment-System-Backend-SpringBoot

# Start the application
./mvnw spring-boot:run

# Or on Windows
mvnw.cmd spring-boot:run
```

### Solution 2: Check Port Configuration
Verify the application is running on the correct port in `application.properties`:
```properties
server.port=8081
```

### Solution 3: Database Connection Issues
If the application fails to start due to database issues:

1. **Check MySQL is running**:
   ```bash
   # Windows
   net start mysql
   
   # Linux/Mac
   sudo systemctl start mysql
   ```

2. **Verify database exists**:
   ```sql
   CREATE DATABASE IF NOT EXISTS sales_management;
   ```

3. **Check database credentials** in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/sales_management?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

### Solution 4: Clear and Rebuild
If there are compilation issues:

```bash
# Clean and rebuild
./mvnw clean compile

# Or full clean install
./mvnw clean install

# Then restart
./mvnw spring-boot:run
```

## Verification Steps

### 1. Test with curl
Once the application is running, test the endpoint:

```bash
# Test GET endpoint (should work)
curl -X GET http://localhost:8081/api/returns

# Test POST endpoint (should require authentication)
curl -X POST http://localhost:8081/api/returns \
  -H "Content-Type: application/json" \
  -d '{
    "originalSaleId": 1,
    "customerId": 1,
    "reason": "DEFECTIVE",
    "notes": "Test return",
    "items": [
      {
        "productId": 1,
        "returnQuantity": 1,
        "reason": "DEFECTIVE"
      }
    ]
  }'
```

### 2. Check Available Endpoints
Get a list of all available endpoints:
```bash
curl -X GET http://localhost:8081/actuator/mappings
```

### 3. Verify Controller Registration
Check the application logs for controller registration:
```
Mapped "{[/api/returns],methods=[GET]}" onto public org.springframework.http.ResponseEntity<...>
Mapped "{[/api/returns],methods=[POST]}" onto public org.springframework.http.ResponseEntity<...>
```

## ReturnController Endpoint Summary

The ReturnController should provide these endpoints:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/returns` | Get all returns with pagination |
| POST | `/api/returns` | Create a new return |
| GET | `/api/returns/{id}` | Get return by ID |
| PUT | `/api/returns/{id}` | Update return |
| DELETE | `/api/returns/{id}` | Delete return |
| GET | `/api/returns/search` | Search returns |
| POST | `/api/returns/{id}/approve` | Approve return |
| POST | `/api/returns/{id}/reject` | Reject return |
| POST | `/api/returns/{id}/refund` | Process refund |
| GET | `/api/returns/{id}/items` | Get return with items |
| GET | `/api/returns/customer/{customerId}` | Get returns by customer |
| GET | `/api/returns/analytics` | Get return analytics |

## Authentication Requirements

The returns endpoint requires JWT authentication. Make sure your frontend is sending:

```javascript
headers: {
  'Authorization': 'Bearer ' + jwtToken,
  'Content-Type': 'application/json'
}
```

## Sample Request Body for POST /api/returns

```json
{
  "originalSaleId": 1,
  "customerId": 1,
  "reason": "DEFECTIVE",
  "notes": "Product arrived damaged",
  "items": [
    {
      "productId": 1,
      "returnQuantity": 1,
      "reason": "DEFECTIVE",
      "condition": "DAMAGED",
      "notes": "Screen is cracked"
    }
  ]
}
```

## Expected Response (201 Created)

```json
{
  "id": 1,
  "returnNumber": "RET-123456-ABC12345",
  "originalSaleId": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "returnDate": "2025-07-06T17:49:51.193",
  "status": "PENDING",
  "reason": "DEFECTIVE",
  "totalRefundAmount": 999.99,
  "notes": "Product arrived damaged",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Smartphone",
      "returnQuantity": 1,
      "reason": "DEFECTIVE",
      "condition": "DAMAGED",
      "refundAmount": 999.99
    }
  ]
}
```

## Common Error Responses

### 400 Bad Request - Validation Error
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "There are validation errors that need to be corrected",
  "errorCode": "VALIDATION_ERROR",
  "validationErrors": {
    "originalSaleId": "Original sale ID is required",
    "customerId": "Customer ID is required"
  }
}
```

### 404 Not Found - Sale Not Found
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Sale not found with id: 999",
  "errorCode": "RESOURCE_NOT_FOUND"
}
```

### 409 Conflict - Return Policy Violation
```json
{
  "status": 409,
  "error": "Business Rule Violation",
  "message": "Return period has expired. Returns must be initiated within 30 days of purchase.",
  "errorCode": "BUSINESS_RULE_VIOLATION"
}
```

## Debugging Steps

### 1. Enable Debug Logging
Add to `application.properties`:
```properties
logging.level.com.hamza.salesmanagementbackend.controller.ReturnController=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 2. Check Application Context
Verify the ReturnController bean is registered:
```bash
# Look for this in startup logs
Bean 'returnController' of type [com.hamza.salesmanagementbackend.controller.ReturnController]
```

### 3. Verify Dependencies
Check that all required dependencies are available:
- ReturnService
- ReturnRepository
- Database connection

### 4. Test Individual Components
Test the service layer directly:
```java
@Autowired
private ReturnService returnService;

// Test in a @Test method or @PostConstruct
ReturnDTO result = returnService.createReturn(testReturnDTO);
```

## If Problem Persists

### 1. Check for Port Conflicts
```bash
# Kill any process using port 8081
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill -9
```

### 2. Try Different Port
Temporarily change the port in `application.properties`:
```properties
server.port=8082
```

### 3. Check Firewall/Antivirus
Ensure your firewall or antivirus isn't blocking the port.

### 4. Restart IDE and Clean Build
Sometimes IDE caching can cause issues:
1. Close your IDE
2. Delete `target/` directory
3. Run `./mvnw clean install`
4. Restart IDE and application

## Success Indicators

When everything is working correctly, you should see:

1. **Application starts successfully** with no errors
2. **Port 8081 is listening** (netstat shows LISTENING)
3. **GET /api/returns returns 200** (even if empty list)
4. **POST /api/returns returns 401** (if no auth) or **201** (if authenticated)
5. **Endpoint mappings appear in logs** during startup

## Contact Support

If none of these solutions work:
1. Check the full application startup logs for errors
2. Verify all dependencies are properly installed
3. Ensure Java version compatibility (Java 17+)
4. Check for any custom security configurations blocking the endpoint

The ReturnController is properly implemented and should work once the application is running correctly on port 8081.
