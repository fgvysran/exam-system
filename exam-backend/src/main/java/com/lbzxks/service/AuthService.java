package com.lbzxks.service;

import com.lbzxks.dto.LoginDTO;
import com.lbzxks.dto.ProfileDTO;
import com.lbzxks.vo.LoginVO;
import com.lbzxks.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void logout();

    UserInfoVO getCurrentUser();

    UserInfoVO updateProfile(ProfileDTO dto);
}
