package com.lbzxks.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 学科新增/修改入参
 */
@Data
public class SubjectDTO {

    @NotBlank(message = "学科名称不能为空")
    private String name;

    private String code;
}
