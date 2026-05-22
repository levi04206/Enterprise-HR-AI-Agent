package com.example.enterprisehraiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.enterprisehraiagent.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
