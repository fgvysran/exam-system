package com.lbzxks.dto;

import lombok.Data;

/**
 * 题目分页列表查询条件
 */
@Data
public class QuestionQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long subjectId;

    private Integer questionType;

    private Integer difficulty;

    private Long knowledgePointId;

    private String keyword;
}
