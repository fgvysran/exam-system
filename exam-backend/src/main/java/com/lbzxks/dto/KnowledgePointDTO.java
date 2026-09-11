package com.lbzxks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识点新增/修改入参
 */
@Data
public class KnowledgePointDTO {

    @NotNull(message = "学科不能为空")
    private Long subjectId;

    @NotBlank(message = "知识点名称不能为空")
    private String name;

    /** 父知识点ID, 空或 0 表示根节点 */
    private Long parentId;
}
