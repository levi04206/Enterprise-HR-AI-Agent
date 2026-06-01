package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.KnowledgeDocument;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record KnowledgeDocumentResponse(
        @Schema(description = "知识库文档主键 ID")
        Long id,
        @Schema(description = "上传的原始文件名")
        String filename,
        @Schema(description = "文件内容类型")
        String contentType,
        @Schema(description = "原始文档读取后的文档数量")
        Integer rawDocumentCount,
        @Schema(description = "文档切分后的分块数量")
        Integer chunkCount,
        @Schema(description = "文档入库状态")
        String status,
        @Schema(description = "文档创建时间")
        LocalDateTime createdAt
) {

    /**
     * 将知识库文档实体转换为前端响应对象。
     */
    public static KnowledgeDocumentResponse from(KnowledgeDocument document) {
        return new KnowledgeDocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getContentType(),
                document.getRawDocumentCount(),
                document.getChunkCount(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }
}
