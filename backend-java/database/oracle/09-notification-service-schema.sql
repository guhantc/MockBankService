-- HDFC Bank Notification Service Database Schema
-- Connect as hdfc_notification

-- Notifications table
CREATE TABLE notifications (
    id NUMBER(19) PRIMARY KEY,
    notification_id VARCHAR2(50) UNIQUE NOT NULL,
    user_id VARCHAR2(50) NOT NULL,
    notification_type VARCHAR2(30) NOT NULL CHECK (notification_type IN ('TRANSACTION_ALERT', 'ACCOUNT_ALERT', 'LOAN_REMINDER', 'CARD_ALERT', 'INVESTMENT_UPDATE', 'PROMOTIONAL', 'SYSTEM_ALERT', 'SECURITY_ALERT')),
    channel VARCHAR2(10) NOT NULL CHECK (channel IN ('SMS', 'EMAIL', 'PUSH', 'IN_APP')),
    priority VARCHAR2(10) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    title VARCHAR2(255) NOT NULL,
    message CLOB NOT NULL,
    template_id VARCHAR2(50),
    template_variables CLOB,
    recipient VARCHAR2(255) NOT NULL,
    sender VARCHAR2(255) DEFAULT 'HDFC Bank',
    subject VARCHAR2(500),
    attachment_path VARCHAR2(1000),
    deeplink_url VARCHAR2(1000),
    campaign_id VARCHAR2(50),
    category VARCHAR2(100),
    tags VARCHAR2(500),
    status VARCHAR2(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'CANCELLED')),
    failure_reason VARCHAR2(1000),
    retry_count NUMBER(2) DEFAULT 0,
    max_retries NUMBER(2) DEFAULT 3,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    expires_at TIMESTAMP,
    metadata CLOB,
    external_message_id VARCHAR2(255),
    delivery_status VARCHAR2(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- Notification templates
CREATE TABLE notification_templates (
    id NUMBER(19) PRIMARY KEY,
    template_id VARCHAR2(50) UNIQUE NOT NULL,
    template_name VARCHAR2(255) NOT NULL,
    template_type VARCHAR2(30) NOT NULL,
    channel VARCHAR2(10) NOT NULL,
    language VARCHAR2(10) DEFAULT 'EN',
    subject_template VARCHAR2(500),
    message_template CLOB NOT NULL,
    variables CLOB,
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    created_by VARCHAR2(50),
    approved_by VARCHAR2(50),
    approval_date DATE,
    version_number NUMBER(5) DEFAULT 1,
    effective_from DATE DEFAULT SYSDATE,
    effective_to DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) TABLESPACE hdfc_data;

-- User notification preferences
CREATE TABLE user_notification_preferences (
    id NUMBER(19) PRIMARY KEY,
    user_id VARCHAR2(50) NOT NULL,
    notification_type VARCHAR2(30) NOT NULL,
    channel VARCHAR2(10) NOT NULL,
    is_enabled CHAR(1) DEFAULT 'Y' CHECK (is_enabled IN ('Y', 'N')),
    frequency VARCHAR2(20) DEFAULT 'IMMEDIATE' CHECK (frequency IN ('IMMEDIATE', 'HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY')),
    quiet_hours_start TIME,
    quiet_hours_end TIME,
    timezone VARCHAR2(50) DEFAULT 'Asia/Kolkata',
    language_preference VARCHAR2(10) DEFAULT 'EN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_notif_pref UNIQUE (user_id, notification_type, channel)
) TABLESPACE hdfc_data;

-- Notification campaigns
CREATE TABLE notification_campaigns (
    id NUMBER(19) PRIMARY KEY,
    campaign_id VARCHAR2(50) UNIQUE NOT NULL,
    campaign_name VARCHAR2(255) NOT NULL,
    campaign_type VARCHAR2(30) NOT NULL CHECK (campaign_type IN ('PROMOTIONAL', 'TRANSACTIONAL', 'EDUCATIONAL', 'REGULATORY')),
    description CLOB,
    target_audience CLOB,
    channel VARCHAR2(10) NOT NULL,
    template_id VARCHAR2(50),
    scheduled_start TIMESTAMP,
    scheduled_end TIMESTAMP,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,
    total_recipients NUMBER(10) DEFAULT 0,
    sent_count NUMBER(10) DEFAULT 0,
    delivered_count NUMBER(10) DEFAULT 0,
    failed_count NUMBER(10) DEFAULT 0,
    unsubscribed_count NUMBER(10) DEFAULT 0,
    bounce_count NUMBER(10) DEFAULT 0,
    click_count NUMBER(10) DEFAULT 0,
    conversion_count NUMBER(10) DEFAULT 0,
    status VARCHAR2(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SCHEDULED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    created_by VARCHAR2(50),
    approved_by VARCHAR2(50),
    approval_date DATE,
    budget_allocated NUMBER(15,2),
    cost_per_message NUMBER(10,4),
    total_cost NUMBER(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaign_template FOREIGN KEY (template_id) REFERENCES notification_templates(template_id)
) TABLESPACE hdfc_data;

-- Notification delivery logs
CREATE TABLE notification_delivery_logs (
    id NUMBER(19) PRIMARY KEY,
    log_id VARCHAR2(50) UNIQUE NOT NULL,
    notification_id VARCHAR2(50) NOT NULL,
    delivery_attempt NUMBER(2) DEFAULT 1,
    delivery_status VARCHAR2(20) NOT NULL,
    provider_name VARCHAR2(100),
    provider_message_id VARCHAR2(255),
    provider_response CLOB,
    delivery_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_time_ms NUMBER(10),
    cost NUMBER(10,4) DEFAULT 0.0000,
    error_code VARCHAR2(50),
    error_message VARCHAR2(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_log_notif FOREIGN KEY (notification_id) REFERENCES notifications(notification_id)
) TABLESPACE hdfc_data;

-- Notification analytics
CREATE TABLE notification_analytics (
    id NUMBER(19) PRIMARY KEY,
    analytics_date DATE NOT NULL,
    channel VARCHAR2(10) NOT NULL,
    notification_type VARCHAR2(30) NOT NULL,
    campaign_id VARCHAR2(50),
    total_sent NUMBER(10) DEFAULT 0,
    total_delivered NUMBER(10) DEFAULT 0,
    total_failed NUMBER(10) DEFAULT 0,
    total_bounced NUMBER(10) DEFAULT 0,
    total_opened NUMBER(10) DEFAULT 0,
    total_clicked NUMBER(10) DEFAULT 0,
    total_unsubscribed NUMBER(10) DEFAULT 0,
    delivery_rate NUMBER(5,2) DEFAULT 0.00,
    open_rate NUMBER(5,2) DEFAULT 0.00,
    click_rate NUMBER(5,2) DEFAULT 0.00,
    bounce_rate NUMBER(5,2) DEFAULT 0.00,
    unsubscribe_rate NUMBER(5,2) DEFAULT 0.00,
    avg_delivery_time_ms NUMBER(10) DEFAULT 0,
    total_cost NUMBER(15,2) DEFAULT 0.00,
    cost_per_delivery NUMBER(10,4) DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_notif_analytics UNIQUE (analytics_date, channel, notification_type, campaign_id)
) TABLESPACE hdfc_data;

-- Notification blacklist
CREATE TABLE notification_blacklist (
    id NUMBER(19) PRIMARY KEY,
    blacklist_type VARCHAR2(20) NOT NULL CHECK (blacklist_type IN ('EMAIL', 'PHONE', 'DEVICE_ID', 'USER_ID')),
    blacklist_value VARCHAR2(255) NOT NULL,
    reason VARCHAR2(500),
    blacklisted_by VARCHAR2(50),
    blacklisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active CHAR(1) DEFAULT 'Y' CHECK (is_active IN ('Y', 'N')),
    CONSTRAINT uk_blacklist UNIQUE (blacklist_type, blacklist_value)
) TABLESPACE hdfc_data;

-- Create sequences
CREATE SEQUENCE notification_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE template_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE preference_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE campaign_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE delivery_log_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE analytics_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE blacklist_seq START WITH 1 INCREMENT BY 1 NOCACHE;

-- Create indexes
CREATE INDEX idx_notif_user_id ON notifications(user_id) TABLESPACE hdfc_index;
CREATE INDEX idx_notif_status ON notifications(status) TABLESPACE hdfc_index;
CREATE INDEX idx_notif_type ON notifications(notification_type) TABLESPACE hdfc_index;
CREATE INDEX idx_notif_channel ON notifications(channel) TABLESPACE hdfc_index;
CREATE INDEX idx_notif_scheduled ON notifications(scheduled_at) TABLESPACE hdfc_index;
CREATE INDEX idx_notif_created ON notifications(created_at) TABLESPACE hdfc_index;
CREATE INDEX idx_template_active ON notification_templates(is_active) TABLESPACE hdfc_index;
CREATE INDEX idx_campaign_status ON notification_campaigns(status) TABLESPACE hdfc_index;
CREATE INDEX idx_delivery_log_notif ON notification_delivery_logs(notification_id) TABLESPACE hdfc_index;
CREATE INDEX idx_blacklist_active ON notification_blacklist(is_active) TABLESPACE hdfc_index;

-- Create triggers
CREATE OR REPLACE TRIGGER trg_notifications_id
    BEFORE INSERT ON notifications
    FOR EACH ROW
BEGIN
    IF :NEW.id IS NULL THEN
        :NEW.id := notification_seq.NEXTVAL;
    END IF;
    IF :NEW.notification_id IS NULL THEN
        :NEW.notification_id := 'NOTIF' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(notification_seq.CURRVAL, 8, '0');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW
BEGIN
    :NEW.updated_at := CURRENT_TIMESTAMP;
    
    -- Set timestamps based on status changes
    IF :NEW.status = 'SENT' AND :OLD.status != 'SENT' THEN
        :NEW.sent_at := CURRENT_TIMESTAMP;
    END IF;
    
    IF :NEW.status = 'DELIVERED' AND :OLD.status != 'DELIVERED' THEN
        :NEW.delivered_at := CURRENT_TIMESTAMP;
    END IF;
    
    IF :NEW.status = 'READ' AND :OLD.status != 'READ' THEN
        :NEW.read_at := CURRENT_TIMESTAMP;
    END IF;
END;
/

-- Procedure to process scheduled notifications
CREATE OR REPLACE PROCEDURE process_scheduled_notifications IS
    CURSOR scheduled_notifs IS
        SELECT notification_id, user_id, channel, recipient, message
        FROM notifications
        WHERE status = 'PENDING'
        AND scheduled_at <= CURRENT_TIMESTAMP
        AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);
BEGIN
    FOR notif IN scheduled_notifs LOOP
        -- Check if user is not in blacklist
        IF NOT is_blacklisted(notif.channel, notif.recipient) THEN
            -- Update status to processing
            UPDATE notifications
            SET status = 'PROCESSING',
                updated_at = CURRENT_TIMESTAMP
            WHERE notification_id = notif.notification_id;
            
            -- Here you would integrate with actual notification providers
            -- For now, just mark as sent
            UPDATE notifications
            SET status = 'SENT',
                sent_at = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE notification_id = notif.notification_id;
        ELSE
            -- Mark as failed due to blacklist
            UPDATE notifications
            SET status = 'FAILED',
                failure_reason = 'Recipient blacklisted',
                updated_at = CURRENT_TIMESTAMP
            WHERE notification_id = notif.notification_id;
        END IF;
    END LOOP;
    
    COMMIT;
END;
/

-- Function to check blacklist
CREATE OR REPLACE FUNCTION is_blacklisted(
    p_channel IN VARCHAR2,
    p_recipient IN VARCHAR2
) RETURN BOOLEAN IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(*)
    INTO v_count
    FROM notification_blacklist
    WHERE blacklist_type = UPPER(p_channel)
    AND blacklist_value = p_recipient
    AND is_active = 'Y'
    AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);
    
    RETURN v_count > 0;
END;
/