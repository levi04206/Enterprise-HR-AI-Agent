package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    @Schema(description = "聊天消息主键 ID")
    private Long id;

    @Schema(description = "所属聊天会话 ID")
    private Long sessionId;

    @Schema(description = "消息角色，例如 USER 或 ASSISTANT")
    private String role;

    @Schema(description = "消息正文内容")
    private String content;

    @Schema(description = "消息创建时间")
    private LocalDateTime createdAt;
}
