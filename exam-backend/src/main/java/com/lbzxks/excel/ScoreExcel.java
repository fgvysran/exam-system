package com.lbzxks.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩导出 Excel 模型
 */
@Data
public class ScoreExcel {

    @ExcelProperty("排名")
    private Integer rank;

    @ExcelProperty("学号")
    private String username;

    @ExcelProperty("姓名")
    private String studentName;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("客观分")
    private BigDecimal objectiveScore;

    @ExcelProperty("主观分")
    private BigDecimal subjectiveScore;

    @ExcelProperty("总分")
    private BigDecimal totalScore;
}
