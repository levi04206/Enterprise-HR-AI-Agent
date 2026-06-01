package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.RagSearchLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record RagSearchLogResponse(
        @Schema(description = "RAG 检索日志主键 ID")
        Long id,
        @Schema(description = "关联聊天会话 ID")
        Long sessionId,
        @Schema(description = "触发检索的用户问题")
        String userMessage,
        @Schema(description = "检索结果排名")
        Integer rankNo,
        @Schema(description = "命中的知识库文档 ID")
        Long documentId,
        @Schema(description = "命中的知识库文档文件名")
        String filename,
        @Schema(description = "命中的文档分块序号")
        Integer chunkIndex,
        @Schema(description = "命中的向量 ID")
        String vectorId,
        @Schema(description = "向量检索相似度分数")
        Double similarityScore,
        @Schema(description = "命中文本内容预览")
        String contentPreview,
        @Schema(description = "日志创建时间")
        LocalDateTime createdAt
) {
    /**
     * 将 RAG 检索日志实体转换为前端响应对象。
     */
    public static RagSearchLogResponse from(RagSearchLog log) {
        return new RagSearchLogResponse(
                log.getId(),
                log.getSessionId(),
                log.getUserMessage(),
                log.getRankNo(),
                log.getDocumentId(),
                log.getFilename(),
                log.getChunkIndex(),
                log.getVectorId(),
                log.getSimilarityScore(),
                log.getContentPreview(),
                log.getCreatedAt()
        );
    }
}
