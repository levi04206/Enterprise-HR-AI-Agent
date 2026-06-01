package com.example.enterprisehraiagent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@ActiveProfiles({"test", "secure"})
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityWebFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void businessApiShouldRejectMissingEmployeeHeaderWhenSecurityEnabled() {
        webTestClient.get()
                .uri("/api/v1/employees")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.message").isEqualTo("缺少员工身份请求头")
                .jsonPath("$.path").isEqualTo("/api/v1/employees");
    }

    @Test
    void businessApiShouldAllowEmployeeHeader() {
        webTestClient.get()
                .uri("/api/v1/employees")
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void adminApiShouldRejectNonAdminRole() {
        webTestClient.post()
                .uri("/api/v1/employees")
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .bodyValue(Map.of(
                        "name", "赵六",
                        "department", "财务部",
                        "email", "zhaoliu@example.com",
                        "annualLeaveTotal", 10,
                        "annualLeaveUsed", 0
                ))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.code").isEqualTo("FORBIDDEN")
                .jsonPath("$.message").isEqualTo("当前角色无权访问该接口")
                .jsonPath("$.path").isEqualTo("/api/v1/employees");
    }

    @Test
    void diagnosticsApiShouldRejectNonAdminRole() {
        webTestClient.get()
                .uri("/api/v1/diagnostics/chat")
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FORBIDDEN")
                .jsonPath("$.path").isEqualTo("/api/v1/diagnostics/chat");
    }

    @Test
    void observabilityApiShouldRejectNonAdminRole() {
        webTestClient.get()
                .uri("/api/v1/observability/rag-search-logs")
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FORBIDDEN")
                .jsonPath("$.path").isEqualTo("/api/v1/observability/rag-search-logs");
    }

    @Test
    void knowledgeWriteApiShouldRejectNonAdminRole() {
        webTestClient.delete()
                .uri("/api/v1/knowledge/documents/{id}", 1)
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.code").isEqualTo("FORBIDDEN")
                .jsonPath("$.path").isEqualTo("/api/v1/knowledge/documents/1");
    }

    @Test
    void knowledgeReadApiShouldAllowEmployeeRole() {
        webTestClient.get()
                .uri("/api/v1/knowledge/documents")
                .header("X-HR-EMPLOYEE-NAME", "张三")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void adminApiShouldAllowAdminRole() {
        webTestClient.post()
                .uri("/api/v1/employees")
                .header("X-HR-EMPLOYEE-NAME", "管理员")
                .header("X-HR-ROLE", "ADMIN")
                .bodyValue(Map.of(
                        "name", "赵六",
                        "department", "财务部",
                        "email", "zhaoliu@example.com",
                        "annualLeaveTotal", 10,
                        "annualLeaveUsed", 0
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber();
    }
}
