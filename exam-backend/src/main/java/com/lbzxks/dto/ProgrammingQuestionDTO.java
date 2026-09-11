package com.lbzxks.dto;

import lombok.Data;

import java.util.List;

/**
 * 编程题扩展信息
 */
@Data
public class ProgrammingQuestionDTO {

    private String languages;

    private Integer timeLimit;

    private Integer memoryLimit;

    private List<TestCaseDTO> testCases;
}
