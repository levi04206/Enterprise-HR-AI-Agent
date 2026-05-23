package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.ChatSession;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ChatSessionResponse from(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
