package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.entity.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record LeaveRecordResponse(
        @Schema(description = "请假记录主键 ID")
        Long id,
        @Schema(description = "请假员工 ID")
        Long empId,
        @Schema(description = "请假开始日期")
        LocalDate startDate,
        @Schema(description = "请假结束日期")
        LocalDate endDate,
        @Schema(description = "请假审批状态")
        LeaveStatus status
) {

    /**
     * 将请假记录实体转换为前端响应对象。
     */
    public static LeaveRecordResponse from(LeaveRecord record) {
        return new LeaveRecordResponse(
                record.getId(),
                record.getEmpId(),
                record.getStartDate(),
                record.getEndDate(),
                record.getStatus()
        );
    }
}
