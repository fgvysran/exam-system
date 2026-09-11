package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 待阅卷题目项
 */
@Data
public class GradingItemVO {

    private Long detailId;

    private Long examId;

    private String examName;

    private Long studentId;

    private String studentName;

    private Long questionId;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private String userAnswer;

    private String referenceAnswer;

    private BigDecimal fullScore;
}
