package com.example.enterprisehraiagent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticFrontendControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void indexShouldServeDemoConsole() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body)
                        .contains("Enterprise HR AI Agent")
                        .contains("/app.js?v=20260527-leave-review")
                        .contains("/styles.css")
                        .contains("employeeForm")
                        .contains("saveEmployeeButton")
                        .contains("searchEmployeesButton")
                        .contains("lookupEmployeeButton")
                        .contains("leaveRecordForm")
                        .contains("refreshPendingLeaveRecordsButton")
                        .contains("lookupDocumentButton")
                        .contains("filterToolLogsButton"));
    }
}
