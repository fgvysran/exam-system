package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷表 (paper)
 */
@Data
@TableName("paper")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long subjectId;

    private BigDecimal totalScore;

    /** 考试时长(分钟) */
    private Integer duration;

    private Integer difficulty;

    private String description;

    private Long creatorId;

    /** 状态: 1草稿 2发布 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
