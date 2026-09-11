package com.lbzxks.service;

import com.lbzxks.dto.SubjectDTO;
import com.lbzxks.vo.SubjectVO;

import java.util.List;

public interface SubjectService {

    List<SubjectVO> list();

    void add(SubjectDTO dto);

    void update(Long id, SubjectDTO dto);

    void delete(Long id);
}
