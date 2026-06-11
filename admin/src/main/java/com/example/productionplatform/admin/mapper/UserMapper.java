package com.example.productionplatform.admin.mapper;

import com.example.productionplatform.admin.model.SysUser;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, nickname, role, create_time AS createTime FROM sys_user WHERE username=#{username}")
    SysUser findByUsername(String username);

    @Select("SELECT id, username, nickname, role, create_time AS createTime FROM sys_user ORDER BY id")
    List<SysUser> findAll();

    @Select("SELECT id, username, nickname, role, create_time AS createTime FROM sys_user WHERE id=#{id}")
    SysUser findById(Long id);

    @Insert("INSERT INTO sys_user (username, password, nickname, role, create_time) VALUES (#{username}, #{password}, #{nickname}, #{role}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser user);

    @Update("UPDATE sys_user SET nickname=#{nickname}, role=#{role} WHERE id=#{id}")
    int update(SysUser user);

    @Update("UPDATE sys_user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM sys_user WHERE id=#{id}")
    int deleteById(Long id);
}
