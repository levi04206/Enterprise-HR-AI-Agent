package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工基础信息表。
 *
 * <p>这里使用 MyBatis-Plus 的实体映射，数据库字段是下划线命名，
 * Java 字段是驼峰命名，二者通过 application.yml 中的
 * map-underscore-to-camel-case 自动转换。</p>
 */
@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    @Schema(description = "员工主键 ID")
    private Long id;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "所属部门")
    private String department;

    @Schema(description = "员工邮箱")
    private String email;

    @Schema(description = "员工总年假天数")
    private Integer annualLeaveTotal;

    @Schema(description = "员工已使用年假天数")
    private Integer annualLeaveUsed;
}
