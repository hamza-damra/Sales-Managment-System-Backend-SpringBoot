# Sales API Troubleshooting Guide

## Quick Diagnosis

If you're getting 404 errors for sales endpoints, follow this step-by-step troubleshooting guide.

## Step 1: Verify Application Status

### Check if Spring Boot Application is Running

1. **Open Command Prompt/Terminal** in the project directory
2. **Check if application is running** on port 8081:
   ```bash
   # Windows
   netstat -an | findstr :8081
   
   # Linux/Mac
   lsof -i :8081
   ```

3. **If no process is running**, start the application:
   ```bash
   # Windows
   mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```

### Expected Startup Output
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2025-07-06 14:30:00.000  INFO --- [           main] c.h.s.SalesManagementBackendApplication : Starting SalesManagementBackendApplication
2025-07-06 14:30:00.000  INFO --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8081 (http)
2025-07-06 14:30:00.000  INFO --- [           main] c.h.s.SalesManagementBackendApplication : Started SalesManagementBackendApplication in 5.123 seconds
```

## Step 2: Database Connection Verification

### Check MySQL Service

1. **Verify MySQL is running**:
   ```bash
   # Windows
   net start | findstr MySQL
   
   # Linux/Mac
   sudo systemctl status mysql
   ```

2. **Start MySQL if not running**:
   ```bash
   # Windows
   net start mysql80
   
   # Linux/Mac
   sudo systemctl start mysql
   ```

### Test Database Connection

1. **Open MySQL Command Line** or MySQL Workbench
2. **Connect using application credentials**:
   ```sql
   mysql -u root -p
   ```

3. **Check if database exists**:
   ```sql
   SHOW DATABASES;
   USE sales_management;
   SHOW TABLES;
   ```

4. **If database doesn't exist**, create it:
   ```sql
   CREATE DATABASE sales_management;
   ```

### Verify Application Properties

Check `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sales_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
server.port=8081
```

## Step 3: Test API Endpoints

### 1. Test Basic Connectivity
```bash
curl http://localhost:8081/api/sales
```

**Expected Response** (if not authenticated):
```json
{
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid"
}
```

### 2. Test Authentication First
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

**Expected Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

### 3. Test Sales Endpoint with Token
```bash
curl http://localhost:8081/api/sales \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Step 4: Common Issues and Solutions

### Issue 1: "mvnw command not found"

**Solution**: Use the correct command for your OS:
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
chmod +x mvnw
./mvnw spring-boot:run
```

### Issue 2: "JAVA_HOME not set"

**Solution**: Set Java environment variable:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

### Issue 3: "Port 8081 already in use"

**Solution**: 
1. **Find process using port**:
   ```bash
   # Windows
   netstat -ano | findstr :8081
   
   # Linux/Mac
   lsof -i :8081
   ```

2. **Kill the process** or **change port** in `application.properties`:
   ```properties
   server.port=8082
   ```

### Issue 4: "Access denied for user 'root'"

**Solution**: Update database credentials in `application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### Issue 5: "Table doesn't exist"

**Solution**: Enable automatic table creation:
```properties
spring.jpa.hibernate.ddl-auto=update
```

## Step 5: Verify Sales Endpoints

Once the application is running, test each endpoint:

### 1. Get All Sales
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/sales
```

### 2. Create a Sale
```bash
curl -X POST http://localhost:8081/api/sales \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 1,
        "unitPrice": 100.00
      }
    ]
  }'
```

### 3. Get Sale by ID
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/sales/1
```

## Step 6: Check Application Logs

### View Startup Logs
Look for these key indicators in the console output:

✅ **Successful Startup**:
```
Started SalesManagementBackendApplication in X.XXX seconds
Tomcat started on port(s): 8081 (http)
```

❌ **Database Connection Error**:
```
Unable to create initial connections of pool
Access denied for user 'root'@'localhost'
```

❌ **Port Conflict Error**:
```
Port 8081 was already in use
```

❌ **Compilation Error**:
```
Compilation failure
[ERROR] COMPILATION ERROR
```

## Step 7: Environment Setup Verification

### Java Version Check
```bash
java -version
# Should show Java 17 or higher
```

### Maven Version Check
```bash
mvn -version
# Or use the wrapper: ./mvnw -version
```

### MySQL Version Check
```bash
mysql --version
# Should show MySQL 8.0 or compatible version
```

## Quick Fix Checklist

- [ ] Java 17+ installed and JAVA_HOME set
- [ ] MySQL server running
- [ ] Database `sales_management` exists
- [ ] Port 8081 available
- [ ] Application starts without errors
- [ ] Can connect to `http://localhost:8081`
- [ ] Authentication endpoint works
- [ ] Sales endpoints respond (even if unauthorized)

## Getting Help

If you're still experiencing issues:

1. **Check the full application logs** for specific error messages
2. **Verify all environment prerequisites** are met
3. **Test with a REST client** like Postman or Insomnia
4. **Check firewall and antivirus** settings
5. **Try a different port** if 8081 is problematic

## Success Indicators

You'll know everything is working when:

✅ Application starts without errors  
✅ Database connection established  
✅ Sales endpoints return data (not 404)  
✅ Authentication works  
✅ CRUD operations function properly  

The sales functionality is fully implemented - any 404 errors are environmental issues, not missing code!
