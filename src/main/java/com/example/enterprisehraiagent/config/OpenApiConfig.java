package com.example.enterprisehraiagent.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 文档配置。
 *
 * <p>springdoc 会扫描 Spring WebFlux 的 Controller、请求 DTO 和响应 DTO，
 * 自动生成 `/v3/api-docs` JSON。Swagger UI 再读取这份 JSON，在
 * `/swagger-ui/index.html` 提供可视化接口调试页面。</p>
 *
 * <p>这里的 Bean 只负责补充项目信息、服务地址和外部文档说明；
 * 具体接口路径、参数、响应结构仍由 springdoc 根据 Controller 自动推导，
 * 避免在业务代码里写大量重复的接口描述。</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI enterpriseHrOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise HR AI Agent API")
                        .description("企业 HR 智能体后端接口文档，包含 RAG 知识库、SSE 流式对话、员工和请假管理接口。")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Enterprise HR AI Agent")
                                .email("hr-ai-agent@example.com"))
                        .license(new License()
                                .name("Internal Demo")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发环境")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("项目说明文档")
                        .url("https://github.com/levi04206/Enterprise-HR-AI-Agent"));
    }
}
