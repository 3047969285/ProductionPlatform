package com.devflow.service.impl;

import com.devflow.auth.TokenStore;
import com.devflow.common.BeanConvert;
import com.devflow.common.HashUtil;
import com.devflow.model.dto.LoginDto;
import com.devflow.mapper.UserMapper;
import com.devflow.model.entity.User;
import com.devflow.service.AuthService;
import com.devflow.model.vo.LoginVo;
import com.devflow.model.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final TokenStore tokenStore;

    /**
     * 用户登录校验账号密码并签发令牌
     *
     * @param dto 登录凭证
     * @return 成功返回登录视图失败返回空
     */
    @Override
    public Optional<LoginVo> login(LoginDto dto) {
        // 按用户名查库
        User user = userMapper.findByUsername(dto.getUsername());
        // 用户不存在或密码哈希不匹配则登录失败
        if (user == null || !HashUtil.sha256(dto.getPassword()).equals(user.getPassword())) {
            return Optional.empty();
        }

        // 组装登录结果：令牌 + 用户信息（不含密码）
        LoginVo vo = new LoginVo();
        vo.setToken(tokenStore.create(user.getId())); // 签发令牌并绑定用户
        vo.setUser(BeanConvert.toVo(user));
        return Optional.of(vo);
    }

    /**
     * 用户登出销毁令牌
     *
     * @param token 访问令牌
     */
    @Override
    public void logout(String token) {
        // 从内存中移除令牌，使其立即失效
        tokenStore.remove(token);
    }

    /**
     * 根据令牌获取当前登录用户
     *
     * @param token 访问令牌
     * @return 用户视图未登录返回空
     */
    @Override
    public UserVo getUser(String token) {
        // 令牌无效则未登录
        Long id = tokenStore.getUserId(token);
        if (id == null) {
            return null;
        }
        // 查用户并转成前端视图
        return BeanConvert.toVo(userMapper.findById(id));
    }
}
