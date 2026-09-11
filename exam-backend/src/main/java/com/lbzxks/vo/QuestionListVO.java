package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目列表项出参(不含答案)
 */
@Data
public class QuestionListVO {

    private Long id;

    private Long subjectId;

    private String subjectName;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private Integer difficulty;

    private BigDecimal defaultScore;

    private Integer status;

    private LocalDateTime createTime;
}
