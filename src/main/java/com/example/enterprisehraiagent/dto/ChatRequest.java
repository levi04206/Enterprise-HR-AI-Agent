package com.example.enterprisehraiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "message 不能为空")
        @Schema(description = "用户发送给 HR Agent 的问题")
        String message,

        @Schema(description = "可选会话 ID，传入后会保存上下文历史")
        Long sessionId
) {
}
