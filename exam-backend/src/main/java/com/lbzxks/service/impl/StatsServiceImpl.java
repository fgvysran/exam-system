package com.lbzxks.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.entity.Exam;
import com.lbzxks.entity.ExamRecord;
import com.lbzxks.entity.Paper;
import com.lbzxks.entity.SysClass;
import com.lbzxks.entity.SysUser;
import com.lbzxks.excel.ScoreExcel;
import com.lbzxks.mapper.ExamMapper;
import com.lbzxks.mapper.ExamRecordMapper;
import com.lbzxks.mapper.PaperMapper;
import com.lbzxks.mapper.SysClassMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.service.StatsService;
import com.lbzxks.vo.ClassStatsVO;
import com.lbzxks.vo.ExamStatsVO;
import com.lbzxks.vo.RankingItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;
    private final PaperMapper paperMapper;
    private final SysUserMapper sysUserMapper;
    private final SysClassMapper sysClassMapper;

    @Override
    public ExamStatsVO summary(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        Paper paper = paperMapper.selectById(exam.getPaperId());
        BigDecimal paperTotal = paper != null && paper.getTotalScore() != null
                ? paper.getTotalScore() : BigDecimal.valueOf(100);

        List<ExamRecord> all = examRecordMapper.selectList(
                Wrappers.<ExamRecord>lambdaQuery().eq(ExamRecord::getExamId, examId));
        List<ExamRecord> submitted = all.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() >= 2)
                .collect(Collectors.toList());
        long gradedCount = submitted.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 3).count();

        ExamStatsVO vo = new ExamStatsVO();
        vo.setExamId(examId);
        vo.setExamName(exam.getName());
        vo.setPaperTotalScore(paperTotal);
        vo.setTotalStudents(all.size());
        vo.setSubmittedCount(submitted.size());
        vo.setGradedCount(gradedCount);

        if (!submitted.isEmpty()) {
            List<BigDecimal> scores = submitted.stream()
                    .map(r -> r.getTotalScore() == null ? BigDecimal.ZERO : r.getTotalScore())
                    .collect(Collectors.toList());
            BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal max = scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            BigDecimal passLine = paperTotal.multiply(BigDecimal.valueOf(0.6));
            long passCount = scores.stream().filter(s -> s.compareTo(passLine) >= 0).count();
            BigDecimal passRate = BigDecimal.valueOf(passCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            vo.setAvgScore(avg);
            vo.setMaxScore(max);
            vo.setMinScore(min);
            vo.setPassRate(passRate);
        }
        return vo;
    }

    @Override
    public List<RankingItemVO> ranking(Long examId) {
        List<ExamRecord> submitted = submittedRecords(examId);
        submitted.sort((a, b) -> {
            BigDecimal sa = a.getTotalScore() == null ? BigDecimal.ZERO : a.getTotalScore();
            BigDecimal sb = b.getTotalScore() == null ? BigDecimal.ZERO : b.getTotalScore();
            return sb.compareTo(sa);
        });

        Map<Long, SysUser> userMap = userMap(submitted);
        Map<Long, String> classNameMap = classNameMap(userMap);

        List<RankingItemVO> list = new ArrayList<>();
        int rank = 1;
        for (ExamRecord r : submitted) {
            RankingItemVO item = new RankingItemVO();
            item.setRank(rank++);
            item.setUserId(r.getUserId());
            SysUser u = userMap.get(r.getUserId());
            if (u != null) {
                item.setUsername(u.getUsername());
                item.setStudentName(u.getRealName());
                item.setClassName(u.getClassId() == null ? null : classNameMap.get(u.getClassId()));
            }
            item.setObjectiveScore(r.getObjectiveScore());
            item.setSubjectiveScore(r.getSubjectiveScore());
            item.setTotalScore(r.getTotalScore());
            item.setStatus(r.getStatus());
            list.add(item);
        }
        return list;
    }

    @Override
    public List<ClassStatsVO> classCompare(Long examId) {
        List<ExamRecord> submitted = submittedRecords(examId);
        Map<Long, SysUser> userMap = userMap(submitted);
        Map<Long, String> classNameMap = classNameMap(userMap);

        Map<Long, List<BigDecimal>> byClass = new LinkedHashMap<>();
        for (ExamRecord r : submitted) {
            SysUser u = userMap.get(r.getUserId());
            Long cid = u == null ? null : u.getClassId();
            byClass.computeIfAbsent(cid, k -> new ArrayList<>())
                    .add(r.getTotalScore() == null ? BigDecimal.ZERO : r.getTotalScore());
        }

        List<ClassStatsVO> list = new ArrayList<>();
        for (Map.Entry<Long, List<BigDecimal>> en : byClass.entrySet()) {
            List<BigDecimal> scores = en.getValue();
            BigDecimal avg = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            ClassStatsVO vo = new ClassStatsVO();
            vo.setClassId(en.getKey());
            vo.setClassName(en.getKey() == null ? "未分班" : classNameMap.get(en.getKey()));
            vo.setStudentCount(scores.size());
            vo.setAvgScore(avg);
            list.add(vo);
        }
        return list;
    }

    @Override
    public List<ScoreExcel> exportScore(Long examId) {
        return ranking(examId).stream().map(r -> {
            ScoreExcel e = new ScoreExcel();
            e.setRank(r.getRank());
            e.setUsername(r.getUsername());
            e.setStudentName(r.getStudentName());
            e.setClassName(r.getClassName());
            e.setObjectiveScore(r.getObjectiveScore());
            e.setSubjectiveScore(r.getSubjectiveScore());
            e.setTotalScore(r.getTotalScore());
            return e;
        }).collect(Collectors.toList());
    }

    // ---- 工具 ----

    private List<ExamRecord> submittedRecords(Long examId) {
        return examRecordMapper.selectList(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(ExamRecord::getExamId, examId)
                        .ge(ExamRecord::getStatus, 2));
    }

    private Map<Long, SysUser> userMap(List<ExamRecord> records) {
        List<Long> userIds = records.stream().map(ExamRecord::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private Map<Long, String> classNameMap(Map<Long, SysUser> userMap) {
        List<Long> classIds = userMap.values().stream()
                .map(SysUser::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysClassMapper.selectByIds(classIds).stream()
                .collect(Collectors.toMap(SysClass::getId, SysClass::getClassName));
    }
}
