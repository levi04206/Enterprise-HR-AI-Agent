ALTER TABLE knowledge_chunk
    ADD COLUMN content LONGTEXT NULL AFTER chunk_index;
