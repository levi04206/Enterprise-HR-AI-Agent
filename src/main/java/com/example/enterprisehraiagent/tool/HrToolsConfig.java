package com.example.enterprisehraiagent.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.entity.Employee;
import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.entity.LeaveStatus;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import com.example.enterprisehraiagent.mapper.LeaveRecordMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Optional;
import java.util.function.Function;

/**
 * 智能体工具箱。
 *
 * <p>这里采用 Spring AI 支持的动态工具方式：每个 @Bean 返回一个 Function，
 * @Description 会进入工具描述，让模型知道“什么时候调用这个工具”。工具入参
 * 使用 record，Spring AI 会根据字段生成 JSON Schema，模型调用工具时会生成
 * 类似 {"employeeName":"张三"} 的结构化参数。</p>
 */
@Configuration
public class HrToolsConfig {

    @Bean
    @Description("根据员工姓名查询员工联系方式，返回员工所属部门和邮箱。适用于用户询问某位同事邮箱、联系方式、所属部门等问题。")
    public Function<EmployeeNameRequest, String> getEmployeeContactTool(EmployeeMapper employeeMapper) {
        return request -> findEmployee(employeeMapper, request.employeeName())
                .map(employee -> "员工%s属于%s，邮箱是%s。".formatted(
                        employee.getName(), employee.getDepartment(), employee.getEmail()))
                .orElse("未查询到名为 %s 的员工，请确认姓名是否正确。".formatted(request.employeeName()));
    }

    @Bean
    @Description("根据员工姓名查询剩余年假天数，并返回最近一条审批中的请假记录。适用于用户询问年假余额、剩余假期、请假审批进度等问题。")
    public Function<EmployeeNameRequest, String> getLeaveBalanceTool(EmployeeMapper employeeMapper,
                                                                    LeaveRecordMapper leaveRecordMapper) {
        return request -> findEmployee(employeeMapper, request.employeeName())
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
                .orElse("未查询到名为 %s 的员工，请确认姓名是否正确。".formatted(request.employeeName()));
    }

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
}
