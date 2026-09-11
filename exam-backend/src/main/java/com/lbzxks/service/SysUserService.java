package com.lbzxks.service;

import com.lbzxks.common.PageResult;
import com.lbzxks.dto.UserDTO;
import com.lbzxks.dto.UserQueryDTO;
import com.lbzxks.vo.UserListVO;

public interface SysUserService {

    PageResult<UserListVO> page(UserQueryDTO query);

    void add(UserDTO dto);

    void update(Long id, UserDTO dto);

    void delete(Long id);
}
