CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    department VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    annual_leave_total INT NOT NULL DEFAULT 0,
    annual_leave_used INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_employee_name (name)
);

CREATE TABLE IF NOT EXISTS leave_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    emp_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    KEY idx_leave_record_emp_id (emp_id),
    CONSTRAINT fk_leave_record_employee FOREIGN KEY (emp_id) REFERENCES employee (id)
);

CREATE TABLE IF NOT EXISTS knowledge_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    raw_document_count INT NOT NULL DEFAULT 0,
    chunk_count INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_knowledge_document_created_at (created_at)
);
