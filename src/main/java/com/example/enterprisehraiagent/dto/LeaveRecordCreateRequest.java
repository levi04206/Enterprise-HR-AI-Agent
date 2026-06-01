package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRecordCreateRequest(
        @NotNull(message = "员工 ID 不能为空")
        @Schema(description = "请假员工 ID")
        Long empId,

        @NotNull(message = "开始日期不能为空")
        @Schema(description = "请假开始日期")
        LocalDate startDate,

        @NotNull(message = "结束日期不能为空")
        @Schema(description = "请假结束日期")
        LocalDate endDate,

        @NotNull(message = "请假状态不能为空")
        @Schema(description = "请假状态，创建时后端会统一保存为 PENDING")
        LeaveStatus status
) {
}
