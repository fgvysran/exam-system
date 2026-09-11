package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单场考试统计汇总
 */
@Data
public class ExamStatsVO {

    private Long examId;

    private String examName;

    private BigDecimal paperTotalScore;

    /** 参加人数(有作答记录的) */
    private long totalStudents;

    /** 已交卷人数 */
    private long submittedCount;

    /** 已判分人数 */
    private long gradedCount;

    private BigDecimal avgScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    /** 及格率(百分比) */
    private BigDecimal passRate;
}
