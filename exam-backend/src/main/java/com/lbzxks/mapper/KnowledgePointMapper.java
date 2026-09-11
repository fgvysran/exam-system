package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lbzxks.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Select;

public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    /**
     * 统计某知识点被题目引用的数量(删除校验用)
     */
    @Select("SELECT COUNT(*) FROM question_knowledge WHERE knowledge_point_id = #{knowledgePointId}")
    long countQuestionsByKnowledgePointId(Long knowledgePointId);
}
