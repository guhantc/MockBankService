-- HDFC Bank Initial Data Setup
-- Insert reference data and sample records

-- Connect as each user and insert sample data

-- ============================================
-- USER SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_user/hdfcbank123;

-- Insert sample users
INSERT INTO users (user_id, first_name, last_name, email, phone_number, password, date_of_birth, gender, status, role)
VALUES ('USER001', 'John', 'Doe', 'john.doe@example.com', '9876543210', '$2a$10$dXJ3SW6G7P2e0x6Y5fZBxOuPLkb6vOvUtIwCYwJYWB3hWKNZ3KHNu', DATE '1985-06-15', 'MALE', 'ACTIVE', 'CUSTOMER');

INSERT INTO users (user_id, first_name, last_name, email, phone_number, password, date_of_birth, gender, status, role)
VALUES ('USER002', 'Jane', 'Smith', 'jane.smith@example.com', '9876543211', '$2a$10$dXJ3SW6G7P2e0x6Y5fZBxOuPLkb6vOvUtIwCYwJYWB3hWKNZ3KHNu', DATE '1990-03-22', 'FEMALE', 'ACTIVE', 'PREMIUM_CUSTOMER');

INSERT INTO users (user_id, first_name, last_name, email, phone_number, password, date_of_birth, gender, status, role)
VALUES ('STAFF001', 'Admin', 'User', 'admin@hdfcbank.com', '9876543212', '$2a$10$dXJ3SW6G7P2e0x6Y5fZBxOuPLkb6vOvUtIwCYwJYWB3hWKNZ3KHNu', DATE '1980-01-01', 'MALE', 'ACTIVE', 'ADMIN');

-- Insert sample addresses
INSERT INTO user_addresses (user_id, address_line1, address_line2, city, state, postal_code, country, address_type)
VALUES ('USER001', '123 Main Street', 'Apartment 4B', 'Mumbai', 'Maharashtra', '400001', 'India', 'PERMANENT');

INSERT INTO user_addresses (user_id, address_line1, address_line2, city, state, postal_code, country, address_type)
VALUES ('USER002', '456 Park Avenue', 'Floor 3', 'Delhi', 'Delhi', '110001', 'India', 'PERMANENT');

COMMIT;

-- ============================================
-- ACCOUNT SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_account/hdfcbank123;

-- Insert sample accounts
INSERT INTO accounts (account_id, user_id, account_number, account_type, balance, branch_code, ifsc_code, micr_code, status)
VALUES ('ACC001', 'USER001', '000100012345', 'SAVINGS', 50000.00, 'HDFC0001', 'HDFC0000001', '400240001', 'ACTIVE');

INSERT INTO accounts (account_id, user_id, account_number, account_type, balance, branch_code, ifsc_code, micr_code, status)
VALUES ('ACC002', 'USER002', '000100012346', 'CURRENT', 100000.00, 'HDFC0001', 'HDFC0000001', '400240001', 'ACTIVE');

INSERT INTO accounts (account_id, user_id, account_number, account_type, balance, branch_code, ifsc_code, micr_code, status)
VALUES ('ACC003', 'USER001', '000100012347', 'SALARY', 75000.00, 'HDFC0001', 'HDFC0000001', '400240001', 'ACTIVE');

-- Insert account holders
INSERT INTO account_holders (account_id, user_id, holder_type, can_operate)
VALUES ('ACC001', 'USER001', 'PRIMARY', 'Y');

INSERT INTO account_holders (account_id, user_id, holder_type, can_operate)
VALUES ('ACC002', 'USER002', 'PRIMARY', 'Y');

-- Insert account limits
INSERT INTO account_limits (account_id, daily_transaction_limit, monthly_transaction_limit, daily_withdrawal_limit)
VALUES ('ACC001', 100000.00, 1000000.00, 50000.00);

INSERT INTO account_limits (account_id, daily_transaction_limit, monthly_transaction_limit, daily_withdrawal_limit)
VALUES ('ACC002', 500000.00, 5000000.00, 100000.00);

COMMIT;

-- ============================================
-- LOAN SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_loan/hdfcbank123;

-- Insert sample loan applications
INSERT INTO loan_applications (application_id, user_id, loan_type, requested_amount, tenure_months, annual_income, employment_type, application_status)
VALUES ('LAPP001', 'USER001', 'HOME_LOAN', 5000000.00, 240, 1200000.00, 'SALARIED', 'APPROVED');

INSERT INTO loan_applications (application_id, user_id, loan_type, requested_amount, tenure_months, annual_income, employment_type, application_status)
VALUES ('LAPP002', 'USER002', 'PERSONAL_LOAN', 500000.00, 60, 1500000.00, 'BUSINESS', 'UNDER_REVIEW');

-- Insert sample loans
INSERT INTO loans (loan_id, user_id, account_id, loan_type, principal_amount, sanctioned_amount, interest_rate, tenure_months, status, loan_officer_id)
VALUES ('LOAN001', 'USER001', 'ACC001', 'HOME_LOAN', 5000000.00, 5000000.00, 8.50, 240, 'APPROVED', 'STAFF001');

-- Generate EMI schedule for the loan
BEGIN
    generate_emi_schedule('LOAN001', 5000000.00, 8.50, 240, SYSDATE + 30);
END;
/

COMMIT;

-- ============================================
-- CARD SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_card/hdfcbank123;

-- Insert sample card applications
INSERT INTO card_applications (application_id, user_id, account_id, card_type, card_category, requested_limit, annual_income, application_status)
VALUES ('CAPP001', 'USER001', 'ACC001', 'CREDIT_CARD', 'PLATINUM', 200000.00, 1200000.00, 'APPROVED');

INSERT INTO card_applications (application_id, user_id, account_id, card_type, card_category, requested_limit, annual_income, application_status)
VALUES ('CAPP002', 'USER002', 'ACC002', 'CREDIT_CARD', 'SIGNATURE', 500000.00, 1500000.00, 'APPROVED');

-- Insert sample cards
INSERT INTO cards (card_id, user_id, account_id, card_number, card_type, card_category, credit_limit, available_limit, expiry_date, status)
VALUES ('CARD001', 'USER001', 'ACC001', '4567123456781234', 'CREDIT_CARD', 'PLATINUM', 200000.00, 200000.00, DATE '2029-06-30', 'ACTIVE');

INSERT INTO cards (card_id, user_id, account_id, card_number, card_type, card_category, credit_limit, available_limit, expiry_date, status)
VALUES ('CARD002', 'USER002', 'ACC002', '4567123456785678', 'CREDIT_CARD', 'SIGNATURE', 500000.00, 500000.00, DATE '2029-12-31', 'ACTIVE');

INSERT INTO cards (card_id, user_id, account_id, card_number, card_type, card_category, expiry_date, status)
VALUES ('CARD003', 'USER001', 'ACC001', '6789123456781234', 'DEBIT_CARD', 'CLASSIC', DATE '2027-06-30', 'ACTIVE');

-- Insert card limits
INSERT INTO card_limits (card_id, daily_purchase_limit, daily_cash_limit, daily_online_limit)
VALUES ('CARD001', 200000.00, 50000.00, 100000.00);

INSERT INTO card_limits (card_id, daily_purchase_limit, daily_cash_limit, daily_online_limit)
VALUES ('CARD002', 500000.00, 100000.00, 300000.00);

COMMIT;

-- ============================================
-- TRANSACTION SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_transaction/hdfcbank123;

-- Insert sample transactions
INSERT INTO transactions (transaction_id, source_account_id, target_account_id, amount, transaction_type, status, description, initiator_user_id)
VALUES ('TXN001', 'ACC001', 'ACC002', 10000.00, 'TRANSFER', 'COMPLETED', 'Transfer to Jane', 'USER001');

INSERT INTO transactions (transaction_id, source_account_id, amount, transaction_type, status, description, initiator_user_id)
VALUES ('TXN002', 'ACC001', 5000.00, 'DEBIT', 'COMPLETED', 'ATM Withdrawal', 'USER001');

INSERT INTO transactions (transaction_id, target_account_id, amount, transaction_type, status, description)
VALUES ('TXN003', 'ACC001', 25000.00, 'CREDIT', 'COMPLETED', 'Salary Credit');

-- Insert transaction limits
INSERT INTO transaction_limits (account_id, transaction_type, daily_limit, monthly_limit, per_transaction_limit)
VALUES ('ACC001', 'TRANSFER', 100000.00, 1000000.00, 50000.00);

INSERT INTO transaction_limits (account_id, transaction_type, daily_limit, monthly_limit, per_transaction_limit)
VALUES ('ACC002', 'TRANSFER', 500000.00, 5000000.00, 200000.00);

COMMIT;

-- ============================================
-- INVESTMENT SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_investment/hdfcbank123;

-- Insert sample market data
INSERT INTO market_data (product_code, isin_code, nav_date, nav_price, previous_nav, data_source)
VALUES ('MF001', 'INF109K01234', SYSDATE, 50.25, 49.80, 'AMFI');

INSERT INTO market_data (product_code, isin_code, nav_date, nav_price, previous_nav, data_source)
VALUES ('MF002', 'INF109K01235', SYSDATE, 125.75, 124.90, 'AMFI');

INSERT INTO market_data (product_code, isin_code, nav_date, nav_price, previous_nav, data_source)
VALUES ('FD001', 'FD001', SYSDATE, 100.00, 100.00, 'INTERNAL');

-- Insert sample investments
INSERT INTO investments (investment_id, user_id, account_id, investment_type, product_code, product_name, invested_amount, current_value, units, nav_price, fund_house)
VALUES ('INV001', 'USER001', 'ACC001', 'MUTUAL_FUND', 'MF001', 'HDFC Equity Fund', 50000.00, 52000.00, 1000.000000, 50.25, 'HDFC Asset Management');

INSERT INTO investments (investment_id, user_id, account_id, investment_type, product_code, product_name, invested_amount, current_value, units, nav_price, fund_house)
VALUES ('INV002', 'USER002', 'ACC002', 'MUTUAL_FUND', 'MF002', 'HDFC Balanced Fund', 100000.00, 103000.00, 800.000000, 125.75, 'HDFC Asset Management');

INSERT INTO investments (investment_id, user_id, account_id, investment_type, product_code, product_name, invested_amount, current_value, maturity_amount, maturity_date)
VALUES ('INV003', 'USER001', 'ACC001', 'FIXED_DEPOSIT', 'FD001', 'HDFC Fixed Deposit', 200000.00, 200000.00, 240000.00, ADD_MONTHS(SYSDATE, 36));

-- Insert sample SIPs
INSERT INTO sip_transactions (sip_id, investment_id, user_id, amount, frequency, start_date, end_date, next_installment_date, auto_debit_account)
VALUES ('SIP001', 'INV001', 'USER001', 5000.00, 'MONTHLY', SYSDATE, ADD_MONTHS(SYSDATE, 60), ADD_MONTHS(SYSDATE, 1), 'ACC001');

-- Update portfolio values
BEGIN
    update_portfolio_value('USER001');
    update_portfolio_value('USER002');
END;
/

COMMIT;

-- ============================================
-- NOTIFICATION SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_notification/hdfcbank123;

-- Insert notification templates
INSERT INTO notification_templates (template_id, template_name, template_type, channel, subject_template, message_template, is_active)
VALUES ('TXN_ALERT_SMS', 'Transaction Alert SMS', 'TRANSACTION_ALERT', 'SMS', 
        NULL, 
        'Dear {{customerName}}, your account {{accountNumber}} has been {{transactionType}} with Rs.{{amount}} on {{date}}. Available balance: Rs.{{balance}}. -HDFC Bank',
        'Y');

INSERT INTO notification_templates (template_id, template_name, template_type, channel, subject_template, message_template, is_active)
VALUES ('TXN_ALERT_EMAIL', 'Transaction Alert Email', 'TRANSACTION_ALERT', 'EMAIL',
        'Transaction Alert - Account {{accountNumber}}',
        'Dear {{customerName}},<br><br>Your account {{accountNumber}} has been {{transactionType}} with Rs.{{amount}} on {{date}} at {{time}}.<br><br>Transaction ID: {{transactionId}}<br>Available Balance: Rs.{{balance}}<br><br>If this transaction was not done by you, please contact us immediately.<br><br>Thank you,<br>HDFC Bank',
        'Y');

INSERT INTO notification_templates (template_id, template_name, template_type, channel, subject_template, message_template, is_active)
VALUES ('WELCOME_SMS', 'Welcome SMS', 'ACCOUNT_ALERT', 'SMS',
        NULL,
        'Welcome to HDFC Bank, {{customerName}}! Your account {{accountNumber}} is now active. Thank you for choosing us. -HDFC Bank',
        'Y');

-- Insert user notification preferences
INSERT INTO user_notification_preferences (user_id, notification_type, channel, is_enabled, frequency)
VALUES ('USER001', 'TRANSACTION_ALERT', 'SMS', 'Y', 'IMMEDIATE');

INSERT INTO user_notification_preferences (user_id, notification_type, channel, is_enabled, frequency)
VALUES ('USER001', 'TRANSACTION_ALERT', 'EMAIL', 'Y', 'IMMEDIATE');

INSERT INTO user_notification_preferences (user_id, notification_type, channel, is_enabled, frequency)
VALUES ('USER002', 'TRANSACTION_ALERT', 'SMS', 'Y', 'IMMEDIATE');

INSERT INTO user_notification_preferences (user_id, notification_type, channel, is_enabled, frequency)
VALUES ('USER002', 'ACCOUNT_ALERT', 'EMAIL', 'Y', 'DAILY');

-- Insert sample notifications
INSERT INTO notifications (notification_id, user_id, notification_type, channel, title, message, recipient, template_id, status)
VALUES ('NOTIF001', 'USER001', 'TRANSACTION_ALERT', 'SMS', 'Transaction Alert', 
        'Dear John Doe, your account 000100012345 has been debited with Rs.10000 on ' || TO_CHAR(SYSDATE, 'DD-MON-YYYY') || '. Available balance: Rs.40000. -HDFC Bank',
        '9876543210', 'TXN_ALERT_SMS', 'SENT');

COMMIT;

-- ============================================
-- SAGA SERVICE INITIAL DATA
-- ============================================
CONNECT hdfc_saga/hdfcbank123;

-- Insert SAGA configuration for Account Opening
INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('ACCOUNT_OPENING', 'CREATE_USER', 'user-service', '/users', 1, 'user-service', '/users/{userId}', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('ACCOUNT_OPENING', 'CREATE_ACCOUNT', 'account-service', '/accounts', 2, 'account-service', '/accounts/{accountId}', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('ACCOUNT_OPENING', 'ISSUE_CARD', 'card-service', '/cards', 3, 'card-service', '/cards/{cardId}/cancel', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('ACCOUNT_OPENING', 'SEND_WELCOME_NOTIFICATION', 'notification-service', '/notifications/welcome', 4, NULL, NULL, 'N');

-- Insert SAGA configuration for Loan Processing
INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('LOAN_PROCESSING', 'VALIDATE_APPLICATION', 'loan-service', '/loans/validate', 1, NULL, NULL, 'N');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('LOAN_PROCESSING', 'CREDIT_CHECK', 'external-credit-service', '/credit-check', 2, NULL, NULL, 'N');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('LOAN_PROCESSING', 'APPROVE_LOAN', 'loan-service', '/loans/{loanId}/approve', 3, 'loan-service', '/loans/{loanId}/reject', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('LOAN_PROCESSING', 'DISBURSE_FUNDS', 'account-service', '/accounts/{accountId}/credit', 4, 'account-service', '/accounts/{accountId}/debit', 'Y');

-- Insert SAGA configuration for Fund Transfer
INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('FUND_TRANSFER', 'VALIDATE_TRANSFER', 'transaction-service', '/transactions/validate', 1, NULL, NULL, 'N');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('FUND_TRANSFER', 'DEBIT_SOURCE_ACCOUNT', 'account-service', '/accounts/{sourceAccountId}/debit', 2, 'account-service', '/accounts/{sourceAccountId}/credit', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('FUND_TRANSFER', 'CREDIT_TARGET_ACCOUNT', 'account-service', '/accounts/{targetAccountId}/credit', 3, 'account-service', '/accounts/{targetAccountId}/debit', 'Y');

INSERT INTO saga_configuration (saga_type, step_name, service_name, service_endpoint, step_order, compensation_service, compensation_endpoint, is_compensatable)
VALUES ('FUND_TRANSFER', 'SEND_NOTIFICATIONS', 'notification-service', '/notifications/transaction', 4, NULL, NULL, 'N');

-- Insert sample SAGA transaction
INSERT INTO saga_transactions (saga_id, saga_type, user_id, status, payload, total_steps)
VALUES ('SAGA001', 'ACCOUNT_OPENING', 'USER001', 'COMPLETED', '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com"}', 4);

-- Initialize steps for the sample SAGA
BEGIN
    initialize_saga_steps('SAGA001', 'ACCOUNT_OPENING');
END;
/

COMMIT;

-- ============================================
-- CREATE CROSS-SCHEMA SYNONYMS
-- ============================================

-- Create synonyms for cross-service communication
-- This allows services to reference each other's objects

-- User service synonyms
CONNECT hdfc_user/hdfcbank123;
CREATE SYNONYM accounts FOR hdfc_account.accounts;
CREATE SYNONYM transactions FOR hdfc_transaction.transactions;

-- Account service synonyms  
CONNECT hdfc_account/hdfcbank123;
CREATE SYNONYM users FOR hdfc_user.users;
CREATE SYNONYM transactions FOR hdfc_transaction.transactions;
CREATE SYNONYM cards FOR hdfc_card.cards;

-- Transaction service synonyms
CONNECT hdfc_transaction/hdfcbank123;
CREATE SYNONYM accounts FOR hdfc_account.accounts;
CREATE SYNONYM users FOR hdfc_user.users;
CREATE SYNONYM notifications FOR hdfc_notification.notifications;

-- Complete setup
CONNECT sys/hdfcbank123 AS SYSDBA;

-- Grant necessary system privileges
GRANT EXECUTE ON DBMS_SCHEDULER TO hdfc_notification;
GRANT EXECUTE ON DBMS_SCHEDULER TO hdfc_saga;

-- Create database links for microservices communication (if needed)
-- This would be used in a distributed database setup

-- Compile all invalid objects
DECLARE
    sql_stmt VARCHAR2(4000);
BEGIN
    FOR obj IN (SELECT owner, object_name, object_type 
                FROM dba_objects 
                WHERE status = 'INVALID' 
                AND owner LIKE 'HDFC_%') LOOP
        BEGIN
            sql_stmt := 'ALTER ' || obj.object_type || ' ' || obj.owner || '.' || obj.object_name || ' COMPILE';
            EXECUTE IMMEDIATE sql_stmt;
        EXCEPTION
            WHEN OTHERS THEN
                DBMS_OUTPUT.PUT_LINE('Error compiling ' || obj.owner || '.' || obj.object_name || ': ' || SQLERRM);
        END;
    END LOOP;
END;
/

-- Create monitoring jobs
BEGIN
    -- Job to process scheduled notifications
    DBMS_SCHEDULER.CREATE_JOB(
        job_name => 'PROCESS_SCHEDULED_NOTIFICATIONS',
        job_type => 'PLSQL_BLOCK',
        job_action => 'BEGIN hdfc_notification.process_scheduled_notifications; END;',
        start_date => SYSTIMESTAMP,
        repeat_interval => 'FREQ=MINUTELY; INTERVAL=5',
        enabled => TRUE,
        comments => 'Process scheduled notifications every 5 minutes'
    );
    
    -- Job to update investment NAV
    DBMS_SCHEDULER.CREATE_JOB(
        job_name => 'UPDATE_INVESTMENT_NAV',
        job_type => 'PLSQL_BLOCK', 
        job_action => 'BEGIN
                        FOR inv IN (SELECT DISTINCT user_id FROM hdfc_investment.investments WHERE status = ''ACTIVE'') LOOP
                            hdfc_investment.update_portfolio_value(inv.user_id);
                        END LOOP;
                       END;',
        start_date => SYSTIMESTAMP,
        repeat_interval => 'FREQ=DAILY; BYHOUR=9; BYMINUTE=0',
        enabled => TRUE,
        comments => 'Update investment portfolio values daily'
    );
END;
/

COMMIT;

-- Display setup completion message
SELECT 'HDFC Bank Database Setup Completed Successfully!' as STATUS FROM dual;