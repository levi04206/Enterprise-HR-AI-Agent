package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.ChatRequest;
import com.example.enterprisehraiagent.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * HR Agent 流式对话接口。
     *
     * <p>请求体：{"message":"张三还剩多少年假？"}
     * 返回值：text/event-stream，每个 SSE data 片段是一段模型增量输出。</p>
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@Valid @RequestBody ChatRequest request) {
        return chatService.streamChat(request.message(), request.sessionId())
                .map(token -> ServerSentEvent.builder(token).event("message").build())
                .concatWithValues(ServerSentEvent.builder("[DONE]").event("done").build())
                .onErrorResume(error -> Flux.just(
                        ServerSentEvent.builder("对话失败：" + error.getMessage()).event("error").build()
                ));
    }
}
