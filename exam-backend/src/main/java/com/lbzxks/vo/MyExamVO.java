package com.lbzxks.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生视角的考试列表项
 */
@Data
public class MyExamVO {

    private Long examId;

    private String examName;

    private String paperName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer duration;

    /** 考试状态: 1未开始 2进行中 3已结束 */
    private Integer examStatus;

    /** 我的作答状态: 0未参加 1进行中 2已交卷 3已判分 */
    private Integer recordStatus;

    private BigDecimal totalScore;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;
}
