package com.lbzxks.vo;

import lombok.Data;

/**
 * 学科出参(含题目数量)
 */
@Data
public class SubjectVO {

    private Long id;

    private String name;

    private String code;

    private Long questionCount;
}
