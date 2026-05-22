package com.example.enterprisehraiagent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeCreateRequest(
        @NotBlank(message = "员工姓名不能为空")
        String name,

        @NotBlank(message = "部门不能为空")
        String department,

        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        String email,

        @NotNull(message = "总年假天数不能为空")
        @Min(value = 0, message = "总年假天数不能小于 0")
        Integer annualLeaveTotal,

        @NotNull(message = "已用年假天数不能为空")
        @Min(value = 0, message = "已用年假天数不能小于 0")
        Integer annualLeaveUsed
) {
}
