-- HDFC Bank Account Service Database Schema
-- Connect as hdfc_account

-- Accounts table
CREATE TABLE accounts (
    id NUMBER(19) PRIMARY KEY,
    account_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    account_number VARCHAR2(20) UNIQUE NOT NULL,
    account_type VARCHAR2(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CURRENT', 'SALARY', 'NRI', 'CORPORATE')),
    balance NUMBER(15,2) DEFAULT 0.00,
    available_balance NUMBER(15,2) DEFAULT 0.00,
    branch_code VARCHAR2(10) NOT NULL,
    ifsc_code VARCHAR2(11) NOT NULL,
    micr_code VARCHAR2(9),
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED', 'DORMANT')),
    currency VARCHAR2(3) DEFAULT 'INR',
    minimum_balance NUMBER(10,2) DEFAULT 1000.00,
    overdraft_limit NUMBER(15,2) DEFAULT 0.00,
    interest_rate NUMBER(5,4) DEFAULT 4.0000,
    opening_date DATE DEFAULT SYSDATE,
    closing_date DATE,
    last_transaction_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Account holders (for joint accounts)
CREATE TABLE account_holders (
    id NUMBER(19) PRIMARY KEY,
    account_id VARCHAR2(50) NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    holder_type VARCHAR2(20) DEFAULT 'PRIMARY' CHECK (holder_type IN ('PRIMARY', 'SECONDARY', 'JOINT', 'NOMINEE')),
    relationship VARCHAR2(50),
    percentage NUMBER(5,2) DEFAULT 100.00,
    can_operate CHAR(1) DEFAULT 'Y' CHECK (can_operate IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_acc_holder_account FOREIGN KEY (account_id) REFERENCES accounts(account_id),
    CONSTRAINT uk_account_user UNIQUE (account_id, user_id)
) TABLESPACE hdfc_data;

-- Account statements
CREATE TABLE account_statements (
    id NUMBER(19) PRIMARY KEY,
    account_id VARCHAR2(50) NOT NULL,
    statement_date DATE NOT NULL,
    opening_balance NUMBER(15,2) NOT NULL,
    closing_balance NUMBER(15,2) NOT NULL,
    total_credits NUMBER(15,2) DEFAULT 0.00,
    total_debits NUMBER(15,2) DEFAULT 0.00,
    transaction_count NUMBER(10) DEFAULT 0,
    statement_file_path VARCHAR2(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_statement_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
) TABLESPACE hdfc_data;

-- Account limits and preferences
CREATE TABLE account_limits (
    id NUMBER(19) PRIMARY KEY,
    account_id VARCHAR2(50) NOT NULL,
    daily_transaction_limit NUMBER(15,2) DEFAULT 100000.00,
    monthly_transaction_limit NUMBER(15,2) DEFAULT 1000000.00,
    daily_withdrawal_limit NUMBER(15,2) DEFAULT 50000.00,
    monthly_withdrawal_limit NUMBER(15,2) DEFAULT 500000.00,
    online_transaction_limit NUMBER(15,2) DEFAULT 25000.00,
    pos_transaction_limit NUMBER(15,2) DEFAULT 75000.00,
    international_transaction CHAR(1) DEFAULT 'N' CHECK (international_transaction IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_limits_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
) TABLESPACE hdfc_data;

-- Standing instructions
CREATE TABLE standing_instructions (
    id NUMBER(19) PRIMARY KEY,
    instruction_id VARCHAR2(50) UNIQUE NOT NULL,
    account_id VARCHAR2(50) NOT NULL,
    instruction_type VARCHAR2(20) NOT NULL CHECK (instruction_type IN ('SIP', 'EMI', 'UTILITY', 'TRANSFER')),
    target_account VARCHAR2(50),
    amount NUMBER(15,2) NOT NULL,
    frequency VARCHAR2(20) NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'COMPLETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_si_account FOREIGN KEY (account_id) REFERENCES accounts(account_id)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE account_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE account_holder_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE statement_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE account_limit_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE standing_instruction_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_accounts_user_id ON accounts(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_accounts_account_number ON accounts(account_number) TABLESPACE hdfc_index;
CREATE INDEX idx_accounts_status ON accounts(status) TABLESPACE hdfc_index;
CREATE INDEX idx_accounts_type ON accounts(account_type) TABLESPACE hdfc_index;
CREATE INDEX idx_accounts_branch ON accounts(branch_code) TABLESPACE hdfc_index;
CREATE INDEX idx_statements_account_date ON account_statements(account_id, statement_date) TABLESPACE hdfc_index;
CREATE INDEX idx_si_execution_date ON standing_instructions(next_execution_date) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_accounts_id
    BEFORE INSERT ON accounts
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := account_seq.NEXTVAL;
    END IF;
    IF :NEW.account_id IS NULL THEN
        :NEW.account_id := 'ACC' || LPAD(account_seq.CURRVAL, 10, '0');
    END IF;
    IF :NEW.account_number IS NULL THEN
        :NEW.account_number := :NEW.branch_code || LPAD(account_seq.CURRVAL, 8, '0');
    END IF;
    :NEW.available_balance := :NEW.balance;
END;
/

CREATE OR REPLACE TRIGGER trg_accounts_updated_at
    BEFORE UPDATE ON accounts
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
    IF :NEW.balance != :OLD.balance THEN
        :NEW.last_transaction_date := CURRENT_TIMESTAMP;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_account_holder_id
    BEFORE INSERT ON account_holders
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := account_holder_seq.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_statement_id
    BEFORE INSERT ON account_statements
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := statement_seq.NEXTVAL;
    END IF;
END;
/