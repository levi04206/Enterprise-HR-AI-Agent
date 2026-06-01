package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.entity.RagSearchLog;
import com.example.enterprisehraiagent.entity.ToolCallLog;
import com.example.enterprisehraiagent.mapper.RagSearchLogMapper;
import com.example.enterprisehraiagent.mapper.ToolCallLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ObservabilityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RagSearchLogMapper ragSearchLogMapper;

    @Autowired
    private ToolCallLogMapper toolCallLogMapper;

    @Test
    void ragSearchLogsShouldReturnRecentReferences() {
        RagSearchLog log = new RagSearchLog();
        log.setSessionId(1L);
        log.setUserMessage("考勤制度是什么");
        log.setRankNo(1);
        log.setDocumentId(10L);
        log.setFilename("员工考勤管理办法.txt");
        log.setChunkIndex(2);
        log.setVectorId("vector-1");
        log.setSimilarityScore(0.91);
        log.setContentPreview("迟到规则片段");
        log.setCreatedAt(LocalDateTime.now());
        ragSearchLogMapper.insert(log);

        webTestClient.get()
                .uri("/api/v1/observability/rag-search-logs?sessionId=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].filename").isEqualTo("员工考勤管理办法.txt")
                .jsonPath("$[0].rankNo").isEqualTo(1)
                .jsonPath("$[0].contentPreview").isEqualTo("迟到规则片段");
    }

    @Test
    void toolCallLogsShouldReturnRecentToolInvocations() {
        ToolCallLog log = new ToolCallLog();
        log.setToolName("getLeaveBalanceTool");
        log.setArgumentsJson("{\"employeeName\":\"张三\"}");
        log.setResultText("张三剩余 10 天年假");
        log.setSuccess(true);
        log.setDurationMs(12L);
        log.setCreatedAt(LocalDateTime.now());
        toolCallLogMapper.insert(log);

        webTestClient.get()
                .uri("/api/v1/observability/tool-call-logs?toolName=getLeaveBalanceTool")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].toolName").isEqualTo("getLeaveBalanceTool")
                .jsonPath("$[0].success").isEqualTo(true)
                .jsonPath("$[0].resultText").isEqualTo("张三剩余 10 天年假");
    }

    @Test
    void toolCallLogsShouldFuzzyMatchToolNameKeyword() {
        ToolCallLog matched = new ToolCallLog();
        matched.setToolName("getLeaveBalanceTool");
        matched.setArgumentsJson("{\"employeeName\":\"张三\"}");
        matched.setResultText("张三剩余 10 天年假");
        matched.setSuccess(true);
        matched.setDurationMs(12L);
        matched.setCreatedAt(LocalDateTime.now());
        toolCallLogMapper.insert(matched);

        ToolCallLog unmatched = new ToolCallLog();
        unmatched.setToolName("getEmployeeContactTool");
        unmatched.setArgumentsJson("{\"employeeName\":\"李四\"}");
        unmatched.setResultText("李四邮箱 lisi@example.com");
        unmatched.setSuccess(true);
        unmatched.setDurationMs(8L);
        unmatched.setCreatedAt(LocalDateTime.now().minusSeconds(1));
        toolCallLogMapper.insert(unmatched);

        webTestClient.get()
                .uri("/api/v1/observability/tool-call-logs?toolName=leave")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].toolName").isEqualTo("getLeaveBalanceTool")
                .jsonPath("$[1]").doesNotExist();
    }
}
