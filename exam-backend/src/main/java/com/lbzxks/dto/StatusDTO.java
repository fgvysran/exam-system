package com.lbzxks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用状态修改入参
 */
@Data
public class StatusDTO {

    @NotNull(message = "状态不能为空")
    private Integer status;
}
