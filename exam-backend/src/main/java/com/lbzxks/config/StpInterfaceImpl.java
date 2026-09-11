package com.lbzxks.config;

import cn.dev33.satoken.stp.StpInterface;
import com.lbzxks.entity.SysRole;
import com.lbzxks.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限数据源: 提供用户的角色/权限列表
 * 使得 StpUtil.checkRole("TEACHER") / @SaCheckRole 能生效
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本系统暂不做细粒度权限, 返回空
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long userId = Long.parseLong(loginId.toString());
        return roleMapper.selectRolesByUserId(userId)
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }
}
