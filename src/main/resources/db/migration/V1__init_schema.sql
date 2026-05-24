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

CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    document_id BIGINT NOT NULL,
    vector_id VARCHAR(128) NOT NULL,
    chunk_index INT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_knowledge_chunk_vector_id (vector_id),
    KEY idx_knowledge_chunk_document_id (document_id),
    CONSTRAINT fk_knowledge_chunk_document FOREIGN KEY (document_id) REFERENCES knowledge_document (id)
);

CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    KEY idx_chat_session_updated_at (updated_at)
);

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_chat_message_session_id (session_id),
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id) REFERENCES chat_session (id)
);
