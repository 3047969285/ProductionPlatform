package com.devflow.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重置密码请求
 */
@Data
public class PasswordDto {

    @NotNull(message = "ID 不能为空")
    private Long id;

    @NotBlank(message = "密码不能为空")
    private String password;
}
