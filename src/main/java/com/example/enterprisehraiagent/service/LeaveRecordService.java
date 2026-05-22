package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.LeaveRecordCreateRequest;
import com.example.enterprisehraiagent.dto.LeaveRecordResponse;
import com.example.enterprisehraiagent.entity.LeaveRecord;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import com.example.enterprisehraiagent.mapper.LeaveRecordMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRecordService {

    private final EmployeeMapper employeeMapper;
    private final LeaveRecordMapper leaveRecordMapper;

    public LeaveRecordService(EmployeeMapper employeeMapper, LeaveRecordMapper leaveRecordMapper) {
        this.employeeMapper = employeeMapper;
        this.leaveRecordMapper = leaveRecordMapper;
    }

    public Long create(LeaveRecordCreateRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        if (employeeMapper.selectById(request.empId()) == null) {
            throw new IllegalArgumentException("员工不存在：" + request.empId());
        }

        LeaveRecord record = new LeaveRecord();
        record.setEmpId(request.empId());
        record.setStartDate(request.startDate());
        record.setEndDate(request.endDate());
        record.setStatus(request.status());
        leaveRecordMapper.insert(record);
        return record.getId();
    }

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
}
