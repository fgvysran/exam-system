package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷列表项出参
 */
@Data
public class PaperListVO {

    private Long id;

    private String name;

    private Long subjectId;

    private String subjectName;

    private BigDecimal totalScore;

    private Integer duration;

    private Integer difficulty;

    private Integer status;

    private Integer questionCount;

    private LocalDateTime createTime;
}
