package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.LeaveRecordCreateRequest;
import com.example.enterprisehraiagent.dto.LeaveRecordResponse;
import com.example.enterprisehraiagent.service.LeaveRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-records")
public class LeaveRecordController {

    private final LeaveRecordService leaveRecordService;

    public LeaveRecordController(LeaveRecordService leaveRecordService) {
        this.leaveRecordService = leaveRecordService;
    }

    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody LeaveRecordCreateRequest request) {
        return Map.of("id", leaveRecordService.create(request));
    }

    @GetMapping("/employee/{empId}")
    public List<LeaveRecordResponse> listByEmployee(@PathVariable Long empId) {
        return leaveRecordService.listByEmployee(empId);
    }
}
