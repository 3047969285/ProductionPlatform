package com.example.productionplatform.admin.service;

import com.example.productionplatform.admin.mapper.UserMapper;
import com.example.productionplatform.admin.model.SysUser;
import com.example.productionplatform.admin.common.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public List<SysUser> listAll() {
        return userMapper.findAll();
    }

    public boolean add(SysUser user) {
        user.setPassword(HashUtil.sha256(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("operator");
        }
        return userMapper.insert(user) > 0;
    }

    public boolean update(SysUser user) {
        return userMapper.update(user) > 0;
    }

    public boolean resetPassword(Long id, String password) {
        return userMapper.updatePassword(id, HashUtil.sha256(password)) > 0;
    }

    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }
}
