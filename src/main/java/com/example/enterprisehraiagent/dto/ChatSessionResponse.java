package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.ChatSession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        @Schema(description = "聊天会话主键 ID")
        Long id,
        @Schema(description = "聊天会话标题")
        String title,
        @Schema(description = "会话创建时间")
        LocalDateTime createdAt,
        @Schema(description = "会话最近更新时间")
        LocalDateTime updatedAt
) {

    /**
     * 将聊天会话实体转换为前端响应对象。
     */
    public static ChatSessionResponse from(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
