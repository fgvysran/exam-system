package com.lbzxks.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前登录用户信息出参
 */
@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private List<String> roles;
}
