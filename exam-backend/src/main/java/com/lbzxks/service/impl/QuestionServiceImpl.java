package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.constant.QuestionType;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.common.util.JsonUtil;
import com.lbzxks.dto.OptionDTO;
import com.lbzxks.dto.ProgrammingQuestionDTO;
import com.lbzxks.dto.QuestionDTO;
import com.lbzxks.dto.QuestionQueryDTO;
import com.lbzxks.dto.TestCaseDTO;
import com.lbzxks.entity.ProgrammingQuestion;
import com.lbzxks.entity.Question;
import com.lbzxks.entity.QuestionKnowledge;
import com.lbzxks.entity.Subject;
import com.lbzxks.mapper.ProgrammingQuestionMapper;
import com.lbzxks.mapper.QuestionKnowledgeMapper;
import com.lbzxks.mapper.QuestionMapper;
import com.lbzxks.mapper.SubjectMapper;
import com.lbzxks.service.QuestionService;
import com.lbzxks.vo.QuestionListVO;
import com.lbzxks.vo.QuestionVO;
import com.alibaba.excel.EasyExcel;
import com.lbzxks.excel.QuestionExcel;
import com.lbzxks.vo.ImportResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final ProgrammingQuestionMapper programmingQuestionMapper;
    private final QuestionKnowledgeMapper questionKnowledgeMapper;
    private final SubjectMapper subjectMapper;

    @Override
    public PageResult<QuestionListVO> page(QuestionQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        Page<Question> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Question> wrapper = Wrappers.<Question>lambdaQuery()
                .eq(query.getSubjectId() != null, Question::getSubjectId, query.getSubjectId())
                .eq(query.getQuestionType() != null, Question::getQuestionType, query.getQuestionType())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .like(StringUtils.hasText(query.getKeyword()), Question::getContent, query.getKeyword())
                .orderByDesc(Question::getId);

        if (query.getKnowledgePointId() != null) {
            List<Long> qids = questionMapper.selectQuestionIdsByKnowledgePointId(query.getKnowledgePointId());
            if (qids.isEmpty()) {
                return new PageResult<>(0L, Collections.emptyList());
            }
            wrapper.in(Question::getId, qids);
        }

        IPage<Question> result = questionMapper.selectPage(page, wrapper);

        List<Long> subjectIds = result.getRecords().stream()
                .map(Question::getSubjectId).distinct().collect(Collectors.toList());
        Map<Long, String> subjectNameMap = subjectIds.isEmpty() ? Collections.emptyMap()
                : subjectMapper.selectByIds(subjectIds).stream()
                        .collect(Collectors.toMap(Subject::getId, Subject::getName));

        List<QuestionListVO> records = result.getRecords().stream()
                .map(q -> toListVO(q, subjectNameMap)).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), records);
    }

    @Override
    public QuestionVO detail(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        QuestionVO vo = toVO(q);

        List<Long> kpIds = questionKnowledgeMapper.selectList(
                        Wrappers.<QuestionKnowledge>lambdaQuery().eq(QuestionKnowledge::getQuestionId, id))
                .stream().map(QuestionKnowledge::getKnowledgePointId).collect(Collectors.toList());
        vo.setKnowledgePointIds(kpIds);

        if (QuestionType.PROGRAMMING.getCode().equals(q.getQuestionType())) {
            ProgrammingQuestion pq = programmingQuestionMapper.selectById(id);
            if (pq != null) {
                vo.setProgramming(toProgrammingVO(pq));
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(QuestionDTO dto) {
        validate(dto);
        Question q = new Question();
        applyDTO(q, dto);
        q.setCreatorId(StpUtil.getLoginIdAsLong());
        q.setStatus(1);
        questionMapper.insert(q);

        saveKnowledgePoints(q.getId(), dto.getKnowledgePointIds());
        if (isProgramming(dto.getQuestionType()) && dto.getProgramming() != null) {
            saveProgramming(q.getId(), dto.getProgramming());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, QuestionDTO dto) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        validate(dto);
        applyDTO(q, dto);
        questionMapper.updateById(q);

        questionKnowledgeMapper.delete(
                Wrappers.<QuestionKnowledge>lambdaQuery().eq(QuestionKnowledge::getQuestionId, id));
        saveKnowledgePoints(id, dto.getKnowledgePointIds());

        if (isProgramming(dto.getQuestionType())) {
            programmingQuestionMapper.deleteById(id);
            if (dto.getProgramming() != null) {
                saveProgramming(id, dto.getProgramming());
            }
        } else {
            programmingQuestionMapper.deleteById(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (questionMapper.selectById(id) == null) {
            throw new BusinessException("题目不存在");
        }
        if (questionMapper.countPaperQuestionByQuestionId(id) > 0) {
            throw new BusinessException("该题目已被试卷引用，无法删除");
        }
        questionMapper.deleteById(id);
        questionKnowledgeMapper.delete(
                Wrappers.<QuestionKnowledge>lambdaQuery().eq(QuestionKnowledge::getQuestionId, id));
        programmingQuestionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BusinessException("题目不存在");
        }
        q.setStatus(status);
        questionMapper.updateById(q);
    }

    // ---- 私有方法 ----

    private boolean isProgramming(Integer type) {
        return QuestionType.PROGRAMMING.getCode().equals(type);
    }

    private void applyDTO(Question q, QuestionDTO dto) {
        q.setSubjectId(dto.getSubjectId());
        q.setQuestionType(dto.getQuestionType());
        q.setContent(dto.getContent());
        q.setOptions(dto.getOptions() == null ? null : JsonUtil.toJson(dto.getOptions()));
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty());
        q.setDefaultScore(dto.getDefaultScore());
        q.setJudgeMode(dto.getJudgeMode());
    }

    private void saveKnowledgePoints(Long questionId, List<Long> kpIds) {
        if (kpIds == null || kpIds.isEmpty()) {
            return;
        }
        for (Long kpId : kpIds) {
            QuestionKnowledge qk = new QuestionKnowledge();
            qk.setQuestionId(questionId);
            qk.setKnowledgePointId(kpId);
            questionKnowledgeMapper.insert(qk);
        }
    }

    private void saveProgramming(Long questionId, ProgrammingQuestionDTO dto) {
        ProgrammingQuestion pq = new ProgrammingQuestion();
        pq.setQuestionId(questionId);
        pq.setLanguages(dto.getLanguages());
        pq.setTimeLimit(dto.getTimeLimit());
        pq.setMemoryLimit(dto.getMemoryLimit());
        pq.setTestCases(dto.getTestCases() == null ? null : JsonUtil.toJson(dto.getTestCases()));
        programmingQuestionMapper.insert(pq);
    }

    private void validate(QuestionDTO dto) {
        if (subjectMapper.selectById(dto.getSubjectId()) == null) {
            throw new BusinessException("学科不存在");
        }
        Integer type = dto.getQuestionType();
        if (type == null) {
            throw new BusinessException("题型不能为空");
        }
        switch (type) {
            case 1:
            case 2:
                if (dto.getOptions() == null || dto.getOptions().size() < 2) {
                    throw new BusinessException("选择题至少需要 2 个选项");
                }
                if (!StringUtils.hasText(dto.getAnswer())) {
                    throw new BusinessException("答案不能为空");
                }
                break;
            case 3:
                if (!"T".equals(dto.getAnswer()) && !"F".equals(dto.getAnswer())) {
                    throw new BusinessException("判断题答案只能是 T 或 F");
                }
                break;
            case 4:
                if (!StringUtils.hasText(dto.getAnswer())) {
                    throw new BusinessException("填空题答案不能为空");
                }
                break;
            case 5:
            case 6:
                break;
            case 7:
                if (dto.getProgramming() == null || dto.getProgramming().getTestCases() == null
                        || dto.getProgramming().getTestCases().isEmpty()) {
                    throw new BusinessException("编程题至少需要 1 组测试用例");
                }
                break;
            default:
                throw new BusinessException("题型不合法");
        }
    }

    private QuestionListVO toListVO(Question q, Map<Long, String> subjectNameMap) {
        QuestionListVO vo = new QuestionListVO();
        vo.setId(q.getId());
        vo.setSubjectId(q.getSubjectId());
        vo.setSubjectName(subjectNameMap.get(q.getSubjectId()));
        vo.setQuestionType(q.getQuestionType());
        vo.setQuestionTypeName(QuestionType.nameOf(q.getQuestionType()));
        vo.setContent(q.getContent());
        vo.setDifficulty(q.getDifficulty());
        vo.setDefaultScore(q.getDefaultScore());
        vo.setStatus(q.getStatus());
        vo.setCreateTime(q.getCreateTime());
        return vo;
    }

    private QuestionVO toVO(Question q) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setSubjectId(q.getSubjectId());
        vo.setQuestionType(q.getQuestionType());
        vo.setQuestionTypeName(QuestionType.nameOf(q.getQuestionType()));
        vo.setContent(q.getContent());
        vo.setOptions(q.getOptions() == null ? null : JsonUtil.fromJsonList(q.getOptions(), OptionDTO.class));
        vo.setAnswer(q.getAnswer());
        vo.setAnalysis(q.getAnalysis());
        vo.setDifficulty(q.getDifficulty());
        vo.setDefaultScore(q.getDefaultScore());
        vo.setJudgeMode(q.getJudgeMode());
        vo.setStatus(q.getStatus());
        return vo;
    }

    private ProgrammingQuestionDTO toProgrammingVO(ProgrammingQuestion pq) {
        ProgrammingQuestionDTO dto = new ProgrammingQuestionDTO();
        dto.setLanguages(pq.getLanguages());
        dto.setTimeLimit(pq.getTimeLimit());
        dto.setMemoryLimit(pq.getMemoryLimit());
        dto.setTestCases(pq.getTestCases() == null ? null : JsonUtil.fromJsonList(pq.getTestCases(), TestCaseDTO.class));
        return dto;
    }

    @Override
    public List<QuestionExcel> listForExport(QuestionQueryDTO query) {
        LambdaQueryWrapper<Question> wrapper = Wrappers.<Question>lambdaQuery()
                .eq(query.getSubjectId() != null, Question::getSubjectId, query.getSubjectId())
                .eq(query.getQuestionType() != null, Question::getQuestionType, query.getQuestionType())
                .eq(query.getDifficulty() != null, Question::getDifficulty, query.getDifficulty())
                .like(StringUtils.hasText(query.getKeyword()), Question::getContent, query.getKeyword())
                .orderByDesc(Question::getId);
        return questionMapper.selectList(wrapper).stream().map(this::toExcel).collect(Collectors.toList());
    }

    @Override
    public ImportResultVO importExcel(MultipartFile file, Long subjectId) {
        if (subjectMapper.selectById(subjectId) == null) {
            throw new BusinessException("学科不存在");
        }
        List<QuestionExcel> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(QuestionExcel.class).sheet().doReadSync();
        } catch (Exception e) {
            throw new BusinessException("Excel 解析失败: " + e.getMessage());
        }

        ImportResultVO result = new ImportResultVO();
        result.setTotal(rows.size());
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            int excelRow = i + 2; // 第 1 行是表头, 数据从第 2 行开始
            try {
                Question q = convert(rows.get(i), subjectId);
                if (q == null) {
                    errors.add("第 " + excelRow + " 行: 题型无法识别");
                    continue;
                }
                q.setCreatorId(StpUtil.getLoginIdAsLong());
                q.setStatus(1);
                questionMapper.insert(q);
                success++;
            } catch (Exception e) {
                errors.add("第 " + excelRow + " 行: " + e.getMessage());
            }
        }
        result.setSuccessCount(success);
        result.setFailCount(rows.size() - success);
        result.setErrors(errors);
        return result;
    }

    private Question convert(QuestionExcel row, Long subjectId) {
        Integer type = typeCode(row.getQuestionType());
        if (type == null) {
            return null;
        }
        if (!StringUtils.hasText(row.getContent())) {
            throw new BusinessException("题干为空");
        }
        Question q = new Question();
        q.setSubjectId(subjectId);
        q.setQuestionType(type);
        q.setContent(row.getContent());
        q.setDifficulty(row.getDifficulty());
        q.setDefaultScore(row.getDefaultScore());
        q.setAnalysis(row.getAnalysis());
        switch (type) {
            case 1:
            case 2:
                List<OptionDTO> options = buildOptions(row);
                if (options.size() < 2) {
                    throw new BusinessException("选择题至少需要 2 个选项");
                }
                q.setOptions(JsonUtil.toJson(options));
                if (!StringUtils.hasText(row.getAnswer())) {
                    throw new BusinessException("答案为空");
                }
                q.setAnswer(row.getAnswer());
                break;
            case 3:
                String a = row.getAnswer();
                if ("对".equals(a)) a = "T";
                if ("错".equals(a)) a = "F";
                if (!"T".equals(a) && !"F".equals(a)) {
                    throw new BusinessException("判断题答案应为 T 或 F");
                }
                q.setAnswer(a);
                break;
            case 4:
                if (!StringUtils.hasText(row.getAnswer())) {
                    throw new BusinessException("填空题答案为空");
                }
                q.setAnswer(row.getAnswer());
                break;
            case 5:
                q.setAnswer(row.getAnswer());
                break;
            default:
                return null;
        }
        return q;
    }

    private Integer typeCode(String name) {
        switch (name) {
            case "单选题": return 1;
            case "多选题": return 2;
            case "判断题": return 3;
            case "填空题": return 4;
            case "简答题": return 5;
            default: return null;
        }
    }

    private List<OptionDTO> buildOptions(QuestionExcel row) {
        List<OptionDTO> list = new ArrayList<>();
        addOption(list, "A", row.getOptionA());
        addOption(list, "B", row.getOptionB());
        addOption(list, "C", row.getOptionC());
        addOption(list, "D", row.getOptionD());
        return list;
    }

    private void addOption(List<OptionDTO> list, String key, String text) {
        if (StringUtils.hasText(text)) {
            OptionDTO o = new OptionDTO();
            o.setKey(key);
            o.setText(text);
            list.add(o);
        }
    }

    private QuestionExcel toExcel(Question q) {
        QuestionExcel e = new QuestionExcel();
        e.setQuestionType(QuestionType.nameOf(q.getQuestionType()));
        e.setContent(q.getContent());
        List<OptionDTO> options = q.getOptions() == null ? null : JsonUtil.fromJsonList(q.getOptions(), OptionDTO.class);
        e.setOptionA(optionByKey(options, "A"));
        e.setOptionB(optionByKey(options, "B"));
        e.setOptionC(optionByKey(options, "C"));
        e.setOptionD(optionByKey(options, "D"));
        e.setAnswer(q.getAnswer());
        e.setDifficulty(q.getDifficulty());
        e.setDefaultScore(q.getDefaultScore());
        e.setAnalysis(q.getAnalysis());
        return e;
    }

    private String optionByKey(List<OptionDTO> options, String key) {
        if (options == null) {
            return null;
        }
        return options.stream().filter(o -> key.equals(o.getKey()))
                .map(OptionDTO::getText).findFirst().orElse(null);
    }
}
