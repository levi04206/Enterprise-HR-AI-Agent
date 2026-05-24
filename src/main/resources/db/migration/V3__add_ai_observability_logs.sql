CREATE TABLE IF NOT EXISTS rag_search_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NULL,
    user_message TEXT NOT NULL,
    rank_no INT NOT NULL,
    document_id BIGINT NULL,
    filename VARCHAR(255) NULL,
    chunk_index INT NULL,
    vector_id VARCHAR(128) NULL,
    similarity_score DOUBLE NULL,
    content_preview TEXT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_rag_search_log_session_id (session_id),
    KEY idx_rag_search_log_created_at (created_at),
    CONSTRAINT fk_rag_search_log_session FOREIGN KEY (session_id) REFERENCES chat_session (id)
);

CREATE TABLE IF NOT EXISTS tool_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_name VARCHAR(128) NOT NULL,
    arguments_json TEXT NULL,
    result_text TEXT NULL,
    success BOOLEAN NOT NULL,
    error_message TEXT NULL,
    duration_ms BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    KEY idx_tool_call_log_tool_name (tool_name),
    KEY idx_tool_call_log_created_at (created_at)
);
