package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试安排表 (exam)
 */
@Data
@TableName("exam")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long paperId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 作答时长(分钟), 为空则取试卷时长 */
    private Integer duration;

    private Long creatorId;

    /** 状态: 1未开始 2进行中 3已结束 */
    private Integer status;

    private LocalDateTime createTime;
}
