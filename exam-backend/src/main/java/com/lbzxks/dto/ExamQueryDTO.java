package com.lbzxks.dto;

import lombok.Data;

/**
 * 考试列表查询条件
 */
@Data
public class ExamQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;
}
