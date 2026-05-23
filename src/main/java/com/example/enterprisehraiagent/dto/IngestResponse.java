package com.example.enterprisehraiagent.dto;

public record IngestResponse(
        Long documentId,
        String filename,
        int rawDocumentCount,
        int chunkCount,
        String message
) {
}
