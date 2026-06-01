package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.LeaveRecordCreateRequest;
import com.example.enterprisehraiagent.dto.LeaveRecordResponse;
import com.example.enterprisehraiagent.dto.LeaveRecordReviewRequest;
import com.example.enterprisehraiagent.entity.Employee;
import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.entity.LeaveStatus;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import com.example.enterprisehraiagent.mapper.LeaveRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class LeaveRecordService {

    private final EmployeeMapper employeeMapper;
    private final LeaveRecordMapper leaveRecordMapper;

    /**
     * 注入员工和请假记录数据访问对象。
     */
    public LeaveRecordService(EmployeeMapper employeeMapper, LeaveRecordMapper leaveRecordMapper) {
        this.employeeMapper = employeeMapper;
        this.leaveRecordMapper = leaveRecordMapper;
    }

    /**
     * 创建待审批请假记录，创建时不直接扣减年假余额。
     */
    @Transactional
    public Long create(LeaveRecordCreateRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        Employee employee = employeeMapper.selectById(request.empId());
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在：" + request.empId());
        }

        LeaveRecord record = new LeaveRecord();
        record.setEmpId(request.empId());
        record.setStartDate(request.startDate());
        record.setEndDate(request.endDate());
        record.setStatus(LeaveStatus.PENDING);
        leaveRecordMapper.insert(record);

        return record.getId();
    }

    /**
     * 查询请假记录列表，可按状态过滤。
     */
    public List<LeaveRecordResponse> list(LeaveStatus status) {
        LambdaQueryWrapper<LeaveRecord> wrapper = new LambdaQueryWrapper<LeaveRecord>()
                .orderByDesc(LeaveRecord::getStartDate)
                .orderByDesc(LeaveRecord::getId);
        if (status != null) {
            wrapper.eq(LeaveRecord::getStatus, status);
        }
        return leaveRecordMapper.selectList(wrapper)
                .stream()
                .map(LeaveRecordResponse::from)
                .toList();
    }

    /**
     * 查询指定员工的请假记录。
     */
    public List<LeaveRecordResponse> listByEmployee(Long empId) {
        return leaveRecordMapper.selectList(
                        new LambdaQueryWrapper<LeaveRecord>()
                                .eq(LeaveRecord::getEmpId, empId)
                                .orderByDesc(LeaveRecord::getStartDate)
                )
                .stream()
                .map(LeaveRecordResponse::from)
                .toList();
    }

    /**
     * 审批请假记录，批准时扣年假，驳回时不扣年假。
     */
    @Transactional
    public LeaveRecordResponse review(Long id, LeaveRecordReviewRequest request) {
        if (request.status() == LeaveStatus.PENDING) {
            throw new IllegalArgumentException("审批状态只能是 APPROVED 或 REJECTED");
        }

        LeaveRecord record = leaveRecordMapper.selectById(id);
        if (record == null) {
            throw new NoSuchElementException("请假记录不存在：" + id);
        }

        LeaveStatus oldStatus = record.getStatus();
        LeaveStatus newStatus = request.status();
        if (oldStatus == newStatus) {
            return LeaveRecordResponse.from(record);
        }

        Employee employee = employeeMapper.selectById(record.getEmpId());
        if (employee == null) {
            throw new NoSuchElementException("员工不存在：" + record.getEmpId());
        }

        if (oldStatus != LeaveStatus.APPROVED && newStatus == LeaveStatus.APPROVED) {
            applyAnnualLeaveDelta(employee, calculateLeaveDays(record));
        } else if (oldStatus == LeaveStatus.APPROVED && newStatus != LeaveStatus.APPROVED) {
            applyAnnualLeaveDelta(employee, -calculateLeaveDays(record));
        }

        record.setStatus(newStatus);
        leaveRecordMapper.updateById(record);
        return LeaveRecordResponse.from(record);
    }

    /**
     * 根据创建请求计算请假天数，开始和结束日期都计入。
     */
    private int calculateLeaveDays(LeaveRecordCreateRequest request) {
        return Math.toIntExact(ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1);
    }

    /**
     * 根据请假记录计算请假天数，开始和结束日期都计入。
     */
    private int calculateLeaveDays(LeaveRecord record) {
        return Math.toIntExact(ChronoUnit.DAYS.between(record.getStartDate(), record.getEndDate()) + 1);
    }

    /**
     * 按增量更新员工已用年假，并校验余额不能透支。
     */
    private void applyAnnualLeaveDelta(Employee employee, int delta) {
        int used = safeNumber(employee.getAnnualLeaveUsed());
        int total = safeNumber(employee.getAnnualLeaveTotal());
        int nextUsed = used + delta;
        if (nextUsed < 0) {
            nextUsed = 0;
        }
        if (nextUsed > total) {
            throw new IllegalArgumentException("剩余年假不足：当前剩余 " + (total - used) + " 天，本次请假 " + delta + " 天");
        }
        employee.setAnnualLeaveUsed(nextUsed);
        employeeMapper.updateById(employee);
    }

    /**
     * 将可能为空的数字字段转换为 0。
     */
    private int safeNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
