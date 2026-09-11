package com.lbzxks.dto;

import lombok.Data;

/**
 * 用户列表查询条件
 */
@Data
public class UserQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private Long classId;

    private String keyword;

    /** 角色筛选: STUDENT / TEACHER, 空=全部 */
    private String role;
}
