package com.lbzxks.vo;

import com.lbzxks.dto.OptionDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 答题时展示的题目(不含答案)
 */
@Data
public class ExamQuestionVO {

    private Long questionId;

    private Integer questionType;

    private String questionTypeName;

    private String content;

    private List<OptionDTO> options;

    private BigDecimal score;

    private Integer sort;
}
