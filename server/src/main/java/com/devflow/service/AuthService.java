package com.devflow.service;

import com.devflow.model.dto.LoginDto;
import com.devflow.model.vo.LoginVo;
import com.devflow.model.vo.UserVo;

import java.util.Optional;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 用户登录校验账号密码并签发令牌
     *
     * @param dto 登录凭证
     * @return 成功返回登录视图失败返回空
     */
    Optional<LoginVo> login(LoginDto dto);

    /**
     * 用户登出销毁令牌
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 根据令牌获取当前登录用户
     *
     * @param token 访问令牌
     * @return 用户视图未登录返回空
     */
    UserVo getUser(String token);
}
