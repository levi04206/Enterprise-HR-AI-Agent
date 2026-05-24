package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.RagSearchLogResponse;
import com.example.enterprisehraiagent.entity.RagSearchLog;
import com.example.enterprisehraiagent.mapper.RagSearchLogMapper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class RagSearchLogService {

    private final RagSearchLogMapper ragSearchLogMapper;

    public RagSearchLogService(RagSearchLogMapper ragSearchLogMapper) {
        this.ragSearchLogMapper = ragSearchLogMapper;
    }

    public void saveSearchLogs(Long sessionId, String userMessage, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        IntStream.range(0, documents.size()).forEach(index -> {
            Document document = documents.get(index);
            Map<String, Object> metadata = document.getMetadata();

            RagSearchLog log = new RagSearchLog();
            log.setSessionId(sessionId);
            log.setUserMessage(limit(userMessage, 1000));
            log.setRankNo(index + 1);
            log.setDocumentId(asLong(metadata.get("knowledgeDocumentId")));
            log.setFilename(asString(metadata.get("filename")));
            log.setChunkIndex(asInteger(metadata.get("chunkIndex")));
            log.setVectorId(document.getId());
            log.setSimilarityScore(readSimilarityScore(metadata));
            log.setContentPreview(limit(document.getText(), 600));
            log.setCreatedAt(now);
            ragSearchLogMapper.insert(log);
        });
    }

    public List<RagSearchLogResponse> listRecent(Long sessionId, Integer limit) {
        int pageSize = limit == null || limit <= 0 ? 50 : Math.min(limit, 200);
        LambdaQueryWrapper<RagSearchLog> wrapper = new LambdaQueryWrapper<RagSearchLog>()
                .orderByDesc(RagSearchLog::getCreatedAt)
                .orderByDesc(RagSearchLog::getId)
                .last("LIMIT " + pageSize);
        if (sessionId != null) {
            wrapper.eq(RagSearchLog::getSessionId, sessionId);
        }
        return ragSearchLogMapper.selectList(wrapper)
                .stream()
                .map(RagSearchLogResponse::from)
                .toList();
    }

    private Double readSimilarityScore(Map<String, Object> metadata) {
        for (String key : List.of("score", "similarity", "distance")) {
            Object value = metadata.get(key);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            if (value instanceof String text && StringUtils.hasText(text)) {
                try {
                    return Double.parseDouble(text);
                } catch (NumberFormatException ignored) {
                    // Ignore non-numeric metadata values.
                }
            }
        }
        return null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.parseLong(text);
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Integer.parseInt(text);
        }
        return null;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private String limit(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
