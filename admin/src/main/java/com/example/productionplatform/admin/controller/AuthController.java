package com.example.productionplatform.admin.controller;

import com.example.productionplatform.admin.common.ApiResult;
import com.example.productionplatform.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ApiResult.badRequest("username and password required");
        }
        return authService.login(username, password)
                .map(ApiResult::ok)
                .orElse(ApiResult.fail("invalid credentials"));
    }

    @GetMapping("/me")
    public ApiResult<?> me(@RequestHeader("Authorization") String auth) {
        String token = auth != null && auth.startsWith("Bearer ") ? auth.substring(7) : null;
        var user = authService.getUserByToken(token);
        return user != null ? ApiResult.ok(authService.toPublic(user)) : ApiResult.unauthorized("unauthorized");
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            authService.logout(auth.substring(7));
        }
        return ApiResult.ok();
    }
}
