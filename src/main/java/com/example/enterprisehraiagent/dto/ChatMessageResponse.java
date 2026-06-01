package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        @Schema(description = "聊天消息主键 ID")
        Long id,
        @Schema(description = "所属聊天会话 ID")
        Long sessionId,
        @Schema(description = "消息角色，例如 USER 或 ASSISTANT")
        String role,
        @Schema(description = "消息正文内容")
        String content,
        @Schema(description = "消息创建时间")
        LocalDateTime createdAt
) {

    /**
     * 将聊天消息实体转换为前端响应对象。
     */
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
