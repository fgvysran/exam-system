package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.ExamDTO;
import com.lbzxks.dto.ExamQueryDTO;
import com.lbzxks.entity.Exam;
import com.lbzxks.entity.ExamClass;
import com.lbzxks.entity.ExamRecord;
import com.lbzxks.entity.Paper;
import com.lbzxks.entity.SysClass;
import com.lbzxks.entity.SysUser;
import com.lbzxks.mapper.ExamClassMapper;
import com.lbzxks.mapper.ExamMapper;
import com.lbzxks.mapper.ExamRecordMapper;
import com.lbzxks.mapper.PaperMapper;
import com.lbzxks.mapper.SysClassMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.service.ExamService;
import com.lbzxks.vo.ClassVO;
import com.lbzxks.vo.ExamDetailVO;
import com.lbzxks.vo.ExamListVO;
import com.lbzxks.vo.MyExamVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ExamClassMapper examClassMapper;
    private final ExamRecordMapper examRecordMapper;
    private final PaperMapper paperMapper;
    private final SysClassMapper sysClassMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public PageResult<ExamListVO> page(ExamQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        Page<Exam> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Exam> wrapper = Wrappers.<Exam>lambdaQuery()
                .like(StringUtils.hasText(query.getKeyword()), Exam::getName, query.getKeyword())
                .orderByDesc(Exam::getId);
        IPage<Exam> result = examMapper.selectPage(page, wrapper);

        List<Long> paperIds = result.getRecords().stream()
                .map(Exam::getPaperId).distinct().collect(Collectors.toList());
        Map<Long, String> paperNameMap = paperIds.isEmpty() ? Collections.emptyMap()
                : paperMapper.selectByIds(paperIds).stream()
                        .collect(Collectors.toMap(Paper::getId, Paper::getName));

        List<ExamListVO> records = result.getRecords().stream()
                .map(e -> toListVO(e, paperNameMap)).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public ExamDetailVO detail(Long id) {
        Exam e = examMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("考试不存在");
        }
        ExamDetailVO vo = new ExamDetailVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setPaperId(e.getPaperId());
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setDuration(resolveDuration(e));
        vo.setStatus(resolveStatus(e));
        Paper paper = paperMapper.selectById(e.getPaperId());
        vo.setPaperName(paper != null ? paper.getName() : null);

        List<Long> classIds = examClassMapper.selectList(
                        Wrappers.<ExamClass>lambdaQuery().eq(ExamClass::getExamId, id))
                .stream().map(ExamClass::getClassId).collect(Collectors.toList());
        List<ClassVO> classes = classIds.isEmpty() ? Collections.emptyList()
                : sysClassMapper.selectByIds(classIds).stream().map(c -> {
                    ClassVO cv = new ClassVO();
                    cv.setId(c.getId());
                    cv.setClassName(c.getClassName());
                    return cv;
                }).collect(Collectors.toList());
        vo.setClasses(classes);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(ExamDTO dto) {
        if (paperMapper.selectById(dto.getPaperId()) == null) {
            throw new BusinessException("试卷不存在");
        }
        if (dto.getEndTime() != null && dto.getStartTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        Exam e = new Exam();
        e.setName(dto.getName());
        e.setPaperId(dto.getPaperId());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setDuration(dto.getDuration());
        e.setCreatorId(StpUtil.getLoginIdAsLong());
        e.setStatus(1);
        examMapper.insert(e);
        saveClasses(e.getId(), dto.getClassIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ExamDTO dto) {
        Exam e = examMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("考试不存在");
        }
        if (paperMapper.selectById(dto.getPaperId()) == null) {
            throw new BusinessException("试卷不存在");
        }
        if (dto.getEndTime() != null && dto.getStartTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        e.setName(dto.getName());
        e.setPaperId(dto.getPaperId());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setDuration(dto.getDuration());
        examMapper.updateById(e);

        examClassMapper.delete(Wrappers.<ExamClass>lambdaQuery().eq(ExamClass::getExamId, id));
        saveClasses(id, dto.getClassIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (examMapper.selectById(id) == null) {
            throw new BusinessException("考试不存在");
        }
        long recordCount = examRecordMapper.selectCount(
                Wrappers.<ExamRecord>lambdaQuery().eq(ExamRecord::getExamId, id));
        if (recordCount > 0) {
            throw new BusinessException("已有考生作答，无法删除");
        }
        examMapper.deleteById(id);
        examClassMapper.delete(Wrappers.<ExamClass>lambdaQuery().eq(ExamClass::getExamId, id));
    }

    @Override
    public List<MyExamVO> myExams() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getClassId() == null) {
            return Collections.emptyList();
        }
        List<Long> examIds = examClassMapper.selectList(
                        Wrappers.<ExamClass>lambdaQuery().eq(ExamClass::getClassId, user.getClassId()))
                .stream().map(ExamClass::getExamId).collect(Collectors.toList());
        if (examIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Exam> exams = examMapper.selectList(
                Wrappers.<Exam>lambdaQuery().in(Exam::getId, examIds).orderByDesc(Exam::getId));

        List<Long> paperIds = exams.stream().map(Exam::getPaperId).distinct().collect(Collectors.toList());
        Map<Long, String> paperNameMap = paperIds.isEmpty() ? Collections.emptyMap()
                : paperMapper.selectByIds(paperIds).stream()
                        .collect(Collectors.toMap(Paper::getId, Paper::getName));

        List<ExamRecord> records = examRecordMapper.selectList(
                Wrappers.<ExamRecord>lambdaQuery()
                        .eq(ExamRecord::getUserId, userId)
                        .in(ExamRecord::getExamId, examIds));
        Map<Long, ExamRecord> recordMap = records.stream()
                .collect(Collectors.toMap(ExamRecord::getExamId, r -> r, (a, b) -> a));

        return exams.stream().map(e -> {
            MyExamVO vo = new MyExamVO();
            vo.setExamId(e.getId());
            vo.setExamName(e.getName());
            vo.setPaperName(paperNameMap.get(e.getPaperId()));
            vo.setStartTime(e.getStartTime());
            vo.setEndTime(e.getEndTime());
            vo.setDuration(resolveDuration(e));
            vo.setExamStatus(resolveStatus(e));
            ExamRecord r = recordMap.get(e.getId());
            vo.setRecordStatus(r == null ? 0 : r.getStatus());
            if (r != null) {
                vo.setTotalScore(r.getTotalScore());
                vo.setObjectiveScore(r.getObjectiveScore());
                vo.setSubjectiveScore(r.getSubjectiveScore());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // ---- 私有方法 ----

    private ExamListVO toListVO(Exam e, Map<Long, String> paperNameMap) {
        ExamListVO vo = new ExamListVO();
        vo.setId(e.getId());
        vo.setName(e.getName());
        vo.setPaperId(e.getPaperId());
        vo.setPaperName(paperNameMap.get(e.getPaperId()));
        vo.setStartTime(e.getStartTime());
        vo.setEndTime(e.getEndTime());
        vo.setDuration(resolveDuration(e));
        vo.setStatus(resolveStatus(e));
        return vo;
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

    private void saveClasses(Long examId, List<Long> classIds) {
        if (classIds == null || classIds.isEmpty()) {
            return;
        }
        for (Long cid : classIds) {
            if (sysClassMapper.selectById(cid) == null) {
                throw new BusinessException("班级不存在: " + cid);
            }
            ExamClass ec = new ExamClass();
            ec.setExamId(examId);
            ec.setClassId(cid);
            examClassMapper.insert(ec);
        }
    }
}
