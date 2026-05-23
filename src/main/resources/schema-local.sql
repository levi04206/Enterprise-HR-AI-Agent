DROP TABLE IF EXISTS leave_record;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS knowledge_document;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_session;

CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    department VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    annual_leave_total INT NOT NULL DEFAULT 0,
    annual_leave_used INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_employee_name (name)
);

CREATE TABLE leave_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    KEY idx_leave_record_emp_id (emp_id)
);

CREATE TABLE knowledge_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    raw_document_count INT NOT NULL DEFAULT 0,
    chunk_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_knowledge_document_created_at (created_at)
);

CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_chat_session_updated_at (updated_at)
);

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_chat_message_session_id (session_id)
);
