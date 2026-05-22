package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.Employee;

public record EmployeeResponse(
        Long id,
        String name,
        String department,
        String email,
        Integer annualLeaveTotal,
        Integer annualLeaveUsed,
        Integer annualLeaveBalance
) {

    public static EmployeeResponse from(Employee employee) {
        int total = employee.getAnnualLeaveTotal() == null ? 0 : employee.getAnnualLeaveTotal();
        int used = employee.getAnnualLeaveUsed() == null ? 0 : employee.getAnnualLeaveUsed();
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getDepartment(),
                employee.getEmail(),
                total,
                used,
                total - used
        );
    }
}
