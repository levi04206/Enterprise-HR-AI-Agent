package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.ToolCallLog;

import java.time.LocalDateTime;

public record ToolCallLogResponse(
        Long id,
        String toolName,
        String argumentsJson,
        String resultText,
        Boolean success,
        String errorMessage,
        Long durationMs,
        LocalDateTime createdAt
) {
    public static ToolCallLogResponse from(ToolCallLog log) {
        return new ToolCallLogResponse(
                log.getId(),
                log.getToolName(),
                log.getArgumentsJson(),
                log.getResultText(),
                log.getSuccess(),
                log.getErrorMessage(),
                log.getDurationMs(),
                log.getCreatedAt()
        );
    }
}
