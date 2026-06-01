package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rag_search_log")
public class RagSearchLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "RAG 检索日志主键 ID")
    private Long id;

    @Schema(description = "关联聊天会话 ID")
    private Long sessionId;

    @Schema(description = "触发检索的用户问题")
    private String userMessage;

    @Schema(description = "检索结果排名")
    private Integer rankNo;

    @Schema(description = "命中的知识库文档 ID")
    private Long documentId;

    @Schema(description = "命中的知识库文档文件名")
    private String filename;

    @Schema(description = "命中的文档分块序号")
    private Integer chunkIndex;

    @Schema(description = "命中的向量 ID")
    private String vectorId;

    @Schema(description = "向量检索相似度分数")
    private Double similarityScore;

    @Schema(description = "命中文本内容预览")
    private String contentPreview;

    @Schema(description = "日志创建时间")
    private LocalDateTime createdAt;
}
