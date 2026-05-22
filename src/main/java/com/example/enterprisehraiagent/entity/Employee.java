package com.example.enterprisehraiagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Long id;

    private String name;

    private String department;

    private String email;

    private Integer annualLeaveTotal;

    private Integer annualLeaveUsed;
}
