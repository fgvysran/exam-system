package com.lbzxks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 阅卷打分入参
 */
@Data
public class GradeDTO {

    @NotNull(message = "得分不能为空")
    private BigDecimal score;

    private String comment;
}
