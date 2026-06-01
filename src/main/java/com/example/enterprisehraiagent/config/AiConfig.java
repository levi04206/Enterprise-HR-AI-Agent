package com.example.enterprisehraiagent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 核心配置。
 *
 * <p>Spring Boot 会根据 spring-ai-starter-model-openai 自动创建 ChatModel
 * 和原型作用域的 ChatClient.Builder。这里我们只关心“智能体默认能力”的编排：
 * 1. 通过 defaultToolNames 注册工具名；
 * 2. 工具名对应 HrToolsConfig 中的 Function Bean；
 * 3. LLM 在需要个人数据时，会按模型的 Function Calling / Tool Calling 协议
 *    选择并调用这些 Bean。</p>
 *
 * <p>Spring AI 也提供 Advisor 机制，可把记忆、RAG 检索、日志观测等横切逻辑
 * 插入 ChatClient 调用链。为了让 RAG 的 TopK 检索过程更直观，本 Demo 在
 * ChatService 中手动检索 VectorStore 并拼接 System Prompt；生产项目可以继续
 * 演进为 QuestionAnswerAdvisor 或自定义 Advisor。</p>
 */
@Configuration
public class AiConfig {

    /**
     * 创建 HR Agent 使用的 ChatClient，并注册可供模型调用的工具。
     */
    @Bean
    public ChatClient hrChatClient(ChatClient.Builder builder) {
        return builder
                .defaultToolNames("getEmployeeContactTool", "getLeaveBalanceTool", "getCompanyEmployeeSummaryTool")
                .build();
    }
}
