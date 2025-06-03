-- HDFC Bank Card Service Database Schema
-- Connect as hdfc_card

-- Cards table
CREATE TABLE cards (
    id NUMBER(19) PRIMARY KEY,
    card_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    account_id VARCHAR2(50) NOT NULL,
    card_number VARCHAR2(20) UNIQUE NOT NULL,
    masked_card_number VARCHAR2(20),
    card_type VARCHAR2(20) NOT NULL CHECK (card_type IN ('CREDIT_CARD', 'DEBIT_CARD', 'PREPAID_CARD', 'FOREX_CARD')),
    card_category VARCHAR2(30) DEFAULT 'CLASSIC' CHECK (card_category IN ('CLASSIC', 'GOLD', 'PLATINUM', 'SIGNATURE', 'INFINITE')),
    card_brand VARCHAR2(20) DEFAULT 'HDFC' CHECK (card_brand IN ('HDFC', 'VISA', 'MASTERCARD', 'RUPAY', 'AMEX')),
    credit_limit NUMBER(15,2) DEFAULT 0.00,
    available_limit NUMBER(15,2) DEFAULT 0.00,
    cash_limit NUMBER(15,2) DEFAULT 0.00,
    international_usage CHAR(1) DEFAULT 'Y' CHECK (international_usage IN ('Y', 'N')),
    contactless_enabled CHAR(1) DEFAULT 'Y' CHECK (contactless_enabled IN ('Y', 'N')),
    online_usage CHAR(1) DEFAULT 'Y' CHECK (online_usage IN ('Y', 'N')),
    issue_date DATE DEFAULT SYSDATE,
    expiry_date DATE NOT NULL,
    activation_date DATE,
    last_used_date TIMESTAMP,
    pin_set CHAR(1) DEFAULT 'N' CHECK (pin_set IN ('Y', 'N')),
    pin_hash VARCHAR2(255),
    cvv_hash VARCHAR2(255),
    status VARCHAR2(20) DEFAULT 'INACTIVE' CHECK (status IN ('INACTIVE', 'ACTIVE', 'BLOCKED', 'EXPIRED', 'CANCELLED', 'LOST', 'STOLEN')),
    block_reason VARCHAR2(500),
    annual_fee NUMBER(10,2) DEFAULT 0.00,
    annual_fee_waived CHAR(1) DEFAULT 'N' CHECK (annual_fee_waived IN ('Y', 'N')),
    reward_points NUMBER(10) DEFAULT 0,
    cashback_earned NUMBER(10,2) DEFAULT 0.00,
    statement_date NUMBER(2) DEFAULT 1,
    payment_due_date NUMBER(2) DEFAULT 20,
    minimum_payment_percentage NUMBER(5,2) DEFAULT 5.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Card transactions
CREATE TABLE card_transactions (
    id NUMBER(19) PRIMARY KEY,
    transaction_id VARCHAR2(50) UNIQUE NOT NULL,
    card_id VARCHAR2(50) NOT NULL,
    merchant_name VARCHAR2(255),
    merchant_category VARCHAR2(100),
    merchant_id VARCHAR2(50),
    terminal_id VARCHAR2(50),
    amount NUMBER(15,2) NOT NULL,
    currency VARCHAR2(3) DEFAULT 'INR',
    conversion_rate NUMBER(10,6) DEFAULT 1.000000,
    local_amount NUMBER(15,2),
    transaction_type VARCHAR2(20) NOT NULL CHECK (transaction_type IN ('PURCHASE', 'CASH_WITHDRAWAL', 'REFUND', 'REVERSAL', 'FEE', 'INTEREST')),
    transaction_mode VARCHAR2(20) DEFAULT 'POS' CHECK (transaction_mode IN ('POS', 'ATM', 'ONLINE', 'CONTACTLESS', 'MANUAL')),
    auth_code VARCHAR2(20),
    rrn VARCHAR2(50),
    approval_code VARCHAR2(20),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED', 'REVERSED', 'SETTLED')),
    decline_reason VARCHAR2(255),
    mcc_code VARCHAR2(10),
    country_code VARCHAR2(3),
    city VARCHAR2(100),
    reward_points_earned NUMBER(10) DEFAULT 0,
    cashback_earned NUMBER(10,2) DEFAULT 0.00,
    fees_charged NUMBER(10,2) DEFAULT 0.00,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    settlement_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_txn_card FOREIGN KEY (card_id) REFERENCES cards(card_id)
) TABLESPACE hdfc_data;

-- Card statements
CREATE TABLE card_statements (
    id NUMBER(19) PRIMARY KEY,
    statement_id VARCHAR2(50) UNIQUE NOT NULL,
    card_id VARCHAR2(50) NOT NULL,
    statement_date DATE NOT NULL,
    due_date DATE NOT NULL,
    previous_balance NUMBER(15,2) DEFAULT 0.00,
    current_balance NUMBER(15,2) DEFAULT 0.00,
    minimum_amount_due NUMBER(15,2) DEFAULT 0.00,
    total_amount_due NUMBER(15,2) DEFAULT 0.00,
    payment_received NUMBER(15,2) DEFAULT 0.00,
    new_purchases NUMBER(15,2) DEFAULT 0.00,
    cash_advances NUMBER(15,2) DEFAULT 0.00,
    fees_charged NUMBER(15,2) DEFAULT 0.00,
    interest_charged NUMBER(15,2) DEFAULT 0.00,
    reward_points_earned NUMBER(10) DEFAULT 0,
    reward_points_redeemed NUMBER(10) DEFAULT 0,
    cashback_earned NUMBER(10,2) DEFAULT 0.00,
    credit_limit NUMBER(15,2),
    available_credit NUMBER(15,2),
    overlimit_amount NUMBER(15,2) DEFAULT 0.00,
    days_past_due NUMBER(5) DEFAULT 0,
    late_fee NUMBER(10,2) DEFAULT 0.00,
    statement_file_path VARCHAR2(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_stmt_card FOREIGN KEY (card_id) REFERENCES cards(card_id)
) TABLESPACE hdfc_data;

-- Card applications
CREATE TABLE card_applications (
    id NUMBER(19) PRIMARY KEY,
    application_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    account_id VARCHAR2(50),
    card_type VARCHAR2(20) NOT NULL,
    card_category VARCHAR2(30) NOT NULL,
    requested_limit NUMBER(15,2),
    annual_income NUMBER(15,2),
    employment_type VARCHAR2(30),
    company_name VARCHAR2(255),
    existing_cards_count NUMBER(3) DEFAULT 0,
    credit_score NUMBER(3),
    application_status VARCHAR2(20) DEFAULT 'SUBMITTED' CHECK (application_status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED')),
    rejection_reason VARCHAR2(1000),
    approved_limit NUMBER(15,2),
    assigned_to VARCHAR2(50),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Card limits
CREATE TABLE card_limits (
    id NUMBER(19) PRIMARY KEY,
    card_id VARCHAR2(50) NOT NULL,
    daily_purchase_limit NUMBER(15,2) DEFAULT 200000.00,
    daily_cash_limit NUMBER(15,2) DEFAULT 50000.00,
    daily_online_limit NUMBER(15,2) DEFAULT 100000.00,
    monthly_limit NUMBER(15,2) DEFAULT 1000000.00,
    international_limit NUMBER(15,2) DEFAULT 500000.00,
    contactless_limit NUMBER(10,2) DEFAULT 5000.00,
    pos_limit NUMBER(15,2) DEFAULT 200000.00,
    atm_limit NUMBER(15,2) DEFAULT 50000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_limits_card FOREIGN KEY (card_id) REFERENCES cards(card_id)
) TABLESPACE hdfc_data;

-- Card rewards
CREATE TABLE card_rewards (
    id NUMBER(19) PRIMARY KEY,
    reward_id VARCHAR2(50) UNIQUE NOT NULL,
    card_id VARCHAR2(50) NOT NULL,
    transaction_id VARCHAR2(50),
    reward_type VARCHAR2(20) NOT NULL CHECK (reward_type IN ('POINTS', 'CASHBACK', 'MILES')),
    points_earned NUMBER(10) DEFAULT 0,
    cashback_amount NUMBER(10,2) DEFAULT 0.00,
    miles_earned NUMBER(10) DEFAULT 0,
    earning_rate NUMBER(5,2),
    category VARCHAR2(100),
    earned_date DATE DEFAULT SYSDATE,
    expiry_date DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REDEEMED', 'EXPIRED', 'CANCELLED')),
    redeemed_date DATE,
    redemption_value NUMBER(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_rewards_card FOREIGN KEY (card_id) REFERENCES cards(card_id)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE card_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE card_txn_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE card_stmt_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE card_app_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE card_limit_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE card_reward_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_cards_user_id ON cards(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_cards_card_number ON cards(card_number) TABLESPACE hdfc_index;
CREATE INDEX idx_cards_status ON cards(status) TABLESPACE hdfc_index;
CREATE INDEX idx_cards_expiry ON cards(expiry_date) TABLESPACE hdfc_index;
CREATE INDEX idx_card_txn_card_id ON card_transactions(card_id) TABLESPACE hdfc_index;
CREATE INDEX idx_card_txn_date ON card_transactions(transaction_date) TABLESPACE hdfc_index;
CREATE INDEX idx_card_txn_status ON card_transactions(status) TABLESPACE hdfc_index;
CREATE INDEX idx_card_stmt_due_date ON card_statements(due_date) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_cards_id
    BEFORE INSERT ON cards
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := card_seq.NEXTVAL;
    END IF;
    IF :NEW.card_id IS NULL THEN
        :NEW.card_id := 'CARD' || LPAD(card_seq.CURRVAL, 10, '0');
    END IF;
    IF :NEW.card_number IS NULL THEN
        -- Generate card number based on type
        IF :NEW.card_type = 'CREDIT_CARD' THEN
            :NEW.card_number := '4567' || LPAD(card_seq.CURRVAL, 12, '0');
        ELSE
            :NEW.card_number := '6789' || LPAD(card_seq.CURRVAL, 12, '0');
        END IF;
    END IF;
    -- Create masked card number
    :NEW.masked_card_number := SUBSTR(:NEW.card_number, 1, 4) || '-****-****-' || SUBSTR(:NEW.card_number, -4);
    :NEW.available_limit := :NEW.credit_limit;
END;
/

-- Function to calculate reward points
CREATE OR REPLACE FUNCTION calculate_reward_points(
    p_card_id IN VARCHAR2,
    p_amount IN NUMBER,
    p_category IN VARCHAR2
) RETURN NUMBER IS
    v_points NUMBER(10) := 0;
    v_card_category VARCHAR2(30);
    v_rate NUMBER(5,2) := 1;
BEGIN
    SELECT card_category INTO v_card_category
    FROM cards WHERE card_id = p_card_id;
    
    -- Different earning rates based on card category
    CASE v_card_category
        WHEN 'PLATINUM' THEN v_rate := 2;
        WHEN 'SIGNATURE' THEN v_rate := 3;
        WHEN 'INFINITE' THEN v_rate := 5;
        ELSE v_rate := 1;
    END CASE;
    
    -- Special categories get higher rates
    IF p_category IN ('GROCERY', 'FUEL', 'DINING') THEN
        v_rate := v_rate * 2;
    END IF;
    
    v_points := FLOOR(p_amount / 100) * v_rate;
    
    RETURN v_points;
END;
/