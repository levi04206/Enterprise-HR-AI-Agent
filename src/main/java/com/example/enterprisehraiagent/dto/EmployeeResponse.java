package com.example.enterprisehraiagent.dto;

import com.example.enterprisehraiagent.entity.Employee;
import io.swagger.v3.oas.annotations.media.Schema;

public record EmployeeResponse(
        @Schema(description = "员工主键 ID")
        Long id,
        @Schema(description = "员工姓名")
        String name,
        @Schema(description = "所属部门")
        String department,
        @Schema(description = "员工邮箱")
        String email,
        @Schema(description = "员工总年假天数")
        Integer annualLeaveTotal,
        @Schema(description = "员工已使用年假天数")
        Integer annualLeaveUsed,
        @Schema(description = "员工剩余年假天数")
        Integer annualLeaveBalance
) {

    /**
     * 将员工实体转换为前端响应对象，并计算剩余年假。
     */
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
