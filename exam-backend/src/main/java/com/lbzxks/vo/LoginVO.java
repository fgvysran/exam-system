package com.lbzxks.vo;

import lombok.Data;

import java.util.List;

/**
 * 登录出参
 */
@Data
public class LoginVO {

    private String token;

    private String tokenName;

    private Long userId;

    private String username;

    private String realName;

    private List<String> roles;
}
