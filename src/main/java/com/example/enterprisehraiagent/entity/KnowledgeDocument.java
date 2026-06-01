package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业知识库文档元数据。
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    @Schema(description = "知识库文档主键 ID")
    private Long id;

    @Schema(description = "上传的原始文件名")
    private String filename;

    @Schema(description = "文件内容类型")
    private String contentType;

    @Schema(description = "原始文档读取后的文档数量")
    private Integer rawDocumentCount;

    @Schema(description = "文档切分后的分块数量")
    private Integer chunkCount;

    @Schema(description = "文档入库状态，例如 INDEXING、INDEXED、FAILED、DELETED")
    private String status;

    @Schema(description = "文档创建时间")
    private LocalDateTime createdAt;
}
