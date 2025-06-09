-- HDFC Bank Sample Data Script
-- Run after creating all tables

-- =====================================================
-- Customer Service Sample Data
-- =====================================================

-- Insert sample customers
INSERT INTO customers (
    customer_id, first_name, last_name, email, phone_number, date_of_birth, 
    pan_number, aadhaar_number, address, city, state, pin_code, 
    status, kyc_status, annual_income, occupation
) VALUES 
('CUST0000000001', 'Rajesh', 'Sharma', 'rajesh.sharma@email.com', '+919876543210', '1985-03-15', 
 'ABCDE1234F', '123456789012', '123 MG Road, Andheri', 'Mumbai', 'Maharashtra', '400001', 
 'ACTIVE', 'COMPLETED', 800000.00, 'Software Engineer'),

('CUST0000000002', 'Priya', 'Patel', 'priya.patel@email.com', '+919876543211', '1990-07-22', 
 'BCDEF2345G', '234567890123', '456 Sector 15, Gurgaon', 'Gurgaon', 'Haryana', '122001', 
 'ACTIVE', 'COMPLETED', 600000.00, 'Marketing Manager'),

('CUST0000000003', 'Amit', 'Kumar', 'amit.kumar@email.com', '+919876543212', '1988-11-08', 
 'CDEFG3456H', '345678901234', '789 Brigade Road', 'Bangalore', 'Karnataka', '560001', 
 'ACTIVE', 'IN_PROGRESS', 1200000.00, 'Business Analyst'),

('CUST0000000004', 'Sneha', 'Reddy', 'sneha.reddy@email.com', '+919876543213', '1992-05-18', 
 'DEFGH4567I', '456789012345', '321 Jubilee Hills', 'Hyderabad', 'Telangana', '500001', 
 'ACTIVE', 'PENDING', 450000.00, 'Teacher'),

('CUST0000000005', 'Vikram', 'Singh', 'vikram.singh@email.com', '+919876543214', '1987-09-25', 
 'EFGHI5678J', '567890123456', '654 Civil Lines', 'Delhi', 'Delhi', '110001', 
 'ACTIVE', 'COMPLETED', 950000.00, 'Consultant');

-- =====================================================
-- Account Service Sample Data
-- =====================================================

-- Insert sample accounts
INSERT INTO accounts (
    account_number, customer_id, account_type, balance, available_balance, 
    status, interest_rate, minimum_balance, daily_transaction_limit, 
    branch_code, ifsc_code
) VALUES 
('50100123456789', 'CUST0000000001', 'SAVINGS', 150000.00, 150000.00, 
 'ACTIVE', 3.50, 10000.00, 50000.00, 'HDFC0001', 'HDFC0001234'),

('50100987654321', 'CUST0000000001', 'CURRENT', 250000.00, 250000.00, 
 'ACTIVE', 0.00, 25000.00, 200000.00, 'HDFC0001', 'HDFC0001234'),

('50100234567890', 'CUST0000000002', 'SAVINGS', 85000.00, 85000.00, 
 'ACTIVE', 3.50, 10000.00, 50000.00, 'HDFC0002', 'HDFC0002345'),

('50100345678901', 'CUST0000000003', 'SALARY', 120000.00, 120000.00, 
 'ACTIVE', 3.50, 0.00, 100000.00, 'HDFC0003', 'HDFC0003456'),

('50100456789012', 'CUST0000000004', 'SAVINGS', 65000.00, 65000.00, 
 'ACTIVE', 3.50, 10000.00, 50000.00, 'HDFC0004', 'HDFC0004567'),

('50100567890123', 'CUST0000000005', 'CURRENT', 180000.00, 180000.00, 
 'ACTIVE', 0.00, 25000.00, 200000.00, 'HDFC0005', 'HDFC0005678'),

('50100678901234', 'CUST0000000003', 'FIXED_DEPOSIT', 500000.00, 500000.00, 
 'ACTIVE', 6.50, 100000.00, 0.00, 'HDFC0003', 'HDFC0003456');

-- =====================================================
-- Transaction Service Sample Data
-- =====================================================

-- Insert sample transactions
INSERT INTO transactions (
    transaction_id, from_account, to_account, amount, transaction_type, 
    status, description, reference_number, transaction_date, processed_date,
    charges, tax, from_account_balance, to_account_balance, 
    branch_code, channel_used
) VALUES 
('TXN001', '50100123456789', NULL, 10000.00, 'DEPOSIT', 'COMPLETED',
 'Cash Deposit', 'REF001', '2024-01-15 10:30:00', '2024-01-15 10:30:05',
 0.00, 0.00, 160000.00, NULL, 'HDFC0001', 'BRANCH'),

('TXN002', '50100123456789', NULL, 5000.00, 'WITHDRAWAL', 'COMPLETED',
 'ATM Withdrawal', 'REF002', '2024-01-16 14:20:00', '2024-01-16 14:20:03',
 21.00, 3.78, 155000.00, NULL, 'HDFC0001', 'ATM'),

('TXN003', '50100123456789', '50100234567890', 15000.00, 'TRANSFER', 'COMPLETED',
 'Fund Transfer to Priya', 'REF003', '2024-01-17 09:15:00', '2024-01-17 09:15:08',
 5.00, 0.90, 140000.00, 100000.00, 'HDFC0001', 'NET_BANKING'),

('TXN004', '50100987654321', NULL, 25000.00, 'DEPOSIT', 'COMPLETED',
 'Cheque Deposit', 'REF004', '2024-01-18 11:45:00', '2024-01-18 11:45:12',
 0.00, 0.00, 275000.00, NULL, 'HDFC0001', 'BRANCH'),

('TXN005', '50100234567890', NULL, 3000.00, 'WITHDRAWAL', 'COMPLETED',
 'ATM Withdrawal', 'REF005', '2024-01-18 16:30:00', '2024-01-18 16:30:04',
 21.00, 3.78, 97000.00, NULL, 'HDFC0002', 'ATM'),

('TXN006', '50100345678901', '50100456789012', 8000.00, 'TRANSFER', 'COMPLETED',
 'Monthly Allowance', 'REF006', '2024-01-19 12:00:00', '2024-01-19 12:00:06',
 0.00, 0.00, 112000.00, 73000.00, 'HDFC0003', 'MOBILE'),

('TXN007', '50100567890123', NULL, 50000.00, 'DEPOSIT', 'COMPLETED',
 'Business Income', 'REF007', '2024-01-20 10:15:00', '2024-01-20 10:15:07',
 0.00, 0.00, 230000.00, NULL, 'HDFC0005', 'NET_BANKING'),

('TXN008', '50100123456789', NULL, 2500.00, 'PAYMENT', 'COMPLETED',
 'Utility Bill Payment', 'REF008', '2024-01-20 15:45:00', '2024-01-20 15:45:03',
 2.50, 0.45, 137500.00, NULL, 'HDFC0001', 'MOBILE'),

('TXN009', '50100234567890', '50100567890123', 12000.00, 'TRANSFER', 'FAILED',
 'Business Payment', 'REF009', '2024-01-21 08:30:00', '2024-01-21 08:30:05',
 5.00, 0.90, 85000.00, 180000.00, 'HDFC0002', 'NET_BANKING'),

('TXN010', '50100678901234', NULL, 100000.00, 'DEPOSIT', 'COMPLETED',
 'FD Interest Credit', 'REF010', '2024-01-21 23:59:00', '2024-01-21 23:59:02',
 0.00, 0.00, 600000.00, NULL, 'HDFC0003', 'SYSTEM');

-- Update account balances to match transaction history
UPDATE accounts SET balance = 150000.00, available_balance = 150000.00 WHERE account_number = '50100123456789';
UPDATE accounts SET balance = 275000.00, available_balance = 275000.00 WHERE account_number = '50100987654321';
UPDATE accounts SET balance = 85000.00, available_balance = 85000.00 WHERE account_number = '50100234567890';
UPDATE accounts SET balance = 112000.00, available_balance = 112000.00 WHERE account_number = '50100345678901';
UPDATE accounts SET balance = 73000.00, available_balance = 73000.00 WHERE account_number = '50100456789012';
UPDATE accounts SET balance = 230000.00, available_balance = 230000.00 WHERE account_number = '50100567890123';
UPDATE accounts SET balance = 600000.00, available_balance = 600000.00 WHERE account_number = '50100678901234';