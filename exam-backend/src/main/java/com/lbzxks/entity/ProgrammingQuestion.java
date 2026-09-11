package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 编程题扩展表 (programming_question)
 */
@Data
@TableName("programming_question")
public class ProgrammingQuestion {

    @TableId(type = IdType.INPUT)
    private Long questionId;

    private String languages;

    private Integer timeLimit;

    private Integer memoryLimit;

    /** 测试用例, JSON 文本 */
    private String testCases;
}
