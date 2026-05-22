package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Long id;

    private Long empId;

    private LocalDate startDate;

    private LocalDate endDate;

    private LeaveStatus status;
}
