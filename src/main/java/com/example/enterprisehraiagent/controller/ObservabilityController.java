package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.RagSearchLogResponse;
import com.example.enterprisehraiagent.dto.ToolCallLogResponse;
import com.example.enterprisehraiagent.service.RagSearchLogService;
import com.example.enterprisehraiagent.service.ToolCallLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final RagSearchLogService ragSearchLogService;
    private final ToolCallLogService toolCallLogService;

    public ObservabilityController(RagSearchLogService ragSearchLogService,
                                   ToolCallLogService toolCallLogService) {
        this.ragSearchLogService = ragSearchLogService;
        this.toolCallLogService = toolCallLogService;
    }

    @GetMapping("/rag-search-logs")
    public List<RagSearchLogResponse> ragSearchLogs(@RequestParam(required = false) Long sessionId,
                                                    @RequestParam(required = false) Integer limit) {
        return ragSearchLogService.listRecent(sessionId, limit);
    }

    @GetMapping("/tool-call-logs")
    public List<ToolCallLogResponse> toolCallLogs(@RequestParam(required = false) String toolName,
                                                  @RequestParam(required = false) Integer limit) {
        return toolCallLogService.listRecent(toolName, limit);
    }
}
