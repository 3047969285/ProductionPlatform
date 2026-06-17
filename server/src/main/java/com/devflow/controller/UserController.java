package com.devflow.controller;

import com.devflow.common.ApiResult;
import com.devflow.common.exception.BizAssert;
import com.devflow.common.validation.ValidGroups;
import com.devflow.model.dto.PasswordDto;
import com.devflow.model.dto.UserDto;
import com.devflow.service.UserService;
import com.devflow.model.vo.UserVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 查询用户列表
     *
     * @return 用户视图列表
     */
    @GetMapping
    public ApiResult<List<UserVo>> list() {
        return ApiResult.ok(userService.list());
    }

    /**
     * 新增用户
     *
     * @param dto 用户信息含明文密码
     * @return 操作结果
     */
    @PostMapping
    public ApiResult<Void> add(@Validated(ValidGroups.Create.class) @RequestBody UserDto dto) {
        // 注解已校验用户名密码，这里只判断入库是否成功
        BizAssert.isTrue(userService.add(dto), "新增失败");
        return ApiResult.ok();
    }

    /**
     * 更新用户昵称与角色
     *
     * @param dto 用户信息须包含用户编号
     * @return 操作结果
     */
    @PutMapping
    public ApiResult<Void> update(@Validated(ValidGroups.Update.class) @RequestBody UserDto dto) {
        BizAssert.isTrue(userService.update(dto), "更新失败");
        return ApiResult.ok();
    }

    /**
     * 重置用户密码
     *
     * @param dto 用户编号与新明文密码
     * @return 操作结果
     */
    @PutMapping("/password")
    public ApiResult<Void> resetPassword(@Valid @RequestBody PasswordDto dto) {
        BizAssert.isTrue(userService.resetPassword(dto), "重置失败");
        return ApiResult.ok();
    }

    /**
     * 删除用户
     *
     * @param id 用户编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        BizAssert.isTrue(userService.delete(id), "删除失败");
        return ApiResult.ok();
    }
}
