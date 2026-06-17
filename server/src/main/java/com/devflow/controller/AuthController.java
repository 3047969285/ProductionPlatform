package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizException;
import com.devflow.model.dto.LoginDto;
import com.devflow.service.AuthService;
import com.devflow.model.vo.LoginVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param dto 登录凭证包含用户名与密码
     * @return 成功返回令牌及用户信息
     */
    @PostMapping("/login")
    public ApiResult<LoginVo> login(@Valid @RequestBody LoginDto dto) {
        // 校验通过后调服务层，失败抛业务异常
        LoginVo vo = authService.login(dto).orElseThrow(() -> new BizException("用户名或密码错误"));
        return ApiResult.ok(vo);
    }

    /**
     * 用户登出
     *
     * @param auth 请求头中的 Bearer 令牌可为空
     * @return 操作结果
     */
    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        // 有 Bearer 令牌才执行登出，未登录也返回成功
        if (auth != null && auth.startsWith("Bearer ")) {
            authService.logout(auth.substring(7)); // 去掉 "Bearer " 前缀取纯令牌
        }
        return ApiResult.ok();
    }
}
