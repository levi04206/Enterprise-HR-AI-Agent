package com.example.enterprisehraiagent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 流式对话核心引擎。
 *
 * <p>这个服务把 RAG 和 Function Calling 组合成一个 HR Agent：
 * 1. 先用用户问题检索企业制度知识库；
 * 2. 把 TopK 片段作为 Context 注入 System Prompt；
 * 3. ChatClient 已经在 AiConfig 中注册 HR 工具；
 * 4. 模型判断需要个人数据时，会自动发起工具调用；
 * 5. stream().content() 将模型输出 token 流式返回。</p>
 */
@Service
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final int topK;

    public ChatService(ChatClient hrChatClient,
                       VectorStore vectorStore,
                       @Value("${app.ai.top-k:3}") int topK) {
        this.chatClient = hrChatClient;
        this.vectorStore = vectorStore;
        this.topK = topK;
    }

    public Flux<String> streamChat(String userMessage) {
        List<Document> relevantDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(userMessage)
                        .topK(topK)
                        .build()
        );

        String context = buildContext(relevantDocuments);
        String systemPrompt = buildSystemPrompt(context);

        return chatClient.prompt()
                // System Prompt 是智能体的稳定行为约束，也是 RAG Context 的注入点。
                .system(systemPrompt)
                // User Prompt 保留用户原始问题，避免把用户问题混进系统规则中。
                .user(userMessage)
                // AiConfig 中 defaultToolNames 已注册工具，所以这里无需再次 toolNames。
                .stream()
                .content();
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

    private String buildSystemPrompt(String context) {
        return """
                你是公司的高级 HR 助理。

                回答规则：
                1. 请优先基于 Context 中的企业制度回答公司规章制度问题。
                2. 如果 Context 不足，请明确说明“当前知识库没有足够依据”，不要编造制度条款。
                3. 如果用户询问员工邮箱、部门、联系方式、年假余额、请假审批进度等个人数据，请调用相应工具获取实时数据。
                4. 回答要简洁、准确、中文优先，必要时给出可执行建议。

                Context：
                %s
                """.formatted(context);
    }
}
