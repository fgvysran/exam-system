package com.lbzxks.common.constant;

/**
 * 考试状态枚举 (与 exam.status 对应)
 */
public enum ExamStatus {

    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    FINISHED(3, "已结束");

    private final Integer code;
    private final String name;

    ExamStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
