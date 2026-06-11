package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.common.ApiResult;
import com.example.productionplatform.admin.model.SysUser;
import com.example.productionplatform.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public ApiResult<List<SysUser>> list() {
        return ApiResult.ok(userService.listAll());
    }

    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody SysUser user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) return ApiResult.badRequest("username required");
        if (user.getPassword() == null || user.getPassword().isBlank()) return ApiResult.badRequest("password required");
        return userService.add(user) ? ApiResult.ok() : ApiResult.fail("add failed");
    }

    @PutMapping("/update")
    public ApiResult<Void> update(@RequestBody SysUser user) {
        if (user.getId() == null) return ApiResult.badRequest("id required");
        return userService.update(user) ? ApiResult.ok() : ApiResult.fail("update failed");
    }

    @PutMapping("/password")
    public ApiResult<Void> resetPassword(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String password = (String) body.get("password");
        if (password == null || password.isBlank()) return ApiResult.badRequest("password required");
        return userService.resetPassword(id, password) ? ApiResult.ok() : ApiResult.fail("reset failed");
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return userService.delete(id) ? ApiResult.ok() : ApiResult.fail("delete failed");
    }
}
