package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.KnowledgeDocument;

import java.time.LocalDateTime;

public record KnowledgeDocumentResponse(
        Long id,
        String filename,
        String contentType,
        Integer rawDocumentCount,
        Integer chunkCount,
        String status,
        LocalDateTime createdAt
) {

    public static KnowledgeDocumentResponse from(KnowledgeDocument document) {
        return new KnowledgeDocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getRawDocumentCount(),
                document.getChunkCount(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }
}
