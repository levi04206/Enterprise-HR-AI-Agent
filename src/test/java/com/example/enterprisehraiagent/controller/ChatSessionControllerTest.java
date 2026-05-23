package com.example.enterprisehraiagent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatSessionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void createAndListSessionsShouldWork() {
        webTestClient.post()
                .uri("/api/v1/chat/sessions")
                .bodyValue(Map.of("title", "年假咨询"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNumber();

        webTestClient.get()
                .uri("/api/v1/chat/sessions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("年假咨询");
    }

    @Test
    void listMessagesShouldReturnEmptyForNewSession() {
        Integer id = webTestClient.post()
                .uri("/api/v1/chat/sessions")
                .bodyValue(Map.of("title", "联系方式咨询"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody()
                .get("id") instanceof Integer value ? value : null;

        webTestClient.get()
                .uri("/api/v1/chat/sessions/{sessionId}/messages", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("[]");
    }

    @Test
    void listMessagesShouldRejectMissingSession() {
        webTestClient.get()
                .uri("/api/v1/chat/sessions/{sessionId}/messages", 999)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("对话会话不存在：999");
    }
}
