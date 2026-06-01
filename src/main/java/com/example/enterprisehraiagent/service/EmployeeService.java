package com.example.enterprisehraiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enterprisehraiagent.dto.EmployeeCreateRequest;
import com.example.enterprisehraiagent.dto.EmployeeResponse;
import com.example.enterprisehraiagent.dto.EmployeeUpdateRequest;
import com.example.enterprisehraiagent.entity.Employee;
import com.example.enterprisehraiagent.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    /**
     * 注入员工数据访问对象。
     */
    public EmployeeService(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    /**
     * 创建员工实体并写入数据库。
     */
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

    /**
     * 查询员工列表，关键词存在时按姓名、部门、邮箱模糊匹配。
     */
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

    /**
     * 根据员工 ID 查询员工，不存在时抛出业务异常。
     */
    public EmployeeResponse getById(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new NoSuchElementException("员工不存在：" + id);
        }
        return EmployeeResponse.from(employee);
    }

    /**
     * 更新员工基础资料和年假字段。
     */
    public void update(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new NoSuchElementException("员工不存在：" + id);
        }

        employee.setName(request.name().trim());
        employee.setDepartment(request.department().trim());
        employee.setEmail(request.email().trim());
        employee.setAnnualLeaveTotal(request.annualLeaveTotal());
        employee.setAnnualLeaveUsed(request.annualLeaveUsed());
        employeeMapper.updateById(employee);
    }

    /**
     * 删除指定员工，不存在时抛出业务异常。
     */
    public void delete(Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new NoSuchElementException("员工不存在：" + id);
        }
        employeeMapper.deleteById(id);
    }
}
