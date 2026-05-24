package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.RagSearchLog;

import java.time.LocalDateTime;

public record RagSearchLogResponse(
        Long id,
        Long sessionId,
        String userMessage,
        Integer rankNo,
        Long documentId,
        String filename,
        Integer chunkIndex,
        String vectorId,
        Double similarityScore,
        String contentPreview,
        LocalDateTime createdAt
) {
    public static RagSearchLogResponse from(RagSearchLog log) {
        return new RagSearchLogResponse(
                log.getId(),
                log.getSessionId(),
                log.getUserMessage(),
                log.getRankNo(),
                log.getDocumentId(),
                log.getFilename(),
                log.getChunkIndex(),
                log.getVectorId(),
                log.getSimilarityScore(),
                log.getContentPreview(),
                log.getCreatedAt()
        );
    }
}
