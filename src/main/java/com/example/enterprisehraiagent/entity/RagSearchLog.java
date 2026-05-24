package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rag_search_log")
public class RagSearchLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String userMessage;

    private Integer rankNo;

    private Long documentId;

    private String filename;

    private Integer chunkIndex;

    private String vectorId;

    private Double similarityScore;

    private String contentPreview;

    private LocalDateTime createdAt;
}
