package com.lbzxks.service;

import com.lbzxks.common.PageResult;
import com.lbzxks.dto.ExamDTO;
import com.lbzxks.dto.ExamQueryDTO;
import com.lbzxks.vo.ExamDetailVO;
import com.lbzxks.vo.ExamListVO;
import com.lbzxks.vo.MyExamVO;

import java.util.List;

public interface ExamService {

    PageResult<ExamListVO> page(ExamQueryDTO query);

    ExamDetailVO detail(Long id);

    void add(ExamDTO dto);

    void update(Long id, ExamDTO dto);

    void delete(Long id);

    List<MyExamVO> myExams();
}
