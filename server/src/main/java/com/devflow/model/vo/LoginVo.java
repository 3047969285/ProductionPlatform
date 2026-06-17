package com.devflow.model.vo;

import lombok.Data;

@Data
public class LoginVo {
    private String token;
    private UserVo user;
}
