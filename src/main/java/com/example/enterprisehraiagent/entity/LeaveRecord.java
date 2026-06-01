package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 请假记录表。
 *
 * <p>status 使用枚举，MyBatis 默认会以枚举 name 写入 VARCHAR 字段，
 * 与 PENDING / APPROVED / REJECTED 这类业务状态值保持一致。</p>
 */
@Data
@TableName("leave_record")
public class LeaveRecord {

    @TableId(type = IdType.AUTO)
    @Schema(description = "请假记录主键 ID")
    private Long id;

    @Schema(description = "请假员工 ID")
    private Long empId;

    @Schema(description = "请假开始日期")
    private LocalDate startDate;

    @Schema(description = "请假结束日期")
    private LocalDate endDate;

    @Schema(description = "请假审批状态")
    private LeaveStatus status;
}
