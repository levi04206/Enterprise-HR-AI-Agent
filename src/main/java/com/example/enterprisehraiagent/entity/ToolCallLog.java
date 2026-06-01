package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tool_call_log")
public class ToolCallLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "工具调用日志主键 ID")
    private Long id;

    @Schema(description = "被调用的工具名称")
    private String toolName;

    @Schema(description = "工具调用入参 JSON")
    private String argumentsJson;

    @Schema(description = "工具调用结果文本")
    private String resultText;

    @Schema(description = "工具调用是否成功")
    private Boolean success;

    @Schema(description = "工具调用失败时的错误消息")
    private String errorMessage;

    @Schema(description = "工具调用耗时，单位毫秒")
    private Long durationMs;

    @Schema(description = "日志创建时间")
    private LocalDateTime createdAt;
}
