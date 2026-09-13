package com.lbzxks.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户(学生)新增/修改入参
 */
@Data
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** 密码: 新增时必填, 修改时留空则不修改 */
    private String password;

    private Long classId;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 角色编码: STUDENT / TEACHER */
    private String role = "STUDENT";
}
