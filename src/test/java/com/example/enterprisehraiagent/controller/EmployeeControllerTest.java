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

    @Test
    void createShouldReturnStructuredErrorWhenBodyInvalid() {
        Map<String, Object> request = Map.of(
                "name", "",
                "department", "",
                "email", "not-an-email",
                "annualLeaveTotal", -1,
                "annualLeaveUsed", -1
        );

        webTestClient.post()
                .uri("/api/v1/employees")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request")
                .jsonPath("$.code").isEqualTo("VALIDATION_FAILED")
                .jsonPath("$.message").isEqualTo("请求参数校验失败")
                .jsonPath("$.path").isEqualTo("/api/v1/employees")
                .jsonPath("$.details.name").exists()
                .jsonPath("$.details.email").exists()
                .jsonPath("$.details.annualLeaveTotal").exists()
                .jsonPath("$.details.annualLeaveUsed").exists();
    }

    @Test
    void createShouldReturnConflictWhenEmployeeNameDuplicated() {
        Map<String, Object> request = Map.of(
                "name", "张三",
                "department", "研发部",
                "email", "zhangsan2@example.com",
                "annualLeaveTotal", 10,
                "annualLeaveUsed", 0
        );

        webTestClient.post()
                .uri("/api/v1/employees")
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.code").isEqualTo("DUPLICATE_RESOURCE")
                .jsonPath("$.message").isEqualTo("数据已存在，请检查唯一字段是否重复")
                .jsonPath("$.details.constraint").exists();
    }

    @Test
    void updateShouldModifyEmployee() {
        Map<String, Object> request = Map.of(
                "name", "张三",
                "department", "平台研发部",
                "email", "zhangsan@example.com",
                "annualLeaveTotal", 16,
                "annualLeaveUsed", 4
        );

        webTestClient.put()
                .uri("/api/v1/employees/{id}", 1)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("员工信息已更新");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.department").isEqualTo("平台研发部")
                .jsonPath("$.annualLeaveBalance").isEqualTo(12);
    }

    @Test
    void getShouldReturnNotFoundWhenEmployeeMissing() {
        webTestClient.get()
                .uri("/api/v1/employees/{id}", 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.code").isEqualTo("RESOURCE_NOT_FOUND")
                .jsonPath("$.message").isEqualTo("员工不存在：999");
    }

    @Test
    void deleteShouldRemoveEmployeeWithoutLeaveRecords() {
        Map<String, Object> request = Map.of(
                "name", "钱七",
                "department", "行政部",
                "email", "qianqi@example.com",
                "annualLeaveTotal", 8,
                "annualLeaveUsed", 0
        );

        Map<?, ?> response = webTestClient.post()
                .uri("/api/v1/employees")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        Number id = (Number) response.get("id");

        webTestClient.delete()
                .uri("/api/v1/employees/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("员工已删除");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }
}
