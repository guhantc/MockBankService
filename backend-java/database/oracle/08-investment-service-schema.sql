-- HDFC Bank Investment Service Database Schema
-- Connect as hdfc_investment

-- Investments table
CREATE TABLE investments (
    id NUMBER(19) PRIMARY KEY,
    investment_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    account_id VARCHAR2(50) NOT NULL,
    investment_type VARCHAR2(30) NOT NULL CHECK (investment_type IN ('MUTUAL_FUND', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT', 'BONDS', 'EQUITY', 'SIP', 'INSURANCE', 'PPF', 'NSC')),
    product_code VARCHAR2(50) NOT NULL,
    product_name VARCHAR2(255) NOT NULL,
    isin_code VARCHAR2(20),
    invested_amount NUMBER(15,2) NOT NULL,
    current_value NUMBER(15,2) DEFAULT 0.00,
    units NUMBER(15,6) DEFAULT 0.000000,
    nav_price NUMBER(15,6) DEFAULT 0.000000,
    purchase_nav NUMBER(15,6) DEFAULT 0.000000,
    maturity_amount NUMBER(15,2),
    returns_amount NUMBER(15,2) DEFAULT 0.00,
    returns_percentage NUMBER(8,4) DEFAULT 0.0000,
    fund_house VARCHAR2(255),
    fund_manager VARCHAR2(255),
    risk_level VARCHAR2(20) DEFAULT 'MODERATE' CHECK (risk_level IN ('LOW', 'MODERATE', 'HIGH', 'VERY_HIGH')),
    investment_category VARCHAR2(50),
    sub_category VARCHAR2(100),
    benchmark_index VARCHAR2(100),
    expense_ratio NUMBER(5,4) DEFAULT 0.0000,
    exit_load NUMBER(5,4) DEFAULT 0.0000,
    lock_in_period NUMBER(5) DEFAULT 0,
    dividend_option VARCHAR2(20) DEFAULT 'GROWTH' CHECK (dividend_option IN ('GROWTH', 'DIVIDEND', 'REINVEST')),
    nomination_details CLOB,
    purchase_date DATE DEFAULT SYSDATE,
    maturity_date DATE,
    last_nav_update TIMESTAMP,
    auto_renewal CHAR(1) DEFAULT 'N' CHECK (auto_renewal IN ('Y', 'N')),
    sip_amount NUMBER(15,2),
    sip_frequency VARCHAR2(20) CHECK (sip_frequency IN ('MONTHLY', 'QUARTERLY', 'YEARLY')),
    sip_start_date DATE,
    sip_end_date DATE,
    next_sip_date DATE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'REDEEMED', 'PARTIALLY_REDEEMED', 'MATURED', 'CANCELLED', 'SUSPENDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Investment transactions
CREATE TABLE investment_transactions (
    id NUMBER(19) PRIMARY KEY,
    transaction_id VARCHAR2(50) UNIQUE NOT NULL,
    investment_id VARCHAR2(50) NOT NULL,
    transaction_type VARCHAR2(20) NOT NULL CHECK (transaction_type IN ('PURCHASE', 'REDEMPTION', 'SWITCH', 'DIVIDEND', 'STP', 'SWP', 'BONUS')),
    amount NUMBER(15,2) NOT NULL,
    units NUMBER(15,6) DEFAULT 0.000000,
    nav_price NUMBER(15,6) NOT NULL,
    total_amount NUMBER(15,2),
    charges NUMBER(10,2) DEFAULT 0.00,
    tax_amount NUMBER(10,2) DEFAULT 0.00,
    net_amount NUMBER(15,2),
    transaction_date DATE DEFAULT SYSDATE,
    settlement_date DATE,
    folio_number VARCHAR2(50),
    order_id VARCHAR2(50),
    exchange_reference VARCHAR2(100),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED', 'SETTLED')),
    rejection_reason VARCHAR2(500),
    processed_by VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_txn_investment FOREIGN KEY (investment_id) REFERENCES investments(investment_id)
) TABLESPACE hdfc_data;

-- Investment portfolio
CREATE TABLE investment_portfolio (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) NOT NULL,
    portfolio_name VARCHAR2(255) DEFAULT 'Default Portfolio',
    total_invested NUMBER(15,2) DEFAULT 0.00,
    current_value NUMBER(15,2) DEFAULT 0.00,
    total_returns NUMBER(15,2) DEFAULT 0.00,
    returns_percentage NUMBER(8,4) DEFAULT 0.0000,
    unrealized_gains NUMBER(15,2) DEFAULT 0.00,
    realized_gains NUMBER(15,2) DEFAULT 0.00,
    dividend_received NUMBER(15,2) DEFAULT 0.00,
    asset_allocation CLOB,
    risk_score NUMBER(3) DEFAULT 50,
    investment_goals CLOB,
    time_horizon NUMBER(5) DEFAULT 60,
    last_reviewed DATE,
    next_review_date DATE,
    auto_rebalance CHAR(1) DEFAULT 'N' CHECK (auto_rebalance IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- SIP transactions
CREATE TABLE sip_transactions (
    id NUMBER(19) PRIMARY KEY,
    sip_id VARCHAR2(50) UNIQUE NOT NULL,
    investment_id VARCHAR2(50) NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    amount NUMBER(15,2) NOT NULL,
    frequency VARCHAR2(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    installment_date NUMBER(2) DEFAULT 1,
    total_installments NUMBER(5),
    completed_installments NUMBER(5) DEFAULT 0,
    next_installment_date DATE,
    last_installment_date DATE,
    total_invested NUMBER(15,2) DEFAULT 0.00,
    current_value NUMBER(15,2) DEFAULT 0.00,
    total_units NUMBER(15,6) DEFAULT 0.000000,
    average_nav NUMBER(15,6) DEFAULT 0.000000,
    auto_debit_account VARCHAR2(50),
    mandate_id VARCHAR2(50),
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'STOPPED', 'COMPLETED', 'CANCELLED')),
    failure_count NUMBER(3) DEFAULT 0,
    max_failures NUMBER(3) DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sip_investment FOREIGN KEY (investment_id) REFERENCES investments(investment_id)
) TABLESPACE hdfc_data;

-- Investment advice and recommendations
CREATE TABLE investment_recommendations (
    id NUMBER(19) PRIMARY KEY,
    recommendation_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    recommendation_type VARCHAR2(30) NOT NULL CHECK (recommendation_type IN ('BUY', 'SELL', 'HOLD', 'SWITCH', 'REBALANCE')),
    product_code VARCHAR2(50) NOT NULL,
    product_name VARCHAR2(255) NOT NULL,
    current_allocation NUMBER(5,2) DEFAULT 0.00,
    recommended_allocation NUMBER(5,2) NOT NULL,
    amount_to_invest NUMBER(15,2),
    reason CLOB,
    risk_analysis CLOB,
    expected_returns NUMBER(8,4),
    time_horizon NUMBER(5),
    priority VARCHAR2(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    generated_by VARCHAR2(50) DEFAULT 'SYSTEM',
    confidence_score NUMBER(3) DEFAULT 75,
    valid_until DATE,
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'EXPIRED')),
    user_action VARCHAR2(20),
    action_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Market data for NAV updates
CREATE TABLE market_data (
    id NUMBER(19) PRIMARY KEY,
    product_code VARCHAR2(50) NOT NULL,
    isin_code VARCHAR2(20),
    nav_date DATE NOT NULL,
    nav_price NUMBER(15,6) NOT NULL,
    previous_nav NUMBER(15,6),
    change_amount NUMBER(15,6),
    change_percentage NUMBER(8,4),
    volume NUMBER(15),
    high_price NUMBER(15,6),
    low_price NUMBER(15,6),
    close_price NUMBER(15,6),
    dividend_declared NUMBER(15,6) DEFAULT 0.000000,
    dividend_date DATE,
    bonus_ratio VARCHAR2(20),
    split_ratio VARCHAR2(20),
    data_source VARCHAR2(50) DEFAULT 'BSE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_market_data UNIQUE (product_code, nav_date)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE investment_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE inv_txn_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE portfolio_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sip_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE recommendation_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE market_data_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_investments_user_id ON investments(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_investments_type ON investments(investment_type) TABLESPACE hdfc_index;
CREATE INDEX idx_investments_status ON investments(status) TABLESPACE hdfc_index;
CREATE INDEX idx_investments_product ON investments(product_code) TABLESPACE hdfc_index;
CREATE INDEX idx_inv_txn_investment ON investment_transactions(investment_id) TABLESPACE hdfc_index;
CREATE INDEX idx_inv_txn_date ON investment_transactions(transaction_date) TABLESPACE hdfc_index;
CREATE INDEX idx_sip_next_date ON sip_transactions(next_installment_date) TABLESPACE hdfc_index;
CREATE INDEX idx_market_data_date ON market_data(nav_date) TABLESPACE hdfc_index;
CREATE INDEX idx_market_data_product ON market_data(product_code) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_investments_id
    BEFORE INSERT ON investments
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := investment_seq.NEXTVAL;
    END IF;
    IF :NEW.investment_id IS NULL THEN
        :NEW.investment_id := 'INV' || LPAD(investment_seq.CURRVAL, 10, '0');
    END IF;
    :NEW.current_value := :NEW.invested_amount;
    :NEW.purchase_nav := :NEW.nav_price;
END;
/

CREATE OR REPLACE TRIGGER trg_investments_updated_at
    BEFORE UPDATE ON investments
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- Calculate returns
    IF :NEW.current_value != :OLD.current_value THEN
        :NEW.returns_amount := :NEW.current_value - :NEW.invested_amount;
        IF :NEW.invested_amount > 0 THEN
            :NEW.returns_percentage := (:NEW.returns_amount / :NEW.invested_amount) * 100;
        END IF;
    END IF;
END;
/

-- Procedure to update portfolio value
CREATE OR REPLACE PROCEDURE update_portfolio_value(p_user_id IN VARCHAR2) IS
    v_total_invested NUMBER(15,2) := 0;
    v_current_value NUMBER(15,2) := 0;
    v_total_returns NUMBER(15,2) := 0;
    v_returns_percentage NUMBER(8,4) := 0;
BEGIN
    SELECT 
        NVL(SUM(invested_amount), 0),
        NVL(SUM(current_value), 0),
        NVL(SUM(returns_amount), 0)
    INTO 
        v_total_invested,
        v_current_value,
        v_total_returns
    FROM investments
    WHERE user_id = p_user_id AND status = 'ACTIVE';
    
    IF v_total_invested > 0 THEN
        v_returns_percentage := (v_total_returns / v_total_invested) * 100;
    END IF;
    
    UPDATE investment_portfolio
    SET 
        total_invested = v_total_invested,
        current_value = v_current_value,
        total_returns = v_total_returns,
        returns_percentage = v_returns_percentage,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;
    
    IF SQL%ROWCOUNT = 0 THEN
        INSERT INTO investment_portfolio (
            user_id, total_invested, current_value, 
            total_returns, returns_percentage
        ) VALUES (
            p_user_id, v_total_invested, v_current_value,
            v_total_returns, v_returns_percentage
        );
    END IF;
    
    COMMIT;
END;
/