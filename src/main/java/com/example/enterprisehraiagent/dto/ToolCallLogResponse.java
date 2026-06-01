package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.ToolCallLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ToolCallLogResponse(
        @Schema(description = "工具调用日志主键 ID")
        Long id,
        @Schema(description = "被调用的工具名称")
        String toolName,
        @Schema(description = "工具调用入参 JSON")
        String argumentsJson,
        @Schema(description = "工具调用结果文本")
        String resultText,
        @Schema(description = "工具调用是否成功")
        Boolean success,
        @Schema(description = "工具调用失败时的错误消息")
        String errorMessage,
        @Schema(description = "工具调用耗时，单位毫秒")
        Long durationMs,
        @Schema(description = "日志创建时间")
        LocalDateTime createdAt
) {
    /**
     * 将工具调用日志实体转换为前端响应对象。
     */
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
