package com.lbzxks.common.constant;

/**
 * 题型枚举 (与 question.question_type 对应)
 */
public enum QuestionType {

    SINGLE_CHOICE(1, "单选题"),
    MULTIPLE_CHOICE(2, "多选题"),
    JUDGE(3, "判断题"),
    FILL_BLANK(4, "填空题"),
    SHORT_ANSWER(5, "简答题"),
    ESSAY(6, "论述题"),
    PROGRAMMING(7, "编程题");

    private final Integer code;
    private final String name;

    QuestionType(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据 code 反查题型名称
     */
    public static String nameOf(Integer code) {
        for (QuestionType t : values()) {
            if (t.getCode().equals(code)) {
                return t.getName();
            }
        }
        return "";
    }
}
