package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_session")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    @Schema(description = "聊天会话主键 ID")
    private Long id;

    @Schema(description = "聊天会话标题")
    private String title;

    @Schema(description = "会话创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "会话最近更新时间")
    private LocalDateTime updatedAt;
}
