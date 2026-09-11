package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 班级表 (sys_class)
 */
@Data
@TableName("sys_class")
public class SysClass {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String className;

    private String grade;

    private String major;

    private LocalDateTime createTime;
}
