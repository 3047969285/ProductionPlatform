package com.devflow.service;

import com.devflow.model.dto.PasswordDto;
import com.devflow.model.dto.UserDto;
import com.devflow.model.vo.UserVo;

import java.util.List;

/**
 * 用户管理服务
 */
public interface UserService {

    /**
     * 查询全部用户
     *
     * @return 用户视图列表
     */
    List<UserVo> list();

    /**
     * 新增用户
     *
     * @param dto 用户信息含明文密码
     * @return 是否成功
     */
    boolean add(UserDto dto);

    /**
     * 更新用户昵称与角色
     *
     * @param dto 用户信息
     * @return 是否成功
     */
    boolean update(UserDto dto);

    /**
     * 重置用户密码
     *
     * @param dto 用户编号与新明文密码
     * @return 是否成功
     */
    boolean resetPassword(PasswordDto dto);

    /**
     * 删除用户
     *
     * @param id 用户编号
     * @return 是否成功
     */
    boolean delete(Long id);
}
