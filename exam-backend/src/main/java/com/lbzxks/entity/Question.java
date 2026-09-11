package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目表 (question)
 */
@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    /** 题型: 1单选 2多选 3判断 4填空 5简答 6论述 7编程 */
    private Integer questionType;

    private String content;

    /** 选项(单选/多选), JSON 文本 */
    private String options;

    private String answer;

    private String analysis;

    /** 难度: 1易 2中 3难 */
    private Integer difficulty;

    private BigDecimal defaultScore;

    private Long creatorId;

    /** 状态: 1启用 0停用 */
    private Integer status;

    /** 判分方式: 1自动 2人工(仅填空/简答生效) */
    private Integer judgeMode;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
