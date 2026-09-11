package com.lbzxks.vo;

import lombok.Data;

import java.util.List;

/**
 * 知识点出参(树形)
 */
@Data
public class KnowledgePointVO {

    private Long id;

    private String name;

    private Long subjectId;

    private Long parentId;

    private List<KnowledgePointVO> children;
}
