package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lbzxks.entity.Subject;
import com.lbzxks.vo.SubjectVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SubjectMapper extends BaseMapper<Subject> {

    /**
     * 学科列表(含题目数量)
     */
    @Select("SELECT s.id, s.name, s.code, COUNT(q.id) AS question_count " +
            "FROM subject s LEFT JOIN question q ON q.subject_id = s.id " +
            "GROUP BY s.id, s.name, s.code ORDER BY s.id")
    List<SubjectVO> selectWithQuestionCount();

    /**
     * 统计某学科下的题目数量(删除校验用)
     */
    @Select("SELECT COUNT(*) FROM question WHERE subject_id = #{subjectId}")
    long countQuestionsBySubjectId(Long subjectId);
}
