package com.lbzxks.common.constant;

/**
 * 判分状态枚举 (与 answer_detail.judge_status 对应)
 */
public enum JudgeStatus {

    PENDING(0, "待判"),
    JUDGING(1, "判分中"),
    JUDGED(2, "已判");

    private final Integer code;
    private final String name;

    JudgeStatus(Integer code, String name) {
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
