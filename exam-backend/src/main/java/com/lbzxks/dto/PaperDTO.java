package com.lbzxks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 试卷新增/修改入参
 */
@Data
public class PaperDTO {

    @NotBlank(message = "试卷名称不能为空")
    private String name;

    @NotNull(message = "学科不能为空")
    private Long subjectId;

    /** 考试时长(分钟) */
    private Integer duration;

    private Integer difficulty;

    private String description;
}
