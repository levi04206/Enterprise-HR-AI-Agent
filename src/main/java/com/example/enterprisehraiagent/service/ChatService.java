package com.example.enterprisehraiagent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import com.example.enterprisehraiagent.entity.ChatMessage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 流式对话核心引擎。
 *
 * <p>这个服务把 RAG 和 Function Calling 组合成一个 HR Agent：
 * 先检索知识库，把 TopK 制度片段注入 System Prompt，再让模型按需调用 HR 工具。
 * 如果请求携带 sessionId，则同时保存用户问题和助手完整回复。</p>
 */
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatHistoryService chatHistoryService;
    private final RagSearchLogService ragSearchLogService;
    private final int topK;
    private final int historySize;

    public ChatService(ChatClient hrChatClient,
                       VectorStore vectorStore,
                       ChatHistoryService chatHistoryService,
                       RagSearchLogService ragSearchLogService,
                       @Value("${app.ai.top-k:3}") int topK,
                       @Value("${app.ai.history-size:8}") int historySize) {
        this.chatClient = hrChatClient;
        this.vectorStore = vectorStore;
        this.chatHistoryService = chatHistoryService;
        this.ragSearchLogService = ragSearchLogService;
        this.topK = topK;
        this.historySize = historySize;
    }

    public Flux<String> streamChat(String userMessage, Long sessionId) {
        List<ChatMessage> recentMessages = sessionId == null
                ? List.of()
                : chatHistoryService.listRecentMessages(sessionId, historySize);

        if (sessionId != null) {
            chatHistoryService.appendMessage(sessionId, "USER", userMessage);
        }

        List<Document> relevantDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userMessage)
                        .topK(topK)
                        .build()
        );
        ragSearchLogService.saveSearchLogs(sessionId, userMessage, relevantDocuments);

        String context = buildContext(relevantDocuments);
        String conversationHistory = buildConversationHistory(recentMessages);
        String systemPrompt = buildSystemPrompt(context, conversationHistory);
        StringBuilder assistantResponse = new StringBuilder();

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .content()
                .doOnNext(assistantResponse::append)
                .doOnComplete(() -> {
                    if (sessionId != null) {
                        chatHistoryService.appendMessage(sessionId, "ASSISTANT", assistantResponse.toString());
                    }
                });
    }

    private String buildContext(List<Document> relevantDocuments) {
        if (relevantDocuments == null || relevantDocuments.isEmpty()) {
            return "当前知识库没有检索到相关制度片段。";
        }

        return IntStream.range(0, relevantDocuments.size())
                .mapToObj(index -> {
                    Document document = relevantDocuments.get(index);
                    return "【制度片段 %d】\n%s".formatted(index + 1, document.getText());
                })
                .collect(Collectors.joining("\n\n"));
    }

    private String buildConversationHistory(List<ChatMessage> recentMessages) {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "当前会话暂无历史消息。";
        }

        return recentMessages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> "%s：%s".formatted(normalizeRole(message.getRole()), message.getContent()))
                .collect(Collectors.joining("\n"));
    }

    private String normalizeRole(String role) {
        return switch (role) {
            case "USER" -> "用户";
            case "ASSISTANT" -> "HR 助理";
            default -> role;
        };
    }

    private String buildSystemPrompt(String context, String conversationHistory) {
        return """
                你是公司的高级 HR 助理。

                回答规则：
                1. 请优先基于 Context 中的企业制度回答公司规章制度问题。
                2. 如果 Context 不足，请明确说明“当前知识库没有足够依据”，不要编造制度条款。
                3. 如果用户询问员工邮箱、部门、联系方式、年假余额、请假审批进度等个人数据，请调用相应工具获取实时数据。
                4. Conversation History 只用于理解上下文和指代关系，不要把历史消息当成制度依据。
                5. 回答要简洁、准确、中文优先，必要时给出可执行建议。

                Context：
                %s

                Conversation History：
                %s
                """.formatted(context, conversationHistory);
    }
}
