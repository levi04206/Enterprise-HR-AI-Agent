package com.example.enterprisehraiagent.dto;

public record AiDiagnosticResponse(
        String provider,
        String status,
        String message,
        Integer embeddingDimensions
) {

    public static AiDiagnosticResponse ok(String provider, String message) {
        return new AiDiagnosticResponse(provider, "UP", message, null);
    }

    public static AiDiagnosticResponse embeddingOk(String provider, int dimensions) {
        return new AiDiagnosticResponse(provider, "UP", "Embedding 调用成功", dimensions);
    }

    public static AiDiagnosticResponse down(String provider, String message) {
        return new AiDiagnosticResponse(provider, "DOWN", message, null);
    }
}
