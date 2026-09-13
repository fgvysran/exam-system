package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lbzxks.entity.SysUser;
import com.lbzxks.vo.UserListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 用户列表(学生/教师), 联表带出班级名和角色
     */
    @Select("SELECT u.id, u.username, u.real_name, u.class_id, c.class_name, r.role_code, u.status, u.create_time, u.email, u.phone " +
            "FROM sys_user u " +
            "LEFT JOIN sys_class c ON u.class_id = c.id " +
            "JOIN sys_user_role ur ON u.id = ur.user_id " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE r.role_code IN ('STUDENT', 'TEACHER') " +
            "AND (#{role} IS NULL OR r.role_code = #{role}) " +
            "AND (#{classId} IS NULL OR u.class_id = #{classId}) " +
            "AND (#{keyword} IS NULL OR u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "     OR u.real_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY u.id DESC")
    IPage<UserListVO> selectUserPage(IPage<UserListVO> page,
                                     @Param("role") String role,
                                     @Param("classId") Long classId,
                                     @Param("keyword") String keyword);
}
