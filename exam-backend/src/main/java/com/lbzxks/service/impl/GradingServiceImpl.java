package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.constant.QuestionType;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.GradeDTO;
import com.lbzxks.entity.AnswerDetail;
import com.lbzxks.entity.ExamRecord;
import com.lbzxks.mapper.AnswerDetailMapper;
import com.lbzxks.mapper.ExamRecordMapper;
import com.lbzxks.service.GradingService;
import com.lbzxks.vo.GradingItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    private final AnswerDetailMapper answerDetailMapper;
    private final ExamRecordMapper examRecordMapper;

    @Override
    public PageResult<GradingItemVO> pending(Long examId, Integer pageNum, Integer pageSize) {
        int pn = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int ps = pageSize == null || pageSize < 1 ? 10 : pageSize;
        IPage<GradingItemVO> page = answerDetailMapper.selectPendingPage(new Page<>(pn, ps), examId);
        page.getRecords().forEach(i -> i.setQuestionTypeName(QuestionType.nameOf(i.getQuestionType())));
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grade(Long detailId, GradeDTO dto) {
        AnswerDetail detail = answerDetailMapper.selectById(detailId);
        if (detail == null) {
            throw new BusinessException("作答明细不存在");
        }
        if (detail.getJudgeStatus() != null && detail.getJudgeStatus() != 0) {
            throw new BusinessException("该题已批改");
        }
        BigDecimal score = dto.getScore() == null ? BigDecimal.ZERO : dto.getScore();
        detail.setScore(score);
        detail.setJudgeStatus(2);
        detail.setJudgeBy(StpUtil.getLoginIdAsLong());
        detail.setJudgeTime(LocalDateTime.now());
        detail.setComment(dto.getComment());
        answerDetailMapper.updateById(detail);

        // 累加主观分, 并判断是否全部批改完
        ExamRecord record = examRecordMapper.selectById(detail.getExamRecordId());
        if (record != null) {
            BigDecimal oldSubj = record.getSubjectiveScore() == null ? BigDecimal.ZERO : record.getSubjectiveScore();
            BigDecimal newSubj = oldSubj.add(score);
            BigDecimal obj = record.getObjectiveScore() == null ? BigDecimal.ZERO : record.getObjectiveScore();
            record.setSubjectiveScore(newSubj);
            record.setTotalScore(obj.add(newSubj));

            long pending = answerDetailMapper.selectCount(
                    Wrappers.<AnswerDetail>lambdaQuery()
                            .eq(AnswerDetail::getExamRecordId, record.getId())
                            .eq(AnswerDetail::getJudgeStatus, 0));
            if (pending == 0) {
                record.setStatus(3);
            }
            examRecordMapper.updateById(record);
        }
    }
}
