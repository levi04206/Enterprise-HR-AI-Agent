package com.example.enterprisehraiagent.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.entity.Employee;
import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.entity.LeaveStatus;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import com.example.enterprisehraiagent.mapper.LeaveRecordMapper;
import com.example.enterprisehraiagent.service.ToolCallLogService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 智能体工具箱。
 *
 * <p>每个 @Bean 都会注册为 Spring AI 可调用的 Function。模型需要查询个人数据时，
 * 会根据 @Description 决定是否调用对应工具。工具内部只返回确定性业务数据，同时写入
 * tool_call_log，方便后续审计模型到底调用了什么内部能力。</p>
 */
@Configuration
public class HrToolsConfig {

    /**
     * 注册员工联系方式查询工具，供模型按员工姓名查询部门和邮箱。
     */
    @Bean
    @Description("根据员工姓名查询员工联系方式，返回员工所属部门和邮箱。适用于用户询问某位同事邮箱、联系方式、所属部门等问题。")
    public Function<EmployeeNameRequest, String> getEmployeeContactTool(EmployeeMapper employeeMapper,
                                                                        ToolCallLogService toolCallLogService) {
        return request -> toolCallLogService.executeAndLog("getEmployeeContactTool", request,
                () -> findEmployee(employeeMapper, request.employeeName())
                        .map(employee -> "员工%s属于%s，邮箱是%s。".formatted(
                                employee.getName(), employee.getDepartment(), employee.getEmail()))
                        .orElse("未查询到名为 %s 的员工，请确认姓名是否正确。".formatted(request.employeeName())));
    }

    /**
     * 注册年假余额查询工具，供模型查询员工剩余年假和待审批请假记录。
     */
    @Bean
    @Description("根据员工姓名查询剩余年假天数，并返回最近一条审批中的请假记录。适用于用户询问年假余额、剩余假期、请假审批进度等问题。")
    public Function<EmployeeNameRequest, String> getLeaveBalanceTool(EmployeeMapper employeeMapper,
                                                                    LeaveRecordMapper leaveRecordMapper,
                                                                    ToolCallLogService toolCallLogService) {
        return request -> toolCallLogService.executeAndLog("getLeaveBalanceTool", request,
                () -> findEmployee(employeeMapper, request.employeeName())
                        .map(employee -> {
                            int total = Optional.ofNullable(employee.getAnnualLeaveTotal()).orElse(0);
                            int used = Optional.ofNullable(employee.getAnnualLeaveUsed()).orElse(0);
                            int balance = total - used;

                            LeaveRecord pendingRecord = leaveRecordMapper.selectOne(
                                    new LambdaQueryWrapper<LeaveRecord>()
                                            .eq(LeaveRecord::getEmpId, employee.getId())
                                            .eq(LeaveRecord::getStatus, LeaveStatus.PENDING)
                                            .orderByDesc(LeaveRecord::getStartDate)
                                            .last("LIMIT 1")
                            );

                            String pendingText = pendingRecord == null
                                    ? "当前没有审批中的请假记录。"
                                    : "最近一条审批中请假记录为：%s 至 %s。".formatted(
                                    pendingRecord.getStartDate(), pendingRecord.getEndDate());

                            return "%s 的总年假为 %d 天，已用 %d 天，剩余 %d 天。%s".formatted(
                                    employee.getName(), total, used, balance, pendingText);
                        })
                        .orElse("未查询到名为 %s 的员工，请确认姓名是否正确。".formatted(request.employeeName())));
    }

    /**
     * 注册公司员工概览工具，供模型统计员工总数并按需返回员工明细。
     */
    @Bean
    @Description("查询当前公司员工总数；当用户要求查看员工清单、详细信息、全部员工资料时，将 includeDetails 设置为 true，并返回员工 ID、姓名、部门、邮箱和年假信息。")
    public Function<EmployeeDirectoryRequest, String> getCompanyEmployeeSummaryTool(EmployeeMapper employeeMapper,
                                                                                   ToolCallLogService toolCallLogService) {
        return request -> toolCallLogService.executeAndLog("getCompanyEmployeeSummaryTool", request, () -> {
            Long total = employeeMapper.selectCount(null);
            if (total == null || total == 0) {
                return "当前公司暂无员工数据。";
            }

            boolean includeDetails = request != null && Boolean.TRUE.equals(request.includeDetails());
            if (!includeDetails) {
                return "当前公司共有 %d 名员工。".formatted(total);
            }

            List<Employee> employees = employeeMapper.selectList(
                    new LambdaQueryWrapper<Employee>()
                            .orderByAsc(Employee::getId)
                            .last("LIMIT 50")
            );
            String details = employees.stream()
                    .map(employee -> {
                        int totalLeave = Optional.ofNullable(employee.getAnnualLeaveTotal()).orElse(0);
                        int usedLeave = Optional.ofNullable(employee.getAnnualLeaveUsed()).orElse(0);
                        int balance = totalLeave - usedLeave;
                        return "#%d %s，部门：%s，邮箱：%s，年假：总计 %d 天，已用 %d 天，剩余 %d 天。".formatted(
                                employee.getId(),
                                employee.getName(),
                                employee.getDepartment(),
                                employee.getEmail(),
                                totalLeave,
                                usedLeave,
                                balance
                        );
                    })
                    .collect(java.util.stream.Collectors.joining("\n"));
            String suffix = total > employees.size()
                    ? "\n当前仅展示前 %d 名员工，请缩小查询范围或前往员工管理页面查看更多。".formatted(employees.size())
                    : "";
            return "当前公司共有 %d 名员工，员工明细如下：\n%s%s".formatted(total, details, suffix);
        });
    }

    /**
     * 根据员工姓名精确查询员工实体。
     */
    private static Optional<Employee> findEmployee(EmployeeMapper employeeMapper, String employeeName) {
        if (employeeName == null || employeeName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(employeeMapper.selectOne(
                new LambdaQueryWrapper<Employee>().eq(Employee::getName, employeeName.trim()).last("LIMIT 1")
        ));
    }

    public record EmployeeNameRequest(
            @JsonProperty(required = true)
            String employeeName
    ) {
    }

    public record EmployeeDirectoryRequest(
            @JsonProperty(defaultValue = "false")
            Boolean includeDetails
    ) {
    }
}
