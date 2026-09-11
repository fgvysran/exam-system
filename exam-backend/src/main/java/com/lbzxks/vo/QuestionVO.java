package com.lbzxks.vo;

import com.lbzxks.dto.OptionDTO;
import com.lbzxks.dto.ProgrammingQuestionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 题目详情出参(含答案, 供教师编辑)
 */
@Data
public class QuestionVO {

    private Long id;

    private Long subjectId;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private List<OptionDTO> options;

    private String answer;

    private String analysis;

    private Integer difficulty;

    private BigDecimal defaultScore;

    private Integer judgeMode;

    private Integer status;

    private List<Long> knowledgePointIds;

    private ProgrammingQuestionDTO programming;
}
