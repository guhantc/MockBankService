-- HDFC Bank Customer Service Database Schema
-- Database: customerdb (H2)

-- Drop tables if they exist (for fresh setup)
DROP TABLE IF EXISTS customers;

-- Create customers table
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
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    annual_income DECIMAL(15,2),
    occupation VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED')),
    CONSTRAINT chk_kyc_status CHECK (kyc_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'EXPIRED')),
    CONSTRAINT chk_annual_income CHECK (annual_income >= 0),
    CONSTRAINT chk_pan_format CHECK (pan_number REGEXP '[A-Z]{5}[0-9]{4}[A-Z]{1}'),
    CONSTRAINT chk_aadhaar_format CHECK (LENGTH(aadhaar_number) = 12 AND aadhaar_number REGEXP '[0-9]{12}'),
    CONSTRAINT chk_pin_code_format CHECK (LENGTH(pin_code) = 6 AND pin_code REGEXP '[0-9]{6}')
);

-- Create indexes for better performance
CREATE INDEX idx_customer_email ON customers(email);
CREATE INDEX idx_customer_phone ON customers(phone_number);
CREATE INDEX idx_customer_pan ON customers(pan_number);
CREATE INDEX idx_customer_aadhaar ON customers(aadhaar_number);
CREATE INDEX idx_customer_status ON customers(status);
CREATE INDEX idx_customer_kyc_status ON customers(kyc_status);
CREATE INDEX idx_customer_city_state ON customers(city, state);
CREATE INDEX idx_customer_created_at ON customers(created_at);

-- Create triggers for updated_at timestamp
CREATE TRIGGER trg_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    SET NEW.updated_at = CURRENT_TIMESTAMP;