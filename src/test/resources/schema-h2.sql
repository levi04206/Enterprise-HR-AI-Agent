DROP TABLE IF EXISTS leave_record;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS knowledge_document;

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
