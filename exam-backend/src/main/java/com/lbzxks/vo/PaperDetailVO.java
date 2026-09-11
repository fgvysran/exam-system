package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试卷详情出参(含题目列表)
 */
@Data
public class PaperDetailVO {

    private Long id;

    private String name;

    private Long subjectId;

    private String subjectName;

    private BigDecimal totalScore;

    private Integer duration;

    private Integer difficulty;

    private String description;

    private Integer status;

    private List<PaperQuestionItemVO> questions;
}
