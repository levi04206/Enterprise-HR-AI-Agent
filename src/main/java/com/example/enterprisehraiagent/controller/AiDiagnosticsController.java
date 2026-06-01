package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.AiDiagnosticResponse;
import com.example.enterprisehraiagent.service.AiDiagnosticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class AiDiagnosticsController {

    private final AiDiagnosticsService aiDiagnosticsService;

    public AiDiagnosticsController(AiDiagnosticsService aiDiagnosticsService) {
        this.aiDiagnosticsService = aiDiagnosticsService;
    }

    /**
     * 检测聊天模型是否可用（已修复 WebFlux 线程阻塞问题）。
     */
    @GetMapping("/chat")
    public Mono<AiDiagnosticResponse> checkChat() {
        // 1. 打包任务
        return Mono.fromCallable(() -> aiDiagnosticsService.checkChat())
                // 2. 扔给弹性后台线程去执行，解放 Netty 主线程！
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 检测向量模型是否可用（同步修复）。
     */
    @GetMapping("/embedding")
    public Mono<AiDiagnosticResponse> checkEmbedding() {
        return Mono.fromCallable(() -> aiDiagnosticsService.checkEmbedding())
                .subscribeOn(Schedulers.boundedElastic());
    }
}