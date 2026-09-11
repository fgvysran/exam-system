package com.lbzxks.service;

import com.lbzxks.common.PageResult;
import com.lbzxks.dto.PaperDTO;
import com.lbzxks.dto.PaperQueryDTO;
import com.lbzxks.dto.PaperQuestionDTO;
import com.lbzxks.vo.PaperDetailVO;
import com.lbzxks.vo.PaperListVO;

import java.util.List;

public interface PaperService {

    PageResult<PaperListVO> page(PaperQueryDTO query);

    PaperDetailVO detail(Long id);

    void add(PaperDTO dto);

    void update(Long id, PaperDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void saveQuestions(Long id, List<PaperQuestionDTO> questions);
}
