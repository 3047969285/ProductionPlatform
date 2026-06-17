package com.devflow.mapper;

import com.devflow.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问
 */
@Mapper
public interface UserMapper {

    /**
     * 按用户名查询含密码
     *
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 查询全部用户不含密码
     *
     * @return 用户列表
     */
    List<User> findAll();

    /**
     * 按编号查询不含密码
     *
     * @param id 用户编号
     * @return 用户实体
     */
    User findById(Long id);

    /**
     * 新增用户
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新昵称与角色
     *
     * @param user 用户实体
     * @return 影响行数
     */
    int update(User user);

    /**
     * 更新密码
     *
     * @param id 用户编号
     * @param password 哈希后的密码
     * @return 影响行数
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 删除用户
     *
     * @param id 用户编号
     * @return 影响行数
     */
    int deleteById(Long id);
}
