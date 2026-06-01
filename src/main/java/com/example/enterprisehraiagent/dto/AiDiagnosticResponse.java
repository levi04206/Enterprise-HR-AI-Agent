package com.example.enterprisehraiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AiDiagnosticResponse(
        @Schema(description = "被检测的 AI 服务或模型名称")
        String provider,
        @Schema(description = "检测状态，UP 表示可用，DOWN 表示不可用")
        String status,
        @Schema(description = "诊断结果说明")
        String message,
        @Schema(description = "Embedding 向量维度，仅向量诊断成功时返回")
        Integer embeddingDimensions
) {

    /**
     * 创建 AI 服务健康的诊断响应。
     */
    public static AiDiagnosticResponse ok(String provider, String message) {
        return new AiDiagnosticResponse(provider, "UP", message, null);
    }

    /**
     * 创建 Embedding 服务健康的诊断响应。
     */
    public static AiDiagnosticResponse embeddingOk(String provider, int dimensions) {
        return new AiDiagnosticResponse(provider, "UP", "Embedding 调用成功", dimensions);
    }

    /**
     * 创建 AI 服务不可用的诊断响应。
     */
    public static AiDiagnosticResponse down(String provider, String message) {
        return new AiDiagnosticResponse(provider, "DOWN", message, null);
    }
}
