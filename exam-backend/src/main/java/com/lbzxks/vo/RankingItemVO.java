package com.lbzxks.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩排名项
 */
@Data
public class RankingItemVO {

    private Integer rank;

    private Long userId;

    private String username;

    private String studentName;

    private String className;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private BigDecimal totalScore;

    /** 2已交卷 3已判分 */
    private Integer status;
}
