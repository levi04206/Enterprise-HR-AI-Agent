package com.example.enterprisehraiagent.controller;

import com.example.enterprisehraiagent.dto.LeaveRecordCreateRequest;
import com.example.enterprisehraiagent.dto.LeaveRecordResponse;
import com.example.enterprisehraiagent.dto.LeaveRecordReviewRequest;
import com.example.enterprisehraiagent.entity.LeaveStatus;
import com.example.enterprisehraiagent.service.LeaveRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/leave-records")
public class LeaveRecordController {

    private final LeaveRecordService leaveRecordService;

    /**
     * 注入请假记录业务服务。
     */
    public LeaveRecordController(LeaveRecordService leaveRecordService) {
        this.leaveRecordService = leaveRecordService;
    }

    /**
     * 员工提交请假申请，默认进入待审批状态。
     */
    @PostMapping
    public Map<String, Long> create(@Valid @RequestBody LeaveRecordCreateRequest request) {
        return Map.of("id", leaveRecordService.create(request));
    }

    /**
     * 查询请假记录列表，可按审批状态筛选。
     */
    @GetMapping
    public List<LeaveRecordResponse> list(@RequestParam(required = false) LeaveStatus status) {
        return leaveRecordService.list(status);
    }

    /**
     * 查询某个员工的全部请假记录。
     */
    @GetMapping("/employee/{empId}")
    public List<LeaveRecordResponse> listByEmployee(@PathVariable Long empId) {
        return leaveRecordService.listByEmployee(empId);
    }

    /**
     * 管理员审批请假记录，支持批准或驳回。
     */
    @PatchMapping("/{id}/status")
    public LeaveRecordResponse review(@PathVariable Long id, @Valid @RequestBody LeaveRecordReviewRequest request) {
        return leaveRecordService.review(id, request);
    }
}
