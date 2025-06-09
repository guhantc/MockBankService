# HDFC Bank Microservices - Database Scripts

This directory contains all the database scripts and configurations for the HDFC Bank Microservices application.

## 📁 Files Overview

### Core Database Scripts
- **01-customer-service-ddl.sql** - Customer Service database schema (H2)
- **02-account-service-ddl.sql** - Account Service database schema (H2)
- **03-transaction-service-ddl.sql** - Transaction Service database schema (H2)
- **04-sample-data.sql** - Sample data for all services

### MySQL Production Scripts
- **06-mysql-ddl.sql** - Complete MySQL schema for all services
- **07-mysql-application-properties.yml** - MySQL configuration for all services
- **08-docker-compose-mysql.yml** - Docker Compose with MySQL
- **09-setup-mysql-user.sql** - MySQL user and database setup

### Configuration Files
- **05-application-properties-persistent.yml** - Persistent H2 configuration

## 🚀 Quick Start

### Option 1: H2 In-Memory Database (Development)
Default configuration - no additional setup required. Data is lost when application stops.

### Option 2: H2 Persistent Database
1. Replace application.yml files with configurations from `05-application-properties-persistent.yml`
2. Create `data` directory in each service folder
3. Run the applications - databases will be created automatically

### Option 3: MySQL Database (Production)
1. Install MySQL 8.0
2. Run `09-setup-mysql-user.sql` as root user
3. Run `06-mysql-ddl.sql` to create tables
4. Run `04-sample-data.sql` to insert sample data
5. Replace application.yml files with configurations from `07-mysql-application-properties.yml`
6. Add MySQL dependency to pom.xml files

### Option 4: Docker with MySQL
1. Run: `docker-compose -f 08-docker-compose-mysql.yml up`
2. Access phpMyAdmin at http://localhost:8080
3. Access services:
   - Eureka: http://localhost:8761
   - Customer Service: http://localhost:8081
   - Account Service: http://localhost:8082
   - Transaction Service: http://localhost:8083

## 📊 Database Schema Details

### Customer Service (customerdb)
- **customers** table with comprehensive customer information
- Banking compliance validations (PAN, Aadhaar)
- KYC status tracking
- Customer lifecycle management

### Account Service (accountdb)
- **accounts** table with multiple account types
- Real banking business logic
- Balance and transaction limit management
- Account status tracking

### Transaction Service (transactiondb)
- **transactions** table with complete transaction history
- Banking charges and tax calculations
- Transaction status and audit trail
- Multiple views for reporting

## 🔐 Security Configuration

### Default Credentials
- **H2 Console**: username=`sa`, password=`hdfc@2024`
- **MySQL**: username=`hdfc_user`, password=`hdfc@2024`

### Database URLs
- **H2 Console**: http://localhost:8081/h2-console (Customer), http://localhost:8082/h2-console (Account)
- **phpMyAdmin**: http://localhost:8080 (MySQL only)

## 🏗️ Schema Features

### Banking Validations
- PAN number format validation
- Aadhaar number length validation
- Age verification (18+ years)
- Email and phone uniqueness
- Balance and limit checks

### Performance Optimizations
- Strategic indexes on frequently queried columns
- Composite indexes for multi-column queries
- Optimized foreign key relationships

### Audit Trail
- Created/Updated timestamps
- Transaction history tracking
- Balance snapshot in transactions
- User action logging

## 📝 Sample Data

The sample data includes:
- 5 customers with different KYC statuses
- 7 accounts of various types (Savings, Current, Salary, FD)
- 10 transactions showing different scenarios
- Realistic Indian banking data (Mumbai, Delhi, Bangalore, etc.)

## 🔄 Migration Guide

### From H2 to MySQL
1. Export data from H2 console
2. Set up MySQL using provided scripts
3. Import data using MySQL Workbench or command line
4. Update application configurations

### Database Backup
```sql
-- H2 Backup
SCRIPT TO 'backup.sql';

-- MySQL Backup
mysqldump -u hdfc_user -p hdfc_customer_db > customer_backup.sql
mysqldump -u hdfc_user -p hdfc_account_db > account_backup.sql
mysqldump -u hdfc_user -p hdfc_transaction_db > transaction_backup.sql
```

## 🔧 Troubleshooting

### Common Issues
1. **Connection refused**: Check if database service is running
2. **Access denied**: Verify credentials and user permissions
3. **Table doesn't exist**: Run DDL scripts first
4. **Port conflicts**: Change ports in configuration files

### Verification Commands
```sql
-- Check tables
SHOW TABLES;

-- Check data
SELECT COUNT(*) FROM customers;
SELECT COUNT(*) FROM accounts;
SELECT COUNT(*) FROM transactions;

-- Check relationships
SELECT c.customer_id, c.first_name, a.account_number, a.account_type 
FROM customers c 
JOIN accounts a ON c.customer_id = a.customer_id;
```

## 📈 Performance Monitoring

### Key Metrics to Monitor
- Query execution time
- Connection pool usage
- Transaction throughput
- Index efficiency

### Optimization Tips
- Use EXPLAIN to analyze query plans
- Monitor slow query logs
- Regularly update table statistics
- Consider partitioning for large tables

---

For technical support or questions, refer to the main application documentation.