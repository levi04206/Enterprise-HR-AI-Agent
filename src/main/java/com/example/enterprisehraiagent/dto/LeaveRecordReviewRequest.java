package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record LeaveRecordReviewRequest(
        @NotNull(message = "审批状态不能为空")
        @Schema(description = "管理员审批状态，只允许 APPROVED 或 REJECTED")
        LeaveStatus status
) {
}
