package com.lbzxks.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.SubjectDTO;
import com.lbzxks.entity.Subject;
import com.lbzxks.mapper.SubjectMapper;
import com.lbzxks.service.SubjectService;
import com.lbzxks.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;

    @Override
    public List<SubjectVO> list() {
        return subjectMapper.selectWithQuestionCount();
    }

    @Override
    public void add(SubjectDTO dto) {
        long count = subjectMapper.selectCount(
                Wrappers.<Subject>lambdaQuery().eq(Subject::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException("学科名称已存在");
        }

        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setCode(dto.getCode());
        subjectMapper.insert(subject);
    }

    @Override
    public void update(Long id, SubjectDTO dto) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException("学科不存在");
        }

        long count = subjectMapper.selectCount(
                Wrappers.<Subject>lambdaQuery()
                        .eq(Subject::getName, dto.getName())
                        .ne(Subject::getId, id));
        if (count > 0) {
            throw new BusinessException("学科名称已存在");
        }

        subject.setName(dto.getName());
        subject.setCode(dto.getCode());
        subjectMapper.updateById(subject);
    }

    @Override
    public void delete(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException("学科不存在");
        }
        if (subjectMapper.countQuestionsBySubjectId(id) > 0) {
            throw new BusinessException("该学科下存在题目，无法删除");
        }
        subjectMapper.deleteById(id);
    }
}
