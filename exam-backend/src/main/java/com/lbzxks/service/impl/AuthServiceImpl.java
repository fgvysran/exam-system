package com.lbzxks.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.LoginDTO;
import com.lbzxks.entity.SysRole;
import com.lbzxks.entity.SysUser;
import com.lbzxks.mapper.SysRoleMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.service.AuthService;
import com.lbzxks.vo.LoginVO;
import com.lbzxks.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));

        if (user == null || !BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // Sa-Token 登录, 下发 token
        StpUtil.login(user.getId());

        List<String> roles = roleMapper.selectRolesByUserId(user.getId())
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setTokenName(StpUtil.getTokenName());
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoles(roles);
        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        List<String> roles = roleMapper.selectRolesByUserId(userId)
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRoles(roles);
        return vo;
    }
}
