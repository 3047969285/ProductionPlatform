package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.auth.TokenStore;
import com.example.productionplatform.admin.mapper.UserMapper;
import com.example.productionplatform.admin.model.SysUser;
import com.example.productionplatform.admin.common.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final TokenStore tokenStore;

    public Optional<Map<String, Object>> login(String username, String password) {
        SysUser user = userMapper.findByUsername(username);
        if (user == null || !HashUtil.sha256(password).equals(user.getPassword())) {
            return Optional.empty();
        }
        String token = tokenStore.create(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", toPublic(user));
        return Optional.of(result);
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    public SysUser getUserByToken(String token) {
        Long userId = tokenStore.getUserId(token);
        return userId == null ? null : userMapper.findById(userId);
    }

    public SysUser toPublic(SysUser user) {
        if (user == null) return null;
        SysUser pub = new SysUser();
        pub.setId(user.getId());
        pub.setUsername(user.getUsername());
        pub.setNickname(user.getNickname());
        pub.setRole(user.getRole());
        pub.setCreateTime(user.getCreateTime());
        return pub;
    }
}
