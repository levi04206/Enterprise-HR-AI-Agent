package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tool_call_log")
public class ToolCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String toolName;

    private String argumentsJson;

    private String resultText;

    private Boolean success;

    private String errorMessage;

    private Long durationMs;

    private LocalDateTime createdAt;
}
