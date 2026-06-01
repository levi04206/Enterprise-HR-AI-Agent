package com.example.enterprisehraiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatSessionCreateRequest(
        @Schema(description = "聊天会话标题")
        String title
) {
}
