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

    /**
     * 对某场考试所有已交卷记录重新执行自动判分（保留人工已判分数）
     *
     * @return 重新判分的记录数
     */
    int regrade(Long examId);
}
