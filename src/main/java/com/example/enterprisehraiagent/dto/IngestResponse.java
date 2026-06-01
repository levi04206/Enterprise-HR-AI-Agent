package com.example.enterprisehraiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IngestResponse(
        @Schema(description = "入库后的知识库文档 ID")
        Long documentId,
        @Schema(description = "上传文件名")
        String filename,
        @Schema(description = "原始文档读取数量")
        int rawDocumentCount,
        @Schema(description = "切分后的知识片段数量")
        int chunkCount,
        @Schema(description = "入库结果提示")
        String message
) {
}
