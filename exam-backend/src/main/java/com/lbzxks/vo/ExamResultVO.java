package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学生交卷后的成绩结果
 */
@Data
public class ExamResultVO {

    private Long examId;

    private String examName;

    private BigDecimal totalScore;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    /** 2已交卷 3已判分 */
    private Integer status;

    private List<ResultQuestionVO> questions;
}
