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
class LeaveRecordControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void listByEmployeeShouldReturnSeedLeaveRecord() {
        webTestClient.get()
                .uri("/api/v1/leave-records/employee/{empId}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].empId").isEqualTo(1)
                .jsonPath("$[0].status").isEqualTo("PENDING");
    }

    @Test
    void createShouldRejectInvalidDateRange() {
        Map<String, Object> request = Map.of(
                "empId", 1,
                "startDate", "2026-07-03",
                "endDate", "2026-07-01",
                "status", "PENDING"
        );

        webTestClient.post()
                .uri("/api/v1/leave-records")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("结束日期不能早于开始日期");
    }

    @Test
    void createShouldPersistPendingLeaveRecordWithoutDeductingBalance() {
        Map<String, Object> request = Map.of(
                "empId", 1,
                "startDate", "2026-07-01",
                "endDate", "2026-07-02",
                "status", "PENDING"
        );

        webTestClient.post()
                .uri("/api/v1/leave-records")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber();

        webTestClient.get()
                .uri("/api/v1/leave-records/employee/{empId}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("PENDING");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.annualLeaveUsed").isEqualTo(5)
                .jsonPath("$.annualLeaveBalance").isEqualTo(10);
    }

    @Test
    void reviewShouldApprovePendingLeaveRecordAndDeductAnnualLeaveBalance() {
        Map<String, Object> request = Map.of(
                "empId", 1,
                "startDate", "2026-07-01",
                "endDate", "2026-07-02",
                "status", "PENDING"
        );

        Map<?, ?> createResponse = webTestClient.post()
                .uri("/api/v1/leave-records")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        Long id = ((Number) createResponse.get("id")).longValue();

        webTestClient.patch()
                .uri("/api/v1/leave-records/{id}/status", id)
                .bodyValue(Map.of("status", "APPROVED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("APPROVED");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.annualLeaveUsed").isEqualTo(7)
                .jsonPath("$.annualLeaveBalance").isEqualTo(8);
    }

    @Test
    void reviewShouldRejectPendingLeaveRecordWithoutDeductingAnnualLeaveBalance() {
        Map<String, Object> request = Map.of(
                "empId", 1,
                "startDate", "2026-07-01",
                "endDate", "2026-07-03",
                "status", "PENDING"
        );

        Map<?, ?> createResponse = webTestClient.post()
                .uri("/api/v1/leave-records")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        Long id = ((Number) createResponse.get("id")).longValue();

        webTestClient.patch()
                .uri("/api/v1/leave-records/{id}/status", id)
                .bodyValue(Map.of("status", "REJECTED"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("REJECTED");

        webTestClient.get()
                .uri("/api/v1/employees/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.annualLeaveUsed").isEqualTo(5)
                .jsonPath("$.annualLeaveBalance").isEqualTo(10);
    }

    @Test
    void listShouldReturnPendingLeaveRecords() {
        webTestClient.get()
                .uri("/api/v1/leave-records?status=PENDING")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("PENDING");
    }
}
