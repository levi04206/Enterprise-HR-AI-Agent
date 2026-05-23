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
                .expectStatus().isUnauthorized();
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
                .expectStatus().isForbidden();
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
