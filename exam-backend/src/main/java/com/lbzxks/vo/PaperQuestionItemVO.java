package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 试卷中的题目项出参
 */
@Data
public class PaperQuestionItemVO {

    private Long questionId;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private BigDecimal score;

    private Integer sort;
}
