package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.constant.QuestionType;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.PaperDTO;
import com.lbzxks.dto.PaperQueryDTO;
import com.lbzxks.dto.PaperQuestionDTO;
import com.lbzxks.entity.Paper;
import com.lbzxks.entity.PaperQuestion;
import com.lbzxks.entity.Question;
import com.lbzxks.entity.Subject;
import com.lbzxks.mapper.PaperMapper;
import com.lbzxks.mapper.PaperQuestionMapper;
import com.lbzxks.mapper.QuestionMapper;
import com.lbzxks.mapper.SubjectMapper;
import com.lbzxks.service.PaperService;
import com.lbzxks.vo.PaperDetailVO;
import com.lbzxks.vo.PaperListVO;
import com.lbzxks.vo.PaperQuestionItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;

    @Override
    public PageResult<PaperListVO> page(PaperQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        Page<Paper> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery()
                .eq(query.getSubjectId() != null, Paper::getSubjectId, query.getSubjectId())
                .eq(query.getStatus() != null, Paper::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getKeyword()), Paper::getName, query.getKeyword())
                .orderByDesc(Paper::getId);

        IPage<Paper> result = paperMapper.selectPage(page, wrapper);

        List<Long> subjectIds = result.getRecords().stream()
                .map(Paper::getSubjectId).distinct().collect(Collectors.toList());
        Map<Long, String> subjectNameMap = subjectIds.isEmpty() ? Collections.emptyMap()
                : subjectMapper.selectByIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, Subject::getName));

        List<Long> paperIds = result.getRecords().stream().map(Paper::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = paperIds.isEmpty() ? Collections.emptyMap()
                : paperQuestionMapper.selectList(
                        Wrappers.<PaperQuestion>lambdaQuery().in(PaperQuestion::getPaperId, paperIds))
                        .stream().collect(Collectors.groupingBy(PaperQuestion::getPaperId, Collectors.counting()));

        List<PaperListVO> records = result.getRecords().stream().map(p -> {
            PaperListVO vo = new PaperListVO();
            vo.setId(p.getId());
            vo.setName(p.getName());
            vo.setSubjectId(p.getSubjectId());
            vo.setSubjectName(subjectNameMap.get(p.getSubjectId()));
            vo.setTotalScore(p.getTotalScore());
            vo.setDuration(p.getDuration());
            vo.setDifficulty(p.getDifficulty());
            vo.setStatus(p.getStatus());
            vo.setQuestionCount(countMap.getOrDefault(p.getId(), 0L).intValue());
            vo.setCreateTime(p.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public PaperDetailVO detail(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        PaperDetailVO vo = new PaperDetailVO();
        vo.setId(paper.getId());
        vo.setName(paper.getName());
        vo.setSubjectId(paper.getSubjectId());
        vo.setTotalScore(paper.getTotalScore());
        vo.setDuration(paper.getDuration());
        vo.setDifficulty(paper.getDifficulty());
        vo.setDescription(paper.getDescription());
        vo.setStatus(paper.getStatus());
        Subject subject = subjectMapper.selectById(paper.getSubjectId());
        vo.setSubjectName(subject != null ? subject.getName() : null);

        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.<PaperQuestion>lambdaQuery()
                        .eq(PaperQuestion::getPaperId, id)
                        .orderByAsc(PaperQuestion::getSort));
        List<Long> questionIds = pqs.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> questionMap = questionIds.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectByIds(questionIds).stream()
                        .collect(Collectors.toMap(Question::getId, q -> q));

        List<PaperQuestionItemVO> items = pqs.stream().map(pq -> {
            PaperQuestionItemVO item = new PaperQuestionItemVO();
            item.setQuestionId(pq.getQuestionId());
            item.setScore(pq.getScore());
            item.setSort(pq.getSort());
            Question q = questionMap.get(pq.getQuestionId());
            if (q != null) {
                item.setQuestionType(q.getQuestionType());
                item.setQuestionTypeName(QuestionType.nameOf(q.getQuestionType()));
                item.setContent(q.getContent());
            }
            return item;
        }).collect(Collectors.toList());
        vo.setQuestions(items);
        return vo;
    }

    @Override
    public void add(PaperDTO dto) {
        if (subjectMapper.selectById(dto.getSubjectId()) == null) {
            throw new BusinessException("学科不存在");
        }
        Paper paper = new Paper();
        paper.setName(dto.getName());
        paper.setSubjectId(dto.getSubjectId());
        paper.setDuration(dto.getDuration());
        paper.setDifficulty(dto.getDifficulty());
        paper.setDescription(dto.getDescription());
        paper.setTotalScore(BigDecimal.ZERO);
        paper.setStatus(1);
        paper.setCreatorId(StpUtil.getLoginIdAsLong());
        paperMapper.insert(paper);
    }

    @Override
    public void update(Long id, PaperDTO dto) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (subjectMapper.selectById(dto.getSubjectId()) == null) {
            throw new BusinessException("学科不存在");
        }
        paper.setName(dto.getName());
        paper.setSubjectId(dto.getSubjectId());
        paper.setDuration(dto.getDuration());
        paper.setDifficulty(dto.getDifficulty());
        paper.setDescription(dto.getDescription());
        paperMapper.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (paperMapper.selectById(id) == null) {
            throw new BusinessException("试卷不存在");
        }
        if (paperMapper.countExamByPaperId(id) > 0) {
            throw new BusinessException("该试卷已被考试引用，无法删除");
        }
        paperMapper.deleteById(id);
        paperQuestionMapper.delete(
                Wrappers.<PaperQuestion>lambdaQuery().eq(PaperQuestion::getPaperId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        if (Integer.valueOf(2).equals(status)) {
            long qCount = paperQuestionMapper.selectCount(
                    Wrappers.<PaperQuestion>lambdaQuery().eq(PaperQuestion::getPaperId, id));
            if (qCount == 0) {
                throw new BusinessException("试卷为空，不能发布");
            }
        }
        paper.setStatus(status);
        paperMapper.updateById(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestions(Long id, List<PaperQuestionDTO> questions) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        paperQuestionMapper.delete(
                Wrappers.<PaperQuestion>lambdaQuery().eq(PaperQuestion::getPaperId, id));

        BigDecimal total = BigDecimal.ZERO;
        if (questions != null && !questions.isEmpty()) {
            int idx = 1;
            for (PaperQuestionDTO dto : questions) {
                if (questionMapper.selectById(dto.getQuestionId()) == null) {
                    throw new BusinessException("题目不存在: " + dto.getQuestionId());
                }
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(id);
                pq.setQuestionId(dto.getQuestionId());
                pq.setScore(dto.getScore());
                pq.setSort(dto.getSort() != null ? dto.getSort() : idx);
                paperQuestionMapper.insert(pq);
                if (dto.getScore() != null) {
                    total = total.add(dto.getScore());
                }
                idx++;
            }
        }
        paper.setTotalScore(total);
        paperMapper.updateById(paper);
    }
}
