# Multi-Tenant SaaS Platform 🚀

[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/Build-Passing-green.svg)](https://github.com/yourusername/multitenantsaas/actions)

## 📋 Overview

This is a robust, scalable Multi-Tenant SaaS backend built with Spring Boot. It supports tenant isolation using separate databases per tenant, subdomain-based routing, JWT authentication, and role-based access control (RBAC). The master database manages tenants, while each tenant operates in its own isolated environment for enhanced security and data privacy.

Designed for developers building SaaS applications, this project demonstrates dynamic tenant creation, secure authentication, and resource management. It's licensed under Apache 2.0 and ready for extension.

## ✨ Features

- **Tenant Isolation**: Dedicated databases per tenant to ensure data separation and compliance.
- **Subdomain Routing**: Automatically detects tenants from request subdomains (e.g., `tenant1.example.com`).
- **Authentication & Authorization**:
  - JWT-based tokens with tenant and role claims.
  - Separate logins for master (SUPERADMIN) and tenants (ADMIN/USER).
- **Dynamic Database Provisioning**: Creates tenant databases on-the-fly with Flyway migrations.
- **API Documentation**: Integrated Swagger UI for easy endpoint exploration.
- **Role-Based Access Control (RBAC)**: Restrict actions based on roles (e.g., SUPERADMIN creates tenants).
- **Error Handling & Logging**: Comprehensive logging with SLF4J for debugging.
- **Example Resources**: CRUD operations for users and items in tenant contexts.

## 🛠 Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Core language |
| **Spring Boot** | 3.3.3 | Framework for backend development |
| **Spring Data JPA** | Included | ORM for database interactions |
| **Hibernate** | Included | JPA implementation |
| **MySQL** | 8+ | Database (master and tenants) |
| **Flyway** | Latest | Database migrations |
| **Spring Security** | Included | Authentication and authorization |
| **JWT (jsonwebtoken)** | 0.11.5 | Token-based auth |
| **Springdoc OpenAPI** | 2.0.5 | API documentation (Swagger) |
| **Lombok** | Latest | Boilerplate reduction |
| **Maven** | 3.6+ | Build tool |

## 📂 Project Structure

```
multitenantsaas/
├── src/
│   ├── main/
│   │   ├── java/com/example/multitenantsaas/
│   │   │   ├── config/                # Configurations (MultiTenantConfig, SecurityConfig, etc.)
│   │   │   ├── controller/            # REST controllers (AuthController, TenantController, etc.)
│   │   │   ├── domain/                # Entities (master/Tenant, tenant/User, Item)
│   │   │   ├── repository/            # Repositories (master/TenantRepository, tenant/UserRepository)
│   │   │   ├── security/              # Security components (JwtUtil, TenantUserDetailsService, etc.)
│   │   │   ├── service/               # Services (TenantService for tenant creation)
│   │   │   └── MultitenantsaasApplication.java  # Application entry point
│   │   └── resources/
│   │       ├── application.properties  # Config (datasources, JWT, etc.)
│   │       └── db/migration/          # Flyway scripts (master/ and tenant/)
│   └── test/java/...                  # Tests (MultitenantsaasApplicationTests)
├── pom.xml                           # Maven dependencies
└── README.md                         # This file
```

## ⚙️ Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8+ (local or Docker)
- Git

1. **Clone the Repository**:
   ```
   git clone https://github.com/yourusername/multitenantsaas.git
   cd multitenantsaas
   ```

2. **Configure Database**:
   - Create a MySQL database: `master_db`.
   - Update `application.properties`:
     ```
     spring.datasource.url=jdbc:mysql://localhost:3306/master_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
     spring.datasource.username=root
     spring.datasource.password=root
     ```
   - Flyway will auto-migrate the master schema.

3. **Build & Run**:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
   - App runs on `http://localhost:8081`.

4. **Subdomain Testing**:
   - Edit `/etc/hosts` (or equivalent):
     ```
     127.0.0.1 tenant1.localhost
     127.0.0.1 tenant2.localhost
     ```
   - Use tools like ngrok for external testing.

## 📖 Usage

### Authentication
- **Master Login** (SUPERADMIN):
  ```
  POST http://localhost:8081/master/login
  {
    "username": "superadmin",
    "password": "superadmin"
  }
  ```
  Response: `{ "token": "eyJ..." }`

- **Tenant Login** (via subdomain):
  ```
  POST http://tenant1.localhost:8081/tenant/login
  {
    "username": "admin",
    "password": "admin"
  }
  ```

### Key Endpoints
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/master/tenants` | Create new tenant | SUPERADMIN |
| POST | `/master/users` | Create master user | SUPERADMIN |
| GET | `/master/users` | List master users | SUPERADMIN |
| POST | `/tenant/users` | Create tenant user | ADMIN |
| GET | `/tenant/users` | List tenant users | AUTHENTICATED |
| POST | `/tenant/items` | Create item | AUTHENTICATED |
| GET | `/tenant/items` | List items | AUTHENTICATED |

Explore full APIs at `http://localhost:8081/swagger-ui.html`.

### Creating a Tenant
As SUPERADMIN, POST to `/master/tenants`:
```
{
  "name": "TenantOne",
  "subdomain": "tenant1"
}
```
This creates `tenant1` database and migrates schema.

## 🔒 Security Notes
- **JWT**: Tokens include username, tenant, roles. Validate on each request.
- **Tenant Context**: Set/cleared per request to prevent leaks.
- **Production Tips**: Use HTTPS, strong `jwt.secret`, and environment variables for credentials.
- **Roles**: SUPERADMIN (master), ADMIN/USER (tenants).

## 🧪 Testing
```
mvn test
```
Includes basic context tests; add more for controllers/services.

## 🐛 Troubleshooting
- **Subdomain Errors**: Check hosts file or DNS setup.
- **DB Issues**: Ensure MySQL user has CREATE privileges.
- **JWT Failures**: Verify `jwt.secret` in properties.
- **Logs**: Use console output for debug info.

## 🤝 Contributing
1. Fork the repo.
2. Create a branch: `git checkout -b feature/xyz`.
3. Commit changes: `git commit -m "Add feature"`.
4. Push: `git push origin feature/xyz`.
5. Open a PR.

Follow Java conventions and add tests.

## 📄 License
Apache 2.0 - See [LICENSE](LICENSE) for details.

## 📧 Contact
- **Author**: Rajsekhar Acharya
- **Email**: rajsekhar.acharya@example.com (placeholder)
- **GitHub**: [https://github.com/yourusername/multitenantsaas](https://github.com/yourusername/multitenantsaas)

⭐ Star the repo if you find it useful!