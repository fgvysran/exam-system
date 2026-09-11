package com.lbzxks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 班级新增/修改入参
 */
@Data
public class SysClassDTO {

    @NotBlank(message = "班级名称不能为空")
    private String className;

    private String grade;

    private String major;
}
