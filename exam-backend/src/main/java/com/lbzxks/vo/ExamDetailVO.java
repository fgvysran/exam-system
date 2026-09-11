package com.lbzxks.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试详情出参
 */
@Data
public class ExamDetailVO {

    private Long id;

    private String name;

    private Long paperId;

    private String paperName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer duration;

    private Integer status;

    private List<ClassVO> classes;
}
