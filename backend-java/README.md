# HDFC Bank Microservices Platform

A comprehensive banking microservices platform built with Spring Boot, implementing advanced patterns like SAGA for distributed transactions, service discovery with Eureka, and comprehensive monitoring.

## 🏗️ Architecture Overview

### Core Components

1. **Service Discovery** - Eureka Server for service registration and discovery
2. **API Gateway** - Single entry point with authentication, rate limiting, and routing
3. **SAGA Orchestrator** - Manages distributed transactions with compensation activities
4. **Core Banking Services** - User, Account, Loan, Card, Investment, Transaction, and Notification services
5. **Data Layer** - Oracle Database with Redis for caching
6. **Monitoring** - Prometheus and Grafana for observability

### Design Patterns Implemented

- **SAGA Pattern** - Ensures data consistency across microservices with compensation activities
- **Circuit Breaker** - Prevents cascade failures
- **API Gateway Pattern** - Centralized routing and cross-cutting concerns
- **Service Discovery** - Dynamic service registration and lookup
- **Event Sourcing** - For audit trails and transaction history
- **CQRS** - Command Query Responsibility Segregation for scalability

## 🚀 Technology Stack

### Backend Technologies
- **Java 21** - Latest LTS version with virtual threads
- **Spring Boot 3.2.0** - Main framework
- **Spring WebFlux** - Reactive programming support
- **Spring Cloud** - Microservices infrastructure
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer

### Infrastructure
- **Oracle Database 21c** - Primary database
- **Redis 7.2** - Caching and session management
- **Docker** - Containerization
- **Kubernetes** - Container orchestration
- **Prometheus & Grafana** - Monitoring and observability

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **TestContainers** - Database testing

## 📋 Services Overview

### 1. Eureka Server (Port: 8761)
- Service discovery and registration
- Health monitoring of microservices
- Load balancing support

### 2. API Gateway (Port: 8080)
- Single entry point for all client requests
- JWT authentication and authorization
- Request routing and load balancing
- Rate limiting and throttling
- Request/response transformation

### 3. User Service (Port: 8081)
- User registration and authentication
- Profile management
- JWT token generation
- Password security and validation

### 4. Account Service (Port: 8082)
- Account creation and management
- Balance inquiries and updates
- Account types (Savings, Current, etc.)
- Account statements

### 5. Loan Service (Port: 8083)
- Loan application processing
- Loan approval workflow
- EMI calculations
- Loan disbursement

### 6. Card Service (Port: 8084)
- Credit/Debit card management
- Card activation and blocking
- Transaction limits
- Reward points management

### 7. Investment Service (Port: 8085)
- Mutual fund investments
- Fixed deposits
- Insurance products
- Portfolio management

### 8. Transaction Service (Port: 8086)
- Money transfers (NEFT, RTGS, IMPS)
- Bill payments
- Transaction history
- Transaction limits and validation

### 9. Notification Service (Port: 8087)
- SMS and email notifications
- Push notifications
- Transaction alerts
- Account activity notifications

### 10. SAGA Orchestrator (Port: 8090)
- Manages distributed transactions
- Implements compensation activities
- Ensures data consistency
- Transaction monitoring and recovery

## 🎯 SAGA Patterns Implemented

### 1. Account Opening SAGA
**Steps:**
1. Create User → 2. Create Account → 3. Issue Debit Card → 4. Send Welcome Notification

**Compensation:**
- If any step fails, previous steps are rolled back in reverse order

### 2. Loan Processing SAGA
**Steps:**
1. Validate Application → 2. Credit Check → 3. Approve Loan → 4. Disburse Funds → 5. Update Account

**Compensation:**
- Complex compensation with partial rollbacks and manual intervention points

### 3. Fund Transfer SAGA
**Steps:**
1. Validate Transfer → 2. Debit Source Account → 3. Credit Target Account → 4. Send Notifications

**Compensation:**
- Automatic reversal of debits if credit fails

## 🛠️ Development Setup

### Prerequisites
- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Kubernetes cluster (for production deployment)
- Oracle Database 21c (or Docker container)

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd hdfc-bank-microservices
```

2. **Build all services**
```bash
./scripts/build-all.sh
```

3. **Start with Docker Compose**
```bash
docker-compose -f docker/docker-compose.yml up -d
```

4. **Verify services**
```bash
# Check Eureka Dashboard
http://localhost:8761

# Check API Gateway
http://localhost:8080/actuator/health

# Check all services health
docker-compose ps
```

## 🐳 Docker Deployment

### Build Docker Images
```bash
# Build all services
./scripts/build-all.sh

# Or build individual service
cd user-service
mvn clean package -DskipTests
docker build -f ../docker/Dockerfile.user-service -t hdfc-bank/user-service:1.0.0 .
```

### Run with Docker Compose
```bash
# Start all services
docker-compose -f docker/docker-compose.yml up -d

# View logs
docker-compose -f docker/docker-compose.yml logs -f

# Stop all services
docker-compose -f docker/docker-compose.yml down
```

## ☸️ Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (1.25+)
- kubectl configured
- Helm 3.x (optional)

### Deploy to Kubernetes
```bash
# Deploy all components
./scripts/deploy-kubernetes.sh

# Or deploy manually
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/oracle-database-deployment.yaml
kubectl apply -f kubernetes/redis-deployment.yaml
kubectl apply -f kubernetes/eureka-server-deployment.yaml
kubectl apply -f kubernetes/api-gateway-deployment.yaml
# ... continue with other services
```

### Access Services
```bash
# Port forward API Gateway
kubectl port-forward svc/api-gateway -n hdfc-bank 8080:80

# Access Prometheus
kubectl port-forward svc/prometheus -n hdfc-bank 9090:9090

# Access Grafana
kubectl port-forward svc/grafana -n hdfc-bank 3000:3000
```

## 🧪 Testing

### Unit Tests
```bash
# Run tests for all services
./scripts/test-all.sh

# Run tests for specific service
cd user-service
mvn test
```

### Integration Tests
```bash
# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

### Load Testing
```bash
# Use provided JMeter scripts
jmeter -n -t tests/load-tests/api-gateway-load-test.jmx
```

## 📊 Monitoring & Observability

### Metrics
- **Prometheus** - Metrics collection
- **Grafana** - Visualization and dashboards
- **Custom metrics** - Business KPIs and SLAs

### Logging
- **Structured logging** with JSON format
- **Centralized logging** with ELK stack (optional)
- **Correlation IDs** for request tracing

### Health Checks
- **Spring Actuator** endpoints
- **Kubernetes liveness/readiness** probes
- **Custom health indicators**

## 🔒 Security

### Authentication & Authorization
- **JWT tokens** with RS256 signing
- **Role-based access control** (RBAC)
- **API rate limiting** and throttling
- **CORS** configuration

### Data Security
- **Password encryption** with BCrypt
- **PII data encryption** at rest
- **TLS/SSL** for all communications
- **API key management**

## 🚀 Performance

### Optimization Techniques
- **Connection pooling** for databases
- **Redis caching** for frequently accessed data
- **Reactive programming** with WebFlux
- **Database indexing** strategies
- **CDN** for static content

### Scalability
- **Horizontal scaling** with Kubernetes
- **Auto-scaling** based on metrics
- **Load balancing** across instances
- **Circuit breakers** for fault tolerance

## 📈 API Documentation

### Swagger/OpenAPI
Each service exposes Swagger documentation at:
```
http://<service-host>:<port>/swagger-ui.html
```

### API Gateway Routes
```
GET    /api/users/{id}           - Get user details
POST   /api/users/register       - Register new user
POST   /api/auth/login          - User login
GET    /api/accounts/{userId}    - Get user accounts
POST   /api/accounts            - Create new account
POST   /api/transactions/transfer - Transfer funds
GET    /api/loans/{userId}       - Get user loans
POST   /api/loans/apply         - Apply for loan
```

## 🤝 Contributing

### Development Guidelines
1. Follow Spring Boot best practices
2. Write comprehensive tests (minimum 80% coverage)
3. Use conventional commit messages
4. Update documentation for API changes
5. Run all tests before submitting PRs

### Code Quality
- **SonarQube** integration for code quality
- **SpotBugs** for bug detection
- **Checkstyle** for code formatting
- **PMD** for code analysis

## 📞 Support

### Documentation
- [API Documentation](./docs/api.md)
- [Deployment Guide](./docs/deployment.md)
- [Troubleshooting](./docs/troubleshooting.md)

### Contact
- **Development Team**: dev@hdfcbank.com
- **Operations Team**: ops@hdfcbank.com
- **Security Team**: security@hdfcbank.com

## 📄 License

This project is proprietary and confidential. All rights reserved by HDFC Bank Ltd.

---

## 🎯 Quick Start Commands

```bash
# Development
./scripts/build-all.sh
docker-compose -f docker/docker-compose.yml up -d

# Production
./scripts/deploy-kubernetes.sh

# Monitoring
kubectl port-forward svc/grafana -n hdfc-bank 3000:3000

# Logs
kubectl logs -f deployment/api-gateway -n hdfc-bank
```