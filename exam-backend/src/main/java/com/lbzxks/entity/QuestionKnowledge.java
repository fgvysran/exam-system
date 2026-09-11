package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目-知识点关联表 (question_knowledge)
 */
@Data
@TableName("question_knowledge")
public class QuestionKnowledge {

    private Long questionId;

    private Long knowledgePointId;
}
