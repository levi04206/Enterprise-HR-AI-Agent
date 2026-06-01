package com.example.enterprisehraiagent.dto;

import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmployeeUpdateRequest(
        @NotBlank(message = "员工姓名不能为空")
        @Schema(description = "员工姓名")
        String name,

        @NotBlank(message = "部门不能为空")
        @Schema(description = "所属部门")
        String department,

        @Email(message = "邮箱格式不正确")
        @NotBlank(message = "邮箱不能为空")
        @Schema(description = "员工邮箱")
        String email,

        @NotNull(message = "总年假天数不能为空")
        @Min(value = 0, message = "总年假天数不能小于 0")
        @Schema(description = "员工总年假天数")
        Integer annualLeaveTotal,

        @NotNull(message = "已用年假天数不能为空")
        @Min(value = 0, message = "已用年假天数不能小于 0")
        @Schema(description = "员工已使用年假天数")
        Integer annualLeaveUsed
) {
}
