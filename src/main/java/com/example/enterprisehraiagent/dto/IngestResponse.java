package com.example.enterprisehraiagent.dto;

public record IngestResponse(
        String filename,
        int rawDocumentCount,
        int chunkCount,
        String message
) {
}
