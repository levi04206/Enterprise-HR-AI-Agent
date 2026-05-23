package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.ChatMessageResponse;
import com.example.enterprisehraiagent.dto.ChatSessionCreateRequest;
import com.example.enterprisehraiagent.dto.ChatSessionResponse;
import com.example.enterprisehraiagent.entity.ChatMessage;
import com.example.enterprisehraiagent.entity.ChatSession;
import com.example.enterprisehraiagent.mapper.ChatMessageMapper;
import com.example.enterprisehraiagent.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ChatHistoryService(ChatSessionMapper chatSessionMapper, ChatMessageMapper chatMessageMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    public Long createSession(ChatSessionCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setTitle(StringUtils.hasText(request.title()) ? request.title().trim() : "新的 HR 对话");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionMapper.insert(session);
        return session.getId();
    }

    public List<ChatSessionResponse> listSessions() {
        return chatSessionMapper.selectList(
                        new LambdaQueryWrapper<ChatSession>()
                                .orderByDesc(ChatSession::getUpdatedAt)
                                .orderByDesc(ChatSession::getId)
                )
                .stream()
                .map(ChatSessionResponse::from)
                .toList();
    }

    public List<ChatMessageResponse> listMessages(Long sessionId) {
        ensureSessionExists(sessionId);
        return chatMessageMapper.selectList(
                        new LambdaQueryWrapper<ChatMessage>()
                                .eq(ChatMessage::getSessionId, sessionId)
                                .orderByAsc(ChatMessage::getCreatedAt)
                                .orderByAsc(ChatMessage::getId)
                )
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    public void appendMessage(Long sessionId, String role, String content) {
        if (sessionId == null || !StringUtils.hasText(content)) {
            return;
        }
        ensureSessionExists(sessionId);

        LocalDateTime now = LocalDateTime.now();
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setCreatedAt(now);
        chatMessageMapper.insert(message);

        ChatSession session = chatSessionMapper.selectById(sessionId);
        session.setUpdatedAt(now);
        chatSessionMapper.updateById(session);
    }

    public void ensureSessionExists(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        if (chatSessionMapper.selectById(sessionId) == null) {
            throw new IllegalArgumentException("对话会话不存在：" + sessionId);
        }
    }
}
