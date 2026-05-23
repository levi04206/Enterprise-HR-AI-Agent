package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.dto.ChatSessionCreateRequest;
import com.example.enterprisehraiagent.entity.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ChatHistoryServiceTest {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Test
    void listRecentMessagesShouldReturnLimitedMessagesInOriginalOrder() {
        Long sessionId = chatHistoryService.createSession(new ChatSessionCreateRequest("上下文测试"));

        chatHistoryService.appendMessage(sessionId, "USER", "第一轮问题");
        chatHistoryService.appendMessage(sessionId, "ASSISTANT", "第一轮回答");
        chatHistoryService.appendMessage(sessionId, "USER", "第二轮问题");

        List<ChatMessage> recentMessages = chatHistoryService.listRecentMessages(sessionId, 2);

        assertThat(recentMessages)
                .extracting(ChatMessage::getContent)
                .containsExactly("第一轮回答", "第二轮问题");
    }
}
