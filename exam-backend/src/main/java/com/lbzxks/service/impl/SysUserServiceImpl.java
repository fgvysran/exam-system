package com.lbzxks.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.UserDTO;
import com.lbzxks.dto.UserQueryDTO;
import com.lbzxks.entity.ExamRecord;
import com.lbzxks.entity.SysRole;
import com.lbzxks.entity.SysUser;
import com.lbzxks.entity.SysUserRole;
import com.lbzxks.mapper.ExamRecordMapper;
import com.lbzxks.mapper.SysRoleMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.mapper.SysUserRoleMapper;
import com.lbzxks.service.SysUserService;
import com.lbzxks.vo.UserListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ExamRecordMapper examRecordMapper;

    @Override
    public PageResult<UserListVO> page(UserQueryDTO query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        IPage<UserListVO> page = sysUserMapper.selectUserPage(
                new Page<>(pageNum, pageSize), query.getRole(), query.getClassId(), query.getKeyword());
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    @Override
    public void add(UserDTO dto) {
        long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        String roleCode = StringUtils.hasText(dto.getRole()) ? dto.getRole() : "STUDENT";
        if (!"STUDENT".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            throw new BusinessException("只能创建学生或教师账号");
        }
        SysRole role = sysRoleMapper.selectOne(
                Wrappers.<SysRole>lambdaQuery().eq(SysRole::getRoleCode, roleCode));
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setRealName(dto.getRealName());
        user.setClassId(dto.getClassId());
        user.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        user.setStatus(1);
        sysUserMapper.insert(user);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        sysUserRoleMapper.insert(ur);
    }

    @Override
    public void update(Long id, UserDTO dto) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        long count = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(SysUser::getUsername, dto.getUsername())
                        .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setClassId(dto.getClassId());
        user.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        user.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        }
        sysUserMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        if (sysUserMapper.selectById(id) == null) {
            throw new BusinessException("用户不存在");
        }
        long recordCount = examRecordMapper.selectCount(
                Wrappers.<ExamRecord>lambdaQuery().eq(ExamRecord::getUserId, id));
        if (recordCount > 0) {
            throw new BusinessException("该用户已有作答记录，无法删除");
        }
        sysUserMapper.deleteById(id);
        sysUserRoleMapper.delete(
                Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, id));
    }
}
