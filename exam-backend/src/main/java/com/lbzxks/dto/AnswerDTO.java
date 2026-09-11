package com.lbzxks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 作答入参(保存答案/交卷)
 */
@Data
public class AnswerDTO {

    @NotNull(message = "题目不能为空")
    private Long questionId;

    private String answer;
}
