package com.lbzxks.service;

import com.lbzxks.common.PageResult;
import com.lbzxks.dto.QuestionDTO;
import com.lbzxks.dto.QuestionQueryDTO;
import com.lbzxks.excel.QuestionExcel;
import com.lbzxks.vo.ImportResultVO;
import com.lbzxks.vo.QuestionListVO;
import com.lbzxks.vo.QuestionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionService {

    PageResult<QuestionListVO> page(QuestionQueryDTO query);

    QuestionVO detail(Long id);

    void add(QuestionDTO dto);

    void update(Long id, QuestionDTO dto);

    void delete(Long id);

    void batchDelete(List<Long> ids);

    void updateStatus(Long id, Integer status);

    List<QuestionExcel> listForExport(QuestionQueryDTO query);

    ImportResultVO importExcel(MultipartFile file, Long subjectId);
}
