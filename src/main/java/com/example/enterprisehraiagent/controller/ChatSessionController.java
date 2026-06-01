package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.ChatMessageResponse;
import com.example.enterprisehraiagent.dto.ChatSessionCreateRequest;
import com.example.enterprisehraiagent.dto.ChatSessionResponse;
import com.example.enterprisehraiagent.service.ChatHistoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat/sessions")
public class ChatSessionController {

    private final ChatHistoryService chatHistoryService;

    /**
     * 注入会话历史服务。
     */
    public ChatSessionController(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 创建新的聊天会话并返回会话 ID。
     */
    @PostMapping
    public Map<String, Long> create(@RequestBody ChatSessionCreateRequest request) {
        return Map.of("id", chatHistoryService.createSession(request));
    }

    /**
     * 查询所有聊天会话。
     */
    @GetMapping
    public List<ChatSessionResponse> list() {
        return chatHistoryService.listSessions();
    }

    /**
     * 查询指定会话下的历史消息。
     */
    @GetMapping("/{sessionId}/messages")
    public List<ChatMessageResponse> messages(@PathVariable Long sessionId) {
        return chatHistoryService.listMessages(sessionId);
    }
}
