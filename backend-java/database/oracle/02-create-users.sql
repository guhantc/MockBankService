-- HDFC Bank Database Users Creation
-- Create separate users for each microservice

-- User Service Schema
CREATE USER hdfc_user IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Account Service Schema
CREATE USER hdfc_account IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Loan Service Schema
CREATE USER hdfc_loan IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Card Service Schema
CREATE USER hdfc_card IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Investment Service Schema
CREATE USER hdfc_investment IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Transaction Service Schema
CREATE USER hdfc_transaction IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Notification Service Schema
CREATE USER hdfc_notification IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- SAGA Orchestrator Schema
CREATE USER hdfc_saga IDENTIFIED BY hdfcbank123
DEFAULT TABLESPACE hdfc_data
TEMPORARY TABLESPACE hdfc_temp
QUOTA UNLIMITED ON hdfc_data
QUOTA UNLIMITED ON hdfc_index;

-- Grant necessary privileges to all users
DECLARE
    CURSOR user_cursor IS
        SELECT username FROM all_users 
        WHERE username LIKE 'HDFC_%';
BEGIN
    FOR user_rec IN user_cursor LOOP
        EXECUTE IMMEDIATE 'GRANT CONNECT TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT RESOURCE TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE TABLE TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE VIEW TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE SEQUENCE TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE TRIGGER TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE PROCEDURE TO ' || user_rec.username;
        EXECUTE IMMEDIATE 'GRANT CREATE SYNONYM TO ' || user_rec.username;
        
        -- Grant access to other schemas for inter-service communication
        FOR target_user IN user_cursor LOOP
            IF user_rec.username != target_user.username THEN
                EXECUTE IMMEDIATE 'GRANT SELECT, INSERT, UPDATE ON ' || target_user.username || '.* TO ' || user_rec.username;
            END IF;
        END LOOP;
    END LOOP;
END;
/