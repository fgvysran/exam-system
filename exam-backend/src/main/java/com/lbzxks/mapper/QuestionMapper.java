package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lbzxks.entity.Question;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 查询某知识点下的题目 ID 列表(筛选用)
     */
    @Select("SELECT question_id FROM question_knowledge WHERE knowledge_point_id = #{knowledgePointId}")
    List<Long> selectQuestionIdsByKnowledgePointId(Long knowledgePointId);

    /**
     * 统计题目被试卷引用的数量(删除校验用)
     */
    @Select("SELECT COUNT(*) FROM paper_question WHERE question_id = #{questionId}")
    long countPaperQuestionByQuestionId(Long questionId);
}
