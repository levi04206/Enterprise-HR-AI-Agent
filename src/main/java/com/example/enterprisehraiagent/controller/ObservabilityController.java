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

    /**
     * 注入 RAG 检索日志和工具调用日志服务。
     */
    public ObservabilityController(RagSearchLogService ragSearchLogService,
                                   ToolCallLogService toolCallLogService) {
        this.ragSearchLogService = ragSearchLogService;
        this.toolCallLogService = toolCallLogService;
    }

    /**
     * 查询最近的 RAG 检索日志，可按会话过滤。
     */
    @GetMapping("/rag-search-logs")
    public List<RagSearchLogResponse> ragSearchLogs(@RequestParam(required = false) Long sessionId,
                                                    @RequestParam(required = false) Integer limit) {
        return ragSearchLogService.listRecent(sessionId, limit);
    }

    /**
     * 查询最近的工具调用日志，可按工具名关键词模糊过滤。
     */
    @GetMapping("/tool-call-logs")
    public List<ToolCallLogResponse> toolCallLogs(@RequestParam(required = false) String toolName,
                                                  @RequestParam(required = false) Integer limit) {
        return toolCallLogService.listRecent(toolName, limit);
    }
}
