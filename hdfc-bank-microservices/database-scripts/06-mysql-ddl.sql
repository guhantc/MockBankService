-- HDFC Bank Microservices - MySQL Database Schema
-- For production deployment with MySQL database

-- =====================================================
-- Customer Service Database (MySQL)
-- =====================================================

CREATE DATABASE IF NOT EXISTS hdfc_customer_db;
USE hdfc_customer_db;

-- Create customers table
DROP TABLE IF EXISTS customers;
CREATE TABLE customers (
    customer_id VARCHAR(20) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    pan_number VARCHAR(10) NOT NULL UNIQUE,
    aadhaar_number VARCHAR(12) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    pin_code VARCHAR(6) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    kyc_status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    annual_income DECIMAL(15,2) DEFAULT 0.00,
    occupation VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_customer_annual_income CHECK (annual_income >= 0),
    CONSTRAINT chk_customer_pan_format CHECK (pan_number REGEXP '^[A-Z]{5}[0-9]{4}[A-Z]{1}$'),
    CONSTRAINT chk_customer_aadhaar_format CHECK (LENGTH(aadhaar_number) = 12 AND aadhaar_number REGEXP '^[0-9]{12}$'),
    CONSTRAINT chk_customer_pin_code_format CHECK (LENGTH(pin_code) = 6 AND pin_code REGEXP '^[0-9]{6}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_phone ON customers(phone_number);
CREATE INDEX idx_customer_pan ON customers(pan_number);
CREATE INDEX idx_customer_aadhaar ON customers(aadhaar_number);
CREATE INDEX idx_customer_status ON customers(status);
CREATE INDEX idx_customer_kyc_status ON customers(kyc_status);
CREATE INDEX idx_customer_city_state ON customers(city, state);
CREATE INDEX idx_customer_created_at ON customers(created_at);

-- =====================================================
-- Account Service Database (MySQL)
-- =====================================================

CREATE DATABASE IF NOT EXISTS hdfc_account_db;
USE hdfc_account_db;

-- Create accounts table
DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'SALARY', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT') NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED', 'FROZEN') NOT NULL DEFAULT 'ACTIVE',
    interest_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    daily_transaction_limit DECIMAL(15,2) NOT NULL DEFAULT 50000.00,
    branch_code VARCHAR(10) NOT NULL,
    ifsc_code VARCHAR(15) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_transaction_date TIMESTAMP NULL,
    
    -- Constraints
    CONSTRAINT chk_account_balance CHECK (balance >= 0),
    CONSTRAINT chk_account_available_balance CHECK (available_balance >= 0),
    CONSTRAINT chk_account_interest_rate CHECK (interest_rate >= 0 AND interest_rate <= 100),
    CONSTRAINT chk_account_minimum_balance CHECK (minimum_balance >= 0),
    CONSTRAINT chk_account_daily_limit CHECK (daily_transaction_limit >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes
CREATE INDEX idx_account_customer_id ON accounts(customer_id);
CREATE INDEX idx_account_type ON accounts(account_type);
CREATE INDEX idx_account_status ON accounts(status);
CREATE INDEX idx_account_branch ON accounts(branch_code);
CREATE INDEX idx_account_ifsc ON accounts(ifsc_code);
CREATE INDEX idx_account_created_at ON accounts(created_at);
CREATE INDEX idx_account_customer_type ON accounts(customer_id, account_type);
CREATE INDEX idx_account_customer_status ON accounts(customer_id, status);

-- =====================================================
-- Transaction Service Database (MySQL)
-- =====================================================

CREATE DATABASE IF NOT EXISTS hdfc_transaction_db;
USE hdfc_transaction_db;

-- Create transactions table
DROP TABLE IF EXISTS transactions;
CREATE TABLE transactions (
    transaction_id VARCHAR(20) PRIMARY KEY,
    from_account VARCHAR(20) NOT NULL,
    to_account VARCHAR(20) NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT', 'REFUND') NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    description VARCHAR(255),
    reference_number VARCHAR(50),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP NULL,
    charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    from_account_balance DECIMAL(15,2) NULL,
    to_account_balance DECIMAL(15,2) NULL,
    failure_reason VARCHAR(500),
    branch_code VARCHAR(10),
    channel_used VARCHAR(20),
    device_id VARCHAR(50),
    ip_address VARCHAR(45),
    
    -- Constraints
    CONSTRAINT chk_transaction_amount CHECK (amount > 0),
    CONSTRAINT chk_transaction_charges CHECK (charges >= 0),
    CONSTRAINT chk_transaction_tax CHECK (tax >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes
CREATE INDEX idx_transaction_from_account ON transactions(from_account);
CREATE INDEX idx_transaction_to_account ON transactions(to_account);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_transaction_status ON transactions(status);
CREATE INDEX idx_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_transaction_processed_date ON transactions(processed_date);
CREATE INDEX idx_transaction_reference ON transactions(reference_number);
CREATE INDEX idx_transaction_branch ON transactions(branch_code);
CREATE INDEX idx_transaction_channel ON transactions(channel_used);
CREATE INDEX idx_transaction_from_account_date ON transactions(from_account, transaction_date);
CREATE INDEX idx_transaction_amount_range ON transactions(amount);

-- Create views
CREATE VIEW daily_transaction_summary AS
SELECT 
    DATE(transaction_date) as transaction_date,
    transaction_type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    SUM(charges) as total_charges,
    SUM(tax) as total_tax,
    AVG(amount) as average_amount
FROM transactions 
WHERE status = 'COMPLETED'
GROUP BY DATE(transaction_date), transaction_type;

CREATE VIEW account_transaction_history AS
SELECT 
    transaction_id,
    from_account as account_number,
    'DEBIT' as transaction_direction,
    amount,
    transaction_type,
    status,
    description,
    transaction_date,
    processed_date,
    charges,
    tax,
    from_account_balance as account_balance
FROM transactions
WHERE from_account IS NOT NULL

UNION ALL

SELECT 
    transaction_id,
    to_account as account_number,
    'CREDIT' as transaction_direction,
    amount,
    transaction_type,
    status,
    description,
    transaction_date,
    processed_date,
    0 as charges,
    0 as tax,
    to_account_balance as account_balance
FROM transactions
WHERE to_account IS NOT NULL;