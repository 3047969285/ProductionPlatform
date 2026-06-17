package com.devflow.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime createdAt;
}
