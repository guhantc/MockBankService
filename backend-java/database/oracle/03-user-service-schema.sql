-- HDFC Bank User Service Database Schema
-- Connect as hdfc_user

-- Users table
CREATE TABLE users (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) UNIQUE NOT NULL,
    first_name VARCHAR2(100) NOT NULL,
    last_name VARCHAR2(100) NOT NULL,
    email VARCHAR2(255) UNIQUE NOT NULL,
    phone_number VARCHAR2(15) UNIQUE,
    password VARCHAR2(255) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR2(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    pan_number VARCHAR2(10) UNIQUE,
    aadhaar_number VARCHAR2(12) UNIQUE,
    status VARCHAR2(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION')),
    role VARCHAR2(20) DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'PREMIUM_CUSTOMER', 'STAFF', 'MANAGER', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    failed_login_attempts NUMBER(2) DEFAULT 0,
    account_locked_until TIMESTAMP
) TABLESPACE hdfc_data;

-- User addresses (embedded in User entity)
CREATE TABLE user_addresses (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) NOT NULL,
    address_line1 VARCHAR2(255),
    address_line2 VARCHAR2(255),
    city VARCHAR2(100),
    state VARCHAR2(100),
    postal_code VARCHAR2(10),
    country VARCHAR2(50) DEFAULT 'India',
    address_type VARCHAR2(20) DEFAULT 'PERMANENT' CHECK (address_type IN ('PERMANENT', 'CURRENT', 'OFFICE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) TABLESPACE hdfc_data;

-- User documents
CREATE TABLE user_documents (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) NOT NULL,
    document_type VARCHAR2(50) NOT NULL,
    document_number VARCHAR2(100) NOT NULL,
    document_path VARCHAR2(500),
    verified CHAR(1) DEFAULT 'N' CHECK (verified IN ('Y', 'N')),
    verification_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_doc_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) TABLESPACE hdfc_data;

-- User sessions (for JWT token management)
CREATE TABLE user_sessions (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) NOT NULL,
    session_token VARCHAR2(1000) NOT NULL,
    device_info VARCHAR2(500),
    ip_address VARCHAR2(45),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id) REFERENCES users(user_id)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE user_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE user_address_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE user_document_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE user_session_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_users_email ON users(email) TABLESPACE hdfc_index;
CREATE INDEX idx_users_phone ON users(phone_number) TABLESPACE hdfc_index;
CREATE INDEX idx_users_pan ON users(pan_number) TABLESPACE hdfc_index;
CREATE INDEX idx_users_status ON users(status) TABLESPACE hdfc_index;
CREATE INDEX idx_users_created_at ON users(created_at) TABLESPACE hdfc_index;
CREATE INDEX idx_user_sessions_token ON user_sessions(session_token) TABLESPACE hdfc_index;
CREATE INDEX idx_user_sessions_expires ON user_sessions(expires_at) TABLESPACE hdfc_index;

-- Create triggers for auto-increment
CREATE OR REPLACE TRIGGER trg_users_id
    BEFORE INSERT ON users
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := user_seq.NEXTVAL;
    END IF;
    IF :NEW.user_id IS NULL THEN
        :NEW.user_id := 'USER' || LPAD(user_seq.CURRVAL, 8, '0');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
END;
/

CREATE OR REPLACE TRIGGER trg_user_address_id
    BEFORE INSERT ON user_addresses
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := user_address_seq.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_user_document_id
    BEFORE INSERT ON user_documents
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := user_document_seq.NEXTVAL;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_user_session_id
    BEFORE INSERT ON user_sessions
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := user_session_seq.NEXTVAL;
    END IF;
END;
/