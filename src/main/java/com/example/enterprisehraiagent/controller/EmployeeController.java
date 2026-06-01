package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.EmployeeCreateRequest;
import com.example.enterprisehraiagent.dto.EmployeeResponse;
import com.example.enterprisehraiagent.dto.EmployeeUpdateRequest;
import com.example.enterprisehraiagent.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 注入员工业务服务。
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 新增员工并返回新员工 ID。
     */
    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return Map.of("id", employeeService.create(request));
    }

    /**
     * 查询员工列表，支持按姓名、部门或邮箱关键词过滤。
     */
    @GetMapping
    public List<EmployeeResponse> list(@RequestParam(required = false) String keyword) {
        return employeeService.list(keyword);
    }

    /**
     * 根据员工 ID 查询单个员工详情。
     */
    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }

    /**
     * 根据员工 ID 更新员工资料和年假数据。
     */
    @PutMapping("/{id}")
    public Map<String, String> update(@PathVariable Long id,
                                      @Valid @RequestBody EmployeeUpdateRequest request) {
        employeeService.update(id, request);
        return Map.of("message", "员工信息已更新");
    }

    /**
     * 根据员工 ID 删除员工。
     */
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Map.of("message", "员工已删除");
    }
}
