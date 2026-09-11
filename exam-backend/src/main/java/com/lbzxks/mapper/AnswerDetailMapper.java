package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lbzxks.entity.AnswerDetail;
import com.lbzxks.vo.GradingItemVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AnswerDetailMapper extends BaseMapper<AnswerDetail> {

    /**
     * 待阅卷列表(judge_status=0), 联表带出学生/考试/题目/参考答案/满分
     */
    @Select("SELECT ad.id AS detail_id, er.exam_id AS exam_id, e.name AS exam_name, " +
            "er.user_id AS student_id, u.real_name AS student_name, " +
            "ad.question_id AS question_id, ad.question_type AS question_type, q.content AS content, " +
            "ad.user_answer AS user_answer, q.answer AS reference_answer, pq.score AS full_score " +
            "FROM answer_detail ad " +
            "JOIN exam_record er ON ad.exam_record_id = er.id " +
            "JOIN exam e ON er.exam_id = e.id " +
            "JOIN sys_user u ON er.user_id = u.id " +
            "JOIN question q ON ad.question_id = q.id " +
            "JOIN paper_question pq ON pq.paper_id = er.paper_id AND pq.question_id = ad.question_id " +
            "WHERE ad.judge_status = 0 " +
            "AND (#{examId} IS NULL OR er.exam_id = #{examId}) " +
            "ORDER BY ad.id")
    IPage<GradingItemVO> selectPendingPage(IPage<GradingItemVO> page, @Param("examId") Long examId);
}
