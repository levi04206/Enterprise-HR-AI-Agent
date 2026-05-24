package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.ToolCallLogResponse;
import com.example.enterprisehraiagent.entity.ToolCallLog;
import com.example.enterprisehraiagent.mapper.ToolCallLogMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@Service
public class ToolCallLogService {

    private final ToolCallLogMapper toolCallLogMapper;
    private final ObjectMapper objectMapper;

    public ToolCallLogService(ToolCallLogMapper toolCallLogMapper, ObjectMapper objectMapper) {
        this.toolCallLogMapper = toolCallLogMapper;
        this.objectMapper = objectMapper;
    }

    public String executeAndLog(String toolName, Object arguments, Supplier<String> supplier) {
        long startedAt = System.nanoTime();
        ToolCallLog log = new ToolCallLog();
        log.setToolName(toolName);
        log.setArgumentsJson(toJson(arguments));
        log.setCreatedAt(LocalDateTime.now());

        try {
            String result = supplier.get();
            log.setResultText(limit(result, 2000));
            log.setSuccess(true);
            return result;
        } catch (RuntimeException error) {
            log.setSuccess(false);
            log.setErrorMessage(limit(error.getMessage(), 1000));
            throw error;
        } finally {
            log.setDurationMs((System.nanoTime() - startedAt) / 1_000_000);
            toolCallLogMapper.insert(log);
        }
    }

    public List<ToolCallLogResponse> listRecent(String toolName, Integer limit) {
        int pageSize = limit == null || limit <= 0 ? 50 : Math.min(limit, 200);
        LambdaQueryWrapper<ToolCallLog> wrapper = new LambdaQueryWrapper<ToolCallLog>()
                .orderByDesc(ToolCallLog::getCreatedAt)
                .orderByDesc(ToolCallLog::getId)
                .last("LIMIT " + pageSize);
        if (toolName != null && !toolName.isBlank()) {
            wrapper.eq(ToolCallLog::getToolName, toolName.trim());
        }
        return toolCallLogMapper.selectList(wrapper)
                .stream()
                .map(ToolCallLogResponse::from)
                .toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException error) {
            return "{\"serializationError\":\"%s\"}".formatted(error.getMessage());
        }
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
