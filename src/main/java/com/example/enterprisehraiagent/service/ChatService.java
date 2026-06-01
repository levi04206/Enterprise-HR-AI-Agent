package com.example.enterprisehraiagent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import com.example.enterprisehraiagent.entity.ChatMessage;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatHistoryService chatHistoryService;
    private final RagSearchLogService ragSearchLogService;
    private final int topK;
    private final int historySize;

    /**
     * 注入模型客户端、向量库、会话服务和观测日志服务。
     */
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

    /**
     * 执行一次 HR Agent 流式对话，并在有会话 ID 时保存问答历史（已修复响应式线程阻塞问题）。
     */
    public Flux<String> streamChat(String userMessage, Long sessionId) {
        // 1. 将所有前期准备工作（查历史、写用户消息、RAG检索）打包成一个 Mono 任务
        return Mono.fromCallable(() -> {
                    // 1. 捞历史消息
                    List<ChatMessage> recentMessages = sessionId == null
                            ? List.of()
                            : chatHistoryService.listRecentMessages(sessionId, historySize);

                    // 2. 把用户刚说的话存进数据库
                    if (sessionId != null) {
                        chatHistoryService.appendMessage(sessionId, "USER", userMessage);
                    }

                    // 3. 翻书包：去向量库查公司规章制度 (RAG)
                    String context = searchAndBuildContext(sessionId, userMessage);

                    // 4. 格式化历史记录和最终的系统提示词
                    String conversationHistory = buildConversationHistory(recentMessages);
                    return buildSystemPrompt(context, conversationHistory);
                })
                // 2. 🌟 核心点：强制切换线程池，把上面那堆同步阻塞的脏活累活扔出去
                .subscribeOn(Schedulers.boundedElastic())
                // 3. 拿到提示词后，无缝平铺接入大模型的流式 Flux 链路
                .flatMapMany(systemPrompt -> {
                    StringBuilder assistantResponse = new StringBuilder();

                    return chatClient.prompt()
                            .system(systemPrompt)
                            .user(userMessage)
                            .stream()
                            .content()
                            .doOnNext(assistantResponse::append)
                            // 4. 当大模型全部吐完词后，保存助手回复到数据库
                            .doOnComplete(() -> {
                                if (sessionId != null) {
                                    // ⚠️ 注意：落库是阻塞操作，必须专门调度到弹性线程池中异步执行
                                    Schedulers.boundedElastic().schedule(() -> {
                                        chatHistoryService.appendMessage(sessionId, "ASSISTANT", assistantResponse.toString());
                                    });
                                }
                            });
                });
    }

    /**
     * 执行 RAG 检索并构造上下文，检索失败时降级为无知识库上下文。
     */
    private String searchAndBuildContext(Long sessionId, String userMessage) {
        try {
            List<Document> relevantDocuments = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(userMessage)
                            .topK(topK)
                            .build()
            );
            ragSearchLogService.saveSearchLogs(sessionId, userMessage, relevantDocuments);
            return buildContext(relevantDocuments);
        } catch (Exception error) {
            log.warn("RAG search failed, continue chat without knowledge context. sessionId={}", sessionId, error);
            return "当前知识库检索暂不可用。个人数据类问题请继续调用 HR 工具获取实时数据；制度类问题请明确说明当前知识库没有足够依据。";
        }
    }

    /**
     * 将向量检索命中的文档片段拼接成模型上下文。
     */
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

    /**
     * 将最近聊天记录整理成模型可读的对话历史文本。
     */
    private String buildConversationHistory(List<ChatMessage> recentMessages) {
        if (recentMessages == null || recentMessages.isEmpty()) {
            return "当前会话暂无历史消息。";
        }

        return recentMessages.stream()
                .filter(message -> StringUtils.hasText(message.getContent()))
                .map(message -> "%s：%s".formatted(normalizeRole(message.getRole()), message.getContent()))
                .collect(Collectors.joining("\n"));
    }

    /**
     * 将内部消息角色转换成中文展示角色。
     */
    private String normalizeRole(String role) {
        return switch (role) {
            case "USER" -> "用户";
            case "ASSISTANT" -> "HR 助理";
            default -> role;
        };
    }

    /**
     * 构建最终传给模型的系统提示词。
     */
    private String buildSystemPrompt(String context, String conversationHistory) {
        return """
                你是公司的高级 HR 助理。

                回答规则：
                1. 请优先基于 Context 中的企业制度回答公司规章制度问题。
                2. 如果 Context 不足，请明确说明“当前知识库没有足够依据”，不要编造制度条款。
                3. 如果用户询问员工邮箱、部门、联系方式、年假余额、请假审批进度等个人数据，请调用相应工具获取实时数据。
                4. 如果用户询问公司员工总数、员工清单、员工详细信息，请调用员工概览工具获取实时数据。
                5. Conversation History 只用于理解上下文和指代关系，不要把历史消息当成制度依据。
                6. 回答要简洁、准确、中文优先，必要时给出可执行建议。

                Context：
                %s

                Conversation History：
                %s
                """.formatted(context, conversationHistory);
    }
}
