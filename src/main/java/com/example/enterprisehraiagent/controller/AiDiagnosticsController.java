package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.AiDiagnosticResponse;
import com.example.enterprisehraiagent.service.AiDiagnosticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class AiDiagnosticsController {

    private final AiDiagnosticsService aiDiagnosticsService;

    public AiDiagnosticsController(AiDiagnosticsService aiDiagnosticsService) {
        this.aiDiagnosticsService = aiDiagnosticsService;
    }

    @GetMapping("/chat")
    public AiDiagnosticResponse checkChat() {
        return aiDiagnosticsService.checkChat();
    }

    @GetMapping("/embedding")
    public AiDiagnosticResponse checkEmbedding() {
        return aiDiagnosticsService.checkEmbedding();
    }
}
