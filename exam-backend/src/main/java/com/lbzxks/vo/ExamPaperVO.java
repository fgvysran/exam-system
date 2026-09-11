package com.lbzxks.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生开始考试后拿到的试卷(不含答案)
 */
@Data
public class ExamPaperVO {

    private Long examId;

    private String examName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer duration;

    private Long recordId;

    private List<ExamQuestionVO> questions;
}
