-- MySQL User Setup Script for HDFC Bank Microservices
-- Run this script as MySQL root user

-- Create user for HDFC Bank applications
CREATE USER IF NOT EXISTS 'hdfc_user'@'%' IDENTIFIED BY 'hdfc@2024';
CREATE USER IF NOT EXISTS 'hdfc_user'@'localhost' IDENTIFIED BY 'hdfc@2024';

-- Create databases
CREATE DATABASE IF NOT EXISTS hdfc_customer_db;
CREATE DATABASE IF NOT EXISTS hdfc_account_db;
CREATE DATABASE IF NOT EXISTS hdfc_transaction_db;

-- Grant privileges to hdfc_user
GRANT ALL PRIVILEGES ON hdfc_customer_db.* TO 'hdfc_user'@'%';
GRANT ALL PRIVILEGES ON hdfc_customer_db.* TO 'hdfc_user'@'localhost';

GRANT ALL PRIVILEGES ON hdfc_account_db.* TO 'hdfc_user'@'%';
GRANT ALL PRIVILEGES ON hdfc_account_db.* TO 'hdfc_user'@'localhost';

GRANT ALL PRIVILEGES ON hdfc_transaction_db.* TO 'hdfc_user'@'%';
GRANT ALL PRIVILEGES ON hdfc_transaction_db.* TO 'hdfc_user'@'localhost';

-- Grant connection privileges
GRANT USAGE ON *.* TO 'hdfc_user'@'%';
GRANT USAGE ON *.* TO 'hdfc_user'@'localhost';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Show databases and users for verification
SHOW DATABASES;
SELECT User, Host FROM mysql.user WHERE User = 'hdfc_user';