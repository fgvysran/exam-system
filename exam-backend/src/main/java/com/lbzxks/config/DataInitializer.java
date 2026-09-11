package com.lbzxks.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.entity.SysRole;
import com.lbzxks.entity.SysUser;
import com.lbzxks.entity.SysUserRole;
import com.lbzxks.mapper.SysRoleMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时初始化: 确保默认管理员账号 admin/123456 存在
 * 密码用 BCrypt 在运行时生成, 避免在 SQL 里硬编码哈希
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public void run(ApplicationArguments args) {
        SysUser admin = userMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, "admin"));

        if (admin == null) {
            admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
            admin.setRealName("系统管理员");
            admin.setStatus(1);
            userMapper.insert(admin);

            SysRole adminRole = roleMapper.selectOne(
                    Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, "ADMIN"));
            if (adminRole != null) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(admin.getId());
                ur.setRoleId(adminRole.getId());
                userRoleMapper.insert(ur);
            }
            log.info("已初始化默认管理员账号: admin / 123456");
        }
    }
}
