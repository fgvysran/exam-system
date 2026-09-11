package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 答题明细表 (answer_detail)
 */
@Data
@TableName("answer_detail")
public class AnswerDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examRecordId;

    private Long questionId;

    private Integer questionType;

    private String userAnswer;

    /** 是否正确(客观题): 1对 0错 */
    private Integer isCorrect;

    private BigDecimal score;

    /** 判分状态: 0待判 1判分中 2已判 */
    private Integer judgeStatus;

    private Long judgeBy;

    private LocalDateTime judgeTime;

    private String comment;
}
