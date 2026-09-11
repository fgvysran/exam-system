package com.lbzxks.service;

import com.lbzxks.common.PageResult;
import com.lbzxks.dto.GradeDTO;
import com.lbzxks.vo.GradingItemVO;

public interface GradingService {

    PageResult<GradingItemVO> pending(Long examId, Integer pageNum, Integer pageSize);

    void grade(Long detailId, GradeDTO dto);
}
