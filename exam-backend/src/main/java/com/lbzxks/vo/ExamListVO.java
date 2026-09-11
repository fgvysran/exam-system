package com.lbzxks.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试列表项出参
 */
@Data
public class ExamListVO {

    private Long id;

    private String name;

    private Long paperId;

    private String paperName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Integer duration;

    /** 动态计算: 1未开始 2进行中 3已结束 */
    private Integer status;
}
