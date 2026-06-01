package com.example.enterprisehraiagent.service;

import com.example.enterprisehraiagent.dto.AiDiagnosticResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 配置诊断服务。
 *
 * <p>这个服务只做最小调用，用于判断 Chat 和 Embedding 两段配置是否可用。
 * 它不会输出 API Key，也不会把异常堆栈直接返回给前端，避免泄露敏感配置。</p>
 */
@Service
public class AiDiagnosticsService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final String chatModel;
    private final String embeddingModelName;

    /**
     * 注入聊天客户端、向量模型和模型名称配置。
     */
    public AiDiagnosticsService(ChatClient hrChatClient,
                                EmbeddingModel embeddingModel,
                                @Value("${spring.ai.openai.chat.options.model:unknown}") String chatModel,
                                @Value("${spring.ai.openai.embedding.options.model:unknown}") String embeddingModelName) {
        this.chatClient = hrChatClient;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.embeddingModelName = embeddingModelName;
    }

    /**
     * 发送最小聊天请求，判断 Chat 模型链路是否可用。
     */
    public AiDiagnosticResponse checkChat() {
        try {
            String content = chatClient.prompt()
                    .system("你是一个健康检查探针。只允许回复 OK，不要输出其他内容。")
                    .user("请回复 OK")
                    .call()
                    .content();

            if (!StringUtils.hasText(content)) {
                return AiDiagnosticResponse.down("DeepSeek Chat: " + chatModel, "模型返回为空");
            }
            return AiDiagnosticResponse.ok("DeepSeek Chat: " + chatModel, "Chat 调用成功，模型返回：" + content.trim());
        } catch (Exception error) {
            return AiDiagnosticResponse.down("DeepSeek Chat: " + chatModel, sanitize(error));
        }
    }

    /**
     * 发送最小向量化请求，判断 Embedding 模型链路是否可用。
     */
    public AiDiagnosticResponse checkEmbedding() {
        try {
            float[] vector = embeddingModel.embed("企业 HR AI Agent embedding health check");
            if (vector == null || vector.length == 0) {
                return AiDiagnosticResponse.down("DashScope Embedding: " + embeddingModelName, "Embedding 返回空向量");
            }
            return AiDiagnosticResponse.embeddingOk("DashScope Embedding: " + embeddingModelName, vector.length);
        } catch (Exception error) {
            return AiDiagnosticResponse.down("DashScope Embedding: " + embeddingModelName, sanitize(error));
        }
    }

    /**
     * 清理异常消息中的敏感认证信息。
     */
    private String sanitize(Exception error) {
        String message = error.getMessage();
        if (!StringUtils.hasText(message)) {
            return error.getClass().getSimpleName();
        }
        return message.replaceAll("(?i)(api[-_ ]?key|authorization|bearer)\\s*[:=]\\s*[^\\s,}]+", "$1=***");
    }
}
