package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.EmployeeCreateRequest;
import com.example.enterprisehraiagent.dto.EmployeeResponse;
import com.example.enterprisehraiagent.entity.Employee;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    public Long create(EmployeeCreateRequest request) {
        Employee employee = new Employee();
        employee.setName(request.name().trim());
        employee.setDepartment(request.department().trim());
        employee.setEmail(request.email().trim());
        employee.setAnnualLeaveTotal(request.annualLeaveTotal());
        employee.setAnnualLeaveUsed(request.annualLeaveUsed());
        employeeMapper.insert(employee);
        return employee.getId();
    }

    public List<EmployeeResponse> list(String keyword) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<Employee>()
                .orderByAsc(Employee::getId);
        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            wrapper.and(query -> query
                    .like(Employee::getName, trimmedKeyword)
                    .or()
                    .like(Employee::getDepartment, trimmedKeyword)
                    .or()
                    .like(Employee::getEmail, trimmedKeyword));
        }
        return employeeMapper.selectList(wrapper).stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    public EmployeeResponse getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在：" + id);
        }
        return EmployeeResponse.from(employee);
    }
}
