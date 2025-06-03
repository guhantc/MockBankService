-- HDFC Bank SAGA Orchestrator Database Schema
-- Connect as hdfc_saga

-- SAGA transactions table
CREATE TABLE saga_transactions (
    id NUMBER(19) PRIMARY KEY,
    saga_id VARCHAR2(50) UNIQUE NOT NULL,
    saga_type VARCHAR2(30) NOT NULL CHECK (saga_type IN ('ACCOUNT_OPENING', 'LOAN_PROCESSING', 'FUND_TRANSFER', 'CARD_ACTIVATION', 'INVESTMENT_PURCHASE')),
    user_id VARCHAR2(50) NOT NULL,
    status VARCHAR2(20) DEFAULT 'STARTED' CHECK (status IN ('STARTED', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED')),
    payload CLOB,
    result CLOB,
    error_message CLOB,
    compensation_data CLOB,
    total_steps NUMBER(5) DEFAULT 0,
    completed_steps NUMBER(5) DEFAULT 0,
    failed_steps NUMBER(5) DEFAULT 0,
    compensated_steps NUMBER(5) DEFAULT 0,
    timeout_minutes NUMBER(10) DEFAULT 30,
    max_retries NUMBER(3) DEFAULT 3,
    retry_count NUMBER(3) DEFAULT 0,
    priority VARCHAR2(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    correlation_id VARCHAR2(100),
    parent_saga_id VARCHAR2(50),
    initiated_by VARCHAR2(50),
    business_key VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    compensation_started_at TIMESTAMP,
    compensation_completed_at TIMESTAMP,
    expires_at TIMESTAMP
) TABLESPACE hdfc_data;

-- SAGA steps table
CREATE TABLE saga_steps (
    id NUMBER(19) PRIMARY KEY,
    step_id VARCHAR2(50) UNIQUE NOT NULL,
    saga_id VARCHAR2(50) NOT NULL,
    step_name VARCHAR2(100) NOT NULL,
    step_order NUMBER(3) NOT NULL,
    step_type VARCHAR2(20) DEFAULT 'SERVICE_CALL' CHECK (step_type IN ('SERVICE_CALL', 'LOCAL_ACTION', 'COMPENSATION', 'VALIDATION')),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'STARTED', 'COMPLETED', 'FAILED', 'SKIPPED', 'COMPENSATED')),
    service_name VARCHAR2(100),
    service_method VARCHAR2(100),
    service_endpoint VARCHAR2(500),
    input_data CLOB,
    output_data CLOB,
    error_data CLOB,
    compensation_service VARCHAR2(100),
    compensation_method VARCHAR2(100),
    compensation_endpoint VARCHAR2(500),
    compensation_data CLOB,
    timeout_seconds NUMBER(10) DEFAULT 30,
    retry_count NUMBER(3) DEFAULT 0,
    max_retries NUMBER(3) DEFAULT 3,
    retry_delay_seconds NUMBER(10) DEFAULT 5,
    is_compensatable CHAR(1) DEFAULT 'Y' CHECK (is_compensatable IN ('Y', 'N')),
    is_idempotent CHAR(1) DEFAULT 'Y' CHECK (is_idempotent IN ('Y', 'N')),
    execution_order NUMBER(3),
    compensation_order NUMBER(3),
    dependency_steps VARCHAR2(1000),
    parallel_group VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    compensated_at TIMESTAMP,
    CONSTRAINT fk_saga_step_transaction FOREIGN KEY (saga_id) REFERENCES saga_transactions(saga_id)
) TABLESPACE hdfc_data;

-- SAGA execution log
CREATE TABLE saga_execution_log (
    id NUMBER(19) PRIMARY KEY,
    log_id VARCHAR2(50) UNIQUE NOT NULL,
    saga_id VARCHAR2(50) NOT NULL,
    step_id VARCHAR2(50),
    event_type VARCHAR2(30) NOT NULL CHECK (event_type IN ('SAGA_STARTED', 'SAGA_COMPLETED', 'SAGA_FAILED', 'STEP_STARTED', 'STEP_COMPLETED', 'STEP_FAILED', 'COMPENSATION_STARTED', 'COMPENSATION_COMPLETED')),
    event_data CLOB,
    execution_time_ms NUMBER(10),
    message VARCHAR2(1000),
    log_level VARCHAR2(10) DEFAULT 'INFO' CHECK (log_level IN ('DEBUG', 'INFO', 'WARN', 'ERROR')),
    correlation_id VARCHAR2(100),
    thread_id VARCHAR2(100),
    hostname VARCHAR2(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saga_log_transaction FOREIGN KEY (saga_id) REFERENCES saga_transactions(saga_id)
) TABLESPACE hdfc_data;

-- SAGA configuration
CREATE TABLE saga_configuration (
    id NUMBER(19) PRIMARY KEY,
    saga_type VARCHAR2(30) NOT NULL,
    step_name VARCHAR2(100) NOT NULL,
    service_name VARCHAR2(100) NOT NULL,
    service_endpoint VARCHAR2(500) NOT NULL,
    http_method VARCHAR2(10) DEFAULT 'POST',
    timeout_seconds NUMBER(10) DEFAULT 30,
    max_retries NUMBER(3) DEFAULT 3,
    retry_delay_seconds NUMBER(10) DEFAULT 5,
    compensation_service VARCHAR2(100),
    compensation_endpoint VARCHAR2(500),
    compensation_method VARCHAR2(10) DEFAULT 'POST',
    is_compensatable CHAR(1) DEFAULT 'Y',
    is_idempotent CHAR(1) DEFAULT 'Y',
    step_order NUMBER(3) NOT NULL,
    compensation_order NUMBER(3),
    dependency_steps VARCHAR2(1000),
    parallel_group VARCHAR2(50),
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_saga_config UNIQUE (saga_type, step_name)
) TABLESPACE hdfc_data;

-- SAGA state snapshots for recovery
CREATE TABLE saga_state_snapshots (
    id NUMBER(19) PRIMARY KEY,
    snapshot_id VARCHAR2(50) UNIQUE NOT NULL,
    saga_id VARCHAR2(50) NOT NULL,
    snapshot_type VARCHAR2(20) DEFAULT 'CHECKPOINT' CHECK (snapshot_type IN ('CHECKPOINT', 'BEFORE_COMPENSATION', 'RECOVERY')),
    state_data CLOB NOT NULL,
    step_states CLOB,
    snapshot_reason VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saga_snapshot_transaction FOREIGN KEY (saga_id) REFERENCES saga_transactions(saga_id)
) TABLESPACE hdfc_data;

-- SAGA metrics and monitoring
CREATE TABLE saga_metrics (
    id NUMBER(19) PRIMARY KEY,
    metric_date DATE NOT NULL,
    saga_type VARCHAR2(30) NOT NULL,
    total_sagas NUMBER(10) DEFAULT 0,
    completed_sagas NUMBER(10) DEFAULT 0,
    failed_sagas NUMBER(10) DEFAULT 0,
    compensated_sagas NUMBER(10) DEFAULT 0,
    avg_execution_time_ms NUMBER(15) DEFAULT 0,
    max_execution_time_ms NUMBER(15) DEFAULT 0,
    min_execution_time_ms NUMBER(15) DEFAULT 0,
    success_rate NUMBER(5,2) DEFAULT 0.00,
    failure_rate NUMBER(5,2) DEFAULT 0.00,
    compensation_rate NUMBER(5,2) DEFAULT 0.00,
    timeout_count NUMBER(10) DEFAULT 0,
    retry_count NUMBER(10) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_saga_metrics UNIQUE (metric_date, saga_type)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE saga_transaction_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE saga_step_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE saga_log_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE saga_config_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE saga_snapshot_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE saga_metrics_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_saga_txn_user_id ON saga_transactions(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_txn_status ON saga_transactions(status) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_txn_type ON saga_transactions(saga_type) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_txn_created ON saga_transactions(created_at) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_txn_expires ON saga_transactions(expires_at) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_steps_saga_id ON saga_steps(saga_id) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_steps_status ON saga_steps(status) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_steps_order ON saga_steps(saga_id, step_order) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_log_saga_id ON saga_execution_log(saga_id) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_log_created ON saga_execution_log(created_at) TABLESPACE hdfc_index;
CREATE INDEX idx_saga_config_type ON saga_configuration(saga_type) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_saga_transactions_id
    BEFORE INSERT ON saga_transactions
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := saga_transaction_seq.NEXTVAL;
    END IF;
    IF :NEW.saga_id IS NULL THEN
        :NEW.saga_id := 'SAGA' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(saga_transaction_seq.CURRVAL, 8, '0');
    END IF;
    :NEW.expires_at := CURRENT_TIMESTAMP + INTERVAL '1' DAY;
END;
/

CREATE OR REPLACE TRIGGER trg_saga_transactions_updated_at
    BEFORE UPDATE ON saga_transactions
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- Set completion timestamp
    IF :NEW.status = 'COMPLETED' AND :OLD.status != 'COMPLETED' THEN
        :NEW.completed_at := CURRENT_TIMESTAMP;
    END IF;
    
    -- Set compensation timestamps
    IF :NEW.status = 'COMPENSATING' AND :OLD.status != 'COMPENSATING' THEN
        :NEW.compensation_started_at := CURRENT_TIMESTAMP;
    END IF;
    
    IF :NEW.status = 'COMPENSATED' AND :OLD.status != 'COMPENSATED' THEN
        :NEW.compensation_completed_at := CURRENT_TIMESTAMP;
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_saga_steps_id
    BEFORE INSERT ON saga_steps
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := saga_step_seq.NEXTVAL;
    END IF;
    IF :NEW.step_id IS NULL THEN
        :NEW.step_id := 'STEP' || LPAD(saga_step_seq.CURRVAL, 10, '0');
    END IF;
    
    -- Set compensation order as reverse of execution order
    IF :NEW.compensation_order IS NULL THEN
        :NEW.compensation_order := 1000 - :NEW.step_order;
    END IF;
END;
/

-- Procedure to initialize SAGA steps from configuration
CREATE OR REPLACE PROCEDURE initialize_saga_steps(
    p_saga_id IN VARCHAR2,
    p_saga_type IN VARCHAR2
) IS
    CURSOR step_cursor IS
        SELECT step_name, service_name, service_endpoint, step_order,
               compensation_service, compensation_endpoint, is_compensatable,
               timeout_seconds, max_retries, parallel_group, dependency_steps
        FROM saga_configuration
        WHERE saga_type = p_saga_type
        AND is_active = 'Y'
        ORDER BY step_order;
BEGIN
    FOR step_rec IN step_cursor LOOP
        INSERT INTO saga_steps (
            saga_id, step_name, step_order, service_name, service_endpoint,
            compensation_service, compensation_endpoint, is_compensatable,
            timeout_seconds, max_retries, parallel_group, dependency_steps
        ) VALUES (
            p_saga_id, step_rec.step_name, step_rec.step_order,
            step_rec.service_name, step_rec.service_endpoint,
            step_rec.compensation_service, step_rec.compensation_endpoint,
            step_rec.is_compensatable, step_rec.timeout_seconds,
            step_rec.max_retries, step_rec.parallel_group, step_rec.dependency_steps
        );
    END LOOP;
    
    -- Update total steps count
    UPDATE saga_transactions
    SET total_steps = (SELECT COUNT(*) FROM saga_steps WHERE saga_id = p_saga_id)
    WHERE saga_id = p_saga_id;
    
    COMMIT;
END;
/

-- Procedure to log SAGA events
CREATE OR REPLACE PROCEDURE log_saga_event(
    p_saga_id IN VARCHAR2,
    p_step_id IN VARCHAR2 DEFAULT NULL,
    p_event_type IN VARCHAR2,
    p_message IN VARCHAR2 DEFAULT NULL,
    p_event_data IN CLOB DEFAULT NULL,
    p_execution_time_ms IN NUMBER DEFAULT NULL
) IS
BEGIN
    INSERT INTO saga_execution_log (
        saga_id, step_id, event_type, message, event_data,
        execution_time_ms, correlation_id
    ) VALUES (
        p_saga_id, p_step_id, p_event_type, p_message, p_event_data,
        p_execution_time_ms, SYS_GUID()
    );
    
    COMMIT;
END;
/

-- View for SAGA monitoring dashboard
CREATE OR REPLACE VIEW v_saga_dashboard AS
SELECT 
    s.saga_type,
    s.status,
    COUNT(*) as saga_count,
    AVG(EXTRACT(EPOCH FROM (s.completed_at - s.started_at)) * 1000) as avg_execution_time_ms,
    MAX(EXTRACT(EPOCH FROM (s.completed_at - s.started_at)) * 1000) as max_execution_time_ms,
    ROUND(COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(*), 2) as success_rate,
    ROUND(COUNT(CASE WHEN s.status = 'FAILED' THEN 1 END) * 100.0 / COUNT(*), 2) as failure_rate
FROM saga_transactions s
WHERE s.created_at >= TRUNC(SYSDATE) - 7  -- Last 7 days
GROUP BY s.saga_type, s.status
ORDER BY s.saga_type, s.status;
/