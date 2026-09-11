package com.lbzxks.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户列表项出参
 */
@Data
public class UserListVO {

    private Long id;

    private String username;

    private String realName;

    private Long classId;

    private String className;

    private String roleCode;

    private Integer status;

    private LocalDateTime createTime;
}
