-- HDFC Bank Loan Service Database Schema
-- Connect as hdfc_loan

-- Loans table
CREATE TABLE loans (
    id NUMBER(19) PRIMARY KEY,
    loan_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    account_id VARCHAR2(50) NOT NULL,
    loan_type VARCHAR2(30) NOT NULL CHECK (loan_type IN ('HOME_LOAN', 'PERSONAL_LOAN', 'CAR_LOAN', 'EDUCATION_LOAN', 'BUSINESS_LOAN', 'GOLD_LOAN', 'LOAN_AGAINST_PROPERTY')),
    principal_amount NUMBER(15,2) NOT NULL,
    sanctioned_amount NUMBER(15,2),
    disbursed_amount NUMBER(15,2) DEFAULT 0.00,
    outstanding_amount NUMBER(15,2) DEFAULT 0.00,
    interest_rate NUMBER(5,4) NOT NULL,
    tenure_months NUMBER(5) NOT NULL,
    emi_amount NUMBER(15,2),
    processing_fee NUMBER(10,2) DEFAULT 0.00,
    prepayment_charges NUMBER(5,4) DEFAULT 0.00,
    late_payment_charges NUMBER(5,4) DEFAULT 2.0000,
    loan_purpose VARCHAR2(500),
    collateral_details CLOB,
    guarantor_details CLOB,
    income_verification CLOB,
    credit_score NUMBER(3),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'DISBURSED', 'ACTIVE', 'CLOSED', 'DEFAULTED')),
    application_date DATE DEFAULT SYSDATE,
    approval_date DATE,
    disbursement_date DATE,
    first_emi_date DATE,
    last_emi_date DATE,
    next_emi_date DATE,
    repayment_method VARCHAR2(20) DEFAULT 'AUTO_DEBIT' CHECK (repayment_method IN ('AUTO_DEBIT', 'CHEQUE', 'CASH', 'ONLINE')),
    loan_officer_id VARCHAR2(50),
    approved_by VARCHAR2(50),
    branch_code VARCHAR2(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Loan EMI schedule
CREATE TABLE loan_emi_schedule (
    id NUMBER(19) PRIMARY KEY,
    loan_id VARCHAR2(50) NOT NULL,
    emi_number NUMBER(5) NOT NULL,
    due_date DATE NOT NULL,
    principal_amount NUMBER(15,2) NOT NULL,
    interest_amount NUMBER(15,2) NOT NULL,
    total_emi_amount NUMBER(15,2) NOT NULL,
    outstanding_principal NUMBER(15,2) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'PARTIAL', 'WAIVED')),
    paid_amount NUMBER(15,2) DEFAULT 0.00,
    paid_date DATE,
    late_fee NUMBER(10,2) DEFAULT 0.00,
    waiver_amount NUMBER(10,2) DEFAULT 0.00,
    remarks VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
) TABLESPACE hdfc_data;

-- Loan documents
CREATE TABLE loan_documents (
    id NUMBER(19) PRIMARY KEY,
    loan_id VARCHAR2(50) NOT NULL,
    document_type VARCHAR2(50) NOT NULL,
    document_name VARCHAR2(255) NOT NULL,
    document_path VARCHAR2(500),
    document_size NUMBER(12),
    mime_type VARCHAR2(100),
    is_mandatory CHAR(1) DEFAULT 'N' CHECK (is_mandatory IN ('Y', 'N')),
    is_verified CHAR(1) DEFAULT 'N' CHECK (is_verified IN ('Y', 'N')),
    verified_by VARCHAR2(50),
    verification_date TIMESTAMP,
    expiry_date DATE,
    remarks VARCHAR2(500),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_doc_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
) TABLESPACE hdfc_data;

-- Loan payments
CREATE TABLE loan_payments (
    id NUMBER(19) PRIMARY KEY,
    payment_id VARCHAR2(50) UNIQUE NOT NULL,
    loan_id VARCHAR2(50) NOT NULL,
    emi_id NUMBER(19),
    payment_type VARCHAR2(20) NOT NULL CHECK (payment_type IN ('EMI', 'PREPAYMENT', 'PART_PAYMENT', 'FORECLOSURE', 'LATE_FEE')),
    amount NUMBER(15,2) NOT NULL,
    principal_amount NUMBER(15,2) DEFAULT 0.00,
    interest_amount NUMBER(15,2) DEFAULT 0.00,
    fee_amount NUMBER(15,2) DEFAULT 0.00,
    payment_method VARCHAR2(20) NOT NULL,
    payment_reference VARCHAR2(100),
    payment_date DATE NOT NULL,
    processed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR2(20) DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REVERSED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loan_payment_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id),
    CONSTRAINT fk_loan_payment_emi FOREIGN KEY (emi_id) REFERENCES loan_emi_schedule(id)
) TABLESPACE hdfc_data;

-- Loan applications (separate from loans for tracking)
CREATE TABLE loan_applications (
    id NUMBER(19) PRIMARY KEY,
    application_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    loan_type VARCHAR2(30) NOT NULL,
    requested_amount NUMBER(15,2) NOT NULL,
    tenure_months NUMBER(5) NOT NULL,
    annual_income NUMBER(15,2) NOT NULL,
    employment_type VARCHAR2(30),
    company_name VARCHAR2(255),
    work_experience_months NUMBER(5),
    existing_loans_count NUMBER(3) DEFAULT 0,
    existing_loans_emi NUMBER(15,2) DEFAULT 0.00,
    property_value NUMBER(15,2),
    down_payment NUMBER(15,2),
    co_applicant_details CLOB,
    application_status VARCHAR2(20) DEFAULT 'SUBMITTED' CHECK (application_status IN ('SUBMITTED', 'DOCUMENT_PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'WITHDRAWN')),
    rejection_reason VARCHAR2(1000),
    assigned_to VARCHAR2(50),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Loan collateral
CREATE TABLE loan_collateral (
    id NUMBER(19) PRIMARY KEY,
    loan_id VARCHAR2(50) NOT NULL,
    collateral_type VARCHAR2(50) NOT NULL,
    collateral_value NUMBER(15,2) NOT NULL,
    market_value NUMBER(15,2),
    valuation_date DATE,
    valuation_by VARCHAR2(255),
    description CLOB,
    location_address VARCHAR2(1000),
    legal_verification CHAR(1) DEFAULT 'N' CHECK (legal_verification IN ('Y', 'N')),
    insurance_details CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collateral_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE loan_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE emi_schedule_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE loan_doc_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE loan_payment_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE loan_app_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE collateral_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_loans_user_id ON loans(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_loans_status ON loans(status) TABLESPACE hdfc_index;
CREATE INDEX idx_loans_type ON loans(loan_type) TABLESPACE hdfc_index;
CREATE INDEX idx_loans_next_emi ON loans(next_emi_date) TABLESPACE hdfc_index;
CREATE INDEX idx_emi_due_date ON loan_emi_schedule(due_date) TABLESPACE hdfc_index;
CREATE INDEX idx_emi_status ON loan_emi_schedule(status) TABLESPACE hdfc_index;
CREATE INDEX idx_loan_app_status ON loan_applications(application_status) TABLESPACE hdfc_index;
CREATE INDEX idx_loan_app_user ON loan_applications(user_id) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_loans_id
    BEFORE INSERT ON loans
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := loan_seq.NEXTVAL;
    END IF;
    IF :NEW.loan_id IS NULL THEN
        :NEW.loan_id := 'LOAN' || LPAD(loan_seq.CURRVAL, 10, '0');
    END IF;
    :NEW.outstanding_amount := :NEW.principal_amount;
END;
/

CREATE OR REPLACE TRIGGER trg_loans_updated_at
    BEFORE UPDATE ON loans
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

-- Procedure to generate EMI schedule
CREATE OR REPLACE PROCEDURE generate_emi_schedule(
    p_loan_id IN VARCHAR2,
    p_principal IN NUMBER,
    p_rate IN NUMBER,
    p_tenure IN NUMBER,
    p_start_date IN DATE
) IS
    v_emi_amount NUMBER(15,2);
    v_monthly_rate NUMBER(10,8);
    v_remaining_principal NUMBER(15,2);
    v_interest_amount NUMBER(15,2);
    v_principal_amount NUMBER(15,2);
    v_emi_date DATE;
BEGIN
    -- Calculate EMI amount
    v_monthly_rate := p_rate / (12 * 100);
    v_emi_amount := (p_principal * v_monthly_rate * POWER(1 + v_monthly_rate, p_tenure)) / 
                    (POWER(1 + v_monthly_rate, p_tenure) - 1);
    
    v_remaining_principal := p_principal;
    v_emi_date := p_start_date;
    
    FOR i IN 1..p_tenure LOOP
        v_interest_amount := v_remaining_principal * v_monthly_rate;
        v_principal_amount := v_emi_amount - v_interest_amount;
        v_remaining_principal := v_remaining_principal - v_principal_amount;
        
        INSERT INTO loan_emi_schedule (
            loan_id, emi_number, due_date, principal_amount, 
            interest_amount, total_emi_amount, outstanding_principal
        ) VALUES (
            p_loan_id, i, v_emi_date, v_principal_amount,
            v_interest_amount, v_emi_amount, v_remaining_principal
        );
        
        v_emi_date := ADD_MONTHS(v_emi_date, 1);
    END LOOP;
    
    -- Update loan with EMI amount
    UPDATE loans 
    SET emi_amount = v_emi_amount,
        first_emi_date = p_start_date,
        last_emi_date = ADD_MONTHS(p_start_date, p_tenure - 1),
        next_emi_date = p_start_date
    WHERE loan_id = p_loan_id;
    
    COMMIT;
END;
/