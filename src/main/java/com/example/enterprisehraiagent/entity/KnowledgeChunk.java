package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库向量分块索引。
 *
 * <p>VectorStore 负责保存向量，关系型数据库保存业务侧可追踪的索引关系。
 * 这样后续删除某个知识库文档时，可以找到它对应的所有 vectorId 并从向量库中移除。</p>
 */
@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {

    @TableId(type = IdType.AUTO)
    @Schema(description = "知识库分块索引主键 ID")
    private Long id;

    @Schema(description = "所属知识库文档 ID")
    private Long documentId;

    @Schema(description = "向量库中的向量 ID")
    private String vectorId;

    @Schema(description = "文档内的分块序号")
    private Integer chunkIndex;

    @Schema(description = "分块原文内容，用于内存向量库重启后的索引重建")
    private String content;

    @Schema(description = "分块索引创建时间")
    private LocalDateTime createdAt;
}
