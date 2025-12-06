# Online Banking System

#Update for change
A comprehensive banking application built with Spring Boot, providing secure account management, transaction processing, and loan services.

## 🏗️ Architecture

- **Backend**: Spring Boot 3.x with Spring Security
- **Database**: Oracle Database with JPA/Hibernate
- **Frontend**: Thymeleaf with Bootstrap 5
- **Authentication**: JWT-based security
- **Build Tool**: Maven

## 🚀 Features

### Core Banking

- **Account Management**: Create and manage multiple account types (Savings, Checking, Business)
- **Transaction Processing**: Deposits, withdrawals, and inter-account transfers
- **Statement Generation**: Mini and full account statements with date filtering
- **Real-time Balance Updates**: Live account balance tracking

### Loan Services

- **General Loans**: Personal loans with flexible terms
- **Student Loans**: Education financing with moratorium periods
- **Vehicle Loans**: Auto financing with competitive rates
- **Loan Management**: Application, approval, disbursement, and EMI tracking

### Security & Administration

- **JWT Authentication**: Secure token-based authentication
- **Role-based Access**: Customer, Loan Officer, and Admin roles
- **Admin Dashboard**: System-wide loan and user management
- **Audit Logging**: Complete transaction and activity tracking

## 👥 Development Team & Contributions

### Sprint 1 (September 2025)

| Contributor             | Tasks Completed                                   | Status  |
| ----------------------- | ------------------------------------------------- | ------- |
| **Kishor Bhagat**       | Security & Configurations, Transaction Management | ✅ Done |
| **John Samuel Meshach** | Admin Dashboard, Student Loan System              | ✅ Done |
| **Bibhab Ranjan Panda** | Customer & Account Management                     | ✅ Done |
| **Aryaman Patra**       | Vehicle Loan System                               | ✅ Done |
| **Subhasish Kabi**      | General Loan System, Statement Generation         | ✅ Done |

### Sprint 2 (September 2025)

| Contributor                             | Tasks Completed                       | Status  |
| --------------------------------------- | ------------------------------------- | ------- |
| **Aryaman Patra**                       | Profile Management                    | ✅ Done |
| **Kishor Bhagat**                       | Transaction Pages                     | ✅ Done |
| **Bibhab Ranjan Panda**                 | Account Section, Login/Register Pages | ✅ Done |
| **John Samuel Meshach & Aryaman Patra** | Loans Module                          | ✅ Done |
| **Aryaman Patra**                       | Dashboard Implementation              | ✅ Done |

## 🛠️ Technology Stack

### Backend

- **Spring Boot 3.x** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Oracle Database** - Primary database
- **JWT** - Token-based authentication
- **Maven** - Dependency management

### Frontend

- **Thymeleaf** - Server-side templating
- **Bootstrap 5** - UI framework
- **JavaScript ES6** - Client-side functionality
- **Font Awesome** - Icons

### Development Tools

- **Swagger** - API documentation
- **JUnit 5** - Testing framework
- **Testcontainers** - Integration testing

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/bankingmini/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── static/         # CSS, JS, images
│       ├── templates/      # Thymeleaf templates
│       └── application.properties
└── test/                   # Test classes
```

## 🚦 Getting Started

### Prerequisites

- Java 17 or higher
- Oracle Database 19c or higher
- Maven 3.6+

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd loanimplementations
   ```

2. **Configure Database**

   ```properties
   # Update src/main/resources/application.properties
   spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Initialize Database**

   ```bash
   # Run the SQL scripts in order:
   # 1. src/main/resources/01_create_sequences.sql
   # 2. src/main/resources/02_fix_loan_sequence_naming.sql
   # 3. src/main/resources/data.sql
   ```

4. **Build and Run**

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access Application**
   - URL: http://localhost:8080
   - Default Admin: Create via registration

## 🔐 Security Features

- **JWT Authentication** with refresh token support
- **Role-based Authorization** (Customer, Loan Officer, Admin)
- **Password Encryption** using BCrypt
- **CORS Configuration** for cross-origin requests
- **Session Management** with secure cookies

## 📊 API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Key Endpoints

- **Authentication**: `/api/auth/*`
- **Accounts**: `/api/account/*`
- **Transactions**: `/api/transactions/*`
- **Loans**: `/api/loan/*`, `/api/student-loans/*`, `/api/vehicle-loans/*`
- **Admin**: `/api/dashboard/*`

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AccountServiceTest

# Generate test coverage report
mvn jacoco:report
```

## 📈 Performance Features

- **Connection Pooling** for database optimization
- **Lazy Loading** for JPA entities
- **Caching** for frequently accessed data
- **Pagination** for large data sets
- **Async Processing** for heavy operations

## 🔧 Configuration

### Database Configuration

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.OracleDialect
spring.jpa.show-sql=false
```

### JWT Configuration

```properties
jwt.secret=your-secret-key
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```
