package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 班级成绩对比项
 */
@Data
public class ClassStatsVO {

    private Long classId;

    private String className;

    private long studentCount;

    private BigDecimal avgScore;
}
