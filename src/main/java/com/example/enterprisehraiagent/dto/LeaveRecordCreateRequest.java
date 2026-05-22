package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveRecordCreateRequest(
        @NotNull(message = "员工 ID 不能为空")
        Long empId,

        @NotNull(message = "开始日期不能为空")
        LocalDate startDate,

        @NotNull(message = "结束日期不能为空")
        LocalDate endDate,

        @NotNull(message = "请假状态不能为空")
        LeaveStatus status
) {
}
