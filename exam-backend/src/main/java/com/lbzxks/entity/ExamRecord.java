package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作答记录表 (exam_record)
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long paperId;

    private Long userId;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private BigDecimal objectiveScore;

    private BigDecimal subjectiveScore;

    private BigDecimal totalScore;

    /** 状态: 1进行中 2已交卷 3已判分 */
    private Integer status;

    private LocalDateTime createTime;
}
