package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.constant.QuestionType;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.common.util.JsonUtil;
import com.lbzxks.dto.AnswerDTO;
import com.lbzxks.dto.OptionDTO;
import com.lbzxks.entity.AnswerDetail;
import com.lbzxks.entity.Exam;
import com.lbzxks.entity.ExamClass;
import com.lbzxks.entity.ExamRecord;
import com.lbzxks.entity.Paper;
import com.lbzxks.entity.PaperQuestion;
import com.lbzxks.entity.Question;
import com.lbzxks.entity.SysUser;
import com.lbzxks.mapper.AnswerDetailMapper;
import com.lbzxks.mapper.ExamClassMapper;
import com.lbzxks.mapper.ExamMapper;
import com.lbzxks.mapper.ExamRecordMapper;
import com.lbzxks.mapper.PaperMapper;
import com.lbzxks.mapper.PaperQuestionMapper;
import com.lbzxks.mapper.QuestionMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.service.AnswerService;
import com.lbzxks.vo.ExamPaperVO;
import com.lbzxks.vo.ExamQuestionVO;
import com.lbzxks.vo.ExamResultVO;
import com.lbzxks.vo.ResultQuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final ExamMapper examMapper;
    private final ExamClassMapper examClassMapper;
    private final ExamRecordMapper examRecordMapper;
    private final AnswerDetailMapper answerDetailMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamPaperVO start(Long examId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        if (resolveStatus(exam) != 2) {
            throw new BusinessException("考试不在进行中");
        }
        checkStudentInExam(userId, examId);

        ExamRecord record = examRecordMapper.selectOne(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId));
        if (record == null) {
            record = new ExamRecord();
            record.setExamId(examId);
            record.setPaperId(exam.getPaperId());
            record.setUserId(userId);
            record.setStartTime(LocalDateTime.now());
            record.setObjectiveScore(BigDecimal.ZERO);
            record.setSubjectiveScore(BigDecimal.ZERO);
            record.setTotalScore(BigDecimal.ZERO);
            record.setStatus(1);
            examRecordMapper.insert(record);
        } else if (record.getStatus() != null && record.getStatus() >= 2) {
            throw new BusinessException("你已交卷，不能再次作答");
        }

        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.<PaperQuestion>lambdaQuery()
                        .eq(PaperQuestion::getPaperId, exam.getPaperId())
                        .orderByAsc(PaperQuestion::getSort));
        List<Long> qids = pqs.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> qMap = qids.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectByIds(qids).stream()
                        .collect(Collectors.toMap(Question::getId, q -> q));

        List<ExamQuestionVO> questions = pqs.stream().map(pq -> {
            ExamQuestionVO vo = new ExamQuestionVO();
            vo.setQuestionId(pq.getQuestionId());
            vo.setScore(pq.getScore());
            vo.setSort(pq.getSort());
            Question q = qMap.get(pq.getQuestionId());
            if (q != null) {
                vo.setQuestionType(q.getQuestionType());
                vo.setQuestionTypeName(QuestionType.nameOf(q.getQuestionType()));
                vo.setContent(q.getContent());
                vo.setOptions(q.getOptions() == null ? null : JsonUtil.fromJsonList(q.getOptions(), OptionDTO.class));
            }
            return vo;
        }).collect(Collectors.toList());

        ExamPaperVO vo = new ExamPaperVO();
        vo.setExamId(examId);
        vo.setExamName(exam.getName());
        vo.setEndTime(exam.getEndTime());
        vo.setDuration(resolveDuration(exam));
        vo.setRecordId(record.getId());
        vo.setQuestions(questions);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAnswers(Long examId, List<AnswerDTO> answers) {
        ExamRecord record = getActiveRecord(examId);
        upsertAnswers(record, answers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResultVO submit(Long examId, List<AnswerDTO> answers) {
        ExamRecord record = getActiveRecord(examId);
        upsertAnswers(record, answers);
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(2);
        examRecordMapper.updateById(record);
        autoGrade(record);
        return buildResult(record);
    }

    @Override
    public ExamResultVO result(Long examId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ExamRecord record = examRecordMapper.selectOne(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId));
        if (record == null || record.getStatus() == null || record.getStatus() < 2) {
            throw new BusinessException("尚未交卷");
        }
        return buildResult(record);
    }

    // ---- 判分 ----

    private void autoGrade(ExamRecord record) {
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                Wrappers.<PaperQuestion>lambdaQuery().eq(PaperQuestion::getPaperId, record.getPaperId()));
        Map<Long, BigDecimal> scoreMap = pqs.stream().collect(
                Collectors.toMap(PaperQuestion::getQuestionId, PaperQuestion::getScore, (a, b) -> a));

        List<AnswerDetail> details = answerDetailMapper.selectList(
                Wrappers.<AnswerDetail>lambdaQuery().eq(AnswerDetail::getExamRecordId, record.getId()));

        BigDecimal objective = BigDecimal.ZERO;
        int pending = 0;
        for (AnswerDetail d : details) {
            Question q = questionMapper.selectById(d.getQuestionId());
            if (q == null) {
                continue;
            }
            BigDecimal fullScore = scoreMap.getOrDefault(d.getQuestionId(), BigDecimal.ZERO);
            if (isAutoGradable(q)) {
                BigDecimal got = grade(q, d.getUserAnswer(), fullScore);
                boolean fullCredit = fullScore.compareTo(BigDecimal.ZERO) > 0
                        && got.compareTo(fullScore) == 0;
                d.setScore(got);
                d.setIsCorrect(fullCredit ? 1 : 0);
                d.setJudgeStatus(2);
                objective = objective.add(got);
            } else {
                d.setScore(BigDecimal.ZERO);
                d.setIsCorrect(null);
                d.setJudgeStatus(0);
                pending++;
            }
            answerDetailMapper.updateById(d);
        }
        record.setObjectiveScore(objective);
        record.setSubjectiveScore(BigDecimal.ZERO);
        record.setTotalScore(objective);
        record.setStatus(pending == 0 ? 3 : 2);
        examRecordMapper.updateById(record);
    }

    private boolean isAutoGradable(Question q) {
        Integer type = q.getQuestionType();
        if (type == null) {
            return false;
        }
        if (type == 1 || type == 2 || type == 3) {
            return true;
        }
        if (type == 4 || type == 5) {
            return q.getJudgeMode() == null || q.getJudgeMode() == 1;
        }
        return false;
    }

    private BigDecimal grade(Question q, String userAnswer, BigDecimal fullScore) {
        if (userAnswer == null) {
            return BigDecimal.ZERO;
        }
        switch (q.getQuestionType()) {
            case 1:
            case 3:
                return equalsIgnoreCase(userAnswer, q.getAnswer()) ? fullScore : BigDecimal.ZERO;
            case 2:
                return sameLetterSet(userAnswer, q.getAnswer()) ? fullScore : BigDecimal.ZERO;
            case 4:
                double ratio = fillBlankRatio(q.getAnswer(), userAnswer);
                return fullScore.multiply(BigDecimal.valueOf(ratio)).setScale(1, RoundingMode.HALF_UP);
            case 5:
                double kwRatio = keywordRatio(q.getAnswer(), userAnswer);
                return fullScore.multiply(BigDecimal.valueOf(kwRatio)).setScale(1, RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }

    /**
     * 简答题按得分点加权给分:
     * 参考答案用 ; 分隔得分点, 得分点内用 | 、 , 分隔等价答案(任一命中即可),
     * 可用冒号指定权重, 如 "封装:6;继承:4" = 封装 6 分、继承 4 分(权重相加为总分)
     * 不指定权重则各得分点均分
     */
    private double keywordRatio(String answer, String userAnswer) {
        List<List<String>> groups = new ArrayList<>();
        List<BigDecimal> weights = new ArrayList<>();
        parseAnswer(answer, groups, weights);
        if (groups.isEmpty()) {
            return 0;
        }
        BigDecimal totalWeight = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            totalWeight = BigDecimal.valueOf(groups.size());
            for (int i = 0; i < weights.size(); i++) {
                weights.set(i, BigDecimal.ONE);
            }
        }
        String normalized = userAnswer == null ? "" : userAnswer.toLowerCase();
        BigDecimal hitWeight = BigDecimal.ZERO;
        for (int i = 0; i < groups.size(); i++) {
            boolean matched = groups.get(i).stream()
                    .anyMatch(kw -> normalized.contains(kw.toLowerCase()));
            if (matched) {
                hitWeight = hitWeight.add(weights.get(i));
            }
        }
        return hitWeight.divide(totalWeight, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 解析参考答案: 按 ; 分隔得分点, 冒号后为权重, 得分点内按 | 、 , ， 换行 分隔等价答案
     */
    private void parseAnswer(String s, List<List<String>> groups, List<BigDecimal> weights) {
        if (s == null || s.isBlank()) {
            return;
        }
        for (String part : s.split("[;；]")) {
            String altsPart = part;
            BigDecimal weight = BigDecimal.ONE;
            int colon = part.indexOf(':');
            if (colon > 0) {
                String weightStr = part.substring(colon + 1).trim();
                try {
                    BigDecimal w = new BigDecimal(weightStr);
                    if (w.compareTo(BigDecimal.ZERO) > 0) {
                        weight = w;
                        altsPart = part.substring(0, colon);
                    }
                } catch (NumberFormatException ignored) {
                    // 冒号后不是数字, 当作答案内容
                }
            }
            List<String> alts = Arrays.stream(altsPart.split("[|、,，\\n\\r]+"))
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toList());
            if (!alts.isEmpty()) {
                groups.add(alts);
                weights.add(weight);
            }
        }
    }

    private boolean sameLetterSet(String a, String b) {
        Set<Character> sa = letters(a);
        Set<Character> sb = letters(b);
        return !sa.isEmpty() && sa.equals(sb);
    }

    private Set<Character> letters(String s) {
        Set<Character> set = new HashSet<>();
        if (s == null) {
            return set;
        }
        for (char c : s.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                set.add(c);
            }
        }
        return set;
    }

    private double fillBlankRatio(String refJson, String userJson) {
        try {
            List<String> refs = JsonUtil.fromJsonList(refJson, String.class);
            List<String> ans = JsonUtil.fromJsonList(userJson, String.class);
            if (refs == null || refs.isEmpty()) {
                return 0;
            }
            int correct = 0;
            for (int i = 0; i < refs.size(); i++) {
                String expect = refs.get(i) == null ? "" : refs.get(i).trim();
                String actual = (i < ans.size() && ans.get(i) != null) ? ans.get(i).trim() : "";
                if (expect.equalsIgnoreCase(actual)) {
                    correct++;
                }
            }
            return (double) correct / refs.size();
        } catch (Exception e) {
            return 0;
        }
    }

    // ---- 结果组装 ----

    private ExamResultVO buildResult(ExamRecord record) {
        Exam exam = examMapper.selectById(record.getExamId());
        ExamResultVO vo = new ExamResultVO();
        vo.setExamId(record.getExamId());
        vo.setExamName(exam != null ? exam.getName() : null);
        vo.setTotalScore(record.getTotalScore());
        vo.setObjectiveScore(record.getObjectiveScore());
        vo.setSubjectiveScore(record.getSubjectiveScore());
        vo.setStatus(record.getStatus());

        List<AnswerDetail> details = answerDetailMapper.selectList(
                Wrappers.<AnswerDetail>lambdaQuery().eq(AnswerDetail::getExamRecordId, record.getId()));
        List<Long> qids = details.stream().map(AnswerDetail::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> qMap = qids.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectByIds(qids).stream()
                        .collect(Collectors.toMap(Question::getId, q -> q));

        List<ResultQuestionVO> items = details.stream().map(d -> {
            ResultQuestionVO rq = new ResultQuestionVO();
            rq.setQuestionId(d.getQuestionId());
            rq.setUserAnswer(d.getUserAnswer());
            rq.setIsCorrect(d.getIsCorrect());
            rq.setScore(d.getScore());
            rq.setJudgeStatus(d.getJudgeStatus());
            Question q = qMap.get(d.getQuestionId());
            if (q != null) {
                rq.setQuestionType(q.getQuestionType());
                rq.setQuestionTypeName(QuestionType.nameOf(q.getQuestionType()));
                rq.setContent(q.getContent());
                rq.setOptions(q.getOptions() == null ? null : JsonUtil.fromJsonList(q.getOptions(), OptionDTO.class));
                rq.setCorrectAnswer(d.getJudgeStatus() != null && d.getJudgeStatus() == 2 ? q.getAnswer() : null);
            }
            return rq;
        }).collect(Collectors.toList());
        vo.setQuestions(items);
        return vo;
    }

    // ---- 工具方法 ----

    private ExamRecord getActiveRecord(Long examId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ExamRecord record = examRecordMapper.selectOne(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId));
        if (record == null) {
            throw new BusinessException("请先开始考试");
        }
        if (record.getStatus() != null && record.getStatus() >= 2) {
            throw new BusinessException("你已交卷");
        }
        return record;
    }

    private void upsertAnswers(ExamRecord record, List<AnswerDTO> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        for (AnswerDTO a : answers) {
            AnswerDetail detail = answerDetailMapper.selectOne(
                    Wrappers.<AnswerDetail>lambdaQuery()
                            .eq(AnswerDetail::getExamRecordId, record.getId())
                            .eq(AnswerDetail::getQuestionId, a.getQuestionId()));
            if (detail == null) {
                detail = new AnswerDetail();
                detail.setExamRecordId(record.getId());
                detail.setQuestionId(a.getQuestionId());
                Question q = questionMapper.selectById(a.getQuestionId());
                detail.setQuestionType(q != null ? q.getQuestionType() : null);
                detail.setUserAnswer(a.getAnswer());
                detail.setJudgeStatus(0);
                detail.setScore(BigDecimal.ZERO);
                answerDetailMapper.insert(detail);
            } else {
                detail.setUserAnswer(a.getAnswer());
                answerDetailMapper.updateById(detail);
            }
        }
    }

    private void checkStudentInExam(Long userId, Long examId) {
        SysUser user = sysUserMapper.selectById(userId);
        Long classId = user == null ? null : user.getClassId();
        if (classId == null) {
            throw new BusinessException("你未分配班级，无法参加考试");
        }
        Long count = examClassMapper.selectCount(
                Wrappers.<ExamClass>lambdaQuery()
                        .eq(ExamClass::getExamId, examId)
                        .eq(ExamClass::getClassId, classId));
        if (count == null || count == 0) {
            throw new BusinessException("你不属于该考试的参加班级");
        }
    }

    private int resolveStatus(Exam e) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(e.getStartTime())) {
            return 1;
        }
        if (now.isAfter(e.getEndTime())) {
            return 3;
        }
        return 2;
    }

    private Integer resolveDuration(Exam e) {
        if (e.getDuration() != null) {
            return e.getDuration();
        }
        Paper p = paperMapper.selectById(e.getPaperId());
        return p != null ? p.getDuration() : null;
    }
}
