package com.example.enterprisehraiagent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void listShouldReturnSeedEmployees() {
        webTestClient.get()
                .uri("/api/v1/employees?keyword={keyword}", "研发")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("张三")
                .jsonPath("$[0].annualLeaveBalance").isEqualTo(10);
    }

    @Test
    void createShouldPersistEmployee() {
        Map<String, Object> request = Map.of(
                "name", "王五",
                "department", "财务部",
                "email", "wangwu@example.com",
                "annualLeaveTotal", 10,
                "annualLeaveUsed", 1
        );

        webTestClient.post()
                .uri("/api/v1/employees")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber();

        webTestClient.get()
                .uri("/api/v1/employees?keyword={keyword}", "王五")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].email").isEqualTo("wangwu@example.com")
                .jsonPath("$[0].annualLeaveBalance").isEqualTo(9);
    }
}
