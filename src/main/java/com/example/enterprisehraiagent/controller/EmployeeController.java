package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.EmployeeCreateRequest;
import com.example.enterprisehraiagent.dto.EmployeeResponse;
import com.example.enterprisehraiagent.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody EmployeeCreateRequest request) {
        return Map.of("id", employeeService.create(request));
    }

    @GetMapping
    public List<EmployeeResponse> list(@RequestParam(required = false) String keyword) {
        return employeeService.list(keyword);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.getById(id);
    }
}
