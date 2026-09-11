package com.lbzxks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 题目新增/修改入参
 */
@Data
public class QuestionDTO {

    @NotNull(message = "学科不能为空")
    private Long subjectId;

    @NotNull(message = "题型不能为空")
    private Integer questionType;

    @NotBlank(message = "题干不能为空")
    private String content;

    /** 选项(单选/多选) */
    private List<OptionDTO> options;

    /** 答案: 选择存 "A"/"A,C", 判断存 "T"/"F", 填空存 JSON 数组 */
    private String answer;

    private String analysis;

    private Integer difficulty;

    private BigDecimal defaultScore;

    /** 判分方式: 1自动 2人工(仅填空/简答生效) */
    private Integer judgeMode;

    private List<Long> knowledgePointIds;

    /** 编程题扩展(题型=7 时) */
    private ProgrammingQuestionDTO programming;
}
