# 🛒 Sales Management System Backend

A comprehensive REST API backend for sales management built with Spring Boot, featuring JWT authentication, role-based access control, and complete CRUD operations for managing customers, products, and sales.

## 🚀 Features

### 🔐 Authentication & Security
- **JWT Authentication** with access and refresh tokens
- **Role-based Access Control** (USER, ADMIN)
- **Secure Password Encryption** using BCrypt
- **CORS Configuration** for cross-origin requests
- **Global Exception Handling** with standardized error responses

### 📊 Core Functionality
- **Customer Management** - Create, read, update, delete customers
- **Product Management** - Inventory tracking with stock management
- **Sales Management** - Complete sales workflow with order processing
- **Reporting System** - Sales analytics and business insights
- **Real-time Stock Updates** - Automatic inventory adjustments

### 🏗️ Technical Features
- **RESTful API Design** following best practices
- **Data Validation** with comprehensive input validation
- **Database Integration** with MySQL and JPA/Hibernate
- **Pagination & Sorting** for large datasets
- **Comprehensive Error Handling** with detailed error messages
- **API Documentation** ready for integration

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test
- **Documentation**: Spring Boot Actuator

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

## ⚡ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/hamza-damra/Sales-Managment-System-Backend-SpringBoot.git
cd Sales-Managment-System-Backend-SpringBoot
```

### 2. Database Setup
```sql
CREATE DATABASE sales_management;
```

### 3. Configure Application
Update `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/sales_management
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration (Base64 encoded secret)
jwt.secret=your_base64_encoded_secret
jwt.expiration=86400000
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8081`

## 📚 API Documentation

### Authentication Endpoints
```
POST /api/auth/signup    - Register new user
POST /api/auth/login     - User login
POST /api/auth/refresh   - Refresh JWT token
```

### Customer Management
```
GET    /api/customers         - Get all customers (paginated)
POST   /api/customers         - Create new customer
GET    /api/customers/{id}    - Get customer by ID
PUT    /api/customers/{id}    - Update customer
DELETE /api/customers/{id}    - Delete customer
```

### Product Management
```
GET    /api/products          - Get all products (paginated)
POST   /api/products          - Create new product
GET    /api/products/{id}     - Get product by ID
PUT    /api/products/{id}     - Update product
DELETE /api/products/{id}     - Delete product
```

### Sales Management
```
GET    /api/sales             - Get all sales (paginated)
POST   /api/sales             - Create new sale
GET    /api/sales/{id}        - Get sale by ID
PUT    /api/sales/{id}        - Update sale
DELETE /api/sales/{id}        - Delete sale
```

### Reports
```
GET    /api/reports/sales-summary     - Sales summary report
GET    /api/reports/top-products      - Top selling products
GET    /api/reports/customer-stats    - Customer statistics
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/hamza/salesmanagementbackend/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── payload/        # Request/Response payloads
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security configuration
│   │   └── service/        # Business logic services
│   └── resources/
│       └── application.properties
└── test/                   # Unit and integration tests
```

## 🔧 Configuration

### Database Configuration
The application uses MySQL by default. Configure your database connection in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sales_management?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```

### JWT Configuration
Ensure your JWT secret is Base64 encoded:

```properties
jwt.secret=bXlTZWNyZXRLZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkw
jwt.expiration=86400000
```

## 🧪 Testing

Run the test suite:
```bash
./mvnw test
```

## 🚀 Deployment

### Production Build
```bash
./mvnw clean package -DskipTests
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/sales-management-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Hamza Damra**
- GitHub: [@hamza-damra](https://github.com/hamza-damra)
- Email: hamza.damra@students.alquds.edu

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Spring Security for robust authentication
- MySQL for reliable data persistence
- All contributors who helped improve this project

---

⭐ **Star this repository if you find it helpful!**
