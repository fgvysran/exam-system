package com.lbzxks.service;

import com.lbzxks.dto.AnswerDTO;
import com.lbzxks.vo.ExamPaperVO;
import com.lbzxks.vo.ExamResultVO;

import java.util.List;

public interface AnswerService {

    ExamPaperVO start(Long examId);

    void saveAnswers(Long examId, List<AnswerDTO> answers);

    ExamResultVO submit(Long examId, List<AnswerDTO> answers);

    ExamResultVO result(Long examId);
}
