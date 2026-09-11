package com.lbzxks.vo;

import com.lbzxks.dto.OptionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩结果中的单题明细
 */
@Data
public class ResultQuestionVO {

    private Long questionId;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private List<OptionDTO> options;

    private String userAnswer;

    /** 标准答案(仅已判题目返回, 待判为 null) */
    private String correctAnswer;

    private Integer isCorrect;

    private BigDecimal score;

    private Integer judgeStatus;
}
