-- HDFC Bank Transaction Service Database Schema
-- Database: transactiondb (H2)

-- Drop tables if they exist (for fresh setup)
DROP TABLE IF EXISTS transactions;

-- Create transactions table
CREATE TABLE transactions (
    transaction_id VARCHAR(20) PRIMARY KEY,
    from_account VARCHAR(20) NOT NULL,
    to_account VARCHAR(20),
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description VARCHAR(255),
    reference_number VARCHAR(50),
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date TIMESTAMP,
    charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    from_account_balance DECIMAL(15,2),
    to_account_balance DECIMAL(15,2),
    failure_reason VARCHAR(500),
    branch_code VARCHAR(10),
    channel_used VARCHAR(20),
    device_id VARCHAR(50),
    ip_address VARCHAR(45),
    
    -- Constraints
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER', 'PAYMENT', 'REFUND')),
    CONSTRAINT chk_transaction_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_charges CHECK (charges >= 0),
    CONSTRAINT chk_tax CHECK (tax >= 0),
    CONSTRAINT chk_to_account_for_transfer CHECK (
        (transaction_type = 'TRANSFER' AND to_account IS NOT NULL) OR 
        (transaction_type != 'TRANSFER')
    )
);

-- Create indexes for better performance
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

-- Create view for daily transaction summary
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

-- Create view for account transaction history
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