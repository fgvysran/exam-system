package com.lbzxks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 试卷题目入参(组卷)
 */
@Data
public class PaperQuestionDTO {

    @NotNull(message = "题目不能为空")
    private Long questionId;

    @NotNull(message = "分值不能为空")
    private BigDecimal score;

    private Integer sort;
}
