package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ChatService chatService;

    @Test
    void streamShouldReturnMessageEventsAndDoneEvent() {
        when(chatService.streamChat(eq("张三还剩多少年假？"), eq(1L)))
                .thenReturn(Flux.just("张三", "还剩 10 天年假"));

        webTestClient.post()
                .uri("/api/v1/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(Map.of("message", "张三还剩多少年假？", "sessionId", 1))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("event:message");
                    assert body.contains("data:张三");
                    assert body.contains("data:还剩 10 天年假");
                    assert body.contains("event:done");
                    assert body.contains("data:[DONE]");
                });
    }

    @Test
    void streamShouldReturnErrorEventWhenChatServiceFails() {
        when(chatService.streamChat(eq("你好"), eq(null)))
                .thenReturn(Flux.error(new RuntimeException("模型暂不可用")));

        webTestClient.post()
                .uri("/api/v1/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(Map.of("message", "你好"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("event:error");
                    assert body.contains("data:对话失败：模型暂不可用");
                });
    }

    @Test
    void streamShouldRejectBlankMessage() {
        webTestClient.post()
                .uri("/api/v1/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(Map.of("message", " "))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("event:error");
                    assert body.contains("data:对话失败：请输入要咨询的问题");
                });
    }
}
