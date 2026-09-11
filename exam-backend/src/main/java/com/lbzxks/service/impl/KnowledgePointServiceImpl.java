package com.lbzxks.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.KnowledgePointDTO;
import com.lbzxks.entity.KnowledgePoint;
import com.lbzxks.mapper.KnowledgePointMapper;
import com.lbzxks.mapper.SubjectMapper;
import com.lbzxks.service.KnowledgePointService;
import com.lbzxks.vo.KnowledgePointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final SubjectMapper subjectMapper;

    @Override
    public List<KnowledgePointVO> tree(Long subjectId) {
        List<KnowledgePoint> list = knowledgePointMapper.selectList(
                Wrappers.<KnowledgePoint>lambdaQuery()
                        .eq(subjectId != null, KnowledgePoint::getSubjectId, subjectId)
                        .orderByAsc(KnowledgePoint::getId));

        List<KnowledgePointVO> vos = list.stream().map(this::toVO).collect(Collectors.toList());

        // 按父节点分组, 组装成树
        Map<Long, List<KnowledgePointVO>> childrenMap = vos.stream()
                .collect(Collectors.groupingBy(v ->
                        (v.getParentId() == null || v.getParentId() == 0) ? 0L : v.getParentId()));

        vos.forEach(v -> v.setChildren(childrenMap.getOrDefault(v.getId(), Collections.emptyList())));

        return childrenMap.getOrDefault(0L, Collections.emptyList());
    }

    @Override
    public void add(KnowledgePointDTO dto) {
        checkSubject(dto.getSubjectId());
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (parentId != 0) {
            checkParent(parentId, dto.getSubjectId());
        }

        KnowledgePoint kp = new KnowledgePoint();
        kp.setSubjectId(dto.getSubjectId());
        kp.setName(dto.getName());
        kp.setParentId(parentId);
        knowledgePointMapper.insert(kp);
    }

    @Override
    public void update(Long id, KnowledgePointDTO dto) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp == null) {
            throw new BusinessException("知识点不存在");
        }
        checkSubject(dto.getSubjectId());
        Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
        if (parentId.equals(id)) {
            throw new BusinessException("父知识点不能是自己");
        }
        if (parentId != 0) {
            checkParent(parentId, dto.getSubjectId());
        }

        kp.setSubjectId(dto.getSubjectId());
        kp.setName(dto.getName());
        kp.setParentId(parentId);
        knowledgePointMapper.updateById(kp);
    }

    @Override
    public void delete(Long id) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp == null) {
            throw new BusinessException("知识点不存在");
        }
        long childCount = knowledgePointMapper.selectCount(
                Wrappers.<KnowledgePoint>lambdaQuery().eq(KnowledgePoint::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该知识点下存在子知识点，无法删除");
        }
        if (knowledgePointMapper.countQuestionsByKnowledgePointId(id) > 0) {
            throw new BusinessException("该知识点已被题目引用，无法删除");
        }
        knowledgePointMapper.deleteById(id);
    }

    private void checkSubject(Long subjectId) {
        if (subjectMapper.selectById(subjectId) == null) {
            throw new BusinessException("学科不存在");
        }
    }

    private void checkParent(Long parentId, Long subjectId) {
        KnowledgePoint parent = knowledgePointMapper.selectById(parentId);
        if (parent == null || !parent.getSubjectId().equals(subjectId)) {
            throw new BusinessException("父知识点不存在或不属于该学科");
        }
    }

    private KnowledgePointVO toVO(KnowledgePoint kp) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(kp.getId());
        vo.setName(kp.getName());
        vo.setSubjectId(kp.getSubjectId());
        vo.setParentId(kp.getParentId());
        return vo;
    }
}
