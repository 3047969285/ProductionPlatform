package com.devflow.service.impl;

import com.devflow.common.BeanConvert;
import com.devflow.common.HashUtil;
import com.devflow.model.dto.PasswordDto;
import com.devflow.model.dto.UserDto;
import com.devflow.mapper.UserMapper;
import com.devflow.model.entity.User;
import com.devflow.service.UserService;
import com.devflow.model.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;

    /**
     * 查询全部用户
     *
     * @return 用户视图列表
     */
    @Override
    public List<UserVo> list() {
        // 查库后转成 VO，自动过滤密码字段
        return BeanConvert.toUserVoList(mapper.findAll());
    }

    /**
     * 新增用户
     *
     * @param dto 用户信息含明文密码
     * @return 是否成功
     */
    @Override
    public boolean add(UserDto dto) {
        // DTO 转实体
        User user = BeanConvert.toModel(dto);
        user.setPassword(HashUtil.sha256(user.getPassword())); // 明文密码哈希后入库
        user.setCreatedAt(LocalDateTime.now());
        // 未指定角色时默认开发者
        if (user.getRole() == null) {
            user.setRole("developer");
        }
        return mapper.insert(user) > 0;
    }

    /**
     * 更新用户昵称与角色
     *
     * @param dto 用户信息
     * @return 是否成功
     */
    @Override
    public boolean update(UserDto dto) {
        return mapper.update(BeanConvert.toModel(dto)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param dto 用户编号与新明文密码
     * @return 是否成功
     */
    @Override
    public boolean resetPassword(PasswordDto dto) {
        // 只更新密码字段，哈希后写入
        return mapper.updatePassword(dto.getId(), HashUtil.sha256(dto.getPassword())) > 0;
    }

    /**
     * 删除用户
     *
     * @param id 用户编号
     * @return 是否成功
     */
    @Override
    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
