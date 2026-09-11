package com.lbzxks.service;

import com.lbzxks.dto.KnowledgePointDTO;
import com.lbzxks.vo.KnowledgePointVO;

import java.util.List;

public interface KnowledgePointService {

    List<KnowledgePointVO> tree(Long subjectId);

    void add(KnowledgePointDTO dto);

    void update(Long id, KnowledgePointDTO dto);

    void delete(Long id);
}
