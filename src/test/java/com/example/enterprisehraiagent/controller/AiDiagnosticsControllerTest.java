package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.AiDiagnosticResponse;
import com.example.enterprisehraiagent.service.AiDiagnosticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiDiagnosticsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AiDiagnosticsService aiDiagnosticsService;

    @Test
    void checkChatShouldReturnModelStatus() {
        when(aiDiagnosticsService.checkChat())
                .thenReturn(AiDiagnosticResponse.ok("DeepSeek Chat: test-chat-model", "Chat 调用成功"));

        webTestClient.get()
                .uri("/api/v1/diagnostics/chat")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.provider").isEqualTo("DeepSeek Chat: test-chat-model")
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.message").isEqualTo("Chat 调用成功");
    }

    @Test
    void checkEmbeddingShouldReturnVectorStatus() {
        when(aiDiagnosticsService.checkEmbedding())
                .thenReturn(AiDiagnosticResponse.embeddingOk("DashScope Embedding: test-embedding-model", 1536));

        webTestClient.get()
                .uri("/api/v1/diagnostics/embedding")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.provider").isEqualTo("DashScope Embedding: test-embedding-model")
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.message").isEqualTo("Embedding 调用成功")
                .jsonPath("$.embeddingDimensions").isEqualTo(1536);
    }
}
