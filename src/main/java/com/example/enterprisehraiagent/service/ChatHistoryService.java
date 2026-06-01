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

    /**
     * 注入会话和消息数据访问对象。
     */
    public ChatHistoryService(ChatSessionMapper chatSessionMapper, ChatMessageMapper chatMessageMapper) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    /**
     * 创建聊天会话并初始化创建时间和更新时间。
     */
    public Long createSession(ChatSessionCreateRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setTitle(StringUtils.hasText(request.title()) ? request.title().trim() : "新的 HR 对话");
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        chatSessionMapper.insert(session);
        return session.getId();
    }

    /**
     * 按最近更新时间倒序查询会话列表。
     */
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

    /**
     * 查询指定会话的完整消息历史。
     */
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

    /**
     * 查询最近若干条消息，用于注入模型上下文。
     */
    public List<ChatMessage> listRecentMessages(Long sessionId, int limit) {
        ensureSessionExists(sessionId);
        if (limit <= 0) {
            return List.of();
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreatedAt)
                        .orderByDesc(ChatMessage::getId)
                        .last("LIMIT " + limit)
        );

        return messages.reversed();
    }

    /**
     * 向会话追加一条用户或助手消息，并刷新会话更新时间。
     */
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

    /**
     * 校验会话是否存在，不存在时抛出业务异常。
     */
    public void ensureSessionExists(Long sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }
        if (chatSessionMapper.selectById(sessionId) == null) {
            throw new IllegalArgumentException("对话会话不存在：" + sessionId);
        }
    }
}
