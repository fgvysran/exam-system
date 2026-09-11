package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 考试-班级关联表 (exam_class)
 */
@Data
@TableName("exam_class")
public class ExamClass {

    private Long examId;

    private Long classId;
}
