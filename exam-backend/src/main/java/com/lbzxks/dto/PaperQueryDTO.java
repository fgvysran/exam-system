package com.lbzxks.dto;

import lombok.Data;

/**
 * 试卷分页列表查询条件
 */
@Data
public class PaperQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long subjectId;

    private Integer status;

    private String keyword;
}
