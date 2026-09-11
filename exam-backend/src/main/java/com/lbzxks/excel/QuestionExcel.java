package com.lbzxks.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 题目导入导出 Excel 模型 (仅客观题: 单选/多选/判断/填空/简答)
 * 编程题测试用例结构复杂, 建议手动录入, 不参与 Excel 导入
 */
@Data
public class QuestionExcel {

    @ExcelProperty("题型")
    private String questionType;

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("难度")
    private Integer difficulty;

    @ExcelProperty("分值")
    private BigDecimal defaultScore;

    @ExcelProperty("解析")
    private String analysis;
}
