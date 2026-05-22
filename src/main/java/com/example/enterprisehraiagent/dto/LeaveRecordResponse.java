package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.entity.LeaveStatus;

import java.time.LocalDate;

public record LeaveRecordResponse(
        Long id,
        Long empId,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status
) {

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
