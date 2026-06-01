package com.example.enterprisehraiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        @Schema(description = "错误发生时间")
        LocalDateTime timestamp,
        @Schema(description = "HTTP 状态码")
        int status,
        @Schema(description = "HTTP 错误名称")
        String error,
        @Schema(description = "业务错误码")
        String code,
        @Schema(description = "前端可展示的错误消息")
        String message,
        @Schema(description = "发生错误的请求路径")
        String path,
        @Schema(description = "字段级错误或补充错误详情")
        Map<String, String> details
) {
    /**
     * 创建统一 API 错误响应对象。
     */
    public static ApiErrorResponse of(int status,
                                      String error,
                                      String code,
                                      String message,
                                      String path,
                                      Map<String, String> details) {
        return new ApiErrorResponse(LocalDateTime.now(), status, error, code, message, path, details);
    }
}
