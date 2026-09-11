package com.lbzxks.vo;

import lombok.Data;

import java.util.List;

/**
 * Excel 批量导入结果
 */
@Data
public class ImportResultVO {

    private int total;

    private int successCount;

    private int failCount;

    private List<String> errors;
}
