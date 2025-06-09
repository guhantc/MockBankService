-- HDFC Bank Account Service Database Schema
-- Database: accountdb (H2)

-- Drop tables if they exist (for fresh setup)
DROP TABLE IF EXISTS accounts;

-- Create accounts table
CREATE TABLE accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(20) NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    interest_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    daily_transaction_limit DECIMAL(15,2) NOT NULL DEFAULT 50000.00,
    branch_code VARCHAR(10) NOT NULL,
    ifsc_code VARCHAR(15) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_transaction_date TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_account_type CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT')),
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'CLOSED', 'FROZEN')),
    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT chk_available_balance CHECK (available_balance >= 0),
    CONSTRAINT chk_interest_rate CHECK (interest_rate >= 0 AND interest_rate <= 100),
    CONSTRAINT chk_minimum_balance CHECK (minimum_balance >= 0),
    CONSTRAINT chk_daily_limit CHECK (daily_transaction_limit >= 0),
    CONSTRAINT chk_balance_minimum CHECK (balance >= minimum_balance OR status != 'ACTIVE')
);

-- Create indexes for better performance
CREATE INDEX idx_account_customer_id ON accounts(customer_id);
CREATE INDEX idx_account_type ON accounts(account_type);
CREATE INDEX idx_account_status ON accounts(status);
CREATE INDEX idx_account_branch ON accounts(branch_code);
CREATE INDEX idx_account_ifsc ON accounts(ifsc_code);
CREATE INDEX idx_account_created_at ON accounts(created_at);
CREATE INDEX idx_account_customer_type ON accounts(customer_id, account_type);
CREATE INDEX idx_account_customer_status ON accounts(customer_id, status);

-- Create triggers for updated_at timestamp
CREATE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    SET NEW.updated_at = CURRENT_TIMESTAMP;

-- Create trigger to update last_transaction_date when balance changes
CREATE TRIGGER trg_accounts_last_transaction
    BEFORE UPDATE ON accounts
    FOR EACH ROW
    WHEN NEW.balance != OLD.balance
    SET NEW.last_transaction_date = CURRENT_TIMESTAMP;