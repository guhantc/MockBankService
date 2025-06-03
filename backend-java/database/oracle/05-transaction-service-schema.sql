-- HDFC Bank Transaction Service Database Schema
-- Connect as hdfc_transaction

-- Transactions table
CREATE TABLE transactions (
    id NUMBER(19) PRIMARY KEY,
    transaction_id VARCHAR2(50) UNIQUE NOT NULL,
    source_account_id VARCHAR2(50),
    target_account_id VARCHAR2(50),
    amount NUMBER(15,2) NOT NULL,
    currency VARCHAR2(3) DEFAULT 'INR',
    transaction_type VARCHAR2(20) NOT NULL CHECK (transaction_type IN ('CREDIT', 'DEBIT', 'TRANSFER', 'NEFT', 'RTGS', 'IMPS', 'UPI', 'CHEQUE', 'CASH')),
    transaction_mode VARCHAR2(20) DEFAULT 'ONLINE' CHECK (transaction_mode IN ('ONLINE', 'ATM', 'BRANCH', 'MOBILE', 'POS')),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REVERSED')),
    reference_number VARCHAR2(50) UNIQUE,
    description VARCHAR2(500),
    remarks VARCHAR2(1000),
    fees NUMBER(10,2) DEFAULT 0.00,
    tax NUMBER(10,2) DEFAULT 0.00,
    total_amount NUMBER(15,2),
    beneficiary_name VARCHAR2(255),
    beneficiary_ifsc VARCHAR2(11),
    beneficiary_account VARCHAR2(20),
    initiator_user_id VARCHAR2(50),
    approver_user_id VARCHAR2(50),
    channel VARCHAR2(20) DEFAULT 'WEB',
    device_info VARCHAR2(500),
    ip_address VARCHAR2(45),
    location_info VARCHAR2(255),
    scheduled_date TIMESTAMP,
    processed_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Transaction limits
CREATE TABLE transaction_limits (
    id NUMBER(19) PRIMARY KEY,
    account_id VARCHAR2(50) NOT NULL,
    transaction_type VARCHAR2(20) NOT NULL,
    daily_limit NUMBER(15,2) NOT NULL,
    monthly_limit NUMBER(15,2) NOT NULL,
    per_transaction_limit NUMBER(15,2) NOT NULL,
    daily_count_limit NUMBER(5) DEFAULT 50,
    monthly_count_limit NUMBER(10) DEFAULT 1000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Transaction audit log
CREATE TABLE transaction_audit (
    id NUMBER(19) PRIMARY KEY,
    transaction_id VARCHAR2(50) NOT NULL,
    old_status VARCHAR2(20),
    new_status VARCHAR2(20),
    changed_by VARCHAR2(50),
    change_reason VARCHAR2(500),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    additional_info CLOB
) TABLESPACE hdfc_data;

-- Recurring transactions
CREATE TABLE recurring_transactions (
    id NUMBER(19) PRIMARY KEY,
    recurring_id VARCHAR2(50) UNIQUE NOT NULL,
    source_account_id VARCHAR2(50) NOT NULL,
    target_account_id VARCHAR2(50),
    amount NUMBER(15,2) NOT NULL,
    transaction_type VARCHAR2(20) NOT NULL,
    frequency VARCHAR2(20) NOT NULL CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    last_execution_date DATE,
    total_executions NUMBER(10) DEFAULT 0,
    max_executions NUMBER(10),
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'COMPLETED', 'CANCELLED')),
    description VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Transaction notifications
CREATE TABLE transaction_notifications (
    id NUMBER(19) PRIMARY KEY,
    transaction_id VARCHAR2(50) NOT NULL,
    notification_type VARCHAR2(20) NOT NULL CHECK (notification_type IN ('SMS', 'EMAIL', 'PUSH')),
    recipient VARCHAR2(255) NOT NULL,
    message_content CLOB,
    sent_status VARCHAR2(20) DEFAULT 'PENDING' CHECK (sent_status IN ('PENDING', 'SENT', 'FAILED', 'DELIVERED')),
    sent_at TIMESTAMP,
    delivery_status VARCHAR2(20),
    delivery_time TIMESTAMP,
    retry_count NUMBER(2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_txn_notif_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
) TABLESPACE hdfc_data;

-- Transaction disputes
CREATE TABLE transaction_disputes (
    id NUMBER(19) PRIMARY KEY,
    dispute_id VARCHAR2(50) UNIQUE NOT NULL,
    transaction_id VARCHAR2(50) NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    dispute_type VARCHAR2(50) NOT NULL,
    dispute_reason VARCHAR2(1000) NOT NULL,
    disputed_amount NUMBER(15,2),
    status VARCHAR2(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED', 'REJECTED', 'ESCALATED')),
    priority VARCHAR2(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    assigned_to VARCHAR2(50),
    resolution_notes CLOB,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dispute_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE transaction_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE txn_limit_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE txn_audit_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE recurring_txn_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE txn_notif_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE dispute_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_txn_source_account ON transactions(source_account_id) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_target_account ON transactions(target_account_id) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_status ON transactions(status) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_type ON transactions(transaction_type) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_created_at ON transactions(created_at) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_reference ON transactions(reference_number) TABLESPACE hdfc_index;
CREATE INDEX idx_txn_scheduled ON transactions(scheduled_date) TABLESPACE hdfc_index;
CREATE INDEX idx_recurring_next_exec ON recurring_transactions(next_execution_date) TABLESPACE hdfc_index;
CREATE INDEX idx_dispute_status ON transaction_disputes(status) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_transactions_id
    BEFORE INSERT ON transactions
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := transaction_seq.NEXTVAL;
    END IF;
    IF :NEW.transaction_id IS NULL THEN
        :NEW.transaction_id := 'TXN' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(transaction_seq.CURRVAL, 8, '0');
    END IF;
    IF :NEW.reference_number IS NULL THEN
        :NEW.reference_number := 'REF' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(transaction_seq.CURRVAL, 8, '0');
    END IF;
    :NEW.total_amount := :NEW.amount + NVL(:NEW.fees, 0) + NVL(:NEW.tax, 0);
END;
/

CREATE OR REPLACE TRIGGER trg_transactions_updated_at
    BEFORE UPDATE ON transactions
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- Log status changes
    IF :NEW.status != :OLD.status THEN
        INSERT INTO transaction_audit (
            transaction_id, old_status, new_status, 
            changed_by, change_reason, changed_at
        ) VALUES (
            :NEW.transaction_id, :OLD.status, :NEW.status,
            USER, 'Status changed via update', CURRENT_TIMESTAMP
        );
    END IF;
    
    -- Set processed date when status becomes COMPLETED
    IF :NEW.status = 'COMPLETED' AND :OLD.status != 'COMPLETED' THEN
        :NEW.processed_date := CURRENT_TIMESTAMP;
    END IF;
END;
/

-- Function to generate unique transaction reference
CREATE OR REPLACE FUNCTION generate_txn_reference
RETURN VARCHAR2
IS
    v_ref VARCHAR2(50);
BEGIN
    SELECT 'HDFC' || TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS') || LPAD(transaction_seq.NEXTVAL, 6, '0')
    INTO v_ref
    FROM dual;
    
    RETURN v_ref;
END;
/