package com.devflow.model.dto;

import com.devflow.common.validation.ValidGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户请求
 */
@Data
public class UserDto {

    @NotNull(message = "ID 不能为空", groups = ValidGroups.Update.class)
    private Long id;

    @NotBlank(message = "用户名不能为空", groups = ValidGroups.Create.class)
    private String username;

    @NotBlank(message = "密码不能为空", groups = ValidGroups.Create.class)
    private String password;

    private String nickname;
    private String role;
}
