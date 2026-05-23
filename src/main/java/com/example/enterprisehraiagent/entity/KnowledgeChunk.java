package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Long id;

    private Long documentId;

    private String vectorId;

    private Integer chunkIndex;

    private LocalDateTime createdAt;
}
