package com.lbzxks.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试安排新增/修改入参
 */
@Data
public class ExamDTO {

    @NotBlank(message = "考试名称不能为空")
    private String name;

    @NotNull(message = "试卷不能为空")
    private Long paperId;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 作答时长(分钟), 为空则取试卷时长 */
    private Integer duration;

    /** 参加考试的班级 */
    private List<Long> classIds;
}
