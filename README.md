# Digital Wallet Application

A Spring Boot-based digital wallet application with multi-environment support (Development, Test, Production).

## 🏗️ Architecture Overview

This application supports three distinct environments:

- **Development (dev)**: Local development with in-memory H2 database and test data initialization
- **Test (test)**: Testing environment with in-memory H2 database and test data initialization  
- **Production (prod)**: Production environment with file-based H2 database and no test data

## 🚀 Quick Start

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Database Initialization

The application uses two SQL files for database setup:

2. **data.sql**: Inserts test data (only in dev/test environments)

### Environment-Specific Settings

| Setting | Development | Test | Production |
|---------|-------------|------|------------|
| Database | H2 in-memory | H2 in-memory | H2 file-based |
| Data Init | ✅ Yes | ✅ Yes | ❌ No |
| H2 Console | ✅ Enabled | ✅ Enabled | ❌ Disabled |
| Swagger | ✅ Enabled | ✅ Enabled | ❌ Disabled |
| SQL Logging | ✅ Yes | ✅ Yes | ❌ No |
| DDL Auto | create-drop | create-drop | update |

## 🔐 Security Configuration

### Default Test Users

The application comes with pre-configured test users (dev/test environments only):

1. **Employee User**
   - Username: `employee`
   - Password: `password`
   - Role: `EMPLOYEE`
   - TCKN: `11111111111`

2. **Customer User**
   - Username: `customer`
   - Password: `password`
   - Role: `CUSTOMER`
   - TCKN: `22222222222`
   - Wallets: Main TRY wallet and USD wallet (both with 0 balance)

### JWT Configuration

- Development: Long-lived test secret
- Test: Test-specific secret
- Production: Must be set via environment variable

## 📦 Docker Support

### Environment-Specific Docker Compose Files

- `docker-compose.dev.yml`: Development environment
- `docker-compose.test.yml`: Test environment  
- `docker-compose.prod.yml`: Production environment
- `docker-compose.yml`: Base configuration

### Building for Specific Environments

```bash
# Development
docker-compose -f docker-compose.dev.yml up --build

# Test
docker-compose -f docker-compose.test.yml up --build

# Production
docker-compose -f docker-compose.prod.yml up --build
```

## 🚀 CI/CD with GitHub Actions

The project includes automated CI/CD pipelines for all environments:

### Workflows

1. **CI Pipeline** (`ci.yml`)
   - Runs on push/PR to main/develop
   - Executes tests with test profile
   - Generates test reports

2. **Development Build** (`build-dev.yml`)
   - Triggers on push to `develop` branch
   - Builds Docker image for development
   - Stores artifacts for deployment

3. **Test Build** (`build-test.yml`)
   - Triggers on push to `test` branch
   - Builds Docker image for testing
   - Uses test-specific secrets

4. **Production Build** (`build-prod.yml`)
   - Triggers on push to `main` branch or version tags
   - Builds production Docker image
   - Includes security scanning with Trivy
   - Stores artifacts with longer retention

### Required GitHub Secrets

Set these secrets in your GitHub repository:

- `TEST_JWT_SECRET`: JWT secret for test environment
- `PROD_JWT_SECRET`: JWT secret for production environment
- `PROD_DB_PASSWORD`: Database password for production

### Environment Variables

Copy `.env.example` to `.env` and customize for your environment:

```bash
cp .env.example .env
```

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/furkan/digitalWallet/
│   └── resources/
│       ├── application.yaml              # Base configuration
│       ├── application-dev.yaml          # Development config
│       ├── application-test.yaml         # Test config
│       ├── application-prod.yaml         # Production config
│       └── data.sql                      # Test data
├── test/
└── ...
.github/
└── workflows/                            # GitHub Actions
docker-compose*.yml                       # Environment-specific Docker configs
run-*.sh                                  # Environment runner scripts
```

## 🔍 API Documentation

When enabled (dev/test environments), Swagger UI is available at:
- Development: http://localhost:8080/swagger-ui.html
- Test: http://localhost:8081/swagger-ui.html

## 🗄️ Database Access

H2 Console is available in dev/test environments:
- Development: http://localhost:8080/h2-console
- Test: http://localhost:8081/h2-console

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:digitalWallet` (dev) / `jdbc:h2:mem:testdb` (test)
- Username: `sa`
- Password: `password` (dev) / `` (empty for test)

## 🚨 Production Considerations

Before deploying to production:

1. ✅ Set strong `JWT_SECRET` environment variable
2. ✅ Set secure `DB_PASSWORD` environment variable  
3. ✅ Verify Swagger UI is disabled
4. ✅ Verify H2 Console is disabled
5. ✅ Ensure no test data initialization
6. ✅ Configure proper logging levels
7. ✅ Set up monitoring and health checks
8. ✅ Configure SSL/TLS termination
9. ✅ Set up database backups for persistent storage

## 📝 License

[Add your license information here]
